package com.example.mentalhealthapp.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.example.mentalhealthapp.database.DatabaseHelper;
import com.example.mentalhealthapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AnalysisFragment extends Fragment {

    // Top AI status card
    private TextView txtStatusEmoji, txtStatusValue, txtStatusNote;

    // Stats section
    private TextView txtTotalEntries, txtAvgMood;

    // AI recommendations
    private TextView txtPatternBody, txtSuggestionBody;

    // Chart
    private LineChart lineChartMood;

    // Database helper
    private DatabaseHelper dbHelper;

    public AnalysisFragment() {}

    private MaterialCardView cardStatus;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the local SQLite helper
        dbHelper = new DatabaseHelper(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {



        return inflater.inflate(R.layout.fragment_analysis, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        // Bind UI components
        txtStatusEmoji = view.findViewById(R.id.txtStatusEmoji);
        txtStatusValue = view.findViewById(R.id.txtStatusValue);
        txtStatusNote  = view.findViewById(R.id.txtStatusNote);

        txtTotalEntries = view.findViewById(R.id.txtTotalEntries);
        txtAvgMood      = view.findViewById(R.id.txtAvgMood);

        txtPatternBody    = view.findViewById(R.id.txtPatternBody);
        txtSuggestionBody = view.findViewById(R.id.txtSuggestionBody);

        lineChartMood = view.findViewById(R.id.lineChartMood);

        // Load and display analysis
        loadAnalysis();
    }

    /**
     * Main entry point to load mood statistics, last status, chart and AI recommendations.
     */
    private void loadAnalysis() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT mood_level, stress_score, recorded_at " +
                        "FROM mood_entries " +
                        "ORDER BY recorded_at ASC " +
                        "LIMIT 7",
                null
        );

        List<String> moodLabels   = new ArrayList<>();
        List<Integer> moodScores  = new ArrayList<>();
        List<Integer> stressScores = new ArrayList<>();
        List<String> dateLabels   = new ArrayList<>();   // NEW

        if (cursor.moveToFirst()) {
            do {
                String moodLevel   = cursor.getString(0);
                int stressScore    = cursor.isNull(1) ? -1 : cursor.getInt(1);
                String recordedAt  = cursor.getString(2); // NEW

                moodLabels.add(moodLevel);
                moodScores.add(mapMoodToScore(moodLevel));
                stressScores.add(stressScore);

                dateLabels.add(formatDateLabel(recordedAt)); // NEW

            } while (cursor.moveToNext());
        }
        cursor.close();

        int totalEntries = getTotalEntries(db);
        double avgMoodScoreAll = getAverageMoodScore(db);

       // Average of the last 3 days (or fewer if not available
        double recentAvgMood = calcRecentAverage(moodScores, 3);   // NEW

        updateStatsUI(totalEntries, avgMoodScoreAll);
        updateLastMoodStatus(moodLabels, recentAvgMood);           // تعديل التوقيع
        updateLineChart(moodScores, dateLabels);                   // تمرير التواريخ
        updateAiRecommendations(avgMoodScoreAll, stressScores);
    }

    private double calcRecentAverage(List<Integer> scores, int lastN) {
        if (scores == null || scores.isEmpty()) return 0;

        int n = Math.min(lastN, scores.size());
        int sum = 0;
        for (int i = scores.size() - n; i < scores.size(); i++) {
            sum += scores.get(i);
        }
        return (double) sum / n;
    }

    private String formatDateLabel(String recordedAt) {
        if (recordedAt == null) return "";

        try {
            Date date;


            if (recordedAt.matches("\\d+")) {
                long millis = Long.parseLong(recordedAt);
                date = new Date(millis);
            } else {
                // Modify the date format according to the storage in your table
                SimpleDateFormat src =
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                date = src.parse(recordedAt);
            }

            SimpleDateFormat dest =
                    new SimpleDateFormat("d MMM", new Locale("ar")); // مثال: 4 نوفمبر
            return dest.format(date);

        } catch (Exception e) {

            return recordedAt;
        }
    }

    /**
     * Maps Arabic mood label to a numerical score from 1 to 5.
     * You can adjust these mappings according to your questionnaire scale.
     */
    private int mapMoodToScore(String mood) {
        if (mood == null) return 3;

        switch (mood) {
            case "سعيد":   return 5;
            case "ممتن":   return 5;
            case "هادئ":   return 4;
            case "محايد":  return 3;
            case "متعب":   return 2;
            case "قلِق":   return 2;
            case "حزين":   return 1;
            case "غاضب":   return 1;
            default:       return 3;
        }
    }

    /**
     * Returns total number of mood entries in the database.
     */
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
     * Computes an approximate average mood score based on mood_level mapping.
     */
    private double getAverageMoodScore(SQLiteDatabase db) {
        Cursor c = db.rawQuery(
                "SELECT mood_level FROM mood_entries", null);

        int sum = 0;
        int count = 0;
        if (c.moveToFirst()) {
            do {
                String mood = c.getString(0);
                sum += mapMoodToScore(mood);
                count++;
            } while (c.moveToNext());
        }
        c.close();

        if (count == 0) return 0;
        return (double) sum / count;
    }

    /**
     * Updates statistics section (total entries and average mood).
     */
    private void updateStatsUI(int totalEntries, double avgMoodScore) {

        txtTotalEntries.setText(String.valueOf(totalEntries));

        if (totalEntries == 0) {
            txtAvgMood.setText("N/A");
        } else {
            txtAvgMood.setText(String.format("%.1f / 5", avgMoodScore));
        }
    }

    /**
     * Updates main AI status card using the most recent mood label.
     */
    private void updateLastMoodStatus(List<String> moodLabels, double recentAvgMood) {

        if (moodLabels.isEmpty()) {
            txtStatusEmoji.setText("😶");
            txtStatusValue.setText("لا توجد بيانات");
            txtStatusNote.setText("ابدأ بتسجيل حالتك المزاجية لعرض تحليل لحالتك النفسية.");
            if (cardStatus != null) {
                cardStatus.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.status_neutral));
            }
            return;
        }

        //Last recorded mood status (for your information only, if you wish to display it elsewhere
        String latestMood = moodLabels.get(moodLabels.size() - 1);

        // Psychological status evaluation based on the average of the last 3 days
        String statusText;
        String statusEmoji;
        int colorResId;

        if (recentAvgMood >= 4.5) {
            statusText  = "ممتاز";
            statusEmoji = "😄";
            colorResId  = R.color.status_excellent;
        } else if (recentAvgMood >= 3.5) {
            statusText  = "جيد جداً";
            statusEmoji = "😊";
            colorResId  = R.color.status_good;
        } else if (recentAvgMood >= 2.5) {
            statusText  = "متوسط";
            statusEmoji = "😐";
            colorResId  = R.color.status_neutral;
        } else {
            statusText  = "منخفض";
            statusEmoji = "😟";
            colorResId  = R.color.status_low;
        }

        // Your psychological state is excellent
        txtStatusEmoji.setText(statusEmoji);
        txtStatusValue.setText(statusText);
        txtStatusNote.setText("بناءً على تحليل آخر ٣ أيام من تسجيلاتك المزاجية.");

        if (cardStatus != null) {
            cardStatus.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), colorResId));
        }
    }


    /**
     * Configures and updates the line chart with mood scores over time.
     */
    private void updateLineChart(List<Integer> moodScores, List<String> dateLabels) {

        if (moodScores.isEmpty()) {
            lineChartMood.clear();
            lineChartMood.setNoDataText("لا توجد بيانات مزاج لعرضها حالياً");
            return;
        }

        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < moodScores.size(); i++) {
            entries.add(new Entry(i, moodScores.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "درجة المزاج");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        // شكل منحني قريب من التصميم الذي في الصورة
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(60);

        LineData lineData = new LineData(dataSet);
        lineChartMood.setData(lineData);

        Description desc = new Description();
        desc.setText("");
        lineChartMood.setDescription(desc);

        XAxis xAxis = lineChartMood.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels)); // الأيام على المحور X
        xAxis.setLabelCount(dateLabels.size(), true);
        xAxis.setLabelRotationAngle(-30f); // ميلان بسيط للقراءة

        lineChartMood.getAxisRight().setEnabled(false);
        lineChartMood.getAxisLeft().setAxisMinimum(1f); // من 1 إلى 5
        lineChartMood.getAxisLeft().setAxisMaximum(5f);

        lineChartMood.invalidate();
    }

    /**
     * Produces simple AI-like recommendations based on average mood and stress scores.
     * This is a heuristic approximation that can later be replaced by real
     * Naive Bayes / Decision Tree model predictions.
     */
    private void updateAiRecommendations(double avgMoodScore, List<Integer> stressScores) {

        if (stressScores.isEmpty()) {
            // No stress data yet – fallback to mood-based rule
            if (avgMoodScore == 0) {
                txtPatternBody.setText("لا توجد بيانات كافية بعد. حاول تسجيل حالتك المزاجية لعدة أيام.");
                txtSuggestionBody.setText("ابدأ باستخدام الاستبيان اليومي لمساعدة الذكاء الاصطناعي على فهم حالتك الذهنية.");
                return;
            }
        }

        // Compute average stress (ignoring -1 as “not provided”)
        int sum = 0;
        int count = 0;
        int highStressDays = 0;
        for (Integer s : stressScores) {
            if (s == null || s < 0) continue;
            sum += s;
            count++;
            if (s >= 4) highStressDays++;
        }

        double avgStress = 0;
        if (count > 0) {
            avgStress = (double) sum / count;
        }

        // Simple decision-tree-like logic:

        if (avgStress >= 4 || highStressDays >= 3) {
            // High stress pattern
            txtPatternBody.setText(
                    "اكتشف الذكاء الاصطناعي نمطاً من التوتر والقلق المرتفع في استجاباتك الأخيرة."
            );
            txtSuggestionBody.setText(
                    "حاول تقليل الأعباء، خذ فترات راحة منتظمة، مارس التنفس العميق، وإذا استمر التوتر، " +
                            "فكر في التحدث إلى شخص تثق به أو مختص."
            );
        } else if (avgMoodScore <= 2.5) {
            // Low mood pattern
            txtPatternBody.setText(
                    "كان متوسط حالتك المزاجية منخفضاً نسبياً في الآونة الأخيرة."
            );
            txtSuggestionBody.setText(
                    "خطط لأنشطة ممتعة، حافظ على تواصلك مع الأشخاص الداعمين، وراقب حالتك المزاجية يومياً. " +
                            "إذا استمر الحزن أو انخفاض الطاقة، قد يكون من المفيد استشارة مختص في الصحة النفسية."
            );
        } else if (avgMoodScore >= 4.0) {
            // Positive pattern
            txtPatternBody.setText(
                    "يُظهر نمط حالتك المزاجية الأخير حالات إيجابية ومستقرة في الغالب."
            );
            txtSuggestionBody.setText(
                    "استمر في الحفاظ على روتينك الصحي، مثل النوم الجيد، والنشاط البدني، والتفاعلات الاجتماعية الهادفة. " +
                            "هذه العادات تدعم رفاهيتك."
            );
        } else {
            // Neutral / mixed pattern
            txtPatternBody.setText(
                    "نمط حالتك المزاجية الأخير مختلط، ويحتوي على أيام إيجابية وأخرى مليئة بالتحديات."
            );
            txtSuggestionBody.setText(
                    "حاول ملاحظة ما يساعدك على الشعور بالتحسن في الأيام الجيدة وكرر تلك الأنشطة. " +
                            "استخدم الاستبيان اليومي باستمرار حتى يتمكن الذكاء الاصطناعي من تقديم ملاحظات أكثر دقة."
            );
        }
    }
}
