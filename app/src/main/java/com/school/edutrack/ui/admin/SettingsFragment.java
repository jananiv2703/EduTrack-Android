package com.school.edutrack.ui.admin;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.ApiResponse;
import com.school.edutrack.model.FeeResponse;
import com.school.edutrack.model.FeeStructure;
import com.school.edutrack.model.FeeStructureRequest;
import com.school.edutrack.network.RetrofitClient;
import com.school.edutrack.utils.DialogUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private Button toggleNotificationsButton, addFeeButton, updateFeeButton, deleteFeeButton;
    private Spinner classSpinner, feeTypeSpinner;
    private EditText customFeeTypeInput, amountInput;
    private TextView deadlineText;
    private RecyclerView feeStructuresList;
    private SharedPreferences sharedPreferences;
    private FeeStructureAdapter feeStructureAdapter;
    private List<FeeStructure> feeStructures = new ArrayList<>();
    private FeeStructure selectedFeeStructure;
    private List<String> feeTypeOptions = Arrays.asList("Term 1", "Term 2", "Term 3", "Other");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        toggleNotificationsButton = view.findViewById(R.id.toggle_notifications_button);
        classSpinner = view.findViewById(R.id.class_spinner);
        feeTypeSpinner = view.findViewById(R.id.fee_type_spinner);
        customFeeTypeInput = view.findViewById(R.id.custom_fee_type_input);
        amountInput = view.findViewById(R.id.amount_input);
        deadlineText = view.findViewById(R.id.deadline_text);
        addFeeButton = view.findViewById(R.id.add_fee_button);
        updateFeeButton = view.findViewById(R.id.update_fee_button);
        deleteFeeButton = view.findViewById(R.id.delete_fee_button);
        feeStructuresList = view.findViewById(R.id.fee_structures_list);

        sharedPreferences = requireActivity().getSharedPreferences("EduTrackPrefs", Context.MODE_PRIVATE);

        // Notifications toggle
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        toggleNotificationsButton.setText(notificationsEnabled ? "Disable Notifications" : "Enable Notifications");
        toggleNotificationsButton.setOnClickListener(v -> {
            boolean currentState = sharedPreferences.getBoolean("notifications_enabled", true);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notifications_enabled", !currentState);
            editor.apply();
            toggleNotificationsButton.setText(!currentState ? "Disable Notifications" : "Enable Notifications");
            DialogUtils.showSuccessDialog(getContext(), "Notifications", "Notifications " + (!currentState ? "enabled" : "disabled"), null);
        });

        // Setup class spinner with class_options from strings.xml
        ArrayAdapter<CharSequence> classAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.class_options, android.R.layout.simple_spinner_item);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(classAdapter);

        // Setup fee type spinner
        ArrayAdapter<String> feeTypeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, feeTypeOptions);
        feeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feeTypeSpinner.setAdapter(feeTypeAdapter);

        // Enable/disable custom fee type input
        feeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                customFeeTypeInput.setEnabled("Other".equals(feeTypeOptions.get(position)));
                if (!customFeeTypeInput.isEnabled()) {
                    customFeeTypeInput.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                customFeeTypeInput.setEnabled(false);
                customFeeTypeInput.setText("");
            }
        });

        // Setup date picker for deadline
        deadlineText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format(Locale.US, "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        deadlineText.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        // Setup RecyclerView
        feeStructureAdapter = new FeeStructureAdapter(feeStructures, feeStructure -> {
            selectedFeeStructure = feeStructure;
            String className = feeStructure.getClassName();
            int classIndex = Arrays.asList(getResources().getStringArray(R.array.class_options)).indexOf(className);
            classSpinner.setSelection(classIndex >= 0 ? classIndex : 0);
            if (feeTypeOptions.contains(feeStructure.getFeeType())) {
                feeTypeSpinner.setSelection(feeTypeOptions.indexOf(feeStructure.getFeeType()));
                customFeeTypeInput.setEnabled(false);
                customFeeTypeInput.setText("");
            } else {
                feeTypeSpinner.setSelection(feeTypeOptions.indexOf("Other"));
                customFeeTypeInput.setEnabled(true);
                customFeeTypeInput.setText(feeStructure.getFeeType());
            }
            amountInput.setText(feeStructure.getAmount());
            deadlineText.setText(feeStructure.getDeadline());
            updateFeeButton.setEnabled(true);
            deleteFeeButton.setEnabled(true);
        });
        feeStructuresList.setLayoutManager(new LinearLayoutManager(getContext()));
        feeStructuresList.setAdapter(feeStructureAdapter);

        // Button listeners
        addFeeButton.setOnClickListener(v -> addFeeStructure());
        updateFeeButton.setOnClickListener(v -> updateFeeStructure());
        deleteFeeButton.setOnClickListener(v -> deleteFeeStructure());

        // Fetch fee structures
        fetchFeeStructures();
    }

    private void addFeeStructure() {
        String feeType = feeTypeSpinner.getSelectedItem().toString();
        if ("Other".equals(feeType)) {
            feeType = customFeeTypeInput.getText().toString().trim();
        }
        String className = classSpinner.getSelectedItem().toString();
        if ("All Classes".equals(className)) {
            DialogUtils.showAlertDialog(getContext(), "Validation Error", "Please select a specific class", null);
            return;
        }
        String amountStr = amountInput.getText().toString().trim();
        String deadline = deadlineText.getText().toString().trim();

        if (feeType.isEmpty() || className.isEmpty() || amountStr.isEmpty() || deadline.isEmpty()) {
            DialogUtils.showAlertDialog(getContext(), "Validation Error", "Please fill all fields", null);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                DialogUtils.showAlertDialog(getContext(), "Validation Error", "Amount must be greater than 0", null);
                return;
            }
        } catch (NumberFormatException e) {
            DialogUtils.showAlertDialog(getContext(), "Validation Error", "Invalid amount format", null);
            return;
        }

        if (!deadline.matches("\\d{4}-\\d{2}-\\d{2}")) {
            DialogUtils.showAlertDialog(getContext(), "Validation Error", "Deadline must be in YYYY-MM-DD format", null);
            return;
        }

        DialogUtils.showLoadingDialog(getContext());
        FeeStructureRequest request = new FeeStructureRequest(feeType, className, amount, deadline);
        RetrofitClient.getApiService().addFeeStructure(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    DialogUtils.showSuccessDialog(getContext(), "Success", "Fee structure added successfully", () -> {
                        clearInputs();
                        fetchFeeStructures();
                    });
                } else {
                    DialogUtils.showFailureDialog(getContext(), "Error", response.body() != null ? response.body().getMessage() : "Failed to add fee structure", null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                DialogUtils.showFailureDialog(getContext(), "Error", "Network error: " + t.getMessage(), null);
            }
        });
    }

    private void updateFeeStructure() {
        if (selectedFeeStructure == null) {
            DialogUtils.showAlertDialog(getContext(), "Selection Error", "Select a fee structure to update", null);
            return;
        }

        String feeType = feeTypeSpinner.getSelectedItem().toString();
        if ("Other".equals(feeType)) {
            feeType = customFeeTypeInput.getText().toString().trim();
        }
        String className = classSpinner.getSelectedItem().toString();
        if ("All Classes".equals(className)) {
            DialogUtils.showAlertDialog(getContext(), "Validation Error", "Please select a specific class", null);
            return;
        }
        String amountStr = amountInput.getText().toString().trim();
        String deadline = deadlineText.getText().toString().trim();

        if (feeType.isEmpty() || className.isEmpty() || amountStr.isEmpty() || deadline.isEmpty()) {
            DialogUtils.showAlertDialog(getContext(), "Validation Error", "Please fill all fields", null);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                DialogUtils.showAlertDialog(getContext(), "Validation Error", "Amount must be greater than 0", null);
                return;
            }
        } catch (NumberFormatException e) {
            DialogUtils.showAlertDialog(getContext(), "Validation Error", "Invalid amount format", null);
            return;
        }

        if (!deadline.matches("\\d{4}-\\d{2}-\\d{2}")) {
            DialogUtils.showAlertDialog(getContext(), "Validation Error", "Deadline must be in YYYY-MM-DD format", null);
            return;
        }

        DialogUtils.showLoadingDialog(getContext());
        FeeStructureRequest request = new FeeStructureRequest(selectedFeeStructure.getId(), feeType, className, amount, deadline);
        RetrofitClient.getApiService().updateFeeStructure(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    DialogUtils.showSuccessDialog(getContext(), "Success", "Fee structure updated successfully", () -> {
                        clearInputs();
                        updateFeeButton.setEnabled(false);
                        deleteFeeButton.setEnabled(false);
                        selectedFeeStructure = null;
                        fetchFeeStructures();
                    });
                } else {
                    DialogUtils.showFailureDialog(getContext(), "Error", response.body() != null ? response.body().getMessage() : "Failed to update fee structure", null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                DialogUtils.showFailureDialog(getContext(), "Error", "Network error: " + t.getMessage(), null);
            }
        });
    }

    private void deleteFeeStructure() {
        if (selectedFeeStructure == null) {
            DialogUtils.showAlertDialog(getContext(), "Selection Error", "Select a fee structure to delete", null);
            return;
        }

        DialogUtils.showLoadingDialog(getContext());
        RetrofitClient.getApiService().deleteFeeStructure(selectedFeeStructure.getId()).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    DialogUtils.showSuccessDialog(getContext(), "Success", "Fee structure deleted successfully", () -> {
                        clearInputs();
                        updateFeeButton.setEnabled(false);
                        deleteFeeButton.setEnabled(false);
                        selectedFeeStructure = null;
                        fetchFeeStructures();
                    });
                } else {
                    DialogUtils.showFailureDialog(getContext(), "Error", response.body() != null ? response.body().getMessage() : "Failed to delete fee structure", null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                DialogUtils.showFailureDialog(getContext(), "Error", "Network error: " + t.getMessage(), null);
            }
        });
    }

    private void fetchFeeStructures() {
        DialogUtils.showLoadingDialog(getContext());
        RetrofitClient.getApiService().getFeeStructures().enqueue(new Callback<FeeResponse>() {
            @Override
            public void onResponse(Call<FeeResponse> call, Response<FeeResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    feeStructures.clear();
                    feeStructures.addAll(response.body().getData());
                    feeStructureAdapter.notifyDataSetChanged();
                } else {
                    DialogUtils.showFailureDialog(getContext(), "Error", "Failed to fetch fee structures", null);
                }
            }

            @Override
            public void onFailure(Call<FeeResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                DialogUtils.showFailureDialog(getContext(), "Error", "Network error: " + t.getMessage(), null);
            }
        });
    }

    private void clearInputs() {
        classSpinner.setSelection(0);
        feeTypeSpinner.setSelection(0);
        customFeeTypeInput.setText("");
        amountInput.setText("");
        deadlineText.setText("");
    }
}

class FeeStructureAdapter extends RecyclerView.Adapter<FeeStructureAdapter.FeeViewHolder> {
    private List<FeeStructure> feeStructures;
    private OnFeeClickListener listener;

    interface OnFeeClickListener {
        void onFeeClick(FeeStructure feeStructure);
    }

    public FeeStructureAdapter(List<FeeStructure> feeStructures, OnFeeClickListener listener) {
        this.feeStructures = feeStructures;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fee_structure, parent, false);
        return new FeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeeViewHolder holder, int position) {
        FeeStructure fee = feeStructures.get(position);
        holder.feeTypeText.setText(fee.getFeeType());
        holder.classText.setText(fee.getClassName());
        holder.amountText.setText(String.format("$%s", fee.getAmount()));
        holder.deadlineText.setText(fee.getDeadline());
        holder.itemView.setOnClickListener(v -> listener.onFeeClick(fee));
    }

    @Override
    public int getItemCount() {
        return feeStructures.size();
    }

    static class FeeViewHolder extends RecyclerView.ViewHolder {
        TextView feeTypeText, classText, amountText, deadlineText;

        FeeViewHolder(View itemView) {
            super(itemView);
            feeTypeText = itemView.findViewById(R.id.fee_type_text);
            classText = itemView.findViewById(R.id.class_text);
            amountText = itemView.findViewById(R.id.amount_text);
            deadlineText = itemView.findViewById(R.id.deadline_text);
        }
    }
}