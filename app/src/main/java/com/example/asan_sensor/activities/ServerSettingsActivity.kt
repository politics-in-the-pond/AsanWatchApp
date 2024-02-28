package com.example.asan_sensor.activities

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType.TYPE_CLASS_NUMBER
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.asan_sensor.GeneralSettingsLoader
import com.example.asan_sensor.R
import com.example.asan_sensor.StaticResources

class ServerSettingsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var ipButton: Button
    private lateinit var portButton: Button
    private lateinit var saveButton: Button
    private lateinit var disconnectButton: Button
    private lateinit var passwordButton: Button
    var loader: GeneralSettingsLoader =
        GeneralSettingsLoader()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.server_settings)

        ipButton = findViewById(R.id.ip)
        ipButton.setOnClickListener(this)

        portButton = findViewById(R.id.port)
        portButton.setOnClickListener(this)

        saveButton = findViewById(R.id.save)
        saveButton.setOnClickListener(this)

        disconnectButton = findViewById(R.id.disconnect)
        disconnectButton.setOnClickListener(this)

        // Load server info from SharedPreferences
        loadSettings()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ip -> {
                Log.d("ServerMainActivity", "IP 버튼이 클릭되었습니다.")
                // Show dialog to input server IP address
                showInputDialog("서버 주소 입력", "주소", ipButton)
            }
            R.id.port -> {
                Log.d("ServerMainActivity", "Port 버튼이 클릭되었습니다.")
                // Show dialog to input server port number
                showInputNumericDialog("포트 번호 입력", "포트 번호", portButton)
            }
            R.id.save -> {
                Log.d("ServerMainActivity", "Save 버튼이 클릭되었습니다.")
                // Save server info to SharedPreferences
                saveSettings()
            }
            R.id.disconnect -> {
                Log.d("ServerMainActivity", "Disconnect 버튼이 클릭되었습니다.")
                // Reset server info and disconnect
                resetSettings()
                Toast.makeText(this, "서버 설정이 초기화되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showInputDialog(title: String, hint: String, button: Button) {
        val inputDialog = EditText(this)
        inputDialog.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE))
        inputDialog.setTextColor(Color.WHITE)
        inputDialog.setText(button.text)
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

    private fun showInputNumericDialog(title: String, hint: String, button: Button) {
        val inputDialog = EditText(this)
        inputDialog.setText(button.text)
        inputDialog.setInputType(TYPE_CLASS_NUMBER);
        inputDialog.setTextColor(Color.WHITE)
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

    private fun loadSettings() {
        loader.getGeneralSettings()
        Log.d("서버 정보 확인", "${StaticResources.ServerURL} : ${StaticResources.port}")

        ipButton.text = StaticResources.ServerURL
        portButton.text = StaticResources.port
    }

    private fun saveSettings() {
        val ipAddress = ipButton.text.toString()
        val portNumber = portButton.text.toString()

        // Save server info to SharedPreferences
        loader.putGeneralSettings(ipAddress, portNumber)
        loader.getGeneralSettings()

        Toast.makeText(this, "서버 정보가 저장되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }


    private fun resetSettings() {
        val ipAddress:String = "210.102.178.186"
        val port:String = "8080"
        ipButton.text = ipAddress // 기본값 설정
        portButton.text = port

        loader.putGeneralSettings(ipAddress, port)
        loader.getGeneralSettings()
    }
}
