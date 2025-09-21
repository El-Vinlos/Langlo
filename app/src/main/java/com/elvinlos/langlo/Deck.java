package com.elvinlos.langlo;

public class Deck {
    private String title;
    private String description;
    private int cardCount;
    private String folderName;

    public Deck(String title, String description, int cardCount, String folderName) {
        this.title = title;
        this.description = description;
        this.cardCount = cardCount;
        this.folderName = folderName;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getCardCount() { return cardCount; }
    public String getFolderName() { return folderName; }
}