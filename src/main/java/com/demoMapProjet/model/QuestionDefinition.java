package com.demoMapProjet.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuestionDefinition {

    private int id;
    private String type;
    private String theme;
    private String title;
    private String question;
    private List<String> choices;
    private String answer;


    private int difficulty;

    private String subject;

    private String legacyAnswer;

    private List<String> legacyChoices;

    public QuestionDefinition() {
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public String getTheme() { return theme; }

    public String getTitle() {
        if (title != null) return title;
        if (subject != null) return subject;
        return "";
    }

    public String getQuestion() { return question; }

    public List<String> getChoices() {
        if (choices != null) return choices;
        if (legacyChoices != null) return legacyChoices;
        return null;
    }

    public String getAnswer() {
        if (answer != null) return answer;
        if (legacyAnswer != null) return legacyAnswer;
        return "";
    }

    public int getDifficulte() { return difficulty; }
    public int getDifficulty() { return difficulty; }

    public void setId(int id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setTheme(String theme) { this.theme = theme; }
    public void setTitle(String title) { this.title = title; }
    public void setQuestion(String question) { this.question = question; }
    public void setChoices(List<String> choices) { this.choices = choices; }
    public void setAnswer(String answer) { this.answer = answer; }
    public void setDifficulte(int difficulty) { this.difficulty = difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    @Override
    public String toString() {
        return "QuestionDefinition{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", theme='" + theme + '\'' +
                ", title='" + getTitle() + '\'' +
                ", difficulty=" + difficulty +
                '}';
    }
}
