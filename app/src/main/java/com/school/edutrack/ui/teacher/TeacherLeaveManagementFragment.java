package com.school.edutrack.ui.teacher;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.LeaveRequest;
import com.school.edutrack.model.LeaveRequestResponse;
import com.school.edutrack.network.ApiService;
import com.school.edutrack.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TeacherLeaveManagementFragment extends Fragment implements LeaveRequestAdapter.OnStatusUpdateListener {

    private static final String TAG = "TeacherLeaveManagementFragment";

    private String teacherId;
    private TextView teacherIdDisplay;
    private RecyclerView leaveRequestsRecyclerView;
    private ProgressBar loadingSpinner;
    private LeaveRequestAdapter adapter;
    private ApiService apiService;
    private List<LeaveRequest> leaveRequests;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        logWithTimestamp("Creating view for TeacherLeaveManagementFragment");
        return inflater.inflate(R.layout.fragment_teacher_leave_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logWithTimestamp("View created, initializing components");

        // Initialize UI components
        teacherIdDisplay = view.findViewById(R.id.teacher_id_display);
        leaveRequestsRecyclerView = view.findViewById(R.id.leave_requests_recycler_view);
        loadingSpinner = view.findViewById(R.id.loading_spinner);

        // Setup RecyclerView
        leaveRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Retrofit API service
        apiService = RetrofitClient.getApiService();
        logWithTimestamp("Retrofit API service initialized");

        // Retrieve teacherId from arguments
        if (getArguments() != null) {
            teacherId = getArguments().getString("teacherId");
            logWithTimestamp("Teacher ID retrieved from arguments: " + teacherId);
        } else {
            teacherId = "T103"; // Fallback for testing
            Log.w(TAG, getTimestamp() + " - Teacher ID not found in arguments, using fallback: " + teacherId);
        }

        // Display teacherId
        if (teacherId != null) {
            teacherIdDisplay.setText("Teacher ID: " + teacherId);
        }

        // Fetch leave requests
        fetchLeaveRequests();
    }

    private void fetchLeaveRequests() {
        logWithTimestamp("Fetching leave requests for teacher_id: " + teacherId);

        loadingSpinner.setVisibility(View.VISIBLE);
        leaveRequestsRecyclerView.setVisibility(View.GONE);

        Call<LeaveRequestResponse> call = apiService.getLeaveRequests(teacherId);
        logWithTimestamp("Sending GET request to fetch leave requests: " +
                "http://10.0.2.2/edutrack-backend/api/teacher/leave_request_api.php?teacher_id=" + teacherId);

        call.enqueue(new Callback<LeaveRequestResponse>() {
            @Override
            public void onResponse(Call<LeaveRequestResponse> call, Response<LeaveRequestResponse> response) {
                loadingSpinner.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    leaveRequests = response.body().getLeaveRequests();
                    logWithTimestamp("GET request successful, received " + (leaveRequests != null ? leaveRequests.size() : 0) + " leave requests: " + leaveRequests);
                    if (leaveRequests == null || leaveRequests.isEmpty()) {
                        Log.w(TAG, getTimestamp() + " - No leave requests found for teacher_id: " + teacherId);
                        Toast.makeText(getContext(), "No leave requests found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Filter leave requests: only include those with non-null and non-empty leave_from, leave_to, and reason
                    List<LeaveRequest> filteredRequests = new ArrayList<>();
                    for (LeaveRequest request : leaveRequests) {
                        if (isValidLeaveRequest(request)) {
                            filteredRequests.add(request);
                        }
                    }
                    logWithTimestamp("Filtered leave requests: " + filteredRequests.size() + " out of " + leaveRequests.size());

                    if (filteredRequests.isEmpty()) {
                        Log.w(TAG, getTimestamp() + " - No valid leave requests after filtering for teacher_id: " + teacherId);
                        Toast.makeText(getContext(), "No valid leave requests found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Setup RecyclerView
                    adapter = new LeaveRequestAdapter(getContext(), filteredRequests, TeacherLeaveManagementFragment.this);
                    leaveRequestsRecyclerView.setAdapter(adapter);
                    leaveRequestsRecyclerView.setVisibility(View.VISIBLE);
                    logWithTimestamp("RecyclerView updated with filtered leave requests");
                } else {
                    String errorBodyString = "No error body";
                    if (response.errorBody() != null) {
                        try {
                            errorBodyString = response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, getTimestamp() + " - Error reading error body: " + e.getMessage(), e);
                        }
                    }
                    Log.e(TAG, getTimestamp() + " - Failed to fetch leave requests. Response code: " + response.code() +
                            ", Message: " + errorBodyString);
                    Toast.makeText(getContext(), "Failed to fetch leave requests: " + errorBodyString, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LeaveRequestResponse> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                Log.e(TAG, getTimestamp() + " - Network error fetching leave requests: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidLeaveRequest(LeaveRequest request) {
        return request.getLeaveFrom() != null && !request.getLeaveFrom().isEmpty() &&
                request.getLeaveTo() != null && !request.getLeaveTo().isEmpty() &&
                request.getReason() != null && !request.getReason().isEmpty();
    }

    @Override
    public void onStatusUpdate(int leaveRequestId, String newStatus) {
        logWithTimestamp("Updating leave request status for id: " + leaveRequestId + " to " + newStatus);

        loadingSpinner.setVisibility(View.VISIBLE);

        ApiService.LeaveRequestUpdate request = new ApiService.LeaveRequestUpdate(leaveRequestId, newStatus, teacherId);
        Call<LeaveRequestResponse> call = apiService.updateLeaveRequestStatus(request);
        logWithTimestamp("Sending PUT request to update leave request status: " +
                "http://10.0.2.2/edutrack-backend/api/teacher/leave_request_api.php with body: " + request.toString());

        call.enqueue(new Callback<LeaveRequestResponse>() {
            @Override
            public void onResponse(Call<LeaveRequestResponse> call, Response<LeaveRequestResponse> response) {
                loadingSpinner.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    logWithTimestamp("PUT request successful, response: " + response.body().getMessage());
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    // Refresh the leave requests list
                    fetchLeaveRequests();
                } else {
                    String errorBodyString = "No error body";
                    if (response.errorBody() != null) {
                        try {
                            errorBodyString = response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, getTimestamp() + " - Error reading error body: " + e.getMessage(), e);
                        }
                    }
                    Log.e(TAG, getTimestamp() + " - Failed to update leave request status. Response code: " + response.code() +
                            ", Message: " + errorBodyString);
                    Toast.makeText(getContext(), "Failed to update leave request: " + errorBodyString, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LeaveRequestResponse> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                Log.e(TAG, getTimestamp() + " - Network error updating leave request status: " + t.getMessage(), t);
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