package com.example.mentalhealthapp.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mentalhealthapp.LocaleUtil;
import com.example.mentalhealthapp.R;

import com.example.mentalhealthapp.fragments.AnalysisFragment;
import com.example.mentalhealthapp.fragments.HomeFragment;
import com.example.mentalhealthapp.fragments.MoodFragment;
import com.example.mentalhealthapp.fragments.ProfileFragment;
import com.example.mentalhealthapp.fragments.SettingsFragment;
import com.example.mentalhealthapp.fragments.fragment_mood_log;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatDelegate;
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private Context newBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Load home fragment on first launch
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        // Bottom navigation item selection
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new HomeFragment();

            } else if (id == R.id.nav_analysis) {
                fragment = new AnalysisFragment();

            } else if (id == R.id.nav_mood) {
                fragment = new MoodFragment();

            } else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();

            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }

            return false;
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // Read the saved language code; default to Arabic ("ar") if none is saved
        SharedPreferences prefs =
                newBase.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        String langCode = prefs.getString("app_lang", "ar");

        // Wrap the base context with a locale-updated context
        Context localizedContext = LocaleUtil.updateLocale(newBase, langCode);

        // Attach the localized context to this Activity
        super.attachBaseContext(localizedContext);
    }
    private void loadFragment(Fragment fragment) {

        // إخفاء أو إظهار البار حسب نوع الشاشة
        if (fragment instanceof fragment_mood_log ) {
            // ضع اسم شاشة تسجيل المشاعر هنا
            bottomNav.setVisibility(View.GONE);
        } else {
            bottomNav.setVisibility(View.VISIBLE);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
