package com.school.edutrack.ui.student;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.school.edutrack.R;
import com.school.edutrack.model.TimetableModel;
import com.school.edutrack.model.StudentTimetableResponse;
import com.school.edutrack.network.RetrofitClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentTimeTableFragment extends Fragment {

    private static final String TAG = "StudentTimeTableFragment";
    private String studentId;
    private LinearLayout timetableContainer;
    private TextView mondayTab, tuesdayTab, wednesdayTab, thursdayTab, fridayTab;
    private TextView[] dayTabs;
    private Map<String, List<TimetableModel>> timetableByDay;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            studentId = getArguments().getString("studentId");
            Log.d(TAG, "Student ID retrieved at 11:34 AM IST, Wednesday, June 11, 2025: " + studentId);
        }
        timetableByDay = new HashMap<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_student_time_table, container, false);

        // Find views
        TextView studentIdDisplay = root.findViewById(R.id.student_id_display);
        timetableContainer = root.findViewById(R.id.timetable_container);
        mondayTab = root.findViewById(R.id.day_monday);
        tuesdayTab = root.findViewById(R.id.day_tuesday);
        wednesdayTab = root.findViewById(R.id.day_wednesday);
        thursdayTab = root.findViewById(R.id.day_thursday);
        fridayTab = root.findViewById(R.id.day_friday);

        dayTabs = new TextView[]{mondayTab, tuesdayTab, wednesdayTab, thursdayTab, fridayTab};

        // Set student ID
        if (studentIdDisplay != null && studentId != null) {
            studentIdDisplay.setText("Time Table - Student ID: " + studentId);
            studentIdDisplay.setTextColor(getResources().getColor(R.color.header_text_color));
            studentIdDisplay.setTextSize(20);
            studentIdDisplay.setTypeface(null, Typeface.BOLD);
        } else {
            Log.e(TAG, "Student ID is null or studentIdDisplay not found at 11:34 AM IST, Wednesday, June 11, 2025");
        }

        // Set up day tab click listeners with enhanced styling
        for (TextView tab : dayTabs) {
            tab.setBackgroundResource(R.drawable.day_tab_unselected); // Default unselected state
            tab.setTextColor(getResources().getColor(R.color.day_tab_unselected));
            tab.setTextSize(16);
            tab.setTypeface(null, Typeface.NORMAL);
            tab.setPadding(20, 10, 20, 10);
        }

        mondayTab.setOnClickListener(v -> selectDay("Monday"));
        tuesdayTab.setOnClickListener(v -> selectDay("Tuesday"));
        wednesdayTab.setOnClickListener(v -> selectDay("Wednesday"));
        thursdayTab.setOnClickListener(v -> selectDay("Thursday"));
        fridayTab.setOnClickListener(v -> selectDay("Friday"));

        // Fetch timetable data
        fetchTimetable(studentId);

        // Select Wednesday by default (since today is Wednesday)
        selectDay("Wednesday");

        return root;
    }

    private void fetchTimetable(String studentId) {
        if (studentId == null || studentId.isEmpty()) {
            Log.e(TAG, "Student ID is null or empty, cannot fetch timetable at 11:34 AM IST, Wednesday, June 11, 2025");
            return;
        }

        RetrofitClient.getStudentApiService().getTimetable(studentId).enqueue(new Callback<StudentTimetableResponse>() {
            @Override
            public void onResponse(Call<StudentTimetableResponse> call, Response<StudentTimetableResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    List<TimetableModel> timetableEntries = response.body().getTimetable();
                    Log.d(TAG, "Fetched " + timetableEntries.size() + " timetable entries for studentId: " + studentId + " at 11:34 AM IST, Wednesday, June 11, 2025");

                    // Organize timetable entries by day
                    timetableByDay.clear();
                    timetableByDay.put("Monday", new ArrayList<>());
                    timetableByDay.put("Tuesday", new ArrayList<>());
                    timetableByDay.put("Wednesday", new ArrayList<>());
                    timetableByDay.put("Thursday", new ArrayList<>());
                    timetableByDay.put("Friday", new ArrayList<>());

                    for (TimetableModel entry : timetableEntries) {
                        String day = entry.getDayOfWeek();
                        if (timetableByDay.containsKey(day)) {
                            timetableByDay.get(day).add(entry);
                        }
                    }

                    // Refresh the currently selected day
                    for (TextView tab : dayTabs) {
                        if (tab.isSelected()) {
                            selectDay(tab.getText().toString());
                            break;
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to fetch timetable: " + response.code() + " at 11:34 AM IST, Wednesday, June 11, 2025");
                }
            }

            @Override
            public void onFailure(Call<StudentTimetableResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching timetable: " + t.getMessage() + " at 11:34 AM IST, Wednesday, June 11, 2025");
            }
        });
    }

    private void selectDay(String day) {
        // Update tab selection state with enhanced styling
        for (TextView tab : dayTabs) {
            boolean isSelected = tab.getText().toString().equals(day);
            tab.setSelected(isSelected);
            if (isSelected) {
                tab.setBackgroundResource(R.drawable.day_tab_selected);
                tab.setTextColor(getResources().getColor(R.color.day_tab_selected));
                tab.setTypeface(null, Typeface.BOLD);
                tab.setElevation(8);
            } else {
                tab.setBackgroundResource(R.drawable.day_tab_unselected);
                tab.setTextColor(getResources().getColor(R.color.day_tab_unselected));
                tab.setTypeface(null, Typeface.NORMAL);
                tab.setElevation(0);
            }
        }

        // Clear previous timetable
        timetableContainer.removeAllViews();

        // Timings for periods
        String[][] timings = {
                {"9:00 AM", "10:00 AM"},
                {"10:15 AM", "11:15 AM"},
                {"11:30 AM", "12:30 PM"},
                {"1:00 PM", "2:00 PM"},
                {"2:00 PM", "3:00 PM"},
                {"3:00 PM", "4:00 PM"}
        };

        // Get timetable entries for the selected day
        List<TimetableModel> dayEntries = timetableByDay.getOrDefault(day, new ArrayList<>());
        Map<Integer, TimetableModel> periodMap = new HashMap<>();
        for (TimetableModel entry : dayEntries) {
            periodMap.put(entry.getPeriodNo(), entry);
        }

        // Create table header
        LinearLayout headerRow = new LinearLayout(getContext());
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setBackgroundResource(R.drawable.table_header_background_enhanced);
        headerRow.setPadding(12, 12, 12, 12);

        // Header: Time
        TextView timeHeader = new TextView(getContext());
        timeHeader.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        timeHeader.setText("Time");
        timeHeader.setTextColor(getResources().getColor(android.R.color.white));
        timeHeader.setTextSize(18);
        timeHeader.setTypeface(null, Typeface.BOLD);
        timeHeader.setPadding(12, 12, 12, 12);
        timeHeader.setGravity(android.view.Gravity.CENTER);

        // Header: Subject
        TextView subjectHeader = new TextView(getContext());
        subjectHeader.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        subjectHeader.setText("Subject");
        subjectHeader.setTextColor(getResources().getColor(android.R.color.white));
        subjectHeader.setTextSize(18);
        subjectHeader.setTypeface(null, Typeface.BOLD);
        subjectHeader.setPadding(12, 12, 12, 12);
        subjectHeader.setGravity(android.view.Gravity.CENTER);

        // Header: Teacher
        TextView teacherHeader = new TextView(getContext());
        teacherHeader.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        teacherHeader.setText("Faculty");
        teacherHeader.setTextColor(getResources().getColor(android.R.color.white));
        teacherHeader.setTextSize(18);
        teacherHeader.setTypeface(null, Typeface.BOLD);
        teacherHeader.setPadding(12, 12, 12, 12);
        teacherHeader.setGravity(android.view.Gravity.CENTER);

        headerRow.addView(timeHeader);
        headerRow.addView(subjectHeader);
        headerRow.addView(teacherHeader);
        timetableContainer.addView(headerRow);

        // Display the timetable as a table
        for (int period = 1; period <= 6; period++) {
            // Row for each period
            LinearLayout row = new LinearLayout(getContext());
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBackgroundResource(R.drawable.table_row_background_enhanced);
            row.setPadding(12, 12, 12, 12);
            row.setElevation(4);
            LinearLayout.LayoutParams rowParams = (LinearLayout.LayoutParams) row.getLayoutParams();
            rowParams.setMargins(0, 4, 0, 4);
            row.setLayoutParams(rowParams);

            // Column 1: Time
            TextView timeText = new TextView(getContext());
            timeText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            timeText.setText(timings[period - 1][0] + " - " + timings[period - 1][1]);
            timeText.setTextColor(getResources().getColor(R.color.text_color));
            timeText.setTextSize(16);
            timeText.setPadding(12, 12, 12, 12);
            timeText.setGravity(android.view.Gravity.CENTER);

            // Column 2: Subject
            TextView subjectText = new TextView(getContext());
            subjectText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            subjectText.setTextColor(getResources().getColor(R.color.subject_color));
            subjectText.setTextSize(18);
            subjectText.setTypeface(null, Typeface.BOLD);
            subjectText.setPadding(12, 12, 12, 12);
            subjectText.setGravity(android.view.Gravity.CENTER);

            // Column 3: Teacher
            TextView teacherText = new TextView(getContext());
            teacherText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            teacherText.setTextColor(getResources().getColor(R.color.teacher_color));
            teacherText.setTextSize(16);
            teacherText.setTypeface(null, Typeface.BOLD_ITALIC);
            teacherText.setPadding(12, 12, 12, 12);
            teacherText.setGravity(android.view.Gravity.CENTER);

            // Populate subject and teacher if period exists
            TimetableModel entry = periodMap.get(period);
            if (entry != null) {
                subjectText.setText(entry.getSubject());
                teacherText.setText(entry.getTeacherName() != null ? entry.getTeacherName() : "TBA");
            } else {
                subjectText.setText("Free");
                teacherText.setText("-");
            }

            row.addView(timeText);
            row.addView(subjectText);
            row.addView(teacherText);
            timetableContainer.addView(row);

            // Add breaks with enhanced labels and styling
            if (period == 1 || period == 2 || period == 3) {
                TextView breakText = new TextView(getContext());
                breakText.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                if (period == 1) {
                    breakText.setText("Morning Break (10:00 AM - 10:15 AM)");
                } else if (period == 2) {
                    breakText.setText("Mid-Morning Break (11:15 AM - 11:30 AM)");
                } else {
                    breakText.setText("Lunch Break (12:30 PM - 1:00 PM)");
                }
                breakText.setTextColor(getResources().getColor(R.color.break_text_color));
                breakText.setTextSize(14);
                breakText.setGravity(android.view.Gravity.CENTER);
                breakText.setBackgroundResource(R.drawable.break_background);
                breakText.setPadding(0, 12, 0, 12);
                breakText.setElevation(2);
                LinearLayout.LayoutParams breakParams = (LinearLayout.LayoutParams) breakText.getLayoutParams();
                breakParams.setMargins(0, 4, 0, 4);
                breakText.setLayoutParams(breakParams);
                timetableContainer.addView(breakText);
            }
        }
    }
}