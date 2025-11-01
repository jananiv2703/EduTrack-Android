package com.school.edutrack.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StudentResponse {
    private String status;
    private String message;
    private String admission_no;
    private String register_no;

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAdmission_no() { return admission_no; }
    public void setAdmission_no(String admission_no) { this.admission_no = admission_no; }

    public String getRegister_no() { return register_no; }
    public void setRegister_no(String register_no) { this.register_no = register_no; }

    @SerializedName("students")
    private List<Student> students;

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }
}