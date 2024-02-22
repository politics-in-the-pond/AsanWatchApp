package com.example.asan_sensor.socket;


import android.util.Log;

import com.example.asan_sensor.StaticResources;

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

public class WebSocketStompClient {

    private StompClient stompClient;
    private List<StompHeader> headerList;
    private static WebSocketStompClient webSocketStompClient = null;

    // WebSocketStompClient singleton 패턴 적용
    public static WebSocketStompClient getInstance(String watchId) {
        if (webSocketStompClient == null)
           webSocketStompClient = new WebSocketStompClient(watchId);
        return webSocketStompClient;
    }

    // 기본 생성자
    public WebSocketStompClient(String watchId) {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, StaticResources.getWSURL());
        // Stomp 헤더에 Authorization 추가
        headerList=new ArrayList<>();
        headerList.add(new StompHeader("Authorization", watchId));

        stompClient.connect(headerList);
    }

    public void sendPositionData(JSONObject jsonData) {
        if (stompClient != null && stompClient.isConnected()) {

            String payload = jsonData.toString();

            stompClient.send("/app/position", payload)
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
        }
    }

    public void sendAccelerometer(JSONObject jsonData) {
        if (stompClient != null && stompClient.isConnected()) {

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
        }
    }

    public void sendHeartrate(JSONObject jsonData) {
        if (stompClient != null && stompClient.isConnected()) {

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
        }
    }

    public void sendGyroscope(JSONObject jsonData) {
        if (stompClient != null && stompClient.isConnected()) {

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
        }
    }

    public void sendPressure(JSONObject jsonData) {
        if (stompClient != null && stompClient.isConnected()) {

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
        }
    }

    public void sendLight(JSONObject jsonData) {
        if (stompClient != null && stompClient.isConnected()) {

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
        }
    }

    public void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }
}

