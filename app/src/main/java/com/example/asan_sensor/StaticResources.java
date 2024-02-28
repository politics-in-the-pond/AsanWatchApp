package com.example.asan_sensor;

import android.content.Context;

public class StaticResources {
    public static String password = "242424";
    public static String ServerURL = "210.102.178.186";
    public static String port = "8080";
    public static String watchID = "";
    public static String deviceID = "";
    public static String device = "";
    public static String os = "";
    public static Context maincontext;
    public static int acchz;
    public static int gyrohz;


    public static String getHttpURL(){
        if(port.equals(""))
            return "http://" + ServerURL + "/";
        else
            return "http://" + ServerURL + ":" + port + "/";
    }

    public static String getWSURL(){
        if(port.equals(""))
            return "ws://" + ServerURL + "/ws";
        else
            return "ws://" + ServerURL + ":" + port + "/ws";
    }
}
