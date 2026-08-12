package com.service.strategy;

import com.demoMapProjet.model.QuestionDefinition;
import com.demoMapProjet.model.TileDefinition;
import com.service.QuestionService;
import com.ui.DifficultyCardView;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Strategy for VADER (Dark Vader) tiles.
 *
 * Always asks a difficulty-4 question chosen randomly from all available questions.
 * The difficulty is forced to 4 regardless of the player's choice;
 * the chosenLevel parameter is ignored.
 *
 * Rules:
 *   - Correct answer  -> player advances 4 tiles.
 *   - Wrong answer    -> player is sent back to the START tile.
 */
public class VaderQuestionStrategy implements TileQuestionStrategy {

    /** Difficulty level forced for Vader tiles */
    public static final int VADER_DIFFICULTY = 4;

    /** Number of tiles to advance on a correct answer */
    public static final int VADER_ADVANCE_ON_CORRECT = 4;

    @Override
    public QuestionDefinition selectQuestion(List<QuestionDefinition> allQuestions, int chosenLevel, Random random) {
        // Always force difficulty 4, regardless of chosenLevel
        List<QuestionDefinition> hardQuestions = allQuestions.stream()
                .filter(q -> q.getDifficulty() == VADER_DIFFICULTY)
                .collect(Collectors.toList());

        if (hardQuestions.isEmpty()) {
            // Fallback: pick any question
            if (allQuestions.isEmpty()) return null;
            return allQuestions.get(random.nextInt(allQuestions.size()));
        }

        return hardQuestions.get(random.nextInt(hardQuestions.size()));
    }

    @Override
    public void askQuestion(TileDefinition tile, QuestionService questionService, StackPane gameRoot, String playerName, DifficultyCardView.AnswerListener answerListener) {

    }
}
