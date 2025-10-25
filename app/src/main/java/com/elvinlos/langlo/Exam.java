package com.elvinlos.langlo;

public class Exam {
    private String title;
    private String questionAmount;

    public Exam(String title, String questionAmount) {
        this.title = title;
        this.questionAmount = questionAmount;
    }

    public String getTitle() { return title; }
    public String getQuestionAmount() { return questionAmount; }
}
