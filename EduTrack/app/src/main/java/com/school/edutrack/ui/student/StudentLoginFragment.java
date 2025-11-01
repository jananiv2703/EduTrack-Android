package com.school.edutrack.ui.student;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.school.edutrack.R;
import com.school.edutrack.model.StudentAuthRequest;
import com.school.edutrack.model.StudentLogin;
import com.school.edutrack.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Random;

public class StudentLoginFragment extends Fragment {

    private static final String TAG = "StudentLoginFragment";
    private TextInputLayout studentIdInputLayout, passwordInputLayout;
    private TextInputEditText studentIdEditText, passwordEditText;
    private EditText[] pinDigits;
    private MaterialButton loginButton;
    private RadioGroup loginModeRadioGroup;
    private RadioButton passwordLoginRadio, pinLoginRadio;
    private TextView forgotPinTextView, backButton;
    private SharedPreferences sharedPreferences;
    private boolean isSettingPin = false;
    private NavController navController;
    private ViewFlipper loginModeFlipper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("EduTrackPrefs", Context.MODE_PRIVATE);

        // Initialize NavController
        navController = Navigation.findNavController(view);
        Log.d(TAG, "NavController initialized in onViewCreated on 2025-06-28 07:06 PM IST");

        // Initialize Views
        TextView titleTextView = view.findViewById(R.id.titleTextView);
        TextView taglineTextView = view.findViewById(R.id.taglineTextView);
        ImageView edutrackLogo = view.findViewById(R.id.edutrackLogo);
        loginModeRadioGroup = view.findViewById(R.id.loginModeRadioGroup);
        passwordLoginRadio = view.findViewById(R.id.passwordLoginRadio);
        pinLoginRadio = view.findViewById(R.id.pinLoginRadio);
        studentIdInputLayout = view.findViewById(R.id.studentIdInputLayout);
        studentIdEditText = view.findViewById(R.id.studentIdEditText);
        passwordInputLayout = view.findViewById(R.id.passwordInputLayout);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        pinDigits = new EditText[]{
                view.findViewById(R.id.pinDigit1),
                view.findViewById(R.id.pinDigit2),
                view.findViewById(R.id.pinDigit3),
                view.findViewById(R.id.pinDigit4),
                view.findViewById(R.id.pinDigit5),
                view.findViewById(R.id.pinDigit6)
        };
        loginButton = view.findViewById(R.id.loginButton);
        forgotPinTextView = view.findViewById(R.id.forgotPinTextView);
        backButton = view.findViewById(R.id.backButton);
        loginModeFlipper = view.findViewById(R.id.loginModeFlipper);

        // Null checks
        if (titleTextView == null || taglineTextView == null || edutrackLogo == null ||
                loginModeRadioGroup == null || passwordLoginRadio == null || pinLoginRadio == null ||
                studentIdInputLayout == null || studentIdEditText == null ||
                passwordInputLayout == null || passwordEditText == null ||
                pinDigits[0] == null || pinDigits[1] == null || pinDigits[2] == null ||
                pinDigits[3] == null || pinDigits[4] == null || pinDigits[5] == null ||
                loginButton == null || forgotPinTextView == null || backButton == null ||
                loginModeFlipper == null) {
            Log.e(TAG, "UI components not found on 2025-06-28 07:06 PM IST");
            showErrorDialog("Error", "UI components not found");
            return;
        }

        // Apply animations
        applyFadeInAnimation(titleTextView, 0);
        applyFadeInAnimation(taglineTextView, 200);
        applyBounceAnimation(edutrackLogo, 400);
        applyScaleAnimation(loginModeRadioGroup, 600);
        applyScaleAnimation(studentIdInputLayout, 800);
        applyScaleAnimation(loginModeFlipper, 1000);
        applyScaleAnimation(loginButton, 1200);
        applyFadeInAnimation(forgotPinTextView, 1400);
        applyFadeInAnimation(backButton, 1600);

        // Set ViewFlipper animations
        loginModeFlipper.setInAnimation(getContext(), android.R.anim.slide_in_left);
        loginModeFlipper.setOutAnimation(getContext(), android.R.anim.slide_out_right);

        // Load saved student ID and check PIN status
        String savedStudentId = sharedPreferences.getString("student_id", "");
        if (!savedStudentId.isEmpty()) {
            studentIdEditText.setText(savedStudentId);
            boolean isPinSet = sharedPreferences.getBoolean("pin_set", false);
            if (isPinSet) {
                pinLoginRadio.setChecked(true);
                toggleLoginMode(true);
                Log.d(TAG, "PIN is set, switched to PIN login mode on 2025-06-28 07:06 PM IST");
            } else {
                passwordLoginRadio.setChecked(true);
                toggleLoginMode(false);
                Log.d(TAG, "PIN not set, defaulting to password login mode on 2025-06-28 07:06 PM IST");
            }
        }

        // Setup PIN input focus handling
        setupPinInput();

        // Login mode toggle
        loginModeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isPinMode = checkedId == R.id.pinLoginRadio;
            toggleLoginMode(isPinMode);
            Log.d(TAG, "Login mode toggled to on 2025-06-28 07:06 PM IST: " + (isPinMode ? "PIN" : "Password"));
        });

        // Login Button Logic
        loginButton.setOnClickListener(v -> {
            String studentId = studentIdEditText.getText() != null ? studentIdEditText.getText().toString().trim() : "";
            if (studentId.isEmpty()) {
                showErrorDialog("Validation Error", "Please enter Student ID");
                Log.w(TAG, "Student ID is empty on 2025-06-28 07:06 PM IST");
                return;
            }

            if (isSettingPin) {
                String pin = getPinInput();
                if (pin.length() != 6) {
                    showErrorDialog("Validation Error", "Please enter a 6-digit PIN");
                    Log.w(TAG, "Invalid PIN length on 2025-06-28 07:06 PM IST: " + pin.length());
                    return;
                }
                setPin(studentId, pin);
            } else if (pinLoginRadio.isChecked()) {
                String pin = getPinInput();
                if (pin.length() != 6) {
                    showErrorDialog("Validation Error", "Please enter a 6-digit PIN");
                    Log.w(TAG, "Invalid PIN length on 2025-06-28 07:06 PM IST: " + pin.length());
                    return;
                }
                loginWithPin(studentId, pin);
            } else {
                String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";
                if (password.isEmpty()) {
                    showErrorDialog("Validation Error", "Please enter password");
                    Log.w(TAG, "Password is empty on 2025-06-28 07:06 PM IST");
                    return;
                }
                loginWithPassword(studentId, password);
            }
        });

        // Forgot PIN/Password
        forgotPinTextView.setOnClickListener(v -> {
            showErrorDialog("Info", "Forgot PIN/Password functionality to be implemented");
            Log.d(TAG, "Forgot PIN/Password clicked on 2025-06-28 07:06 PM IST");
        });

        // Back to WelcomeFragment
        backButton.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_studentLogin_to_welcomeFragment);
                Log.d(TAG, "Navigated back to WelcomeFragment on 2025-06-28 07:06 PM IST");
            } catch (Exception e) {
                Log.e(TAG, "Back navigation error on 2025-06-28 07:06 PM IST: ", e);
                showErrorDialog("Navigation Error", "Back navigation error: " + e.getMessage());
            }
        });
    }

    private void setupPinInput() {
        for (int i = 0; i < pinDigits.length; i++) {
            final int index = i;
            pinDigits[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < pinDigits.length - 1) {
                        pinDigits[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        pinDigits[index - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private String getPinInput() {
        StringBuilder pin = new StringBuilder();
        for (EditText digit : pinDigits) {
            pin.append(digit.getText() != null ? digit.getText().toString() : "");
        }
        return pin.toString();
    }

    private void clearPinInput() {
        for (EditText digit : pinDigits) {
            digit.setText("");
        }
        pinDigits[0].requestFocus();
    }

    private void loginWithPassword(String studentId, String password) {
        StudentAuthRequest request = new StudentAuthRequest(studentId, password);
        RetrofitClient.getStudentApiService().loginStudent(request).enqueue(new Callback<StudentLogin>() {
            @Override
            public void onResponse(Call<StudentLogin> call, Response<StudentLogin> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StudentLogin loginResponse = response.body();
                    Log.d(TAG, "Password login response on 2025-06-28 07:06 PM IST: " + loginResponse.getMessage());
                    if ("Login successful".equals(loginResponse.getMessage())) {
                        // Save student details
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("student_id", studentId);
                        editor.putString("user_name", loginResponse.getUser().getName());
                        editor.putString("user_email", loginResponse.getUser().getEmail());
                        // Set random profile image
                        int profileDrawableId = getRandomProfileImage();
                        editor.putInt("student_profile_drawable", profileDrawableId);
                        editor.apply();
                        Log.d(TAG, "Saved student_id on 2025-06-28 07:06 PM IST: " + studentId);
                        Log.d(TAG, "Saved user_name on 2025-06-28 07:06 PM IST: " + loginResponse.getUser().getName());
                        Log.d(TAG, "Saved user_email on 2025-06-28 07:06 PM IST: " + loginResponse.getUser().getEmail());
                        Log.d(TAG, "Set profile image drawable ID: " + profileDrawableId + " on 2025-06-28 07:06 PM IST");

                        if (!loginResponse.isPinSet()) {
                            // Prompt for PIN setup
                            loginModeFlipper.setDisplayedChild(1);
                            loginModeRadioGroup.setVisibility(View.GONE);
                            loginButton.setText("Set PIN");
                            isSettingPin = true;
                            showInfoDialog("PIN Setup", "Please set a 6-digit PIN");
                            Log.d(TAG, "Prompting for PIN setup on 2025-06-28 07:06 PM IST");
                        } else {
                            SharedPreferences.Editor editorPin = sharedPreferences.edit();
                            editorPin.putBoolean("pin_set", true);
                            editorPin.apply();
                            Log.d(TAG, "Set pin_set to true on 2025-06-28 07:06 PM IST");
                            showLoadingDialog(studentId);
                        }
                    } else {
                        showErrorDialog("Login Failed", loginResponse.getMessage());
                        Log.w(TAG, "Password login failed on 2025-06-28 07:06 PM IST: " + loginResponse.getMessage());
                    }
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<StudentLogin> call, Throwable t) {
                Log.e(TAG, "Password login error on 2025-06-28 07:06 PM IST: ", t);
                showErrorDialog("Error", "Login error: " + t.getMessage());
            }
        });
    }

    private void setPin(String studentId, String pin) {
        StudentAuthRequest request = new StudentAuthRequest(studentId, pin, true);
        RetrofitClient.getStudentApiService().setStudentPin(request).enqueue(new Callback<StudentLogin>() {
            @Override
            public void onResponse(Call<StudentLogin> call, Response<StudentLogin> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StudentLogin pinResponse = response.body();
                    Log.d(TAG, "Set PIN response on 2025-06-28 07:06 PM IST: " + pinResponse.getMessage());
                    if ("PIN set successfully".equals(pinResponse.getMessage())) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("pin_set", true);
                        editor.apply();
                        Log.d(TAG, "Set pin_set to true on 2025-06-28 07:06 PM IST");
                        showLoadingDialog(studentId);
                    } else {
                        showErrorDialog("PIN Setup Failed", pinResponse.getMessage());
                        Log.w(TAG, "Set PIN failed on 2025-06-28 07:06 PM IST: " + pinResponse.getMessage());
                        clearPinInput();
                    }
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<StudentLogin> call, Throwable t) {
                Log.e(TAG, "Set PIN error on 2025-06-28 07:06 PM IST: ", t);
                showErrorDialog("Error", "Set PIN error: " + t.getMessage());
                clearPinInput();
            }
        });
    }

    private void loginWithPin(String studentId, String pin) {
        StudentAuthRequest request = new StudentAuthRequest(studentId, pin, true);
        RetrofitClient.getStudentApiService().loginStudentWithPin(request).enqueue(new Callback<StudentLogin>() {
            @Override
            public void onResponse(Call<StudentLogin> call, Response<StudentLogin> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StudentLogin pinResponse = response.body();
                    Log.d(TAG, "PIN login response on 2025-06-28 07:06 PM IST: " + pinResponse.getMessage());
                    if ("PIN login successful".equals(pinResponse.getMessage())) {
                        // Save user details
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("student_id", studentId);
                        editor.putString("user_name", pinResponse.getUser().getName());
                        editor.putString("user_email", pinResponse.getUser().getEmail());
                        // Set random profile image
                        int profileDrawableId = getRandomProfileImage();
                        editor.putInt("student_profile_drawable", profileDrawableId);
                        editor.putBoolean("pin_set", true);
                        editor.apply();
                        Log.d(TAG, "Saved student_id on 2025-06-28 07:06 PM IST: " + studentId);
                        Log.d(TAG, "Saved user_name on 2025-06-28 07:06 PM IST: " + pinResponse.getUser().getName());
                        Log.d(TAG, "Saved user_email on 2025-06-28 07:06 PM IST: " + pinResponse.getUser().getEmail());
                        Log.d(TAG, "Set profile image drawable ID: " + profileDrawableId + " on 2025-06-28 07:06 PM IST");
                        showLoadingDialog(studentId);
                    } else {
                        showErrorDialog("PIN Login Failed", pinResponse.getMessage());
                        Log.w(TAG, "PIN login failed on 2025-06-28 07:06 PM IST: " + pinResponse.getMessage());
                        clearPinInput();
                    }
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<StudentLogin> call, Throwable t) {
                Log.e(TAG, "PIN login error on 2025-06-28 07:06 PM IST: ", t);
                showErrorDialog("Error", "PIN login error: " + t.getMessage());
                clearPinInput();
            }
        });
    }

    private void toggleLoginMode(boolean isPinMode) {
        isSettingPin = false;
        loginButton.setText("Login");
        loginModeRadioGroup.setVisibility(View.VISIBLE);
        loginModeFlipper.setDisplayedChild(isPinMode ? 1 : 0);
        applyScaleAnimation(loginModeFlipper.getCurrentView(), 200);
        Log.d(TAG, "Toggled login mode UI to on 2025-06-28 07:06 PM IST: " + (isPinMode ? "PIN" : "Password"));
    }

    private int getRandomProfileImage() {
        String gender = sharedPreferences.getString("gender", "male");
        Random random = new Random();
        int[] maleImages = {R.drawable.ic_malestudent1, R.drawable.ic_malestudent2, R.drawable.ic_malestudent3};
        int[] femaleImages = {R.drawable.ic_femalestudent1, R.drawable.ic_femalestudent2, R.drawable.ic_femalestudent3};
        return "female".equalsIgnoreCase(gender) ?
                femaleImages[random.nextInt(femaleImages.length)] :
                maleImages[random.nextInt(maleImages.length)];
    }

    private void navigateToStudentActivity(String studentId) {
        try {
            Log.d(TAG, "Attempting to navigate to StudentActivity with studentId on 2025-06-28 07:06 PM IST: " + studentId);
            Bundle args = new Bundle();
            args.putString("studentId", studentId);
            navController.navigate(R.id.action_studentLogin_to_studentActivity, args);
            Log.d(TAG, "Navigation to StudentActivity successful on 2025-06-28 07:06 PM IST");
        } catch (Exception e) {
            Log.e(TAG, "Navigation error on 2025-06-28 07:06 PM IST: ", e);
            showErrorDialog("Navigation Error", "Navigation error: " + e.getMessage());
        }
    }

    private void handleErrorResponse(Response<?> response) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
            Log.e(TAG, "Request failed on 2025-06-28 07:06 PM IST: Response code " + response.code() + ", Error body: " + errorBody);
            showErrorDialog("Request Failed", "Request failed: Response code " + response.code());
        } catch (Exception e) {
            Log.e(TAG, "Error reading response on 2025-06-28 07:06 PM IST: ", e);
            showErrorDialog("Request Failed", "Request failed: Unable to parse response");
        }
        clearPinInput();
    }

    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showInfoDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showLoadingDialog(String studentId) {
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

        // Navigate after 2 seconds to allow GIF to play
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss();
            navigateToStudentActivity(studentId);
        }, 2000);
    }

    private void applyFadeInAnimation(View view, long delay) {
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.setStartOffset(delay);
        view.startAnimation(fadeIn);
    }

    private void applyScaleAnimation(View view, long delay) {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.8f, 1f, 0.8f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(500);
        scaleAnimation.setStartOffset(delay);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setStartOffset(delay);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        view.startAnimation(animationSet);
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
}
