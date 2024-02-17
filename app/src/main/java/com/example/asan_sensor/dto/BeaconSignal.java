package com.example.asan_sensor.dto;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BeaconSignal {
    @SerializedName("BLEaddress") public String BLEaddress;
    @SerializedName("rssi") public double rssi;
    @Expose
    @SerializedName("major") public int major;
    @Expose
    @SerializedName("minor") public int minor;

    public String getBLEaddress() {
        return BLEaddress;
    }

    public void setBLEaddress(String BLEaddress) {
        this.BLEaddress = BLEaddress;
    }

    public double getRssi() {
        return rssi;
    }

    public void setRssi(double rssi) {
        this.rssi = rssi;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getRx() {
        return rx;
    }

    public void setRx(int rx) {
        this.rx = rx;
    }

    @Expose
    @SerializedName("rx") public int rx;

}
