package com.example.asan_sensor.activities;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.asan_sensor.Ping;
import com.example.asan_sensor.R;
import com.example.asan_sensor.GeneralSettingsLoader;
import com.example.asan_sensor.SensorSettingsLoader;
import com.example.asan_sensor.StaticResources;
import com.example.asan_sensor.WatchForegroundService;
import com.example.asan_sensor.databinding.ActivityMaintempBinding;
import com.example.asan_sensor.dto.DeviceInfo;
import com.example.asan_sensor.socket.WebSocketStompClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private TextView deviceidText;
    private ActivityMaintempBinding binding;
    private Intent foregroundService;
    private ImageView setting;
    private ImageView network;
    private ImageView server;
    private ImageView bt;

    private Handler handler;

    private Runnable networkCheckRunnable;
    private Runnable serverCheckRunnable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StaticResources.maincontext = getApplicationContext();

        UIBind();
        getPermission();
        bluetooth_check();

        //foregroundService = new Intent(this, WatchForegroundService.class);
        //foregroundService.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        //foregroundService.setData(Uri.parse("package:" + getPackageName()));
        //startForegroundService(foregroundService);

        String deviceid = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("Main", deviceid);
        StaticResources.deviceID = deviceid;
        StaticResources.device = Build.MODEL;
        StaticResources.os = Build.VERSION.RELEASE;
        SensorSettingsLoader sensorSettingsLoader = new SensorSettingsLoader();
        GeneralSettingsLoader generalSettingsLoader = new GeneralSettingsLoader();
        sensorSettingsLoader.getSensorSettings();
        generalSettingsLoader.getGeneralSettings();
        getWatchid(deviceid);


        handler = new Handler();
        networkCheckRunnable = new Runnable() {
            @Override
            public void run() {
                network_check();
                handler.postDelayed(this, 10000); // 10초마다 네트워크 상태 확인
            }
        };
        serverCheckRunnable = new Runnable() {
            @Override
            public void run() {
                server_check();
                handler.postDelayed(this, 10000); // 10초마다 서버 상태 확인
            }
        };

        handler.post(networkCheckRunnable);
        handler.post(serverCheckRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(networkCheckRunnable);
        handler.post(serverCheckRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(networkCheckRunnable);
        handler.removeCallbacks(serverCheckRunnable);
    }


    public static String getApplicationName(Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }

    protected void getPermission(){
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BODY_SENSORS
                ,Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADVERTISE,Manifest.permission.BLUETOOTH_ADMIN
        };

        ArrayList<String> notGranted = new ArrayList<String>();
        for(String permission : permissions){
            if(ContextCompat.checkSelfPermission(getApplicationContext(), permission)!=PackageManager.PERMISSION_GRANTED){
                notGranted.add(permission);
            }
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED){
                notGranted.add(Manifest.permission.BLUETOOTH_SCAN);
            }
        }

        if(notGranted.size()>0)
            ActivityCompat.requestPermissions(this, notGranted.toArray(new String[notGranted.size()]), 29573);
    }
    protected void UIBind(){
        binding = ActivityMaintempBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        deviceidText = binding.deviceid;
        setting = binding.setting;
        network = binding.networkicon;
        server = binding.servericon;
        bt = binding.bticon;

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pwintent = new Intent(MainActivity.this, PasswordActivity.class);
                startActivity(pwintent);
            }
        });
    }

    protected void bluetooth_check(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null){
            bt.setImageResource(R.drawable.baseline_check_24);
        }
    }

    protected void network_check(){
        Ping ping = new Ping("https://dns.google/");
        ping.start();
        try{
            ping.join();
            if(ping.isSuccess()){
                network.setImageResource(R.drawable.baseline_check_24);
            }
            else{
                network.setImageResource(R.drawable.baseline_close_24);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void server_check(){
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceID(StaticResources.deviceID);
        deviceInfo.setDevice(StaticResources.device);
        deviceInfo.setOs(StaticResources.os);
        Ping ping = new Ping(StaticResources.getHttpURL() + "api/watch");
        ping.start();
        try{
            ping.join();
            if(ping.isSuccess()){
                server.setImageResource(R.drawable.baseline_check_24);
            }else{
                server.setImageResource(R.drawable.baseline_close_24);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getWatchid(String deviceId) {

        String URL = StaticResources.getHttpURL() + "api/watch/"+deviceId;
        JSONObject json_object = new JSONObject();
        try {
            json_object.put("androidId", deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendNetworkGetRequest(URL, json_object, deviceId);
    }

    protected void sendNetworkGetRequest(String URL, JSONObject jsonData, String androidId) {

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String mRequestBody = jsonData.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);

                int state = jsonResponse.getInt("status");
                if (state == 200) {
                    String watchId = jsonResponse.getJSONObject("data").getString("watchId");
                    StaticResources.watchID = watchId;
                    server.setImageResource(R.drawable.baseline_check_24);
//                    Log.d("WatchId", "Received watchId: " + watchId);
                    deviceidText.setText(StaticResources.watchID);
                    Intent intent = new Intent(getApplicationContext(), WatchForegroundService.class);
                    intent.putExtra("watchId", watchId);
                    startForegroundService(intent);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                new android.os.Handler().postDelayed(() -> getWatchid(androidId), 5000);
            }
        }, error -> {
            Log.e("NetworkError", error.toString());
            registerWatch(androidId, StaticResources.device);
            new android.os.Handler().postDelayed(() -> getWatchid(androidId), 5000);
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return mRequestBody.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = new String(response.data, StandardCharsets.UTF_8);
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        requestQueue.add(stringRequest);
    }

    private void registerWatch(String deviceId, String device) {
        String URL = StaticResources.getHttpURL() + "api/watch";
        JSONObject json_object = new JSONObject();
        try {
            json_object.put("uuid", deviceId);
            json_object.put("device", device);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendNetworkPostRequest(URL, json_object);
    }

    protected void sendNetworkPostRequest(String URL, JSONObject jsonData) {

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String mRequestBody = jsonData.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);

                int state = jsonResponse.getInt("status");
                if (state == 200) {
                    String watchId = jsonResponse.getJSONObject("data").getString("watchId");
//                    Log.d("WatchId", "Received watchId: " + watchId);
                    Intent intent = new Intent(getApplicationContext(), WatchForegroundService.class);
                    intent.putExtra("watchId", watchId);
                    startForegroundService(intent);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.e("NetworkError2", error.toString());
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return mRequestBody.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = new String(response.data, StandardCharsets.UTF_8);
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        requestQueue.add(stringRequest);
    }
}