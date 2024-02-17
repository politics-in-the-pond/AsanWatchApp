package com.example.asan_sensor.dto;

import com.google.gson.annotations.SerializedName;

public class DeviceInfo {
    @SerializedName("deviceID") public String deviceID;
    @SerializedName("device") public String device;

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    @SerializedName("os") public String os;
}
