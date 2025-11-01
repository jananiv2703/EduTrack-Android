package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;

public class LearningMaterial {
    private int id;
    private String teacherId;
    @SerializedName("class")
    private String className;
    private String section;
    private String subject;
    private String type;
    private String name;
    private String description;
    private String file;
    private String due_date;
    private String created_at;
    private String file_data; // For base64-encoded file in POST/PUT
    private String file_name; // Original filename in POST/PUT

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFile() { return file; }
    public void setFile(String file) { this.file = file; }
    public String getDue_date() { return due_date; }
    public void setDue_date(String due_date) { this.due_date = due_date; }
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public String getFile_data() { return file_data; }
    public void setFile_data(String file_data) { this.file_data = file_data; }
    public String getFile_name() { return file_name; }
    public void setFile_name(String file_name) { this.file_name = file_name; }
}