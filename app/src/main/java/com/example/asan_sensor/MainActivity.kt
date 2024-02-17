package com.example.asan_sensor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    //private var sensorService: SensorService = SensorService()
    private var isMeasuring: Boolean = false // 측정 중인지 여부를 나타내는 플래그 변수

    var sensorintent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sensorintent = Intent(this, SensorService::class.java)

        // 로고 버튼 클릭 이벤트 등록
        val logoButton: ImageButton = findViewById(R.id.settings)
        logoButton.setOnClickListener(this)

        // 사용자 정보 버튼 클릭 이벤트 등록
        val userButton: Button = findViewById(R.id.user)
        userButton.setOnClickListener(this)

        // 서버 정보 버튼 클릭 이벤트 등록
        val serverButton: Button = findViewById(R.id.server)
        serverButton.setOnClickListener(this)

        // 측정 시작 버튼 클릭 이벤트 등록
        val startButton: Button = findViewById(R.id.start)
        startButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.settings -> {
                if (!isMeasuring) { // 측정 중이 아닐 때만 설정 액티비티로 이동
                    Log.d("MainActivity", "로고 버튼이 클릭되었습니다.")
                    val intent = Intent(this, SettingsMainActivity::class.java)
                    startActivity(intent)
                }
            }

            R.id.user -> {
                if (!isMeasuring) { // 측정 중이 아닐 때만 사용자 정보 액티비티로 이동
                    Log.d("MainActivity", "사용자 정보 버튼이 클릭되었습니다.")
                    val intent = Intent(this, UserMainActivity::class.java)
                    startActivity(intent)
                }
            }

            R.id.server -> {
                if (!isMeasuring) { // 측정 중이 아닐 때만 서버 정보 액티비티로 이동
                    Log.d("MainActivity", "서버 정보 버튼이 클릭되었습니다.")
                    val intent = Intent(this, ServerMainActivity::class.java)
                    startActivity(intent)
                }
            }

            R.id.start -> {
                val startButton: Button = findViewById(R.id.start)
                if (isMeasuring) {
                    Log.d("MainActivity", "측정 중지 버튼이 클릭되었습니다.")
                    // 측정 중일 때 버튼을 누르면 측정 중지하고 버튼 텍스트를 "측정 시작"으로 변경
                    stopService(sensorintent)
                    Toast.makeText(this, "측정이 종료되었습니다.", Toast.LENGTH_SHORT).show()
                    startButton.text = "측정 시작"
                    isMeasuring = false // 측정 중이 아님을 표시
                } else {
                    Log.d("MainActivity", "측정 시작 버튼이 클릭되었습니다.")
                    // 측정 시작 버튼 클릭 시 텍스트 변경
                    startButton.text = "측정 중"

                    // 센서 정보 가져오기
                    val selectedSensors = SettingsMainActivity.SettingsManager.selectedSensors
                    // SensorService 시작

                    sensorintent?.action = "UPDATE_SENSORS"
                    sensorintent?.putStringArrayListExtra("selectedSensors", ArrayList(selectedSensors))
                    startService(sensorintent)

                    // 토스트 메시지로 출력
                    Toast.makeText(this, "측정을 시작합니다.", Toast.LENGTH_SHORT).show()
                    isMeasuring = true // 측정 중임을 표시
                }
            }
        }
    }
}