package com.school.edutrack.ui.student;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.school.edutrack.R;
import com.school.edutrack.model.Announcement;
import com.school.edutrack.model.LearningMaterial;
import com.school.edutrack.model.LearningMaterialsResponse;
import com.school.edutrack.model.StudentQuizModels;
import com.school.edutrack.network.RetrofitClient;
import com.school.edutrack.utils.DialogUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentDashboardFragment extends Fragment {

    private static final String TAG = "StudentDashboardFragment";
    private String studentId;
    private String studentClass, studentSection;
    private SharedPreferences sharedPreferences;
    private NavController navController;
    private View rootView;
    private LinearLayout notificationContainer;
    private LinearLayout assignmentsContainer;
    private TextView notificationCount, newAnnouncementsText, assignmentsText, noAssignmentsMessage;
    private Button retryAssignmentsButton;
    private int lastKnownAnnouncementCount = 0;
    private boolean announcementsSeen = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences("EduTrackPrefs", Context.MODE_PRIVATE);

        if (getArguments() != null) {
            studentId = getArguments().getString("studentId");
            Log.d(TAG, "Student ID retrieved from arguments on 2025-06-28 12:10 PM IST: " + studentId);
        }

        if (studentId == null || studentId.isEmpty()) {
            studentId = sharedPreferences.getString("student_id", null);
            Log.w(TAG, "Student ID not found in arguments, retrieved from SharedPreferences on 2025-06-28 12:10 PM IST: " + studentId);
        }

        lastKnownAnnouncementCount = sharedPreferences.getInt("last_student_announcement_count", 0);
        announcementsSeen = sharedPreferences.getBoolean("student_announcements_seen", false);
        studentClass = sharedPreferences.getString("student_class", null);
        studentSection = sharedPreferences.getString("student_section", null);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_student_dashboard, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView welcomeMessage = view.findViewById(R.id.welcome_message);
        TextView studentIdDisplay = view.findViewById(R.id.student_id_display);
        newAnnouncementsText = view.findViewById(R.id.new_announcements_text);
        assignmentsText = view.findViewById(R.id.assignments_title);
        assignmentsContainer = view.findViewById(R.id.assignments_container);
        noAssignmentsMessage = view.findViewById(R.id.no_assignments_message);
        retryAssignmentsButton = view.findViewById(R.id.retry_assignments_button);
        TextView feePaymentText = view.findViewById(R.id.fee_payment_text);
        TextView resultsText = view.findViewById(R.id.results_text);
        notificationContainer = view.findViewById(R.id.notification_container);
        notificationCount = view.findViewById(R.id.notification_count);
        CardView newAnnouncementsCard = view.findViewById(R.id.new_announcements_card);
        CardView assignmentsCard = view.findViewById(R.id.assignments_card);
        CardView feePaymentCard = view.findViewById(R.id.fee_payment_card);
        CardView resultsCard = view.findViewById(R.id.results_card);
        Button viewAllAssignmentsButton = view.findViewById(R.id.view_all_assignments_button);
        FloatingActionButton fabAlerts = view.findViewById(R.id.fab_alerts);

        navController = Navigation.findNavController(view);

        if (studentId == null || studentId.isEmpty()) {
            Log.e(TAG, "Student ID not found on 2025-06-28 12:10 PM IST, navigating to login");
            Snackbar.make(view, "Error: Student ID not found, please log in again", Snackbar.LENGTH_LONG).show();
            logout();
            return;
        }

        welcomeMessage.setText("Welcome, Student " + studentId + "!");
        studentIdDisplay.setText("Student ID: " + studentId);

        // Apply animations
        applyCardAnimation(newAnnouncementsCard, 0);
        applyCardAnimation(assignmentsCard, 200);
        applyCardAnimation(feePaymentCard, 400);
        applyCardAnimation(resultsCard, 600);

        ObjectAnimator fabBounce = ObjectAnimator.ofFloat(fabAlerts, "translationY", 100f, 0f);
        fabBounce.setDuration(1000);
        fabBounce.setInterpolator(new BounceInterpolator());
        fabBounce.start();

        // Set click listeners
        fabAlerts.setOnClickListener(v -> {
            String timestamp = "2025-06-28 12:10 PM IST";
            Log.d(TAG, "FAB clicked at " + timestamp);
            showQrCodeDialog(studentId);
        });

        newAnnouncementsCard.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("studentId", studentId);
            try {
                navController.navigate(R.id.action_studentDashboard_to_announcements, bundle);
            } catch (Exception e) {
                Snackbar.make(view, "Navigation error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

        viewAllAssignmentsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("studentId", studentId);
            try {
                navController.navigate(R.id.action_studentDashboard_to_assignments, bundle);
            } catch (Exception e) {
                Snackbar.make(view, "Navigation error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

        resultsCard.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("studentId", studentId);
            try {
                navController.navigate(R.id.action_studentDashboard_to_examMarks, bundle);
            } catch (Exception e) {
                Snackbar.make(view, "Navigation error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

        feePaymentCard.setOnClickListener(v -> {
            Snackbar.make(view, "Fee Payment feature coming soon!", Snackbar.LENGTH_SHORT).show();
        });

        welcomeMessage.setOnLongClickListener(v -> {
            logout();
            return true;
        });

        retryAssignmentsButton.setOnClickListener(v -> fetchStudentDetails());

        // Fetch data
        fetchStudentDetails();
        fetchAnnouncements();
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
                    Log.d(TAG, "Fetched class: " + studentClass + ", section: " + studentSection);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("student_class", studentClass);
                    editor.putString("student_section", studentSection);
                    editor.apply();
                    fetchAssignments();
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

    private void fetchAssignments() {
        if (studentClass == null || studentSection == null) {
            Log.e(TAG, "Class or section is null, cannot fetch assignments");
            showRetryButton(true);
            return;
        }

        DialogUtils.showLoadingDialog(requireContext());
        RetrofitClient.getStudentApiService().getStudentLearningMaterials(studentId, studentClass, studentSection).enqueue(new Callback<LearningMaterialsResponse>() {
            @Override
            public void onResponse(Call<LearningMaterialsResponse> call, Response<LearningMaterialsResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && response.body().getStatus().equals("success")) {
                    List<LearningMaterial> assignments = new ArrayList<>();
                    for (LearningMaterial material : response.body().getData()) {
                        if ("Assignment".equalsIgnoreCase(material.getType()) && material.getDue_date() != null) {
                            assignments.add(material);
                        }
                    }
                    Log.d(TAG, "Fetched " + assignments.size() + " assignments");
                    updateAssignmentsGlimpse(assignments);
                    showRetryButton(false);
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Failed to fetch assignments: " + error);
                        DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to fetch assignments: " + error, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                    showRetryButton(true);
                }
            }

            @Override
            public void onFailure(Call<LearningMaterialsResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                Log.e(TAG, "Network error fetching assignments: " + t.getMessage());
                DialogUtils.showFailureDialog(requireContext(), "Network Error", "Failed to fetch assignments: " + t.getMessage(), null);
                showRetryButton(true);
            }
        });
    }

    private void updateAssignmentsGlimpse(List<LearningMaterial> assignments) {
        assignmentsContainer.removeAllViews();
        assignmentsText.setText(assignments.size() + " pending assignments");
        if (assignments.isEmpty()) {
            noAssignmentsMessage.setVisibility(View.VISIBLE);
            assignmentsContainer.setVisibility(View.GONE);
            return;
        }

        noAssignmentsMessage.setVisibility(View.GONE);
        assignmentsContainer.setVisibility(View.VISIBLE);

        // Show up to 3 recent assignments
        int count = Math.min(assignments.size(), 3);
        for (int i = 0; i < count; i++) {
            LearningMaterial assignment = assignments.get(i);
            View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_assignment_glimpse, assignmentsContainer, false);
            TextView nameView = itemView.findViewById(R.id.assignment_name);
            TextView subjectView = itemView.findViewById(R.id.assignment_subject);
            TextView dueDateView = itemView.findViewById(R.id.assignment_due_date);

            nameView.setText(assignment.getName());
            subjectView.setText(assignment.getSubject());
            dueDateView.setText("Due: " + assignment.getDue_date());

            // Apply animation
            AnimationSet animationSet = new AnimationSet(true);
            AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
            fadeIn.setDuration(500);
            fadeIn.setStartOffset(i * 100L);
            TranslateAnimation slideIn = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, -1f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f
            );
            slideIn.setDuration(500);
            slideIn.setStartOffset(i * 100L);
            animationSet.addAnimation(fadeIn);
            animationSet.addAnimation(slideIn);
            itemView.startAnimation(animationSet);

            assignmentsContainer.addView(itemView);
        }
    }

    private void showRetryButton(boolean show) {
        retryAssignmentsButton.setVisibility(show ? View.VISIBLE : View.GONE);
        noAssignmentsMessage.setVisibility(show || assignmentsContainer.getChildCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void fetchAnnouncements() {
        Log.d(TAG, "Fetching announcements on 2025-06-28 12:10 PM IST");
        RetrofitClient.getApiService().getAnnouncements().enqueue(new Callback<List<Announcement>>() {
            @Override
            public void onResponse(Call<List<Announcement>> call, Response<List<Announcement>> response) {
                Log.d(TAG, "Received response for announcements on 2025-06-28 12:10 PM IST, success: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    List<Announcement> allAnnouncements = response.body();
                    List<Announcement> studentAnnouncements = new ArrayList<>();

                    for (Announcement announcement : allAnnouncements) {
                        if ("student".equalsIgnoreCase(announcement.getRole()) || "all".equalsIgnoreCase(announcement.getRole())) {
                            studentAnnouncements.add(announcement);
                        }
                    }

                    int studentAnnouncementCount = studentAnnouncements.size();
                    Log.d(TAG, "Filtered " + studentAnnouncementCount + " announcements for students");

                    if (studentAnnouncementCount > lastKnownAnnouncementCount) {
                        announcementsSeen = false;
                        Log.d(TAG, "New announcements detected, resetting seen state");
                    }

                    lastKnownAnnouncementCount = studentAnnouncementCount;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("last_student_announcement_count", lastKnownAnnouncementCount);
                    editor.putBoolean("student_announcements_seen", announcementsSeen);
                    editor.apply();

                    requireActivity().runOnUiThread(() -> {
                        Log.d(TAG, "Updating UI with announcements");
                        updateAnnouncementsCard(studentAnnouncements);
                        updateNotificationBadge();
                    });

                    Log.d(TAG, "Announcements fetched on 2025-06-28 12:10 PM IST: " + studentAnnouncementCount + " announcements for students.");
                } else {
                    String errorMsg = "Failed to fetch announcements: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ", " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading errorBody on 2025-06-28 12:10 PM IST", e);
                        }
                    }
                    Log.e(TAG, errorMsg);
                    requireActivity().runOnUiThread(() -> {
                        newAnnouncementsText.setText("Failed to load announcements");
                        notificationContainer.setVisibility(View.GONE);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Announcement>> call, Throwable t) {
                String errorMsg = "Fetch error: " + t.getMessage();
                Log.e(TAG, errorMsg + " on 2025-06-28 12:10 PM IST");
                requireActivity().runOnUiThread(() -> {
                    newAnnouncementsText.setText("Error loading announcements");
                    notificationContainer.setVisibility(View.GONE);
                });
            }
        });
    }

    private void updateAnnouncementsCard(List<Announcement> announcements) {
        if (announcements.isEmpty()) {
            newAnnouncementsText.setText("No new announcements.");
            Log.d(TAG, "No announcements to display");
        } else {
            Announcement latestAnnouncement = announcements.get(announcements.size() - 1);
            String title = latestAnnouncement.getTitle() != null ? latestAnnouncement.getTitle() : "Untitled";
            newAnnouncementsText.setText(title);
            Log.d(TAG, "Updated announcements card with latest title: " + title);
        }
    }

    private void updateNotificationBadge() {
        if (lastKnownAnnouncementCount > 0 && !announcementsSeen) {
            notificationCount.setText(String.valueOf(lastKnownAnnouncementCount));
            notificationContainer.setVisibility(View.VISIBLE);
            Log.d(TAG, "Showing notification badge with count: " + lastKnownAnnouncementCount);
        } else {
            notificationContainer.setVisibility(View.GONE);
            Log.d(TAG, "Hiding notification badge");
        }
    }

    private void showQrCodeDialog(String studentId) {
        if (studentId == null || studentId.isEmpty()) {
            Log.e(TAG, "Student ID is null or empty, cannot generate QR code");
            Snackbar.make(rootView, "Error: Student ID not available", Snackbar.LENGTH_LONG).show();
            return;
        }

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_qr_code);
        dialog.setCancelable(true);

        ImageView qrCodeImageView = dialog.findViewById(R.id.qr_code_image);
        Button closeButton = dialog.findViewById(R.id.close_button);

        // Generate blue QR code
        Bitmap qrCodeBitmap = generateBlueQrCode(studentId);
        if (qrCodeBitmap != null) {
            qrCodeImageView.setImageBitmap(qrCodeBitmap);
            Log.d(TAG, "QR code generated and set for studentId: " + studentId);
        } else {
            Log.e(TAG, "Failed to generate QR code for studentId: " + studentId);
            qrCodeImageView.setImageDrawable(null);
            Snackbar.make(rootView, "Failed to generate QR code", Snackbar.LENGTH_SHORT).show();
        }

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private Bitmap generateBlueQrCode(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 600, 600);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLUE : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            Log.e(TAG, "Failed to generate QR code: " + e.getMessage(), e);
            return null;
        }
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.student_nav_graph, true)
                .build();
        try {
            navController.navigate(R.id.action_studentDashboard_to_login, null, navOptions);
        } catch (Exception e) {
            Snackbar.make(rootView, "Navigation error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void applyCardAnimation(View card, long delay) {
        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(500);
        fadeIn.setStartOffset(delay);
        TranslateAnimation slideIn = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, -1f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f
        );
        slideIn.setDuration(500);
        slideIn.setStartOffset(delay);
        animationSet.addAnimation(fadeIn);
        animationSet.addAnimation(slideIn);
        card.startAnimation(animationSet);
    }
}