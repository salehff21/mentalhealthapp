package com.example.mentalhealthapp.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mentalhealthapp.R;
import com.example.mentalhealthapp.database.DatabaseHelper;

import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etUsername, etEmail, etBirthYear, etPassword, etConfirmPassword;
    private Spinner spGender;
    private Button btnRegister;
    private TextView tvLogin;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // تحميل اللغة قبل عرض الواجهة
        loadLocale();

        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etBirthYear = findViewById(R.id.etBirthYear);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spGender = findViewById(R.id.spGender);

        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        Spinner spGender = findViewById(R.id.spGender);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_list,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(adapter);

    }

    private void registerUser() {

        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String birthYearStr = etBirthYear.getText().toString().trim();
        String gender = spGender.getSelectedItem().toString();
        String pass = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        // التحقق من الحقول المطلوبة
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty()
                || birthYearStr.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_required_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        // تطابق كلمة المرور
        if (!pass.equals(confirm)) {
            Toast.makeText(this, getString(R.string.passwords_not_match), Toast.LENGTH_SHORT).show();
            return;
        }

        // التحقق من سنة الميلاد
        int birthYear;
        try {
            birthYear = Integer.parseInt(birthYearStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.invalid_birth_year), Toast.LENGTH_SHORT).show();
            return;
        }

        // التحقق من اختيار الجنس
        if (spGender.getSelectedItemPosition() == 0) {
            Toast.makeText(this, getString(R.string.choose_gender), Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // إدخال البيانات في جدول المستخدمين
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("username", username);
        values.put("email", email);
        values.put("password_hash", pass);
        values.put("gender", gender);
        values.put("birth_year", birthYear);

        long userId = db.insert("users", null, values);

        if (userId == -1) {
            Toast.makeText(this, getString(R.string.invalid_email_or_password), Toast.LENGTH_LONG).show();
            return;
        }

        // إدخال الإعدادات الافتراضية للخصوصية
        ContentValues privacy = new ContentValues();
        privacy.put("user_id", userId);
        privacy.put("share_data", 0);
        privacy.put("notifications", 1);
        privacy.put("language_pref", getSavedLanguage());

        db.insert("privacy_settings", null, privacy);

        // رسالة النجاح + الانتقال مباشرة للتسجيل
        Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


    // ================= Language Support =================

    private void loadLocale() {
        String lang = getSavedLanguage();
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(
                config, getBaseContext().getResources().getDisplayMetrics()
        );
    }

    private String getSavedLanguage() {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        return prefs.getString("App_Lang", "ar");
    }
}
