package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StudentTimetableResponse {
    private String status;

    @SerializedName("data")
    private List<TimetableModel> timetable;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TimetableModel> getTimetable() {
        return timetable;
    }

    public void setTimetable(List<TimetableModel> timetable) {
        this.timetable = timetable;
    }
}