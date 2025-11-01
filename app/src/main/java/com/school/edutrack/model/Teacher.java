package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;

public class Teacher {
    @SerializedName("teacher_id")
    private String teacher_id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("class")
    private String class_;

    @SerializedName("section")
    private String section;

    @SerializedName("subject")
    private String subject;

    @SerializedName("doj")
    private String doj;

    @SerializedName("is_class_teacher")
    private String isClassTeacher;

    @SerializedName("password")
    private String password;

    @SerializedName("gender")
    private String gender;

    @SerializedName("mobile_number")
    private String mobileNumber;

    @SerializedName("address")
    private String address;

    // Getters and Setters
    public String getTeacher_id() {
        return teacher_id;
    }

    public void setTeacher_id(String teacher_id) {
        this.teacher_id = teacher_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClass_() {
        return class_;
    }

    public void setClass_(String class_) {
        this.class_ = class_;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDoj() {
        return doj;
    }

    public void setDoj(String doj) {
        this.doj = doj;
    }

    public String getIsClassTeacher() {
        return isClassTeacher;
    }

    public void setIsClassTeacher(String isClassTeacher) {
        this.isClassTeacher = isClassTeacher;
    }

    // UI-friendly getter/setter for spinner
    public String getIsClassTeacherForUI() {
        return "yes".equalsIgnoreCase(isClassTeacher) ? "Yes" : "No";
    }

    public void setIsClassTeacherFromUI(String isClassTeacherUI) {
        this.isClassTeacher = "Yes".equalsIgnoreCase(isClassTeacherUI) ? "yes" : "no";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}