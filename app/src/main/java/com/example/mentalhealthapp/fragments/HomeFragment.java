package com.example.mentalhealthapp.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.mentalhealthapp.fragments.Technecal_support_fragment;
import com.example.mentalhealthapp.R;
import com.example.mentalhealthapp.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private BottomNavigationView bottomNav;

    private TextView tvUserName, tvWelcome, tvHowFeel;
    private TextView tvLastMoodEmoji, tvLastMoodLabel, tvLastMoodDate;

    // Temporary current user id (later should be taken from login session / SharedPreferences)
    private int currentUserId = 1;
    private Fragment technecal_support_fragment  =new Technecal_support_fragment();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // Bind UI elements
        tvUserName = v.findViewById(R.id.tvUserName);
        tvWelcome  = v.findViewById(R.id.tvWelcome);
        tvHowFeel  = v.findViewById(R.id.tvHowFeel);

        tvLastMoodEmoji = v.findViewById(R.id.tvLastMoodEmoji);
        tvLastMoodLabel = v.findViewById(R.id.tvLastMoodLabel);
        tvLastMoodDate  = v.findViewById(R.id.tvLastMoodDate);

        // Load user information and last mood entry
        loadUserInfo();
        loadLastEntry();

        // Get BottomNavigationView from MainActivity
        bottomNav = requireActivity().findViewById(R.id.bottomNav);

        // "Record Mood Now" card -> open MoodLogFragment

        v.findViewById(R.id.cardRecordMood).setOnClickListener(view -> {
            // Create the mood log fragment (replace with your actual class name if different)
            Fragment moodLogFragment = new fragment_mood_log();

            // Optionally change selected bottom tab to "mood"
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_mood);
            }
            // Replace the current fragment with MoodLogFragment in the main container
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, moodLogFragment) // use the same container as other fragments
                    .addToBackStack(null) // allow back navigation
                    .commit();
        });


        //View Technecal support
        v.findViewById(R.id.cardSupport).setOnClickListener(view -> {
            // Create the mood log fragment (replace with your actual class name if different)

            // Optionally change selected bottom tab to "mood"
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_mood);
            }
            // Replace the current fragment with MoodLogFragment in the main container
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, technecal_support_fragment) // use the same container as other fragments
                    .addToBackStack(null) // allow back navigation
                    .commit();
        });

        // Questionnaire card -> switch to Mood tab

        v.findViewById(R.id.cardQuestionnaire).setOnClickListener(view -> {
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_mood);
        });
        // AI analysis card -> switch to Analysis tab

        v.findViewById(R.id.cardAI).setOnClickListener(view -> {
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_analysis);
        });


        // Settings card -> switch to Settings tab

        v.findViewById(R.id.cardSettings).setOnClickListener(view -> {
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_settings);
        });

        // Support card -> currently also goes to Settings tab
        v.findViewById(R.id.cardSupport).setOnClickListener(view -> {
            // Create the mood log fragment (replace with your actual class name if different)

            // Optionally change selected bottom tab to "mood"
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_mood);
            }
            // Replace the current fragment with MoodLogFragment in the main container
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, technecal_support_fragment) // use the same container as other fragments
                    .addToBackStack(null) // allow back navigation
                    .commit();
        });


        // Header icons (bell + avatar) -> open Settings for now

        v.findViewById(R.id.ivBell).setOnClickListener(view -> {
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_settings);
        });

        v.findViewById(R.id.ivAvatar).setOnClickListener(view -> {
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_settings);
        });

        return v;
    }

    /**
     * Load basic user info (full name) and show welcome text.
     */
    private void loadUserInfo() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT full_name FROM users WHERE user_id=?",
                new String[]{String.valueOf(currentUserId)}
        );

        if (c.moveToFirst()) {
            String name = c.getString(0);
            tvUserName.setText(name);
            tvWelcome.setText(getString(R.string.home_welcome) + "، " + name);
            tvHowFeel.setText(getString(R.string.home_how_feel));
        }

        c.close();
    }

    /**
     * Load the last mood entry for the current user (if any).
     */
    private void loadLastEntry() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT mood_level, recorded_at FROM mood_entries " +
                        "WHERE user_id=? ORDER BY recorded_at DESC LIMIT 1",
                new String[]{String.valueOf(currentUserId)}
        );

        if (c.moveToFirst()) {
            String mood = c.getString(0);
            String date = c.getString(1);

            tvLastMoodLabel.setText(mood);
            tvLastMoodDate.setText(date);
            tvLastMoodEmoji.setText(getEmojiForMood(mood));
        } else {
            // No entries yet
            tvLastMoodLabel.setText(getString(R.string.no_entries_yet));
            tvLastMoodDate.setText("");
            tvLastMoodEmoji.setText("🙂");
        }

        c.close();
    }

    /**
     * Map mood text to an emoji icon.
     */
    private String getEmojiForMood(String mood) {
        if (mood == null) return "🙂";
        String m = mood.toLowerCase();

        if (m.contains("happy") || m.contains("سعيد")) return "😊";
        if (m.contains("calm")  || m.contains("هادئ")) return "😌";
        if (m.contains("sad")   || m.contains("حزين")) return "😢";
        if (m.contains("anx")   || m.contains("قلق"))  return "😰";
        if (m.contains("angry") || m.contains("غاضب")) return "😡";

        return "🙂";
    }
}
