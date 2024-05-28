package com.example.asan_sensor

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import com.example.asan_sensor.socket.WebSocketStompClient
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Date


class SensorService : Service(), SensorEventListener {

    private val MAX_BUFFER_SIZE = 102400
    private val TAG = "측정 중"
    private lateinit var sensorManager: SensorManager
    private lateinit var wifiManager: WifiManager
    private var isMeasuring: Boolean = false
    private var webSocketStompClient: WebSocketStompClient? = null
    private var watchId = ""
    private var acccheck = 0
    private var gyrocheck = 0

    // Separate threads for each sensor type
    private val heartRateThread = HandlerThread("HeartRateThread")
    private val accelerometerThread = HandlerThread("AccelerometerThread")
    private val lightThread = HandlerThread("LightThread")
    private val gyroscopeThread = HandlerThread("GyroscopeThread")
    private val pressureThread = HandlerThread("PressureThread")

    private lateinit var heartRateHandler: Handler
    private lateinit var accelerometerHandler: Handler
    private lateinit var lightHandler: Handler
    private lateinit var gyroscopeHandler: Handler
    private lateinit var pressureHandler: Handler

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        // Start handler threads
        heartRateThread.start()
        accelerometerThread.start()
        lightThread.start()
        gyroscopeThread.start()
        pressureThread.start()

        // Initialize handlers
        heartRateHandler = Handler(heartRateThread.looper)
        accelerometerHandler = Handler(accelerometerThread.looper)
        lightHandler = Handler(lightThread.looper)
        gyroscopeHandler = Handler(gyroscopeThread.looper)
        pressureHandler = Handler(pressureThread.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && webSocketStompClient == null) {
            this.watchId = intent.getStringExtra("watchId").toString()
            webSocketStompClient = WebSocketStompClient.getInstance(watchId)
        }

        startSensorMeasurement()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSensorMeasurement()
        heartRateThread.quitSafely()
        accelerometerThread.quitSafely()
        lightThread.quitSafely()
        gyroscopeThread.quitSafely()
        pressureThread.quitSafely()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val sensorType = event.sensor.type
            val sensorData = processSensorData(event)
//            Log.d("LOG", "$sensorType 측정")
            sensorData?.let {
                when (sensorType) {
                    Sensor.TYPE_HEART_RATE -> heartRateHandler.post { sendData(event, sensorType) }
                    Sensor.TYPE_ACCELEROMETER -> accelerometerHandler.post { sendData(event, sensorType) }
                    Sensor.TYPE_LIGHT -> lightHandler.post { sendData(event, sensorType) }
                    Sensor.TYPE_GYROSCOPE -> gyroscopeHandler.post { sendData(event, sensorType) }
                    Sensor.TYPE_PRESSURE -> pressureHandler.post { sendData(event, sensorType) }
                    else -> {}
                }
            } ?: Log.e(TAG, "Failed to process sensor data.")
        }
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
        try {
            // Create data objects based on sensor type
            when (sensorType) {
                Sensor.TYPE_HEART_RATE -> {
                    val result_json = JSONObject()
                    result_json.put("value", sensorEvent.values[0])
                    result_json.put("timeStamp", System.currentTimeMillis())
                    webSocketStompClient?.sendHeartrate(result_json)
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    acccheck += 1
                    acccheck %= 10000
                    if (acccheck % 2 == 0) {
                        val result_json = JSONObject()
                        result_json.put("accX", sensorEvent.values[0])
                        result_json.put("accY", sensorEvent.values[1])
                        result_json.put("accZ", sensorEvent.values[2])
                        result_json.put("timeStamp", System.currentTimeMillis())
                        webSocketStompClient?.sendAccelerometer(result_json)
                    }
                }

                Sensor.TYPE_LIGHT -> {
                    val result_json = JSONObject()
                    result_json.put("value", sensorEvent.values[0])
                    result_json.put("timeStamp", System.currentTimeMillis())
                    webSocketStompClient?.sendLight(result_json)
                }

                Sensor.TYPE_GYROSCOPE -> {
                    gyrocheck += 1
                    gyrocheck %= 10000
                    if (gyrocheck % 2 == 0) {
                        val result_json = JSONObject()
                        result_json.put("gyroX", sensorEvent.values[0])
                        result_json.put("gyroY", sensorEvent.values[1])
                        result_json.put("gyroZ", sensorEvent.values[2])
                        result_json.put("timeStamp", System.currentTimeMillis())
                        webSocketStompClient?.sendGyroscope(result_json)
                    }
                }

                Sensor.TYPE_PRESSURE -> {
                    val result_json = JSONObject()
                    result_json.put("value", sensorEvent.values[0])
                    result_json.put("timeStamp", System.currentTimeMillis())
                    webSocketStompClient?.sendPressure(result_json)
                }

                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startSensorMeasurement() {
        val sensorTypes = arrayOf(
            Sensor.TYPE_HEART_RATE,
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_PRESSURE
        )
        if (!isMeasuring) {
            for (sensorTypeInt: Int in sensorTypes) {
                // Check if the sensor type is valid
                if (sensorTypeInt != -1) {
                    val sensor: Sensor? = sensorManager.getDefaultSensor(sensorTypeInt)
                    sensor?.let {
                        // Register listener for the sensor
//                        val samplingRateMsAcc = 1000 / StaticResources.acchz // Hz를 ms로 변환
//                        val samplingRateMsGyro = 1000 / StaticResources.gyrohz
                        // startSensorMeasurement() 메서드 내에서 각 센서의 측정 속도를 설정할 때, 가속도 센서와 자이로 센서에 대한 Hz 값을 사용하도록 변경합니다.
                        sensorManager.registerListener(
                            this,
                            it,
                            when (sensorTypeInt) {
//                                Sensor.TYPE_HEART_RATE -> {
//                                    300000 // 1Hz for heart rate
//                                }

                                Sensor.TYPE_ACCELEROMETER -> {
                                    1000000 / StaticResources.acchz

                                    //30000000
                                }
                                Sensor.TYPE_GYROSCOPE -> {
                                    1000000 / StaticResources.acchz
//                                    30000000
                                }
                                else -> 1000000 / 3  //SensorManager.SENSOR_DELAY_NORMAL
                            }
                        )
                    } ?: run {

                    }
                } else {
                    Log.d("Failure 2", "지원되지 않는 센서 타입입니다.")
                }
            }
            isMeasuring = true
        }
    }

    fun stopSensorMeasurement() {
        val sensorTypes = arrayOf(
            Sensor.TYPE_HEART_RATE,
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_PRESSURE
        )
        if (isMeasuring) {
            for (sensorTypeInt: Int in sensorTypes) {
                // Check if the sensor type is valid
                if (sensorTypeInt != -1) {
                    val sensor: Sensor? = sensorManager.getDefaultSensor(sensorTypeInt)
                    sensor?.let {
                        // Unregister listener for the sensor
                        sensorManager.unregisterListener(this, it)
                    } ?: run {
                    }
                } else {
                    Log.d("Failure", "지원되지 않는 센서 타입입니다.")
                }
                isMeasuring = false
            }
        }
    }
}