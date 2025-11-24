package com.example.mentalhealthapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mentalhealthapp.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.imgLogo);
        TextView appName = findViewById(R.id.tvAppName);

        // Logo scale animation
        Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.logo_scale);
        logo.startAnimation(scaleAnim);

        // App name fade-in (optional)
        if (appName != null) {
            new Handler().postDelayed(() -> {
                Animation fadeAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                appName.startAnimation(fadeAnim);
            }, 500);
        }

        // Go to MainActivity
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, SPLASH_TIME);
    }
}
