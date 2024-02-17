package com.example.asan_sensor;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsLoader {
    SharedPreferences pref = StaticResources.maincontext.getSharedPreferences("settings", Context.MODE_PRIVATE);
    public void getsettings(){
        StaticResources.ServerURL = this.pref.getString("url", "http://192.168.45.40:8080/");
        StaticResources.password = this.pref.getInt("pw", 123);
    }

    public void putsettings(String url, int pw){
        SharedPreferences.Editor editor = this.pref.edit();
        editor.putString("url", url);
        editor.putInt("pw", pw);
        editor.commit();
    }
}
