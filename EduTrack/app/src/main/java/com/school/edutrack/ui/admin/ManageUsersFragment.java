package com.school.edutrack.ui.admin;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.school.edutrack.R;
import com.school.edutrack.model.ApiResponse;
import com.school.edutrack.model.Teacher;
import com.school.edutrack.network.RetrofitClient;
import com.school.edutrack.utils.DialogUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageUsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private List<Teacher> teacherList;
    private List<Teacher> filteredTeacherList;
    private FloatingActionButton addTeacherFab;
    private SearchView searchView;
    private Spinner classFilterSpinner;
    private Spinner subjectFilterSpinner;
    private String selectedClass = "All Classes";
    private String selectedSubject = "All Subjects";
    private String currentQuery = "";
    private HeaderAnimationListener headerAnimationListener;
    private float lastScrollY = 0f;
    private static final float HEADER_SHRINK_THRESHOLD = 200f; // Pixels to scroll before fully shrinking
    private final Handler scrollHandler = new Handler(Looper.getMainLooper());
    private Runnable scrollRunnable;
    private float lastReportedScrollPercentage = 0f;

    public interface HeaderAnimationListener {
        void onHeaderScroll(float scrollPercentage);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HeaderAnimationListener) {
            headerAnimationListener = (HeaderAnimationListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement HeaderAnimationListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.users_recycler_view);
        addTeacherFab = view.findViewById(R.id.add_teacher_fab);
        searchView = view.findViewById(R.id.search_view);
        classFilterSpinner = view.findViewById(R.id.class_filter_spinner);
        subjectFilterSpinner = view.findViewById(R.id.subject_filter_spinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        teacherList = new ArrayList<>();
        filteredTeacherList = new ArrayList<>();
        adapter = new TeacherAdapter(filteredTeacherList);
        recyclerView.setAdapter(adapter);

        // Add scroll listener to detect swipe-up with debouncing
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastScrollY += dy;
                float scrollPercentage = Math.min(lastScrollY / HEADER_SHRINK_THRESHOLD, 1f);

                if (scrollRunnable != null) {
                    scrollHandler.removeCallbacks(scrollRunnable);
                }

                scrollRunnable = () -> {
                    if (headerAnimationListener != null && Math.abs(scrollPercentage - lastReportedScrollPercentage) >= 0.01f) {
                        headerAnimationListener.onHeaderScroll(scrollPercentage);
                        lastReportedScrollPercentage = scrollPercentage;
                        Log.d("ManageUsersFragment", "Scroll percentage: " + scrollPercentage + " at 2025-06-24 07:22 AM IST");
                    }
                };
                scrollHandler.postDelayed(scrollRunnable, 50);
            }
        });

        // Setup Class Filter Spinner with null check
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
            DialogUtils.showFailureDialog(requireContext(), "Error", "Class filter spinner not found", null);
        }

        // Setup Subject Filter Spinner with null check
        if (subjectFilterSpinner != null) {
            ArrayAdapter<CharSequence> subjectAdapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.subject_filter_options,
                    android.R.layout.simple_spinner_item
            );
            subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            subjectFilterSpinner.setAdapter(subjectAdapter);
            subjectFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedSubject = parent.getItemAtPosition(position).toString();
                    applyFilters();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedSubject = "All Subjects";
                    applyFilters();
                }
            });
        } else {
            DialogUtils.showFailureDialog(requireContext(), "Error", "Subject filter spinner not found", null);
        }

        addTeacherFab.setOnClickListener(v -> showTeacherDialog(null));

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

        // Fetch teachers in background
        new Thread(this::fetchTeachers).start();
    }

    private void fetchTeachers() {
        requireActivity().runOnUiThread(() -> {
            DialogUtils.showLoadingDialog(requireContext());
            recyclerView.setVisibility(View.GONE);
        });

        RetrofitClient.getApiService().getTeachers().enqueue(new Callback<List<Teacher>>() {
            @Override
            public void onResponse(Call<List<Teacher>> call, Response<List<Teacher>> response) {
                requireActivity().runOnUiThread(() -> {
                    DialogUtils.hideLoadingDialog();
                    recyclerView.setVisibility(View.VISIBLE);
                });
                if (response.isSuccessful() && response.body() != null) {
                    teacherList.clear();
                    teacherList.addAll(response.body());
                    requireActivity().runOnUiThread(() -> {
                        applyFilters();
                        recyclerView.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
                    });
                    Log.d("ManageUsers", "Fetched " + teacherList.size() + " teachers at 2025-06-24 07:22 AM IST: " + new Gson().toJson(teacherList));
                } else {
                    String errorMsg = "Failed to fetch teachers: HTTP " + response.code();
                    ResponseBody errorBody = response.errorBody();
                    if (errorBody != null) {
                        try {
                            String rawError = errorBody.string();
                            errorMsg += ", Raw response: " + rawError;
                            Log.e("ManageUsers", "Malformed JSON response at 2025-06-24 07:22 AM IST: " + rawError);
                        } catch (Exception e) {
                            Log.e("ManageUsers", "Error reading errorBody at 2025-06-24 07:22 AM IST", e);
                        }
                    }
                    DialogUtils.showFailureDialog(requireContext(), "Fetch Error", errorMsg, null);
                    Log.e("ManageUsers", errorMsg + " at 2025-06-24 07:22 AM IST");
                }
            }

            @Override
            public void onFailure(Call<List<Teacher>> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    DialogUtils.hideLoadingDialog();
                    recyclerView.setVisibility(View.VISIBLE);
                });
                DialogUtils.showFailureDialog(requireContext(), "Fetch Error", "Fetch error: " + t.getMessage(), null);
                Log.e("ManageUsers", "Fetch error at 2025-06-24 07:22 AM IST", t);
            }
        });
    }

    private void applyFilters() {
        filteredTeacherList.clear();

        for (Teacher teacher : teacherList) {
            boolean matchesQuery = currentQuery.isEmpty() || (teacher.getName() != null && teacher.getName().toLowerCase().contains(currentQuery.toLowerCase()));
            boolean matchesClass = selectedClass.equals("All Classes") || (teacher.getClass_() != null && teacher.getClass_().equals(selectedClass.replace("Class ", "")));
            boolean matchesSubject = selectedSubject.equals("All Subjects") || (teacher.getSubject() != null && teacher.getSubject().equals(selectedSubject));

            if (matchesQuery && matchesClass && matchesSubject) {
                filteredTeacherList.add(teacher);
            }
        }

        if (filteredTeacherList.isEmpty() && !currentQuery.isEmpty()) {
            requireActivity().runOnUiThread(() -> {
                DialogUtils.showLoadingDialog(requireContext());
            });
            RetrofitClient.getApiService().getTeacher(currentQuery).enqueue(new Callback<Teacher>() {
                @Override
                public void onResponse(Call<Teacher> call, Response<Teacher> response) {
                    requireActivity().runOnUiThread(() -> {
                        DialogUtils.hideLoadingDialog();
                    });
                    if (response.isSuccessful() && response.body() != null) {
                        Teacher teacher = response.body();
                        boolean matchesClass = selectedClass.equals("All Classes") || (teacher.getClass_() != null && teacher.getClass_().equals(selectedClass.replace("Class ", "")));
                        boolean matchesSubject = selectedSubject.equals("All Subjects") || (teacher.getSubject() != null && teacher.getSubject().equals(selectedSubject));
                        if (matchesClass && matchesSubject) {
                            filteredTeacherList.add(teacher);
                            requireActivity().runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                                DialogUtils.showAlertDialog(requireContext(), "Teacher Found", "Teacher found by ID", null);
                            });
                        }
                    } else {
                        String errorMsg = "No teacher found for: " + currentQuery + ", HTTP " + response.code();
                        ResponseBody errorBody = response.errorBody();
                        if (errorBody != null) {
                            try {
                                errorMsg += ", Raw response: " + errorBody.string();
                                Log.e("ManageUsers", "Malformed JSON response at 2025-06-24 07:22 AM IST: " + errorBody.string());
                            } catch (Exception e) {
                                Log.e("ManageUsers", "Error reading errorBody at 2025-06-24 07:22 AM IST", e);
                            }
                        }
                        DialogUtils.showFailureDialog(requireContext(), "Search Error", errorMsg, null);
                        Log.e("ManageUsers", errorMsg + " at 2025-06-24 07:22 AM IST");
                    }
                    requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                }

                @Override
                public void onFailure(Call<Teacher> call, Throwable t) {
                    requireActivity().runOnUiThread(() -> {
                        DialogUtils.hideLoadingDialog();
                        adapter.notifyDataSetChanged();
                    });
                    DialogUtils.showFailureDialog(requireContext(), "Search Error", "Search error: " + t.getMessage(), null);
                    Log.e("ManageUsers", "Search error at 2025-06-24 07:22 AM IST", t);
                }
            });
        } else {
            requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
        }
    }

    private void showTeacherDialog(Teacher teacher) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_teacher);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText idInput = dialog.findViewById(R.id.teacher_id_input);
        EditText nameInput = dialog.findViewById(R.id.name_input);
        EditText emailInput = dialog.findViewById(R.id.email_input);
        EditText classInput = dialog.findViewById(R.id.class_input);
        EditText sectionInput = dialog.findViewById(R.id.section_input);
        EditText subjectInput = dialog.findViewById(R.id.subject_input);
        EditText dojInput = dialog.findViewById(R.id.doj_input);
        Spinner classTeacherSpinner = dialog.findViewById(R.id.class_teacher_spinner);
        EditText passwordInput = dialog.findViewById(R.id.password_input);
        EditText genderInput = dialog.findViewById(R.id.gender_input);
        EditText mobileInput = dialog.findViewById(R.id.mobile_input);
        EditText addressInput = dialog.findViewById(R.id.address_input);
        Button saveButton = dialog.findViewById(R.id.save_button);
        Button cancelButton = dialog.findViewById(R.id.cancel_button);

        // Setup Class Teacher Spinner
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.class_teacher_options,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classTeacherSpinner.setAdapter(spinnerAdapter);

        // Setup Date Picker for DOJ
        dojInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        dojInput.setText(date);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        boolean isEdit = teacher != null;
        if (isEdit) {
            idInput.setText(teacher.getTeacher_id());
            idInput.setEnabled(false);
            nameInput.setText(teacher.getName());
            emailInput.setText(teacher.getEmail());
            classInput.setText(teacher.getClass_());
            sectionInput.setText(teacher.getSection());
            subjectInput.setText(teacher.getSubject());
            dojInput.setText(teacher.getDoj());
            classTeacherSpinner.setSelection(teacher.getIsClassTeacherForUI().equals("Yes") ? 0 : 1);
            genderInput.setText(teacher.getGender());
            mobileInput.setText(teacher.getMobileNumber());
            addressInput.setText(teacher.getAddress());
            passwordInput.setVisibility(View.GONE);
        } else {
            passwordInput.setVisibility(View.VISIBLE);
        }

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String teacherId = idInput.getText().toString().trim();
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String className = classInput.getText().toString().trim();
            String section = sectionInput.getText().toString().trim();
            String subject = subjectInput.getText().toString().trim();
            String doj = dojInput.getText().toString().trim();
            String isClassTeacher = classTeacherSpinner.getSelectedItem().toString().toLowerCase();
            String password = passwordInput.getText().toString().trim();
            String gender = genderInput.getText().toString().trim();
            String mobile = mobileInput.getText().toString().trim();
            String address = addressInput.getText().toString().trim();

            if (teacherId.isEmpty() || name.isEmpty() || email.isEmpty() || className.isEmpty() ||
                    section.isEmpty() || subject.isEmpty() || doj.isEmpty() || (!isEdit && password.isEmpty()) ||
                    gender.isEmpty() || mobile.isEmpty() || address.isEmpty()) {
                DialogUtils.showFailureDialog(requireContext(), "Validation Error", "Please fill all fields", null);
                return;
            }

            Teacher newTeacher = new Teacher();
            newTeacher.setTeacher_id(teacherId);
            newTeacher.setName(name);
            newTeacher.setEmail(email);
            newTeacher.setClass_(className);
            newTeacher.setSection(section);
            newTeacher.setSubject(subject);
            newTeacher.setDoj(doj);
            newTeacher.setIsClassTeacherFromUI(isClassTeacher);
            newTeacher.setGender(gender);
            newTeacher.setMobileNumber(mobile);
            newTeacher.setAddress(address);
            if (!isEdit) {
                newTeacher.setPassword(password);
            }

            Log.d("ManageUsers", "Sending teacher at 2025-06-24 07:22 AM IST: " + new Gson().toJson(newTeacher));

            requireActivity().runOnUiThread(() -> {
                DialogUtils.showLoadingDialog(requireContext());
            });
            Call<ApiResponse> call = isEdit ?
                    RetrofitClient.getApiService().updateTeacher(newTeacher) :
                    RetrofitClient.getApiService().addTeacher(newTeacher);

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    requireActivity().runOnUiThread(() -> {
                        DialogUtils.hideLoadingDialog();
                    });
                    if (response.isSuccessful() && response.body() != null) {
                        DialogUtils.showSuccessDialog(requireContext(), "Success", response.body().getMessage(), () -> {
                            Log.d("ManageUsers", "Operation response at 2025-06-24 07:22 AM IST: " + new Gson().toJson(response.body()));
                            if (response.body().getStatus().equals("success")) {
                                new Thread(() -> fetchTeachers()).start();
                                dialog.dismiss();
                            }
                        });
                    } else {
                        String errorMsg = "Operation failed: HTTP " + response.code();
                        ResponseBody errorBody = response.errorBody();
                        if (errorBody != null) {
                            try {
                                errorMsg += ", Raw response: " + errorBody.string();
                                Log.e("ManageUsers", "Malformed JSON response at 2025-06-24 07:22 AM IST: " + errorBody.string());
                            } catch (Exception e) {
                                Log.e("ManageUsers", "Error reading errorBody at 2025-06-24 07:22 AM IST", e);
                            }
                        }
                        DialogUtils.showFailureDialog(requireContext(), "Operation Failed", errorMsg, null);
                        Log.e("ManageUsers", errorMsg + " at 2025-06-24 07:22 AM IST");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    requireActivity().runOnUiThread(() -> {
                        DialogUtils.hideLoadingDialog();
                    });
                    DialogUtils.showFailureDialog(requireContext(), "Operation Error", "Operation error: " + t.getMessage(), null);
                    Log.e("ManageUsers", "Operation error at 2025-06-24 07:22 AM IST", t);
                }
            });
        });

        dialog.show();
    }

    private class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {
        private final List<Teacher> teachers;

        public TeacherAdapter(List<Teacher> teachers) {
            this.teachers = teachers;
        }

        @NonNull
        @Override
        public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher, parent, false);
            return new TeacherViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
            Teacher teacher = teachers.get(position);
            holder.nameText.setText(teacher.getName() != null ? teacher.getName() : "N/A");
            holder.classText.setText(teacher.getClass_() != null && teacher.getSection() != null
                    ? "Class: " + teacher.getClass_() + "-" + teacher.getSection()
                    : "Class: N/A");
            holder.subjectText.setText(teacher.getSubject() != null ? "Subject: " + teacher.getSubject() : "Subject: N/A");
            holder.dojText.setText(teacher.getDoj() != null ? "DOJ: " + teacher.getDoj() : "DOJ: N/A");
            holder.genderText.setText(teacher.getGender() != null ? "Gender: " + teacher.getGender() : "Gender: N/A");
            holder.mobileText.setText(teacher.getMobileNumber() != null ? "Mobile: " + teacher.getMobileNumber() : "Mobile: N/A");
            holder.addressText.setText(teacher.getAddress() != null ? "Address: " + teacher.getAddress() : "Address: N/A");

            holder.editButton.setOnClickListener(v -> showTeacherDialog(teacher));
            holder.deleteButton.setOnClickListener(v -> {
                requireActivity().runOnUiThread(() -> {
                    DialogUtils.showLoadingDialog(requireContext());
                });
                RetrofitClient.getApiService().deleteTeacher(teacher.getTeacher_id()).enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        requireActivity().runOnUiThread(() -> {
                            DialogUtils.hideLoadingDialog();
                        });
                        if (response.isSuccessful() && response.body() != null) {
                            DialogUtils.showSuccessDialog(requireContext(), "Success", response.body().getMessage(), () -> {
                                Log.d("ManageUsers", "Delete response at 2025-06-24 07:22 AM IST: " + new Gson().toJson(response.body()));
                                if (response.body().getStatus().equals("success")) {
                                    new Thread(() -> fetchTeachers()).start();
                                }
                            });
                        } else {
                            String errorMsg = "Delete failed: HTTP " + response.code();
                            ResponseBody errorBody = response.errorBody();
                            if (errorBody != null) {
                                try {
                                    errorMsg += ", Raw response: " + errorBody.string();
                                    Log.e("ManageUsers", "Malformed JSON response at 2025-06-24 07:22 AM IST: " + errorBody.string());
                                } catch (Exception e) {
                                    Log.e("ManageUsers", "Error reading errorBody at 2025-06-24 07:22 AM IST", e);
                                }
                            }
                            DialogUtils.showFailureDialog(requireContext(), "Delete Failed", errorMsg, null);
                            Log.e("ManageUsers", errorMsg + " at 2025-06-24 07:22 AM IST");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        requireActivity().runOnUiThread(() -> {
                            DialogUtils.hideLoadingDialog();
                        });
                        DialogUtils.showFailureDialog(requireContext(), "Delete Error", "Delete error: " + t.getMessage(), null);
                        Log.e("ManageUsers", "Delete error at 2025-06-24 07:22 AM IST", t);
                    }
                });
            });
        }

        @Override
        public int getItemCount() {
            return teachers.size();
        }

        class TeacherViewHolder extends RecyclerView.ViewHolder {
            TextView nameText, classText, subjectText, dojText, genderText, mobileText, addressText;
            Button editButton, deleteButton;

            public TeacherViewHolder(@NonNull View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.teacher_name);
                classText = itemView.findViewById(R.id.teacher_class);
                subjectText = itemView.findViewById(R.id.teacher_subject);
                dojText = itemView.findViewById(R.id.teacher_doj);
                genderText = itemView.findViewById(R.id.teacher_gender);
                mobileText = itemView.findViewById(R.id.teacher_mobile);
                addressText = itemView.findViewById(R.id.teacher_address);
                editButton = itemView.findViewById(R.id.edit_button);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }
        }
    }
}