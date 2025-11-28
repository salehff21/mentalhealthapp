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

    // Data
    private List<Question> questions = new ArrayList<>();
    private int currentIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mood, container, false);

        // 1) Bind views
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
        btnNext=view.findViewById(R.id.btnNext);
        // 2) Build questions list
        setupQuestions();

        // 3) Show first question
        showQuestion();

        // 4) Previous button
        btnPrev.setOnClickListener(v -> {
            // Save current answer
            saveAnswer();

            if (currentIndex > 0) {
                currentIndex--;
                showQuestion();
            } else {
                Toast.makeText(getContext(), "This is the first question", Toast.LENGTH_SHORT).show();
            }
        });




        // 5) Next arrow (in header)
        btnNext.setOnClickListener(v -> {

            // User must select an option before proceeding
            if (!saveAnswer()) {
                Toast.makeText(getContext(), "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            // If we still have more questions → move to the next one
            if (currentIndex < questions.size() - 1) {

                // Move to next question
                currentIndex++;
                showQuestion();

            } else {
                // If this is the last question → navigate to AnalysisFragment

                // Create the AnalysisFragment instance
                AnalysisFragment analysisFragment = new AnalysisFragment();

                // Replace the current fragment with AnalysisFragment
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, analysisFragment) // Make sure this ID matches your Activity layout
                        .addToBackStack(null) // Allow user to go back if needed
                        .commit();
            }
        });





        // 5) Next arrow (in header)
        imgNextArrow.setOnClickListener(v -> {

            // User must select an option before proceeding
            if (!saveAnswer()) {
                Toast.makeText(getContext(), "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            // If we still have more questions → move to the next one
            if (currentIndex < questions.size() - 1) {

                // Move to next question
                currentIndex++;
                showQuestion();

            } else {
                // If this is the last question → navigate to AnalysisFragment

                // Create the AnalysisFragment instance
                AnalysisFragment analysisFragment = new AnalysisFragment();

                // Replace the current fragment with AnalysisFragment
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, analysisFragment) // Make sure this ID matches your Activity layout
                        .addToBackStack(null) // Allow user to go back if needed
                        .commit();
            }
        });


        return view;
    }

    /**
     * Initialize the 6 daily questions and their 4 options.
     */
    private void setupQuestions() {
        questions.clear();

        questions.add(new Question(
                "كيف تقيم مزاجك العام اليوم؟",
                "ممتاز", "جيد", "متوسط", "سيئ"
        ));

        questions.add(new Question(
                "ما مستوى التوتر الذي تشعر به الآن؟",
                "منخفض جدًا", "منخفض", "متوسط", "مرتفع"
        ));

        questions.add(new Question(
                "كيف تقيم جودة نومك خلال الليلة الماضية؟",
                "جيدة جدًا", "جيدة", "متوسطة", "سيئة"
        ));

        questions.add(new Question(
                "ما مستوى طاقتك اليوم؟",
                "عالية جدًا", "جيدة", "متوسطة", "منخفضة"
        ));

        questions.add(new Question(
                "إلى أي مدى تشعر بالرضا عن علاقاتك الاجتماعية؟",
                "راضٍ جدًا", "راضٍ", "محايد", "غير راضٍ"
        ));

        questions.add(new Question(
                "ما مدى قدرتك على التركيز على مهامك اليوم؟",
                "تركيز ممتاز", "جيد", "متوسط", "ضعيف"
        ));

        progressBar.setMax(questions.size());
    }

    /**
     * Display the current question and restore any previously selected answer.
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
     * Save the selected answer for the current question.
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
            return false; // no option selected
        }

        questions.get(currentIndex).selectedIndex = selected;
        return true;
    }

    /**
     * Simple Question model: question text, 4 options, and selected option index.
     */
    public static class Question {
        String text;
        String opt1, opt2, opt3, opt4;
        int selectedIndex = -1; // -1 = not answered yet

        public Question(String text, String opt1, String opt2, String opt3, String opt4) {
            this.text = text;
            this.opt1 = opt1;
            this.opt2 = opt2;
            this.opt3 = opt3;
            this.opt4 = opt4;
        }
    }
}
