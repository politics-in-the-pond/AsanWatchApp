package com.example.asan_sensor.activities

import android.app.ActivityManager
import android.app.ActivityManager.RunningServiceInfo
import android.content.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.asan_sensor.Ping
import com.example.asan_sensor.R
import com.example.asan_sensor.SensorService
import com.example.asan_sensor.StaticResources

class MenuActivity : AppCompatActivity(), View.OnClickListener {

    private var isMeasuring: Boolean = false // 측정 중인지 여부를 나타내는 플래그 변수

    var sensorintent: Intent? = null
    lateinit var startButton: Button

    private var wifiConnected: Boolean = true
    private var serverConnected: Boolean = true

    private val connectivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!isNetworkConnected()) {
                if (isMeasuring) {
                    stopMeasurement()
                    Toast.makeText(this@MenuActivity, "와이파이 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sensorintent = Intent(this, SensorService::class.java)

        // 로고 버튼 클릭 이벤트 등록
        val logoButton: Button = findViewById(R.id.settings)
        logoButton.setOnClickListener(this)

        // 사용자 정보 버튼 클릭 이벤트 등록
        val userButton: Button = findViewById(R.id.user)
        userButton.setOnClickListener(this)

        // 서버 정보 버튼 클릭 이벤트 등록
        val serverButton: Button = findViewById(R.id.server)
        serverButton.setOnClickListener(this)

        // 측정 시작 버튼 클릭 이벤트 등록
        startButton = findViewById(R.id.start)
        startButton.setOnClickListener(this)

        // Register the connectivity receiver
        registerReceiver(connectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the connectivity receiver
        unregisterReceiver(connectivityReceiver)
    }

    override fun onResume() {
        super.onResume()
        val am: ActivityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (rsi: RunningServiceInfo in am.getRunningServices(Integer.MAX_VALUE)) {
            if (SensorService::class.java.name == rsi.service.className) {
                isMeasuring = true
                startButton.text = "측정 중"
                startButton.setBackground(resources.getDrawable(R.drawable.rounded_button))
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.settings -> {
                if (!isMeasuring) { // 측정 중이 아닐 때만 설정 액티비티로 이동
                    Log.d("MainActivity", "로고 버튼이 클릭되었습니다.")
                    val intent = Intent(this, SensorSettingsActivity::class.java)
                    startActivity(intent)
                }
            }

            R.id.user -> {
                Log.d("MainActivity", "사용자 정보 버튼이 클릭되었습니다.")
                val intent = Intent(this, UserMainActivity::class.java)
                startActivity(intent)
            }

            R.id.server -> {
                if (!isMeasuring) { // 측정 중이 아닐 때만 서버 정보 액티비티로 이동
                    Log.d("MainActivity", "서버 정보 버튼이 클릭되었습니다.")
                    val intent = Intent(this, GeneralSettingsActivity::class.java)
                    startActivity(intent)
                }
            }

            R.id.start -> {
                if (isMeasuring) {
                    Log.d("MainActivity", "측정 중지 버튼이 클릭되었습니다.")
                    // 측정 중일 때 버튼을 누르면 측정 중지하고 버튼 텍스트를 "측정 시작"으로 변경
                    stopMeasurement()
                } else {
                   // serverTest()
                 //   wifiTest()
                    if (!wifiConnected) {
                        Toast.makeText(this, "와이파이 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
                    } else if (!serverConnected) {
                        Toast.makeText(this, "서버 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("MainActivity", "측정 시작 버튼이 클릭되었습니다.")
                        startMeasurement()
                    }
                }
            }
        }
    }

    private fun startMeasurement() {
        startButton.text = "측정 중"
        startButton.setBackground(resources.getDrawable(R.drawable.measure_button))
        sensorintent?.action = "UPDATE_SENSORS"
        startService(sensorintent)
        Toast.makeText(this, "측정을 시작합니다.", Toast.LENGTH_SHORT).show()
        isMeasuring = true
    }

    private fun stopMeasurement() {
        stopService(sensorintent)
        Toast.makeText(this, "측정이 종료되었습니다.", Toast.LENGTH_SHORT).show()
        startButton.text = "측정 시작"
        startButton.setBackground(resources.getDrawable(R.drawable.rounded_button))
        isMeasuring = false
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}