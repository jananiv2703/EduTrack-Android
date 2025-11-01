package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;

public class AttendanceRecord {
    private String id;
    private String student_id;
    @SerializedName("class")
    private String class_name;
    private String section;
    private String date;
    private String status;
    private String student_name;
    private String teacher_id;

    public String getId() {
        return id;
    }

    public String getStudentId() {
        return student_id;
    }

    public String getClassName() {
        return class_name;
    }

    public String getSection() {
        return section;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public String getStudentName() {
        return student_name;
    }

    public String getTeacherId() {
        return teacher_id;
    }
}