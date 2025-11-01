package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class QuizResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("quiz")
    private Quiz quiz;

    @SerializedName("questions")
    private List<Question> questions;

    @SerializedName("message")
    private String message;

    public static class Quiz {
        @SerializedName("quiz_name")
        private String quizName;

        @SerializedName("class")
        private String className;

        @SerializedName("section")
        private String section;

        @SerializedName("description")
        private String description;

        @SerializedName("created_at")
        private String createdAt;

        public String getQuizName() { return quizName; }
        public String getClassName() { return className; }
        public String getSection() { return section; }
        public String getDescription() { return description; }
        public String getCreatedAt() { return createdAt; }
    }

    public static class Question {
        @SerializedName("id")
        private int id;

        @SerializedName("question_text")
        private String questionText;

        @SerializedName("options")
        private Options options;

        public static class Options {
            @SerializedName("a")
            private String optionA;

            @SerializedName("b")
            private String optionB;

            @SerializedName("c")
            private String optionC;

            @SerializedName("d")
            private String optionD;

            public String getOptionA() { return optionA; }
            public String getOptionB() { return optionB; }
            public String getOptionC() { return optionC; }
            public String getOptionD() { return optionD; }
        }

        public int getId() { return id; }
        public String getQuestionText() { return questionText; }
        public Options getOptions() { return options; }
    }

    // Fields for student response submission
    @SerializedName("quiz_name")
    private String quizName;

    @SerializedName("student_reg_number")
    private String studentRegNumber;

    @SerializedName("student_name")
    private String studentName;

    @SerializedName("class")
    private String className;

    @SerializedName("section")
    private String section;

    @SerializedName("response")
    private Map<String, Integer> response;

    public QuizResponse(String quizName, String studentRegNumber, String studentName, String className, String section, Map<String, Integer> response) {
        this.quizName = quizName;
        this.studentRegNumber = studentRegNumber;
        this.studentName = studentName;
        this.className = className;
        this.section = section;
        this.response = response;
    }

    public String getStatus() { return status; }
    public Quiz getQuiz() { return quiz; }
    public List<Question> getQuestions() { return questions; }
    public String getMessage() { return message; }
    public String getQuizName() { return quizName; }
    public String getStudentRegNumber() { return studentRegNumber; }
    public String getStudentName() { return studentName; }
    public String getClassName() { return className; }
    public String getSection() { return section; }
    public Map<String, Integer> getResponse() { return response; }
}