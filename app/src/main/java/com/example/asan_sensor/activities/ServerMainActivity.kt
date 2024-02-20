package com.example.asan_sensor.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.asan_sensor.R
import com.example.asan_sensor.SensorService

class ServerMainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var ipButton: Button
    private lateinit var portButton: Button
    private lateinit var saveButton: Button
    private lateinit var disconnectButton: Button

    object serverManager{
        var serverIpAddress = "210.102.172.186"
        var serverPort: Int = 8080
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.server_main)

        ipButton = findViewById(R.id.ip)
        ipButton.setOnClickListener(this)

        portButton = findViewById(R.id.port)
        portButton.setOnClickListener(this)

        saveButton = findViewById(R.id.save)
        saveButton.setOnClickListener(this)

        disconnectButton = findViewById(R.id.disconnect)
        disconnectButton.setOnClickListener(this)

        // Load server info from SharedPreferences
        loadServerInfo()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ip -> {
                Log.d("ServerMainActivity", "IP 버튼이 클릭되었습니다.")
                // Show dialog to input server IP address
                showInputDialog("서버 IP 주소 입력", "IP 주소", ipButton)
            }
            R.id.port -> {
                Log.d("ServerMainActivity", "Port 버튼이 클릭되었습니다.")
                // Show dialog to input server port number
                showInputDialog("포트 번호 입력", "포트 번호", portButton)
            }
            R.id.save -> {
                Log.d("ServerMainActivity", "Save 버튼이 클릭되었습니다.")
                // Save server info to SharedPreferences
                saveServerInfo()
                Toast.makeText(this, "서버 정보가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            R.id.disconnect -> {
                Log.d("ServerMainActivity", "Disconnect 버튼이 클릭되었습니다.")
                // Reset server info and disconnect
                resetServerInfo()
                Toast.makeText(this, "연결이 해제되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showInputDialog(title: String, hint: String, button: Button) {
        val inputDialog = EditText(this)
        inputDialog.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE))
        var text: TextView = TextView(this)
        text.setPadding(0, 100, 0, 0)
        text.setText(title)
        val dialog = AlertDialog.Builder(this)
            .setCustomTitle(text)
            .setView(inputDialog)
            .setPositiveButton("확인") { _, _ ->
                val input = inputDialog.text.toString()
                button.text = input
            }
            .setNegativeButton("취소", null)
            .create()

        // Adjust dialog width
        dialog.show()
        val width = resources.displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = (width * 0.75).toInt()
        dialog.window?.attributes = layoutParams
    }

    private fun loadServerInfo() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        serverManager.serverIpAddress = preferences.getString("serverIpAddress", "-") ?: ""
        serverManager.serverPort = preferences.getInt("serverPort", 0)
        Log.d("서버 정보 확인", "${serverManager.serverIpAddress} : ${serverManager.serverPort}")

        ipButton.text = serverManager.serverIpAddress
        portButton.text = serverManager.serverPort.toString()
    }

    private fun saveServerInfo() {
        val ipAddress = ipButton.text.toString()
        val portNumber = portButton.text.toString()

        // Save server info to SharedPreferences
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        editor.putString("serverIpAddress", ipAddress)
        editor.putInt("serverPort", portNumber.toInt())
        editor.apply()

        // Send selected sensors to SensorService
        sendServerInfoToService(ipAddress, portNumber.toInt())

        Toast.makeText(this, "서버 정보가 저장되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun sendServerInfoToService(ipAddress: String, port: Int) {
        val intent = Intent(this, SensorService::class.java)
        intent.action = "UPDATE_SERVER_INFO"
        intent.putExtra("serverIpAddress", ipAddress)
        intent.putExtra("serverPort", port)
    }

    private fun resetServerInfo() {
        ipButton.text = serverManager.serverIpAddress // 기본값 설정
        portButton.text = serverManager.serverPort.toString() // 기본값 설정
        serverManager.serverIpAddress = "210.102.172.186"
        serverManager.serverPort = 8080

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        editor.putString("serverIpAddress", serverManager.serverIpAddress) // 기본값 설정
        editor.putInt("serverPort", serverManager.serverPort) // 기본값 설정
        editor.apply()
    }
}
