package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ClassTeacherResponse {
    @SerializedName("class")
    private String className;
    private String section;
    private List<Student> students;

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

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }
}