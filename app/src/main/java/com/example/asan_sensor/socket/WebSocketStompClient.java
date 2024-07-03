package com.example.asan_sensor.socket;

import com.example.asan_sensor.StaticResources;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ua.naiksoftware.stomp.dto.StompHeader;
import android.os.Handler;
import android.util.Log;

public class WebSocketStompClient {

    private StompClient stompClient;
    private List<StompHeader> headerList;
    private static WebSocketStompClient webSocketStompClient = null;
    private String watchId;
    private static final long RECONNECT_DELAY_MS = 5000; // 5초 후 재연결 시도
    private Handler handler = new Handler();
    private boolean isConnected = false;  // 추가된 연결 상태 추적 변수

    // WebSocketStompClient singleton 패턴 적용
    public static WebSocketStompClient getInstance(String watchId) {
        if (webSocketStompClient == null)
            webSocketStompClient = new WebSocketStompClient(watchId);
        return webSocketStompClient;
    }

    // 기본 생성자
    public WebSocketStompClient(String watchId) {
        this.watchId = watchId;
        initializeStompClient();
    }

    private void initializeStompClient() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, StaticResources.getWSURL());
        headerList = new ArrayList<>();
        headerList.add(new StompHeader("Authorization", watchId));
        Log.e("gad", headerList.toString());

        stompClient.withServerHeartbeat(10000); // 10초마다 서버로 하트비트 보내기
        stompClient.withClientHeartbeat(10000); // 10초마다 클라이언트로 하트비트 받기

        connectStompClient();
    }

    private void connectStompClient() {
        stompClient.connect(headerList);
        setupLifecycleListener();
    }

    private synchronized void reconnect() {
        if (!isConnected) {
            handler.postDelayed(() -> {
                synchronized (WebSocketStompClient.this) {
                    if (isConnected) {
                        return; // 이미 연결된 상태라면 재연결하지 않음
                    }
                    disconnect();  // 기존 연결 정리
                    stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, StaticResources.getWSURL());
                    headerList = new ArrayList<>();
                    headerList.add(new StompHeader("Authorization", watchId));
                    stompClient.withServerHeartbeat(10000);
                    stompClient.withClientHeartbeat(10000);
                    stompClient.connect(headerList);
                    setupLifecycleListener();
                }
            }, RECONNECT_DELAY_MS);
        }
    }
    private void setupLifecycleListener() {
        stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            isConnected = true;
                            System.out.println("Stomp connection opened");
                            break;
                        case ERROR:
                            isConnected = false;
                            System.out.println("Stomp connection error: " + lifecycleEvent.getException());
                            reconnect();
                            break;
                        case CLOSED:
                            isConnected = false;
                            System.out.println("Stomp connection closed, trying to reconnect");
                            reconnect();
                            break;
                    }
                });
    }

    public void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
        }
        isConnected = false;  // 연결 상태를 '끊김'으로 설정
    }


    public void sendAccelerometer(JSONObject jsonData) {
        if (stompClient != null && stompClient.isConnected()) {
            try {
                // 현재 시간을 초 단위로 얻기
                long currentTimeSeconds = System.currentTimeMillis() / 1000;

                // JSON 데이터에 현재 시간 추가
                jsonData.put("timeStamp", currentTimeSeconds);

                String payload = jsonData.toString();

                stompClient.send("/app/accelerometer", payload)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                // 구독 시작 시 필요한 작업 (옵션)
                            }

                            @Override
                            public void onComplete() {
                                // 메시지 전송 성공 처리
                            }

                            @Override
                            public void onError(Throwable e) {
                                // 메시지 전송 실패 처리
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendHeartrate(JSONObject jsonData) {
        if (stompClient != null && stompClient.isConnected()) {
            try {
                // 현재 시간을 초 단위로 얻기
                long currentTimeSeconds = System.currentTimeMillis() / 1000;

                // JSON 데이터에 현재 시간 추가
                jsonData.put("timeStamp", currentTimeSeconds);

                String payload = jsonData.toString();

                stompClient.send("/app/heart-rate", payload)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                // 구독 시작 시 필요한 작업 (옵션)
                            }

                            @Override
                            public void onComplete() {
                                // 메시지 전송 성공 처리
                            }

                            @Override
                            public void onError(Throwable e) {
                                // 메시지 전송 실패 처리
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendGyroscope(JSONObject jsonData) {
        if (stompClient != null && stompClient.isConnected()) {
            try {
                // 현재 시간을 초 단위로 얻기
                long currentTimeSeconds = System.currentTimeMillis() / 1000;

                // JSON 데이터에 현재 시간 추가
                jsonData.put("timeStamp", currentTimeSeconds);

                String payload = jsonData.toString();

                stompClient.send("/app/gyroscope", payload)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                // 구독 시작 시 필요한 작업 (옵션)
                            }

                            @Override
                            public void onComplete() {
                                // 메시지 전송 성공 처리
                            }

                            @Override
                            public void onError(Throwable e) {
                                // 메시지 전송 실패 처리
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPressure(JSONObject jsonData) {
        if (stompClient != null && stompClient.isConnected()) {
            try {
                // 현재 시간을 초 단위로 얻기
                long currentTimeSeconds = System.currentTimeMillis() / 1000;

                // JSON 데이터에 현재 시간 추가
                jsonData.put("timeStamp", currentTimeSeconds);

                String payload = jsonData.toString();

                stompClient.send("/app/barometer", payload)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                // 구독 시작 시 필요한 작업 (옵션)
                            }

                            @Override
                            public void onComplete() {
                                // 메시지 전송 성공 처리
                            }

                            @Override
                            public void onError(Throwable e) {
                                // 메시지 전송 실패 처리
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendLight(JSONObject jsonData) {
        if (stompClient != null && stompClient.isConnected()) {
            try {
                // 현재 시간을 초 단위로 얻기
                long currentTimeSeconds = System.currentTimeMillis() / 1000;

                // JSON 데이터에 현재 시간 추가
                jsonData.put("timeStamp", currentTimeSeconds);

                String payload = jsonData.toString();

                stompClient.send("/app/light", payload)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                // 구독 시작 시 필요한 작업 (옵션)
                            }

                            @Override
                            public void onComplete() {
                                // 메시지 전송 성공 처리
                            }

                            @Override
                            public void onError(Throwable e) {
                                // 메시지 전송 실패 처리
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
