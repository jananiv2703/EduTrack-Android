package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TimetableResponse {
    @SerializedName("timetable")
    private List<Timetable> timetable;

    public List<Timetable> getTimetable() {
        return timetable;
    }

    public void setTimetable(List<Timetable> timetable) {
        this.timetable = timetable;
    }
}