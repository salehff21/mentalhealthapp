package com.example.mentalhealthapp.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mentalhealthapp.R;
import com.example.mentalhealthapp.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MoodFragment extends Fragment {

    // UI elements
    private TextView txtQuestion, txtQuestionCounter;
    private RadioGroup radioGroup;
    private RadioButton rb1, rb2, rb3, rb4;
    private ProgressBar progressBar;
    private Button btnPrev;
    private ImageView imgNextArrow;
    private Button btnNext;

    // Questionnaire data
    private final List<Question> questions = new ArrayList<>();
    private int currentIndex = 0;

    // Database helper
    private DatabaseHelper dbHelper;

    // Current user id (change according to your auth logic)
    private int currentUserId = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mood, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // Bind views
        txtQuestion = view.findViewById(R.id.txtQuestion);
        txtQuestionCounter = view.findViewById(R.id.txtQuestionCounter);

        radioGroup = view.findViewById(R.id.radioGroupOptions);
        rb1 = view.findViewById(R.id.rbOption1);
        rb2 = view.findViewById(R.id.rbOption2);
        rb3 = view.findViewById(R.id.rbOption3);
        rb4 = view.findViewById(R.id.rbOption4);

        progressBar = view.findViewById(R.id.progressBar);
        btnPrev = view.findViewById(R.id.btnPrev);
        imgNextArrow = view.findViewById(R.id.imgNextArrow);
        btnNext = view.findViewById(R.id.btnNext);

        // Build questions list
        setupQuestions();

        // Show first question
        showQuestion();

        // Previous button
        btnPrev.setOnClickListener(v -> {
            saveAnswer();
            if (currentIndex > 0) {
                currentIndex--;
                showQuestion();
            } else {
                Toast.makeText(getContext(), "This is the first question", Toast.LENGTH_SHORT).show();
            }
        });

        // Next button and header arrow use the same logic
        btnNext.setOnClickListener(v -> goNext());
        imgNextArrow.setOnClickListener(v -> goNext());

        return view;
    }

    /**
     * Handles moving to the next question or saving the questionnaire at the end.
     */
    private void goNext() {
        // User must select an option
        if (!saveAnswer()) {
            Toast.makeText(getContext(), "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            showQuestion();
        } else {
            // Last question → save to database then navigate to AnalysisFragment
            saveQuestionnaireToDatabase();

            AnalysisFragment analysisFragment = new AnalysisFragment();
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, analysisFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * Initializes the 6 daily questions and their 4 options.
     * IDs (1..6) must match the IDs created in the "questions" table.
     */
    private void setupQuestions() {
        questions.clear();

        // Q1: overall mood (used as mood_level in mood_entries)
        questions.add(new Question(
                1,
                "كيف تقيم مزاجك العام اليوم؟",
                "ممتاز", 4,
                "جيد",   3,
                "متوسط", 2,
                "سيئ",   1
        ));

        // Q2: stress level (used as stress_score in mood_entries)
        questions.add(new Question(
                2,
                "ما مستوى التوتر الذي تشعر به الآن؟",
                "منخفض جدًا", 1,
                "منخفض",      2,
                "متوسط",      3,
                "مرتفع",      4
        ));

        // Q3: sleep quality
        questions.add(new Question(
                3,
                "كيف تقيم جودة نومك خلال الليلة الماضية؟",
                "جيدة جدًا", 4,
                "جيدة",     3,
                "متوسطة",   2,
                "سيئة",     1
        ));

        // Q4: energy level
        questions.add(new Question(
                4,
                "ما مستوى طاقتك اليوم؟",
                "عالية جدًا", 4,
                "جيدة",      3,
                "متوسطة",    2,
                "منخفضة",    1
        ));

        // Q5: social satisfaction
        questions.add(new Question(
                5,
                "إلى أي مدى تشعر بالرضا عن علاقاتك الاجتماعية؟",
                "راضٍ جدًا", 4,
                "راضٍ",     3,
                "محايد",    2,
                "غير راضٍ", 1
        ));

        // Q6: focus
        questions.add(new Question(
                6,
                "ما مدى قدرتك على التركيز على مهامك اليوم؟",
                "تركيز ممتاز", 4,
                "جيد",        3,
                "متوسط",      2,
                "ضعيف",       1
        ));

        progressBar.setMax(questions.size());
    }

    /**
     * Displays the current question and restores any previously selected answer.
     */
    private void showQuestion() {
        Question q = questions.get(currentIndex);

        txtQuestion.setText(q.text);
        txtQuestionCounter.setText("السؤال " + (currentIndex + 1) + " من " + questions.size());
        progressBar.setProgress(currentIndex + 1);

        rb1.setText(q.opt1);
        rb2.setText(q.opt2);
        rb3.setText(q.opt3);
        rb4.setText(q.opt4);

        radioGroup.clearCheck();

        // Restore previously selected answer if exists
        if (q.selectedIndex != -1) {
            switch (q.selectedIndex) {
                case 0:
                    rb1.setChecked(true);
                    break;
                case 1:
                    rb2.setChecked(true);
                    break;
                case 2:
                    rb3.setChecked(true);
                    break;
                case 3:
                    rb4.setChecked(true);
                    break;
            }
        }
    }

    /**
     * Saves the selected answer index for the current question in memory.
     *
     * @return true if an option is selected, false otherwise.
     */
    private boolean saveAnswer() {
        int checkedId = radioGroup.getCheckedRadioButtonId();
        int selected = -1;

        if (checkedId == R.id.rbOption1) {
            selected = 0;
        } else if (checkedId == R.id.rbOption2) {
            selected = 1;
        } else if (checkedId == R.id.rbOption3) {
            selected = 2;
        } else if (checkedId == R.id.rbOption4) {
            selected = 3;
        }

        if (selected == -1) {
            return false;
        }

        questions.get(currentIndex).selectedIndex = selected;
        return true;
    }

    /**
     * Saves the whole questionnaire:
     *  - One row in mood_entries
     *  - One row per question in entry_answers
     */
    private void saveQuestionnaireToDatabase() {

        // Ensure all questions are answered
        for (Question q : questions) {
            if (q.selectedIndex == -1) {
                Toast.makeText(getContext(),
                        "Please answer all questions before finishing",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Q1 = mood_level
        Question moodQuestion = questions.get(0);
        // Q2 = stress_score
        Question stressQuestion = questions.get(1);

        String moodLevel = moodQuestion.getSelectedText();
        int stressScore = stressQuestion.getSelectedScore();

        // Insert one mood_entries row and get entry_id
        long entryId = dbHelper.insertMoodEntry(
                currentUserId,
                moodLevel,
                stressScore,
                null,   // sleep_quality (you can map Q3 here if you want)
                null,   // energy_level  (you can map Q4 here if you want)
                null    // notes
        );

        // Insert each answer into entry_answers
        for (Question q : questions) {
            String answerText = q.getSelectedText();
            int scoreValue = q.getSelectedScore();

            // option_id is null for now (we only store text + score)
            dbHelper.insertEntryAnswer(
                    entryId,
                    q.id,          // question_id
                    null,          // option_id
                    answerText,    // answer_text
                    scoreValue     // score_value
            );
        }

        Toast.makeText(getContext(),
                "Questionnaire saved successfully",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Question model: ID, text, four options, scores, and selected option index.
     */
    public static class Question {
        int id;
        String text;
        String opt1, opt2, opt3, opt4;
        int selectedIndex = -1;

        int scoreOpt1, scoreOpt2, scoreOpt3, scoreOpt4;

        public Question(int id,
                        String text,
                        String opt1, int scoreOpt1,
                        String opt2, int scoreOpt2,
                        String opt3, int scoreOpt3,
                        String opt4, int scoreOpt4) {

            this.id = id;
            this.text = text;
            this.opt1 = opt1;
            this.opt2 = opt2;
            this.opt3 = opt3;
            this.opt4 = opt4;

            this.scoreOpt1 = scoreOpt1;
            this.scoreOpt2 = scoreOpt2;
            this.scoreOpt3 = scoreOpt3;
            this.scoreOpt4 = scoreOpt4;
        }

        public String getSelectedText() {
            switch (selectedIndex) {
                case 0: return opt1;
                case 1: return opt2;
                case 2: return opt3;
                case 3: return opt4;
                default: return null;
            }
        }

        public int getSelectedScore() {
            switch (selectedIndex) {
                case 0: return scoreOpt1;
                case 1: return scoreOpt2;
                case 2: return scoreOpt3;
                case 3: return scoreOpt4;
                default: return 0;
            }
        }
    }
}
