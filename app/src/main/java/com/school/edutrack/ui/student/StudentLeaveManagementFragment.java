package com.school.edutrack.ui.student;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.school.edutrack.R;
import com.school.edutrack.model.StudentLeaveRequest;
import com.school.edutrack.network.RetrofitClient;
import com.school.edutrack.network.StudentApiService;
import com.school.edutrack.utils.DialogUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentLeaveManagementFragment extends Fragment {

    private static final String TAG = "StudentLeaveManagementFragment";
    private String studentId;
    private TextInputEditText leaveFromInput, leaveToInput, reasonInput;
    private TextInputLayout leaveFromLayout, leaveToLayout;
    private TextView proofFileName;
    private RecyclerView leaveRequestsRecyclerView;
    private TextView noLeaveRequestsText;
    private LeaveRequestAdapter leaveRequestAdapter;
    private List<StudentLeaveRequest> leaveRequestList;
    private StudentApiService studentApiService;
    private Uri selectedFileUri;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            studentId = getArguments().getString("studentId");
            Log.d(TAG, "Student ID retrieved at 03:48 PM IST, Friday, June 13, 2025: " + (studentId != null ? studentId : "null"));
        } else {
            Log.e(TAG, "Arguments bundle is null at 03:48 PM IST, Friday, June 13, 2025");
        }
        studentApiService = RetrofitClient.getStudentApiService();
        leaveRequestList = new ArrayList<>();

        // Initialize file picker launcher
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                selectedFileUri = result.getData().getData();
                if (selectedFileUri != null) {
                    String fileName = selectedFileUri.getLastPathSegment();
                    proofFileName.setText(fileName != null ? fileName : "File selected");
                } else {
                    proofFileName.setText("No file selected (Optional for Reference)");
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_student_leave_management, container, false);

        TextView studentIdDisplay = root.findViewById(R.id.student_id_display);
        leaveFromLayout = root.findViewById(R.id.leave_from_layout);
        leaveToLayout = root.findViewById(R.id.leave_to_layout);
        leaveFromInput = root.findViewById(R.id.leave_from_input);
        leaveToInput = root.findViewById(R.id.leave_to_input);
        reasonInput = root.findViewById(R.id.reason_input);
        proofFileName = root.findViewById(R.id.proof_file_name);
        Button uploadProofButton = root.findViewById(R.id.upload_proof_button);
        Button submitLeaveButton = root.findViewById(R.id.submit_leave_button);
        leaveRequestsRecyclerView = root.findViewById(R.id.leave_requests_recycler_view);
        noLeaveRequestsText = root.findViewById(R.id.no_leave_requests_text);

        if (studentIdDisplay != null && studentId != null) {
            studentIdDisplay.setText("Leave Management - Student ID: " + studentId);
        } else {
            if (studentIdDisplay == null) {
                Log.e(TAG, "studentIdDisplay TextView is null at 03:48 PM IST, Friday, June 13, 2025");
            }
            if (studentId == null) {
                Log.e(TAG, "studentId is null when setting studentIdDisplay at 03:48 PM IST, Friday, June 13, 2025");
            }
        }

        // Setup RecyclerView
        leaveRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        leaveRequestAdapter = new LeaveRequestAdapter(leaveRequestList);
        leaveRequestsRecyclerView.setAdapter(leaveRequestAdapter);

        // Setup Date Pickers
        leaveFromInput.setOnClickListener(v -> showDatePickerDialog(leaveFromInput));
        leaveToInput.setOnClickListener(v -> showDatePickerDialog(leaveToInput));
        leaveFromLayout.setEndIconOnClickListener(v -> showDatePickerDialog(leaveFromInput));
        leaveToLayout.setEndIconOnClickListener(v -> showDatePickerDialog(leaveToInput));

        // Setup File Upload (Optional for Reference)
        uploadProofButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // Allow all file types; you can restrict to "application/pdf" or "image/*" if needed
            filePickerLauncher.launch(intent);
        });

        // Fetch leave history
        if (studentId != null && !studentId.isEmpty()) {
            Log.d(TAG, "Initiating fetchLeaveHistory with studentId: " + studentId + " at 03:48 PM IST, Friday, June 13, 2025");
            fetchLeaveHistory();
        } else {
            Log.e(TAG, "Student ID is null or empty at 03:48 PM IST, Friday, June 13, 2025");
            showErrorDialog("Error", "Student ID not found");
        }

        // Submit leave request
        submitLeaveButton.setOnClickListener(v -> {
            String leaveFrom = leaveFromInput.getText().toString().trim();
            String leaveTo = leaveToInput.getText().toString().trim();
            String reason = reasonInput.getText().toString().trim();

            // Validate required fields
            if (leaveFrom.isEmpty() || leaveTo.isEmpty() || reason.isEmpty()) {
                showErrorDialog("Validation Error", "Leave From, Leave To, and Reason are required");
                return;
            }

            // Prepare JSON data
            StudentLeaveRequest leaveRequest = new StudentLeaveRequest(studentId, leaveFrom, leaveTo, reason, null);
            String jsonData = new Gson().toJson(leaveRequest);
            submitLeaveRequest(jsonData, selectedFileUri);
        });

        return root;
    }

    private void showDatePickerDialog(TextInputEditText target) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    target.setText(date);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void fetchLeaveHistory() {
        Log.d(TAG, "Fetching leave history for studentId: " + studentId + " at 03:48 PM IST, Friday, June 13, 2025");
        Call<StudentLeaveRequest> call = studentApiService.getLeaveHistory(studentId);
        Log.d(TAG, "API call initiated: GET api/teacher/leave_request_api.php?student_id=" + studentId + " at 03:48 PM IST, Friday, June 13, 2025");

        call.enqueue(new Callback<StudentLeaveRequest>() {
            @Override
            public void onResponse(Call<StudentLeaveRequest> call, Response<StudentLeaveRequest> response) {
                Log.d(TAG, "Received response for leave history fetch at 03:48 PM IST, Friday, June 13, 2025 - HTTP Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    StudentLeaveRequest leaveResponse = response.body();
                    Log.d(TAG, "Response body: " + new Gson().toJson(leaveResponse) + " at 03:48 PM IST, Friday, June 13, 2025");
                    leaveRequestList.clear();
                    if (leaveResponse.getLeave_requests() != null) {
                        leaveRequestList.addAll(Arrays.asList(leaveResponse.getLeave_requests()));
                        Log.d(TAG, "Leave requests fetched: " + leaveRequestList.size() + " at 03:48 PM IST, Friday, June 13, 2025");
                    } else {
                        Log.w(TAG, "leave_requests field is null in response at 03:48 PM IST, Friday, June 13, 2025");
                    }
                    leaveRequestAdapter.notifyDataSetChanged();
                    noLeaveRequestsText.setVisibility(leaveRequestList.isEmpty() ? View.VISIBLE : View.GONE);
                    leaveRequestsRecyclerView.setVisibility(leaveRequestList.isEmpty() ? View.GONE : View.VISIBLE);
                    Log.d(TAG, "Updated UI with " + leaveRequestList.size() + " leave requests at 03:48 PM IST, Friday, June 13, 2025");
                } else {
                    String rawResponse = "Unknown response";
                    try {
                        if (response.errorBody() != null) {
                            rawResponse = response.errorBody().string();
                        } else if (response.raw().body() != null) {
                            rawResponse = response.raw().body().toString();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading raw response at 03:48 PM IST, Friday, June 13, 2025: ", e);
                    }
                    String errorMessage = response.body() != null && response.body().getMessage() != null ?
                            response.body().getMessage() : "Error fetching leave history: " + response.message();
                    Log.e(TAG, "Failed to fetch leave history at 03:48 PM IST, Friday, June 13, 2025 - HTTP Code: " + response.code() + ", Raw Response: " + rawResponse);
                    showErrorDialog("Error", errorMessage);
                }
            }

            @Override
            public void onFailure(Call<StudentLeaveRequest> call, Throwable t) {
                Log.e(TAG, "Network error fetching leave history at 03:48 PM IST, Friday, June 13, 2025: ", t);
                showErrorDialog("Network Error", "Network error: " + t.getMessage());
            }
        });
    }

    private void submitLeaveRequest(String jsonData, Uri fileUri) {
        // Prepare the data part with application/json media type
        RequestBody dataPart = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), jsonData);

        // Prepare the file part (optional for reference)
        MultipartBody.Part filePart = null;
        if (fileUri != null) {
            try {
                File file = new File(requireContext().getCacheDir(), "temp_file");
                // Note: For simplicity, we're assuming the file can be accessed directly.
                // In a production app, you'd need to copy the file from the Uri to a temp file using ContentResolver.
                String fileName = fileUri.getLastPathSegment();
                filePart = MultipartBody.Part.createFormData("proof", fileName, RequestBody.create(MediaType.parse("application/octet-stream"), file));
            } catch (Exception e) {
                Log.e(TAG, "Error preparing file for upload at 03:48 PM IST, Friday, June 13, 2025: ", e);
                showErrorDialog("Error", "Failed to prepare file for upload");
                return;
            }
        }

        Call<StudentLeaveRequest> call = studentApiService.submitLeaveRequest(dataPart, filePart);
        call.enqueue(new Callback<StudentLeaveRequest>() {
            @Override
            public void onResponse(Call<StudentLeaveRequest> call, Response<StudentLeaveRequest> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StudentLeaveRequest leaveResponse = response.body();
                    DialogUtils.showSuccessDialog(
                            requireContext(),
                            "Leave Request Submitted",
                            leaveResponse.getMessage(),
                            () -> {
                                // Clear the form
                                leaveFromInput.setText("");
                                leaveToInput.setText("");
                                reasonInput.setText("");
                                proofFileName.setText("No file selected (Optional for Reference)");
                                selectedFileUri = null;
                                // Refresh leave history
                                fetchLeaveHistory();
                            }
                    );
                    Log.d(TAG, "Leave request submitted successfully at 03:48 PM IST, Friday, June 13, 2025: " + leaveResponse.getMessage());
                } else {
                    String rawResponse = "Unknown response";
                    try {
                        if (response.errorBody() != null) {
                            rawResponse = response.errorBody().string();
                        } else if (response.raw().body() != null) {
                            rawResponse = response.raw().body().toString();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading raw response at 03:48 PM IST, Friday, June 13, 2025: ", e);
                    }
                    String errorMessage = response.body() != null && response.body().getMessage() != null ?
                            response.body().getMessage() : "Error submitting leave request: " + response.message();
                    Log.e(TAG, "Failed to submit leave request at 03:48 PM IST, Friday, June 13, 2025 - HTTP Code: " + response.code() + ", Raw Response: " + rawResponse);
                    showErrorDialog("Error", errorMessage);
                }
            }

            @Override
            public void onFailure(Call<StudentLeaveRequest> call, Throwable t) {
                Log.e(TAG, "Network error submitting leave request at 03:48 PM IST, Friday, June 13, 2025: ", t);
                showErrorDialog("Network Error", "Network error: " + t.getMessage());
            }
        });
    }

    private void showErrorDialog(String title, String message) {
        if (getActivity() == null || !isAdded()) {
            Log.e(TAG, "Cannot show dialog at 03:48 PM IST, Friday, June 13, 2025: Fragment not attached to activity");
            return;
        }
        Dialog errorDialog = new Dialog(requireContext());
        errorDialog.setContentView(R.layout.dialog_error_message);
        errorDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        errorDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView errorTitle = errorDialog.findViewById(R.id.error_title);
        TextView errorMessage = errorDialog.findViewById(R.id.error_message);
        Button errorOkButton = errorDialog.findViewById(R.id.error_ok_button);

        errorTitle.setText(title);
        errorMessage.setText(message);

        errorOkButton.setOnClickListener(v -> errorDialog.dismiss());
        errorDialog.show();
    }
}