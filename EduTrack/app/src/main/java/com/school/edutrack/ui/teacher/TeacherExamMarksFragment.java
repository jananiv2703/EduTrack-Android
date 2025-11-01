package com.school.edutrack.ui.teacher;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputLayout;
import com.school.edutrack.R;
import com.school.edutrack.model.ClassTeacherResponse;
import com.school.edutrack.model.ExamSchedule;
import com.school.edutrack.model.ExamScore;
import com.school.edutrack.model.Student;
import com.school.edutrack.model.ApiResponse;
import com.school.edutrack.network.RetrofitClient;
import com.school.edutrack.utils.DialogUtils;
import retrofit2.Call;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TeacherExamMarksFragment extends Fragment {

    private static final String TAG = "TeacherExamMarks";
    private static final int MAX_RETRIES = 2;
    private static final int TIMEOUT_SECONDS = 10;

    private String teacherId;
    private ProgressBar loadingSpinner;
    private CardView announceExamCard;
    private CardView examScoresCard;
    private TextInputLayout examNameInputLayout, examDateInputLayout, startTimeInputLayout, endTimeInputLayout;
    private TextInputLayout classVenueInputLayout, seatingArrangementInputLayout, roomNumberInputLayout;
    private TextInputLayout marksObtainedInputLayout, maxMarksInputLayout;
    private Spinner examTypeDropdown, classDropdown, sectionDropdown;
    private Spinner examNameDropdown, studentDropdown, subjectDropdown;
    private Button announceExamButton, examScoresButton, submitExamButton, updateScoresButton;
    private EditText examDayInput;
    private String selectedClass, selectedSection;
    private List<ExamSchedule> examSchedulesList = new ArrayList<>();
    private List<ExamScore> examScoresList = new ArrayList<>();
    private List<Student> studentsList = new ArrayList<>();
    private int retryCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        logWithTimestamp("Creating view for TeacherExamMarksFragment");
        return inflater.inflate(R.layout.fragment_teacher_exam_marks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logWithTimestamp("View created, initializing components");

        // Retrieve teacherId from arguments
        if (getArguments() != null) {
            teacherId = getArguments().getString("teacherId");
            if (teacherId == null || teacherId.isEmpty()) {
                logWithTimestamp("Teacher ID is null or empty");
                DialogUtils.showFailureDialog(requireContext(), "Error", "Teacher ID is missing", null);
                return;
            }
            logWithTimestamp("Teacher ID retrieved: " + teacherId);
        } else {
            logWithTimestamp("Arguments bundle is null");
            DialogUtils.showFailureDialog(requireContext(), "Error", "Teacher ID not found", null);
            return;
        }

        // Initialize Views
        TextView teacherIdDisplay = view.findViewById(R.id.teacher_id_display);
        announceExamButton = view.findViewById(R.id.announce_exam_button);
        examScoresButton = view.findViewById(R.id.exam_scores_button);
        loadingSpinner = view.findViewById(R.id.loading_spinner);
        announceExamCard = view.findViewById(R.id.announce_exam_card);
        examScoresCard = view.findViewById(R.id.exam_scores_card);

        // Announce Exam Card Inputs
        examNameInputLayout = view.findViewById(R.id.exam_name_input_layout);
        examDateInputLayout = view.findViewById(R.id.exam_date_input_layout);
        startTimeInputLayout = view.findViewById(R.id.start_time_input_layout);
        endTimeInputLayout = view.findViewById(R.id.end_time_input_layout);
        classVenueInputLayout = view.findViewById(R.id.class_venue_input_layout);
        seatingArrangementInputLayout = view.findViewById(R.id.seating_arrangement_input_layout);
        roomNumberInputLayout = view.findViewById(R.id.room_number_input_layout);
        examDayInput = view.findViewById(R.id.exam_day_input);
        examTypeDropdown = view.findViewById(R.id.exam_type_dropdown);
        classDropdown = view.findViewById(R.id.class_dropdown);
        sectionDropdown = view.findViewById(R.id.section_dropdown);
        submitExamButton = view.findViewById(R.id.submit_exam_button);

        // Exam Scores Card Inputs
        marksObtainedInputLayout = view.findViewById(R.id.marks_obtained_input_layout);
        maxMarksInputLayout = view.findViewById(R.id.max_marks_input_layout);
        examNameDropdown = view.findViewById(R.id.exam_name_dropdown);
        studentDropdown = view.findViewById(R.id.student_dropdown);
        subjectDropdown = view.findViewById(R.id.subject_dropdown);
        updateScoresButton = view.findViewById(R.id.update_scores_button);

        // Display teacherId
        teacherIdDisplay.setText("Teacher ID: " + teacherId);

        // Setup initial UI
        if (examTypeDropdown == null || classDropdown == null || sectionDropdown == null ||
                examNameDropdown == null || studentDropdown == null || subjectDropdown == null) {
            logWithTimestamp("One or more Spinners are null");
            DialogUtils.showFailureDialog(requireContext(), "Error", "UI initialization failed. Please restart the app.", null);
            return;
        }

        setupExamTypeDropdown();
        setupClassAndSectionDropdowns();
        setupSubjectDropdown();
        setupDateTimePickers();

        // Button click listeners
        announceExamButton.setOnClickListener(v -> toggleCardVisibility(announceExamCard, examScoresCard));
        examScoresButton.setOnClickListener(v -> {
            toggleCardVisibility(examScoresCard, announceExamCard);
            fetchExamNames();
        });
        submitExamButton.setOnClickListener(v -> announceExam());
        updateScoresButton.setOnClickListener(v -> updateExamScores());
    }

    private void setupExamTypeDropdown() {
        List<String> examTypes = Arrays.asList("Select Exam Type", "Half-yearly", "Quarterly", "Annual", "Term 1", "Term 2", "Term 3", "Model 1", "Model 2", "Model 3");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, examTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        examTypeDropdown.setAdapter(adapter);
    }

    private void setupClassAndSectionDropdowns() {
        new FetchClassTeacherDataTask().execute();
    }

    private void setupSubjectDropdown() {
        List<String> subjects = Arrays.asList("Select Subject", "Mathematics", "Science", "English", "Social Studies", "Language");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectDropdown.setAdapter(adapter);
    }

    private void setupDateTimePickers() {
        EditText examDateInput = examDateInputLayout.getEditText();
        EditText startTimeInput = startTimeInputLayout.getEditText();
        EditText endTimeInput = endTimeInputLayout.getEditText();

        examDateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                        examDateInput.setText(date);
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                        if (examDayInput != null) examDayInput.setText(dayFormat.format(calendar.getTime()));
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .show();
        });

        startTimeInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(requireContext(),
                    (view, hourOfDay, minute) -> {
                        String amPm = hourOfDay >= 12 ? "PM" : "AM";
                        int hour = hourOfDay % 12;
                        if (hour == 0) hour = 12;
                        startTimeInput.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm));
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
                    .show();
        });

        endTimeInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(requireContext(),
                    (view, hourOfDay, minute) -> {
                        String amPm = hourOfDay >= 12 ? "PM" : "AM";
                        int hour = hourOfDay % 12;
                        if (hour == 0) hour = 12;
                        endTimeInput.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm));
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
                    .show();
        });
    }

    private void toggleCardVisibility(CardView showCard, CardView hideCard) {
        if (showCard.getVisibility() == View.VISIBLE) {
            showCard.setVisibility(View.GONE);
        } else {
            showCard.setVisibility(View.VISIBLE);
            hideCard.setVisibility(View.GONE);
        }
    }

    private void fetchExamNames() {
        new FetchExamNamesTask().execute();
    }

    private void fetchScoresForExam(String examName) {
        new FetchScoresTask().execute(examName);
    }

    private void announceExam() {
        String examName = examNameInputLayout.getEditText().getText().toString().trim();
        String examType = examTypeDropdown.getSelectedItem().toString();
        String examDate = examDateInputLayout.getEditText().getText().toString();
        String examDay = examDayInput.getText().toString().trim();
        String startTime = startTimeInputLayout.getEditText().getText().toString().trim();
        String endTime = endTimeInputLayout.getEditText().getText().toString().trim();
        String classVenue = classVenueInputLayout.getEditText().getText().toString().trim();
        String seatingArrangement = seatingArrangementInputLayout.getEditText().getText().toString().trim();
        String roomNumber = roomNumberInputLayout.getEditText().getText().toString().trim();

        if (examName.isEmpty() || examType.equals("Select Exam Type") || selectedClass == null || selectedSection == null ||
                examDate.isEmpty() || examDay.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            DialogUtils.showFailureDialog(requireContext(), "Error", "Please fill all required fields", null);
            return;
        }

        if (!startTime.matches("\\d{2}:\\d{2} (AM|PM)") || !endTime.matches("\\d{2}:\\d{2} (AM|PM)")) {
            logWithTimestamp("Invalid time format. Start Time: " + startTime + ", End Time: " + endTime);
            DialogUtils.showFailureDialog(requireContext(), "Error", "Invalid time format. Use HH:MM AM/PM", null);
            return;
        }

        ExamSchedule examSchedule = new ExamSchedule();
        examSchedule.setExamName(examName);
        examSchedule.setExamType(examType);
        examSchedule.setClassName(selectedClass);
        examSchedule.setSection(selectedSection);
        examSchedule.setExamDate(examDate);
        examSchedule.setExamDay(examDay);
        examSchedule.setStartTime(startTime);
        examSchedule.setEndTime(endTime);
        examSchedule.setClassVenue(classVenue.isEmpty() ? null : classVenue);
        examSchedule.setSeatingArrangement(seatingArrangement.isEmpty() ? null : seatingArrangement);
        examSchedule.setRoomNumber(roomNumber.isEmpty() ? null : roomNumber);
        examSchedule.setTeacherId(teacherId);

        new AnnounceExamTask().execute(examSchedule);
    }

    private void updateExamScores() {
        int examPosition = examNameDropdown.getSelectedItemPosition();
        int studentPosition = studentDropdown.getSelectedItemPosition();
        String subject = subjectDropdown.getSelectedItem().toString();
        String marksObtained = marksObtainedInputLayout.getEditText().getText().toString().trim();
        String maxMarks = maxMarksInputLayout.getEditText().getText().toString().trim();

        if (examPosition <= 0 || studentPosition <= 0 || subject.equalsIgnoreCase("Select Subject") ||
                marksObtained.isEmpty() || maxMarks.isEmpty()) {
            DialogUtils.showFailureDialog(requireContext(), "Error", "Please fill all required fields", null);
            return;
        }

        try {
            double score = Double.parseDouble(marksObtained);
            double max = Double.parseDouble(maxMarks);
            if (score < 0 || max <= 0 || score > max) {
                logWithTimestamp("Invalid marks. Obtained: " + marksObtained + ", Max: " + maxMarks);
                DialogUtils.showFailureDialog(requireContext(), "Error", "Invalid marks. Ensure 0 ≤ Obtained Marks ≤ Max Marks, and Max Marks > 0", null);
                return;
            }
        } catch (NumberFormatException e) {
            logWithTimestamp("Invalid number format for marks: " + e.getMessage());
            DialogUtils.showFailureDialog(requireContext(), "Error", "Marks must be valid numbers", null);
            return;
        }

        if (studentsList.isEmpty() || studentPosition > studentsList.size()) {
            logWithTimestamp("Invalid student selection. Students List Size: " + studentsList.size() + ", Position: " + studentPosition);
            DialogUtils.showFailureDialog(requireContext(), "Error", "Error: Invalid student selection", null);
            return;
        }

        String studentId = studentsList.get(studentPosition - 1).getStudent_id();
        Object selectedExamName = examNameDropdown.getSelectedItem();
        if (selectedExamName == null) {
            logWithTimestamp("Selected exam name is null");
            DialogUtils.showFailureDialog(requireContext(), "Error", "Error: Please select an exam", null);
            return;
        }
        String examName = selectedExamName.toString();

        ExamScore existingScore = null;
        for (ExamScore score : examScoresList) {
            if (score.getStudentId() != null && score.getExamName() != null &&
                    score.getStudentId().equals(studentId) && score.getExamName().equals(examName)) {
                existingScore = score;
                break;
            }
        }

        if (existingScore == null) {
            logWithTimestamp("Score record not found. Student ID: " + studentId + ", Exam: " + examName);
            DialogUtils.showFailureDialog(requireContext(), "Error", "Score record not found for this student and exam", null);
            return;
        }

        existingScore.setSubject(subject);
        existingScore.setMarksObtained(marksObtained);
        existingScore.setMaxMarks(maxMarks);
        existingScore.setTeacherId(teacherId);

        new UpdateScoresTask().execute(existingScore);
    }

    private void clearAnnounceExamForm() {
        examNameInputLayout.getEditText().setText("");
        examTypeDropdown.setSelection(0);
        examDateInputLayout.getEditText().setText("");
        if (examDayInput != null) examDayInput.setText("");
        startTimeInputLayout.getEditText().setText("");
        endTimeInputLayout.getEditText().setText("");
        classVenueInputLayout.getEditText().setText("");
        seatingArrangementInputLayout.getEditText().setText("");
        roomNumberInputLayout.getEditText().setText("");
    }

    private void clearExamScoresForm() {
        examNameDropdown.setSelection(0);
        studentDropdown.setSelection(0);
        subjectDropdown.setSelection(0);
        marksObtainedInputLayout.getEditText().setText("");
        maxMarksInputLayout.getEditText().setText("");
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'IST'", Locale.US);
        return sdf.format(new Date());
    }

    private void logWithTimestamp(String message) {
        Log.d(TAG, getTimestamp() + " - " + message);
    }

    // AsyncTask for fetching class and section data
    private class FetchClassTeacherDataTask extends AsyncTask<Void, Void, ClassTeacherResponse> {
        @Override
        protected void onPreExecute() {
            DialogUtils.showLoadingDialog(requireContext());
        }

        @Override
        protected ClassTeacherResponse doInBackground(Void... params) {
            try {
                Call<ClassTeacherResponse> call = RetrofitClient.getApiService().getClassTeacherData(teacherId);
                Response<ClassTeacherResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body();
                }
            } catch (Exception e) {
                logWithTimestamp("Error in doInBackground: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ClassTeacherResponse result) {
            DialogUtils.hideLoadingDialog();
            if (result != null) {
                selectedClass = result.getClassName();
                selectedSection = result.getSection();
                studentsList = result.getStudents() != null ? result.getStudents() : new ArrayList<>();

                if (selectedClass == null || selectedClass.isEmpty() || selectedSection == null || selectedSection.isEmpty()) {
                    logWithTimestamp("Class or Section is null/empty. Class: " + selectedClass + ", Section: " + selectedSection);
                    DialogUtils.showFailureDialog(requireContext(), "Error", "You are not assigned as a class teacher.", null);
                    disableInputs();
                    return;
                }

                logWithTimestamp("Class/Section data fetched: Class=" + selectedClass + ", Section=" + selectedSection + ", Students=" + studentsList.size());
                List<String> classes = new ArrayList<>();
                classes.add("Class: " + selectedClass);
                ArrayAdapter<String> classAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, classes);
                classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                classDropdown.setAdapter(classAdapter);

                List<String> sections = new ArrayList<>();
                sections.add("Section: " + selectedSection);
                ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sections);
                sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sectionDropdown.setAdapter(sectionAdapter);

                setupStudentDropdown();
            } else if (retryCount < MAX_RETRIES) {
                retryCount++;
                logWithTimestamp("Retry attempt " + retryCount + " for fetching class/section data");
                DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to fetch data, retrying... (" + retryCount + "/" + MAX_RETRIES + ")", null);
                new FetchClassTeacherDataTask().execute();
            } else {
                retryCount = 0;
                logWithTimestamp("Failed to fetch class/section data after retries");
                DialogUtils.showFailureDialog(requireContext(), "Error", "Unable to fetch class/section data. Please check your network.", null);
                disableInputs();
            }
        }
    }

    private void setupStudentDropdown() {
        List<String> studentNames = new ArrayList<>();
        studentNames.add("Select Student");
        if (studentsList.isEmpty()) {
            logWithTimestamp("Students list is empty");
            DialogUtils.showFailureDialog(requireContext(), "Error", "No students found for this class/section", null);
        } else {
            for (Student student : studentsList) {
                if (student.getName() != null && student.getStudent_id() != null) {
                    studentNames.add(student.getName() + " (" + student.getStudent_id() + ")");
                }
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, studentNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studentDropdown.setAdapter(adapter);
    }

    private void disableInputs() {
        announceExamButton.setEnabled(false);
        examScoresButton.setEnabled(false);
        classDropdown.setEnabled(false);
        sectionDropdown.setEnabled(false);
        submitExamButton.setEnabled(false);
        updateScoresButton.setEnabled(false);
        studentDropdown.setEnabled(false);
        examNameDropdown.setEnabled(false);
        subjectDropdown.setEnabled(false);
    }

    // AsyncTask for fetching exam names
    private class FetchExamNamesTask extends AsyncTask<Void, Void, List<ExamSchedule>> {
        @Override
        protected void onPreExecute() {
            DialogUtils.showLoadingDialog(requireContext());
        }

        @Override
        protected List<ExamSchedule> doInBackground(Void... params) {
            try {
                Call<List<ExamSchedule>> call = RetrofitClient.getApiService().getExamSchedules();
                Response<List<ExamSchedule>> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body();
                }
            } catch (Exception e) {
                logWithTimestamp("Error fetching exam names: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ExamSchedule> result) {
            DialogUtils.hideLoadingDialog();
            if (result != null) {
                examSchedulesList.clear();
                examSchedulesList.addAll(result);
                List<String> examNames = new ArrayList<>();
                examNames.add("Select Exam");
                if (examSchedulesList.isEmpty()) {
                    logWithTimestamp("No exam schedules found");
                    DialogUtils.showFailureDialog(requireContext(), "Error", "No exams found. Please announce an exam first.", null);
                } else {
                    for (ExamSchedule schedule : examSchedulesList) {
                        if (schedule.getExamName() != null) {
                            examNames.add(schedule.getExamName());
                        }
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, examNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                examNameDropdown.setAdapter(adapter);

                examNameDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position > 0) {
                            ExamSchedule selectedExam = examSchedulesList.get(position - 1);
                            fetchScoresForExam(selectedExam.getExamName());
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            } else {
                logWithTimestamp("Failed to fetch exam schedules");
                DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to fetch exams. Please try again.", null);
            }
        }
    }

    // AsyncTask for fetching scores
    private class FetchScoresTask extends AsyncTask<String, Void, List<ExamScore>> {
        @Override
        protected void onPreExecute() {
            DialogUtils.showLoadingDialog(requireContext());
        }

        @Override
        protected List<ExamScore> doInBackground(String... params) {
            if (params.length == 0 || selectedClass == null || selectedClass.isEmpty() || params[0] == null || params[0].isEmpty()) {
                return null;
            }
            try {
                Call<List<ExamScore>> call = RetrofitClient.getApiService().fetchScores(null, selectedClass, params[0]);
                Response<List<ExamScore>> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body();
                }
            } catch (Exception e) {
                logWithTimestamp("Error fetching scores: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ExamScore> result) {
            DialogUtils.hideLoadingDialog();
            if (result != null) {
                examScoresList.clear();
                examScoresList.addAll(result);
                if (examScoresList.isEmpty()) {
                    logWithTimestamp("No scores found");
                    DialogUtils.showFailureDialog(requireContext(), "Error", "No scores found for this exam.", null);
                }
            } else {
                logWithTimestamp("Failed to fetch scores");
                DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to fetch scores. Please try again.", null);
            }
        }
    }

    // AsyncTask for announcing exam
    private class AnnounceExamTask extends AsyncTask<ExamSchedule, Void, ApiResponse> {
        @Override
        protected void onPreExecute() {
            DialogUtils.showLoadingDialog(requireContext());
        }

        @Override
        protected ApiResponse doInBackground(ExamSchedule... params) {
            if (params.length == 0) return null;
            try {
                Call<ApiResponse> call = RetrofitClient.getApiService().addExamSchedule(params[0]);
                Response<ApiResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body();
                }
            } catch (Exception e) {
                logWithTimestamp("Error announcing exam: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ApiResponse result) {
            DialogUtils.hideLoadingDialog();
            if (result != null) {
                logWithTimestamp("Exam announced successfully: " + result.getMessage());
                DialogUtils.showSuccessDialog(requireContext(), "Success", result.getMessage(), () -> {
                    announceExamCard.setVisibility(View.GONE);
                    clearAnnounceExamForm();
                });
            } else {
                logWithTimestamp("Failed to announce exam");
                DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to announce exam. Please try again.", null);
            }
        }
    }

    // AsyncTask for updating scores
    private class UpdateScoresTask extends AsyncTask<ExamScore, Void, ApiResponse> {
        @Override
        protected void onPreExecute() {
            DialogUtils.showLoadingDialog(requireContext());
        }

        @Override
        protected ApiResponse doInBackground(ExamScore... params) {
            if (params.length == 0) return null;
            try {
                Call<ApiResponse> call = RetrofitClient.getApiService().updateMarks(params[0]);
                Response<ApiResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body();
                }
            } catch (Exception e) {
                logWithTimestamp("Error updating scores: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ApiResponse result) {
            DialogUtils.hideLoadingDialog();
            if (result != null) {
                logWithTimestamp("Exam scores updated successfully: " + result.getMessage());
                DialogUtils.showSuccessDialog(requireContext(), "Success", result.getMessage(), () -> {
                    examScoresCard.setVisibility(View.GONE);
                    clearExamScoresForm();
                });
            } else {
                logWithTimestamp("Failed to update scores");
                DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to update scores. Please try again.", null);
            }
        }
    }
}