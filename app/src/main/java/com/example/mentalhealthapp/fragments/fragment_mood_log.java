package com.example.mentalhealthapp.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mentalhealthapp.R;
import com.example.mentalhealthapp.database.DatabaseHelper;

import org.jetbrains.annotations.Nullable;

public class fragment_mood_log extends Fragment {

    // Mood selection cards
    private CardView cardHappy, cardCalm, cardSad, cardAnxious,
            cardTired, cardAngry, cardGrateful, cardNeutral, cardMoodPreview;

    // Notes input and preview widgets
    private EditText edtNotes;
    private TextView txtPreviewEmoji, txtPreviewMood;

    // Currently selected mood and emoji
    private String selectedMood = null;
    private String selectedEmoji = null;

    // Local database helper (SQLite)
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mood_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize local database helper
        dbHelper = new DatabaseHelper(requireContext());

        // Back button: return to previous screen
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Bind views
        cardHappy        = view.findViewById(R.id.cardHappy);
        cardCalm         = view.findViewById(R.id.cardCalm);
        cardSad          = view.findViewById(R.id.cardSad);
        cardAnxious      = view.findViewById(R.id.cardAnxious);
        cardTired        = view.findViewById(R.id.cardTired);
        cardAngry        = view.findViewById(R.id.cardAngry);
        cardGrateful     = view.findViewById(R.id.cardGrateful);
        cardNeutral      = view.findViewById(R.id.cardNeutral);
        cardMoodPreview  = view.findViewById(R.id.cardMoodPreview);

        edtNotes         = view.findViewById(R.id.edtNotes);
        txtPreviewEmoji  = view.findViewById(R.id.txtPreviewEmoji);
        txtPreviewMood   = view.findViewById(R.id.txtPreviewMood);

        // Attach listeners for each mood option
        // (UI text is Arabic, but comments and logic are in English)
        setupMoodCard(cardHappy,    "سعيد",  "😊", 0xFFEFB63B);
        setupMoodCard(cardCalm,     "هادئ",  "🙂", 0xFF42A5F5);
        setupMoodCard(cardSad,      "حزين",  "😟", 0xFF7E57C2);
        setupMoodCard(cardAnxious,  "قلِق",  "😰", 0xFF26C6DA);
        setupMoodCard(cardTired,    "متعب",  "😴", 0xFF8D6E63);
        setupMoodCard(cardAngry,    "غاضب",  "😡", 0xFFE53935);
        setupMoodCard(cardGrateful, "ممتن",  "🥰", 0xFFEC407A);
        setupMoodCard(cardNeutral,  "محايد", "😐", 0xFFB0BEC5);

        // Save button: store the selected mood and notes into the database
        Button btnSaveMood = view.findViewById(R.id.btnSaveMood);
        btnSaveMood.setOnClickListener(v -> {

            if (selectedMood == null) {
                Toast.makeText(getContext(),
                        "Please select your mood first",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String notes = edtNotes.getText().toString().trim();

            // Temporary user ID until login system is implemented
            int userId = 1;


            // For now, we pass null because this screen does not collect stress/sleep/energy
            Integer stressScore = null;
            Integer sleepQuality = null;
            Integer energyLevel = null;

            boolean success = dbHelper.insertMood(
                    userId,
                    selectedMood,
                    stressScore,
                    sleepQuality,
                    energyLevel,
                    notes
            );

            if (success) {
                Toast.makeText(getContext(),
                        "Mood has been saved successfully",
                        Toast.LENGTH_SHORT).show();

                edtNotes.setText("");
            } else {
                Toast.makeText(getContext(),
                        "Error saving mood",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Helper method to attach a click listener to each mood card.
     */
    private void setupMoodCard(CardView card, String moodName, String emoji, int colorInt) {
        card.setOnClickListener(v -> selectMood(card, moodName, emoji, colorInt));
    }

    /**
     * Called when the user selects one of the mood cards.
     * Updates selected mood state, highlights the chosen card,
     * and refreshes the big preview card.
     */
    private void selectMood(CardView selectedCard, String moodName, String emoji, int colorInt) {
        selectedMood  = moodName;
        selectedEmoji = emoji;

        // Reset background color for all mood cards to white
        resetAllCards();

        // Highlight the selected card
        selectedCard.setCardBackgroundColor(colorInt);

        // Update the large preview card
        cardMoodPreview.setCardBackgroundColor(colorInt);
        txtPreviewEmoji.setText(emoji);
        txtPreviewMood.setText(moodName);
    }

    /**
     * Resets all mood cards to default (white) background color.
     */
    private void resetAllCards() {
        int white = Color.parseColor("#FFFFFF");
        cardHappy.setCardBackgroundColor(white);
        cardCalm.setCardBackgroundColor(white);
        cardSad.setCardBackgroundColor(white);
        cardAnxious.setCardBackgroundColor(white);
        cardTired.setCardBackgroundColor(white);
        cardAngry.setCardBackgroundColor(white);
        cardGrateful.setCardBackgroundColor(white);
        cardNeutral.setCardBackgroundColor(white);
    }
}
