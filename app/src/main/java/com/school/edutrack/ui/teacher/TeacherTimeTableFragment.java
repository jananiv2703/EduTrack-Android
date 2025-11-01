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
import com.google.android.material.tabs.TabLayout;
import com.school.edutrack.R;
import com.school.edutrack.model.Timetable;
import com.school.edutrack.model.TimetableResponse;
import com.school.edutrack.network.ApiService;
import com.school.edutrack.network.RetrofitClient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherTimeTableFragment extends Fragment {

    private static final String TAG = "TeacherTimeTableFragment";

    private String teacherId;
    private TextView teacherIdDisplay;
    private RecyclerView timetableRecyclerView;
    private View noEntriesLayout;
    private TextView noEntriesText;
    private ProgressBar loadingSpinner;
    private TabLayout daysTabLayout;
    private ApiService apiService;
    private List<Timetable> allTimetableEntries;
    private TimetableAdapter timetableAdapter;
    private final String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        logWithTimestamp("Creating view for TeacherTimeTableFragment");
        return inflater.inflate(R.layout.fragment_teacher_time_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logWithTimestamp("View created, initializing components");

        // Initialize UI components
        teacherIdDisplay = view.findViewById(R.id.teacher_id_display);
        timetableRecyclerView = view.findViewById(R.id.timetable_recycler_view);
        noEntriesLayout = view.findViewById(R.id.no_entries_layout);
        noEntriesText = view.findViewById(R.id.no_entries_text);
        loadingSpinner = view.findViewById(R.id.loading_spinner);
        daysTabLayout = view.findViewById(R.id.days_tab_layout);

        // Initialize Retrofit API service
        apiService = RetrofitClient.getApiService();
        logWithTimestamp("Retrofit API service initialized");

        // Setup RecyclerView
        timetableRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        timetableAdapter = new TimetableAdapter();
        timetableRecyclerView.setAdapter(timetableAdapter);

        // Populate TabLayout with days
        for (String day : daysOfWeek) {
            daysTabLayout.addTab(daysTabLayout.newTab().setText(day));
        }

        // Initialize timetable entries list
        allTimetableEntries = new ArrayList<>();

        // Retrieve teacherId from arguments
        if (getArguments() != null) {
            teacherId = getArguments().getString("teacherId");
        }

        // Display teacherId
        if (teacherId != null) {
            teacherIdDisplay.setText("Teacher ID: " + teacherId);
            fetchTimetable();
        } else {
            teacherIdDisplay.setText("Teacher ID: Not provided");
            noEntriesText.setText("Error: Teacher ID is required to fetch timetable.");
            noEntriesLayout.setVisibility(View.VISIBLE);
            timetableRecyclerView.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Teacher ID not provided", Toast.LENGTH_SHORT).show();
        }

        // Set up tab selection listener
        daysTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedDay = daysOfWeek[tab.getPosition()];
                displayTimetableForDay(selectedDay);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // No action needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                String selectedDay = daysOfWeek[tab.getPosition()];
                displayTimetableForDay(selectedDay);
            }
        });
    }

    private void fetchTimetable() {
        logWithTimestamp("Fetching timetable for teacherId: " + teacherId);

        loadingSpinner.setVisibility(View.VISIBLE);
        timetableRecyclerView.setVisibility(View.GONE);
        noEntriesLayout.setVisibility(View.GONE);

        Call<TimetableResponse> call = apiService.getTimetablesByTeacher(teacherId);
        call.enqueue(new Callback<TimetableResponse>() {
            @Override
            public void onResponse(Call<TimetableResponse> call, Response<TimetableResponse> response) {
                loadingSpinner.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    allTimetableEntries = response.body().getTimetable();
                    logWithTimestamp("Timetable fetched successfully: " + allTimetableEntries.size() + " entries");

                    if (allTimetableEntries.isEmpty()) {
                        noEntriesText.setText("No timetable entries found for this teacher.");
                        noEntriesLayout.setVisibility(View.VISIBLE);
                        timetableRecyclerView.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No timetable entries found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Display timetable for the first day (Monday) by default
                    displayTimetableForDay(daysOfWeek[0]);
                } else {
                    String errorBody = "No error body";
                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, getTimestamp() + " - Error reading error body: " + e.getMessage(), e);
                        }
                    }
                    Log.e(TAG, getTimestamp() + " - Failed to fetch timetable. Response code: " + response.code() + ", Error: " + errorBody);
                    noEntriesText.setText("Failed to load timetable.");
                    noEntriesLayout.setVisibility(View.VISIBLE);
                    timetableRecyclerView.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to fetch timetable: " + errorBody, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<TimetableResponse> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                Log.e(TAG, getTimestamp() + " - Network error fetching timetable: " + t.getMessage(), t);
                noEntriesText.setText("Network error fetching timetable.");
                noEntriesLayout.setVisibility(View.VISIBLE);
                timetableRecyclerView.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTimetableForDay(String day) {
        logWithTimestamp("Displaying timetable for day: " + day);

        // Filter timetable entries for the selected day
        List<Timetable> dayEntries = new ArrayList<>();
        for (Timetable entry : allTimetableEntries) {
            if (entry.getDayOfWeek().equalsIgnoreCase(day)) {
                dayEntries.add(entry);
            }
        }

        // Update RecyclerView
        if (dayEntries.isEmpty()) {
            noEntriesText.setText("No timetable entries for " + day + ".");
            noEntriesLayout.setVisibility(View.VISIBLE);
            timetableRecyclerView.setVisibility(View.GONE);
        } else {
            noEntriesLayout.setVisibility(View.GONE);
            timetableRecyclerView.setVisibility(View.VISIBLE);
            timetableAdapter.setTimetableEntries(dayEntries);
        }
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'IST'", Locale.US);
        return sdf.format(new Date());
    }

    private void logWithTimestamp(String message) {
        Log.d(TAG, getTimestamp() + " - " + message);
    }
}