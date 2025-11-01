package com.school.edutrack.model;
import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    private String status;
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    @SerializedName("score")
    private Integer score;

    @SerializedName("total_questions")
    private Integer totalQuestions;


    public Integer getScore() { return score; }
    public Integer getTotalQuestions() { return totalQuestions; }
}