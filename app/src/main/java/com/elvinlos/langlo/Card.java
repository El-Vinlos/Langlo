package com.elvinlos.langlo;
public class Card {
    private String english;
    private String vietnamese;
    private String audioFile; // optional

    public Card(String english, String vietnamese, String audioFile) {
        this.english = english;
        this.vietnamese = vietnamese;
        this.audioFile = audioFile;
    }

    public String getEnglish() { return english; }
    public String getVietnamese() { return vietnamese; }
    public String getAudioFile() { return audioFile; }
}
