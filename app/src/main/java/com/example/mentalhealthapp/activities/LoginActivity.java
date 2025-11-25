package com.example.mentalhealthapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mentalhealthapp.R;
import com.example.mentalhealthapp.database.DatabaseHelper;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvCreateAccount, tvForgot, tvChangeLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // تحميل اللغة قبل عرض واجهة XML
        loadLocale();

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
        tvForgot = findViewById(R.id.tvForgot);



        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();

            if(email.isEmpty() || pass.isEmpty()){
                Toast.makeText(this, getString(R.string.fill_required_fields), Toast.LENGTH_SHORT).show();
                return;
            }
            DatabaseHelper db = new DatabaseHelper(this);

            if (!db.checkUser(email, pass)) {
                Toast.makeText(this, getString(R.string.invalid_email_or_password), Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: لاحقاً ربط API للتحقق من الحساب


            Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        tvCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        tvForgot.setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.forgot_password), Toast.LENGTH_SHORT).show());


    }


    //                       دعم اللغة — Language Support



    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("AppSettings", MODE_PRIVATE).edit();
        editor.putString("App_Lang", lang);
        editor.apply();
    }
    // تحميل اللغة المحفوظة
    private void loadLocale() {
        String lang = getSavedLanguage();
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    // استرجاع اللغة المختارة
    private String getSavedLanguage() {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        return prefs.getString("App_Lang", "ar"); // العربية هي الافتراضية
    }
}
