package com.demoMapProjet.model;

/** A single quest requirement: answer [count] questions correctly in a given [theme]. */
public class Requirement {

    // Must match the keys from QuestService.normalizeTheme()
    private String theme;

    private int count;

    public Requirement(String theme, int count) {
        this.theme = theme;
        this.count = count;
    }

    public String getTheme() {
        return theme;
    }

    public int getCount() {
        return count;
    }
}
