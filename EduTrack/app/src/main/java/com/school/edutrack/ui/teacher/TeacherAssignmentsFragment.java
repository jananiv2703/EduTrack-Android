package com.school.edutrack.ui.teacher;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.LearningMaterial;
import com.school.edutrack.model.LearningMaterialsResponse;
import com.school.edutrack.network.RetrofitClient;
import com.school.edutrack.utils.DialogUtils;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherAssignmentsFragment extends Fragment {

    private static final String TAG = "TeacherAssignments";
    private String teacherId;
    private Spinner classInput, sectionInput, subjectInput, typeSpinner;
    private EditText nameInput, descriptionInput, dueDateInput;
    private Button filePickerButton, submitButton;
    private TextView selectedFileName;
    private RecyclerView entriesRecyclerView;
    private TextView noEntriesMessage;
    private LearningMaterialAdapter adapter;
    private List<LearningMaterial> entries = new ArrayList<>();
    private String selectedFileData;
    private String selectedFileNameStr;
    private LearningMaterial editingEntry;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teacher_assignments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        TextView teacherIdDisplay = view.findViewById(R.id.teacher_id_display);
        classInput = view.findViewById(R.id.class_input);
        sectionInput = view.findViewById(R.id.section_input);
        subjectInput = view.findViewById(R.id.subject_input);
        typeSpinner = view.findViewById(R.id.type_spinner);
        nameInput = view.findViewById(R.id.name_input);
        descriptionInput = view.findViewById(R.id.description_input);
        dueDateInput = view.findViewById(R.id.due_date_input);
        filePickerButton = view.findViewById(R.id.file_picker_button);
        selectedFileName = view.findViewById(R.id.selected_file_name);
        submitButton = view.findViewById(R.id.submit_button);
        entriesRecyclerView = view.findViewById(R.id.entries_recycler_view);
        noEntriesMessage = view.findViewById(R.id.no_entries_message);

        // Retrieve teacherId
        if (getArguments() != null) {
            teacherId = getArguments().getString("teacherId");
            Log.d(TAG, "Teacher ID retrieved: " + teacherId);
        } else {
            Log.w(TAG, "Teacher ID not found");
            DialogUtils.showFailureDialog(requireContext(), "Error", "Teacher ID not found", () -> {
                if (getActivity() != null) getActivity().onBackPressed();
            });
            return;
        }
        teacherIdDisplay.setText("Teacher ID: " + teacherId);

        // Setup type spinner (hardcoded as per PHP API)
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"Homework", "Assignment", "Study Material"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dueDateInput.setVisibility(typeSpinner.getSelectedItem().equals("Assignment") ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup date picker
        dueDateInput.setOnClickListener(v -> showDatePicker());

        // Setup file picker
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                try {
                    selectedFileData = fileToBase64(uri);
                    selectedFileNameStr = getFileName(uri);
                    selectedFileName.setText(selectedFileNameStr);
                    selectedFileName.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Log.e(TAG, "File selection error: " + e.getMessage());
                    DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to select file", null);
                }
            }
        });
        filePickerButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            filePickerLauncher.launch(intent);
        });

        // Setup RecyclerView
        adapter = new LearningMaterialAdapter(entries, this::editEntry, this::deleteEntry);
        entriesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        entriesRecyclerView.setAdapter(adapter);

        // Setup submit button
        submitButton.setOnClickListener(v -> submitEntry());

        // Setup listeners for class and section spinners to fetch entries
        AdapterView.OnItemSelectedListener fetchListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchEntries();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        classInput.setOnItemSelectedListener(fetchListener);
        sectionInput.setOnItemSelectedListener(fetchListener);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    dueDateInput.setText(String.format(Locale.US, "%d-%02d-%02d", year, month + 1, day));
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private String fileToBase64(Uri uri) throws Exception {
        File file = new File(requireContext().getContentResolver().openInputStream(uri).toString());
        byte[] bytes = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(bytes);
        fis.close();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private String getFileName(Uri uri) {
        String path = uri.getPath();
        return path != null ? new File(path).getName() : "file";
    }

    private void submitEntry() {
        String className = classInput.getSelectedItem().toString();
        String section = sectionInput.getSelectedItem().toString();
        String subject = subjectInput.getSelectedItem().toString();
        String type = typeSpinner.getSelectedItem().toString();
        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String dueDate = dueDateInput.getText().toString().trim();

        // Validate inputs
        if (className.equals("All Classes")) {
            DialogUtils.showFailureDialog(requireContext(), "Error", "Please select a specific class", null);
            return;
        }
        if (section.equals("All Sections")) {
            DialogUtils.showFailureDialog(requireContext(), "Error", "Please select a specific section", null);
            return;
        }
        if (subject.equals("All Subjects")) {
            DialogUtils.showFailureDialog(requireContext(), "Error", "Please select a specific subject", null);
            return;
        }
        if (name.isEmpty() || description.isEmpty()) {
            DialogUtils.showFailureDialog(requireContext(), "Error", "Name and description are required", null);
            return;
        }
        if (type.equals("Assignment") && dueDate.isEmpty()) {
            DialogUtils.showFailureDialog(requireContext(), "Error", "Due date is required for Assignment", null);
            return;
        }

        LearningMaterial material = new LearningMaterial();
        material.setTeacherId(teacherId);
        material.setClassName(className);
        material.setSection(section);
        material.setSubject(subject);
        material.setType(type);
        material.setName(name);
        material.setDescription(description);
        material.setDue_date(type.equals("Assignment") ? dueDate : null);
        material.setFile_data(selectedFileData);
        material.setFile_name(selectedFileNameStr);

        DialogUtils.showLoadingDialog(requireContext());
        if (editingEntry != null) {
            material.setId(editingEntry.getId());
            updateEntry(material);
        } else {
            createEntry(material);
        }
    }

    private void createEntry(LearningMaterial material) {
        RetrofitClient.getApiService().createLearningMaterial(material).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful()) {
                    DialogUtils.showSuccessDialog(requireContext(), "Success", "Entry created successfully", () -> {
                        clearForm();
                        fetchEntries();
                    });
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to create entry: " + error, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                DialogUtils.showFailureDialog(requireContext(), "Network Error", "Failed to create entry: " + t.getMessage(), null);
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });
    }

    private void updateEntry(LearningMaterial material) {
        RetrofitClient.getApiService().updateLearningMaterial(material).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful()) {
                    DialogUtils.showSuccessDialog(requireContext(), "Success", "Entry updated successfully", () -> {
                        clearForm();
                        fetchEntries();
                    });
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to update entry: " + error, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                DialogUtils.showFailureDialog(requireContext(), "Network Error", "Failed to update entry: " + t.getMessage(), null);
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });
    }

    private void fetchEntries() {
        String className = classInput.getSelectedItem().toString();
        String section = sectionInput.getSelectedItem().toString();
        if (className.equals("All Classes") || section.equals("All Sections")) {
            entries.clear();
            adapter.notifyDataSetChanged();
            updateEntriesVisibility();
            return;
        }
        DialogUtils.showLoadingDialog(requireContext());
        RetrofitClient.getApiService().getLearningMaterials(teacherId, className, section).enqueue(new Callback<LearningMaterialsResponse>() {
            @Override
            public void onResponse(Call<LearningMaterialsResponse> call, Response<LearningMaterialsResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && response.body().getStatus().equals("success")) {
                    entries.clear();
                    entries.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                    updateEntriesVisibility();
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to fetch entries: " + error, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<LearningMaterialsResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                DialogUtils.showFailureDialog(requireContext(), "Network Error", "Failed to fetch entries: " + t.getMessage(), null);
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });
    }

    private void editEntry(LearningMaterial material) {
        editingEntry = material;
        ArrayAdapter<String> classAdapter = (ArrayAdapter<String>) classInput.getAdapter();
        ArrayAdapter<String> sectionAdapter = (ArrayAdapter<String>) sectionInput.getAdapter();
        ArrayAdapter<String> subjectAdapter = (ArrayAdapter<String>) subjectInput.getAdapter();
        ArrayAdapter<String> typeAdapter = (ArrayAdapter<String>) typeSpinner.getAdapter();

        classInput.setSelection(classAdapter.getPosition(material.getClassName()));
        sectionInput.setSelection(sectionAdapter.getPosition(material.getSection()));
        subjectInput.setSelection(subjectAdapter.getPosition(material.getSubject()));
        typeSpinner.setSelection(typeAdapter.getPosition(material.getType()));
        nameInput.setText(material.getName());
        descriptionInput.setText(material.getDescription());
        dueDateInput.setText(material.getDue_date());
        selectedFileName.setText(material.getFile() != null ? new File(material.getFile()).getName() : "No file selected");
        selectedFileName.setVisibility(material.getFile() != null ? View.VISIBLE : View.GONE);
        selectedFileData = null;
        selectedFileNameStr = null;
        submitButton.setText("Update");
    }

    private void deleteEntry(LearningMaterial material) {
        DialogUtils.showLoadingDialog(requireContext());
        RetrofitClient.getApiService().deleteLearningMaterial(material.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful()) {
                    DialogUtils.showSuccessDialog(requireContext(), "Success", "Entry deleted successfully", () -> fetchEntries());
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to delete entry: " + error, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                DialogUtils.showFailureDialog(requireContext(), "Network Error", "Failed to delete entry: " + t.getMessage(), null);
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });
    }

    private void clearForm() {
        classInput.setSelection(0);
        sectionInput.setSelection(0);
        subjectInput.setSelection(0);
        typeSpinner.setSelection(0);
        nameInput.setText("");
        descriptionInput.setText("");
        dueDateInput.setText("");
        selectedFileName.setText("No file selected");
        selectedFileName.setVisibility(View.GONE);
        selectedFileData = null;
        selectedFileNameStr = null;
        submitButton.setText("Submit");
        editingEntry = null;
    }

    private void updateEntriesVisibility() {
        if (entries.isEmpty()) {
            entriesRecyclerView.setVisibility(View.GONE);
            noEntriesMessage.setVisibility(View.VISIBLE);
        } else {
            entriesRecyclerView.setVisibility(View.VISIBLE);
            noEntriesMessage.setVisibility(View.GONE);
        }
    }

    private class LearningMaterialAdapter extends RecyclerView.Adapter<LearningMaterialAdapter.ViewHolder> {
        private List<LearningMaterial> materials;
        private OnItemClickListener editListener;
        private OnItemClickListener deleteListener;

        public LearningMaterialAdapter(List<LearningMaterial> materials, OnItemClickListener editListener, OnItemClickListener deleteListener) {
            this.materials = materials;
            this.editListener = editListener;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_learning_material, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LearningMaterial material = materials.get(position);
            holder.name.setText(material.getName());
            holder.description.setText(material.getDescription());
            holder.details.setText(String.format(Locale.US, "Class: %s | Section: %s | Subject: %s | Type: %s | Created: %s%s",
                    material.getClassName(), material.getSection(), material.getSubject(), material.getType(), material.getCreated_at(),
                    material.getDue_date() != null ? " | Due: " + material.getDue_date() : ""));
            if (material.getFile() != null) {
                holder.file.setText("File: " + new File(material.getFile()).getName());
                holder.file.setVisibility(View.VISIBLE);
            } else {
                holder.file.setVisibility(View.GONE);
            }
            holder.editButton.setOnClickListener(v -> editListener.onItemClick(material));
            holder.deleteButton.setOnClickListener(v -> deleteListener.onItemClick(material));
        }

        @Override
        public int getItemCount() {
            return materials.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, description, details, file;
            Button editButton, deleteButton;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.item_name);
                description = itemView.findViewById(R.id.item_description);
                details = itemView.findViewById(R.id.item_details);
                file = itemView.findViewById(R.id.item_file);
                editButton = itemView.findViewById(R.id.edit_button);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }
        }
    }

    private interface OnItemClickListener {
        void onItemClick(LearningMaterial material);
    }
}