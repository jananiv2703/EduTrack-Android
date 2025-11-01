package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AttendanceResponse {
    @SerializedName("message")
    private String message;
    private String status;

    private List<AttendanceRecord> data;
    public String getStatus() {
        return status;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public List<AttendanceRecord> getData() {
        return data;
    }
}