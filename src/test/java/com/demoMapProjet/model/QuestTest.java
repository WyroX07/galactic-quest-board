package com.demoMapProjet.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the Quest class: constructor, getters, and isCompleted() logic. */

class QuestTest {

    @Test
    void testConstructorAndGetters() {
        Requirement requirement = new Requirement("Star Wars", 3);
        Quest quest = new Quest(1, "Answer 3 Star Wars questions", List.of(requirement));

        assertEquals(1, quest.getId());
        assertEquals("Answer 3 Star Wars questions", quest.getDescription());
        assertEquals(List.of(requirement), quest.getRequirements());
    }

    @Test
    void testIsCompletedReturnsTrueWhenRequirementsAreMet() {
        Requirement requirement = new Requirement("Star Wars", 3);
        Quest quest = new Quest(1, "Star Wars quest", List.of(requirement));

        Map<String, Integer> correctAnswersByTheme = Map.of("Star Wars", 3);

        assertTrue(quest.isCompleted(correctAnswersByTheme));
    }

    @Test
    void testIsCompletedReturnsFalseWhenRequirementsAreNotMet() {
        Requirement requirement = new Requirement("Tourism & Travel", 4);
        Quest quest = new Quest(2, "Tourism quest", List.of(requirement));

        Map<String, Integer> correctAnswersByTheme = Map.of("Tourism & Travel", 2);

        assertFalse(quest.isCompleted(correctAnswersByTheme));
    }

    @Test
    void testIsCompletedReturnsFalseWhenThemeIsMissing() {
        Requirement requirement = new Requirement("Entertainment", 1);
        Quest quest = new Quest(3, "Entertainment quest", List.of(requirement));

        // No entry at all for that theme in the map
        Map<String, Integer> correctAnswersByTheme = Map.of();

        // Missing theme should be treated as 0 -> not completed
        assertFalse(quest.isCompleted(correctAnswersByTheme));
    }
}
