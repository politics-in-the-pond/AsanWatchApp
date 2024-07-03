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
import android.os.RemoteException;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.asan_sensor.activities.MainActivity;
import com.example.asan_sensor.dto.BeaconSignal;
import com.example.asan_sensor.dto.WatchItem;
import com.example.asan_sensor.socket.WebSocketStompClient;
import com.example.asan_sensor.socket.WebSocketStompClientForPosition;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WatchForegroundService extends Service {
    String TAG = "WatchForegroundService";
    private PowerManager.WakeLock wakeLock;
    private BeaconManager beaconManager;
    boolean is_started = false;
    private WebSocketStompClientForPosition webSocketStompClientForPosition = null;

    private WebSocketStompClient webSocketStompClient = null;

    private String watchId = "";
    JSONObject one_beacon_json = new JSONObject();
    JSONObject result_json = new JSONObject();
    HashMap<String, ArrayList<Double>> accumulatedRssiData = new HashMap<>();



    private int scanCount = 0;
    private final int MAX_SCAN_COUNT = 100;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("WatchForegroundService", "measure start");
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "bletrack::wakelock");


        wakeLock.acquire();
        if (!is_started) {
            initBeaconManager();
            startBeaconScanning();
            foregroundNotification();
            is_started = true;
        }


        if (intent != null && webSocketStompClientForPosition == null) {
            this.watchId = intent.getStringExtra("watchId");
            webSocketStompClientForPosition = WebSocketStompClientForPosition.getInstance(watchId);
            webSocketStompClient = WebSocketStompClient.getInstance(watchId);
        }

        return START_STICKY;



    }

    private void initBeaconManager() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        beaconManager.setBackgroundScanPeriod(50);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setForegroundScanPeriod(50);  // Set to 500ms, matching the broadcast period
        beaconManager.setForegroundBetweenScanPeriod(0);  // Set to 500ms



        NotificationCompat.Builder builder;
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "BLE_service_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "BLE Scanning", NotificationManager.IMPORTANCE_HIGH);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        builder.setSmallIcon(R.drawable.baseline_pin_drop_24)
                .setContentTitle("ASAN BLE")
                .setContentIntent(pendingIntent);

        beaconManager.enableForegroundServiceScanning(builder.build(), 456);
        beaconManager.setEnableScheduledScanJobs(false);
    }




    private void startBeaconScanning() {
        Log.d("check", "countasdasd");
        beaconManager.startRangingBeacons(new Region("myRangingUniqueId", null, null, null));
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for (Beacon beacon : beacons) {
                    processBeacon(beacon);
                }

                scanCount++;
                if (scanCount >= MAX_SCAN_COUNT) {

                    HashMap<String, Double> medianRssiMap = new HashMap<>();
                    synchronized (accumulatedRssiData){
                        for (Map.Entry<String, ArrayList<Double>> entry : accumulatedRssiData.entrySet()) {
                            List<Double> rssiValues = entry.getValue();
                            if(rssiValues.size() >= 7) {
                                rssiValues.sort(Comparator.reverseOrder());
                                System.out.println(entry.getKey() + "rssiValues = " + rssiValues);
                                double medianValue = rssiValues.get(rssiValues.size() / 2);
                                String key = entry.getKey();
                                medianRssiMap.put(key, medianValue);
                            }
                        }}

                    String maxRssiKey = null;
                    double maxRssiValue = Double.NEGATIVE_INFINITY;
                    for (Map.Entry<String, Double> entry : medianRssiMap.entrySet()) {
                        if (entry.getValue() > maxRssiValue) {
                            maxRssiValue = entry.getValue();
                            maxRssiKey = entry.getKey();

                        }
                    }


//                    System.out.println("maxRssiKey = " + maxRssiKey);
                    if (maxRssiKey != null) {
                        medianRssiMap.put(maxRssiKey, medianRssiMap.get(maxRssiKey) + 10);
                    }
                    try {
                        performBackgroundTask(medianRssiMap);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    synchronized (accumulatedRssiData){
                        accumulatedRssiData.clear();}
                    scanCount = 0;
                }
            }
        });
    }



    private void processBeacon(Beacon beacon) {
        WatchItem watchitem = new WatchItem();
        BeaconSignal tmp = new BeaconSignal();
        HashMap<String, Kalman> kalmanmap = new HashMap<>();
        int rssi = beacon.getRssi();
        String address = beacon.getId3().toString();
//        System.out.println("beacon.getId3().toString() = " + beacon.getId3().toString());
        Kalman kalman = kalmanmap.getOrDefault(address, new Kalman((double) rssi));
        assert kalman != null;
        double frssi = kalman.do_calc((double) rssi);
        kalmanmap.put(address, kalman);
        synchronized (accumulatedRssiData){
        accumulatedRssiData.putIfAbsent(address, new ArrayList<>());
        accumulatedRssiData.get(address).add(frssi);
        }
        tmp.setRssi(frssi);
        tmp.setBLEaddress(address);
        watchitem.item.add(tmp);
    }

    @SuppressLint("ForegroundServiceType")
    void foregroundNotification() {
        NotificationCompat.Builder builder;
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "BLE_service_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "BLE Scanning", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
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

    private void performBackgroundTask(HashMap<String, Double> beaconDataMap) throws InterruptedException {
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
            result_json.put("watchId", watchId); //여기 이름 변경해야함.
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 서버와 통신


        if(webSocketStompClientForPosition != null) {
            webSocketStompClientForPosition.sendPositionData(result_json);
        }



    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocketStompClientForPosition != null) {
            webSocketStompClientForPosition.disconnect();
        }
        if (webSocketStompClient != null) {
            webSocketStompClient.disconnect();
        }

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

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
