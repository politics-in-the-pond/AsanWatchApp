package com.example.asan_sensor;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsLoader {
    SharedPreferences pref = StaticResources.maincontext.getSharedPreferences("settings", Context.MODE_PRIVATE);
    public void getsettings(){
        StaticResources.ServerURL = this.pref.getString("url", "210.102.178.186");
        StaticResources.port = this.pref.getString("port", "8080");
        StaticResources.password = this.pref.getString("pw", "242424");
    }

    public void putsettings(String url, String port){
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
