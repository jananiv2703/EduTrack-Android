package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;

public class StudentAuthRequest {

    @SerializedName("student_id")
    private String studentId;

    @SerializedName("password")
    private String password;

    @SerializedName("pin")
    private String pin;

    // Constructor for password-based login
    public StudentAuthRequest(String studentId, String password) {
        this.studentId = studentId;
        this.password = password;
        this.pin = null;
    }

    // Constructor for PIN-based requests (set PIN or PIN login)
    public StudentAuthRequest(String studentId, String pin, boolean isPinRequest) {
        this.studentId = studentId;
        this.password = null;
        this.pin = pin;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}