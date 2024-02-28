package com.example.asan_sensor.activities

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.asan_sensor.R
import com.example.asan_sensor.StaticResources
import com.example.asan_sensor.socket.WebSocketStompClient
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class UserMainActivity : AppCompatActivity() {

    private var webSocketStompClient: WebSocketStompClient? = null
    private var watchId = ""
    private var name = ""
    private var host = ""
    lateinit var userNameTextView : TextView
    lateinit var watchIDTextView : TextView
    lateinit var userLocationTextView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_detail)

        userNameTextView = findViewById(R.id.userNameTextView)
        watchIDTextView = findViewById(R.id.watchIDTextView)
        userLocationTextView = findViewById(R.id.userLocationTextView)

        getUserInfo()
    }

    fun getUserInfo() {
        val URL = StaticResources.getHttpURL() + "api/watch/" + StaticResources.deviceID
        Log.d("url", URL)
        val json_object = JSONObject()
        try {
            json_object.put("androidId", StaticResources.deviceID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val requestQueue = Volley.newRequestQueue(
            applicationContext
        )
        val mRequestBody: String = json_object.toString()

        val stringRequest: StringRequest = object : StringRequest(
            Method.GET, URL,
            Response.Listener<String> { response: String? ->
                try {
                    val jsonResponse = JSONObject(response)
                    val state = jsonResponse.getInt("status")
                    if (state == 200) {
                        watchId = jsonResponse.getJSONObject("data").getString("watchId")
                        name = jsonResponse.getJSONObject("data").getString("name")
                        host = jsonResponse.getJSONObject("data").getString("host")
                        userNameTextView.text = name
                        watchIDTextView.text = "Watch ID: $watchId"
                        userLocationTextView.text = "병실: $host"
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error: VolleyError ->
                Log.e("NetworkError", error.toString())
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return mRequestBody.toByteArray(StandardCharsets.UTF_8)
            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                var responseString = ""
                if (response != null) {
                    responseString = String(response.data, StandardCharsets.UTF_8)
                }
                return Response.success(
                    responseString,
                    HttpHeaderParser.parseCacheHeaders(response)
                )
            }
        }

        requestQueue.add(stringRequest)
    }
}
