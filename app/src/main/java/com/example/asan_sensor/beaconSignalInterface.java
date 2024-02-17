package com.example.asan_sensor;

import com.asan.bletrack.dto.WatchItem;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface beaconSignalInterface {
    @POST("/api/addData")
    Call<WatchItem> register(@Body WatchItem watchItem);
}
