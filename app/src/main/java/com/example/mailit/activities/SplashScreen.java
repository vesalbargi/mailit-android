package com.example.mailit.activities;

import static com.example.mailit.prefrences.PreferencesManager.PREF_KEY_IS_LOGIN;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mailit.databinding.ActivitySplashScreenBinding;
import com.example.mailit.prefrences.PreferencesManager;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashScreenBinding binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Intent intent;
        PreferencesManager preferencesManager = PreferencesManager.getInstance(this);
        boolean hasLoggedIn = preferencesManager.get(PREF_KEY_IS_LOGIN, false);
        if (hasLoggedIn) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
