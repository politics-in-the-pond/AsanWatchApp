package com.example.asan_sensor;

import android.content.Context;
import android.content.SharedPreferences;

public class GeneralSettingsLoader {
    SharedPreferences pref = StaticResources.maincontext.getSharedPreferences("settings", Context.MODE_PRIVATE);
    public void getGeneralSettings(){
        StaticResources.ServerURL = this.pref.getString("url", StaticResources.ServerURL);
//        StaticResources.ServerURL = this.pref.getString("url", "192.168.45.157");
        StaticResources.port = this.pref.getString("port", StaticResources.port);
        StaticResources.password = this.pref.getString("pw", "242424");
    }

    public void putGeneralSettings(String url, String port){
        SharedPreferences.Editor editor = this.pref.edit();
        editor.putString("url", url);
        editor.putString("port", port);
        editor.commit();
        editor.apply();
    }

    public void putPassword(String pw){
        SharedPreferences.Editor editor = this.pref.edit();
        editor.putString("pw", pw);
        editor.commit();
        editor.apply();
    }
}
