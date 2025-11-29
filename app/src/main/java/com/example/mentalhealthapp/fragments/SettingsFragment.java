package com.example.mentalhealthapp.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mentalhealthapp.R;
import com.example.mentalhealthapp.database.DatabaseHelper;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    // DB
    private DatabaseHelper dbHelper;
    // Replace with real logged-in user id
    private int currentUserId = 1;

    // Profile views
    private EditText editFullName;
    private TextView tvEmail;
    private EditText editPhone;
    private TextView tvRole;
    private Button btnSaveProfile;
    private Button btnEditProfile;

    // Password views
    private EditText editCurrentPassword;
    private EditText editNewPassword;
    private EditText editConfirmPassword;
    private Button btnSavePassword;

    // Language views
    private TextView txtLanguageValue;
    private LinearLayout rowLanguage;

    // Support / logout
    private LinearLayout rowHelpCenter;
    private LinearLayout rowPrivacy;
    private LinearLayout rowTerms;
    private Button btnLogout;

    public SettingsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());

        // Bind profile views
        editFullName   = view.findViewById(R.id.editFullName);
        tvEmail        = view.findViewById(R.id.tvEmail);
        editPhone      = view.findViewById(R.id.editPhone);
        tvRole         = view.findViewById(R.id.tvRole);
        btnSaveProfile = view.findViewById(R.id.saveProfileButton);
        btnEditProfile = view.findViewById(R.id.editProfileButton);

        // Bind password views
        editCurrentPassword = view.findViewById(R.id.editCurrentPassword);
        editNewPassword     = view.findViewById(R.id.editNewPassword);
        editConfirmPassword = view.findViewById(R.id.editConfirmPassword);
        btnSavePassword     = view.findViewById(R.id.btnSavePassword);

        // Bind language and support views
        txtLanguageValue = view.findViewById(R.id.txtLanguageValue);
        rowLanguage      = view.findViewById(R.id.rowLanguage);
        rowHelpCenter    = view.findViewById(R.id.rowHelpCenter);
        rowPrivacy       = view.findViewById(R.id.rowPrivacy);
        rowTerms         = view.findViewById(R.id.rowTerms);
        btnLogout        = view.findViewById(R.id.btnLogout);

        // Load initial data from DB
        loadUserProfile();
        loadLanguagePref();

        // Buttons logic
        btnEditProfile.setOnClickListener(v -> enableProfileEditing(true));
        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());

        btnSavePassword.setOnClickListener(v -> changePassword());

        rowLanguage.setOnClickListener(v -> showLanguageDialog());

        // Simple example actions
        rowHelpCenter.setOnClickListener(v ->
                Toast.makeText(getContext(), "Help center clicked", Toast.LENGTH_SHORT).show());

        rowPrivacy.setOnClickListener(v ->
                Toast.makeText(getContext(), "Privacy policy clicked", Toast.LENGTH_SHORT).show());

        rowTerms.setOnClickListener(v ->
                Toast.makeText(getContext(), "Terms clicked", Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v ->
                Toast.makeText(getContext(), "Logout clicked (add your logic here)", Toast.LENGTH_SHORT).show());
    }

    /**
     * Loads user info from users table and fills the profile section.
     */
    private void loadUserProfile() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT full_name, email, username, gender " +
                        "FROM users WHERE user_id = ?",
                new String[]{String.valueOf(currentUserId)}
        );

        if (c.moveToFirst()) {
            String fullName = c.getString(0);
            String email    = c.getString(1);
            String phone    = c.getString(2);   // stored in username column
            String gender   = c.getString(3);   // used here as "role"

            editFullName.setText(fullName);
            tvEmail.setText(email);
            editPhone.setText(phone != null ? phone : "");
            tvRole.setText(gender != null ? gender : "");
        }
        c.close();

        // Start with fields disabled
        enableProfileEditing(false);
    }

    /**
     * Enables or disables profile fields for editing.
     */
    private void enableProfileEditing(boolean enable) {
        editFullName.setEnabled(enable);
        editPhone.setEnabled(enable);
        btnSaveProfile.setEnabled(enable);
    }

    /**
     * Saves edited name/phone into users table.
     */
    private void saveProfileChanges() {
        String fullName = editFullName.getText().toString().trim();
        String phone    = editPhone.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put("full_name", fullName);
        // store phone into username column (change to your real column if exists)
        cv.put("username", phone);

        int rows = db.update(
                "users",
                cv,
                "user_id = ?",
                new String[]{String.valueOf(currentUserId)}
        );

        if (rows > 0) {
            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
            enableProfileEditing(false);
        } else {
            Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Changes password after validating current password and confirmation.
     */
    private void changePassword() {
        String current = editCurrentPassword.getText().toString();
        String newPwd  = editNewPassword.getText().toString();
        String confirm = editConfirmPassword.getText().toString();

        if (current.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(getContext(), "Fill all password fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPwd.equals(confirm)) {
            Toast.makeText(getContext(), "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPwd.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT user_id FROM users WHERE user_id = ? AND password_hash = ?",
                new String[]{String.valueOf(currentUserId), current}
        );
        boolean correctCurrent = c.moveToFirst();
        c.close();

        if (!correctCurrent) {
            Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase dbw = dbHelper.getWritableDatabase();
        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put("password_hash", newPwd);   // plain text in your current schema

        int rows = dbw.update(
                "users",
                cv,
                "user_id = ?",
                new String[]{String.valueOf(currentUserId)}
        );

        if (rows > 0) {
            Toast.makeText(getContext(), "Password updated", Toast.LENGTH_SHORT).show();
            editCurrentPassword.setText("");
            editNewPassword.setText("");
            editConfirmPassword.setText("");
        } else {
            Toast.makeText(getContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Ensures there is a row in privacy_settings for this user and loads language_pref.
     */
    private void loadLanguagePref() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Insert row if not exists (defaults will be used)
        db.execSQL("INSERT OR IGNORE INTO privacy_settings (user_id) VALUES (?)",
                new Object[]{currentUserId});

        String langCode = "ar";

        Cursor c = db.rawQuery(
                "SELECT language_pref FROM privacy_settings WHERE user_id = ?",
                new String[]{String.valueOf(currentUserId)}
        );
        if (c.moveToFirst()) {
            String val = c.getString(0);
            if (val != null && !val.isEmpty()) {
                langCode = val;
            }
        }
        c.close();

        if (langCode.equals("en")) {
            txtLanguageValue.setText("English");
        } else {
            txtLanguageValue.setText("العربية");
        }
    }

    /**
     * Shows a dialog to select language and saves it in privacy_settings.
     */
    private void showLanguageDialog() {
        final String[] labels = {"العربية", "English"};
        final String[] codes  = {"ar", "en"};

        new AlertDialog.Builder(requireContext())
                .setTitle("اختر اللغة")
                .setItems(labels, (dialog, which) -> {
                    String selectedCode  = codes[which];
                    String selectedLabel = labels[which];

                    saveLanguagePref(selectedCode);
                    txtLanguageValue.setText(selectedLabel);

                    // Apply locale and recreate activity
                    applyLanguage(selectedCode);
                })
                .show();
    }

    /**
     * Updates language_pref column for the current user.
     */
    private void saveLanguagePref(String langCode) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put("language_pref", langCode);

        db.update(
                "privacy_settings",
                cv,
                "user_id = ?",
                new String[]{String.valueOf(currentUserId)}
        );
    }

    /**
     * Applies a new locale and recreates the hosting Activity.
     */
    private void applyLanguage(String langCode) {
        // 1) Persist the selected language so it survives app restarts
        saveLanguage(langCode);

        // 2) Update the current Resources locale so strings are reloaded
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources res = requireContext().getResources();
        Configuration config = res.getConfiguration();
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());

        // 3) Recreate the Activity so all views are inflated with the new locale
        requireActivity().recreate();
    }

    private void saveLanguage(String langCode) {
        // Save the language code (e.g., "en", "ar") in SharedPreferences
        SharedPreferences prefs =
                requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        // Apply the change asynchronously
        prefs.edit()
                .putString("app_lang", langCode)
                .apply();
    }

}
