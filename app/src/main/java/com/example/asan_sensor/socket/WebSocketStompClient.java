package com.example.asan_sensor.socket;


import android.util.Log;

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

    public WebSocketStompClient(String watchId) {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://192.168.94.153:8080/ws");
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

    public void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }
}

