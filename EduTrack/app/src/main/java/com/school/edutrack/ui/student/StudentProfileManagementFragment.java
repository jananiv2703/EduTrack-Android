package com.school.edutrack.ui.student;

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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.school.edutrack.R;
import com.school.edutrack.model.Student;
import com.school.edutrack.network.RetrofitClient;
import com.school.edutrack.network.StudentApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentProfileManagementFragment extends Fragment {

    private static final String TAG = "StudentProfileFragment";
    private String studentId;
    private TextView studentName, registerNumber, email, className, section, dob, gender, address, phoneNumber,
            fatherName, fatherContact, dateOfJoining, admissionNumber, rollNumber, errorMessage;
    private ImageView profileImageView, qrCodeImageView; // Added ImageView for QR code
    private ProgressBar loadingProgress;
    private CardView profileCard;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            studentId = getArguments().getString("studentId");
            Log.d(TAG, "Received studentId: " + studentId);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Inflating layout: fragment_student_profile_management");
        View root = inflater.inflate(R.layout.fragment_student_profile_management, container, false);

        // Initialize UI elements
        studentName = root.findViewById(R.id.student_name);
        registerNumber = root.findViewById(R.id.register_number);
        email = root.findViewById(R.id.email);
        className = root.findViewById(R.id.class_name);
        section = root.findViewById(R.id.section);
        dob = root.findViewById(R.id.dob);
        gender = root.findViewById(R.id.gender);
        address = root.findViewById(R.id.address);
        phoneNumber = root.findViewById(R.id.phone_number);
        fatherName = root.findViewById(R.id.father_name);
        fatherContact = root.findViewById(R.id.father_contact);
        dateOfJoining = root.findViewById(R.id.date_of_joining);
        admissionNumber = root.findViewById(R.id.admission_number);
        rollNumber = root.findViewById(R.id.roll_number);
        profileImageView = root.findViewById(R.id.studentProfileImageView);
        qrCodeImageView = root.findViewById(R.id.qr_code_image); // Initialize QR code ImageView
        loadingProgress = root.findViewById(R.id.loading_progress);
        errorMessage = root.findViewById(R.id.error_message);
        profileCard = root.findViewById(R.id.profile_card);

        // Fetch student details if studentId is available
        if (studentId != null) {
            fetchStudentDetails(studentId);
        } else {
            showError("Student ID is missing");
        }

        return root;
    }

    private void fetchStudentDetails(String studentId) {
        // Show loading state
        if (loadingProgress != null) loadingProgress.setVisibility(View.VISIBLE);
        if (profileCard != null) profileCard.setVisibility(View.GONE);
        if (errorMessage != null) errorMessage.setVisibility(View.GONE);

        // Log the API call
        Log.d(TAG, "Fetching student details for studentId: " + studentId);

        // Make API call using RetrofitClient
        StudentApiService apiService = RetrofitClient.getStudentApiService();
        Call<Student> call = apiService.getStudentDetails(studentId);
        call.enqueue(new Callback<Student>() {
            @Override
            public void onResponse(Call<Student> call, Response<Student> response) {
                if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                Log.d(TAG, "API Response Code: " + response.code());
                Log.d(TAG, "API Response Body: " + (response.body() != null ? response.body().toString() : "null"));

                if (response.isSuccessful() && response.body() != null) {
                    Student student = response.body();
                    Log.d(TAG, "Student Data: Name=" + student.getName() + ", Email=" + student.getEmail());
                    updateUI(student);
                    generateQrCode(student.getRegister_no()); // Generate QR code with register_no
                    if (profileCard != null) profileCard.setVisibility(View.VISIBLE);
                } else {
                    String errorMsg = "Failed to load student details: " + response.message();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += "\nError Body: " + response.errorBody().string();
                        } catch (Exception e) {
                            errorMsg += "\nError Body: Unable to parse";
                        }
                    }
                    showError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Student> call, Throwable t) {
                if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                showError(errorMsg);
            }
        });
    }

    private void updateUI(Student student) {
        if (studentName != null) studentName.setText(student.getName() != null ? student.getName() : "N/A");
        if (registerNumber != null) registerNumber.setText("Register Number: " + (student.getStudent_id() != null ? student.getStudent_id() : "N/A"));
        if (email != null) email.setText(student.getEmail() != null ? student.getEmail() : "N/A");
        if (className != null) className.setText(student.getClass_name() != null ? student.getClass_name() : "N/A");
        if (section != null) section.setText(student.getSection() != null ? student.getSection() : "N/A");
        if (dob != null) dob.setText(student.getDob() != null ? student.getDob() : "N/A");
        if (gender != null) gender.setText(student.getGender() != null ? student.getGender() : "N/A");
        if (address != null) address.setText(student.getAddress() != null ? student.getAddress() : "N/A");
        if (phoneNumber != null) phoneNumber.setText(student.getPhone() != null ? student.getPhone() : "N/A");
        if (fatherName != null) fatherName.setText(student.getParent_name() != null ? student.getParent_name() : "N/A");
        if (fatherContact != null) fatherContact.setText(student.getParent_contact() != null ? student.getParent_contact() : "N/A");
        if (dateOfJoining != null) dateOfJoining.setText(student.getAdmission_date() != null ? student.getAdmission_date() : "N/A");
        if (admissionNumber != null) admissionNumber.setText(student.getAdmission_no() != null ? student.getAdmission_no() : "N/A");
        if (rollNumber != null) rollNumber.setText(student.getRegister_no() != null ? student.getRegister_no() : "N/A");

        // Update profile image based on gender
        if (profileImageView != null) {
            if ("Female".equalsIgnoreCase(student.getGender())) {
                profileImageView.setImageResource(R.drawable.ic_femalestudent);
            } else {
                profileImageView.setImageResource(R.drawable.ic_malestudent);
            }
        }
    }

    private void generateQrCode(String registerNo) {
        if (registerNo == null || registerNo.isEmpty()) {
            Log.e(TAG, "Register number is null or empty, cannot generate QR code");
            if (qrCodeImageView != null) qrCodeImageView.setImageDrawable(null);
            return;
        }

        try {
            // Encode the register_no into a QR code
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(registerNo, BarcodeFormat.QR_CODE, 350, 350);
            if (qrCodeImageView != null) {
                qrCodeImageView.setImageBitmap(bitmap);
                Log.d(TAG, "QR code generated and set for register_no: " + registerNo);
            }
        } catch (WriterException e) {
            Log.e(TAG, "Failed to generate QR code: " + e.getMessage(), e);
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