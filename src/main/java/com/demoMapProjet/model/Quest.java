package com.demoMapProjet.model;

import java.util.List;
import java.util.Map;

/** A game quest with requirements the player must complete to win. */
public class Quest {

    private int id;
    private String description;
    private List<Requirement> requirements;

    public Quest(int id, String description, List<Requirement> requirements) {
        this.id = id;
        this.description = description;
        this.requirements = requirements;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }


    public List<Requirement> getRequirements() {
        return requirements;
    }

    /**
     * Checks if the quest is completed based on player's per-theme answer counts.
     *
     * @param correctAnswersByTheme  map of canonical theme → number of correct answers
     * @return true if every requirement is satisfied
     */
    public boolean isCompleted(Map<String, Integer> correctAnswersByTheme) {
        for (Requirement requirement : requirements) {
            int currentAmount = correctAnswersByTheme.getOrDefault(requirement.getTheme(), 0);
            if (currentAmount < requirement.getCount()) {
                return false;
            }
        }
        return true;
    }

}