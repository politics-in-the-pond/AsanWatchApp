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

class PasswordSettingsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var saveButton: Button
    private lateinit var disconnectButton: Button
    private lateinit var passwordButton: Button
    var loader: GeneralSettingsLoader =
        GeneralSettingsLoader()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.password_settings)

        saveButton = findViewById(R.id.save)
        saveButton.setOnClickListener(this)

        disconnectButton = findViewById(R.id.disconnect)
        disconnectButton.setOnClickListener(this)

        passwordButton = findViewById(R.id.pw)
        passwordButton.setOnClickListener(this)

        // Load server info from SharedPreferences
        loadSettings()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.pw -> {
                Log.d("ServerMainActivity", "Disconnect 버튼이 클릭되었습니다.")
                // Reset server info and disconnect
                showInputNumericDialog("비밀번호 입력", "비밀번호", passwordButton)
            }
            R.id.disconnect -> {
                Log.d("ServerMainActivity", "Disconnect 버튼이 클릭되었습니다.")
                // Reset server info and disconnect
                resetSettings()
                Toast.makeText(this, "비밀번호가 초기화되었습니다.", Toast.LENGTH_SHORT).show()
            }
            R.id.save -> {
                Log.d("ServerMainActivity", "Save 버튼이 클릭되었습니다.")
                // Save server info to SharedPreferences
                saveSettings()
            }
        }
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
        passwordButton.text = StaticResources.password
    }

    private fun saveSettings() {
        val pw = passwordButton.text.toString()

        // Save server info to SharedPreferences
        loader.putPassword(pw)
        loader.getGeneralSettings()
        Toast.makeText(this, "비밀번호가 저장되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun resetSettings() {
        val pw:String = "242424"
        passwordButton.text = pw

        loader.putPassword(pw)
        loader.getGeneralSettings()
    }
}
