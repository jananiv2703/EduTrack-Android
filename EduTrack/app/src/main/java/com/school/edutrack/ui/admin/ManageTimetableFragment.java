package com.school.edutrack.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.school.edutrack.R;
import com.school.edutrack.model.ApiResponse;
import com.school.edutrack.model.Teacher;
import com.school.edutrack.model.TeachersResponse;
import com.school.edutrack.model.Timetable;
import com.school.edutrack.network.ApiService;
import com.school.edutrack.network.RetrofitClient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageTimetableFragment extends Fragment {

    private static final String TAG = "ManageTimetableFragment";

    private AutoCompleteTextView teacherDropdown;
    private TextInputLayout teacherInputLayout;
    private Spinner daySpinner, periodSpinner, subjectSpinner, classSpinner, sectionSpinner;
    private MaterialButton addTimetableButton;
    private ProgressBar loadingSpinner;
    private ApiService apiService;
    private List<Teacher> teachers;
    private Map<String, String> teacherIdToNameMap;
    private Map<String, String> displayTextToTeacherIdMap;
    private Map<String, String> displayTextToTeacherNameMap;
    private ArrayAdapter<String> teacherAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        logWithTimestamp("Creating view for ManageTimetableFragment");
        return inflater.inflate(R.layout.fragment_manage_timetable, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logWithTimestamp("View created, initializing components");

        // Initialize UI components
        teacherInputLayout = view.findViewById(R.id.teacher_input_layout);
        teacherDropdown = view.findViewById(R.id.teacher_dropdown);
        daySpinner = view.findViewById(R.id.day_spinner);
        periodSpinner = view.findViewById(R.id.period_spinner);
        subjectSpinner = view.findViewById(R.id.subject_spinner);
        classSpinner = view.findViewById(R.id.class_spinner);
        sectionSpinner = view.findViewById(R.id.section_spinner);
        addTimetableButton = view.findViewById(R.id.add_timetable_button);
        loadingSpinner = view.findViewById(R.id.loading_spinner);

        // Initialize Retrofit API service
        apiService = RetrofitClient.getApiService();
        logWithTimestamp("Retrofit API service initialized");

        // Setup Spinners
        setupSpinners();

        // Initialize teacher maps
        teacherIdToNameMap = new HashMap<>();
        displayTextToTeacherIdMap = new HashMap<>();
        displayTextToTeacherNameMap = new HashMap<>();

        // Set button listener
        addTimetableButton.setOnClickListener(v -> addTimetable());

        // Set click listener for the end icon to show the dropdown with constrained size
        teacherInputLayout.setEndIconOnClickListener(v -> {
            if (teacherAdapter != null && teacherAdapter.getCount() > 0) {
                teacherDropdown.post(() -> {
                    try {
                        // Constrain dropdown width to avoid OpenGL limits
                        int maxWidth = getResources().getDisplayMetrics().widthPixels - 50; // Leave some padding
                        teacherDropdown.setDropDownWidth(maxWidth);
                        teacherDropdown.showDropDown();
                        logWithTimestamp("Dropdown shown with max width: " + maxWidth);
                    } catch (Exception e) {
                        Log.e(TAG, getTimestamp() + " - Error showing dropdown: " + e.getMessage(), e);
                        Toast.makeText(getContext(), "Failed to show dropdown", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "No teachers available", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch teachers for dropdown
        fetchTeachers();
    }

    private void setupSpinners() {
        // Days of the week
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);

        // Periods (e.g., 1 to 5)
        String[] periods = {"1", "2", "3", "4", "5"};
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, periods);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(periodAdapter);

        // Subjects from strings.xml
        ArrayAdapter<CharSequence> subjectAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.subject_options, android.R.layout.simple_spinner_item);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        // Classes from strings.xml
        ArrayAdapter<CharSequence> classAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.class_filter_options, android.R.layout.simple_spinner_item);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(classAdapter);

        // Sections from strings.xml
        ArrayAdapter<CharSequence> sectionAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.section_filter_options, android.R.layout.simple_spinner_item);
        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionSpinner.setAdapter(sectionAdapter);
    }

    private void fetchTeachers() {
        logWithTimestamp("Fetching teachers list");

        // Show loading spinner while fetching teachers
        teacherInputLayout.setEnabled(false);
        loadingSpinner.setVisibility(View.VISIBLE);

        Call<TeachersResponse> call = apiService.fetchTeachers();
        call.enqueue(new Callback<TeachersResponse>() {
            @Override
            public void onResponse(Call<TeachersResponse> call, Response<TeachersResponse> response) {
                loadingSpinner.setVisibility(View.GONE);
                teacherInputLayout.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    teachers = response.body().getTeachers();
                    logWithTimestamp("Teachers fetched successfully: " + teachers.size());
                    if (teachers.isEmpty()) {
                        Toast.makeText(getContext(), "No teachers found", Toast.LENGTH_SHORT).show();
                        teacherInputLayout.setError("No teachers available");
                        return;
                    }
                    teacherInputLayout.setError(null);

                    // Populate dropdown
                    List<String> teacherDisplayList = new ArrayList<>();
                    for (Teacher teacher : teachers) {
                        String teacherId = teacher.getTeacher_id();
                        String teacherName = teacher.getName();
                        if (teacherId == null || teacherName == null) {
                            logWithTimestamp("Skipping teacher with null ID or name: " + teacher.toString());
                            continue;
                        }
                        String displayText = teacherName + " (" + teacherId + ")";
                        teacherDisplayList.add(displayText);
                        teacherIdToNameMap.put(teacherId, displayText);
                        displayTextToTeacherIdMap.put(displayText, teacherId);
                        displayTextToTeacherNameMap.put(displayText, teacherName);
                    }
                    if (teacherDisplayList.isEmpty()) {
                        Toast.makeText(getContext(), "No valid teachers found", Toast.LENGTH_SHORT).show();
                        teacherInputLayout.setError("No valid teachers available");
                        return;
                    }
                    teacherAdapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_dropdown_item_1line, teacherDisplayList);
                    teacherDropdown.setAdapter(teacherAdapter);
                    teacherDropdown.setThreshold(1); // Start suggesting after 1 character

                    // Ensure dropdown is constrained
                    teacherDropdown.setOnItemClickListener((parent, view, position, id) -> {
                        int maxWidth = getResources().getDisplayMetrics().widthPixels - 50;
                        teacherDropdown.setDropDownWidth(maxWidth);
                        logWithTimestamp("Dropdown item selected, constrained width: " + maxWidth);
                    });
                } else {
                    Log.e(TAG, getTimestamp() + " - Failed to fetch teachers. Response code: " + response.code());
                    Toast.makeText(getContext(), "Failed to fetch teachers", Toast.LENGTH_SHORT).show();
                    teacherInputLayout.setError("Failed to load teachers");
                }
            }

            @Override
            public void onFailure(Call<TeachersResponse> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                teacherInputLayout.setEnabled(true);
                Log.e(TAG, getTimestamp() + " - Network error fetching teachers: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                teacherInputLayout.setError("Network error");
            }
        });
    }

    private void addTimetable() {
        String teacherDisplay = teacherDropdown.getText().toString().trim();
        String dayOfWeek = daySpinner.getSelectedItem().toString();
        String periodNo = periodSpinner.getSelectedItem().toString();
        String subject = subjectSpinner.getSelectedItem().toString();
        String classSelection = classSpinner.getSelectedItem().toString();
        String sectionSelection = sectionSpinner.getSelectedItem().toString();

        // Validation
        if (teacherDisplay.isEmpty() || subject.equals("Select Subject") ||
                classSelection.equals("All Classes") || sectionSelection.equals("All Sections")) {
            Toast.makeText(getContext(), "Please fill all fields and select valid options", Toast.LENGTH_SHORT).show();
            if (teacherDisplay.isEmpty()) {
                teacherInputLayout.setError("Teacher is required");
            } else {
                teacherInputLayout.setError(null);
            }
            return;
        }

        // Extract teacher_id and teacher_name from the display text
        String teacherId = displayTextToTeacherIdMap.get(teacherDisplay);
        String teacherName = displayTextToTeacherNameMap.get(teacherDisplay);
        logWithTimestamp("Selected teacher display: " + teacherDisplay + ", teacherId: " + teacherId + ", teacherName: " + teacherName);
        if (teacherId == null || teacherName == null) {
            Toast.makeText(getContext(), "Please select a valid teacher from the dropdown", Toast.LENGTH_SHORT).show();
            teacherInputLayout.setError("Invalid teacher selection");
            return;
        }
        teacherInputLayout.setError(null);

        // Extract class number (e.g., "Class 10" -> "10")
        String className = classSelection.replace("Class ", "");
        // Use section as is (e.g., "A")
        String section = sectionSelection;

        // Create Timetable object with teacherName
        Timetable timetable = new Timetable(0, teacherId, teacherName, dayOfWeek, periodNo, subject, className, section);
        logWithTimestamp("Adding timetable: " + timetable.toString());

        loadingSpinner.setVisibility(View.VISIBLE);

        Call<ApiResponse> call = apiService.addTimetable(timetable);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                loadingSpinner.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    logWithTimestamp("Timetable added successfully: " + response.body().getMessage());
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    // Clear inputs
                    teacherDropdown.setText("");
                    subjectSpinner.setSelection(0);
                    classSpinner.setSelection(0);
                    sectionSpinner.setSelection(0);
                    daySpinner.setSelection(0);
                    periodSpinner.setSelection(0);
                } else {
                    String errorBody = "No error body";
                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, getTimestamp() + " - Error reading error body: " + e.getMessage(), e);
                        }
                    }
                    Log.e(TAG, getTimestamp() + " - Failed to add timetable. Response code: " + response.code() + ", Error: " + errorBody);
                    Toast.makeText(getContext(), "Failed to add timetable: " + errorBody, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                Log.e(TAG, getTimestamp() + " - Network error adding timetable: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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