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

import com.example.mentalhealthapp.R;
import com.example.mentalhealthapp.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    private DatabaseHelper dbHelper;

    private TextView tvUserName, tvWelcome, tvHowFeel;
    private TextView tvLastMoodEmoji, tvLastMoodLabel, tvLastMoodDate;

    // مؤقتاً: المستخدم الحالي (لاحقاً تربطه بالـSession بعد Login)
    private int currentUserId = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // ربط العناصر
        tvUserName = v.findViewById(R.id.tvUserName);
        tvWelcome  = v.findViewById(R.id.tvWelcome);
        tvHowFeel  = v.findViewById(R.id.tvHowFeel);

        tvLastMoodEmoji = v.findViewById(R.id.tvLastMoodEmoji);
        tvLastMoodLabel = v.findViewById(R.id.tvLastMoodLabel);
        tvLastMoodDate  = v.findViewById(R.id.tvLastMoodDate);

        // تحميل بيانات المستخدم وآخر تسجيل
        loadUserInfo();
        loadLastEntry();

        // BottomNavigation من MainActivity
        BottomNavigationView bottomNav =
                requireActivity().findViewById(R.id.bottomNav);

        // كرت سجل مشاعرك الآن -> روح لتبويب المزاج
        v.findViewById(R.id.cardRecordMood).setOnClickListener(view -> {
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_mood);
        });

        // كرت الاستبيان النفسي -> روح لتبويب المزاج (لأن الاستبيان هناك)
        v.findViewById(R.id.cardQuestionnaire).setOnClickListener(view -> {
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_mood);
        });

        // كرت تحليل الذكاء الاصطناعي -> تبويب التحليل
        v.findViewById(R.id.cardAI).setOnClickListener(view -> {
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_analysis);
        });

        // كرت الإعدادات -> تبويب الإعدادات
        v.findViewById(R.id.cardSettings).setOnClickListener(view -> {
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_settings);
        });

        // كرت الدعم الفني -> حالياً نفس تبويب الإعدادات أو اتركه لاحقاً
        v.findViewById(R.id.cardSupport).setOnClickListener(view -> {
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_settings);
        });

        // أيقونات أعلى الهيدر (اختياري)
        v.findViewById(R.id.ivBell).setOnClickListener(view -> {
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_settings);
        });

        v.findViewById(R.id.ivAvatar).setOnClickListener(view -> {
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_settings);
        });

        return v;
    }

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
        }

        c.close();
    }

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
            tvLastMoodLabel.setText(getString(R.string.no_entries_yet));
            tvLastMoodDate.setText("");
            tvLastMoodEmoji.setText("🙂");
        }

        c.close();
    }

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
