package com.example.asan_sensor

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import com.example.asan_sensor.activities.ServerMainActivity
import com.example.asan_sensor.activities.SettingsMainActivity
import com.example.asan_sensor.socket.WebSocketStompClient
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SensorService : Service(), SensorEventListener {

    private val MAX_BUFFER_SIZE = 102400
    private val TAG = "측정 중"
    private lateinit var sensorManager: SensorManager
    private lateinit var wifiManager: WifiManager
    private var isMeasuring: Boolean = false
    var serverIpAddress = ServerMainActivity.serverManager.serverIpAddress
    var serverPort: Int = ServerMainActivity.serverManager.serverPort
    private var selectedSensors = SettingsMainActivity.SettingsManager.selectedSensors
    private var webSocketStompClient: WebSocketStompClient? = null
    private var watchId = ""

    data class HeartRateData(val value: Int, val timeStamp: String)
    data class AccelerometerData(val xValue: Float, val yValue: Float, val zValue: Float, val timeStamp: String)
    data class LightData(val value: Int, val timeStamp: String)
    data class GyroscopeData(val xValue: Float, val yValue: Float, val zValue: Float, val timeStamp: String)
    data class PressureData(val value: Float, val timeStamp: String)


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && webSocketStompClient == null) {
            this.watchId = intent.getStringExtra("watchId").toString();
            webSocketStompClient = WebSocketStompClient.getInstance(watchId)
        }

        startSensorMeasurement()
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val sensorName = getSensorName(event.sensor.type)
            val sensorType = event.sensor.type
            val data = event.values
            val sensorData = processSensorData(event)

            if (sensorData != null) {
                logSensorData(sensorName, data)
                sendData(event, sensorType)
            } else {
                Log.e(TAG, "Failed to process sensor data.")
            }
        }
    }

    private fun getSensorName(sensorType: Int): String {
        return when (sensorType) {
            Sensor.TYPE_HEART_RATE -> "심박수"
            Sensor.TYPE_ACCELEROMETER -> "가속도 센서"
            Sensor.TYPE_LIGHT -> "광센서"
            Sensor.TYPE_GYROSCOPE -> "자이로 센서"
            Sensor.TYPE_PRESSURE -> "바로미터 센서"
            else -> "알 수 없는 센서"
        }
    }

    private fun logSensorData(sensorName: String, sensorData: FloatArray) {
        var sensorValue: String = ""

        sensorValue = when (sensorName) {
            "심박수", "광센서", "바로미터 센서" -> {
                // For sensors like heart rate, light, and pressure, we read a single int value
                val sensorIntValue = sensorData[0]
                sensorIntValue.toString()
            }
            "가속도 센서", "자이로 센서" -> {
                // For sensors like accelerometer and gyroscope, we read 3 float values (x, y, z)
                val xValue = sensorData[0]
                val yValue = sensorData[1]
                val zValue = sensorData[2]
                "X: $xValue, Y: $yValue, Z: $zValue"
            }
            else -> {
                // For other sensor types, we simply convert the ByteBuffer to string
                sensorData.toString()
            }
        }

        // Log the sensor data
        Log.d(TAG, "Sensor: $sensorName, Data: $sensorValue")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not implemented yet
    }

    private fun processSensorData(event: SensorEvent): ByteBuffer? {
        val sensorType = event.sensor.type
        val timestamp = event.timestamp
        val data = event.values

        // Calculate the total size needed for the ByteBuffer
        val bufferSize = 4 + 8 + data.size * 4 // 4 for sensorType, 8 for timestamp, 4 for each float value

        if (bufferSize > MAX_BUFFER_SIZE) {
            Log.e(TAG, "Buffer size exceeds maximum limit.")
            return null
        }

        val byteBuffer = ByteBuffer.allocate(bufferSize)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.putInt(sensorType)
        byteBuffer.putLong(timestamp)

        for (value in data) {
            byteBuffer.putFloat(value)
        }

        byteBuffer.position(0)
        return byteBuffer
    }

    private fun sendData(sensorEvent: SensorEvent, sensorType: Int) {
        Thread {
            try {
                // Create data objects based on sensor type
                val data = when (sensorType) {
                    Sensor.TYPE_HEART_RATE -> {
                        val result_json = JSONObject()
                        result_json.put("value", sensorEvent.values[0])
                        result_json.put("yyyy-MM-dd HH:mm:ss", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                        webSocketStompClient?.sendHeartrate(result_json)
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        val result_json = JSONObject()
                        result_json.put("xValue", sensorEvent.values[0])
                        result_json.put("yValue", sensorEvent.values[1])
                        result_json.put("zValue", sensorEvent.values[2])
                        result_json.put("yyyy-MM-dd HH:mm:ss", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                        webSocketStompClient?.sendAccelerometer(result_json)
                    }

                    Sensor.TYPE_LIGHT -> {
                        val result_json = JSONObject()
                        result_json.put("value", sensorEvent.values[0])
                        result_json.put("yyyy-MM-dd HH:mm:ss", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                        webSocketStompClient?.sendLight(result_json)
                    }

                    Sensor.TYPE_GYROSCOPE -> {
                        val result_json = JSONObject()
                        result_json.put("xValue", sensorEvent.values[0])
                        result_json.put("yValue", sensorEvent.values[1])
                        result_json.put("zValue", sensorEvent.values[2])
                        result_json.put("yyyy-MM-dd HH:mm:ss", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                        webSocketStompClient?.sendGyroscope(result_json)
                    }

                    Sensor.TYPE_PRESSURE -> {
                        val result_json = JSONObject()
                        result_json.put("value", sensorEvent.values[0])
                        result_json.put("yyyy-MM-dd HH:mm:ss", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                        webSocketStompClient?.sendPressure(result_json)
                    }

                    else -> null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun startSensorMeasurement() {
        if (!isMeasuring && selectedSensors.isNotEmpty()) {
            selectedSensors.forEach { sensorType ->
                val sensorTypeInt: Int = when (sensorType) {
                    "심박수" -> Sensor.TYPE_HEART_RATE
                    "가속도 센서" -> Sensor.TYPE_ACCELEROMETER
                    "광센서" -> Sensor.TYPE_LIGHT
                    "자이로 센서" -> Sensor.TYPE_GYROSCOPE
                    "바로미터 센서" -> Sensor.TYPE_PRESSURE
                    else -> -1
                }

                // Check if the sensor type is valid
                if (sensorTypeInt != -1) {
                    val sensor: Sensor? = sensorManager.getDefaultSensor(sensorTypeInt)
                    sensor?.let {
                        // Register listener for the sensor
                        val samplingRateMsAcc = 1000 / SettingsMainActivity.SettingsManager.hzValueAccelerometer.toInt() // Hz를 ms로 변환
                        val samplingRateMsGyro = 1000 / SettingsMainActivity.SettingsManager.hzValueGyroscope.toInt()
                        // startSensorMeasurement() 메서드 내에서 각 센서의 측정 속도를 설정할 때, 가속도 센서와 자이로 센서에 대한 Hz 값을 사용하도록 변경합니다.
                        sensorManager.registerListener(
                            this,
                            it,
                            when (sensorType) {
                                "가속도 센서" -> {
                                    samplingRateMsAcc
                                }
                                "자이로 센서" -> {
                                    samplingRateMsGyro
                                }
                                else -> SensorManager.SENSOR_DELAY_FASTEST
                            }
                        )

                        Log.d("Success", "$sensorType 측정을 시작합니다.")
                    } ?: run {
                        Log.d("Failure 1", "$sensorType 센서를 찾을 수 없습니다.")
                    }
                } else {
                    Log.d("Failure 2", "지원되지 않는 센서 타입입니다.")
                }
            }
            isMeasuring = true
        }
    }

    fun stopSensorMeasurement() {
        if (isMeasuring) {
            selectedSensors.forEach { sensorType ->
                val sensorTypeInt: Int = when (sensorType) {
                    "심박수" -> Sensor.TYPE_HEART_RATE
                    "가속도 센서" -> Sensor.TYPE_ACCELEROMETER
                    "광센서" -> Sensor.TYPE_LIGHT
                    "자이로 센서" -> Sensor.TYPE_GYROSCOPE
                    "바로미터 센서" -> Sensor.TYPE_PRESSURE
                    else -> -1
                }

                // Check if the sensor type is valid
                if (sensorTypeInt != -1) {
                    val sensor: Sensor? = sensorManager.getDefaultSensor(sensorTypeInt)
                    sensor?.let {
                        // Unregister listener for the sensor
                        sensorManager.unregisterListener(this, it)
                        Log.d("Finished", "$sensorType 측정이 종료되었습니다.")
                    } ?: run {
                        Log.d("Failure", "$sensorType 센서를 찾을 수 없습니다.")
                    }
                } else {
                    Log.d("Failure", "지원되지 않는 센서 타입입니다.")
                }
                isMeasuring = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSensorMeasurement()
    }
}