package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StudentExamScoresResponse {
    private String status;

    @SerializedName("data")
    private List<StudentExamScore> marks;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<StudentExamScore> getMarks() {
        return marks;
    }

    public void setMarks(List<StudentExamScore> marks) {
        this.marks = marks;
    }
}