package com.school.edutrack.ui.admin;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.school.edutrack.R;
import com.school.edutrack.model.LoginResponse;
import com.school.edutrack.network.ApiService;
import com.school.edutrack.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLoginFragment extends Fragment {

    private static final String TAG = "AdminLoginFragment";
    private TextInputLayout usernameInputLayout, passwordInputLayout;
    private TextInputEditText usernameEditText, passwordEditText;
    private MaterialButton loginButton;
    private TextView forgotPasswordTextView, backButton;
    private SharedPreferences sharedPreferences;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("EduTrackPrefs", Context.MODE_PRIVATE);

        // Initialize NavController
        navController = Navigation.findNavController(view);
        Log.d(TAG, "NavController initialized at 08:44 PM IST, Saturday, June 28, 2025");

        // Initialize Views
        ImageView edutrackLogo = view.findViewById(R.id.edutrackLogo);
        TextView titleTextView = view.findViewById(R.id.titleTextView);
        TextView taglineTextView = view.findViewById(R.id.taglineTextView);
        usernameInputLayout = view.findViewById(R.id.usernameInputLayout);
        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordInputLayout = view.findViewById(R.id.passwordInputLayout);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        forgotPasswordTextView = view.findViewById(R.id.forgotPasswordTextView);
        backButton = view.findViewById(R.id.backButton);

        // Null checks
        if (edutrackLogo == null || titleTextView == null || taglineTextView == null ||
                usernameInputLayout == null || usernameEditText == null ||
                passwordInputLayout == null || passwordEditText == null ||
                loginButton == null || forgotPasswordTextView == null || backButton == null) {
            Log.e(TAG, "UI components not found at 08:44 PM IST, Saturday, June 28, 2025");
            showErrorDialog("Error", "UI components not found");
            return;
        }

        // Apply animations
        applyBounceAnimation(edutrackLogo, 0);
        applyScaleFadeAnimation(titleTextView, 200);
        applyScaleFadeAnimation(taglineTextView, 400);
        applySlideUpAnimation(usernameInputLayout, 600);
        applySlideUpAnimation(passwordInputLayout, 800);
        applySlideUpAnimation(loginButton, 1000);
        applySlideInAnimation(forgotPasswordTextView, 1200);
        applySlideInAnimation(backButton, 1400);

        // Load saved username
        String savedUsername = sharedPreferences.getString("user_username", "");
        if (!savedUsername.isEmpty()) {
            usernameEditText.setText(savedUsername);
            Log.d(TAG, "Loaded saved username at 08:44 PM IST, Saturday, June 28, 2025: " + savedUsername);
        }

        // Login Button Logic
        loginButton.setOnClickListener(v -> {
            applyButtonPressAnimation(loginButton);
            String username = usernameEditText.getText() != null ? usernameEditText.getText().toString().trim() : "";
            String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";

            if (username.isEmpty() || password.isEmpty()) {
                showErrorDialog("Validation Error", "Please enter both username and password");
                Log.w(TAG, "Username or password empty at 08:44 PM IST, Saturday, June 28, 2025");
                return;
            }

            // Show loading dialog with GIF
            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.custom_dialog_loading);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(false);
            ImageView loadingImage = dialog.findViewById(R.id.loading_image);
            TextView loadingText = dialog.findViewById(R.id.loading_text);
            loadingText.setText("Logging in...");
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.loading_anim)
                    .into(loadingImage);
            dialog.show();

            // Create login request
            ApiService.LoginRequest request = new ApiService.LoginRequest(username, password);
            RetrofitClient.getApiService().loginAdmin(request).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        dialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse loginResponse = response.body();
                            Log.d(TAG, "Login response at 08:44 PM IST, Saturday, June 28, 2025: " + loginResponse.getMessage());
                            if ("Login successful".equals(loginResponse.getMessage())) {
                                // Save user session
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("user_id", loginResponse.getUser().getId());
                                editor.putString("user_username", loginResponse.getUser().getUsername());
                                editor.putString("user_name", loginResponse.getUser().getName());
                                editor.putString("user_role", loginResponse.getUser().getRole());
                                editor.apply();
                                navigateToAdminActivity();
                            } else {
                                showErrorDialog("Login Failed", loginResponse.getMessage());
                                Log.w(TAG, "Login failed at 08:44 PM IST, Saturday, June 28, 2025: " + loginResponse.getMessage());
                            }
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                                Log.e(TAG, "Login failed at 08:44 PM IST, Saturday, June 28, 2025: Response code " + response.code() + ", Error body: " + errorBody);
                                showErrorDialog("Login Failed", "Response code " + response.code());
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading response at 08:44 PM IST, Saturday, June 28, 2025: ", e);
                                showErrorDialog("Login Failed", "Unable to parse response");
                            }
                        }
                    }, 2000);
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        dialog.dismiss();
                        Log.e(TAG, "Login error at 08:44 PM IST, Saturday, June 28, 2025: ", t);
                        showErrorDialog("Network Error", "Failed to connect: " + t.getMessage());
                    }, 2000);
                }
            });
        });

        // Forgot Password
        forgotPasswordTextView.setOnClickListener(v -> {
            applyHoverAnimation(forgotPasswordTextView);
            Log.d(TAG, "Forgot Password clicked at 08:44 PM IST, Saturday, June 28, 2025");
            showErrorDialog("Info", "Forgot Password functionality to be implemented");
        });

        // Back Button Logic
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "Back button pressed at 08:44 PM IST, Saturday, June 28, 2025");
            try {
                if (navController.getCurrentDestination().getId() == R.id.adminLoginFragment) {
                    navController.popBackStack();
                    Log.d(TAG, "Popped back stack at 08:44 PM IST, Saturday, June 28, 2025");
                } else {
                    requireActivity().finish();
                    Log.d(TAG, "Finished activity at 08:44 PM IST, Saturday, June 28, 2025");
                }
            } catch (Exception e) {
                Log.e(TAG, "Back navigation error at 08:44 PM IST, Saturday, June 28, 2025: ", e);
                showErrorDialog("Navigation Error", "Failed to navigate back: " + e.getMessage());
            }
        });
    }

    private void navigateToAdminActivity() {
        try {
            Log.d(TAG, "Attempting to navigate to AdminActivity at 08:44 PM IST, Saturday, June 28, 2025");
            navController.navigate(R.id.action_adminLogin_to_adminActivity);
            Log.d(TAG, "Navigation to AdminActivity successful at 08:44 PM IST, Saturday, June 28, 2025");
        } catch (Exception e) {
            Log.e(TAG, "Navigation error at 08:44 PM IST, Saturday, June 28, 2025: ", e);
            showErrorDialog("Navigation Error", "Navigation error: " + e.getMessage());
        }
    }

    private void showErrorDialog(String title, String message) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.custom_dialog_error);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialog.findViewById(R.id.dialog_message);
        dialogTitle.setText(title);
        dialogMessage.setText(message);
        dialog.findViewById(R.id.dialog_button).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void applyScaleFadeAnimation(View view, long delay) {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.7f, 1f, 0.7f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(600);
        scaleAnimation.setStartOffset(delay);
        scaleAnimation.setInterpolator(new OvershootInterpolator());
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(600);
        alphaAnimation.setStartOffset(delay);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        view.startAnimation(animationSet);
    }

    private void applySlideUpAnimation(View view, long delay) {
        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation slideUp = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, 100f,
                Animation.ABSOLUTE, 0f
        );
        slideUp.setDuration(600);
        slideUp.setStartOffset(delay);
        slideUp.setInterpolator(new AccelerateDecelerateInterpolator());
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(600);
        alphaAnimation.setStartOffset(delay);
        animationSet.addAnimation(slideUp);
        animationSet.addAnimation(alphaAnimation);
        view.startAnimation(animationSet);
    }

    private void applySlideInAnimation(View view, long delay) {
        TranslateAnimation slideIn = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f
        );
        slideIn.setDuration(500);
        slideIn.setStartOffset(delay);
        slideIn.setInterpolator(new OvershootInterpolator());
        view.startAnimation(slideIn);
    }

    private void applyBounceAnimation(View view, long delay) {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.7f, 1f, 0.7f, 1f,
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

    private void applyButtonPressAnimation(View view) {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation press = new ScaleAnimation(
                1f, 0.95f, 1f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        press.setDuration(100);
        ScaleAnimation release = new ScaleAnimation(
                0.95f, 1f, 0.95f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        release.setDuration(100);
        release.setStartOffset(100);
        release.setInterpolator(new OvershootInterpolator());
        animationSet.addAnimation(press);
        animationSet.addAnimation(release);
        view.startAnimation(animationSet);
    }

    private void applyHoverAnimation(View view) {
        ScaleAnimation hover = new ScaleAnimation(
                1f, 1.1f, 1f, 1.1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        hover.setDuration(200);
        hover.setRepeatMode(Animation.REVERSE);
        hover.setRepeatCount(1);
        view.startAnimation(hover);
    }
}
