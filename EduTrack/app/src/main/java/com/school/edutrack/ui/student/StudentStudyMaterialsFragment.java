package com.school.edutrack.ui.student;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.school.edutrack.R;

public class StudentStudyMaterialsFragment extends Fragment {

    private static final String TAG = "StudentStudyMaterialsFragment";
    private String studentId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            studentId = getArguments().getString("studentId");
            Log.d(TAG, "Student ID retrieved at 06:50 PM IST, Thursday, June 12, 2025: " + studentId);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_student_study_materials, container, false);

        TextView studentIdDisplay = root.findViewById(R.id.study_materials_student_id);
        if (studentId != null && !studentId.isEmpty()) {
            studentIdDisplay.setText("Student ID: " + studentId);
        } else {
            Log.e(TAG, "Student ID is null or empty at 06:50 PM IST, Thursday, June 12, 2025");
            studentIdDisplay.setText("Student ID: Not Set");
        }

        return root;
    }
}