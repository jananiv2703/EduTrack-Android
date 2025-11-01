package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StudentExamSchedulesResponse {
    private String status;

    @SerializedName("data")
    private List<StudentExamSchedule> schedules;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<StudentExamSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<StudentExamSchedule> schedules) {
        this.schedules = schedules;
    }
}