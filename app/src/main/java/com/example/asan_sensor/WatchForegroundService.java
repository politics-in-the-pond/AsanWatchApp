package com.example.asan_sensor;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.asan_sensor.dto.BeaconSignal;
import com.example.asan_sensor.dto.WatchItem;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WatchForegroundService extends Service{
    String TAG = "WatchForegroundService";
    private Context context = null;
    private PowerManager.WakeLock wakeLock;
    private BeaconManager beaconManager;
    boolean is_started = false;



    JSONObject one_beacon_json = new JSONObject();
    JSONObject result_json = new JSONObject();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override //서비스 시작 시
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("WatchForegroundService","measure start");
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,
                "bletrack::wakelock");
        wakeLock.acquire();
        if(!is_started) {
            initBeaconManager();
            foregroundNotification();
        }
        is_started = true;

        return START_STICKY;
    }

    void initBeaconManager(){
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.setBackgroundScanPeriod(5000);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setForegroundScanPeriod(5000);
        beaconManager.setForegroundBetweenScanPeriod(0);
        HashMap<String, Kalman> kalmanmap = new HashMap<String, Kalman>();

        NotificationCompat.Builder builder;
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "BLE_service_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "BLE Scanning",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);

            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        builder.setSmallIcon(R.drawable.baseline_pin_drop_24)
                .setContentTitle("ASAN BLE")
                .setContentIntent(pendingIntent);
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                HashMap<String, Double> beaconDataMap = new HashMap<String, Double>();
                int size = beacons.size();
                Log.d("BLE", Integer.toString(size));
                WatchItem watchitem = new WatchItem();
                for (Beacon beacon : beacons) {
                    BeaconSignal tmp = new BeaconSignal();
                    int rssi = beacon.getRssi();
                    String address = beacon.getId1().toString() + "-" + beacon.getId2().toString() + "-" + beacon.getId3().toString();
                    Kalman kalman = null;
                    if(kalmanmap.containsKey(address)){
                        kalman = kalmanmap.get(address);
                    }else{
                        kalmanmap.put(address, new Kalman((double) rssi));
                        kalman = kalmanmap.get(address);
                    }
                    assert kalman != null;
                    double frssi = kalman.do_calc((double) rssi);

                    kalmanmap.replace(address, kalman);
                    beaconDataMap.put(address,frssi);
                    Log.i(TAG, "Detected beacon, raw rssi :"+ Integer.toString(rssi) + " filtered: " + Double.toString(frssi) + " address : " + address);

                    tmp.setRssi(frssi);
                    tmp.setBLEaddress(address);
                    watchitem.item.add(tmp);
                }
                watchitem.deviceID = StaticResources.deviceID;
                performBackgroundTask(StaticResources.ServerURL, String.valueOf((StaticResources.password)),beaconDataMap);
            }
        });
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.startRangingBeacons(new Region("myRangingUniqueId", null, null, null));

    }

    @SuppressLint("ForegroundServiceType")
    void foregroundNotification() { // foreground 실행 후 신호 전달 (안하면 앱 강제종료 됨)
        NotificationCompat.Builder builder;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "BLE_service_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "BLE Scanning",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);

            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        builder.setSmallIcon(R.drawable.baseline_pin_drop_24)
                .setContentTitle("ASAN BLE")
                .setContentIntent(pendingIntent);

        startForeground(1, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private void performBackgroundTask(String serverAddress, String passwordText, HashMap<String, Double> beaconDataMap) {
        String deviceid = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        String URL = serverAddress + "api/receiveData";


        Log.e("test",URL);

        JSONArray json_array = new JSONArray();

        // beaconDataMap을 사용하여 JSON 데이터 생성
        for (Map.Entry<String, Double> entry : beaconDataMap.entrySet()) {
            one_beacon_json = new JSONObject();
            try {
                one_beacon_json.put("bssid", entry.getKey());
                one_beacon_json.put("rssi", entry.getValue());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            json_array.put(one_beacon_json);
        }

        try {
            result_json.put("beacon_data", json_array);
            result_json.put("password", passwordText);
            result_json.put("android_id", deviceid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 서버와 통신
        sendNetworkRequest(URL, result_json);

    }


    private void sendNetworkRequest(String URL, JSONObject jsonData) {

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String mRequestBody = jsonData.toString();


        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, response -> {
            // 응답 처리
            Intent intent = new Intent("ACTION_UPDATE_UI");
            intent.putExtra("response", response);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }, error -> {
            Log.e("NetworkError", error.toString());
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

