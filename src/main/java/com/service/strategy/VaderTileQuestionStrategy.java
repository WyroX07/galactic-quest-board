package com.service.strategy;

import com.demoMapProjet.model.QuestionDefinition;
import com.demoMapProjet.model.TileDefinition;
import com.service.QuestionService;
import com.ui.DifficultyCardView;
import com.ui.QuestionCardView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.Random;

/**
 * Strategy for DARK_VADOR tiles.
 *
 * Rules:
 *  - Player chooses a theme (Blue / Green / Orange / Yellow).
 *  - Question is always difficulty 4.
 *  - Correct answer  -> player advances 4 tiles.
 *  - Wrong answer    -> player moves back to START.
 */
public class VaderTileQuestionStrategy implements TileQuestionStrategy {

    private static final int VADER_DIFFICULTY = 4;

    @Override
    public QuestionDefinition selectQuestion(List<QuestionDefinition> allQuestions,
                                             int chosenLevel, Random random) {
        return null;
    }

    @Override
    public void askQuestion(TileDefinition tile,
                            QuestionService questionService,
                            StackPane gameRoot,
                            String playerName,
                            DifficultyCardView.AnswerListener answerListener) {
        showVaderThemeSelector(questionService, gameRoot, playerName, answerListener);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static void showVaderThemeSelector(QuestionService questionService,
                                               StackPane gameRoot,
                                               String playerName,
                                               DifficultyCardView.AnswerListener answerListener) {
        if (gameRoot == null) return;

        // Blur background
        for (var child : gameRoot.getChildren()) {
            child.setEffect(new GaussianBlur(12));
        }

        // Semi-transparent overlay
        Rectangle dimOverlay = new Rectangle();
        dimOverlay.setFill(Color.rgb(0, 0, 20, 0.65));
        dimOverlay.widthProperty().bind(gameRoot.widthProperty());
        dimOverlay.heightProperty().bind(gameRoot.heightProperty());
        dimOverlay.setMouseTransparent(false);

        // Card container — orange (Vader colour)
        double cardWidth  = 780;
        double cardHeight = 570;

        StackPane cardContainer = new StackPane();
        cardContainer.setMaxSize(cardWidth, cardHeight);
        cardContainer.setMinSize(cardWidth, cardHeight);

        ImageView cardBg = new ImageView();
        try {
            cardBg.setImage(new Image(
                    VaderTileQuestionStrategy.class.getResourceAsStream("/img/cardQuestionOrange.png")
            ));
        } catch (Exception ignored) {}
        cardBg.setFitWidth(cardWidth);
        cardBg.setFitHeight(cardHeight);
        cardBg.setPreserveRatio(false);

        Label titleLabel = new Label("⚡ DARK VADOR — " + playerName.toUpperCase());
        titleLabel.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;" +
                "-fx-effect: dropshadow(gaussian, black, 6, 0.6, 0, 2);"
        );
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(620);
        titleLabel.setAlignment(Pos.CENTER);

        Label subLabel = new Label(
                "Choisissez votre thème  •  Difficulté 4  •  " +
                "Correct : +4 cases  •  Faux : retour au DÉPART"
        );
        subLabel.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);" +
                "-fx-effect: dropshadow(gaussian, black, 3, 0.4, 0, 1);"
        );
        subLabel.setWrapText(true);
        subLabel.setMaxWidth(620);
        subLabel.setAlignment(Pos.CENTER);

        // Theme buttons
        HBox buttonsBox = new HBox(14);
        buttonsBox.setAlignment(Pos.CENTER);

        String[] themeLabels = {"🔵\nTech / IT", "🟢\nTourism\n& World", "🟠\nEntertain-\nment", "⭐\nStar Wars"};
        String[] themeKeys   = {"IT & Programming", "Tourism & Travel", "Entertainment", "Star Wars"};
        String[] bgColors    = {"#4CC9F0", "#80ED99", "#F4A261", "#FFD60A"};

        for (int i = 0; i < 4; i++) {
            final String themeKey = themeKeys[i];
            Button btn = buildThemeBtn(themeLabels[i], bgColors[i]);

            btn.setOnAction(e -> {
                // Close theme selector
                gameRoot.getChildren().remove(cardContainer);
                gameRoot.getChildren().remove(dimOverlay);
                for (var child : gameRoot.getChildren()) child.setEffect(null);

                // Ask difficulty-4 question for chosen theme
                QuestionDefinition question =
                        questionService.getQuestionForTheme(themeKey, VADER_DIFFICULTY);

                System.out.println("[VADER] theme=" + themeKey
                        + " / question=" + (question != null ? question.getTitle() : "null"));

                QuestionCardView.show(question, gameRoot, playerName, isCorrect -> {
                    if (answerListener != null) {
                        answerListener.onAnswer(question, isCorrect);
                    }
                });
            });

            buttonsBox.getChildren().add(btn);
        }

        VBox content = new VBox(24, titleLabel, subLabel, buttonsBox);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(70, 55, 50, 55));
        content.setMaxWidth(cardWidth);

        // Close (X) button with "Back to choice" back button
        Button closeBtn = new Button("X");
        closeBtn.setStyle(
                "-fx-background-color: rgba(0,0,0,0.6);" +
                "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-background-radius: 20; -fx-min-width: 28px; -fx-min-height: 28px; -fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> {
            cardContainer.setVisible(false);
            dimOverlay.setVisible(false);
            for (var child : gameRoot.getChildren()) {
                if (child != cardContainer && child != dimOverlay) child.setEffect(null);
            }

            Button backBtn = new Button("Back to choice");
            backBtn.setStyle(
                    "-fx-background-color: #4ECDC4; -fx-text-fill: white; -fx-font-size: 16px;" +
                    "-fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 8;" +
                    "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 3);"
            );
            StackPane.setAlignment(backBtn, Pos.BOTTOM_CENTER);
            StackPane.setMargin(backBtn, new Insets(0, 0, 30, 0));
            gameRoot.getChildren().add(backBtn);

            backBtn.setOnAction(ev -> {
                gameRoot.getChildren().remove(backBtn);
                cardContainer.setVisible(true);
                dimOverlay.setVisible(true);
                for (var child : gameRoot.getChildren()) {
                    if (child != cardContainer && child != dimOverlay)
                        child.setEffect(new GaussianBlur(12));
                }
            });
        });

        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        StackPane innerCard = new StackPane(cardBg, content, closeBtn);
        StackPane.setMargin(closeBtn, new Insets(10, 10, 0, 0));

        cardContainer.getChildren().add(innerCard);
        gameRoot.getChildren().add(dimOverlay);
        gameRoot.getChildren().add(cardContainer);
    }

    private static Button buildThemeBtn(String labelText, String bgColor) {
        Button btn = new Button(labelText);
        String base =
                "-fx-background-color: " + bgColor + "; -fx-text-fill: white;" +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 12;" +
                "-fx-padding: 12 16 12 16; -fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0.3, 0, 2);" +
                "-fx-alignment: center; -fx-text-alignment: center;";
        String hover =
                "-fx-background-color: derive(" + bgColor + ", -15%); -fx-text-fill: white;" +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 12;" +
                "-fx-padding: 12 16 12 16; -fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0.4, 0, 3);" +
                "-fx-alignment: center; -fx-text-alignment: center;" +
                "-fx-scale-x: 1.05; -fx-scale-y: 1.05;";
        btn.setStyle(base);
        btn.setPrefWidth(130);
        btn.setPrefHeight(95);
        btn.setWrapText(true);
        btn.setOnMouseEntered(ev -> btn.setStyle(hover));
        btn.setOnMouseExited(ev -> btn.setStyle(base));
        return btn;
    }
}
