package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Student {
    @SerializedName("id")
    private String id; // Added to match API response

    @SerializedName("student_id")
    private String student_id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("class")
    private String class_name;

    @SerializedName("section")
    private String section;

    @SerializedName("dob")
    private String dob;

    @SerializedName("gender")
    private String gender;

    @SerializedName("address")
    private String address;

    @SerializedName("phone")
    private String phone;

    @SerializedName("parent_name")
    private String parent_name;

    @SerializedName("parent_contact")
    private String parent_contact;

    @SerializedName("admission_date")
    private String admission_date;

    @SerializedName("admission_no")
    private String admission_no;

    @SerializedName("register_no")
    private String register_no;

    // Fields for other endpoints (not used in this API response)
    @SerializedName("password")
    private String password;

    @SerializedName("attendance")
    private List<Attendance> attendance;

    @SerializedName("status")
    private String status;

    @SerializedName("date")
    private String date;

    @SerializedName("teacher_id")
    private String teacherId;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudent_id() { return student_id; }
    public void setStudent_id(String student_id) { this.student_id = student_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getClass_name() { return class_name; }
    public void setClass_name(String class_name) { this.class_name = class_name; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getParent_name() { return parent_name; }
    public void setParent_name(String parent_name) { this.parent_name = parent_name; }

    public String getParent_contact() { return parent_contact; }
    public void setParent_contact(String parent_contact) { this.parent_contact = parent_contact; }

    public String getAdmission_date() { return admission_date; }
    public void setAdmission_date(String admission_date) { this.admission_date = admission_date; }

    public String getAdmission_no() { return admission_no; }
    public void setAdmission_no(String admission_no) { this.admission_no = admission_no; }

    public String getRegister_no() { return register_no; }
    public void setRegister_no(String register_no) { this.register_no = register_no; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<Attendance> getAttendance() { return attendance; }
    public void setAttendance(List<Attendance> attendance) { this.attendance = attendance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", student_id='" + student_id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", class_name='" + class_name + '\'' +
                ", section='" + section + '\'' +
                ", dob='" + dob + '\'' +
                ", gender='" + gender + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", parent_name='" + parent_name + '\'' +
                ", parent_contact='" + parent_contact + '\'' +
                ", admission_date='" + admission_date + '\'' +
                ", admission_no='" + admission_no + '\'' +
                ", register_no='" + register_no + '\'' +
                '}';
    }
}