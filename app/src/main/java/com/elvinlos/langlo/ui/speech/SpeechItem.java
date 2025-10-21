package com.elvinlos.langlo.ui.speech;

class SpeechItem {
    private String title;
    private int score;

    public SpeechItem(String title, int score) {
        this.title = title;
        this.score = score;
    }

    public String getTitle() {
        return title;
    }

    public int getScore() {
        return score;
    }
}
