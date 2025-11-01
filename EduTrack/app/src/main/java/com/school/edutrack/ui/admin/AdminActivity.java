package com.school.edutrack.ui.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.school.edutrack.R;
import android.animation.ValueAnimator;
import android.view.Gravity;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener, ManageUsersFragment.HeaderAnimationListener {

    private TextView welcomeTextView;
    private TextView adminNameTextView;
    private TextView adminRoleTextView;
    private SharedPreferences sharedPreferences;
    private String username;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private NavController navController;
    private View headerBackground;
    private ImageView edutrackLogo;
    private LinearLayout adminProfileContainer;
    private ImageView adminProfileImage;
    private float initialHeaderHeight;
    private float initialLogoWidth;
    private float initialLogoHeight;
    private float initialProfileImageSize;
    private ValueAnimator headerAnimator;
    private float lastScrollPercentage = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Make the status bar transparent and allow the layout to draw behind it
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        }
        Log.d("AdminActivity", "Status bar set to transparent with light icons at 2025-06-28 02:47 PM IST");

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("EduTrackPrefs", MODE_PRIVATE);

        // Retrieve username and role from SharedPreferences
        username = sharedPreferences.getString("user_username", "Unknown User");
        String role = sharedPreferences.getString("user_role", "Admin");

        // Initialize NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        } else {
            Log.e("AdminActivity", "NavHostFragment not found at 2025-06-28 02:47 PM IST");
            Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (drawerLayout != null && navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            Log.d("AdminActivity", "DrawerLayout and NavigationView initialized at 2025-06-28 02:47 PM IST");
        } else {
            Log.e("AdminActivity", "DrawerLayout or NavigationView not found at 2025-06-28 02:47 PM IST");
        }

        // Initialize Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnNavigationItemSelectedListener(this);
            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
            bottomNavigationView.startAnimation(slideIn);
            bottomNavigationView.setSelectedItemId(R.id.bottom_nav_manage_users);
        } else {
            Log.e("AdminActivity", "BottomNavigationView not found at 2025-06-28 02:47 PM IST");
        }

        // Initialize Views
        welcomeTextView = findViewById(R.id.welcomeTextView);
        adminNameTextView = findViewById(R.id.admin_name);
        adminRoleTextView = findViewById(R.id.admin_role);
        headerBackground = findViewById(R.id.header_background);
        edutrackLogo = findViewById(R.id.edutrack_logo);
        adminProfileContainer = findViewById(R.id.admin_profile_container);
        adminProfileImage = findViewById(R.id.admin_profile_image);

        // Set logo color to white
        if (edutrackLogo != null) {
            edutrackLogo.setColorFilter(getResources().getColor(android.R.color.white));
        } else {
            Log.w("AdminActivity", "EduTrack logo not found at 2025-06-28 02:47 PM IST");
        }

        // Store initial values for animation
        if (headerBackground != null) {
            initialHeaderHeight = headerBackground.getLayoutParams().height;
        }
        if (edutrackLogo != null) {
            initialLogoWidth = edutrackLogo.getLayoutParams().width;
            initialLogoHeight = edutrackLogo.getLayoutParams().height;
        }
        if (adminProfileImage != null) {
            initialProfileImageSize = adminProfileImage.getLayoutParams().width;
        }

        // Set username and role in the header
        if (adminNameTextView != null) {
            adminNameTextView.setText(username);
        }
        if (adminRoleTextView != null) {
            adminRoleTextView.setText(role);
        }

        // Display welcome message
        if (welcomeTextView != null) {
            welcomeTextView.setText("Welcome, " + username + "!");
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            welcomeTextView.startAnimation(fadeIn);
        }

        Log.d("AdminActivity", "Username from SharedPreferences at 2025-06-28 02:47 PM IST: " + username);
    }

    @Override
    public void onHeaderScroll(float scrollPercentage) {
        if (Math.abs(scrollPercentage - lastScrollPercentage) < 0.01f) {
            return;
        }

        if (headerAnimator != null && headerAnimator.isRunning()) {
            headerAnimator.cancel();
        }

        headerAnimator = ValueAnimator.ofFloat(lastScrollPercentage, scrollPercentage);
        headerAnimator.setDuration(600);
        headerAnimator.setInterpolator(new DecelerateInterpolator());
        headerAnimator.addUpdateListener(animation -> {
            float animatedPercentage = (float) animation.getAnimatedValue();

            // Adjust header background height (25% reduction)
            if (headerBackground != null) {
                float targetHeight = initialHeaderHeight - (initialHeaderHeight * 0.25f * animatedPercentage);
                ViewGroup.LayoutParams headerParams = headerBackground.getLayoutParams();
                headerParams.height = (int) targetHeight;
                headerBackground.setLayoutParams(headerParams);
            }

            // Adjust logo size (25% reduction)
            if (edutrackLogo != null) {
                float targetLogoWidth = initialLogoWidth - (initialLogoWidth * 0.25f * animatedPercentage);
                float targetLogoHeight = initialLogoHeight - (initialLogoHeight * 0.25f * animatedPercentage);
                ViewGroup.LayoutParams logoParams = edutrackLogo.getLayoutParams();
                logoParams.width = (int) targetLogoWidth;
                logoParams.height = (int) targetLogoHeight;
                edutrackLogo.setLayoutParams(logoParams);
            }

            // Adjust profile image size (25% reduction) and maintain circle
            if (adminProfileImage != null) {
                float targetProfileSize = initialProfileImageSize - (initialProfileImageSize * 0.25f * animatedPercentage);
                ViewGroup.LayoutParams profileImageParams = adminProfileImage.getLayoutParams();
                profileImageParams.width = (int) targetProfileSize;
                profileImageParams.height = (int) targetProfileSize; // Ensure square for circle
                adminProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP); // Cover the circle
                adminProfileImage.setLayoutParams(profileImageParams);
            }

            // Adjust text sizes (25% reduction)
            if (adminNameTextView != null && adminRoleTextView != null) {
                float targetNameTextSize = 20f - (20f * 0.25f * animatedPercentage);
                float targetRoleTextSize = 16f - (16f * 0.25f * animatedPercentage);
                adminNameTextView.setTextSize(targetNameTextSize);
                adminRoleTextView.setTextSize(targetRoleTextSize);
            }

            // Improve alignment after shrinking
            if (adminProfileContainer != null) {
                if (animatedPercentage > 0.5f) { // After significant shrink
                    adminProfileContainer.setGravity(Gravity.START); // Align to start
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) adminProfileImage.getLayoutParams();
                    params.gravity = Gravity.CENTER_VERTICAL;
                    params.weight = 0; // Reset weight for tight alignment
                    adminProfileImage.setLayoutParams(params);
                    LinearLayout.LayoutParams nameParams = (LinearLayout.LayoutParams) adminNameTextView.getLayoutParams();
                    nameParams.gravity = Gravity.CENTER_VERTICAL;
                    adminNameTextView.setLayoutParams(nameParams);
                } else {
                    adminProfileContainer.setGravity(Gravity.CENTER_VERTICAL);
                }
            }

            Log.d("AdminActivity", "Header animation updated at 2025-06-28 02:47 PM IST: scrollPercentage=" + scrollPercentage);
        });

        headerAnimator.start();
        lastScrollPercentage = scrollPercentage;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.bottom_nav_more) {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
                Log.d("AdminActivity", "Drawer opened via More button at 2025-06-28 02:47 PM IST");
            }
            return true;
        } else if (itemId == R.id.bottom_nav_manage_users) {
            navController.navigate(R.id.manageUsersFragment);
            Toast.makeText(this, "Manage Users selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.bottom_nav_announcements) {
            navController.navigate(R.id.announcementsFragment);
            Toast.makeText(this, "Announcements selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.bottom_nav_manage_timetable) {
            navController.navigate(R.id.manageTimetableFragment);
            Toast.makeText(this, "Manage Timetable selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_manage_users) {
            navController.navigate(R.id.manageUsersFragment);
            Toast.makeText(this, "Manage Users selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_announcements) {
            navController.navigate(R.id.announcementsFragment);
            Toast.makeText(this, "Announcements selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_manage_timetable) {
            navController.navigate(R.id.manageTimetableFragment);
            Toast.makeText(this, "Manage Timetable selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_settings) {
            navController.navigate(R.id.settingsFragment);
            Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_logout) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Log.d("AdminActivity", "Logged out and finishing activity at 2025-06-28 02:47 PM IST");
            // Navigate to LoginActivity (uncomment and adjust as needed)
            // Intent intent = new Intent(this, LoginActivity.class);
            // startActivity(intent);
            finish();
        }

        if (drawerLayout != null && itemId != R.id.bottom_nav_more) {
            drawerLayout.closeDrawer(GravityCompat.START);
            Log.d("AdminActivity", "Drawer closed after selecting item at 2025-06-28 02:47 PM IST: " + item.getTitle());
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public String getUsername() {
        return username != null ? username : sharedPreferences.getString("user_username", "Unknown User");
    }
}