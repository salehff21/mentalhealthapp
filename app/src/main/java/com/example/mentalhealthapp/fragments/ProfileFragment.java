package com.example.mentalhealthapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mentalhealthapp.R;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // ربط الواجهة
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ربط عناصر الواجهة (الأسماء من ملف fragment_profile.xml)
        TextView txtUserName              = view.findViewById(R.id.txtUserName);
        TextView txtUserEmail             = view.findViewById(R.id.txtUserEmail);
        TextView txtMemberStatus          = view.findViewById(R.id.txtMemberStatus);
        TextView txtActiveDaysValue       = view.findViewById(R.id.txtActiveDaysValue);
        TextView txtCriticalEntriesValue  = view.findViewById(R.id.txtCriticalEntriesValue);

        // بيانات تجريبية الآن – لاحقًا استبدلها ببيانات من الـ DB / API / SharedPreferences
        txtUserName.setText("أحمد محمد");
        txtUserEmail.setText("saleh2@gmail.com");
        txtMemberStatus.setText("عضو نشط");
        txtActiveDaysValue.setText("42");
        txtCriticalEntriesValue.setText("2");

        // لو أردت تعبئة قائمة "المشاعر الأكثر شيوعاً" وسجل المشاعر
        // استخدم الـ LinearLayout:
        // LinearLayout layoutTopMoods   = view.findViewById(R.id.layoutTopMoods);
        // LinearLayout layoutRecentMoods = view.findViewById(R.id.layoutRecentMoods);
        // ثم أضف Views ديناميكياً حسب البيانات.
    }
}
