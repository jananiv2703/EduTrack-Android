package com.school.edutrack.ui.student;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.school.edutrack.R;
import com.school.edutrack.model.StudentExamSchedule;
import com.school.edutrack.model.StudentExamScore;
import com.school.edutrack.model.StudentExamScoresResponse;
import com.school.edutrack.model.StudentExamSchedulesResponse;
import com.school.edutrack.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudentExamMarksFragment extends Fragment {

    private static final String TAG = "StudentExamMarksFragment";
    private String studentId;
    private LinearLayout examSchedulesContainer, examScoresContainer, examScoresTable;
    private ProgressBar loadingIndicator;
    private Spinner examTypeSpinner;
    private TextView examScoresSummary;
    private List<StudentExamSchedule> examSchedulesList = new ArrayList<>();
    private List<StudentExamScore> examScoresList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            studentId = getArguments().getString("studentId");
            Log.d(TAG, "Student ID retrieved at 12:18 PM IST, Wednesday, June 11, 2025: " + studentId);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_student_exam_marks, container, false);

        // Find views
        TextView studentIdDisplay = root.findViewById(R.id.student_id_display);
        Button buttonViewExamSchedules = root.findViewById(R.id.button_view_exam_schedules);
        Button buttonViewMarks = root.findViewById(R.id.button_view_marks);
        examSchedulesContainer = root.findViewById(R.id.exam_schedules_container);
        examScoresContainer = root.findViewById(R.id.exam_scores_container);
        examTypeSpinner = root.findViewById(R.id.exam_type_spinner);
        examScoresTable = root.findViewById(R.id.exam_scores_table);
        examScoresSummary = root.findViewById(R.id.exam_scores_summary);
        loadingIndicator = root.findViewById(R.id.loading_indicator);

        // Set student ID
        if (studentIdDisplay != null && studentId != null) {
            studentIdDisplay.setText("Exam Marks - Student ID: " + studentId);
        } else {
            Log.e(TAG, "Student ID is null or studentIdDisplay not found at 12:18 PM IST, Wednesday, June 11, 2025");
        }

        // Set click listeners for buttons
        buttonViewExamSchedules.setOnClickListener(v -> {
            examScoresContainer.setVisibility(View.GONE);
            fetchExamSchedules();
        });

        buttonViewMarks.setOnClickListener(v -> {
            examSchedulesContainer.setVisibility(View.GONE);
            fetchStudentMarks();
        });

        return root;
    }

    private void fetchExamSchedules() {
        loadingIndicator.setVisibility(View.VISIBLE);
        loadingIndicator.setAlpha(1f);
        examSchedulesContainer.setVisibility(View.GONE);

        RetrofitClient.getStudentApiService().getExamSchedules("schedules").enqueue(new Callback<StudentExamSchedulesResponse>() {
            @Override
            public void onResponse(Call<StudentExamSchedulesResponse> call, Response<StudentExamSchedulesResponse> response) {
                loadingIndicator.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                loadingIndicator.setVisibility(View.GONE);
                            }
                        })
                        .start();

                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    examSchedulesList = response.body().getSchedules();
                    Log.d(TAG, "Fetched " + examSchedulesList.size() + " exam schedules at 12:18 PM IST, Wednesday, June 11, 2025");
                    displayExamSchedules();
                } else {
                    String errorMessage = "Failed to fetch exam schedules: " + (response.body() != null ? response.body().getStatus() : "Unknown error");
                    Log.e(TAG, errorMessage + " at 12:18 PM IST, Wednesday, June 11, 2025");
                }
            }

            @Override
            public void onFailure(Call<StudentExamSchedulesResponse> call, Throwable t) {
                loadingIndicator.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                loadingIndicator.setVisibility(View.GONE);
                            }
                        })
                        .start();
                Log.e(TAG, "Error fetching exam schedules: " + t.getMessage() + " at 12:18 PM IST, Wednesday, June 11, 2025");
            }
        });
    }

    private void displayExamSchedules() {
        examSchedulesContainer.removeAllViews();
        examSchedulesContainer.setVisibility(View.VISIBLE);
        examSchedulesContainer.setAlpha(0f);
        examSchedulesContainer.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        for (StudentExamSchedule schedule : examSchedulesList) {
            // Create expandable card
            LinearLayout card = new LinearLayout(getContext());
            card.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.card_background);
            card.setPadding(0, 0, 0, 0);
            card.setElevation(4);
            LinearLayout.LayoutParams cardParams = (LinearLayout.LayoutParams) card.getLayoutParams();
            cardParams.setMargins(0, 12, 0, 12);
            card.setLayoutParams(cardParams);

            // Card Header (Title)
            LinearLayout header = new LinearLayout(getContext());
            header.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setBackgroundResource(R.drawable.card_header_background);
            header.setPadding(20, 16, 20, 16);

            TextView title = new TextView(getContext());
            title.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            title.setText(schedule.getExamName());
            title.setTextSize(18);
            title.setTextColor(getResources().getColor(R.color.header_text_color));
            title.setTypeface(null, Typeface.BOLD);

            TextView arrow = new TextView(getContext());
            arrow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            arrow.setText("▼");
            arrow.setTextSize(16);
            arrow.setTextColor(getResources().getColor(R.color.icon_color));

            header.addView(title);
            header.addView(arrow);

            // Card Details (Hidden by Default)
            LinearLayout details = new LinearLayout(getContext());
            details.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            details.setOrientation(LinearLayout.VERTICAL);
            details.setPadding(20, 16, 20, 16);
            details.setVisibility(View.GONE);

            addDetailRow(details, "Exam Type", schedule.getExamType());
            addDivider(details);
            addDetailRow(details, "Class", schedule.getClassName());
            addDivider(details);
            addDetailRow(details, "Section", schedule.getSection());
            addDivider(details);
            addDetailRow(details, "Date", schedule.getExamDate());
            addDivider(details);
            addDetailRow(details, "Day", schedule.getExamDay());
            addDivider(details);
            addDetailRow(details, "Time", schedule.getStartTime() + " - " + schedule.getEndTime());
            addDivider(details);
            addDetailRow(details, "Venue", schedule.getClassVenue());
            addDivider(details);
            addDetailRow(details, "Seating Arrangement", schedule.getSeatingArrangement());
            addDivider(details);
            addDetailRow(details, "Room Number", schedule.getRoomNumber());
            addDivider(details);
            addDetailRow(details, "Teacher ID", schedule.getTeacherId());
            addDivider(details);
            addDetailRow(details, "Created At", schedule.getCreatedAt());

            card.addView(header);
            card.addView(details);

            // Toggle visibility on header click with animation
            header.setOnClickListener(v -> {
                if (details.getVisibility() == View.VISIBLE) {
                    details.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    details.setVisibility(View.GONE);
                                    details.setAlpha(1f);
                                }
                            })
                            .start();
                    arrow.setText("▼");
                    card.setElevation(4);
                } else {
                    details.setAlpha(0f);
                    details.setVisibility(View.VISIBLE);
                    details.animate()
                            .alpha(1f)
                            .setDuration(200)
                            .start();
                    arrow.setText("▲");
                    card.setElevation(6);
                }
            });

            examSchedulesContainer.addView(card);
        }
    }

    private void addDetailRow(LinearLayout container, String label, String value) {
        LinearLayout row = new LinearLayout(getContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        TextView labelText = new TextView(getContext());
        labelText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        labelText.setText(label + ":");
        labelText.setTextSize(16);
        labelText.setTextColor(getResources().getColor(R.color.text_color));
        labelText.setTypeface(null, Typeface.BOLD);

        TextView valueText = new TextView(getContext());
        valueText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        valueText.setText(value != null ? value : "-");
        valueText.setTextSize(16);
        valueText.setTextColor(getResources().getColor(R.color.value_color));
        valueText.setGravity(android.view.Gravity.END);

        row.addView(labelText);
        row.addView(valueText);
        container.addView(row);
    }

    private void addDivider(LinearLayout container) {
        View divider = new View(getContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        divider.setBackgroundColor(getResources().getColor(R.color.divider_color));
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        container.addView(divider);
    }

    private void fetchStudentMarks() {
        if (studentId == null || studentId.isEmpty()) {
            Log.e(TAG, "Student ID is null or empty, cannot fetch marks at 12:18 PM IST, Wednesday, June 11, 2025");
            return;
        }

        loadingIndicator.setVisibility(View.VISIBLE);
        loadingIndicator.setAlpha(1f);
        examScoresContainer.setVisibility(View.GONE);

        RetrofitClient.getStudentApiService().getStudentMarks("marks", studentId).enqueue(new Callback<StudentExamScoresResponse>() {
            @Override
            public void onResponse(Call<StudentExamScoresResponse> call, Response<StudentExamScoresResponse> response) {
                loadingIndicator.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                loadingIndicator.setVisibility(View.GONE);
                            }
                        })
                        .start();

                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    examScoresList = response.body().getMarks();
                    Log.d(TAG, "Fetched " + examScoresList.size() + " marks for studentId: " + studentId + " at 12:18 PM IST, Wednesday, June 11, 2025");
                    setupExamTypeSpinner();
                    examScoresContainer.setVisibility(View.VISIBLE);
                    examScoresContainer.setAlpha(0f);
                    examScoresContainer.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start();
                } else {
                    String errorMessage = "Failed to fetch marks: " + (response.body() != null ? response.body().getStatus() : "Unknown error");
                    Log.e(TAG, errorMessage + " at 12:18 PM IST, Wednesday, June 11, 2025");
                }
            }

            @Override
            public void onFailure(Call<StudentExamScoresResponse> call, Throwable t) {
                loadingIndicator.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                loadingIndicator.setVisibility(View.GONE);
                            }
                        })
                        .start();
                Log.e(TAG, "Error fetching marks: " + t.getMessage() + " at 12:18 PM IST, Wednesday, June 11, 2025");
            }
        });
    }

    private void setupExamTypeSpinner() {
        // Get unique exam types
        Set<String> examTypesSet = new HashSet<>();
        for (StudentExamScore score : examScoresList) {
            if (score.getExamType() != null) {
                examTypesSet.add(score.getExamType());
            }
        }
        List<String> examTypes = new ArrayList<>(examTypesSet);
        if (examTypes.isEmpty()) {
            examTypes.add("No Exam Types Available");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, examTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        examTypeSpinner.setAdapter(adapter);

        examTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedExamType = examTypes.get(position);
                displayExamScores(selectedExamType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void displayExamScores(String examType) {
        examScoresTable.removeAllViews();

        // Filter scores by exam type
        List<StudentExamScore> filteredScores = new ArrayList<>();
        for (StudentExamScore score : examScoresList) {
            if (examType.equals(score.getExamType())) {
                filteredScores.add(score);
            }
        }

        if (filteredScores.isEmpty()) {
            TextView noDataText = new TextView(getContext());
            noDataText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            noDataText.setText("No scores available for this exam type");
            noDataText.setTextSize(16);
            noDataText.setTextColor(getResources().getColor(R.color.text_color));
            noDataText.setGravity(android.view.Gravity.CENTER);
            noDataText.setPadding(0, 16, 0, 16);
            examScoresTable.addView(noDataText);
            examScoresSummary.setText("");
            return;
        }

        // Create table header
        LinearLayout headerRow = new LinearLayout(getContext());
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setBackgroundResource(R.drawable.table_header_background);
        headerRow.setPadding(12, 12, 12, 12);

        String[] headers = {"Subject", "Marks Obtained", "Max Marks", "Grade"};
        for (String header : headers) {
            TextView headerText = new TextView(getContext());
            headerText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            headerText.setText(header);
            headerText.setTextColor(getResources().getColor(android.R.color.white));
            headerText.setTextSize(16);
            headerText.setTypeface(null, Typeface.BOLD);
            headerText.setGravity(android.view.Gravity.CENTER);
            headerText.setPadding(8, 8, 8, 8);
            headerRow.addView(headerText);
        }
        examScoresTable.addView(headerRow);

        // Add scores rows
        double totalMarksObtained = 0;
        double totalMaxMarks = 0;
        int subjectCount = 0;

        for (int i = 0; i < filteredScores.size(); i++) {
            StudentExamScore score = filteredScores.get(i);
            if (score.getSubject() == null || score.getMarksObtained() == null || score.getMaxMarks() == null) {
                continue; // Skip incomplete entries
            }

            LinearLayout row = new LinearLayout(getContext());
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(12, 12, 12, 12);
            row.setBackgroundColor(i % 2 == 0 ? getResources().getColor(R.color.card_background) : getResources().getColor(R.color.table_alternate_row));

            // Subject
            TextView subjectText = new TextView(getContext());
            subjectText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            subjectText.setText(score.getSubject());
            subjectText.setTextSize(16);
            subjectText.setTextColor(getResources().getColor(R.color.subject_color));
            subjectText.setGravity(android.view.Gravity.CENTER);
            subjectText.setPadding(8, 8, 8, 8);

            // Marks Obtained
            TextView marksObtainedText = new TextView(getContext());
            marksObtainedText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            marksObtainedText.setText(String.valueOf(score.getMarksObtained()));
            marksObtainedText.setTextSize(16);
            marksObtainedText.setTextColor(getResources().getColor(R.color.text_color));
            marksObtainedText.setGravity(android.view.Gravity.CENTER);
            marksObtainedText.setPadding(8, 8, 8, 8);

            // Max Marks
            TextView maxMarksText = new TextView(getContext());
            maxMarksText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            maxMarksText.setText(String.valueOf(score.getMaxMarks()));
            maxMarksText.setTextSize(16);
            maxMarksText.setTextColor(getResources().getColor(R.color.text_color));
            maxMarksText.setGravity(android.view.Gravity.CENTER);
            maxMarksText.setPadding(8, 8, 8, 8);

            // Grade
            double percentage = (score.getMarksObtained() * 100.0) / score.getMaxMarks();
            String grade = calculateGrade(percentage);
            TextView gradeText = new TextView(getContext());
            gradeText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            gradeText.setText(grade);
            gradeText.setTextSize(16);
            gradeText.setTextColor(getResources().getColor(grade.startsWith("A") ? R.color.teacher_color : R.color.text_color));
            gradeText.setGravity(android.view.Gravity.CENTER);
            gradeText.setPadding(8, 8, 8, 8);

            row.addView(subjectText);
            row.addView(marksObtainedText);
            row.addView(maxMarksText);
            row.addView(gradeText);
            examScoresTable.addView(row);

            // Accumulate for average
            totalMarksObtained += score.getMarksObtained();
            totalMaxMarks += score.getMaxMarks();
            subjectCount++;
        }

        // Calculate average and overall grade
        if (subjectCount > 0) {
            double averagePercentage = (totalMarksObtained * 100.0) / totalMaxMarks;
            String overallGrade = calculateGrade(averagePercentage);
            String summaryText = "Average: " + String.format("%.2f", averagePercentage) + "%\n" +
                    "Overall Grade: " + overallGrade + "\n" +
                    "Congratulations on your performance!";
            examScoresSummary.setText(summaryText);
        } else {
            examScoresSummary.setText("No valid scores to calculate average.");
        }
    }

    private String calculateGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 50) return "D";
        return "F";
    }
}