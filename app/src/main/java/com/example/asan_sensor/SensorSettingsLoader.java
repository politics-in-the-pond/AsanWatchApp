package com.example.asan_sensor;

import android.content.Context;
import android.content.SharedPreferences;

public class SensorSettingsLoader {
    SharedPreferences pref = StaticResources.maincontext.getSharedPreferences("sensor_settings", Context.MODE_PRIVATE);
    public void getSensorSettings(){
        StaticResources.acchz = this.pref.getInt("acchz", 30);
        StaticResources.gyrohz = this.pref.getInt("gyrohz", 30);
    }

    public void putSensorAcchz(int acchz){
        SharedPreferences.Editor editor = this.pref.edit();
        editor.putInt("acchz", acchz);
        editor.commit();
        editor.apply();
    }

    public void putSensorGyro(int gyrohz){
        SharedPreferences.Editor editor = this.pref.edit();
        editor.putInt("gyrohz", gyrohz);
        editor.commit();
        editor.apply();
    }
}
