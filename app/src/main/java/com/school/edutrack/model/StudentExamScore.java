package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;

public class StudentExamScore {
    private int id;

    @SerializedName("student_id")
    private String studentId;

    @SerializedName("student_name")
    private String studentName;

    @SerializedName("class")
    private String className;

    private String section;

    @SerializedName("exam_name")
    private String examName;

    @SerializedName("exam_type")
    private String examType;

    @SerializedName("exam_date")
    private String examDate;

    @SerializedName("exam_day")
    private String examDay;

    private String subject;

    @SerializedName("marks_obtained")
    private Integer marksObtained;

    @SerializedName("max_marks")
    private Integer maxMarks;

    @SerializedName("exam_schedule_id")
    private Integer examScheduleId;

    @SerializedName("teacher_id")
    private String teacherId;

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getExamDay() {
        return examDay;
    }

    public void setExamDay(String examDay) {
        this.examDay = examDay;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(Integer marksObtained) {
        this.marksObtained = marksObtained;
    }

    public Integer getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(Integer maxMarks) {
        this.maxMarks = maxMarks;
    }

    public Integer getExamScheduleId() {
        return examScheduleId;
    }

    public void setExamScheduleId(Integer examScheduleId) {
        this.examScheduleId = examScheduleId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }
}