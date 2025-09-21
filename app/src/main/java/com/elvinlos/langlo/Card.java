package com.elvinlos.langlo;


import java.util.Objects;

public class Card {
    private final String id;
    private final String english;
    private final String vietnamese;
    private final String audioFile;   // optional
    private String category;

    public Card(String id, String english, String vietnamese, String audioFile) {
        this.id = id;
        this.english = english;
        this.vietnamese = vietnamese;
        this.audioFile = audioFile;
        this.category = "General"; // default
    }

    public String getId() { return id; }
    public String getEnglish() { return english; }
    public String getVietnamese() { return vietnamese; }
    public String getAudioFile() { return audioFile; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return Objects.equals(id, card.id) &&
                Objects.equals(english, card.english) &&
                Objects.equals(vietnamese, card.vietnamese) &&
                Objects.equals(audioFile, card.audioFile) &&
                Objects.equals(category, card.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, english, vietnamese, audioFile, category);
    }
}
