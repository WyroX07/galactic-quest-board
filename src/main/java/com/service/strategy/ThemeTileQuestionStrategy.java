package com.service.strategy;

import com.demoMapProjet.model.QuestionDefinition;
import com.demoMapProjet.model.TileDefinition;
import com.service.QuestionService;
import com.ui.DifficultyCardView;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.Random;

public class ThemeTileQuestionStrategy implements TileQuestionStrategy {
    @Override
    public QuestionDefinition selectQuestion(List<QuestionDefinition> allQuestions, int chosenLevel, Random random) {
        return null;
    }

    @Override
    public void askQuestion(TileDefinition tile, QuestionService questionService, StackPane gameRoot,String playerName, DifficultyCardView.AnswerListener answerListener) {
        DifficultyCardView.show(tile, questionService, gameRoot,playerName ,answerListener);
    }
}
