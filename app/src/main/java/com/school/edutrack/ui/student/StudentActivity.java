package com.school.edutrack.ui.student;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.school.edutrack.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentActivity extends AppCompatActivity {

    private static final String TAG = "StudentActivity";
    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private TextView studentNameTextView, dateTextView;
    private ImageView studentProfileImageView;
    private SharedPreferences sharedPreferences;
    private String studentId;
    private LinearLayout menuDashboard, menuProfileManagement, menuAnnouncements, menuAttendanceView,
            menuAssignments, menuTimeTable, menuExamMarks, menuLeaveManagement, menuPayments,
            menuStudyMaterials, menuQuizzes, menuLogout;
    private LinearLayout lastSelectedMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("EduTrackPrefs", MODE_PRIVATE);

        // Retrieve studentId from Intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            studentId = extras.getString("studentId", "");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("student_id", studentId);
            editor.apply();
            Log.d(TAG, "Student ID retrieved from extras at 2025-06-28 07:39 PM IST: " + studentId);
        } else {
            studentId = sharedPreferences.getString("student_id", "");
            if (studentId.isEmpty()) {
                Log.e(TAG, "Student ID not found in SharedPreferences at 2025-06-28 07:39 PM IST");
                Toast.makeText(this, "Error: Student ID not found", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            Log.d(TAG, "Student ID retrieved from SharedPreferences at 2025-06-28 07:39 PM IST: " + studentId);
        }

        // Set up Toolbar


        // Initialize Views
        studentNameTextView = findViewById(R.id.student_name);
        dateTextView = findViewById(R.id.dateTextView);
        studentProfileImageView = findViewById(R.id.studentProfileImageView);
        bottomNavigationView = findViewById(R.id.studentBottomNavigationView);

        // Initialize Horizontal Menu Items
        menuDashboard = findViewById(R.id.menu_dashboard);
        menuProfileManagement = findViewById(R.id.menu_profile_management);
        menuAnnouncements = findViewById(R.id.menu_announcements);
        menuAttendanceView = findViewById(R.id.menu_attendance_view);
        menuAssignments = findViewById(R.id.menu_assignments);
        menuTimeTable = findViewById(R.id.menu_time_table);
        menuExamMarks = findViewById(R.id.menu_exam_marks);
        menuLeaveManagement = findViewById(R.id.menu_leave_management);
        menuPayments = findViewById(R.id.menu_payments);
        menuStudyMaterials = findViewById(R.id.menu_study_materials);
        menuQuizzes = findViewById(R.id.menu_quizzes);
        menuLogout = findViewById(R.id.menu_logout);

        // Set Student Info
        String studentName = sharedPreferences.getString("user_name", "Student");
        studentNameTextView.setText(studentName);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy, hh:mm a", Locale.getDefault());
        dateTextView.setText(sdf.format(new Date()));

        // Profile Click
        studentProfileImageView.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("studentId", studentId);
            try {
                navController.navigate(R.id.action_global_studentProfileManagementFragment, args);
                Log.d(TAG, "Navigated to StudentProfileManagementFragment from profile image at 2025-06-28 07:39 PM IST");
                selectMenuItem(menuProfileManagement);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error to Profile Management from profile image at 2025-06-28 07:39 PM IST: ", e);
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Set up NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.studentNavHostFragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            // Pass studentId to the start destination (Dashboard)
            Bundle args = new Bundle();
            args.putString("studentId", studentId);
            navController.setGraph(R.navigation.student_nav_graph, args);
            Log.d(TAG, "NavController initialized successfully at 2025-06-28 07:39 PM IST, current destination: " + navController.getCurrentDestination().getLabel());
        } else {
            Log.e(TAG, "NavHostFragment not found at 2025-06-28 07:39 PM IST");
            Toast.makeText(this, "Navigation setup error: NavHostFragment not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set up Horizontal Menu Click Listeners
        setupHorizontalMenu();

        // Set the Dashboard as the default selected item
        selectMenuItem(menuDashboard);

        // Set up BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "Bottom navigation item selected at 2025-06-28 07:39 PM IST: " + itemId);
            Bundle args = new Bundle();
            args.putString("studentId", studentId);

            try {
                if (itemId == R.id.student_nav_profile) {
                    navController.navigate(R.id.action_global_studentProfileManagementFragment, args);
                    Log.d(TAG, "Navigated to StudentProfileManagementFragment from Bottom Navigation at 2025-06-28 07:39 PM IST");
                    selectMenuItem(menuProfileManagement);
                    return true;
                } else if (itemId == R.id.student_nav_announcements) {
                    navController.navigate(R.id.action_global_studentAnnouncementsFragment, args);
                    Log.d(TAG, "Navigated to StudentAnnouncementsFragment from Bottom Navigation at 2025-06-28 07:39 PM IST");
                    selectMenuItem(menuAnnouncements);
                    return true;
                } else if (itemId == R.id.student_nav_attendance) {
                    navController.navigate(R.id.action_global_studentAttendanceViewFragment, args);
                    Log.d(TAG, "Navigated to StudentAttendanceViewFragment from Bottom Navigation at 2025-06-28 07:39 PM IST");
                    selectMenuItem(menuAttendanceView);
                    return true;
                } else if (itemId == R.id.student_nav_more) {
                    Log.d(TAG, "Bottom navigation: More selected, showing bottom sheet at 2025-06-28 07:39 PM IST");
                    showMoreBottomSheet();
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Bottom navigation error for item at 2025-06-28 07:39 PM IST: " + itemId + ", " + e.getMessage());
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return false;
        });
    }

    private void setupHorizontalMenu() {
        Bundle args = new Bundle();
        args.putString("studentId", studentId);

        menuDashboard.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_global_studentDashboardFragment, args);
                Log.d(TAG, "Navigated to StudentDashboardFragment from Horizontal Menu at 2025-06-28 07:39 PM IST");
                selectMenuItem(menuDashboard);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error to Dashboard from Horizontal Menu at 2025-06-28 07:39 PM IST: ", e);
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        menuProfileManagement.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_global_studentProfileManagementFragment, args);
                Log.d(TAG, "Navigated to StudentProfileManagementFragment from Horizontal Menu at 2025-06-28 07:39 PM IST");
                selectMenuItem(menuProfileManagement);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error to Profile Management from Horizontal Menu at 2025-06-28 07:39 PM IST: ", e);
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        menuAnnouncements.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_global_studentAnnouncementsFragment, args);
                Log.d(TAG, "Navigated to StudentAnnouncementsFragment from Horizontal Menu at 2025-06-28 07:39 PM IST");
                selectMenuItem(menuAnnouncements);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error to Announcements from Horizontal Menu at 2025-06-28 07:39 PM IST: ", e);
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        menuAttendanceView.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_global_studentAttendanceViewFragment, args);
                Log.d(TAG, "Navigated to StudentAttendanceViewFragment from Horizontal Menu at 2025-06-28 07:39 PM IST");
                selectMenuItem(menuAttendanceView);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error to Attendance View from Horizontal Menu at 2025-06-28 07:39 PM IST: ", e);
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        menuAssignments.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_global_studentAssignmentsFragment, args);
                Log.d(TAG, "Navigated to StudentAssignmentsFragment from Horizontal Menu at 2025-06-28 07:39 PM IST");
                selectMenuItem(menuAssignments);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error to Assignments from Horizontal Menu at 2025-06-28 07:39 PM IST: ", e);
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        menuTimeTable.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_global_studentTimeTableFragment, args);
                Log.d(TAG, "Navigated to StudentTimeTableFragment from Horizontal Menu at 2025-06-28 07:39 PM IST");
                selectMenuItem(menuTimeTable);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error to Time Table from Horizontal Menu at 2025-06-28 07:39 PM IST: ", e);
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        menuExamMarks.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_global_studentExamMarksFragment, args);
                Log.d(TAG, "Navigated to StudentExamMarksFragment from Horizontal Menu at 2025-06-28 07:39 PM IST");
                selectMenuItem(menuExamMarks);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error to Exam Marks from Horizontal Menu at 2025-06-28 07:39 PM IST: ", e);
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        menuLeaveManagement.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_global_studentLeaveManagementFragment, args);
                Log.d(TAG, "Navigated to StudentLeaveManagementFragment from Horizontal Menu at 2025-06-28 07:39 PM IST");
                selectMenuItem(menuLeaveManagement);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error to Leave Management from Horizontal Menu at 2025-06-28 07:39 PM IST: ", e);
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        menuPayments.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_global_studentPaymentsFragment, args);
                Log.d(TAG, "Navigated to StudentPaymentsFragment from Horizontal Menu at 2025-06-28 07:39 PM IST");
                selectMenuItem(menuPayments);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error to Payments from Horizontal Menu at 2025-06-28 07:39 PM IST: ", e);
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        menuStudyMaterials.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_global_studentStudyMaterialsFragment, args);
                Log.d(TAG, "Navigated to StudentStudyMaterialsFragment from Horizontal Menu at 2025-06-28 07:39 PM IST");
                selectMenuItem(menuStudyMaterials);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error to Study Materials from Horizontal Menu at 2025-06-28 07:39 PM IST: ", e);
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        menuQuizzes.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_global_studentQuizzesFragment, args);
                Log.d(TAG, "Navigated to StudentQuizzesFragment from Horizontal Menu at 2025-06-28 07:39 PM IST");
                selectMenuItem(menuQuizzes);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error to Quizzes from Horizontal Menu at 2025-06-28 07:39 PM IST: ", e);
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        menuLogout.setOnClickListener(v -> {
            Log.d(TAG, "Logout selected, showing logout dialog at 2025-06-28 07:39 PM IST");
            showLogoutDialog();
        });
    }

    private void selectMenuItem(LinearLayout selectedItem) {
        // Deselect the previously selected item
        if (lastSelectedMenuItem != null && lastSelectedMenuItem != selectedItem) {
            lastSelectedMenuItem.setSelected(false);
        }

        // Select the new item
        selectedItem.setSelected(true);
        lastSelectedMenuItem = selectedItem;
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_logout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        ImageView logoutImage = dialog.findViewById(R.id.logout_image);
        TextView logoutText = dialog.findViewById(R.id.logout_text);

        logoutText.setText("Logging out...");
        Glide.with(this)
                .asGif()
                .load(R.drawable.logout_anim)
                .into(logoutImage);

        dialog.show();

        // Clear SharedPreferences and navigate after 2 seconds to allow GIF to play
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Log.d(TAG, "SharedPreferences cleared on logout at 2025-06-28 07:39 PM IST");
            try {
                navController.navigate(R.id.action_global_studentLogin);
                Log.d(TAG, "Navigated to StudentLoginFragment on logout at 2025-06-28 07:39 PM IST");
                finish();
            } catch (Exception e) {
                Log.e(TAG, "Logout navigation error at 2025-06-28 07:39 PM IST: ", e);
                Toast.makeText(this, "Logout error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        }, 2000);
    }

    private void showMoreBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_more);

        LinearLayout quizzes = bottomSheetDialog.findViewById(R.id.more_quizzes);
        LinearLayout payments = bottomSheetDialog.findViewById(R.id.more_payments);
        LinearLayout profile = bottomSheetDialog.findViewById(R.id.more_profile);
        LinearLayout studyMaterials = bottomSheetDialog.findViewById(R.id.more_study_materials);

        Bundle args = new Bundle();
        args.putString("studentId", studentId);

        if (quizzes != null) {
            quizzes.setOnClickListener(v -> {
                try {
                    navController.navigate(R.id.action_global_studentQuizzesFragment, args);
                    Log.d(TAG, "Navigated to StudentQuizzesFragment from More menu at 2025-06-28 07:39 PM IST");
                    selectMenuItem(menuQuizzes);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error to Quizzes from More menu at 2025-06-28 07:39 PM IST: ", e);
                    Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                bottomSheetDialog.dismiss();
            });
        }

        if (payments != null) {
            payments.setOnClickListener(v -> {
                try {
                    navController.navigate(R.id.action_global_studentPaymentsFragment, args);
                    Log.d(TAG, "Navigated to StudentPaymentsFragment from More menu at 2025-06-28 07:39 PM IST");
                    selectMenuItem(menuPayments);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error to Payments from More menu at 2025-06-28 07:39 PM IST: ", e);
                    Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                bottomSheetDialog.dismiss();
            });
        }

        if (profile != null) {
            profile.setOnClickListener(v -> {
                try {
                    navController.navigate(R.id.action_global_studentProfileManagementFragment, args);
                    Log.d(TAG, "Navigated to StudentProfileManagementFragment from More menu at 2025-06-28 07:39 PM IST");
                    selectMenuItem(menuProfileManagement);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error to Profile Management from More menu at 2025-06-28 07:39 PM IST: ", e);
                    Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                bottomSheetDialog.dismiss();
            });
        }

        if (studyMaterials != null) {
            studyMaterials.setOnClickListener(v -> {
                try {
                    navController.navigate(R.id.action_global_studentStudyMaterialsFragment, args);
                    Log.d(TAG, "Navigated to StudentStudyMaterialsFragment from More menu at 2025-06-28 07:39 PM IST");
                    selectMenuItem(menuStudyMaterials);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error to Study Materials from More menu at 2025-06-28 07:39 PM IST: ", e);
                    Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.show();
    }

    @Override
    public void onBackPressed() {
        int currentDestinationId = navController.getCurrentDestination().getId();
        Log.d(TAG, "Back pressed at 2025-06-28 07:39 PM IST, current destination: " + currentDestinationId);
        if (currentDestinationId == R.id.studentDashboardFragment) {
            Log.d(TAG, "On dashboard, finishing activity at 2025-06-28 07:39 PM IST");
            finish();
        } else {
            try {
                Bundle args = new Bundle();
                args.putString("studentId", studentId);
                navController.navigate(R.id.action_global_studentDashboardFragment, args);
                Log.d(TAG, "Navigated back to StudentDashboardFragment using global action at 2025-06-28 07:39 PM IST");
                selectMenuItem(menuDashboard);
            } catch (Exception e) {
                Log.e(TAG, "Back navigation error at 2025-06-28 07:39 PM IST: ", e);
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "Home button pressed, no drawer to toggle at 2025-06-28 07:39 PM IST");
            return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
