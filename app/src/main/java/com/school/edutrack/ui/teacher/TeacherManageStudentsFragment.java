package com.school.edutrack.ui.teacher;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.school.edutrack.R;
import com.school.edutrack.model.Student;
import com.school.edutrack.model.StudentResponse;
import com.school.edutrack.network.RetrofitClient;
import com.school.edutrack.utils.DialogUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherManageStudentsFragment extends Fragment implements StudentAdapter.OnStudentActionListener {

    private static final String TAG = "TeacherManageStudentsFragment";
    private String teacherId;
    private TextView teacherIdDisplay;
    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private List<Student> studentList;
    private List<Student> filteredStudentList;
    private FloatingActionButton addStudentFab;
    private SearchView searchView;
    private Spinner classFilterSpinner;
    private Spinner sectionFilterSpinner;
    private String selectedClass = "All Classes";
    private String selectedSection = "All Sections";
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teacher_manage_students, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve teacherId from arguments
        if (getArguments() != null) {
            teacherId = getArguments().getString("teacherId");
            Log.d(TAG, "Teacher ID retrieved on 2025-10-25 15:35 PM IST: " + teacherId);
        } else {
            Log.w(TAG, "Teacher ID not found in arguments on 2025-10-25 15:35 PM IST");
            requireActivity().runOnUiThread(() ->
                    DialogUtils.showFailureDialog(requireContext(), "Error", "Teacher ID not found", null));
            return;
        }

        // Initialize Views
        teacherIdDisplay = view.findViewById(R.id.teacher_id_display);
        recyclerView = view.findViewById(R.id.students_recycler_view);
        addStudentFab = view.findViewById(R.id.add_student_fab);
        searchView = view.findViewById(R.id.search_view);
        classFilterSpinner = view.findViewById(R.id.class_filter_spinner);
        sectionFilterSpinner = view.findViewById(R.id.section_filter_spinner);

        // Display teacherId
        teacherIdDisplay.setText("Teacher ID: " + teacherId);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        studentList = new ArrayList<>();
        filteredStudentList = new ArrayList<>();
        adapter = new StudentAdapter(this);
        recyclerView.setAdapter(adapter);

        // Setup Class Filter Spinner
        if (classFilterSpinner != null) {
            ArrayAdapter<CharSequence> classAdapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.class_filter_options,
                    android.R.layout.simple_spinner_item
            );
            classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            classFilterSpinner.setAdapter(classAdapter);
            classFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedClass = parent.getItemAtPosition(position).toString();
                    applyFilters();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedClass = "All Classes";
                    applyFilters();
                }
            });
        } else {
            requireActivity().runOnUiThread(() ->
                    DialogUtils.showFailureDialog(requireContext(), "Error", "Class filter spinner not found", null));
        }

        // Setup Section Filter Spinner
        if (sectionFilterSpinner != null) {
            ArrayAdapter<CharSequence> sectionAdapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.section_filter_options,
                    android.R.layout.simple_spinner_item
            );
            sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sectionFilterSpinner.setAdapter(sectionAdapter);
            sectionFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedSection = parent.getItemAtPosition(position).toString();
                    applyFilters();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedSection = "All Sections";
                    applyFilters();
                }
            });
        } else {
            requireActivity().runOnUiThread(() ->
                    DialogUtils.showFailureDialog(requireContext(), "Error", "Section filter spinner not found", null));
        }

        // Set up FAB
        addStudentFab.setOnClickListener(v -> showStudentDialog(null));

        // Set up SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query.trim();
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText.trim();
                applyFilters();
                return true;
            }
        });

        // Fetch students in background
        new Thread(this::fetchStudents).start();
    }

    private void fetchStudents() {
        requireActivity().runOnUiThread(() -> DialogUtils.showLoadingDialog(requireContext()));

        RetrofitClient.getApiService().getStudents().enqueue(new Callback<List<Student>>() {
            @Override
            public void onResponse(Call<List<Student>> call, Response<List<Student>> response) {
                requireActivity().runOnUiThread(() -> DialogUtils.hideLoadingDialog());
                if (response.isSuccessful() && response.body() != null) {
                    studentList.clear();
                    studentList.addAll(response.body());
                    requireActivity().runOnUiThread(() -> {
                        applyFilters();
                        recyclerView.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
                    });
                    Log.d(TAG, "Fetched " + studentList.size() + " students on 2025-10-25 15:35 PM IST: " + new Gson().toJson(studentList));
                } else {
                    String errorMsg = "Failed to fetch students: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ", " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading errorBody on 2025-10-25 15:35 PM IST", e);
                        }
                    }
                    String finalErrorMsg = errorMsg;
                    requireActivity().runOnUiThread(() ->
                            DialogUtils.showFailureDialog(requireContext(), "Error", finalErrorMsg, null));
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Student>> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    DialogUtils.hideLoadingDialog();
                    DialogUtils.showFailureDialog(requireContext(), "Error", "Fetch error: " + t.getMessage(), null);
                });
                Log.e(TAG, "Fetch error on 2025-10-25 15:35 PM IST", t);
            }
        });
    }

    private void applyFilters() {
        filteredStudentList.clear();

        for (Student student : studentList) {
            boolean matchesQuery = currentQuery.isEmpty() || (student.getName() != null && student.getName().toLowerCase().contains(currentQuery.toLowerCase()));
            boolean matchesClass = selectedClass.equals("All Classes") || (student.getClass_name() != null && student.getClass_name().equals(selectedClass.replace("Class ", "")));
            boolean matchesSection = selectedSection.equals("All Sections") || (student.getSection() != null && student.getSection().equals(selectedSection));

            if (matchesQuery && matchesClass && matchesSection) {
                filteredStudentList.add(student);
            }
        }

        if (filteredStudentList.isEmpty() && !currentQuery.isEmpty()) {
            requireActivity().runOnUiThread(() -> DialogUtils.showLoadingDialog(requireContext()));
            RetrofitClient.getApiService().getStudentById(currentQuery).enqueue(new Callback<Student>() {
                @Override
                public void onResponse(Call<Student> call, Response<Student> response) {
                    requireActivity().runOnUiThread(() -> DialogUtils.hideLoadingDialog());
                    if (response.isSuccessful() && response.body() != null) {
                        Student student = response.body();
                        boolean matchesClass = selectedClass.equals("All Classes") || (student.getClass_name() != null && student.getClass_name().equals(selectedClass.replace("Class ", "")));
                        boolean matchesSection = selectedSection.equals("All Sections") || (student.getSection() != null && student.getSection().equals(selectedSection));
                        if (matchesClass && matchesSection) {
                            filteredStudentList.add(student);
                            adapter.notifyDataSetChanged();
                            requireActivity().runOnUiThread(() ->
                                    DialogUtils.showSuccessDialog(requireContext(), "Success", "Student found by ID", null));
                        }
                    } else {
                        String errorMsg = "No student found for: " + currentQuery + ", HTTP " + response.code();
                        if (response.errorBody() != null) {
                            try {
                                errorMsg += ", " + response.errorBody().string();
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading errorBody on 2025-10-25 15:35 PM IST", e);
                            }
                        }
                        String finalErrorMsg = errorMsg;
                        requireActivity().runOnUiThread(() ->
                                DialogUtils.showFailureDialog(requireContext(), "Error", finalErrorMsg, null));
                        Log.e(TAG, errorMsg);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<Student> call, Throwable t) {
                    requireActivity().runOnUiThread(() -> {
                        DialogUtils.hideLoadingDialog();
                        adapter.notifyDataSetChanged();
                        DialogUtils.showFailureDialog(requireContext(), "Error", "Search error: " + t.getMessage(), null);
                    });
                    Log.e(TAG, "Search error on 2025-10-25 15:35 PM IST", t);
                }
            });
        } else {
            adapter.setStudentList(filteredStudentList);
            adapter.notifyDataSetChanged();
        }
    }

    private void showStudentDialog(Student student) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_student);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);

        TextInputEditText idInput = dialog.findViewById(R.id.student_id_input);
        TextInputEditText nameInput = dialog.findViewById(R.id.name_input);
        TextInputEditText emailInput = dialog.findViewById(R.id.email_input);
        Spinner classInput = dialog.findViewById(R.id.class_input);
        Spinner sectionInput = dialog.findViewById(R.id.section_input);
        TextInputEditText dobInput = dialog.findViewById(R.id.dob_input);
        RadioGroup genderInput = dialog.findViewById(R.id.gender_input);
        RadioButton genderMale = dialog.findViewById(R.id.gender_male);
        RadioButton genderFemale = dialog.findViewById(R.id.gender_female);
        RadioButton genderOther = dialog.findViewById(R.id.gender_other);
        TextInputEditText addressInput = dialog.findViewById(R.id.address_input);
        TextInputEditText phoneInput = dialog.findViewById(R.id.phone_input);
        TextInputEditText parentNameInput = dialog.findViewById(R.id.parent_name_input);
        TextInputEditText parentContactInput = dialog.findViewById(R.id.parent_contact_input);
        TextInputEditText admissionDateInput = dialog.findViewById(R.id.admission_date_input);
        TextInputEditText passwordInput = dialog.findViewById(R.id.password_input);
        Button saveButton = dialog.findViewById(R.id.save_button);
        Button cancelButton = dialog.findViewById(R.id.cancel_button);

        // Set up Class Dropdown
        ArrayAdapter<CharSequence> classAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.class_filter_options,
                android.R.layout.simple_spinner_item
        );
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classInput.setAdapter(classAdapter);

        // Set up Section Dropdown
        ArrayAdapter<CharSequence> sectionAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.section_filter_options,
                android.R.layout.simple_spinner_item
        );
        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionInput.setAdapter(sectionAdapter);

        // Student ID generation variables
        final String[] selectedClass = {""};
        final String[] selectedSection = {""};
        final String[] admissionYear = {""};

        // Auto-generate Student ID
        AdapterView.OnItemSelectedListener classListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedClass[0] = parent.getItemAtPosition(position).toString().replace("Class ", "");
                updateStudentId(idInput, selectedClass[0], selectedSection[0], admissionYear[0]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        AdapterView.OnItemSelectedListener sectionListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSection[0] = parent.getItemAtPosition(position).toString();
                updateStudentId(idInput, selectedClass[0], selectedSection[0], admissionYear[0]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        // Set up Date Picker for DOB
        dobInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(requireContext(),
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        dobInput.setText(date);
                    }, year, month, day).show();
        });

        // Set up Date Picker for Admission Date
        admissionDateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(requireContext(),
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        admissionDateInput.setText(date);
                        admissionYear[0] = String.valueOf(selectedYear).substring(2);
                        updateStudentId(idInput, selectedClass[0], selectedSection[0], admissionYear[0]);
                    }, year, month, day).show();
        });

        boolean isEdit = student != null;
        if (isEdit) {
            idInput.setText(student.getStudent_id());
            idInput.setEnabled(false);
            nameInput.setText(student.getName());
            emailInput.setText(student.getEmail());

            String className = student.getClass_name();
            if (className != null) {
                int classPosition = classAdapter.getPosition("Class " + className);
                if (classPosition != -1) classInput.setSelection(classPosition);
            }

            String section = student.getSection();
            if (section != null) {
                int sectionPosition = sectionAdapter.getPosition(section);
                if (sectionPosition != -1) sectionInput.setSelection(sectionPosition);
            }

            dobInput.setText(student.getDob());
            String gender = student.getGender();
            if (gender != null) {
                switch (gender.toLowerCase()) {
                    case "male": genderMale.setChecked(true); break;
                    case "female": genderFemale.setChecked(true); break;
                    case "other": genderOther.setChecked(true); break;
                }
            }

            addressInput.setText(student.getAddress());
            phoneInput.setText(student.getPhone());
            parentNameInput.setText(student.getParent_name());
            parentContactInput.setText(student.getParent_contact());
            admissionDateInput.setText(student.getAdmission_date());
            passwordInput.setVisibility(View.GONE);
            // Disable ID generation listeners during edit
            classInput.setOnItemSelectedListener(null);
            sectionInput.setOnItemSelectedListener(null);
        } else {
            passwordInput.setVisibility(View.VISIBLE);
            classInput.setOnItemSelectedListener(classListener);
            sectionInput.setOnItemSelectedListener(sectionListener);
        }

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String studentId = idInput.getText().toString().trim();
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String className = classInput.getSelectedItem().toString().replace("Class ", "");
            String section = sectionInput.getSelectedItem().toString();
            String dob = dobInput.getText().toString().trim();
            int selectedGenderId = genderInput.getCheckedRadioButtonId();
            String gender = "";
            if (selectedGenderId != -1) {
                RadioButton selectedGender = dialog.findViewById(selectedGenderId);
                gender = selectedGender.getText().toString();
            }
            String address = addressInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String parentName = parentNameInput.getText().toString().trim();
            String parentContact = parentContactInput.getText().toString().trim();
            String admissionDate = admissionDateInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (studentId.isEmpty() || name.isEmpty() || className.isEmpty() || section.isEmpty() || (!isEdit && password.isEmpty())) {
                DialogUtils.showFailureDialog(requireContext(), "Error", "Please fill all required fields", null);
                return;
            }

            Student newStudent = new Student();
            newStudent.setStudent_id(studentId);
            newStudent.setName(name);
            newStudent.setEmail(email);
            newStudent.setClass_name(className);
            newStudent.setSection(section);
            newStudent.setDob(dob);
            newStudent.setGender(gender);
            newStudent.setAddress(address);
            newStudent.setPhone(phone);
            newStudent.setParent_name(parentName);
            newStudent.setParent_contact(parentContact);
            newStudent.setAdmission_date(admissionDate);
            if (!isEdit) newStudent.setPassword(password);

            Log.d(TAG, "Sending student on 2025-10-25 15:35 PM IST: " + new Gson().toJson(newStudent));

            // Show confirmation dialog
            DialogUtils.showAlertDialog(requireContext(),
                    isEdit ? "Update Student" : "Add Student",
                    "Are you sure you want to " + (isEdit ? "update" : "add") + " student " + name + "?",
                    () -> {
                        requireActivity().runOnUiThread(() -> DialogUtils.showLoadingDialog(requireContext()));
                        Call<StudentResponse> call = isEdit ?
                                RetrofitClient.getApiService().updateStudent(newStudent) :
                                RetrofitClient.getApiService().addStudent(newStudent);

                        call.enqueue(new Callback<StudentResponse>() {
                            @Override
                            public void onResponse(Call<StudentResponse> call, Response<StudentResponse> response) {
                                requireActivity().runOnUiThread(() -> DialogUtils.hideLoadingDialog());
                                if (response.isSuccessful() && response.body() != null) {
                                    StudentResponse studentResponse = response.body();
                                    if ("success".equals(studentResponse.getStatus())) {
                                        requireActivity().runOnUiThread(() ->
                                                DialogUtils.showSuccessDialog(requireContext(),
                                                        "Success",
                                                        "Student " + (isEdit ? "updated" : "added") + " successfully!",
                                                        () -> {
                                                            dialog.dismiss();
                                                            new Thread(() -> fetchStudents()).start();
                                                        }));
                                        Log.d(TAG, "Operation response on 2025-10-25 15:35 PM IST: " + new Gson().toJson(studentResponse));
                                    } else {
                                        requireActivity().runOnUiThread(() ->
                                                DialogUtils.showFailureDialog(requireContext(), "Error", studentResponse.getMessage(), null));
                                    }
                                } else {
                                    String errorMsg = "Operation failed: HTTP " + response.code();
                                    try {
                                        if (response.errorBody() != null)
                                            errorMsg += ", " + response.errorBody().string();
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error reading errorBody on 2025-10-25 15:35 PM IST", e);
                                    }
                                    String finalErrorMsg = errorMsg;
                                    requireActivity().runOnUiThread(() ->
                                            DialogUtils.showFailureDialog(requireContext(), "Error", finalErrorMsg, null));
                                    Log.e(TAG, errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<StudentResponse> call, Throwable t) {
                                requireActivity().runOnUiThread(() -> {
                                    DialogUtils.hideLoadingDialog();
                                    DialogUtils.showFailureDialog(requireContext(), "Error", "Operation error: " + t.getMessage(), null);
                                });
                                Log.e(TAG, "Operation error on 2025-10-25 15:35 PM IST", t);
                            }
                        });
                    });
        });

        dialog.show();
    }

    @Override
    public void onUpdate(Student student) {
        showStudentDialog(student);
    }

    @Override
    public void onDelete(Student student) {
        DialogUtils.showAlertDialog(requireContext(),
                "Delete Student",
                "Are you sure you want to delete student " + student.getName() + "?",
                () -> {
                    requireActivity().runOnUiThread(() -> DialogUtils.showLoadingDialog(requireContext()));
                    RetrofitClient.getApiService().deleteStudent(student.getStudent_id()).enqueue(new Callback<StudentResponse>() {
                        @Override
                        public void onResponse(Call<StudentResponse> call, Response<StudentResponse> response) {
                            requireActivity().runOnUiThread(() -> DialogUtils.hideLoadingDialog());
                            if (response.isSuccessful() && response.body() != null) {
                                StudentResponse studentResponse = response.body();
                                if ("success".equals(studentResponse.getStatus())) {
                                    requireActivity().runOnUiThread(() ->
                                            DialogUtils.showSuccessDialog(requireContext(),
                                                    "Success",
                                                    "Student deleted successfully!",
                                                    () -> new Thread(() -> fetchStudents()).start()));
                                    Log.d(TAG, "Delete response on 2025-10-25 15:35 PM IST: " + new Gson().toJson(studentResponse));
                                } else {
                                    requireActivity().runOnUiThread(() ->
                                            DialogUtils.showFailureDialog(requireContext(), "Error", studentResponse.getMessage(), null));
                                }
                            } else {
                                String errorMsg = "Delete failed: HTTP " + response.code();
                                try {
                                    if (response.errorBody() != null)
                                        errorMsg += ", " + response.errorBody().string();
                                } catch (Exception e) {
                                    Log.e(TAG, "Error reading errorBody on 2025-10-25 15:35 PM IST", e);
                                }
                                String finalErrorMsg = errorMsg;
                                requireActivity().runOnUiThread(() ->
                                        DialogUtils.showFailureDialog(requireContext(), "Error", finalErrorMsg, null));
                                Log.e(TAG, errorMsg);
                            }
                        }

                        @Override
                        public void onFailure(Call<StudentResponse> call, Throwable t) {
                            requireActivity().runOnUiThread(() -> {
                                DialogUtils.hideLoadingDialog();
                                DialogUtils.showFailureDialog(requireContext(), "Error", "Delete error: " + t.getMessage(), null);
                            });
                            Log.e(TAG, "Delete error on 2025-10-25 15:35 PM IST", t);
                        }
                    });
                });
    }

    private void updateStudentId(TextInputEditText idInput, String className, String section, String admissionYear) {
        if (!className.isEmpty() && !section.isEmpty() && !admissionYear.isEmpty()) {
            // Generate sequence number (mocked; in production, fetch from backend or local counter)
            String sequence = String.format("%03d", getStudentCount(className, section) + 1);
            String studentId = String.format("%sSTU%s%s%s", admissionYear, className, section, sequence);
            idInput.setText(studentId);
        }
    }

    private int getStudentCount(String className, String section) {
        // Mock implementation: Count students in the same class and section
        int count = 0;
        for (Student student : studentList) {
            if (className.equals(student.getClass_name()) && section.equals(student.getSection())) {
                count++;
            }
        }
        return count;
    }
}