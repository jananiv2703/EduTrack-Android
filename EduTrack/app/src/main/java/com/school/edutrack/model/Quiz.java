package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Quiz {
    @SerializedName("teacher_id")
    private String teacherId;

    @SerializedName("class")
    private String className;

    @SerializedName("section")
    private String section;

    @SerializedName("quiz_name")
    private String quizName;

    @SerializedName("description")
    private String description;

    @SerializedName("questions")
    private List<Question> questions;

    public static class Question {
        @SerializedName("question_text")
        private String questionText;

        @SerializedName("options")
        private Options options;

        @SerializedName("correct_option")
        private int correctOption;

        public static class Options {
            @SerializedName("a")
            private String optionA;

            @SerializedName("b")
            private String optionB;

            @SerializedName("c")
            private String optionC;

            @SerializedName("d")
            private String optionD;

            public Options(String optionA, String optionB, String optionC, String optionD) {
                this.optionA = optionA;
                this.optionB = optionB;
                this.optionC = optionC;
                this.optionD = optionD;
            }

            public String getOptionA() { return optionA; }
            public String getOptionB() { return optionB; }
            public String getOptionC() { return optionC; }
            public String getOptionD() { return optionD; }
        }

        public Question(String questionText, Options options, int correctOption) {
            this.questionText = questionText;
            this.options = options;
            this.correctOption = correctOption;
        }

        public String getQuestionText() { return questionText; }
        public Options getOptions() { return options; }
        public int getCorrectOption() { return correctOption; }
    }

    public Quiz(String teacherId, String className, String section, String quizName, String description, List<Question> questions) {
        this.teacherId = teacherId;
        this.className = className;
        this.section = section;
        this.quizName = quizName;
        this.description = description;
        this.questions = questions;
    }

    public String getTeacherId() { return teacherId; }
    public String getClassName() { return className; }
    public String getSection() { return section; }
    public String getQuizName() { return quizName; }
    public String getDescription() { return description; }
    public List<Question> getQuestions() { return questions; }
}