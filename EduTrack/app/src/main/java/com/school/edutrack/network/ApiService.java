package com.school.edutrack.network;

import com.school.edutrack.model.Announcement;
import com.school.edutrack.model.ApiResponse;
import com.school.edutrack.model.Attendance;
import com.school.edutrack.model.AttendanceResponse;
import com.school.edutrack.model.ClassTeacherResponse;
import com.school.edutrack.model.ExamSchedule;
import com.school.edutrack.model.ExamScore;
import com.school.edutrack.model.LearningMaterial;
import com.school.edutrack.model.LearningMaterialsResponse;
import com.school.edutrack.model.LeaveRequest;
import com.school.edutrack.model.LeaveRequestResponse;
import com.school.edutrack.model.LoginResponse;
import com.school.edutrack.model.Teacher;
import com.school.edutrack.model.Student;
import com.school.edutrack.model.StudentResponse;
import com.school.edutrack.model.Timetable;
import com.school.edutrack.model.TeachersResponse; // Added import
import com.school.edutrack.model.TimetableResponse;
import com.school.edutrack.model.Quiz;
import com.school.edutrack.model.QuizResponse;
import com.school.edutrack.model.FeeResponse;
import com.school.edutrack.model.FeeStructureRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    class LoginRequest {
        private String username;
        private String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    class TeacherLoginRequest {
        private String teacher_id;
        private String password;

        public TeacherLoginRequest(String teacher_id, String password) {
            this.teacher_id = teacher_id;
            this.password = password;
        }
    }

    class TeacherLoginResponse {
        private String message;
        private boolean pin_set;
        private Teacher user;

        public String getMessage() {
            return message;
        }

        public boolean isPinSet() {
            return pin_set;
        }

        public Teacher getUser() {
            return user;
        }
    }

    class SetPinRequest {
        private String teacher_id;
        private String pin;

        public SetPinRequest(String teacher_id, String pin) {
            this.teacher_id = teacher_id;
            this.pin = pin;
        }
    }

    class PinResponse {
        private String message;

        public String getMessage() {
            return message;
        }
    }

    class PinLoginRequest {
        private String teacher_id;
        private String pin;

        public PinLoginRequest(String teacher_id, String pin) {
            this.teacher_id = teacher_id;
            this.pin = pin;
        }
    }

    class PinLoginResponse {
        private String message;
        private Teacher user;

        public String getMessage() {
            return message;
        }

        public Teacher getUser() {
            return user;
        }
    }

    class LeaveRequestUpdate {
        private int id;
        private String status;
        private String teacher_id;

        public LeaveRequestUpdate(int id, String status, String teacher_id) {
            this.id = id;
            this.status = status;
            this.teacher_id = teacher_id;
        }

        public int getId() {
            return id;
        }

        public String getStatus() {
            return status;
        }

        public String getTeacherId() {
            return teacher_id;
        }

        @Override
        public String toString() {
            return "LeaveRequestUpdate{" +
                    "id=" + id +
                    ", status='" + status + '\'' +
                    ", teacher_id='" + teacher_id + '\'' +
                    '}';
        }
    }

    @POST("admin/login.php")
    Call<LoginResponse> loginAdmin(@Body LoginRequest request);

    @POST("admin/TeacherManagement.php")
    Call<ApiResponse> addTeacher(@Body Teacher teacher);

    @GET("admin/TeacherManagement.php")
    Call<List<Teacher>> getTeachers();

    @GET("admin/TeacherManagement.php")
    Call<Teacher> getTeacher(@Query("teacher_id") String teacherId);

    @PUT("admin/TeacherManagement.php")
    Call<ApiResponse> updateTeacher(@Body Teacher teacher);

    @DELETE("admin/TeacherManagement.php")
    Call<ApiResponse> deleteTeacher(@Query("teacher_id") String teacherId);

    @GET("admin/announcement_api.php?action=get")
    Call<List<Announcement>> getAnnouncements();

    @Multipart
    @POST("admin/announcement_api.php?action=add")
    Call<ApiResponse> addAnnouncement(
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("role") RequestBody role,
            @Part MultipartBody.Part banner,
            @Part MultipartBody.Part file
    );

    @DELETE("admin/announcement_api.php?action=delete")
    Call<ApiResponse> deleteAnnouncement(@Query("id") int id);

    @Multipart
    @POST("admin/announcement_api.php?action=update")
    Call<ApiResponse> updateAnnouncement(
            @Part("id") RequestBody id,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("role") RequestBody role,
            @Part("existing_banner") RequestBody existingBanner,
            @Part("existing_file") RequestBody existingFile,
            @Part MultipartBody.Part banner,
            @Part MultipartBody.Part file
    );

    @POST("teacher/teacher_login_api.php")
    Call<TeacherLoginResponse> loginTeacher(@Body TeacherLoginRequest request);

    @POST("teacher/set_teacher_pin.php")
    Call<PinResponse> setTeacherPin(@Body SetPinRequest request);

    @POST("teacher/teacher_pin_login.php")
    Call<PinLoginResponse> pinLoginTeacher(@Body PinLoginRequest request);

    @POST("teacher/students_api.php")
    Call<StudentResponse> addStudent(@Body Student student);

    @GET("teacher/students_api.php")
    Call<List<Student>> getStudents();

    @GET("teacher/students_api.php")
    Call<Student> getStudentById(@Query("student_id") String studentId);

    @GET("teacher/students_api.php")
    Call<List<Student>> getStudentsByClassAndSection(@Query("class") String className, @Query("section") String section);

    @PUT("teacher/students_api.php")
    Call<StudentResponse> updateStudent(@Body Student student);

    @DELETE("teacher/students_api.php")
    Call<StudentResponse> deleteStudent(@Query("student_id") String studentId);

    @GET("teacher/is_a_classteacher.php")
    Call<ClassTeacherResponse> getClassTeacherData(@Query("teacher_id") String teacherId);

    @GET("teacher/exam_schedule.php")
    Call<List<ExamSchedule>> getExamSchedules();

    @POST("teacher/exam_schedule.php")
    Call<ApiResponse> addExamSchedule(@Body ExamSchedule examSchedule);

    @PUT("teacher/exam_schedule.php")
    Call<ApiResponse> updateExamSchedule(@Body ExamSchedule examSchedule);

    @DELETE("teacher/exam_schedule.php")
    Call<ApiResponse> deleteExamSchedule(@Query("id") String id);

    @GET("teacher/exam_scores.php")
    Call<List<ExamScore>> fetchScores(@Query("student_id") String studentId,
                                      @Query("class") String className,
                                      @Query("exam_name") String examName);

    @PUT("teacher/exam_scores.php")
    Call<ApiResponse> updateMarks(@Body ExamScore examScore);

    @DELETE("teacher/exam_scores.php")
    Call<ApiResponse> deleteScore(@Query("id") String id);
    @GET("teacher/attendance_marking.php")
    Call<StudentResponse> getStudents(
            @Query("class") String className,
            @Query("section") String section
    );

    @PUT("teacher/attendance_marking.php")
    Call<AttendanceResponse> updateAttendance(
            @Body List<Attendance> attendanceRecords
    );

    @GET("teacher/attendance_marking.php")
    Call<List<Attendance>> getAttendanceRecords(
            @Query("class") String className,
            @Query("section") String section,
            @Query("date") String date
    );

    @GET("teacher/leave_request_api.php")
    Call<LeaveRequestResponse> getLeaveRequests(
            @Query("teacher_id") String teacherId
    );

    @PUT("teacher/leave_request_api.php")
    Call<LeaveRequestResponse> updateLeaveRequestStatus(
            @Body LeaveRequestUpdate request
    );

    // Fetch Teachers Endpoint
    @GET("teacher/fetch_teachers.php")
    Call<TeachersResponse> fetchTeachers();

    // Timetable Endpoints
    @GET("teacher/timetable_api.php")
    Call<TimetableResponse> getTimetablesByClassAndSection(
            @Query("class") String className,
            @Query("section") String section
    );

    @GET("teacher/timetable_api.php")
    Call<TimetableResponse> getTimetablesByTeacher(
            @Query("teacher_id") String teacherId
    );

    @POST("teacher/timetable_api.php")
    Call<ApiResponse> addTimetable(@Body Timetable timetable);

    @POST("teacher/create_quiz.php")
    Call<ApiResponse> createQuiz(@Body Quiz quiz);

    @GET("teacher/get_quiz_details.php")
    Call<QuizResponse> getQuizDetails(@Query("quiz_name") String quizName);

    @POST("teacher/submit_response.php")
    Call<ApiResponse> submitQuizResponse(@Body QuizResponse quizResponse);

    @GET("teacher/learning_materials.php")
    Call<LearningMaterialsResponse> getLearningMaterials(
            @Query("teacherId") String teacherId,
            @Query("class") String className,
            @Query("section") String section
    );

    @POST("teacher/learning_materials.php")
    Call<ResponseBody> createLearningMaterial(@Body LearningMaterial material);

    @PUT("teacher/learning_materials.php")
    Call<ResponseBody> updateLearningMaterial(@Body LearningMaterial material);

    @DELETE("teacher/learning_materials.php")
    Call<ResponseBody> deleteLearningMaterial(@Query("id") int id);
    // Fee structure endpoints
    @GET("admin/fee_payments.php")
    Call<FeeResponse> getFeeStructures();

    @GET("admin/fee_payments.php")
    Call<FeeResponse> getFeeStructuresByClass(@Query("class") String className);

    @POST("admin/fee_payments.php")
    Call<ApiResponse> addFeeStructure(@Body FeeStructureRequest body);

    @PUT("admin/fee_payments.php")
    Call<ApiResponse> updateFeeStructure(@Body FeeStructureRequest body);

    @DELETE("admin/fee_payments.php")
    Call<ApiResponse> deleteFeeStructure(@Query("id") int id);
}