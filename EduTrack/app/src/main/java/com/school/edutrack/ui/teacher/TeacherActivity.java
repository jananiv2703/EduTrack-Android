package com.school.edutrack.ui.teacher;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.school.edutrack.R;
import com.school.edutrack.model.Teacher;
import com.school.edutrack.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class TeacherActivity extends AppCompatActivity {

    private static final String TAG = "TeacherActivity";
    private DrawerLayout drawerLayout;
    private NavController navController;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private TextView teacherNameTextView, dateTextView;
    private ImageView teacherProfileImageView;
    private SharedPreferences sharedPreferences;
    private String teacherId;
    private ImageView activeBackground;
    private ImageView activeUnderline;
    private View lastSelectedItemView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("EduTrackPrefs", MODE_PRIVATE);

        // Retrieve teacherId from Intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            teacherId = extras.getString("teacherId", "");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("teacher_id", teacherId);
            editor.apply();
            Log.d(TAG, "Teacher ID retrieved from extras at 08:13 PM IST, Saturday, June 28, 2025: " + teacherId);
        } else {
            teacherId = sharedPreferences.getString("teacher_id", "");
            if (teacherId.isEmpty()) {
                Log.e(TAG, "Teacher ID not found in SharedPreferences at 08:13 PM IST, Saturday, June 28, 2025");
                showErrorDialog("Error", "Teacher ID not found");
                finish();
                return;
            }
            Log.d(TAG, "Teacher ID retrieved from SharedPreferences at 08:13 PM IST, Saturday, June 28, 2025: " + teacherId);
        }

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.teacherDrawerLayout);
        navigationView = findViewById(R.id.teacherNavView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize Views
        teacherNameTextView = findViewById(R.id.teacher_name);
        dateTextView = findViewById(R.id.dateTextView);
        teacherProfileImageView = findViewById(R.id.teacherProfileImageView);
        bottomNavigationView = findViewById(R.id.teacherBottomNavigationView);

        // Apply animations
        applyFadeInAnimation(teacherNameTextView, 200);
        applyFadeInAnimation(dateTextView, 400);
        applyBounceAnimation(teacherProfileImageView, 600);

        // Set Teacher Info
        String teacherName = sharedPreferences.getString("user_name", "Teacher");
        teacherNameTextView.setText(teacherName);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy, hh:mm a", Locale.getDefault());
        dateTextView.setText(sdf.format(new Date()));

        // Fetch teacher profile to get gender and set the profile image
        fetchTeacherProfileForImage();

        // Profile Click
        teacherProfileImageView.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("teacherId", teacherId);
            try {
                navController.navigate(R.id.teacherProfileManagementFragment, args);
                Log.d(TAG, "Navigated to TeacherProfileManagementFragment from profile image at 08:13 PM IST, Saturday, June 28, 2025");
            } catch (Exception e) {
                Log.e(TAG, "Navigation error to TeacherProfileManagementFragment from profile image at 08:13 PM IST, Saturday, June 28, 2025: ", e);
                showErrorDialog("Navigation Error", "Failed to navigate: " + e.getMessage());
            }
        });

        // Set up NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.teacherNavHostFragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            Bundle args = new Bundle();
            args.putString("teacherId", teacherId);
            navController.setGraph(R.navigation.teacher_nav_graph, args);
            Log.d(TAG, "NavController initialized successfully at 08:13 PM IST, Saturday, June 28, 2025, current destination: " + navController.getCurrentDestination().getLabel());
        } else {
            Log.e(TAG, "NavHostFragment not found at 08:13 PM IST, Saturday, June 28, 2025");
            showErrorDialog("Navigation Error", "Navigation setup failed: NavHostFragment not found");
            finish();
            return;
        }

        // Set up NavigationView with NavController
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "Navigation drawer item selected at 08:13 PM IST, Saturday, June 28, 2025: " + itemId);

            if (itemId == R.id.teacher_nav_logout) {
                Log.d(TAG, "Logout selected, clearing SharedPreferences at 08:13 PM IST, Saturday, June 28, 2025");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                try {
                    navController.navigate(R.id.action_teacherdashboard_to_teacherLogin);
                    Log.d(TAG, "Navigated to teacherLoginFragment on logout at 08:13 PM IST, Saturday, June 28, 2025");
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Logout navigation error at 08:13 PM IST, Saturday, June 28, 2025: ", e);
                    showErrorDialog("Navigation Error", "Logout failed: " + e.getMessage());
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            Bundle args = new Bundle();
            args.putString("teacherId", teacherId);

            try {
                navController.navigate(itemId, args);
                Log.d(TAG, "Navigated to destination at 08:13 PM IST, Saturday, June 28, 2025: " + itemId);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error for item at 08:13 PM IST, Saturday, June 28, 2025 " + itemId + ": ", e);
                showErrorDialog("Navigation Error", "Failed to navigate: " + e.getMessage());
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Create active background (gradient with shadow)
        activeBackground = new ImageView(this);
        activeBackground.setImageResource(R.drawable.active_background_gradient);
        ConstraintLayout.LayoutParams backgroundParams = new ConstraintLayout.LayoutParams(100, 56);
        activeBackground.setLayoutParams(backgroundParams);
        ((ConstraintLayout) bottomNavigationView.getParent()).addView(activeBackground);
        activeBackground.setVisibility(View.GONE);

        // Create active underline (animated underline)
        activeUnderline = new ImageView(this);
        activeUnderline.setImageResource(R.drawable.active_underline);
        ConstraintLayout.LayoutParams underlineParams = new ConstraintLayout.LayoutParams(48, 4);
        activeUnderline.setLayoutParams(underlineParams);
        activeUnderline.setScaleX(0f);
        ((ConstraintLayout) bottomNavigationView.getParent()).addView(activeUnderline);
        activeUnderline.setVisibility(View.GONE);

        // Ensure the initial selected item is set correctly
        int initialSelectedItemId = bottomNavigationView.getSelectedItemId();
        Log.d(TAG, "Initial selected item ID at 08:13 PM IST, Saturday, June 28, 2025: " + initialSelectedItemId);

        if (initialSelectedItemId == View.NO_ID) {
            bottomNavigationView.setSelectedItemId(R.id.teacher_nav_profile);
            initialSelectedItemId = R.id.teacher_nav_profile;
            Log.d(TAG, "No initial item selected, forcing selection to teacher_nav_profile at 08:13 PM IST, Saturday, June 28, 2025");
        }

        // Position the active background and underline for the initially selected item
        final int finalInitialSelectedItemId = initialSelectedItemId;
        bottomNavigationView.post(() -> {
            View initialItemView = bottomNavigationView.findViewById(finalInitialSelectedItemId);
            if (initialItemView != null) {
                Log.d(TAG, "Initial item view found for ID " + finalInitialSelectedItemId + " at 08:13 PM IST, Saturday, June 28, 2025");
                updateActiveIndicatorsPosition(initialItemView);
                activeBackground.setVisibility(View.VISIBLE);
                activeUnderline.setVisibility(View.VISIBLE);
                animateUnderline(initialItemView);
                animateIcon(initialItemView);
                lastSelectedItemView = initialItemView;
            } else {
                Log.e(TAG, "Initial item view NOT found for ID " + finalInitialSelectedItemId + " at 08:13 PM IST, Saturday, June 28, 2025");
            }
        });

        // Set up BottomNavigationView with Animation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "Bottom navigation item selected at 08:13 PM IST, Saturday, June 28, 2025: " + itemId);

            // Apply animations to the selected item
            View itemView = bottomNavigationView.findViewById(itemId);
            if (itemView != null) {
                Log.d(TAG, "Item view found for ID " + itemId + " at 08:13 PM IST, Saturday, June 28, 2025");
                updateActiveIndicatorsPosition(itemView);
                activeBackground.setVisibility(View.VISIBLE);
                activeUnderline.setVisibility(View.VISIBLE);
                animateUnderline(itemView);
                animateIcon(itemView);
                if (lastSelectedItemView != null && lastSelectedItemView != itemView) {
                    resetIcon(lastSelectedItemView);
                }
                lastSelectedItemView = itemView;
            } else {
                Log.e(TAG, "Item view NOT found for ID " + itemId + " at 08:13 PM IST, Saturday, June 28, 2025");
                return false;
            }

            Bundle args = new Bundle();
            args.putString("teacherId", teacherId);

            try {
                if (itemId == R.id.teacher_nav_profile) {
                    navController.navigate(R.id.teacherProfileManagementFragment, args);
                    Log.d(TAG, "Navigated to TeacherProfileManagementFragment at 08:13 PM IST, Saturday, June 28, 2025");
                    return true;
                } else if (itemId == R.id.teacher_nav_announcements) {
                    navController.navigate(R.id.teacherAnnouncementsFragment, args);
                    Log.d(TAG, "Navigated to TeacherAnnouncementsFragment at 08:13 PM IST, Saturday, June 28, 2025");
                    return true;
                } else if (itemId == R.id.teacher_nav_attendance) {
                    navController.navigate(R.id.teacherAttendanceManagementFragment, args);
                    Log.d(TAG, "Navigated to TeacherAttendanceManagementFragment at 08:13 PM IST, Saturday, June 28, 2025");
                    return true;
                } else if (itemId == R.id.teacher_nav_more) {
                    Log.d(TAG, "Bottom navigation: More selected, opening drawer at 08:13 PM IST, Saturday, June 28, 2025");
                    drawerLayout.openDrawer(GravityCompat.START);
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Bottom navigation error for item at 08:13 PM IST, Saturday, June 28, 2025 " + itemId + ": ", e);
                showErrorDialog("Navigation Error", "Failed to navigate: " + e.getMessage());
            }
            return false;
        });

        // Setup Navigation Drawer Header
        View headerView = navigationView.getHeaderView(0);
        TextView headerName = headerView.findViewById(R.id.teacherNavHeaderNameTextView);
        TextView headerRole = headerView.findViewById(R.id.teacherNavHeaderRoleTextView);
        if (headerName != null && headerRole != null) {
            headerName.setText(teacherName);
            headerRole.setText("Teacher");
            applyFadeInAnimation(headerName, 200);
            applyFadeInAnimation(headerRole, 400);
            Log.d(TAG, "Navigation drawer header set at 08:13 PM IST, Saturday, June 28, 2025: Name=" + teacherName + ", Role=Teacher");
        }
    }

    private void fetchTeacherProfileForImage() {
        Log.d(TAG, "Fetching teacher details for teacherId: " + teacherId + " to set profile image at 08:13 PM IST, Saturday, June 28, 2025");

        RetrofitClient.getApiService().getTeacher(teacherId).enqueue(new Callback<Teacher>() {
            @Override
            public void onResponse(Call<Teacher> call, Response<Teacher> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Teacher teacher = response.body();
                    setProfileImage(teacher.getGender());
                    Log.d(TAG, "Teacher profile fetched for image setting on 2025-06-28 08:13 PM IST: " + teacher.getGender());
                } else {
                    String errorMsg = "Failed to fetch teacher profile for image: HTTP " + response.code();
                    Log.e(TAG, errorMsg + " on 2025-06-28 08:13 PM IST");
                    showErrorDialog("Error", errorMsg);
                    setProfileImage("Male");
                }
            }

            @Override
            public void onFailure(Call<Teacher> call, Throwable t) {
                Log.e(TAG, "Fetch error for image setting on 2025-06-28 08:13 PM IST: " + t.getMessage(), t);
                showErrorDialog("Error", "Failed to fetch profile: " + t.getMessage());
                setProfileImage("Male");
            }
        });
    }

    private void setProfileImage(String gender) {
        if (teacherProfileImageView == null) return;

        String teacherGender = gender != null ? gender : "Male";
        String imageResKey = "teacher_profile_image_res_" + teacherId;
        String genderKey = "teacher_profile_gender_" + teacherId;

        String savedGender = sharedPreferences.getString(genderKey, "");
        boolean useSavedImage = sharedPreferences.contains(imageResKey) && teacherGender.equalsIgnoreCase(savedGender);

        int imageResource;
        if (useSavedImage) {
            imageResource = sharedPreferences.getInt(imageResKey, R.drawable.ic_maleteacher1);
            Log.d(TAG, "Using saved profile image for teacherId " + teacherId + ", gender " + teacherGender + ", resource ID: " + imageResource + " at 08:13 PM IST, Saturday, June 28, 2025");
        } else {
            Random random = new Random();
            int randomIndex = random.nextInt(4) + 1;

            if ("Female".equalsIgnoreCase(teacherGender)) {
                switch (randomIndex) {
                    case 1: imageResource = R.drawable.ic_femaleteacher1; break;
                    case 2: imageResource = R.drawable.ic_femaleteacher2; break;
                    case 3: imageResource = R.drawable.ic_femaleteacher3; break;
                    case 4: default: imageResource = R.drawable.ic_femaleteacher4; break;
                }
            } else {
                switch (randomIndex) {
                    case 1: imageResource = R.drawable.ic_maleteacher1; break;
                    case 2: imageResource = R.drawable.ic_maleteacher2; break;
                    case 3: imageResource = R.drawable.ic_maleteacher3; break;
                    case 4: default: imageResource = R.drawable.ic_maleteacher4; break;
                }
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(imageResKey, imageResource);
            editor.putString(genderKey, teacherGender);
            editor.apply();
            Log.d(TAG, "Selected and saved new profile image for teacherId " + teacherId + ", gender " + teacherGender + " to ic_" + (teacherGender.equalsIgnoreCase("Female") ? "femaleteacher" : "maleteacher") + randomIndex + " at 08:13 PM IST, Saturday, June 28, 2025");
        }
        teacherProfileImageView.setImageResource(imageResource);
        applyBounceAnimation(teacherProfileImageView, 0);
    }

    private void updateActiveIndicatorsPosition(View itemView) {
        float backgroundCenterX = itemView.getX() + (itemView.getWidth() - activeBackground.getWidth()) / 2f;
        float backgroundCenterY = itemView.getY() + (itemView.getHeight() - activeBackground.getHeight()) / 2f - 4;
        activeBackground.animate()
                .x(backgroundCenterX)
                .y(backgroundCenterY)
                .setDuration(300)
                .start();
        Log.d(TAG, "Active background positioned at X: " + backgroundCenterX + ", Y: " + backgroundCenterY + " at 08:13 PM IST, Saturday, June 28, 2025");

        float underlineCenterX = itemView.getX() + (itemView.getWidth() - activeUnderline.getWidth()) / 2f;
        float underlineBottomY = itemView.getY() + itemView.getHeight() - activeUnderline.getHeight();
        activeUnderline.animate()
                .x(underlineCenterX)
                .y(underlineBottomY)
                .setDuration(300)
                .start();
        Log.d(TAG, "Active underline positioned at X: " + underlineCenterX + ", Y: " + underlineBottomY + " at 08:13 PM IST, Saturday, June 28, 2025");
    }

    private void animateUnderline(View itemView) {
        activeUnderline.setScaleX(0f);
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0f, 1f, 1f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(300);
        scaleAnimation.setFillAfter(true);
        activeUnderline.startAnimation(scaleAnimation);
    }

    private void animateIcon(View itemView) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1f, 1.3f, 1f, 1.3f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(300);
        scaleAnimation.setInterpolator(new BounceInterpolator());
        scaleAnimation.setFillAfter(true);
        itemView.startAnimation(scaleAnimation);
    }

    private void resetIcon(View itemView) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.3f, 1f, 1.3f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(300);
        scaleAnimation.setFillAfter(true);
        itemView.startAnimation(scaleAnimation);
    }

    private void showErrorDialog(String title, String message) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_error);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialog.findViewById(R.id.error_message);
        dialogTitle.setText(title);
        dialogMessage.setText(message);
        dialog.findViewById(R.id.error_message).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void applyFadeInAnimation(View view, long delay) {
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.setStartOffset(delay);
        view.startAnimation(fadeIn);
    }

    private void applyBounceAnimation(View view, long delay) {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.8f, 1f, 0.8f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(800);
        scaleAnimation.setStartOffset(delay);
        scaleAnimation.setInterpolator(new BounceInterpolator());
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(800);
        alphaAnimation.setStartOffset(delay);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        view.startAnimation(animationSet);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            Log.d(TAG, "Drawer open, closing drawer on back press at 08:13 PM IST, Saturday, June 28, 2025");
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            int currentDestinationId = navController.getCurrentDestination().getId();
            Log.d(TAG, "Back pressed at 08:13 PM IST, Saturday, June 28, 2025, current destination: " + currentDestinationId);
            if (currentDestinationId == R.id.teacherDashboardFragment) {
                Log.d(TAG, "On dashboard, finishing activity at 08:13 PM IST, Saturday, June 28, 2025");
                finish();
            } else {
                try {
                    Log.d(TAG, "Navigating back to dashboard at 08:13 PM IST, Saturday, June 28, 2025");
                    if (currentDestinationId == R.id.teacherProfileManagementFragment) {
                        navController.navigate(R.id.action_teacherProfileManagement_to_teacherDashboard);
                    } else if (currentDestinationId == R.id.teacherViewStudentsFragment) {
                        navController.navigate(R.id.action_teacherViewStudents_to_teacherDashboard);
                    } else if (currentDestinationId == R.id.teacherAttendanceManagementFragment) {
                        navController.navigate(R.id.action_teacherAttendanceManagement_to_teacherDashboard);
                    } else if (currentDestinationId == R.id.teacherManageStudentsFragment) {
                        navController.navigate(R.id.action_teacherManageStudents_to_teacherDashboard);
                    } else if (currentDestinationId == R.id.teacherAnnouncementsFragment) {
                        navController.navigate(R.id.action_teacherAnnouncements_to_teacherDashboard);
                    } else if (currentDestinationId == R.id.teacherAssignmentsFragment) {
                        navController.navigate(R.id.action_teacherAssignments_to_teacherDashboard);
                    } else if (currentDestinationId == R.id.teacherTimeTableFragment) {
                        navController.navigate(R.id.action_teacherTimeTable_to_teacherDashboard);
                    } else if (currentDestinationId == R.id.teacherExamMarksFragment) {
                        navController.navigate(R.id.action_teacherExamMarks_to_teacherDashboard);
                    } else if (currentDestinationId == R.id.teacherConductQuizFragment) {
                        navController.navigate(R.id.action_teacherConductQuiz_to_teacherDashboard);
                    } else if (currentDestinationId == R.id.teacherLeaveManagementFragment) {
                        navController.navigate(R.id.action_teacherLeaveManagement_to_teacherDashboard);
                    } else {
                        Log.w(TAG, "Unknown destination on back press at 08:13 PM IST, Saturday, June 28, 2025: " + currentDestinationId);
                        finish();
                    }
                    Log.d(TAG, "Navigated back to teacher dashboard at 08:13 PM IST, Saturday, June 28, 2025");
                } catch (Exception e) {
                    Log.e(TAG, "Back navigation error at 08:13 PM IST, Saturday, June 28, 2025: ", e);
                    showErrorDialog("Navigation Error", "Back navigation failed: " + e.getMessage());
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "Home button pressed at 08:13 PM IST, Saturday, June 28, 2025");
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                Log.d(TAG, "Drawer open, closing drawer at 08:13 PM IST, Saturday, June 28, 2025");
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                Log.d(TAG, "Drawer closed, opening drawer at 08:13 PM IST, Saturday, June 28, 2025");
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
