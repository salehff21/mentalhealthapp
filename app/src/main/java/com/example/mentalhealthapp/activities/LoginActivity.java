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

        // Apply saved language before inflating the XML layout
        loadLocale();

        // Check if user is already logged in (login once behavior)
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int savedUserId = prefs.getInt("user_id", -1);
        if (savedUserId != -1) {
            // User already logged in -> go directly to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        // View bindings
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
        tvForgot = findViewById(R.id.tvForgot);
        // tvChangeLang = findViewById(R.id.tvChangeLang); // if you have a language switcher

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();

            // Basic validation
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_required_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper db = new DatabaseHelper(this);

            // Check if user exists with this email and password
            if (!db.checkUser(email, pass)) {
                Toast.makeText(this, getString(R.string.invalid_email_or_password), Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Replace this with a real user id from the database
            // Example (you need to implement getUserIdByEmail in DatabaseHelper):
            // int userId = db.getUserIdByEmail(email);
            int userId = 11; // temporary placeholder

            // Save session and navigate to MainActivity
            onLoginSuccess(userId, email);
            Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
        });

        tvCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        tvForgot.setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.forgot_password), Toast.LENGTH_SHORT).show());
    }

    /**
     * Called when login is successful.
     * Saves the user session (user_id and user_email) so the user does not need to log in again.
     */
    private void onLoginSuccess(int userId, String email) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Store user session data
        editor.putInt("user_id", userId);
        editor.putString("user_email", email);
        editor.apply();

        // Navigate to the main screen
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Prevent going back to the login screen
    }

    /**
     * Change the app locale and persist the selected language.
     */
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

    /**
     * Load the saved language and apply it to the current context.
     */
    private void loadLocale() {
        String lang = getSavedLanguage();
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(
                config,
                getBaseContext().getResources().getDisplayMetrics()
        );
    }

    /**
     * Retrieve the saved app language from SharedPreferences.
     * Default language is Arabic ("ar").
     */
    private String getSavedLanguage() {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        return prefs.getString("App_Lang", "ar");
    }
}
