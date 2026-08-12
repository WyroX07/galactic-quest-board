package com.demoMapProjet.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Quest class.
 *
 * These tests verify:
 * - Constructor and getters work correctly
 * - Quest completion logic based on requirements
 * - Behavior when requirements are not met or missing
 * - Random quest selection by difficulty
 */

class QuestTest {

    @Test
    void testConstructorAndGetters() {
        // Create a requirement and a quest
        Requirement requirement = new Requirement(1, 3);
        Quest quest = new Quest(1, "Answer 3 easy questions", List.of(requirement));

        // Verify that constructor initializes fields correctly
        assertEquals(1, quest.getId());
        assertEquals("Answer 3 easy questions", quest.getDescription());
        assertEquals(List.of(requirement), quest.getRequirements());
    }

    @Test
    void testIsCompletedReturnsTrueWhenRequirementsAreMet() {
        // Create a requirement: level 1, need 3 correct answers
        Requirement requirement = new Requirement(1, 3);
        Quest quest = new Quest(1, "Level 1 quest", List.of(requirement));

        // Player has enough correct answers
        Map<Integer, Integer> correctAnswersByLevel = Map.of(
                1, 3
        );

        // Quest should be completed
        assertTrue(quest.isCompleted(correctAnswersByLevel));
    }

    @Test
    void testIsCompletedReturnsFalseWhenRequirementsAreNotMet() {
        // Create a requirement: level 2, need 4 correct answers
        Requirement requirement = new Requirement(2, 4);
        Quest quest = new Quest(2, "Level 2 quest", List.of(requirement));

        // Player does NOT have enough answers
        Map<Integer, Integer> correctAnswersByLevel = Map.of(
                2, 2
        );

        // Quest should NOT be completed
        assertFalse(quest.isCompleted(correctAnswersByLevel));
    }

    @Test
    void testIsCompletedReturnsFalseWhenLevelIsMissing() {
        // Requirement for level 3
        Requirement requirement = new Requirement(3, 1);
        Quest quest = new Quest(3, "Level 3 quest", List.of(requirement));

        // No entry for level 3 in the map
        Map<Integer, Integer> correctAnswersByLevel = Map.of();

        // Missing level should be treated as 0 → not completed
        assertFalse(quest.isCompleted(correctAnswersByLevel));
    }

    @Test
    void testGetRandomQuestByDifficultyReturnsQuest() {
        // Create quests list
        Requirement requirement = new Requirement(1, 1);
        Quest quest1 = new Quest(1, "Quest 1", List.of(requirement));
        Quest quest2 = new Quest(2, "Quest 2", List.of(requirement));

        // Map difficulty to quests
        Map<String, List<Quest>> questsByDifficulty = Map.of(
                "easy", List.of(quest1, quest2)
        );

        // Get a random quest
        Quest result = Quest.getRandomQuestByDifficulty(questsByDifficulty, "easy");

        // Check that result is not null and belongs to the list
        assertNotNull(result);
        assertTrue(List.of(quest1, quest2).contains(result));
    }

    @Test
    void testGetRandomQuestByDifficultyThrowsExceptionWhenEmpty() {
        // Empty map (no quests for difficulty)
        Map<String, List<Quest>> questsByDifficulty = Map.of();

        // Expect an exception
        assertThrows(IllegalArgumentException.class, () -> {
            Quest.getRandomQuestByDifficulty(questsByDifficulty, "hard");
        });
    }

    @Test
    void testGetRandomQuestByDifficultyThrowsExceptionWhenListIsEmpty() {
        // Create a map with a difficulty but an empty quest list
        Map<String, List<Quest>> questsByDifficulty = Map.of(
                "medium", List.of()
        );

        // Expect an exception when the list exists but is empty
        assertThrows(IllegalArgumentException.class, () -> {
            Quest.getRandomQuestByDifficulty(questsByDifficulty, "medium");
        });
    }
}