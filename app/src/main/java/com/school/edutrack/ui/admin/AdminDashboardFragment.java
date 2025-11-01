//package com.school.edutrack.ui.admin;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.Build;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.view.GravityCompat;
//import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;
//import androidx.navigation.NavController;
//import androidx.navigation.Navigation;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.android.material.navigation.NavigationView;
//import com.school.edutrack.R;
//import java.util.Random;
//
//public class AdminDashboardFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {
//
//    private TextView welcomeTextView;
//    private TextView adminNameTextView;
//    private TextView adminRoleTextView;
//    private SharedPreferences sharedPreferences;
//    private String username;
//    private DrawerLayout drawerLayout;
//    private BottomNavigationView bottomNavigationView;
//    private NavController navController;
//    private View fragmentContainer;
//    private ImageView menuIcon;
//    private ImageView adminProfileImage;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        // Removed hardcoded status bar color setting; now controlled by the theme
//        Log.d("AdminDashboard", "Status bar color controlled by theme at 2025-06-16 01:18 PM IST");
//
//        // Initialize SharedPreferences
//        sharedPreferences = requireActivity().getSharedPreferences("EduTrackPrefs", Context.MODE_PRIVATE);
//
//        // Retrieve username and role from SharedPreferences
//        username = sharedPreferences.getString("user_username", "Unknown User");
//        String role = sharedPreferences.getString("user_role", "Admin");
//
//        // Initialize Navigation Controller
//        navController = Navigation.findNavController(view);
//
//        // Initialize DrawerLayout and NavigationView
//        drawerLayout = view.findViewById(R.id.drawer_layout);
//        NavigationView navigationView = view.findViewById(R.id.nav_view);
//        if (drawerLayout != null && navigationView != null) {
//            navigationView.setNavigationItemSelectedListener(this);
//            Log.d("AdminDashboard", "DrawerLayout and NavigationView initialized successfully at 2025-06-16 01:18 PM IST");
//        } else {
//            Log.e("AdminDashboard", "DrawerLayout or NavigationView not found at 2025-06-16 01:18 PM IST: drawerLayout=" + drawerLayout + ", navigationView=" + navigationView);
//        }
//
//        // Initialize Menu Icon
//        menuIcon = view.findViewById(R.id.menu_icon);
//        if (menuIcon != null) {
//            menuIcon.setOnClickListener(v -> {
//                if (drawerLayout != null) {
//                    drawerLayout.openDrawer(GravityCompat.START);
//                    Log.d("AdminDashboard", "Drawer opened via menu icon at 2025-06-16 01:18 PM IST");
//                }
//            });
//        }
//
//        // Initialize Bottom Navigation
//        bottomNavigationView = view.findViewById(R.id.bottom_navigation);
//        if (bottomNavigationView != null) {
//            bottomNavigationView.setOnNavigationItemSelectedListener(this);
//            Animation slideIn = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
//            bottomNavigationView.startAnimation(slideIn);
//            // Set the default selected item to ensure the color is applied
//            bottomNavigationView.setSelectedItemId(R.id.bottom_nav_manage_users);
//        }
//
//        // Initialize Views
//        welcomeTextView = view.findViewById(R.id.welcomeTextView);
//        adminNameTextView = view.findViewById(R.id.admin_name);
//        adminRoleTextView = view.findViewById(R.id.admin_role);
//        adminProfileImage = view.findViewById(R.id.admin_profile_image);
//        fragmentContainer = view.findViewById(R.id.fragment_container);
//
//        // Randomly select admin profile image
//        if (adminProfileImage != null) {
//            Random random = new Random();
//            int imageResource = random.nextBoolean() ? R.drawable.ic_admin : R.drawable.ic_admin_2;
//            adminProfileImage.setImageResource(imageResource);
//            Log.d("AdminDashboard", "Admin profile image set to: " + (imageResource == R.drawable.ic_admin ? "ic_admin" : "ic_admin_2") + " at 2025-06-16 01:18 PM IST");
//        }
//
//        // Set username and role
//        if (adminNameTextView != null) {
//            adminNameTextView.setText(username);
//        }
//        if (adminRoleTextView != null) {
//            adminRoleTextView.setText(role);
//        }
//
//        // Display welcome message
//        if (welcomeTextView != null) {
//            welcomeTextView.setText("Welcome, " + username + "!");
//            Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
//            welcomeTextView.startAnimation(fadeIn);
//        }
//
//        // Load default fragment
//        loadFragment(new ManageUsersFragment());
//        Animation fadeInContent = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
//        fragmentContainer.startAnimation(fadeInContent);
//
//        // Log the username for debugging
//        Log.d("AdminDashboard", "Username from SharedPreferences at 2025-06-16 01:18 PM IST: " + username);
//    }
//
//    private void loadFragment(Fragment fragment) {
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.replace(R.id.fragment_container, fragment);
//        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
//        transaction.commit();
//    }
//
//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        int itemId = item.getItemId();
//        // Handle Drawer and Bottom Navigation
//        if (itemId == R.id.nav_manage_users || itemId == R.id.bottom_nav_manage_users) {
//            loadFragment(new ManageUsersFragment());
//            Toast.makeText(getContext(), "Manage Users selected", Toast.LENGTH_SHORT).show();
//        } else if (itemId == R.id.nav_announcements || itemId == R.id.bottom_nav_announcements) {
//            loadFragment(new AnnouncementsFragment());
//            Toast.makeText(getContext(), "Announcements selected", Toast.LENGTH_SHORT).show();
//        } else if (itemId == R.id.nav_manage_timetable || itemId == R.id.bottom_nav_manage_timetable) {
//            loadFragment(new ManageTimetableFragment());
//            Toast.makeText(getContext(), "Manage Timetable selected", Toast.LENGTH_SHORT).show();
//        } else if (itemId == R.id.nav_settings || itemId == R.id.bottom_nav_profile) {
//            loadFragment(new SettingsFragment());
//            Toast.makeText(getContext(), "Settings selected", Toast.LENGTH_SHORT).show();
//        } else if (itemId == R.id.nav_logout) {
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.clear();
//            editor.apply();
//            try {
//                navController.navigate(R.id.action_adminDashboard_to_adminLogin);
//                Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
//            } catch (Exception e) {
//                Log.e("AdminDashboard", "Navigation error on logout at 2025-06-16 01:18 PM IST: " + e.getMessage(), e);
//                Toast.makeText(getContext(), "Logout failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        }
//
//        if (drawerLayout != null) {
//            drawerLayout.closeDrawer(GravityCompat.START);
//            Log.d("AdminDashboard", "Drawer closed after selecting item at 2025-06-16 01:18 PM IST: " + item.getTitle());
//        }
//        return true;
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (drawerLayout != null) {
//            drawerLayout.closeDrawer(GravityCompat.START);
//        }
//    }
//
//    public String getUsername() {
//        return username != null ? username : sharedPreferences.getString("user_username", "Unknown User");
//    }
//}