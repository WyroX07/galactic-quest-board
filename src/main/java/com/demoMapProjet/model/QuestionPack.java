package com.demoMapProjet.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionPack {

    private List<Card> cards;

    private List<QuestionDefinition> questions;

    public List<QuestionDefinition> getQuestions() {
        if (questions != null) return questions;

        if (cards != null) {
            List<QuestionDefinition> allQuestions = new ArrayList<>();
            for (Card card : cards) {
                if (card.getQuestions() != null) {
                    for (QuestionDefinition question : card.getQuestions()) {
                        if (question.getTheme() == null && card.getColor() != null) {
                            question.setTheme(card.getColor().toUpperCase());
                        }
                        allQuestions.add(question);
                    }
                }
            }
            return allQuestions;
        }

        return Collections.emptyList();
    }

    public void setQuestions(List<QuestionDefinition> questions) {
        this.questions = questions;
    }

    public static class Card {
        @SerializedName("couleur")
        private String color;
        private List<QuestionDefinition> questions;

        public String getColor() { return color; }
        public List<QuestionDefinition> getQuestions() { return questions; }
    }
}
