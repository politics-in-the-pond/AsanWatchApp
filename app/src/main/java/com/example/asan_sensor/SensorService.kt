package com.example.asan_sensor

import android.app.Service
import android.content.Context
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
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class SensorService : Service(), SensorEventListener {

    private val MAX_BUFFER_SIZE = 102400
    private val TAG = "측정 중"
    private lateinit var sensorManager: SensorManager
    private lateinit var wifiManager: WifiManager
    private var isMeasuring: Boolean = false
    var serverIpAddress = ServerMainActivity.serverManager.serverIpAddress
    var serverPort: Int = ServerMainActivity.serverManager.serverPort
    private var selectedSensors = SettingsMainActivity.SettingsManager.selectedSensors

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
                val address = InetAddress.getByName(serverIpAddress)
                val socket = DatagramSocket()

                // Create JSON object based on sensor type
                val sensorDataJson = when (sensorType) {
                    Sensor.TYPE_HEART_RATE -> createHeartRateJson(sensorEvent)
                    Sensor.TYPE_ACCELEROMETER -> createAccelerometerJson(sensorEvent)
                    Sensor.TYPE_LIGHT -> createLightJson(sensorEvent)
                    Sensor.TYPE_GYROSCOPE -> createGyroscopeJson(sensorEvent)
                    Sensor.TYPE_PRESSURE -> createPressureJson(sensorEvent)
                    else -> null
                }

                sensorDataJson?.let { jsonData ->
                    val packet = DatagramPacket(jsonData.toByteArray(), jsonData.length, address, serverPort)
                    socket.send(packet)
                    socket.close()
                } ?: run {
                    Log.e(TAG, "Invalid sensor type.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun createHeartRateJson(sensorEvent: SensorEvent): String {
        val heartRate = sensorEvent.values[0].toInt() // Assuming heart rate is an integer
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        Log.d("잘 나오고 있니? 심박수", "Heart Rate: $heartRate")

        val jsonObject = JSONObject()
        jsonObject.put("messageType", "HEART_RATE")

        val dataJson = JSONObject()
        dataJson.put("value", heartRate)
        dataJson.put("timeStamp", timeStamp)

        jsonObject.put("data", dataJson)

        return jsonObject.toString()
    }

    private fun createAccelerometerJson(sensorEvent: SensorEvent): String {
        val xValue = sensorEvent.values[0]
        val yValue = sensorEvent.values[1]
        val zValue = sensorEvent.values[2]
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        Log.d("잘 나오고 있니? 가속도 센서", "X: $xValue, Y: $yValue, Z: $zValue")

        val jsonObject = JSONObject()
        jsonObject.put("messageType", "ACCELEROMETER")

        val dataJson = JSONObject()
        dataJson.put("xValue", xValue)
        dataJson.put("yValue", yValue)
        dataJson.put("zValue", zValue)
        dataJson.put("timeStamp", timeStamp)

        jsonObject.put("data", dataJson)

        return jsonObject.toString()
    }

    private fun createLightJson(sensorEvent: SensorEvent): String {
        val light = sensorEvent.values[0].toInt()
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        Log.d("잘 나오고 있니? : 조도센서", "$light")

        val jsonObject = JSONObject()
        jsonObject.put("messageType", "LIGHT")

        val dataJson = JSONObject()
        dataJson.put("value", light)
        dataJson.put("timeStamp", timeStamp)

        jsonObject.put("data", dataJson)

        return jsonObject.toString()
    }

    private fun createGyroscopeJson(sensorEvent: SensorEvent): String {
        val xValue = sensorEvent.values[0]
        val yValue = sensorEvent.values[1]
        val zValue = sensorEvent.values[2]
        Log.d("잘 나오고 있니? 자이로스코프", "$xValue, $yValue, $zValue")
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val jsonObject = JSONObject()
        jsonObject.put("messageType", "GYROSCOPE")

        val dataJson = JSONObject()
        dataJson.put("xValue", xValue)
        dataJson.put("yValue", yValue)
        dataJson.put("zValue", zValue)
        dataJson.put("timeStamp", timeStamp)

        jsonObject.put("data", dataJson)

        return jsonObject.toString()
    }

    private fun createPressureJson(sensorEvent: SensorEvent): String {
        val pressure = sensorEvent.values
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        Log.d("잘 나오고 있니? : 기압센서", "$pressure")

        val jsonObject = JSONObject()
        jsonObject.put("messageType", "PRESSURE")

        val dataJson = JSONObject()
        dataJson.put("value", pressure)
        dataJson.put("timeStamp", timeStamp)

        jsonObject.put("data", dataJson)

        return jsonObject.toString()
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