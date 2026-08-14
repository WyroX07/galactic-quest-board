package com.service.strategy;

import com.demoMapProjet.model.TileDefinition;
import com.service.QuestionService;
import com.ui.DifficultyCardView;
import javafx.scene.layout.StackPane;

public interface TileQuestionStrategy {
    void askQuestion(TileDefinition tile, QuestionService questionService, StackPane gameRoot, String playerName, DifficultyCardView.AnswerListener answerListener);
}
