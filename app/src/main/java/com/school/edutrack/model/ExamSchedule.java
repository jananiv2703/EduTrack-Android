package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;

public class ExamSchedule {
    @SerializedName("id")
    private String id;

    @SerializedName("exam_name")
    private String examName;

    @SerializedName("exam_type")
    private String examType;

    @SerializedName("class")
    private String className;

    @SerializedName("section")
    private String section;

    @SerializedName("exam_date")
    private String examDate;

    @SerializedName("exam_day")
    private String examDay;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("class_venue")
    private String classVenue;

    @SerializedName("seating_arrangement")
    private String seatingArrangement;

    @SerializedName("room_number")
    private String roomNumber;

    @SerializedName("teacher_id")
    private String teacherId;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getExamDate() { return examDate; }
    public void setExamDate(String examDate) { this.examDate = examDate; }

    public String getExamDay() { return examDay; }
    public void setExamDay(String examDay) { this.examDay = examDay; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getClassVenue() { return classVenue; }
    public void setClassVenue(String classVenue) { this.classVenue = classVenue; }

    public String getSeatingArrangement() { return seatingArrangement; }
    public void setSeatingArrangement(String seatingArrangement) { this.seatingArrangement = seatingArrangement; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
}