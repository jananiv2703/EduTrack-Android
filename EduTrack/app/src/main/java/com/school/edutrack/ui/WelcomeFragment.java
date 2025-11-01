package com.school.edutrack.ui;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.school.edutrack.R;

public class WelcomeFragment extends Fragment {

    private static final String TAG = "WelcomeFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        TextView tagline = view.findViewById(R.id.textViewTagline);
        ImageButton adminButton = view.findViewById(R.id.adminButton);
        ImageButton studentButton = view.findViewById(R.id.studentButton);
        ImageButton teacherButton = view.findViewById(R.id.teacherButton);
        ImageView wave1 = view.findViewById(R.id.waveBackground1);
        ImageView wave2 = view.findViewById(R.id.waveBackground2);
        ImageView wave3 = view.findViewById(R.id.waveBackground3);
        ImageView wave4 = view.findViewById(R.id.waveBackground4);
        ImageView wave5 = view.findViewById(R.id.waveBackground5);

        // Fade-In Animation for Tagline with Glow
        AnimationSet taglineAnimation = new AnimationSet(true);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);
        taglineAnimation.addAnimation(fadeIn);

        // Subtle glow effect for tagline
        ObjectAnimator glow = ObjectAnimator.ofFloat(tagline, "alpha", 1f, 0.8f, 1f);
        glow.setDuration(3000);
        glow.setRepeatCount(ObjectAnimator.INFINITE);
        glow.setRepeatMode(ObjectAnimator.REVERSE);
        glow.start();

        tagline.startAnimation(taglineAnimation);

        // Scale Animation for Buttons (Staggered)
        applyScaleAnimation(adminButton, 0);
        applyScaleAnimation(studentButton, 200);
        applyScaleAnimation(teacherButton, 400);

        // Animate Waves with increased realism
        animateWave(wave1, 0, 30f, 2000, 10f, 1500);   // Gentle wave
        animateWave(wave2, 200, 60f, 3500, 25f, 2000);  // Realistic wave with deeper motion
        animateWave(wave3, 400, 80f, 4000, 35f, 2500);  // More pronounced wave
        animateWave(wave4, 600, 50f, 3000, 20f, 1800);  // Softer overlapping wave
        animateWave(wave5, 800, 70f, 4500, 30f, 2200);  // Deep wave

        // Navigation
        NavController navController = Navigation.findNavController(view);
        adminButton.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to Admin Login at 2025-06-24 07:13 AM IST");
            navController.navigate(R.id.action_welcome_to_adminLogin);
        });
        studentButton.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to Student Login at 2025-06-24 07:13 AM IST");
            navController.navigate(R.id.action_welcome_to_studentLogin);
        });
        teacherButton.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to Teacher Login at 2025-06-24 07:13 AM IST");
            navController.navigate(R.id.action_welcomeFragment_to_teacherLoginFragment);
        });
    }

    private void applyScaleAnimation(View view, long delay) {
        AnimationSet animationSet = new AnimationSet(true);

        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.5f, 1f,
                0.5f, 1f,
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

    private void animateWave(View waveView, long delay, float horizontalAmplitude, long horizontalDuration,
                             float verticalAmplitude, long verticalDuration) {
        // Slide down animation
        TranslateAnimation slideDown = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, -waveView.getHeight(),
                Animation.ABSOLUTE, 0f
        );
        slideDown.setDuration(1500);
        slideDown.setStartOffset(delay);
        slideDown.setFillAfter(true);

        // Horizontal wave-like oscillation
        ObjectAnimator horizontalWave = ObjectAnimator.ofFloat(waveView, "translationX", -horizontalAmplitude, horizontalAmplitude);
        horizontalWave.setDuration(horizontalDuration);
        horizontalWave.setStartDelay(delay);
        horizontalWave.setRepeatCount(ObjectAnimator.INFINITE);
        horizontalWave.setRepeatMode(ObjectAnimator.REVERSE);

        // Vertical undulation for realism
        ObjectAnimator verticalWave = ObjectAnimator.ofFloat(waveView, "translationY", -verticalAmplitude, verticalAmplitude);
        verticalWave.setDuration(verticalDuration);
        verticalWave.setStartDelay(delay);
        verticalWave.setRepeatCount(ObjectAnimator.INFINITE);
        verticalWave.setRepeatMode(ObjectAnimator.REVERSE);

        // Start animations
        waveView.startAnimation(slideDown);
        horizontalWave.start();
        verticalWave.start();
    }
}