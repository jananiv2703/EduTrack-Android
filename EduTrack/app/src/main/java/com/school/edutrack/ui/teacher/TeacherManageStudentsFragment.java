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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
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
    private ProgressBar loadingSpinner;
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
            Log.d(TAG, "Teacher ID retrieved on 2025-06-03 19:53 PM IST: " + teacherId);
        } else {
            Log.w(TAG, "Teacher ID not found in arguments on 2025-06-03 19:53 PM IST");
            Toast.makeText(getContext(), "Error: Teacher ID not found", Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize Views
        teacherIdDisplay = view.findViewById(R.id.teacher_id_display);
        recyclerView = view.findViewById(R.id.students_recycler_view);
        loadingSpinner = view.findViewById(R.id.loading_spinner);
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
            Toast.makeText(requireContext(), "Class filter spinner not found", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), "Section filter spinner not found", Toast.LENGTH_SHORT).show();
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
        requireActivity().runOnUiThread(() -> {
            loadingSpinner.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        });

        RetrofitClient.getApiService().getStudents().enqueue(new Callback<List<Student>>() {
            @Override
            public void onResponse(Call<List<Student>> call, Response<List<Student>> response) {
                requireActivity().runOnUiThread(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                });
                if (response.isSuccessful() && response.body() != null) {
                    studentList.clear();
                    studentList.addAll(response.body());
                    requireActivity().runOnUiThread(() -> {
                        applyFilters();
                        recyclerView.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
                    });
                    Log.d(TAG, "Fetched " + studentList.size() + " students on 2025-06-03 19:53 PM IST: " + new Gson().toJson(studentList));
                } else {
                    String errorMsg = "Failed to fetch students: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ", " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading errorBody on 2025-06-03 19:53 PM IST", e);
                        }
                    }
                    showToast(errorMsg);
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Student>> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                });
                showToast("Fetch error: " + t.getMessage());
                Log.e(TAG, "Fetch error on 2025-06-03 19:53 PM IST", t);
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
            requireActivity().runOnUiThread(() -> loadingSpinner.setVisibility(View.VISIBLE));
            RetrofitClient.getApiService().getStudentById(currentQuery).enqueue(new Callback<Student>() {
                @Override
                public void onResponse(Call<Student> call, Response<Student> response) {
                    requireActivity().runOnUiThread(() -> loadingSpinner.setVisibility(View.GONE));
                    if (response.isSuccessful() && response.body() != null) {
                        Student student = response.body();
                        boolean matchesClass = selectedClass.equals("All Classes") || (student.getClass_name() != null && student.getClass_name().equals(selectedClass.replace("Class ", "")));
                        boolean matchesSection = selectedSection.equals("All Sections") || (student.getSection() != null && student.getSection().equals(selectedSection));
                        if (matchesClass && matchesSection) {
                            filteredStudentList.add(student);
                            requireActivity().runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                                showToast("Student found by ID");
                            });
                        }
                    } else {
                        String errorMsg = "No student found for: " + currentQuery + ", HTTP " + response.code();
                        if (response.errorBody() != null) {
                            try {
                                errorMsg += ", " + response.errorBody().string();
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading errorBody on 2025-06-03 19:53 PM IST", e);
                            }
                        }
                        showToast(errorMsg);
                        Log.e(TAG, errorMsg);
                    }
                    requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                }

                @Override
                public void onFailure(Call<Student> call, Throwable t) {
                    requireActivity().runOnUiThread(() -> {
                        loadingSpinner.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    });
                    showToast("Search error: " + t.getMessage());
                    Log.e(TAG, "Search error on 2025-06-03 19:53 PM IST", t);
                }
            });
        } else {
            requireActivity().runOnUiThread(() -> {
                adapter.setStudentList(filteredStudentList);
                adapter.notifyDataSetChanged();
            });
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

        // Set up Date Picker for DOB
        dobInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        dobInput.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Set up Date Picker for Admission Date
        admissionDateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        admissionDateInput.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        boolean isEdit = student != null;
        if (isEdit) {
            idInput.setText(student.getStudent_id());
            idInput.setEnabled(false);
            nameInput.setText(student.getName());
            emailInput.setText(student.getEmail());

            // Set Class Spinner
            String className = student.getClass_name();
            if (className != null) {
                int classPosition = classAdapter.getPosition("Class " + className);
                if (classPosition != -1) {
                    classInput.setSelection(classPosition);
                }
            }

            // Set Section Spinner
            String section = student.getSection();
            if (section != null) {
                int sectionPosition = sectionAdapter.getPosition(section);
                if (sectionPosition != -1) {
                    sectionInput.setSelection(sectionPosition);
                }
            }

            dobInput.setText(student.getDob());

            // Set Gender Radio Buttons
            String gender = student.getGender();
            if (gender != null) {
                switch (gender.toLowerCase()) {
                    case "male":
                        genderMale.setChecked(true);
                        break;
                    case "female":
                        genderFemale.setChecked(true);
                        break;
                    case "other":
                        genderOther.setChecked(true);
                        break;
                }
            }

            addressInput.setText(student.getAddress());
            phoneInput.setText(student.getPhone());
            parentNameInput.setText(student.getParent_name());
            parentContactInput.setText(student.getParent_contact());
            admissionDateInput.setText(student.getAdmission_date());
            passwordInput.setVisibility(View.GONE);
        } else {
            passwordInput.setVisibility(View.VISIBLE);
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
                showToast("Please fill all required fields");
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
            if (!isEdit) {
                newStudent.setPassword(password);
            }

            Log.d(TAG, "Sending student on 2025-06-03 19:53 PM IST: " + new Gson().toJson(newStudent));

            requireActivity().runOnUiThread(() -> loadingSpinner.setVisibility(View.VISIBLE));
            Call<StudentResponse> call = isEdit ?
                    RetrofitClient.getApiService().updateStudent(newStudent) :
                    RetrofitClient.getApiService().addStudent(newStudent);

            call.enqueue(new Callback<StudentResponse>() {
                @Override
                public void onResponse(Call<StudentResponse> call, Response<StudentResponse> response) {
                    requireActivity().runOnUiThread(() -> loadingSpinner.setVisibility(View.GONE));
                    if (response.isSuccessful() && response.body() != null) {
                        StudentResponse studentResponse = response.body();
                        showToast(studentResponse.getMessage());
                        Log.d(TAG, "Operation response on 2025-06-03 19:53 PM IST: " + new Gson().toJson(studentResponse));
                        if ("success".equals(studentResponse.getStatus())) {
                            new Thread(() -> fetchStudents()).start();
                            dialog.dismiss();
                        }
                    } else {
                        String errorMsg = "Operation failed: HTTP " + response.code();
                        if (response.errorBody() != null) {
                            try {
                                errorMsg += ", " + response.errorBody().string();
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading errorBody on 2025-06-03 19:53 PM IST", e);
                            }
                        }
                        showToast(errorMsg);
                        Log.e(TAG, errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<StudentResponse> call, Throwable t) {
                    requireActivity().runOnUiThread(() -> loadingSpinner.setVisibility(View.GONE));
                    showToast("Operation error: " + t.getMessage());
                    Log.e(TAG, "Operation error on 2025-06-03 19:53 PM IST", t);
                }
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
        requireActivity().runOnUiThread(() -> loadingSpinner.setVisibility(View.VISIBLE));
        RetrofitClient.getApiService().deleteStudent(student.getStudent_id()).enqueue(new Callback<StudentResponse>() {
            @Override
            public void onResponse(Call<StudentResponse> call, Response<StudentResponse> response) {
                requireActivity().runOnUiThread(() -> loadingSpinner.setVisibility(View.GONE));
                if (response.isSuccessful() && response.body() != null) {
                    StudentResponse studentResponse = response.body();
                    showToast(studentResponse.getMessage());
                    Log.d(TAG, "Delete response on 2025-06-03 19:53 PM IST: " + new Gson().toJson(studentResponse));
                    if ("success".equals(studentResponse.getStatus())) {
                        new Thread(() -> fetchStudents()).start();
                    }
                } else {
                    String errorMsg = "Delete failed: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ", " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading errorBody on 2025-06-03 19:53 PM IST", e);
                        }
                    }
                    showToast(errorMsg);
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<StudentResponse> call, Throwable t) {
                requireActivity().runOnUiThread(() -> loadingSpinner.setVisibility(View.GONE));
                showToast("Delete error: " + t.getMessage());
                Log.e(TAG, "Delete error on 2025-06-03 19:53 PM IST", t);
            }
        });
    }

    private void showToast(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show());
    }
}