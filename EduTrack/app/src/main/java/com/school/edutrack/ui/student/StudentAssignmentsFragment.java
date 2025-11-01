package com.school.edutrack.ui.student;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.LearningMaterial;
import com.school.edutrack.model.LearningMaterialsResponse;
import com.school.edutrack.model.StudentQuizModels;
import com.school.edutrack.network.RetrofitClient;
import com.school.edutrack.utils.DialogUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentAssignmentsFragment extends Fragment {

    private static final String TAG = "StudentAssignments";
    private String studentId;
    private String studentClass, studentSection;
    private TextView studentIdDisplay, classSectionDisplay, noEntriesMessage;
    private Spinner subjectFilter, typeFilter;
    private RecyclerView entriesRecyclerView;
    private Button retryButton;
    private LearningMaterialAdapter adapter;
    private List<LearningMaterial> entries = new ArrayList<>();
    private List<LearningMaterial> filteredEntries = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            studentId = getArguments().getString("studentId");
            Log.d(TAG, "Student ID retrieved: " + studentId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_student_assignments, container, false);

        // Initialize views
        studentIdDisplay = root.findViewById(R.id.student_id_display);
        classSectionDisplay = root.findViewById(R.id.class_section_display);
        subjectFilter = root.findViewById(R.id.subject_filter);
        typeFilter = root.findViewById(R.id.type_filter);
        entriesRecyclerView = root.findViewById(R.id.entries_recycler_view);
        noEntriesMessage = root.findViewById(R.id.no_entries_message);
        retryButton = root.findViewById(R.id.retry_button);

        // Set student ID
        if (studentId != null) {
            studentIdDisplay.setText("Student ID: " + studentId);
        } else {
            Log.w(TAG, "Student ID is null");
            DialogUtils.showFailureDialog(requireContext(), "Error", "Student ID not found", () -> {
                if (getActivity() != null) getActivity().onBackPressed();
            });
            return root;
        }

        // Setup subject filter spinner
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.subject_filter_options));
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectFilter.setAdapter(subjectAdapter);

        // Setup type filter spinner
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"All Types", "Homework", "Assignment", "Study Material"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeFilter.setAdapter(typeAdapter);

        // Setup filter listeners
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterEntries();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        subjectFilter.setOnItemSelectedListener(filterListener);
        typeFilter.setOnItemSelectedListener(filterListener);

        // Setup retry button
        retryButton.setOnClickListener(v -> fetchStudentDetails());

        // Setup RecyclerView with animation
        adapter = new LearningMaterialAdapter(filteredEntries);
        entriesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        entriesRecyclerView.setAdapter(adapter);
        entriesRecyclerView.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator() {
            @Override
            public void onAnimationStarted(RecyclerView.ViewHolder viewHolder) {
                viewHolder.itemView.setAlpha(0f);
                viewHolder.itemView.setScaleY(0.8f);
                Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);
                fadeIn.setDuration(300);
                viewHolder.itemView.startAnimation(fadeIn);
            }
        });

        // Fetch class and section, then learning materials
        fetchStudentDetails();

        return root;
    }

    private void fetchStudentDetails() {
        if (studentId == null || studentId.isEmpty()) {
            Log.e(TAG, "Student ID is null or empty");
            DialogUtils.showFailureDialog(requireContext(), "Error", "Cannot fetch student details: Invalid student ID", null);
            showRetryButton(true);
            return;
        }

        DialogUtils.showLoadingDialog(requireContext());
        RetrofitClient.getStudentApiService().getStudentQuizDetails("get_student_details", studentId).enqueue(new Callback<StudentQuizModels.StudentQuizDetailsResponse>() {
            @Override
            public void onResponse(Call<StudentQuizModels.StudentQuizDetailsResponse> call, Response<StudentQuizModels.StudentQuizDetailsResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && response.body().getStatus().equals("success")) {
                    StudentQuizModels.StudentQuizDetailsResponse detailsResponse = response.body();
                    studentClass = detailsResponse.getStudent().getClassName();
                    studentSection = detailsResponse.getStudent().getSection();
                    classSectionDisplay.setText(String.format(Locale.US, "Class: %s | Section: %s", studentClass, studentSection));
                    Log.d(TAG, "Fetched class: " + studentClass + ", section: " + studentSection);
                    fetchLearningMaterials();
                } else {
                    Log.e(TAG, "Failed to fetch student details: " + response.code());
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to fetch student details: " + error, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                    showRetryButton(true);
                }
            }

            @Override
            public void onFailure(Call<StudentQuizModels.StudentQuizDetailsResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                Log.e(TAG, "Network error fetching student details: " + t.getMessage());
                DialogUtils.showFailureDialog(requireContext(), "Network Error", "Failed to fetch student details: " + t.getMessage(), null);
                showRetryButton(true);
            }
        });
    }

    private void fetchLearningMaterials() {
        if (studentClass == null || studentSection == null) {
            Log.e(TAG, "Class or section is null, cannot fetch learning materials");
            showRetryButton(true);
            return;
        }

        DialogUtils.showLoadingDialog(requireContext());
        RetrofitClient.getStudentApiService().getStudentLearningMaterials(studentId, studentClass, studentSection).enqueue(new Callback<LearningMaterialsResponse>() {
            @Override
            public void onResponse(Call<LearningMaterialsResponse> call, Response<LearningMaterialsResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && response.body().getStatus().equals("success")) {
                    entries.clear();
                    entries.addAll(response.body().getData());
                    Log.d(TAG, "Fetched " + entries.size() + " learning materials");
                    filterEntries();
                    showRetryButton(false);
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Failed to fetch learning materials: " + error);
                        DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to fetch learning materials: " + error, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                    showRetryButton(true);
                }
            }

            @Override
            public void onFailure(Call<LearningMaterialsResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                Log.e(TAG, "Network error: " + t.getMessage());
                DialogUtils.showFailureDialog(requireContext(), "Network Error", "Failed to fetch learning materials: " + t.getMessage(), null);
                showRetryButton(true);
            }
        });
    }

    private void filterEntries() {
        String selectedSubject = subjectFilter.getSelectedItem().toString();
        String selectedType = typeFilter.getSelectedItem().toString();
        filteredEntries.clear();
        for (LearningMaterial entry : entries) {
            boolean subjectMatch = selectedSubject.equals("All Subjects") || entry.getSubject().equals(selectedSubject);
            boolean typeMatch = selectedType.equals("All Types") || entry.getType().equals(selectedType);
            if (subjectMatch && typeMatch) {
                filteredEntries.add(entry);
            }
        }
        adapter.notifyDataSetChanged();
        updateEntriesVisibility();
    }

    private void updateEntriesVisibility() {
        if (filteredEntries.isEmpty()) {
            entriesRecyclerView.setVisibility(View.GONE);
            noEntriesMessage.setVisibility(View.VISIBLE);
        } else {
            entriesRecyclerView.setVisibility(View.VISIBLE);
            noEntriesMessage.setVisibility(View.GONE);
            showRetryButton(false);
        }
    }

    private void showRetryButton(boolean show) {
        retryButton.setVisibility(show ? View.VISIBLE : View.GONE);
        noEntriesMessage.setVisibility(show || filteredEntries.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private class LearningMaterialAdapter extends RecyclerView.Adapter<LearningMaterialAdapter.ViewHolder> {
        private List<LearningMaterial> materials;

        public LearningMaterialAdapter(List<LearningMaterial> materials) {
            this.materials = materials;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_learning_material_student, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LearningMaterial material = materials.get(position);
            holder.name.setText(material.getName());
            holder.description.setText(material.getDescription());
            holder.details.setText(String.format(Locale.US, "Subject: %s | Type: %s | Created: %s%s",
                    material.getSubject(), material.getType(), material.getCreated_at(),
                    material.getDue_date() != null ? " | Due: " + material.getDue_date() : ""));
            if (material.getFile() != null) {
                holder.file.setText(new File(material.getFile()).getName());
                holder.fileContainer.setVisibility(View.VISIBLE);
                holder.fileContainer.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(material.getFile()));
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening file: " + e.getMessage());
                        DialogUtils.showFailureDialog(requireContext(), "Error", "Cannot open file", null);
                    }
                });
            } else {
                holder.fileContainer.setVisibility(View.GONE);
            }

            // Hover effect
            holder.cardView.setOnClickListener(v -> {
                holder.cardView.setCardElevation(12f);
                Animation scaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_anim);
                holder.cardView.startAnimation(scaleUp);
                holder.cardView.postDelayed(() -> {
                    holder.cardView.setCardElevation(8f);
                    Animation scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_in);
                    holder.cardView.startAnimation(scaleDown);
                }, 100);
            });
        }

        @Override
        public int getItemCount() {
            return materials.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView name, description, details, file;
            LinearLayout fileContainer;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.card_view);
                name = itemView.findViewById(R.id.item_name);
                description = itemView.findViewById(R.id.item_description);
                details = itemView.findViewById(R.id.item_details);
                file = itemView.findViewById(R.id.item_file);
                fileContainer = itemView.findViewById(R.id.file_container);
            }
        }
    }
}