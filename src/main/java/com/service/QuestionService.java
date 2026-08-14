package com.service;

import com.demoMapProjet.model.QuestionDefinition;
import com.demoMapProjet.model.QuestionPack;
import com.demoMapProjet.model.TileDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class QuestionService {

    private static final Map<String, List<String>> TILE_THEME_TO_QUESTION_THEMES = Map.of(
            "BLUE", List.of("TECH", "IT", "Computing", "Informatics", "informatics", "Computer Science", "IT & Programming"),
            "YELLOW", List.of("Star Wars"),
            "GREEN", List.of("WORLD", "Tourism", "tourism", "Tourism & Travel"),
            "ORANGE", List.of("ENTERTAINMENT", "entertainment", "Entertainment")
    );

    private final List<QuestionDefinition> questions;
    private final Random random = new Random();

    public QuestionService(QuestionPack pack) {
        if (pack == null || pack.getQuestions() == null) {
            this.questions = Collections.emptyList();
            System.out.println("No questions loaded");
        } else {
            this.questions = pack.getQuestions();
            System.out.println("Loaded question count: " + questions.size());
        }
    }

    public QuestionDefinition getQuestionForTile(TileDefinition tile, int difficulty) {
        List<String> requestedThemes = resolveQuestionThemes(tile);

        System.out.println("Requested tile: type=" + safeTileType(tile)
                + ", themes=" + requestedThemes
                + ", difficulty=" + difficulty);

        List<QuestionDefinition> filteredQuestions = questions.stream()
                .filter(question -> matchesAnyTheme(requestedThemes, question))
                .filter(question -> difficulty <= 0 || question.getDifficulte() == difficulty)
                .collect(Collectors.toList());

        System.out.println("Filtered questions with difficulty " + difficulty + ": " + filteredQuestions.size());

        if (filteredQuestions.isEmpty()) {
            filteredQuestions = questions.stream()
                    .filter(question -> matchesAnyTheme(requestedThemes, question))
                    .collect(Collectors.toList());
            System.out.println("Fallback questions without difficulty: " + filteredQuestions.size());
        }

        if (filteredQuestions.isEmpty()) {
            return null;
        }

        return filteredQuestions.get(random.nextInt(filteredQuestions.size()));
    }

    /**
     * Returns a random question matching the given theme string and difficulty.
     * Used by special tile strategies that need to force a specific question theme
     * regardless of the tile's own theme.
     *
     * @param theme      the theme string to look up (e.g. "Star Wars")
     * @param difficulty the required difficulty level (1-4); pass -1 for any
     * @return a randomly selected question, or null if none found
     */
    public QuestionDefinition getQuestionForTheme(String theme, int difficulty) {
        List<QuestionDefinition> matches = questions.stream()
                .filter(q -> matchesTheme(theme, q))
                .filter(q -> difficulty <= 0 || q.getDifficulte() == difficulty)
                .collect(Collectors.toList());

        System.out.println("getQuestionForTheme(\"" + theme + "\", " + difficulty
                + "): " + matches.size() + " match(es)");

        if (matches.isEmpty()) {
            matches = questions.stream()
                    .filter(q -> matchesTheme(theme, q))
                    .collect(Collectors.toList());
        }

        if (matches.isEmpty()) return null;
        return matches.get(random.nextInt(matches.size()));
    }

    private boolean matchesAnyTheme(List<String> requestedThemes, QuestionDefinition question) {
        if (requestedThemes == null || requestedThemes.isEmpty() || question == null || question.getTheme() == null) {
            return false;
        }

        return requestedThemes.stream().anyMatch(theme -> matchesTheme(theme, question));
    }

    private boolean matchesTheme(String requestedTheme, QuestionDefinition question) {
        if (requestedTheme == null || question == null || question.getTheme() == null) {
            return false;
        }

        return normalize(question.getTheme()).equals(normalize(requestedTheme));
    }

    private List<String> resolveQuestionThemes(TileDefinition tile) {
        if (tile == null || tile.getType() == null) {
            return TILE_THEME_TO_QUESTION_THEMES.get("BLUE");
        }

        String tileType = tile.getType().toUpperCase(Locale.ROOT);

        if ("START".equals(tileType)) {
            return TILE_THEME_TO_QUESTION_THEMES.get("YELLOW");
        }

        if ("DARK_VADOR".equals(tileType) || "VADER".equals(tileType)) {
            return TILE_THEME_TO_QUESTION_THEMES.get("YELLOW");
        }

        if ("THEME".equals(tileType) && tile.getTheme() != null) {
            String tileTheme = tile.getTheme().toUpperCase(Locale.ROOT);
            return TILE_THEME_TO_QUESTION_THEMES.getOrDefault(tileTheme, List.of(tile.getTheme()));
        }

        return TILE_THEME_TO_QUESTION_THEMES.get("BLUE");
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String safeTileType(TileDefinition tile) {
        return tile == null ? "null" : tile.getType();
    }
}
