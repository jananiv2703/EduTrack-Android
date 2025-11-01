package com.school.edutrack.ui.teacher;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.school.edutrack.R;
import com.school.edutrack.model.Attendance;
import com.school.edutrack.model.AttendanceResponse;
import com.school.edutrack.model.Student;
import com.school.edutrack.model.StudentResponse;
import com.school.edutrack.network.ApiService;
import com.school.edutrack.network.RetrofitClient;
import com.school.edutrack.utils.DialogUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherAttendanceManagementFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "TeacherAttendanceManagementFragment";

    private String teacherId;
    private MaterialTextView teacherIdDisplay;
    private MaterialAutoCompleteTextView classDropdown, sectionDropdown;
    private TextInputEditText dateInput;
    private LinearLayout studentsContainer;
    private ProgressBar loadingSpinner;
    private ApiService apiService;
    private List<Student> studentsList;
    private String selectedClass, selectedSection;
    private String selectedDate;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        logWithTimestamp("Creating view for TeacherAttendanceManagementFragment");
        rootView = inflater.inflate(R.layout.fragment_teacher_attendance_management, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logWithTimestamp("View created, initializing components");

        // Initialize UI components
        teacherIdDisplay = rootView.findViewById(R.id.teacher_id_display);
        classDropdown = rootView.findViewById(R.id.class_dropdown);
        sectionDropdown = rootView.findViewById(R.id.section_dropdown);
        dateInput = rootView.findViewById(R.id.date_input);
        studentsContainer = rootView.findViewById(R.id.students_container);
        loadingSpinner = rootView.findViewById(R.id.loading_spinner);

        // Initialize Retrofit API service
        apiService = RetrofitClient.getApiService();
        logWithTimestamp("Retrofit API service initialized");

        // Retrieve teacherId from arguments
        if (getArguments() != null) {
            teacherId = getArguments().getString("teacherId");
            logWithTimestamp("Teacher ID retrieved from arguments: " + teacherId);
        } else {
            teacherId = "T123"; // Fallback for testing
            Log.w(TAG, getTimestamp() + " - Teacher ID not found in arguments, using fallback: " + teacherId);
        }

        // Display header
        teacherIdDisplay.setText("Attendance Management");

        // Setup dropdowns
        setupDropdowns();

        // Setup date picker
        setupDatePicker();

        // Setup submit button listener
        rootView.findViewById(R.id.submit_attendance_button).setOnClickListener(v -> submitAttendance());
        logWithTimestamp("Setup complete, ready for user interaction");
    }

    private void setupDropdowns() {
        logWithTimestamp("Setting up dropdowns for class and section");

        // Class Dropdown
        List<String> classes = new ArrayList<>();
        classes.add("Select Class");
        for (int i = 1; i <= 12; i++) {
            classes.add("Class " + i);
        }
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, classes);
        classDropdown.setAdapter(classAdapter);
        classDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedClass = parent.getItemAtPosition(position).toString();
            logWithTimestamp("Class selected: " + selectedClass);
            if (selectedClass != null && !selectedClass.equals("Select Class") &&
                    selectedSection != null && !selectedSection.equals("Select Section") &&
                    selectedDate != null && !selectedDate.isEmpty()) {
                logWithTimestamp("Class, section, and date selected, proceeding to fetch students");
                fetchStudents();
            }
        });
        logWithTimestamp("Class dropdown initialized with options: " + classes);

        // Section Dropdown
        List<String> sections = Arrays.asList("Select Section", "A", "B", "C", "D");
        ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, sections);
        sectionDropdown.setAdapter(sectionAdapter);
        sectionDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedSection = parent.getItemAtPosition(position).toString();
            logWithTimestamp("Section selected: " + selectedSection);
            if (selectedClass != null && !selectedClass.equals("Select Class") &&
                    selectedSection != null && !selectedSection.equals("Select Section") &&
                    selectedDate != null && !selectedDate.isEmpty()) {
                logWithTimestamp("Class, section, and date selected, proceeding to fetch students");
                fetchStudents();
            }
        });
        logWithTimestamp("Section dropdown initialized with options: " + sections);
    }

    private void setupDatePicker() {
        logWithTimestamp("Setting up date picker");
        dateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year1, month1, dayOfMonth) -> {
                        month1 = month1 + 1; // Months are 0-based
                        String date = String.format(Locale.US, "%d-%02d-%02d", year1, month1, dayOfMonth);
                        dateInput.setText(date);
                        selectedDate = date;
                        logWithTimestamp("Date selected: " + date);
                        if (selectedClass != null && !selectedClass.equals("Select Class") &&
                                selectedSection != null && !selectedSection.equals("Select Section")) {
                            fetchStudents();
                        }
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // No-op: Handled by setOnItemClickListener in setupDropdowns
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        logWithTimestamp("No selection made in dropdown");
    }

    private void fetchStudents() {
        String classNumber = selectedClass.split(" ")[1];
        logWithTimestamp("Fetching students for class: " + classNumber + ", section: " + selectedSection);

        DialogUtils.showLoadingDialog(requireContext());
        studentsContainer.setVisibility(View.GONE);
        rootView.findViewById(R.id.submit_attendance_button).setVisibility(View.GONE);

        Call<StudentResponse> call = apiService.getStudents(classNumber, selectedSection);
        logWithTimestamp("Sending GET request to fetch students: " +
                "http://10.0.2.2/edutrack-backend/api/teacher/attendance_marking.php?class=" + classNumber +
                "§ion=" + selectedSection);

        call.enqueue(new Callback<StudentResponse>() {
            @Override
            public void onResponse(Call<StudentResponse> call, Response<StudentResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null) {
                    studentsList = response.body().getStudents();
                    logWithTimestamp("GET request successful, received " + (studentsList != null ? studentsList.size() : 0) + " students");
                    if (studentsList == null || studentsList.isEmpty()) {
                        Log.w(TAG, getTimestamp() + " - No students found for class: " + classNumber + ", section: " + selectedSection);
                        DialogUtils.showAlertDialog(requireContext(), "No Students", "No students found", null);
                        return;
                    }
                    fetchAttendanceRecords(classNumber, selectedSection);
                } else {
                    String errorMsg = "Failed to fetch students. Response code: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ", " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, getTimestamp() + " - Error reading error body", e);
                        }
                    }
                    Log.e(TAG, getTimestamp() + " - " + errorMsg);
                    DialogUtils.showFailureDialog(requireContext(), "Error", errorMsg, null);
                }
            }

            @Override
            public void onFailure(Call<StudentResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                Log.e(TAG, getTimestamp() + " - Network error fetching students: " + t.getMessage(), t);
                DialogUtils.showFailureDialog(requireContext(), "Network Error", t.getMessage(), null);
            }
        });
    }

    private void fetchAttendanceRecords(String classNumber, String section) {
        logWithTimestamp("Fetching attendance records for date: " + selectedDate);

        Call<List<Attendance>> call = apiService.getAttendanceRecords(classNumber, section, selectedDate);
        logWithTimestamp("Sending GET request to fetch attendance records: " +
                "http://10.0.2.2/edutrack-backend/api/teacher/attendance_marking.php?class=" + classNumber +
                "§ion=" + section + "&date=" + selectedDate);

        call.enqueue(new Callback<List<Attendance>>() {
            @Override
            public void onResponse(Call<List<Attendance>> call, Response<List<Attendance>> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null) {
                    List<Attendance> attendanceRecords = response.body();
                    logWithTimestamp("Fetched " + (attendanceRecords != null ? attendanceRecords.size() : 0) + " attendance records");

                    HashMap<String, Attendance> attendanceMap = new HashMap<>();
                    if (attendanceRecords != null) {
                        for (Attendance record : attendanceRecords) {
                            attendanceMap.put(record.getStudentId(), record);
                        }
                    }

                    if (studentsList != null) {
                        for (Student student : studentsList) {
                            Attendance record = attendanceMap.get(student.getStudent_id());
                            if (record != null) {
                                student.setStatus(record.getStatus());
                                student.setDate(record.getDate());
                            } else {
                                student.setStatus(null);
                                student.setDate(null);
                            }
                        }
                    }

                    displayStudents();
                } else {
                    String errorMsg = "Failed to fetch attendance records. Response code: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ", " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, getTimestamp() + " - Error reading error body", e);
                        }
                    }
                    Log.e(TAG, getTimestamp() + " - " + errorMsg);
                    DialogUtils.showFailureDialog(requireContext(), "Error", errorMsg, null);
                    displayStudents(); // Proceed without attendance data
                }
            }

            @Override
            public void onFailure(Call<List<Attendance>> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                Log.e(TAG, getTimestamp() + " - Network error fetching attendance records: " + t.getMessage(), t);
                DialogUtils.showFailureDialog(requireContext(), "Network Error", t.getMessage(), null);
                displayStudents(); // Proceed without attendance data
            }
        });
    }

    private void displayStudents() {
        studentsContainer.removeAllViews();
        if (studentsList != null) {
            studentsContainer.setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.submit_attendance_button).setVisibility(View.VISIBLE);

            for (Student student : studentsList) {
                View studentView = LayoutInflater.from(requireContext()).inflate(R.layout.item_attendance, studentsContainer, false);

                ImageView profileImage = studentView.findViewById(R.id.profile_logo);
                TextView idView = studentView.findViewById(R.id.student_id);
                SwitchMaterial attendanceSwitch = studentView.findViewById(R.id.attendance_toggle);

                idView.setText("ID: " + (student.getStudent_id() != null ? student.getStudent_id() : "N/A"));
                profileImage.setImageResource(R.drawable.ic_profile);
                attendanceSwitch.setChecked(student.getStatus() != null && student.getStatus().equalsIgnoreCase("Present"));
                attendanceSwitch.setTag(student.getStudent_id() != null ? student.getStudent_id() : "");

                studentsContainer.addView(studentView);
            }
        }
    }

    private void submitAttendance() {
        String date = dateInput.getText().toString().trim();
        if (date.isEmpty()) {
            Log.w(TAG, getTimestamp() + " - Submit attempted but date is empty");
            DialogUtils.showAlertDialog(requireContext(), "Error", "Please select a date", null);
            return;
        }
        logWithTimestamp("Date selected for submission: " + date);

        List<Attendance> attendanceRecords = new ArrayList<>();
        if (studentsContainer != null) {
            for (int i = 0; i < studentsContainer.getChildCount(); i++) {
                View studentView = studentsContainer.getChildAt(i);
                SwitchMaterial switchCompat = studentView.findViewById(R.id.attendance_toggle);
                String studentId = (String) switchCompat.getTag();
                if (studentId != null && !studentId.isEmpty()) {
                    boolean isPresent = switchCompat.isChecked();
                    Attendance attendance = new Attendance(studentId, date, isPresent ? "Present" : "Absent", teacherId);
                    attendanceRecords.add(attendance);
                }
            }
        }

        if (attendanceRecords.isEmpty()) {
            Log.w(TAG, getTimestamp() + " - Submit attempted but no attendance records marked");
            DialogUtils.showAlertDialog(requireContext(), "Error", "Please mark attendance for at least one student", null);
            return;
        }

        logWithTimestamp("Submitting attendance for " + attendanceRecords.size() + " students");

        DialogUtils.showLoadingDialog(requireContext());

        Call<AttendanceResponse> call = apiService.updateAttendance(attendanceRecords);
        logWithTimestamp("Sending PUT request to update attendance: " +
                "http://10.0.2.2/edutrack-backend/api/teacher/attendance_marking.php with body: " + attendanceRecords.toString());

        call.enqueue(new Callback<AttendanceResponse>() {
            @Override
            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null) {
                    logWithTimestamp("PUT request successful, response: " + response.body().getMessage());
                    DialogUtils.showSuccessDialog(requireContext(), "Success", response.body().getMessage(), null);
                    if (studentsContainer != null) {
                        studentsContainer.setVisibility(View.GONE);
                    }
                    rootView.findViewById(R.id.submit_attendance_button).setVisibility(View.GONE);
                    classDropdown.setText(null);
                    sectionDropdown.setText(null);
                    dateInput.setText("");
                    selectedDate = null;
                    logWithTimestamp("UI reset after successful submission");
                } else {
                    String errorMsg = "Failed to submit attendance. Response code: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ", " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, getTimestamp() + " - Error reading error body", e);
                        }
                    }
                    Log.e(TAG, getTimestamp() + " - " + errorMsg);
                    DialogUtils.showFailureDialog(requireContext(), "Error", errorMsg, null);
                }
            }

            @Override
            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                Log.e(TAG, getTimestamp() + " - Network error submitting attendance: " + t.getMessage(), t);
                DialogUtils.showFailureDialog(requireContext(), "Network Error", t.getMessage(), null);
            }
        });
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'IST'", Locale.US);
        return sdf.format(new Date());
    }

    private void logWithTimestamp(String message) {
        Log.d(TAG, getTimestamp() + " - " + message);
    }
}