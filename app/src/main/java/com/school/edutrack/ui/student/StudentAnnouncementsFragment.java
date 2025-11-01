package com.school.edutrack.ui.student;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.school.edutrack.R;
import com.school.edutrack.model.Announcement;
import com.school.edutrack.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentAnnouncementsFragment extends Fragment {

    private static final String TAG = "StudentAnnouncements";
    private static final String BASE_URL = "http://192.168.92.227/edutrack-backend/api/";
    private static final int ITEMS_PER_PAGE = 5;

    private String studentId;
    private LinearLayout announcementsContainer;
    private ProgressBar loadingSpinner;
    private TextView noAnnouncementsMessage;
    private Button loadMoreButton;
    private SharedPreferences sharedPreferences;

    private List<Announcement> allStudentAnnouncements = new ArrayList<>();
    private int currentPage = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            studentId = getArguments().getString("studentId");
        }
        sharedPreferences = requireActivity().getSharedPreferences("EduTrackPrefs", getContext().MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_announcements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Mark announcements as seen
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("student_announcements_seen", true);
        editor.apply();

        TextView studentIdDisplay = view.findViewById(R.id.student_id_display);
        announcementsContainer = view.findViewById(R.id.announcements_container);
        loadingSpinner = view.findViewById(R.id.loading_spinner);
        noAnnouncementsMessage = view.findViewById(R.id.no_announcements_message);
        loadMoreButton = view.findViewById(R.id.load_more_button);

        loadMoreButton.setOnClickListener(v -> loadMoreAnnouncements());

        if (studentIdDisplay != null && studentId != null) {
            studentIdDisplay.setText("Announcements - Student ID: " + studentId);
        } else {
            Log.w(TAG, "Student ID not found in arguments on 2025-06-09 11:43 AM IST");
            Toast.makeText(getContext(), "Error: Student ID not found", Toast.LENGTH_LONG).show();
        }

        fetchAnnouncements();
    }

    private void fetchAnnouncements() {
        requireActivity().runOnUiThread(() -> {
            loadingSpinner.setVisibility(View.VISIBLE);
            announcementsContainer.setVisibility(View.GONE);
            noAnnouncementsMessage.setVisibility(View.GONE);
            loadMoreButton.setVisibility(View.GONE);
        });

        RetrofitClient.getStudentApiService().getAnnouncements().enqueue(new Callback<List<Announcement>>() {
            @Override
            public void onResponse(Call<List<Announcement>> call, Response<List<Announcement>> response) {
                requireActivity().runOnUiThread(() -> loadingSpinner.setVisibility(View.GONE));

                if (response.isSuccessful() && response.body() != null) {
                    List<Announcement> allAnnouncements = response.body();
                    allStudentAnnouncements.clear();

                    for (Announcement announcement : allAnnouncements) {
                        if ("student".equalsIgnoreCase(announcement.getRole()) || "all".equalsIgnoreCase(announcement.getRole())) {
                            allStudentAnnouncements.add(announcement);
                        }
                    }

                    requireActivity().runOnUiThread(() -> {
                        if (allStudentAnnouncements.isEmpty()) {
                            noAnnouncementsMessage.setVisibility(View.VISIBLE);
                            announcementsContainer.setVisibility(View.GONE);
                            loadMoreButton.setVisibility(View.GONE);
                        } else {
                            noAnnouncementsMessage.setVisibility(View.GONE);
                            announcementsContainer.setVisibility(View.VISIBLE);
                            currentPage = 0;
                            displayAnnouncements();
                        }
                    });

                    Log.d(TAG, "Announcements fetched on 2025-06-09 11:43 AM IST: " + allStudentAnnouncements.size() + " announcements for students.");
                } else {
                    String errorMsg = "Failed to fetch announcements: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ", " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading errorBody on 2025-06-09 11:43 AM IST", e);
                        }
                    }
                    showToast(errorMsg);
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Announcement>> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    noAnnouncementsMessage.setVisibility(View.VISIBLE);
                    announcementsContainer.setVisibility(View.GONE);
                    loadMoreButton.setVisibility(View.GONE);
                });
                showToast("Fetch error: " + t.getMessage());
                Log.e(TAG, "Fetch error on 2025-06-09 11:43 AM IST: " + t.toString());
            }
        });
    }

    private void displayAnnouncements() {
        announcementsContainer.removeAllViews();
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allStudentAnnouncements.size());

        for (int i = startIndex; i < endIndex; i++) {
            Announcement announcement = allStudentAnnouncements.get(i);

            CardView cardView = new CardView(requireContext());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(8, 12, 8, 12);
            cardView.setLayoutParams(cardParams);
            cardView.setCardElevation(14f);
            cardView.setRadius(28f);
            cardView.setUseCompatPadding(true);

            LinearLayout cardContent = new LinearLayout(requireContext());
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            RelativeLayout bannerContainer = new RelativeLayout(requireContext());
            bannerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    320
            ));

            String titleLower = announcement.getTitle() != null ? announcement.getTitle().toLowerCase() : "";
            Integer bannerResource = null;

            if (titleLower.contains("holiday")) {
                bannerResource = R.drawable.ic_holiday;
            } else if (titleLower.contains("working day")) {
                bannerResource = R.drawable.ic_workingday;
            } else if (titleLower.contains("exam")) {
                bannerResource = R.drawable.ic_exams;
            } else if (titleLower.contains("meeting")) {
                bannerResource = R.drawable.ic_meeting;
            } else if (titleLower.contains("annual day")) {
                bannerResource = R.drawable.ic_annual;
            } else if (titleLower.contains("children")) {
                bannerResource = R.drawable.ic_childrensday;
            } else if (titleLower.contains("teacher")) {
                bannerResource = R.drawable.ic_teachersday;
            }

            ImageView bannerView = new ImageView(requireContext());
            RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    320
            );
            bannerView.setLayoutParams(imageParams);
            bannerView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            bannerView.setAdjustViewBounds(false);
            bannerView.setClipToOutline(true);
            bannerView.setElevation(8f);

            GradientDrawable imageBorder = new GradientDrawable();
            imageBorder.setCornerRadii(new float[]{28f, 28f, 28f, 28f, 0f, 0f, 0f, 0f});
            imageBorder.setStroke(3, ContextCompat.getColor(requireContext(), R.color.light_blue_400));
            bannerView.setBackground(imageBorder);

            boolean hasApiBanner = announcement.getBannerImage() != null && !announcement.getBannerImage().isEmpty();
            if (bannerResource != null) {
                Glide.with(requireContext())
                        .load(bannerResource)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_delete)
                        .into(bannerView);
            } else if (hasApiBanner) {
                String bannerUrl = BASE_URL + announcement.getBannerImage();
                Glide.with(requireContext())
                        .load(bannerUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_delete)
                        .into(bannerView);
            } else {
                Glide.with(requireContext())
                        .load(R.drawable.ic_placeholder)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_delete)
                        .into(bannerView);
            }
            bannerContainer.addView(bannerView);

            LinearLayout detailsSection = new LinearLayout(requireContext());
            detailsSection.setOrientation(LinearLayout.VERTICAL);
            detailsSection.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            detailsSection.setPadding(20, 20, 20, 20);
            detailsSection.setVisibility(View.GONE);

            GradientDrawable detailsBackground = new GradientDrawable();
            detailsBackground.setCornerRadii(new float[]{0f, 0f, 0f, 0f, 28f, 28f, 28f, 28f});
            detailsBackground.setColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            detailsSection.setBackground(detailsBackground);

            TextView titleView = new TextView(requireContext());
            titleView.setText(announcement.getTitle());
            titleView.setTextSize(24);
            titleView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
            titleView.setTypeface(null, Typeface.BOLD);
            titleView.setFontFeatureSettings("@font/poppins_bold");
            titleView.setPadding(0, 0, 0, 12);
            detailsSection.addView(titleView);

            TextView descView = new TextView(requireContext());
            descView.setText(announcement.getDescription());
            descView.setTextSize(16);
            descView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
            descView.setFontFeatureSettings("@font/poppins");
            descView.setLineSpacing(1.3f, 1.0f);
            detailsSection.addView(descView);

            bannerContainer.setOnClickListener(v -> {
                if (detailsSection.getVisibility() == View.GONE) {
                    detailsSection.setVisibility(View.VISIBLE);
                } else {
                    detailsSection.setVisibility(View.GONE);
                }
            });

            bannerContainer.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ScaleAnimation scaleDown = new ScaleAnimation(
                                1f, 0.95f, 1f, 0.95f,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f
                        );
                        scaleDown.setDuration(150);
                        v.startAnimation(scaleDown);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        ScaleAnimation scaleUp = new ScaleAnimation(
                                0.95f, 1f, 0.95f, 1f,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f
                        );
                        scaleUp.setDuration(150);
                        v.startAnimation(scaleUp);
                        break;
                }
                return false;
            });

            cardContent.addView(bannerContainer);
            cardContent.addView(detailsSection);
            cardView.addView(cardContent);
            announcementsContainer.addView(cardView);
        }

        if (endIndex < allStudentAnnouncements.size()) {
            loadMoreButton.setVisibility(View.VISIBLE);
        } else {
            loadMoreButton.setVisibility(View.GONE);
        }
    }

    private void loadMoreAnnouncements() {
        currentPage++;
        displayAnnouncements();
    }

    private void showToast(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show());
    }
}