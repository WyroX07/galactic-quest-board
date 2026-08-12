package com.demoMapProjet.model;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for the QuestionDefinition class.
 *
 * These tests verify:
 * - Setters and getters correctly store and return values
 * - Default values are handled properly when fields are null
 * - Alternative fields (sujet, reponse, choix) are correctly used when needed
 * - The toString method contains important information about the object
 */

import static org.junit.jupiter.api.Assertions.*;

class QuestionDefinitionTest {

    @Test
    void testBasicSettersAndGetters() {
        // Create a new question object
        QuestionDefinition question = new QuestionDefinition();

        // Set values using setters
        question.setId(1);
        question.setType("multiple-choice");
        question.setTheme("Geography");
        question.setTitle("Capital cities");
        question.setQuestion("What is the capital of France?");
        question.setChoices(List.of("Paris", "London", "Berlin"));
        question.setAnswer("Paris");
        question.setDifficulte(2);

        // Check that getters return the correct values
        assertEquals(1, question.getId());
        assertEquals("multiple-choice", question.getType());
        assertEquals("Geography", question.getTheme());
        assertEquals("Capital cities", question.getTitle());
        assertEquals("What is the capital of France?", question.getQuestion());
        assertEquals(List.of("Paris", "London", "Berlin"), question.getChoices());
        assertEquals("Paris", question.getAnswer());
        assertEquals(2, question.getDifficulte());
    }

    @Test
    void testDefaultTitleWhenTitleIsNull() {
        // Create a question without setting a title
        QuestionDefinition question = new QuestionDefinition();

        // If title and sujet are null, getTitle should return an empty string
        assertEquals("", question.getTitle());
    }

    @Test
    void testDefaultAnswerWhenAnswerIsNull() {
        // Create a question without setting an answer
        QuestionDefinition question = new QuestionDefinition();

        // If answer and reponse are null, getAnswer should return an empty string
        assertEquals("", question.getAnswer());
    }

    @Test
    void testChoicesReturnNullWhenNoChoicesExist() {
        // Create a question without choices
        QuestionDefinition question = new QuestionDefinition();

        // If choices and choix are null, getChoices should return null
        assertNull(question.getChoices());
    }

    @Test
    void testToStringContainsImportantInformation() {
        // Create a question object
        QuestionDefinition question = new QuestionDefinition();

        question.setId(5);
        question.setType("text");
        question.setTheme("History");
        question.setTitle("World War II");
        question.setDifficulte(3);

        // Convert the object to a String
        String result = question.toString();

        // Check that the String contains useful information
        assertTrue(result.contains("id=5"));
        assertTrue(result.contains("type='text'"));
        assertTrue(result.contains("theme='History'"));
        assertTrue(result.contains("title='World War II'"));
        assertTrue(result.contains("difficulte=3"));
    }

    @Test
    void testGetTitleUsesSujetWhenTitleIsNull() throws Exception {
        // Create a question without setting a title
        QuestionDefinition question = new QuestionDefinition();

        // Use reflection to set the alternative field "sujet"
        Field sujetField = QuestionDefinition.class.getDeclaredField("sujet");
        sujetField.setAccessible(true);
        sujetField.set(question, "Titre alternatif");

        // Verify that getTitle returns the value of "sujet" when "title" is null
        assertEquals("Titre alternatif", question.getTitle());
    }

    @Test
    void testGetAnswerUsesReponseWhenAnswerIsNull() throws Exception {
        // Create a question without setting an answer
        QuestionDefinition question = new QuestionDefinition();

        // Use reflection to set the alternative field "reponse"
        Field reponseField = QuestionDefinition.class.getDeclaredField("reponse");
        reponseField.setAccessible(true);
        reponseField.set(question, "Réponse alternative");

        // Verify that getAnswer returns the value of "reponse" when "answer" is null
        assertEquals("Réponse alternative", question.getAnswer());
    }

    @Test
    void testGetChoicesUsesChoixWhenChoicesIsNull() throws Exception {
        // Create a question without setting choices
        QuestionDefinition question = new QuestionDefinition();

        // Prepare alternative choices list
        List<String> choix = Arrays.asList("Choix A", "Choix B");

        // Use reflection to set the alternative field "choix"
        Field choixField = QuestionDefinition.class.getDeclaredField("choix");
        choixField.setAccessible(true);
        choixField.set(question, choix);

        // Verify that getChoices returns "choix" when "choices" is null
        assertEquals(choix, question.getChoices());
    }
}