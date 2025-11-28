package com.example.mentalhealthapp.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mentalhealthapp.R;
import com.example.mentalhealthapp.database.DatabaseHelper;

public class ProfileFragment extends Fragment {

    private DatabaseHelper dbHelper;
    // Change this to the actual logged-in user id from your auth/session logic
    private int currentUserId = 1;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());

        // Bind profile header views
        TextView txtUserName             = view.findViewById(R.id.txtUserName);
        TextView txtUserEmail            = view.findViewById(R.id.txtUserEmail);
        TextView txtMemberStatus         = view.findViewById(R.id.txtMemberStatus);
        TextView txtActiveDaysValue      = view.findViewById(R.id.txtActiveDaysValue);
        TextView txtCriticalEntriesValue = view.findViewById(R.id.txtCriticalEntriesValue);

        // Containers for dynamic lists
        LinearLayout layoutTopMoods    = view.findViewById(R.id.layoutTopMoods);
        LinearLayout layoutRecentMoods = view.findViewById(R.id.layoutRecentMoods);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 1) Load basic user info
        String fullName  = "مستخدم جديد";
        String email     = "unknown@example.com";
        String createdAt = null;

        // RIGHT
        int totalEntries = getTotalEntries(db);
        txtCriticalEntriesValue.setText(String.valueOf(totalEntries));

        Cursor userCursor = db.rawQuery(
                "SELECT full_name, email, created_at FROM users WHERE user_id = ?",
                new String[]{String.valueOf(currentUserId)}
        );
        if (userCursor.moveToFirst()) {
            fullName  = userCursor.getString(0);
            email     = userCursor.getString(1);
            createdAt = userCursor.getString(2);
        }
        userCursor.close();

        txtUserName.setText(fullName);
        txtUserEmail.setText(email);


        // 2) Load stats from mood_entries

        // Active days = count of distinct days with mood entries
        int activeDays = 0;
        Cursor daysCursor = db.rawQuery(
                "SELECT COUNT(DISTINCT substr(recorded_at, 1, 10)) " +
                        "FROM mood_entries WHERE user_id = ?",
                new String[]{String.valueOf(currentUserId)}
        );
        if (daysCursor.moveToFirst()) {
            activeDays = daysCursor.getInt(0);
        }
        daysCursor.close();
        txtActiveDaysValue.setText(String.valueOf(activeDays));

        // Critical entries = high stress or low mood
        int criticalEntries = 0;
        Cursor criticalCursor = db.rawQuery(
                "SELECT COUNT(*) FROM mood_entries " +
                        "WHERE user_id = ? AND (" +
                        "stress_score >= 4 OR " +
                        "mood_level IN ('سيئ','حزين','غاضب','متعب','قلِق')" +
                        ")",
                new String[]{String.valueOf(currentUserId)}
        );
        if (criticalCursor.moveToFirst()) {
            criticalEntries = criticalCursor.getInt(0);
        }
        criticalCursor.close();
        txtCriticalEntriesValue.setText(String.valueOf(criticalEntries));

        // Member status based on activity
        if (activeDays >= 7) {
            txtMemberStatus.setText("عضو نشط");
        } else if (activeDays > 0) {
            txtMemberStatus.setText("عضو جديد");
        } else {
            txtMemberStatus.setText("لم يبدأ باستخدام التطبيق بعد");
        }

        // 3) Top moods list (most frequent moods)
        layoutTopMoods.removeAllViews();
        Cursor topMoodsCursor = db.rawQuery(
                "SELECT mood_level, COUNT(*) AS cnt " +
                        "FROM mood_entries " +
                        "WHERE user_id = ? " +
                        "GROUP BY mood_level " +
                        "ORDER BY cnt DESC " +
                        "LIMIT 3",
                new String[]{String.valueOf(currentUserId)}
        );

        while (topMoodsCursor.moveToNext()) {
            String moodLevel = topMoodsCursor.getString(0);
            int count        = topMoodsCursor.getInt(1);

            // Create card programmatically
            CardView card = new CardView(requireContext());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density);
            card.setLayoutParams(cardParams);
            card.setRadius(12 * getResources().getDisplayMetrics().density);
            card.setCardElevation(2 * getResources().getDisplayMetrics().density);

            LinearLayout innerLayout = new LinearLayout(requireContext());
            innerLayout.setOrientation(LinearLayout.HORIZONTAL);
            innerLayout.setPadding(
                    dp(12), dp(12), dp(12), dp(12)
            );
            innerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

            // Left text: "مرة X"
            TextView txtCount = new TextView(requireContext());
            LinearLayout.LayoutParams lpCount = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
            );
            txtCount.setLayoutParams(lpCount);
            txtCount.setText("مرة " + count);
            txtCount.setTextSize(13);
            txtCount.setTextColor(0xFF777777);

            // Mood text
            TextView txtMood = new TextView(requireContext());
            txtMood.setText(moodLevel);
            txtMood.setTextSize(14);
            txtMood.setTextColor(0xFF333333);
            LinearLayout.LayoutParams lpMood = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lpMood.setMarginEnd(dp(8));
            txtMood.setLayoutParams(lpMood);

            // Emoji
            TextView txtEmoji = new TextView(requireContext());
            txtEmoji.setText(getEmojiForMood(moodLevel));
            txtEmoji.setTextSize(20);

            innerLayout.addView(txtCount);
            innerLayout.addView(txtMood);
            innerLayout.addView(txtEmoji);

            card.addView(innerLayout);
            layoutTopMoods.addView(card);
        }
        topMoodsCursor.close();

        // 4) Recent moods list (last 5 entries)
        layoutRecentMoods.removeAllViews();
        Cursor recentCursor = db.rawQuery(
                "SELECT mood_level, recorded_at, stress_score " +
                        "FROM mood_entries " +
                        "WHERE user_id = ? " +
                        "ORDER BY recorded_at DESC " +
                        "LIMIT 5",
                new String[]{String.valueOf(currentUserId)}
        );

        while (recentCursor.moveToNext()) {
            String moodLevel  = recentCursor.getString(0);
            String recordedAt = recentCursor.getString(1);
            int stressScore   = recentCursor.isNull(2) ? -1 : recentCursor.getInt(2);

            String dateLabel = recordedAt != null && recordedAt.length() >= 10
                    ? recordedAt.substring(0, 10) : recordedAt;

            CardView card = new CardView(requireContext());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.bottomMargin = (int) (6 * getResources().getDisplayMetrics().density);
            card.setLayoutParams(cardParams);
            card.setRadius(12 * getResources().getDisplayMetrics().density);
            card.setCardElevation(2 * getResources().getDisplayMetrics().density);

            LinearLayout innerLayout = new LinearLayout(requireContext());
            innerLayout.setOrientation(LinearLayout.HORIZONTAL);
            innerLayout.setPadding(dp(12), dp(10), dp(12), dp(10));
            innerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

            // Date label
            TextView txtDate = new TextView(requireContext());
            LinearLayout.LayoutParams lpDate = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
            );
            txtDate.setLayoutParams(lpDate);
            txtDate.setText(dateLabel);
            txtDate.setTextSize(13);
            txtDate.setTextColor(0xFF777777);

            // Mood + stress
            String stressText = (stressScore >= 0) ? (" | توتر: " + stressScore) : "";
            TextView txtMood = new TextView(requireContext());
            txtMood.setText(moodLevel + stressText);
            txtMood.setTextSize(14);
            txtMood.setTextColor(0xFF333333);
            LinearLayout.LayoutParams lpMood = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lpMood.setMarginEnd(dp(8));
            txtMood.setLayoutParams(lpMood);

            // Emoji
            TextView txtEmoji = new TextView(requireContext());
            txtEmoji.setText(getEmojiForMood(moodLevel));
            txtEmoji.setTextSize(18);

            innerLayout.addView(txtDate);
            innerLayout.addView(txtMood);
            innerLayout.addView(txtEmoji);

            card.addView(innerLayout);
            layoutRecentMoods.addView(card);
        }
        recentCursor.close();
    }
    private int getTotalEntries(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM mood_entries", null);
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }
    /**
     * Maps mood text (Arabic) to an emoji.
     */
    private String getEmojiForMood(String mood) {
        if (mood == null) return "😶";
        switch (mood) {
            case "سعيد":   return "😊";
            case "ممتن":   return "🥰";
            case "هادئ":   return "🙂";
            case "محايد":  return "😐";
            case "متعب":   return "😴";
            case "قلِق":   return "😰";
            case "حزين":   return "😟";
            case "غاضب":   return "😡";
            default:        return "😶";
        }
    }

    /**
     * Utility to convert dp to pixels.
     */
    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density);
    }
}
