package com.elvinlos.langlo;

public class User {
    private String userId;
    private String name;
    private String email;
    private int totalScore;
    private int gamesPlayed;

    public User() {
        // Default constructor required for Firebase
    }

    // Constructor được sử dụng trong promptForName
    public User(String name, int totalScore) {
        this.name = name;  // Gán vào name
        this.totalScore = totalScore;
        this.gamesPlayed = 0;
    }

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;  // Gán vào name
        this.email = email;
        this.totalScore = 0;
        this.gamesPlayed = 0;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getName() {  // ĐỔI TỪ getUsername → getName
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {  // ĐỔI TỪ setUsername → setName
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }
}