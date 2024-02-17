package com.example.asan_sensor.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class WatchItem {
    public ArrayList<BeaconSignal> getItem() {
        return item;
    }

    public void setItem(ArrayList<BeaconSignal> item) {
        this.item = item;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    @Expose
    @SerializedName("item") public ArrayList<BeaconSignal> item = new ArrayList<BeaconSignal>();
    @SerializedName("deviceID") public String deviceID;
}
