package com.example.asan_sensor.activities

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
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
import com.example.asan_sensor.SensorSettingsLoader
import com.example.asan_sensor.StaticResources

class SensorSettingsActivity : AppCompatActivity(), View.OnClickListener {

    var loader: SensorSettingsLoader =
        SensorSettingsLoader()
    private lateinit var acchzButton: Button
    private lateinit var gyrohzButton: Button
    private lateinit var saveButton: Button
    private lateinit var resetButton: Button
    private var acchz:Int = 3
    private var gyrohz:Int = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sensor_settings)

        acchzButton = findViewById(R.id.sensors)
        acchzButton.text = StaticResources.acchz.toString()
        acchzButton.setOnClickListener(this)

        gyrohzButton = findViewById(R.id.hz)
        gyrohzButton.text = StaticResources.gyrohz.toString()
        gyrohzButton.setOnClickListener(this)

        saveButton = findViewById(R.id.save)
        saveButton.setOnClickListener(this)

        resetButton = findViewById(R.id.reset)
        resetButton.setOnClickListener(this)

        // Load saved values
        loader.getSensorSettings()
        acchz = StaticResources.acchz
        gyrohz = StaticResources.gyrohz

        // Apply saved values to buttons
        acchzButton.text = "가속도 센서 Hz : ${StaticResources.acchz.toString()}"
        gyrohzButton.text = "자이로 센서 Hz : ${StaticResources.gyrohz.toString()}"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.server -> {
                Log.d("SettingsMainActivity", "Settings 버튼이 클릭되었습니다.")
            }

            R.id.sensors -> {
                Log.d("SettingsMainActivity", "AccHz 버튼이 클릭되었습니다.")
                var text: TextView = TextView(this)
                text.setPadding(0, 100, 0, 0)
                text.setText("가속도 센서 Hz 변환 값 입력")
                val hzInputDialog = EditText(this)
                hzInputDialog.setHint("값을 입력해주세요.")
                hzInputDialog.setText(acchz.toString())
                hzInputDialog.setTextColor(Color.WHITE)
                hzInputDialog.setInputType(InputType.TYPE_CLASS_NUMBER);
                hzInputDialog.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE))
                val hzDialog = AlertDialog.Builder(this)
                    .setCustomTitle(text)
                    .setView(hzInputDialog)
                    .setPositiveButton("OK") { dialog, which ->
                        acchz = hzInputDialog.text.toString().toInt()
                        acchzButton.text = "가속도 센서 Hz : ${acchz.toString()}"
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
                var text: TextView = TextView(this)
                text.setPadding(0, 100, 0, 0)
                text.setText("자이로 센서 Hz 변환 값 입력")
                val hzInputDialog = EditText(this)
                hzInputDialog.setHint("값을 입력해주세요.")
                hzInputDialog.setText(gyrohz.toString())
                hzInputDialog.setTextColor(Color.WHITE)
                hzInputDialog.setInputType(InputType.TYPE_CLASS_NUMBER);
                hzInputDialog.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE))
                val hzDialog = AlertDialog.Builder(this)
                    .setCustomTitle(text)
                    .setTitle("자이로 센서 Hz 변환 값 입력")
                    .setView(hzInputDialog)
                    .setPositiveButton("OK") { dialog, which ->
                        gyrohz = hzInputDialog.text.toString().toInt()
                        gyrohzButton.text = "자이로 센서 Hz : ${gyrohz.toString()}"
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
                loader.putSensorAcchz(acchz)
                loader.putSensorGyro(gyrohz)
                loader.getSensorSettings()
                acchz = StaticResources.acchz
                gyrohz = StaticResources.gyrohz

                // Apply saved values to buttons
                acchzButton.text = "가속도 센서 Hz : ${StaticResources.acchz.toString()}"
                gyrohzButton.text = "자이로 센서 Hz : ${StaticResources.gyrohz.toString()}"
                Toast.makeText(this, "설정이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }

            R.id.reset -> {
                Log.d("SettingsMainActivity", "Reset 버튼이 클릭되었습니다.")
                // Handle reset button click
                loader.putSensorAcchz(3)
                loader.putSensorGyro(3)
                loader.getSensorSettings()
                acchz = StaticResources.acchz
                gyrohz = StaticResources.gyrohz

                // Apply saved values to buttons
                acchzButton.text = "가속도 센서 Hz : ${StaticResources.acchz.toString()}"
                gyrohzButton.text = "자이로 센서 Hz : ${StaticResources.gyrohz.toString()}"
                Toast.makeText(this, "설정이 초기화되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
