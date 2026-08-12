package com.service.strategy;

import com.demoMapProjet.model.QuestionDefinition;
import com.demoMapProjet.model.TileDefinition;
import com.service.QuestionService;
import com.ui.DifficultyCardView;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Strategy for theme-based tiles (BLUE, YELLOW, GREEN, ORANGE, START treated as YELLOW).
 *
 * Selects a random question matching the given theme and difficulty.
 * Theme mapping:
 *   BLUE   -> "IT & Programming"
 *   YELLOW -> "Star Wars"
 *   GREEN  -> "Tourism & Travel"
 *   ORANGE -> "Entertainment"
 *   START  -> treated the same as YELLOW ("Star Wars")
 *
 * If no question matches both the theme and the chosen difficulty,
 * falls back to any question of that theme.
 */
public class ThemeQuestionStrategy implements TileQuestionStrategy {

    // Accepted question-bank theme strings for this tile color.
    private static final Map<String, List<String>> THEME_ALIASES = Map.of(
            "IT & Programming", List.of("TECH", "IT", "Computing", "Informatics", "informatics", "Computer Science", "IT & Programming"),
            "Star Wars", List.of("Star Wars"),
            "Tourism & Travel", List.of("WORLD", "Tourism", "tourism", "Tourism & Travel"),
            "Entertainment", List.of("ENTERTAINMENT", "entertainment", "Entertainment")
    );

    private final String targetTheme;

    public ThemeQuestionStrategy(String targetTheme) {
        this.targetTheme = targetTheme;
    }

    @Override
    public QuestionDefinition selectQuestion(List<QuestionDefinition> allQuestions, int chosenLevel, Random random) {
        List<String> acceptedThemes = THEME_ALIASES.getOrDefault(targetTheme, List.of(targetTheme));

        // Filter by all accepted theme names for this tile color.
        List<QuestionDefinition> themeMatches = allQuestions.stream()
                .filter(q -> q.getTheme() != null)
                .filter(q -> acceptedThemes.stream().anyMatch(theme -> theme.equalsIgnoreCase(q.getTheme())))
                .collect(Collectors.toList());

        if (themeMatches.isEmpty()) return null;

        // Filter by difficulty if a level was chosen
        if (chosenLevel > 0) {
            List<QuestionDefinition> diffMatches = themeMatches.stream()
                    .filter(q -> q.getDifficulty() == chosenLevel)
                    .collect(Collectors.toList());

            if (!diffMatches.isEmpty()) {
                return diffMatches.get(random.nextInt(diffMatches.size()));
            }
        }

        // Fallback: return any question of that theme
        return themeMatches.get(random.nextInt(themeMatches.size()));
    }

    public String getTargetTheme() {
        return targetTheme;
    }

    @Override
    public void askQuestion(TileDefinition tile, QuestionService questionService, StackPane gameRoot, String playerName,DifficultyCardView.AnswerListener answerListener) {

    }
}
