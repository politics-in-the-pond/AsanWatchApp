package com.example.asan_sensor.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.asan_sensor.R
import com.example.asan_sensor.socket.WebSocketStompClient

class UserMainActivity : AppCompatActivity() {

    private var webSocketStompClient: WebSocketStompClient? = null
    private var watchId = ""
    private var name = ""
    private var host = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_detail)

        if (intent != null && webSocketStompClient == null) {
            this.watchId = intent.getStringExtra("watchId").toString()
            this.name = intent.getStringExtra("name").toString()
            this.host = intent.getStringExtra("host").toString()
            webSocketStompClient = WebSocketStompClient.getInstance(watchId)
        }

        val userNameTextView : TextView = findViewById(R.id.userNameTextView)
        val watchIDTextView : TextView = findViewById(R.id.watchIDTextView)
        val userLocationTextView : TextView = findViewById(R.id.userLocationTextView)

        userNameTextView.text = name
        watchIDTextView.text = "Watch ID: $watchId"
        userLocationTextView.text = "병실: $host"
    }
}
