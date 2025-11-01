package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;

public class Timetable {
    private int id;

    @SerializedName("teacher_id")
    private String teacherId;

    @SerializedName("teacher_name")
    private String teacherName;

    @SerializedName("day_of_week")
    private String dayOfWeek;

    @SerializedName("period_no")
    private String periodNo;

    private String subject;

    @SerializedName("class")
    private String className;

    private String section;

    // Constructor
    public Timetable(int id, String teacherId, String teacherName, String dayOfWeek, String periodNo, String subject, String className, String section) {
        this.id = id;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.dayOfWeek = dayOfWeek;
        this.periodNo = periodNo;
        this.subject = subject;
        this.className = className;
        this.section = section;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getPeriodNo() {
        return periodNo;
    }

    public void setPeriodNo(String periodNo) {
        this.periodNo = periodNo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @Override
    public String toString() {
        return "Timetable{" +
                "id=" + id +
                ", teacherId='" + teacherId + '\'' +
                ", teacherName='" + teacherName + '\'' +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                ", periodNo='" + periodNo + '\'' +
                ", subject='" + subject + '\'' +
                ", className='" + className + '\'' +
                ", section='" + section + '\'' +
                '}';
    }
}