package com.example.mentalhealthapp.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mentalhealthapp.R;
import com.example.mentalhealthapp.database.DatabaseHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalysisFragment extends Fragment {

    // Top AI status card
    private TextView txtStatusEmoji, txtStatusValue, txtStatusNote;

    // Stats section
    private TextView txtTotalEntries, txtAvgMood;

    // AI recommendations
    private TextView txtPatternBody, txtSuggestionBody;

    // Chart
    private LineChart lineChartMood;

    // Status "card" container (in XML it's a LinearLayout)
    private View cardStatus;

    // Database helper
    private DatabaseHelper dbHelper;

    public AnalysisFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // في XML العنصر cardMoodStatus هو LinearLayout
        cardStatus = view.findViewById(R.id.cardMoodStatus);

        // Load and display analysis
        loadAnalysis();
    }

    /**
     * Load mood statistics, last status, 6-days chart and AI recommendations.
     */
    private void loadAnalysis() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT mood_level, stress_score, recorded_at " +
                        "FROM mood_entries " +
                        "ORDER BY recorded_at ASC",
                null
        );

        // All moods (for last mood label, AI, etc.)
        List<String> moodLabels = new ArrayList<>();
        List<Integer> stressScores = new ArrayList<>();

        // For chart: average per day for the last 6 days
        Map<String, List<Integer>> moodByDayLabel = new LinkedHashMap<>();

        Date now = new Date();
        long sixDaysMillis = 5L * 24 * 60 * 60 * 1000;   // اليوم + آخر 5 أيام
        long cutoffTime = now.getTime() - sixDaysMillis;

        SimpleDateFormat labelFormat = new SimpleDateFormat("d MMM", new Locale("ar"));

        if (cursor.moveToFirst()) {
            do {
                String moodLevel  = cursor.getString(0);
                int stressScore   = cursor.isNull(1) ? -1 : cursor.getInt(1);
                String recordedAt = cursor.getString(2);

                // Keep full label list for last mood & AI
                moodLabels.add(moodLevel);
                stressScores.add(stressScore);

                // Parse date
                Date dateObj = parseRecordedAtToDate(recordedAt);
                if (dateObj == null) {
                    continue;
                }

                // Only keep entries within the last 6 days
                if (dateObj.getTime() >= cutoffTime) {
                    String dayLabel = labelFormat.format(dateObj); // ex: "4 نوفمبر"

                    int moodScore = mapMoodToScore(moodLevel);

                    List<Integer> dayList = moodByDayLabel.get(dayLabel);
                    if (dayList == null) {
                        dayList = new ArrayList<>();
                        moodByDayLabel.put(dayLabel, dayList);
                    }
                    dayList.add(moodScore);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

        // Build chart lists: one point per day (average mood)
        List<Integer> moodScoresForChart = new ArrayList<>();
        List<String> dateLabelsForChart  = new ArrayList<>();

        for (Map.Entry<String, List<Integer>> entry : moodByDayLabel.entrySet()) {
            String dayLabel = entry.getKey();
            List<Integer> scores = entry.getValue();

            int sum = 0;
            for (int s : scores) {
                sum += s;
            }
            int avg = Math.round(sum * 1f / scores.size());

            moodScoresForChart.add(avg);
            dateLabelsForChart.add(dayLabel);
        }

        // Stats over all entries
        int totalEntries = getTotalEntries(db);
        double avgMoodScoreAll = getAverageMoodScore(db);

        // Average of last 3 days from the 6-days chart
        double recentAvgMood = calcRecentAverage(moodScoresForChart, 3);

        updateStatsUI(totalEntries, avgMoodScoreAll);
        updateLastMoodStatus(moodLabels, recentAvgMood);
        updateLineChart(moodScoresForChart, dateLabelsForChart);
        updateAiRecommendations(avgMoodScoreAll, stressScores);
    }

    /**
     * Parse recorded_at (either millis string or "yyyy-MM-dd HH:mm:ss") to Date.
     */
    private Date parseRecordedAtToDate(String recordedAt) {
        if (recordedAt == null) return null;

        try {
            if (recordedAt.matches("\\d+")) {
                long millis = Long.parseLong(recordedAt);
                return new Date(millis);
            } else {
                SimpleDateFormat src =
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                return src.parse(recordedAt);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private double calcRecentAverage(List<Integer> scores, int lastN) {
        if (scores == null || scores.isEmpty()) return -1; // -1 = no recent data

        int n = Math.min(lastN, scores.size());
        int sum = 0;
        for (int i = scores.size() - n; i < scores.size(); i++) {
            sum += scores.get(i);
        }
        return (double) sum / n;
    }

    /**
     * Maps Arabic mood label to a numerical score from 1 to 5.
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
     * Computes average mood score over all entries.
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
            txtAvgMood.setText(String.format(Locale.getDefault(), "%.1f / 5", avgMoodScore));
        }
    }

    /**
     * Updates main AI status card using the average of the last 3 days.
     */
    private void updateLastMoodStatus(List<String> moodLabels, double recentAvgMood) {

        if (moodLabels.isEmpty() || recentAvgMood < 0) {
            txtStatusEmoji.setText("😶");
            txtStatusValue.setText("لا توجد بيانات");
            txtStatusNote.setText("ابدأ بتسجيل حالتك المزاجية لعرض تحليل لحالتك النفسية.");
            if (cardStatus != null) {
                cardStatus.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.status_neutral));
            }
            return;
        }

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

        txtStatusEmoji.setText(statusEmoji);
        txtStatusValue.setText(statusText);
        txtStatusNote.setText("بناءً على تحليل آخر ٣ أيام من تسجيلاتك المزاجية.");

        if (cardStatus != null) {
            cardStatus.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), colorResId));
        }
    }

    /**
     * Configures and updates the line chart with mood scores over time (last 6 days).
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

        // Smooth curve similar to design
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
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        xAxis.setLabelCount(dateLabels.size(), true);
        xAxis.setLabelRotationAngle(-30f);

        lineChartMood.getAxisRight().setEnabled(false);
        lineChartMood.getAxisLeft().setAxisMinimum(1f);
        lineChartMood.getAxisLeft().setAxisMaximum(5f);

        lineChartMood.invalidate();
    }

    /**
     * Produces simple AI-like recommendations based on average mood and stress scores.
     */
    private void updateAiRecommendations(double avgMoodScore, List<Integer> stressScores) {

        if (stressScores.isEmpty()) {
            if (avgMoodScore == 0) {
                txtPatternBody.setText("لا توجد بيانات كافية بعد. حاول تسجيل حالتك المزاجية لعدة أيام.");
                txtSuggestionBody.setText("ابدأ باستخدام الاستبيان اليومي لمساعدة الذكاء الاصطناعي على فهم حالتك الذهنية.");
                return;
            }
        }

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

        if (avgStress >= 4 || highStressDays >= 3) {
            txtPatternBody.setText(
                    "اكتشف الذكاء الاصطناعي نمطاً من التوتر والقلق المرتفع في استجاباتك الأخيرة."
            );
            txtSuggestionBody.setText(
                    "حاول تقليل الأعباء، خذ فترات راحة منتظمة، مارس التنفس العميق، وإذا استمر التوتر، " +
                            "فكر في التحدث إلى شخص تثق به أو مختص."
            );
        } else if (avgMoodScore <= 2.5) {
            txtPatternBody.setText(
                    "كان متوسط حالتك المزاجية منخفضاً نسبياً في الآونة الأخيرة."
            );
            txtSuggestionBody.setText(
                    "خطط لأنشطة ممتعة، حافظ على تواصلك مع الأشخاص الداعمين، وراقب حالتك المزاجية يومياً. " +
                            "إذا استمر الحزن أو انخفاض الطاقة، قد يكون من المفيد استشارة مختص في الصحة النفسية."
            );
        } else if (avgMoodScore >= 4.0) {
            txtPatternBody.setText(
                    "يُظهر نمط حالتك المزاجية الأخير حالات إيجابية ومستقرة في الغالب."
            );
            txtSuggestionBody.setText(
                    "استمر في الحفاظ على روتينك الصحي، مثل النوم الجيد، والنشاط البدني، والتفاعلات الاجتماعية الهادفة. " +
                            "هذه العادات تدعم رفاهيتك."
            );
        } else {
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
