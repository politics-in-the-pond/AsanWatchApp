package com.example.asan_sensor.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.asan_sensor.R;
import com.example.asan_sensor.StaticResources;
import com.example.asan_sensor.databinding.ActivityPasswordBinding;

public class GeneralSettingsActivity extends Activity {
    private Button server;
    private Button pw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIBind();
    }

    protected void UIBind(){
        setContentView(R.layout.settings_menu);
        pw = findViewById(R.id.pw);
        server = findViewById(R.id.server);

        pw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pwintent = new Intent(GeneralSettingsActivity.this, PasswordSettingsActivity.class);
                startActivity(pwintent);
            }
        });

        server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pwintent = new Intent(GeneralSettingsActivity.this, ServerSettingsActivity.class);
                startActivity(pwintent);
            }
        });

    }
}
