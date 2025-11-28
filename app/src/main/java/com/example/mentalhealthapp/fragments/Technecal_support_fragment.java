package com.example.mentalhealthapp.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mentalhealthapp.R;

public class Technecal_support_fragment extends Fragment {

    private static final String SUPPORT_EMAIL = "support@raha.app"; // change to your email

    // Contact views
    private TextView txtEmailValue;
    private TextView txtPhoneValue;

    // Send message views
    private EditText editSupportName;
    private EditText editSupportEmail;
    private EditText editSupportMessage;
    private Button btnSendSupport;

    public Technecal_support_fragment() {
        // Required empty public constructor
    }

    public static Technecal_support_fragment newInstance(String param1, String param2) {
        Technecal_support_fragment fragment = new Technecal_support_fragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.technecal_support_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        txtEmailValue      = view.findViewById(R.id.txtEmailValue);
        txtPhoneValue      = view.findViewById(R.id.txtPhoneValue);
        editSupportName    = view.findViewById(R.id.editSupportName);
        editSupportEmail   = view.findViewById(R.id.editSupportEmail);
        editSupportMessage = view.findViewById(R.id.editSupportMessage);
        btnSendSupport     = view.findViewById(R.id.btnSendSupport);

        // Ensure email label uses constant
        txtEmailValue.setText(SUPPORT_EMAIL);

        // Open email app when clicking the email text
        txtEmailValue.setOnClickListener(v -> openEmailClient(null));

        // Open dialer when clicking phone number (optional)
        txtPhoneValue.setOnClickListener(v -> {
            String phone = txtPhoneValue.getText().toString().trim();
            if (phone.isEmpty()) return;

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), "No dialer app found", Toast.LENGTH_SHORT).show();
            }
        });

        // Send message button
        btnSendSupport.setOnClickListener(v -> {
            String name    = editSupportName.getText().toString().trim();
            String email   = editSupportEmail.getText().toString().trim();
            String message = editSupportMessage.getText().toString().trim();

            if (message.isEmpty()) {
                Toast.makeText(getContext(), "Please write your message", Toast.LENGTH_SHORT).show();
                return;
            }

            String body =
                    "Name: " + name + "\n" +
                            "Email: " + email + "\n\n" +
                            "Message:\n" + message;

            openEmailClient(body);
        });
    }

    private void openEmailClient(@Nullable String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + SUPPORT_EMAIL));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Support request from app");
        if (body != null) {
            intent.putExtra(Intent.EXTRA_TEXT, body);
        }

        try {
            startActivity(Intent.createChooser(intent, "Send email"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
        }
    }
}
