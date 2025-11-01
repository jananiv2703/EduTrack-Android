package com.school.edutrack.ui.student;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.school.edutrack.R;
import com.school.edutrack.model.AttendanceRecord;
import com.school.edutrack.model.AttendanceResponse;
import com.school.edutrack.network.RetrofitClient;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentAttendanceViewFragment extends Fragment {

    private String studentId;
    private String studentClass;
    private String studentSection;
    private EditText fromDateEditText;
    private EditText toDateEditText;
    private GridLayout calendarGrid;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
    private HashMap<Date, String> attendanceData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            studentId = getArguments().getString("studentId");
            studentClass = getArguments().getString("class");
            studentSection = getArguments().getString("section");
        }
        attendanceData = new HashMap<>();
        fetchAttendanceData();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_student_attendance_view, container, false);

        TextView studentIdDisplay = root.findViewById(R.id.student_id_display);
        TextView classSectionTextView = root.findViewById(R.id.class_section_textview);
        fromDateEditText = root.findViewById(R.id.from_date);
        toDateEditText = root.findViewById(R.id.to_date);
        Button showAttendanceButton = root.findViewById(R.id.show_attendance_button);
        calendarGrid = root.findViewById(R.id.calendar_grid);

        if (studentIdDisplay != null && studentId != null) {
            studentIdDisplay.setText("Attendance View - Student ID: " + studentId);
        }

        if (classSectionTextView != null) {
            if (studentClass != null && studentSection != null) {
                classSectionTextView.setText("Class: " + studentClass + " | Section: " + studentSection);
            } else {
                classSectionTextView.setText("Class and Section not available");
            }
        }

        fromDateEditText.setOnClickListener(v -> showDatePickerDialog(fromDateEditText));
        toDateEditText.setOnClickListener(v -> showDatePickerDialog(toDateEditText));

        showAttendanceButton.setOnClickListener(v -> displayCalendar());

        return root;
    }

    private void showDatePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    editText.setText(dateFormat.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void fetchAttendanceData() {
        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(getContext(), "Student ID is missing", Toast.LENGTH_LONG).show();
            return;
        }

        Call<AttendanceResponse> call = RetrofitClient.getStudentApiService().getStudentAttendance(studentId);
        call.enqueue(new Callback<AttendanceResponse>() {
            @Override
            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AttendanceResponse attendanceResponse = response.body();
                    if ("success".equals(attendanceResponse.getStatus())) {
                        List<AttendanceRecord> records = attendanceResponse.getData();
                        populateAttendanceData(records);
                    } else {
                        Toast.makeText(getContext(), attendanceResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to fetch attendance: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error fetching attendance: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("StudentAttendance", "API Call Failed: " + t.getMessage());
            }
        });
    }

    private void populateAttendanceData(List<AttendanceRecord> records) {
        attendanceData.clear();
        for (AttendanceRecord record : records) {
            try {
                Date date = dateFormat.parse(record.getDate());
                String status = record.getStatus();
                String statusCode;
                switch (status.toLowerCase()) {
                    case "present":
                        statusCode = "P";
                        break;
                    case "absent":
                        statusCode = "A";
                        break;
                    case "leave":
                        statusCode = "L";
                        break;
                    default:
                        continue;
                }
                attendanceData.put(date, statusCode);
            } catch (ParseException e) {
                Log.e("StudentAttendance", "Date Parse Error: " + e.getMessage());
            }
        }
    }

    private void displayCalendar() {
        calendarGrid.removeAllViews();

        try {
            Date fromDate = dateFormat.parse(fromDateEditText.getText().toString());
            Date toDate = dateFormat.parse(toDateEditText.getText().toString());

            if (fromDate == null || toDate == null || fromDate.after(toDate)) {
                Toast.makeText(getContext(), "Invalid date range", Toast.LENGTH_SHORT).show();
                return;
            }

            long diffInMillies = Math.abs(toDate.getTime() - fromDate.getTime());
            int days = (int) TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fromDate);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2;
            if (dayOfWeek < 0) dayOfWeek = 6;

            for (int i = 0; i < dayOfWeek; i++) {
                LinearLayout emptyCell = new LinearLayout(getContext());
                emptyCell.setLayoutParams(new GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED, 1f),
                        GridLayout.spec(GridLayout.UNDEFINED, 1f)
                ));
                emptyCell.setMinimumHeight(60);
                calendarGrid.addView(emptyCell);
            }

            calendar.setTime(fromDate);
            for (int i = 0; i < days; i++) {
                Date currentDate = calendar.getTime();
                boolean isSunday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;

                // Create a tile-like cell
                LinearLayout dayCell = new LinearLayout(getContext());
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED, 1f),
                        GridLayout.spec(GridLayout.UNDEFINED, 1f)
                );
                layoutParams.setMargins(4, 4, 4, 4);
                dayCell.setLayoutParams(layoutParams);
                dayCell.setOrientation(LinearLayout.VERTICAL);
                dayCell.setGravity(Gravity.CENTER);
                dayCell.setPadding(8, 8, 8, 8);
                dayCell.setMinimumHeight(60);
                dayCell.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.tile_background));
                dayCell.setElevation(4f);

                // Date Text
                TextView dateText = new TextView(getContext());
                dateText.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                dateText.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
                dateText.setFontFeatureSettings("@font/poppins");
                dateText.setTextColor(Color.parseColor("#333333"));
                dateText.setTextSize(14);

                // Status Indicator
                LinearLayout statusContainer = new LinearLayout(getContext());
                statusContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                statusContainer.setOrientation(LinearLayout.HORIZONTAL);
                statusContainer.setGravity(Gravity.CENTER);

                View statusIndicator = new View(getContext());
                LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(16, 16);
                statusParams.setMargins(0, 4, 4, 0);
                statusIndicator.setLayoutParams(statusParams);
                statusIndicator.setElevation(2f);

                TextView emojiText = new TextView(getContext());
                emojiText.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                emojiText.setTextSize(14);

                String status = attendanceData.get(currentDate);
                if (isSunday) {
                    status = "L";
                    emojiText.setText("🏖️");
                    statusIndicator.setBackgroundResource(R.drawable.status_leave);
                } else if (status != null) {
                    switch (status) {
                        case "P":
                            statusIndicator.setBackgroundResource(R.drawable.status_present);
                            break;
                        case "A":
                            statusIndicator.setBackgroundResource(R.drawable.status_absent);
                            break;
                        case "L":
                            statusIndicator.setBackgroundResource(R.drawable.status_leave);
                            emojiText.setText("🏖️");
                            break;
                    }
                } else {
                    statusIndicator.setBackgroundResource(android.R.color.transparent);
                }

                statusContainer.addView(statusIndicator);
                if (isSunday || (status != null && status.equals("L"))) {
                    statusContainer.addView(emojiText);
                }

                dayCell.addView(dateText);
                dayCell.addView(statusContainer);

                // Set click listener for tooltip
                final String finalStatus = status != null ? status : "N/A";
                final Date finalDate = currentDate;
                dayCell.setOnClickListener(v -> {
                    String statusMessage;
                    switch (finalStatus) {
                        case "P":
                            statusMessage = "Present";
                            break;
                        case "A":
                            statusMessage = "Absent";
                            break;
                        case "L":
                            statusMessage = "Leave";
                            break;
                        default:
                            statusMessage = "No Record";
                    }
                    String message = displayDateFormat.format(finalDate) + ": " + statusMessage;
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                });

                calendarGrid.addView(dayCell);

                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

        } catch (ParseException e) {
            Toast.makeText(getContext(), "Error parsing dates: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("StudentAttendance", "Parse Exception: " + e.getMessage());
        }
    }
}