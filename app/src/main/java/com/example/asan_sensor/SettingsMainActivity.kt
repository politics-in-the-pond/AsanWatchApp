package com.example.asan_sensor

import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class SettingsMainActivity : AppCompatActivity(), View.OnClickListener {

    object SettingsManager {
        var hzValueAccelerometer: String = SensorManager.SENSOR_DELAY_NORMAL.toString()
        var hzValueGyroscope: String = SensorManager.SENSOR_DELAY_NORMAL.toString()

        var selectedSensors: MutableList<String> = mutableListOf(
            "심박수", "광센서", "가속도 센서", "자이로 센서", "바로미터 센서"
        )
    }

    private lateinit var acchzButton: Button
    private lateinit var gyrohzButton: Button
    private lateinit var saveButton: Button
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_main)

        val settingsButton: ImageButton = findViewById(R.id.settings)
        settingsButton.setOnClickListener(this)

        acchzButton = findViewById(R.id.sensors)
        acchzButton.text = "가속도 센서 Hz : ${SettingsManager.hzValueAccelerometer}"
        acchzButton.setOnClickListener(this)

        gyrohzButton = findViewById(R.id.hz)
        gyrohzButton.text = "자이로 센서 Hz : ${SettingsManager.hzValueGyroscope}"
        gyrohzButton.setOnClickListener(this)

        saveButton = findViewById(R.id.save)
        saveButton.setOnClickListener(this)

        resetButton = findViewById(R.id.reset)
        resetButton.setOnClickListener(this)

        // Load saved values
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        SettingsManager.hzValueAccelerometer = preferences.getString("hzValueAccelrometer", "3")!!
        SettingsManager.hzValueGyroscope = preferences.getString("hzValueGyroscope", "3")!!

        // Apply saved values to buttons
        acchzButton.text = "가속도 센서 Hz : ${SettingsManager.hzValueAccelerometer}"
        gyrohzButton.text = "자이로 센서 Hz : ${SettingsManager.hzValueGyroscope}"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.settings -> {
                Log.d("SettingsMainActivity", "Settings 버튼이 클릭되었습니다.")
            }

            R.id.sensors -> {
                Log.d("SettingsMainActivity", "AccHz 버튼이 클릭되었습니다.")
                val hzInputDialog = EditText(this)
                val hzDialog = AlertDialog.Builder(this)
                    .setTitle("가속도 센서 Hz 변환 값 입력")
                    .setView(hzInputDialog)
                    .setPositiveButton("OK") { dialog, which ->
                        var hzValueAccelrometer = hzInputDialog.text.toString()
                        SettingsManager.hzValueAccelerometer = hzValueAccelrometer // 가속도 센서 값만 변경
                        acchzButton.text = "가속도 센서 Hz : $hzValueAccelrometer"
                    }
                    .setNegativeButton("Cancel", null)
                    .create()

                // Resize AlertDialog
                hzDialog.show()
                val width = resources.displayMetrics.widthPixels
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.copyFrom(hzDialog.window?.attributes)
                layoutParams.width = (width * 0.75).toInt() // Set width to 80% of screen
                hzDialog.window?.attributes = layoutParams
            }

            R.id.hz -> {
                Log.d("SettingsMainActivity", "GyroHz 버튼이 클릭되었습니다.")
                val hzInputDialog = EditText(this)
                val hzDialog = AlertDialog.Builder(this)
                    .setTitle("자이로 센서 Hz 변환 값 입력")
                    .setView(hzInputDialog)
                    .setPositiveButton("OK") { dialog, which ->
                        val hzValueGyroscope = hzInputDialog.text.toString()
                        SettingsManager.hzValueGyroscope = hzValueGyroscope
                        gyrohzButton.text = "자이로 센서 Hz : $hzValueGyroscope"
                    }
                    .setNegativeButton("Cancel", null)
                    .create()

                // Resize AlertDialog
                hzDialog.show()
                val width = resources.displayMetrics.widthPixels
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.copyFrom(hzDialog.window?.attributes)
                layoutParams.width = (width * 0.75).toInt() // Set width to 80% of screen
                hzDialog.window?.attributes = layoutParams
            }

            R.id.save -> {
                Log.d("SettingsMainActivity", "Save 버튼이 클릭되었습니다.")
                // Handle save button click
                val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                preferences.edit {
                    putString("hzValueAccelerometer", SettingsManager.hzValueAccelerometer)
                    putString("hzValueGyroscope", SettingsManager.hzValueGyroscope)
                }
                // Send selected sensors to SensorService
                sendSelectedSensorsToService()
                Toast.makeText(this, "설정이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }

            R.id.reset -> {
                Log.d("SettingsMainActivity", "Reset 버튼이 클릭되었습니다.")
                // Handle reset button click
                SettingsManager.hzValueAccelerometer = SensorManager.SENSOR_DELAY_NORMAL.toString()
                SettingsManager.hzValueGyroscope = SensorManager.SENSOR_DELAY_NORMAL.toString()
                acchzButton.text = "가속도 센서 Hz : ${SensorManager.SENSOR_DELAY_NORMAL}"
                gyrohzButton.text = "자이로 센서 Hz : ${SensorManager.SENSOR_DELAY_NORMAL}"
                // Reset saved values
                val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                preferences.edit {
                    putString("hzValueAccelerometer", SettingsManager.hzValueAccelerometer)
                    putString("hzValueGyroscope", SettingsManager.hzValueGyroscope)
                }
                Toast.makeText(this, "설정이 초기화되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendSelectedSensorsToService() {
        if (SettingsManager.selectedSensors.isNotEmpty()) {
            val intent = Intent(this, SensorService::class.java)
            intent.action = "UPDATE_SENSORS"
            intent.putStringArrayListExtra("selectedSensors", ArrayList(SettingsManager.selectedSensors))
            Log.d("Send Sensor Info", "선택된 센서 목록을 서비스로 전달합니다: ${SettingsManager.selectedSensors}")
        } else {
            Log.d("Sensor Info Failure", "선택된 센서가 없습니다. 측정을 시작할 수 없습니다.")
            Toast.makeText(this, "선택된 센서가 없습니다. 측정을 시작할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
