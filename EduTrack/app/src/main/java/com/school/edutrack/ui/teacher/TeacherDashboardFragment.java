package com.school.edutrack.ui.teacher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.Announcement;
import com.school.edutrack.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherDashboardFragment extends Fragment {

    private static final String TAG = "TeacherDashboardFragment";
    private TextView greetingTextView;
    private RecyclerView dashboardRecyclerView;
    private SharedPreferences sharedPreferences;
    private String teacherId;
    private NavController navController;
    private DashboardAdapter adapter;
    private int lastKnownAnnouncementCount = 0;
    private boolean announcementsSeen = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teacher_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("EduTrackPrefs", Context.MODE_PRIVATE);

        // Retrieve teacherId from arguments or SharedPreferences
        teacherId = sharedPreferences.getString("teacher_id", "");
        if (teacherId.isEmpty()) {
            // Fallback to arguments if SharedPreferences is empty
            if (getArguments() != null) {
                teacherId = getArguments().getString("teacherId", "");
            }
            if (teacherId.isEmpty()) {
                Toast.makeText(getContext(), "Error: Teacher ID not found", Toast.LENGTH_LONG).show();
                logout();
                return;
            }
        }

        // Load saved state
        lastKnownAnnouncementCount = sharedPreferences.getInt("last_announcement_count", 0);
        announcementsSeen = sharedPreferences.getBoolean("announcements_seen", false);

        // Initialize Views
//        greetingTextView = view.findViewById(R.id.greetingTextView);
//        dashboardRecyclerView = view.findViewById(R.id.dashboardRecyclerView);

        // Null checks
        if (greetingTextView == null || dashboardRecyclerView == null) {
            Toast.makeText(getContext(), "Error: UI components not found", Toast.LENGTH_LONG).show();
            return;
        }

        // Set Teacher Info
        String teacherName = sharedPreferences.getString("user_name", "Teacher");
        greetingTextView.setText("Welcome, " + teacherName + "!");

        // Setup RecyclerView
        setupDashboardRecyclerView();

        // Initialize NavController
        navController = Navigation.findNavController(view);

        // Fetch announcement count
        fetchAnnouncementCount();
    }

    private void setupDashboardRecyclerView() {
        // Data for dashboard cards
        List<DashboardItem> dashboardItems = new ArrayList<>();
        dashboardItems.add(new DashboardItem("Attendance", "Manage student attendance", R.drawable.ic_assignment));
        dashboardItems.add(new DashboardItem("Students", "View student details", R.drawable.ic_people));
        dashboardItems.add(new DashboardItem("Assignments", "Assign homework", R.drawable.ic_assignment));
        dashboardItems.add(new DashboardItem("Announcements", "View updates", R.drawable.ic_announcement));

        adapter = new DashboardAdapter(dashboardItems);
        dashboardRecyclerView.setAdapter(adapter);

        // Apply initial seen state
        adapter.updateAnnouncementCount(lastKnownAnnouncementCount);
        adapter.setAnnouncementsSeen(announcementsSeen);
    }

    private void fetchAnnouncementCount() {
        RetrofitClient.getApiService().getAnnouncements().enqueue(new Callback<List<Announcement>>() {
            @Override
            public void onResponse(Call<List<Announcement>> call, Response<List<Announcement>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Announcement> allAnnouncements = response.body();
                    int teacherAnnouncementCount = 0;

                    // Filter announcements for teachers
                    for (Announcement announcement : allAnnouncements) {
                        if ("teacher".equalsIgnoreCase(announcement.getRole()) || "all".equalsIgnoreCase(announcement.getRole())) {
                            teacherAnnouncementCount++;
                        }
                    }

                    // If the count has increased, reset the seen state
                    if (teacherAnnouncementCount > lastKnownAnnouncementCount) {
                        announcementsSeen = false;
                    }

                    // Save the new count and seen state
                    lastKnownAnnouncementCount = teacherAnnouncementCount;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("last_announcement_count", lastKnownAnnouncementCount);
                    editor.putBoolean("announcements_seen", announcementsSeen);
                    editor.apply();

                    // Update the Announcements card
                    final int finalCount = teacherAnnouncementCount;
                    requireActivity().runOnUiThread(() -> {
                        adapter.updateAnnouncementCount(finalCount);
                        adapter.setAnnouncementsSeen(announcementsSeen);
                    });

                    Log.d(TAG, "Announcements fetched on 2025-06-05 11:12 AM IST: " + teacherAnnouncementCount + " announcements for teachers.");
                } else {
                    String errorMsg = "Failed to fetch announcements: HTTP " + response.code();
                    Log.e(TAG, errorMsg);
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                        adapter.updateAnnouncementCount(0);
                        adapter.setAnnouncementsSeen(true);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Announcement>> call, Throwable t) {
                String errorMsg = "Fetch error: " + t.getMessage();
                Log.e(TAG, errorMsg);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    adapter.updateAnnouncementCount(0);
                    adapter.setAnnouncementsSeen(true);
                });
            }
        });
    }

    private void logout() {
        // Clear SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Navigate to Login Screen
        try {
            navController.navigate(R.id.action_teacherdashboard_to_teacherLogin);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Navigation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Data model for dashboard items
    private static class DashboardItem {
        private String title;
        private String description;
        private int iconResId;

        DashboardItem(String title, String description, int iconResId) {
            this.title = title;
            this.description = description;
            this.iconResId = iconResId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public int getIconResId() {
            return iconResId;
        }
    }

    // Adapter for RecyclerView
    private class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {
        private List<DashboardItem> items;
        private int announcementCount = 0;
        private boolean announcementsSeen = false;

        DashboardAdapter(List<DashboardItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.teacher_dashboard_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DashboardItem item = items.get(position);
            holder.titleTextView.setText(item.getTitle());
            holder.descriptionTextView.setText(item.getDescription());
            holder.iconView.setImageResource(item.getIconResId());

            // Special handling for Announcements card
            if ("Announcements".equals(item.getTitle())) {
                if (announcementCount > 0 && !announcementsSeen) {
                    holder.announcementsCountTextView.setVisibility(View.VISIBLE);
                    holder.announcementsCountTextView.setText(String.valueOf(announcementCount));
                } else {
                    holder.announcementsCountTextView.setVisibility(View.GONE);
                }
                holder.descriptionTextView.setText("View updates");

                // Set click listener to navigate to TeacherAnnouncementsFragment
                holder.cardView.setOnClickListener(v -> {
                    announcementsSeen = true;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("announcements_seen", true);
                    editor.apply();
                    notifyItemChanged(position); // Update the card to hide the badge

                    Bundle args = new Bundle();
                    args.putString("teacherId", teacherId);
                    try {
                        navController.navigate(R.id.action_teacherDashboard_to_announcements, args);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Navigation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                holder.announcementsCountTextView.setVisibility(View.GONE);
            }

            // Set click listeners for other cards (if needed)
            if ("Attendance".equals(item.getTitle())) {
                holder.cardView.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putString("teacherId", teacherId);
                    navController.navigate(R.id.action_teacherDashboard_to_attendanceManagement, args);
                });
            } else if ("Students".equals(item.getTitle())) {
                holder.cardView.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putString("teacherId", teacherId);
                    navController.navigate(R.id.action_teacherDashboard_to_viewStudents, args);
                });
            } else if ("Assignments".equals(item.getTitle())) {
                holder.cardView.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putString("teacherId", teacherId);
                    navController.navigate(R.id.action_teacherDashboard_to_assignments, args);
                });
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        void updateAnnouncementCount(int count) {
            this.announcementCount = count;
            notifyItemChanged(getAnnouncementsIndex());
        }

        void setAnnouncementsSeen(boolean seen) {
            this.announcementsSeen = seen;
            notifyItemChanged(getAnnouncementsIndex());
        }

        private int getAnnouncementsIndex() {
            int announcementsIndex = -1;
            for (int i = 0; i < items.size(); i++) {
                if ("Announcements".equals(items.get(i).getTitle())) {
                    announcementsIndex = i;
                    break;
                }
            }
            return announcementsIndex;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            ImageView iconView;
            TextView titleTextView;
            TextView descriptionTextView;
            TextView announcementsCountTextView;

            ViewHolder(View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.teacherCardView);
                iconView = itemView.findViewById(R.id.cardIconImageView);
                titleTextView = itemView.findViewById(R.id.teacherCardTitleTextView);
                descriptionTextView = itemView.findViewById(R.id.teacherCardDescriptionTextView);
                announcementsCountTextView = itemView.findViewById(R.id.announcementCountTextView);
            }
        }
    }
}