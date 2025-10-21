package com.elvinlos.langlo;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {
    public String name;
    public Map<String, Integer> scores;
    public int total_score;
    private String uid;

    public User() {
        // Required for Firebase
    }

    public User(String name, int totalScore) {
        this.name = name;
        this.scores = new HashMap<>();
        this.total_score = totalScore;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<String, Integer> scores) {
        this.scores = scores;
    }

    public int getTotal_score() {
        return total_score;
    }

    public void setTotal_score(int total_score) {
        this.total_score = total_score;
    }

    // Use @Exclude to prevent Firebase from storing UID
    @Exclude
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}