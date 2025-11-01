package com.school.edutrack.ui.teacher;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.school.edutrack.R;
import com.school.edutrack.model.Teacher;
import com.school.edutrack.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Random;

public class TeacherProfileManagementFragment extends Fragment {

    private static final String TAG = "TeacherProfileFragment";
    private String teacherId;
    private TextView teacherName, teacherIdDisplay, teacherEmail, teacherClassSection, teacherSubject,
            teacherDoj, teacherIsClassTeacher, teacherPhone, teacherAddress, errorMessage;
    private ImageView qrCodeImageView, profileImage;
    private ProgressBar loadingProgress;
    private CardView profileCard;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            teacherId = getArguments().getString("teacherId");
            Log.d(TAG, "Received teacherId: " + teacherId + " on 2025-06-15 12:58 PM IST");
        }
        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("EduTrackPrefs", requireActivity().MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Inflating layout: fragment_teacher_profile_management on 2025-06-15 12:58 PM IST");
        return inflater.inflate(R.layout.fragment_teacher_profile_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        teacherName = view.findViewById(R.id.teacher_name);
        teacherIdDisplay = view.findViewById(R.id.teacher_id);
        teacherEmail = view.findViewById(R.id.teacher_email);
        teacherClassSection = view.findViewById(R.id.teacher_class_section);
        teacherSubject = view.findViewById(R.id.teacher_subject);
        teacherDoj = view.findViewById(R.id.teacher_doj);
        teacherIsClassTeacher = view.findViewById(R.id.teacher_is_class_teacher);
        teacherPhone = view.findViewById(R.id.teacher_phone);
        teacherAddress = view.findViewById(R.id.teacher_address);
        qrCodeImageView = view.findViewById(R.id.qr_code_image);
        profileImage = view.findViewById(R.id.teacher_profile_image);
        loadingProgress = view.findViewById(R.id.loading_progress);
        errorMessage = view.findViewById(R.id.error_message);
        profileCard = view.findViewById(R.id.profile_card);

        // Fetch teacher profile if teacherId is available
        if (teacherId != null) {
            fetchTeacherProfile();
        } else {
            showError("Teacher ID is missing");
        }
    }

    private void fetchTeacherProfile() {
        // Show loading state
        if (loadingProgress != null) loadingProgress.setVisibility(View.VISIBLE);
        if (profileCard != null) profileCard.setVisibility(View.GONE);
        if (errorMessage != null) errorMessage.setVisibility(View.GONE);

        Log.d(TAG, "Fetching teacher details for teacherId: " + teacherId + " on 2025-06-15 12:58 PM IST");

        RetrofitClient.getApiService().getTeacher(teacherId).enqueue(new Callback<Teacher>() {
            @Override
            public void onResponse(Call<Teacher> call, Response<Teacher> response) {
                if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Teacher teacher = response.body();
                    updateUI(teacher);
                    generateQrCode(teacherId);
                    if (profileCard != null) profileCard.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Teacher profile fetched on 2025-06-15 12:58 PM IST: " + new Gson().toJson(teacher));
                } else {
                    String errorMsg = "Failed to fetch teacher profile: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ", " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading errorBody on 2025-06-15 12:58 PM IST", e);
                        }
                    }
                    showError(errorMsg);
                    Log.e(TAG, errorMsg + " on 2025-06-15 12:58 PM IST");
                }
            }

            @Override
            public void onFailure(Call<Teacher> call, Throwable t) {
                if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                String errorMsg = "Network error: " + t.getMessage();
                showError(errorMsg);
                Log.e(TAG, "Fetch error on 2025-06-15 12:58 PM IST: " + errorMsg, t);
            }
        });
    }

    private void updateUI(Teacher teacher) {
        if (teacherName != null) teacherName.setText(teacher.getName() != null ? teacher.getName() : "N/A");
        if (teacherIdDisplay != null) teacherIdDisplay.setText("Teacher ID: " + (teacherId != null ? teacherId : "N/A"));
        if (teacherEmail != null) teacherEmail.setText(teacher.getEmail() != null ? teacher.getEmail() : "N/A");
        if (teacherClassSection != null) teacherClassSection.setText(teacher.getClass_() != null && teacher.getSection() != null ? teacher.getClass_() + "-" + teacher.getSection() : "N/A");
        if (teacherSubject != null) teacherSubject.setText(teacher.getSubject() != null ? teacher.getSubject() : "N/A");
        if (teacherDoj != null) teacherDoj.setText(teacher.getDoj() != null ? teacher.getDoj() : "N/A");
        if (teacherIsClassTeacher != null) teacherIsClassTeacher.setText(teacher.getIsClassTeacherForUI() != null ? teacher.getIsClassTeacherForUI() : "N/A");
        if (teacherPhone != null) teacherPhone.setText(teacher.getMobileNumber() != null ? teacher.getMobileNumber() : "N/A");
        if (teacherAddress != null) teacherAddress.setText(teacher.getAddress() != null ? teacher.getAddress() : "N/A");

        // Update profile image based on gender, validating saved image
        if (profileImage != null) {
            String gender = teacher.getGender() != null ? teacher.getGender() : "Male"; // Default to Male if gender is null
            int imageResource;

            // Define keys scoped to teacherId
            String imageResKey = "teacher_profile_image_res_" + teacherId;
            String genderKey = "teacher_profile_gender_" + teacherId;

            // Check if an image resource is saved and if it matches the current gender
            String savedGender = sharedPreferences.getString(genderKey, "");
            boolean useSavedImage = sharedPreferences.contains(imageResKey) && gender.equalsIgnoreCase(savedGender);

            if (useSavedImage) {
                imageResource = sharedPreferences.getInt(imageResKey, R.drawable.ic_maleteacher1);
                Log.d(TAG, "Using saved profile image for teacherId " + teacherId + ", gender " + gender + ", resource ID: " + imageResource + " on 2025-06-15 12:58 PM IST");
            } else {
                // No valid saved image, select a new random one based on gender and save it
                Random random = new Random();
                int randomIndex = random.nextInt(4) + 1; // Random number between 1 and 4

                if ("Female".equalsIgnoreCase(gender)) {
                    switch (randomIndex) {
                        case 1:
                            imageResource = R.drawable.ic_femaleteacher1;
                            break;
                        case 2:
                            imageResource = R.drawable.ic_femaleteacher2;
                            break;
                        case 3:
                            imageResource = R.drawable.ic_femaleteacher3;
                            break;
                        case 4:
                        default:
                            imageResource = R.drawable.ic_femaleteacher4;
                            break;
                    }
                } else {
                    // Default to male if gender is "Male" or invalid
                    switch (randomIndex) {
                        case 1:
                            imageResource = R.drawable.ic_maleteacher1;
                            break;
                        case 2:
                            imageResource = R.drawable.ic_maleteacher2;
                            break;
                        case 3:
                            imageResource = R.drawable.ic_maleteacher3;
                            break;
                        case 4:
                        default:
                            imageResource = R.drawable.ic_maleteacher4;
                            break;
                    }
                }
                // Save the selected image resource and gender to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(imageResKey, imageResource);
                editor.putString(genderKey, gender);
                editor.apply();
                Log.d(TAG, "Selected and saved new profile image for teacherId " + teacherId + ", gender " + gender + " to ic_" + (gender.equalsIgnoreCase("Female") ? "femaleteacher" : "maleteacher") + randomIndex + " on 2025-06-15 12:58 PM IST");
            }
            profileImage.setImageResource(imageResource);
            profileImage.setColorFilter(null); // Ensure no tint is applied
        }
    }

    private void generateQrCode(String teacherId) {
        if (teacherId == null || teacherId.isEmpty()) {
            Log.e(TAG, "Teacher ID is null or empty, cannot generate QR code on 2025-06-15 12:58 PM IST");
            if (qrCodeImageView != null) qrCodeImageView.setImageDrawable(null);
            return;
        }

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(teacherId, BarcodeFormat.QR_CODE, 350, 350);
            if (qrCodeImageView != null) {
                qrCodeImageView.setImageBitmap(bitmap);
                Log.d(TAG, "QR code generated and set for teacherId: " + teacherId + " on 2025-06-15 12:58 PM IST");
            }
        } catch (WriterException e) {
            Log.e(TAG, "Failed to generate QR code: " + e.getMessage() + " on 2025-06-15 12:58 PM IST", e);
            if (qrCodeImageView != null) qrCodeImageView.setImageDrawable(null);
        }
    }

    private void showError(String message) {
        if (errorMessage != null) {
            errorMessage.setText(message);
            errorMessage.setVisibility(View.VISIBLE);
        }
        if (profileCard != null) profileCard.setVisibility(View.GONE);
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}