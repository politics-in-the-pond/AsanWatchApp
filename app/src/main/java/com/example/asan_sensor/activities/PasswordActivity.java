package com.example.asan_sensor.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.asan_sensor.StaticResources;
import com.example.asan_sensor.databinding.ActivityPasswordBinding;

public class PasswordActivity extends Activity {
    private EditText pw;
    private ImageView ok;
    private com.example.asan_sensor.databinding.ActivityPasswordBinding binding;
    private Boolean intentLock = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIBind();
    }

    protected void UIBind(){
        binding = ActivityPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pw = binding.passwordInput;
        pw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().equals(StaticResources.password)){
                    if(!intentLock) {
                        intentLock = true;
                        Intent stintent = new Intent(PasswordActivity.this, MenuActivity.class);
                        startActivity(stintent);
                        finish();
                    }
                }
            }
        });

    }
}
