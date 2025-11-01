package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;

public class Attendance {
    @SerializedName("student_id")
    private String studentId;

    @SerializedName("date")
    private String date;

    @SerializedName("status")
    private String status;

    @SerializedName("teacher_id")
    private String teacherId;

    // Constructor
    public Attendance(String studentId, String date, String status, String teacherId) {
        this.studentId = studentId;
        this.date = date;
        this.status = status;
        this.teacherId = teacherId;
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "studentId='" + studentId + '\'' +
                ", date='" + date + '\'' +
                ", status='" + status + '\'' +
                ", teacherId='" + teacherId + '\'' +
                '}';
    }
}