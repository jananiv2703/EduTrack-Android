package com.school.edutrack.network;

import com.school.edutrack.model.Announcement;
import com.school.edutrack.model.ApiResponse;
import com.school.edutrack.model.LearningMaterialsResponse;
import com.school.edutrack.model.Student;
import com.school.edutrack.model.StudentAuthRequest;
import com.school.edutrack.model.StudentLogin;
import com.school.edutrack.model.AttendanceResponse;
import com.school.edutrack.model.StudentQuizModels;
import com.school.edutrack.model.StudentTimetableResponse;
import com.school.edutrack.model.StudentExamScoresResponse;
import com.school.edutrack.model.StudentExamSchedulesResponse;
import com.school.edutrack.model.StudentLeaveRequest;
import com.school.edutrack.model.StudentLeaveResponse;
import com.school.edutrack.model.FeePaymentResponse;
import com.school.edutrack.model.FeePaymentRequest;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface StudentApiService {

    @POST("student/student_login.php")
    Call<StudentLogin> loginStudent(@Body StudentAuthRequest request);

    @POST("student/set_student_pin.php")
    Call<StudentLogin> setStudentPin(@Body StudentAuthRequest request);

    @POST("student/student_pin_login.php")
    Call<StudentLogin> loginStudentWithPin(@Body StudentAuthRequest request);

    @GET("student/students_api.php") // Updated to match the new folder
    Call<Student> getStudentDetails(@Query("student_id") String studentId);
    @GET("student/attendance_api.php")
    Call<AttendanceResponse> getStudentAttendance(@Query("student_id") String studentId);

    @GET("admin/announcement_api.php?action=get")
    Call<List<Announcement>> getAnnouncements();
    @GET("student/gettimetable.php")
    Call<StudentTimetableResponse> getTimetable(@Query("student_id") String studentId);

    @GET("student/getmarks.php")
    Call<StudentExamScoresResponse> getStudentMarks(@Query("action") String action, @Query("student_id") String studentId);

    @GET("student/getmarks.php")
    Call<StudentExamSchedulesResponse> getExamSchedules(@Query("action") String action);

    // Quiz-related endpoints using consolidated models
    @GET("student/quiz_api.php")
    Call<StudentQuizModels.StudentQuizDetailsResponse> getStudentQuizDetails(@Query("action") String action, @Query("student_id") String studentId);

    @GET("student/quiz_api.php")
    Call<StudentQuizModels.StudentQuizzesResponse> getStudentQuizzes(@Query("action") String action, @Query("student_id") String studentId, @Query("class") String studentClass, @Query("section") String studentSection);

    @POST("student/quiz_api.php")
    Call<StudentQuizModels.StudentQuizSubmissionResponse> submitStudentQuiz(@Query("action") String action, @Body StudentQuizModels.StudentQuizSubmissionRequest request);

    @GET("student/quiz_api.php")
    Call<StudentQuizModels.StudentSubmittedResponse> getSubmittedResponse(@Query("action") String action, @Query("student_reg_number") String studentRegNumber, @Query("quiz_name") String quizName);

    // New methods for leave management
    @GET("teacher/leave_request_api.php")
    Call<StudentLeaveRequest> getLeaveHistory(@Query("student_id") String studentId);

    @Multipart
    @POST("teacher/leave_request_api.php")
    Call<StudentLeaveRequest> submitLeaveRequest(
            @Part("data") RequestBody data,
            @Part MultipartBody.Part proof
    );

    @GET("teacher/learning_materials.php")
    Call<LearningMaterialsResponse> getStudentLearningMaterials(
            @Query("studentId") String studentId,
            @Query("class") String className,
            @Query("section") String section
    );

    @GET("admin/fee_payments.php")
    Call<FeePaymentResponse> getFeePayments(@Query("student_id") String studentId);

    @POST("admin/fee_payments.php")
    Call<FeePaymentResponse.ApiResponse> addFeePayment(@Body FeePaymentRequest request);

}