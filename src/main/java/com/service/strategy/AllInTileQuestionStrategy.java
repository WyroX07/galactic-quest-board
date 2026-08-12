package com.service.strategy;

import com.demoMapProjet.model.QuestionDefinition;
import com.demoMapProjet.model.TileDefinition;
import com.service.QuestionService;
import com.ui.DifficultyCardView;
import com.ui.QuestionCardView;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.Random;

/**
 * Strategy for ALL_IN tiles.
 *
 * Rules:
 *  - The question is always from the "Star Wars" theme.
 *  - The player chooses a difficulty level (1-4) via DifficultyCardView.
 *  - Wrong answer  -> player moves BACK by <chosenLevel> tiles.
 *  - Correct answer -> player moves FORWARD by <chosenLevel + 1> tiles.
 *
 * To communicate the chosen level back to PlanetBoardView, the answer
 * listener receives a AllInAnswerListener (subtype of AnswerListener)
 * that also carries the chosen level.
 */
public class AllInTileQuestionStrategy implements TileQuestionStrategy {

    /** Tile theme string used in questions.json for Star Wars questions */
    public static final String ALL_IN_QUESTION_THEME = "Star Wars";

    /**
     * Extended listener that also exposes the difficulty level the player chose,
     * so PlanetBoardView can compute the correct number of steps.
     */
    public interface AllInAnswerListener extends DifficultyCardView.AnswerListener {
        /** Called once the player has answered; level is the chosen difficulty (1-4). */
        void onAllInAnswer(QuestionDefinition question, boolean isCorrect, int chosenLevel);

        /** Default bridge: forward to the richer callback. */
        @Override
        default void onAnswer(QuestionDefinition question, boolean isCorrect) {
            // Not used directly; the strategy calls onAllInAnswer instead.
        }
    }

    @Override
    public QuestionDefinition selectQuestion(List<QuestionDefinition> allQuestions, int chosenLevel, Random random) {
        return null;
    }

    @Override
    public void askQuestion(TileDefinition tile,
                            QuestionService questionService,
                            StackPane gameRoot,
                            String playerName,
                            DifficultyCardView.AnswerListener answerListener) {

        // Uses a yellow proxy tile to load the Star Wars card image
        TileDefinition yellowProxy = createYellowProxy();
        showAllInDifficultyCard(yellowProxy, questionService, gameRoot, playerName, answerListener);
    }



    /**
     * Shows a difficulty card whose question is always fetched from Star Wars,
     * and whose answer listener carries the chosen level.
     */
    private static void showAllInDifficultyCard(TileDefinition yellowProxy,
                                                QuestionService questionService,
                                                StackPane gameRoot,
                                                String playerName,
                                                DifficultyCardView.AnswerListener outerListener) {

        if (gameRoot == null) return;

        // Blur background
        for (var child : gameRoot.getChildren()) {
            child.setEffect(new javafx.scene.effect.GaussianBlur(12));
        }

        // Semi-transparent overlay
        javafx.scene.shape.Rectangle dimOverlay = new javafx.scene.shape.Rectangle();
        dimOverlay.setFill(javafx.scene.paint.Color.rgb(0, 0, 20, 0.55));
        dimOverlay.widthProperty().bind(gameRoot.widthProperty());
        dimOverlay.heightProperty().bind(gameRoot.heightProperty());
        dimOverlay.setMouseTransparent(false);

        // Card container (YELLOW / Star Wars image)
        double cardWidth = 780;
        double cardHeight = 570;

        javafx.scene.layout.StackPane cardContainer = new javafx.scene.layout.StackPane();
        cardContainer.setMaxSize(cardWidth, cardHeight);
        cardContainer.setMinSize(cardWidth, cardHeight);

        javafx.scene.image.ImageView cardBg = new javafx.scene.image.ImageView();
        try {
            javafx.scene.image.Image img = new javafx.scene.image.Image(
                    AllInTileQuestionStrategy.class.getResourceAsStream("/img/cardQuestionYellow.png")
            );
            cardBg.setImage(img);
        } catch (Exception ignored) {}
        cardBg.setFitWidth(cardWidth);
        cardBg.setFitHeight(cardHeight);
        cardBg.setPreserveRatio(false);

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(playerName + " ALL-IN — Choose your level!");
        titleLabel.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-effect: dropshadow(gaussian, black, 6, 0.6, 0, 2);"
        );
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(560);
        titleLabel.setAlignment(javafx.geometry.Pos.CENTER);

        javafx.scene.control.Label subLabel = new javafx.scene.control.Label(
                "Star Wars theme  •  Correct: +level+1 tiles  •  Wrong: -level tiles"
        );
        subLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: rgba(255,255,255,0.85);" +
                        "-fx-effect: dropshadow(gaussian, black, 3, 0.4, 0, 1);"
        );
        subLabel.setWrapText(true);
        subLabel.setMaxWidth(560);
        subLabel.setAlignment(javafx.geometry.Pos.CENTER);

        // Difficulty buttons
        javafx.scene.layout.HBox buttonsBox = new javafx.scene.layout.HBox(14);
        buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);

        String[] labels   = { "⭐\nEasy",  "⭐⭐\nNormal", "⭐⭐⭐\nHard",  "⭐⭐⭐⭐\nExpert" };
        String[] bgColors = { "#4CC9F0",   "#80ED99",       "#F4A261",    "#ff6b6b" };

        for (int i = 1; i <= 4; i++) {
            final int level = i;
            javafx.scene.control.Button btn = buildDiffBtn(labels[i - 1], bgColors[i - 1]);

            btn.setOnAction(e -> {
                closeOverlay(gameRoot, dimOverlay, cardContainer);

                // Force Star Wars question regardless of tile colour
                QuestionDefinition question =
                        questionService.getQuestionForTheme(ALL_IN_QUESTION_THEME, level);

                System.out.println("[ALL-IN] level=" + level
                        + " / question=" + (question != null ? question.getTitle() : "null"));

                QuestionCardView.show(question, gameRoot,playerName, isCorrect -> {
                    if (outerListener instanceof AllInAnswerListener al) {
                        al.onAllInAnswer(question, isCorrect, level);
                    } else if (outerListener != null) {
                        outerListener.onAnswer(question, isCorrect);
                    }
                });
            });

            buttonsBox.getChildren().add(btn);
        }

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(24, titleLabel, subLabel, buttonsBox);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setPadding(new javafx.geometry.Insets(70, 55, 50, 55));
        content.setMaxWidth(cardWidth);

        // Close (X) button
        javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("X");
        closeBtn.setStyle(
                "-fx-background-color: rgba(0,0,0,0.6);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-min-width: 28px;" +
                        "-fx-min-height: 28px;" +
                        "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> {
            cardContainer.setVisible(false);
            dimOverlay.setVisible(false);
            for (var child : gameRoot.getChildren()) {
                if (child != cardContainer && child != dimOverlay) child.setEffect(null);
            }

            javafx.scene.control.Button backBtn =
                    new javafx.scene.control.Button("Back to choice");
            backBtn.setStyle(
                    "-fx-background-color: #4ECDC4;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 12 30;" +
                    "-fx-background-radius: 8;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 3);"
            );
            javafx.scene.layout.StackPane.setAlignment(backBtn, javafx.geometry.Pos.BOTTOM_CENTER);
            javafx.scene.layout.StackPane.setMargin(backBtn,
                    new javafx.geometry.Insets(0, 0, 30, 0));
            gameRoot.getChildren().add(backBtn);

            backBtn.setOnAction(ev -> {
                gameRoot.getChildren().remove(backBtn);
                cardContainer.setVisible(true);
                dimOverlay.setVisible(true);
                for (var child : gameRoot.getChildren()) {
                    if (child != cardContainer && child != dimOverlay)
                        child.setEffect(new javafx.scene.effect.GaussianBlur(12));
                }
            });
        });

        javafx.scene.layout.StackPane.setAlignment(closeBtn, javafx.geometry.Pos.TOP_RIGHT);
        javafx.scene.layout.StackPane innerCard =
                new javafx.scene.layout.StackPane(cardBg, content, closeBtn);
        javafx.scene.layout.StackPane.setMargin(closeBtn, new javafx.geometry.Insets(10, 10, 0, 0));

        cardContainer.getChildren().add(innerCard);

        gameRoot.getChildren().add(dimOverlay);
        gameRoot.getChildren().add(cardContainer);
    }

    private static javafx.scene.control.Button buildDiffBtn(String labelText, String bgColor) {
        javafx.scene.control.Button btn = new javafx.scene.control.Button(labelText);
        String base = "-fx-background-color:" + bgColor + ";" +
                "-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;" +
                "-fx-background-radius:12;-fx-padding:12 16 12 16;-fx-cursor:hand;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.4),6,0.3,0,2);" +
                "-fx-alignment:center;-fx-text-alignment:center;";
        String hover = "-fx-background-color:derive(" + bgColor + ",-15%);" +
                "-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;" +
                "-fx-background-radius:12;-fx-padding:12 16 12 16;-fx-cursor:hand;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.6),10,0.4,0,3);" +
                "-fx-alignment:center;-fx-text-alignment:center;" +
                "-fx-scale-x:1.05;-fx-scale-y:1.05;";
        btn.setStyle(base);
        btn.setPrefWidth(125);
        btn.setPrefHeight(95);
        btn.setWrapText(true);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private static void closeOverlay(javafx.scene.layout.StackPane gameRoot,
                                     javafx.scene.shape.Rectangle dimOverlay,
                                     javafx.scene.layout.StackPane cardContainer) {
        gameRoot.getChildren().remove(cardContainer);
        gameRoot.getChildren().remove(dimOverlay);
        for (var child : gameRoot.getChildren()) child.setEffect(null);
    }

    /** Creates a synthetic tile of type THEME/YELLOW used only for the card image lookup. */
    private TileDefinition createYellowProxy() {
        // TileDefinition has no public setters, so we return null and rely on
        // the hard-coded "/img/cardQuestionYellow.png" path in the method above.
        return null;
    }
}
