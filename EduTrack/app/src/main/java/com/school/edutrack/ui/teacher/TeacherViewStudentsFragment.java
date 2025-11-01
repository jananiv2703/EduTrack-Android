package com.school.edutrack.ui.teacher;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.school.edutrack.R;
import com.school.edutrack.model.AttendanceRecord;
import com.school.edutrack.model.AttendanceResponse;
import com.school.edutrack.model.ClassTeacherResponse;
import com.school.edutrack.model.Student;
import com.school.edutrack.network.RetrofitClient;
import com.school.edutrack.utils.DialogUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherViewStudentsFragment extends Fragment {

    private static final String TAG = "TeacherViewStudents";
    private String teacherId;
    private LinearLayout studentsContainer;
    private ProgressBar loadingSpinner;
    private TextView noStudentsMessage;
    private TextView classSectionDisplay;
    private TextView studentCountDisplay;
    private EditText searchInput;
    private ImageView clearSearchIcon;
    private List<Student> allStudents;
    private List<Student> filteredStudents;
    private HashMap<String, List<AttendanceRecord>> studentAttendanceMap;
    private HashMap<String, Float> attendancePercentageMap;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teacher_view_students, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        TextView teacherIdDisplay = view.findViewById(R.id.teacher_id_display);
        classSectionDisplay = view.findViewById(R.id.class_section_display);
        studentCountDisplay = view.findViewById(R.id.student_count_display);
        searchInput = view.findViewById(R.id.search_input);
        clearSearchIcon = view.findViewById(R.id.clear_search_icon);
        studentsContainer = view.findViewById(R.id.students_container);
        loadingSpinner = view.findViewById(R.id.loading_spinner);
        noStudentsMessage = view.findViewById(R.id.no_students_message);

        // Initialize lists and maps
        allStudents = new ArrayList<>();
        filteredStudents = new ArrayList<>();
        studentAttendanceMap = new HashMap<>();
        attendancePercentageMap = new HashMap<>();

        // Retrieve teacherId from arguments
        if (getArguments() != null) {
            teacherId = getArguments().getString("teacherId");
            Log.d(TAG, "Teacher ID retrieved: " + teacherId);
        } else {
            Log.w(TAG, "Teacher ID not found in arguments");
            DialogUtils.showFailureDialog(requireContext(), "Error", "Teacher ID not found", () -> {
                if (getActivity() != null) getActivity().onBackPressed();
            });
            return;
        }

        // Display teacherId
        if (teacherId != null) {
            teacherIdDisplay.setText("Teacher ID: " + teacherId);
        }

        // Setup search functionality
        setupSearch();

        // Fetch class teacher data
        fetchClassTeacherData();
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String query = s.toString().trim();
                clearSearchIcon.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                filterStudents(query);
            }
        });

        clearSearchIcon.setOnClickListener(v -> {
            searchInput.setText("");
            clearSearchIcon.setVisibility(View.GONE);
        });
    }

    private void filterStudents(String query) {
        filteredStudents.clear();
        if (query.isEmpty()) {
            filteredStudents.addAll(allStudents);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Student student : allStudents) {
                if (student.getName().toLowerCase().contains(lowerQuery) ||
                        student.getRegister_no().toLowerCase().contains(lowerQuery)) {
                    filteredStudents.add(student);
                }
            }
        }
        displayStudents(filteredStudents);
        updateNoStudentsMessage();
    }

    private void fetchClassTeacherData() {
        DialogUtils.showLoadingDialog(requireContext());
        studentsContainer.setVisibility(View.GONE);
        noStudentsMessage.setVisibility(View.GONE);

        RetrofitClient.getApiService().getClassTeacherData(teacherId).enqueue(new Callback<ClassTeacherResponse>() {
            @Override
            public void onResponse(Call<ClassTeacherResponse> call, Response<ClassTeacherResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null) {
                    ClassTeacherResponse classTeacherResponse = response.body();
                    classSectionDisplay.setText("Class: " + classTeacherResponse.getClassName() + " | Section: " + classTeacherResponse.getSection());
                    allStudents.clear();
                    filteredStudents.clear();
                    if (classTeacherResponse.getStudents() != null) {
                        allStudents.addAll(classTeacherResponse.getStudents());
                        filteredStudents.addAll(allStudents);
                        for (Student student : allStudents) {
                            fetchAttendanceData(student.getStudent_id());
                        }
                    }
                    studentCountDisplay.setText("Total Students: " + allStudents.size());

                    if (!allStudents.isEmpty()) {
                        noStudentsMessage.setVisibility(View.GONE);
                        studentsContainer.setVisibility(View.VISIBLE);
                        displayStudents(filteredStudents);
                        DialogUtils.showSuccessDialog(requireContext(), "Success", "Successfully fetched " + allStudents.size() + " students for Class " + classTeacherResponse.getClassName() + "-" + classTeacherResponse.getSection(), null);
                    } else {
                        noStudentsMessage.setVisibility(View.VISIBLE);
                        studentsContainer.setVisibility(View.GONE);
                        DialogUtils.showAlertDialog(requireContext(), "No Students", "No students found for Class " + classTeacherResponse.getClassName() + "-" + classTeacherResponse.getSection(), null);
                    }
                    Log.d(TAG, "Class teacher data fetched: Class " + classTeacherResponse.getClassName() + ", Section " + classTeacherResponse.getSection() + ", Students: " + (classTeacherResponse.getStudents() != null ? classTeacherResponse.getStudents().size() : 0));
                } else {
                    String errorMsg = "Failed to fetch class teacher data: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ", " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading errorBody", e);
                        }
                    }
                    DialogUtils.showFailureDialog(requireContext(), "Fetch Error", errorMsg, null);
                    noStudentsMessage.setVisibility(View.VISIBLE);
                    studentsContainer.setVisibility(View.GONE);
                    studentCountDisplay.setText("Total Students: 0");
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ClassTeacherResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                DialogUtils.showFailureDialog(requireContext(), "Network Error", "Failed to fetch data: " + t.getMessage(), null);
                loadingSpinner.setVisibility(View.GONE);
                noStudentsMessage.setVisibility(View.VISIBLE);
                studentsContainer.setVisibility(View.GONE);
                studentCountDisplay.setText("Total Students: 0");
                Log.e(TAG, "Fetch error: " + t.toString());
            }
        });
    }

    private void fetchAttendanceData(String studentId) {
        if (studentId == null || studentId.isEmpty()) {
            Log.w(TAG, "Student ID is missing for attendance fetch");
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
                        studentAttendanceMap.put(studentId, records);
                        float percentage = calculateAttendancePercentage(records);
                        attendancePercentageMap.put(studentId, percentage);
                        displayStudents(filteredStudents);
                    } else {
                        Log.w(TAG, "Attendance fetch failed for student " + studentId + ": " + attendanceResponse.getMessage());
                        attendancePercentageMap.put(studentId, 0f);
                    }
                } else {
                    Log.e(TAG, "Failed to fetch attendance for student " + studentId + ": HTTP " + response.code());
                    attendancePercentageMap.put(studentId, 0f);
                }
            }

            @Override
            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                Log.e(TAG, "Network error fetching attendance for student " + studentId + ": " + t.getMessage());
                attendancePercentageMap.put(studentId, 0f);
            }
        });
    }

    private float calculateAttendancePercentage(List<AttendanceRecord> records) {
        if (records == null || records.isEmpty()) return 0f;

        Calendar start = Calendar.getInstance();
        start.set(2024, Calendar.JULY, 1);
        Calendar end = Calendar.getInstance();
        end.set(2025, Calendar.JUNE, 30);
        long totalDays = 0;
        int presentDays = 0;

        Calendar current = (Calendar) start.clone();
        while (!current.after(end)) {
            if (current.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                totalDays++;
            }
            current.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (AttendanceRecord record : records) {
            try {
                Date date = dateFormat.parse(record.getDate());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                if (!cal.before(start) && !cal.after(end) && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                    if ("present".equalsIgnoreCase(record.getStatus())) {
                        presentDays++;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date: " + record.getDate(), e);
            }
        }

        return totalDays > 0 ? (presentDays * 100f) / totalDays : 0f;
    }

    private void displayStudents(List<Student> students) {
        studentsContainer.removeAllViews();

        for (Student student : students) {
            CardView cardView = new CardView(requireContext());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 16, 0, 16);
            cardView.setLayoutParams(cardParams);
            cardView.setCardElevation(6f);
            cardView.setRadius(16f);
            cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));
            cardView.setUseCompatPadding(true);
            cardView.setMinimumHeight(140);

            LinearLayout cardContent = new LinearLayout(requireContext());
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(24, 24, 24, 24);
            cardContent.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            // Top Row: Profile Image, Text, and Attendance Button
            LinearLayout topRow = new LinearLayout(requireContext());
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            // Profile Image
            ImageView profileImage = new ImageView(requireContext());
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(100, 100);
            imageParams.setMargins(0, 0, 24, 0);
            profileImage.setLayoutParams(imageParams);
            profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            profileImage.setBackgroundResource(R.drawable.circle_background);
            profileImage.setImageResource(R.drawable.ic_profile);
            profileImage.setColorFilter(null);
            topRow.addView(profileImage);

            // Name and Register Number Container
            LinearLayout textContainer = new LinearLayout(requireContext());
            textContainer.setOrientation(LinearLayout.VERTICAL);
            textContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f));

            TextView nameView = new TextView(requireContext());
            nameView.setText(student.getName() != null ? student.getName() : "N/A");
            nameView.setTextSize(20);
            nameView.setTextColor(getResources().getColor(R.color.text_primary));
            nameView.setTypeface(null, Typeface.BOLD);
            nameView.setFontFeatureSettings("@font/poppins");
            textContainer.addView(nameView);

            TextView registerNoView = new TextView(requireContext());
            registerNoView.setText("Reg: " + (student.getRegister_no() != null ? student.getRegister_no() : "N/A"));
            registerNoView.setTextSize(16);
            registerNoView.setTextColor(getResources().getColor(R.color.text_secondary));
            registerNoView.setFontFeatureSettings("@font/poppins");
            textContainer.addView(registerNoView);

            topRow.addView(textContainer);

            // Attendance Percentage Button
            Button attendanceButton = new Button(requireContext());
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(60, 60);
            buttonParams.setMargins(16, 0, 16, 0);
            buttonParams.gravity = Gravity.RIGHT;
            attendanceButton.setLayoutParams(buttonParams);
            attendanceButton.setBackgroundResource(R.drawable.circle_button_background);
            attendanceButton.setTextColor(getResources().getColor(android.R.color.white));
            attendanceButton.setTextSize(14);
            attendanceButton.setFontFeatureSettings("@font/poppins");
            Float percentage = attendancePercentageMap.get(student.getStudent_id());
            attendanceButton.setText(percentage != null ? String.format(Locale.US, "%.0f%%", percentage) : "0%");
            topRow.addView(attendanceButton);

            cardContent.addView(topRow);

            // Attendance Table
            TableLayout tableLayout = new TableLayout(requireContext());
            tableLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            tableLayout.setStretchAllColumns(true);
            tableLayout.setVisibility(View.GONE);
            tableLayout.setPadding(16, 16, 16, 16);
            tableLayout.setBackgroundColor(getResources().getColor(R.color.table_background));

            // Table Header
            TableRow headerRow = new TableRow(requireContext());
            headerRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            headerRow.setPadding(8, 8, 8, 8);
            headerRow.setBackgroundColor(getResources().getColor(R.color.table_header));

            TextView dateHeader = new TextView(requireContext());
            dateHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            dateHeader.setText("Date");
            dateHeader.setTextSize(16);
            dateHeader.setTextColor(getResources().getColor(R.color.text_primary));
            dateHeader.setTypeface(null, Typeface.BOLD);
            dateHeader.setFontFeatureSettings("@font/poppins");

            TextView statusHeader = new TextView(requireContext());
            statusHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            statusHeader.setText("Status");
            statusHeader.setTextSize(16);
            statusHeader.setTextColor(getResources().getColor(R.color.text_primary));
            statusHeader.setTypeface(null, Typeface.BOLD);
            statusHeader.setFontFeatureSettings("@font/poppins");

            headerRow.addView(dateHeader);
            headerRow.addView(statusHeader);
            tableLayout.addView(headerRow);

            // Table Data
            List<AttendanceRecord> records = studentAttendanceMap.get(student.getStudent_id());
            if (records != null && !records.isEmpty()) {
                for (AttendanceRecord record : records) {
                    try {
                        Date date = dateFormat.parse(record.getDate());
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) continue;

                        TableRow row = new TableRow(requireContext());
                        row.setLayoutParams(new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT));
                        row.setPadding(8, 8, 8, 8);

                        TextView dateView = new TextView(requireContext());
                        dateView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        dateView.setText(record.getDate());
                        dateView.setTextSize(14);
                        dateView.setTextColor(getResources().getColor(R.color.text_primary));
                        dateView.setFontFeatureSettings("@font/poppins");

                        TextView statusView = new TextView(requireContext());
                        statusView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        statusView.setText(record.getStatus() != null ? record.getStatus() : "N/A");
                        statusView.setTextSize(14);
                        statusView.setTextColor(getResources().getColor(R.color.text_primary));
                        statusView.setFontFeatureSettings("@font/poppins");

                        row.addView(dateView);
                        row.addView(statusView);
                        tableLayout.addView(row);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing date: " + record.getDate(), e);
                    }
                }
            } else {
                TableRow row = new TableRow(requireContext());
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                row.setPadding(8, 8, 8, 8);

                TextView noDataView = new TextView(requireContext());
                noDataView.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                noDataView.setText("No attendance records found");
                noDataView.setTextSize(14);
                noDataView.setTextColor(getResources().getColor(R.color.text_secondary));
                noDataView.setFontFeatureSettings("@font/poppins");
                row.addView(noDataView);
                tableLayout.addView(row);
            }

            cardContent.addView(tableLayout);

            // Toggle table visibility on button click
            attendanceButton.setOnClickListener(v -> {
                boolean isVisible = tableLayout.getVisibility() == View.VISIBLE;
                tableLayout.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                attendanceButton.setText(isVisible ? (percentage != null ? String.format(Locale.US, "%.0f%%", percentage) : "0%") : "Hide");
            });

            cardView.addView(cardContent);
            studentsContainer.addView(cardView);
        }
    }

    private void updateNoStudentsMessage() {
        if (filteredStudents.isEmpty()) {
            noStudentsMessage.setVisibility(View.VISIBLE);
            studentsContainer.setVisibility(View.GONE);
        } else {
            noStudentsMessage.setVisibility(View.GONE);
            studentsContainer.setVisibility(View.VISIBLE);
        }
    }
}