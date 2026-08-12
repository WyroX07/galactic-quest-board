package com.ui;

import com.demoMapProjet.model.QuestionDefinition;
import com.demoMapProjet.model.TileDefinition;
import com.service.QuestionService;
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

/**
 * Difficulty selection pop-up.
 * All four colour variants (BLUE, GREEN, ORANGE, YELLOW) share the same
 * fixed dimensions: CARD_WIDTH x CARD_HEIGHT.
 */
public class DifficultyCardView {

    // Unique fixed size for every variant
    private static final double CARD_WIDTH  = 780;
    private static final double CARD_HEIGHT = 570;

    // Content area width = card width minus total horizontal padding
    private static final double CONTENT_WIDTH = CARD_WIDTH - 110;

    public static void show(TileDefinition tile, QuestionService questionService, StackPane gameRoot, String playerName, AnswerListener answerListener) {
        if (gameRoot == null) return;

        // Blur the background
        for (var child : gameRoot.getChildren()) {
            child.setEffect(new GaussianBlur(12));
        }

        // Semi-transparent overlay
        Rectangle dimOverlay = new Rectangle();
        dimOverlay.setFill(Color.rgb(0, 0, 20, 0.55));
        dimOverlay.widthProperty().bind(gameRoot.widthProperty());
        dimOverlay.heightProperty().bind(gameRoot.heightProperty());
        dimOverlay.setMouseTransparent(false);

        String cardImagePath = cardImageFor(tile);

        StackPane cardContainer = new StackPane();
        cardContainer.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        cardContainer.setMinSize(CARD_WIDTH, CARD_HEIGHT);

        // Card background image
        ImageView cardBg = new ImageView();
        try {
            Image cardImg = new Image(
                    DifficultyCardView.class.getResourceAsStream(cardImagePath)
            );
            cardBg.setImage(cardImg);
        } catch (Exception e) {
            System.err.println("Unable to load card image: " + cardImagePath);
        }
        cardBg.setFitWidth(CARD_WIDTH);
        cardBg.setFitHeight(CARD_HEIGHT);
        cardBg.setPreserveRatio(false);

        // Title
        Label titleLabel = new Label(playerName + " choose your difficulty");
        titleLabel.setStyle(
                "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-effect: dropshadow(gaussian, black, 6, 0.6, 0, 2);"
        );
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(CONTENT_WIDTH);
        titleLabel.setAlignment(Pos.CENTER);

        Label subLabel = new Label("Select a level from 1 (easy) to 4 (hard)");
        subLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: rgba(255,255,255,0.85);" +
                        "-fx-effect: dropshadow(gaussian, black, 3, 0.4, 0, 1);"
        );
        subLabel.setWrapText(true);
        subLabel.setMaxWidth(CONTENT_WIDTH);
        subLabel.setAlignment(Pos.CENTER);

        // Difficulty buttons
        HBox buttonsBox = new HBox(14);
        buttonsBox.setAlignment(Pos.CENTER);

        String[] labels   = { "⭐\nEasy",  "⭐⭐\nNormal", "⭐⭐⭐\nHard",  "⭐⭐⭐⭐\nExpert" };
        String[] bgColors = { "#4CC9F0",   "#80ED99",       "#F4A261",    "#ff6b6b" };

        for (int i = 1; i <= 4; i++) {
            final int level = i;
            Button btn = buildDiffButton(labels[i - 1], bgColors[i - 1]);

            btn.setOnAction(e -> {
                closeOverlay(gameRoot, dimOverlay, cardContainer);

                QuestionDefinition question = questionService.getQuestionForTile(tile, level);

                System.out.println("Selected difficulty: " + level
                        + " / question: " + (question != null ? question.getTitle() : "null"));

                QuestionCardView.show(question, tile, gameRoot,playerName, isCorrect -> {
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
        content.setMaxWidth(CARD_WIDTH);

        // Close button
        Button closeBtn = new Button("X");
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
                if (child != cardContainer && child != dimOverlay) {
                    child.setEffect(null);
                }
            }

            Image backBtnImage = new Image(DifficultyCardView.class.getResourceAsStream("/img/QuestionButton.png"));
            ImageView backBtnView = new ImageView(backBtnImage);
            backBtnView.setFitWidth(270);
            backBtnView.setPreserveRatio(true);
            backBtnView.setCursor(javafx.scene.Cursor.HAND);
            backBtnView.setOnMouseEntered(me -> backBtnView.setOpacity(0.8));
            backBtnView.setOnMouseExited(me  -> backBtnView.setOpacity(1.0));
            backBtnView.setOnMousePressed(me -> backBtnView.setScaleX(0.95));
            backBtnView.setOnMouseReleased(me -> backBtnView.setScaleX(1.0));

            Button backBtn = new Button();
            backBtn.setGraphic(backBtnView);
            backBtn.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-cursor: hand;");

            StackPane.setAlignment(backBtn, Pos.BOTTOM_CENTER);
            StackPane.setMargin(backBtn, new Insets(0, 0, 5, 0));
            gameRoot.getChildren().add(backBtn);

            backBtn.setOnAction(ev -> {
                gameRoot.getChildren().remove(backBtn);
                cardContainer.setVisible(true);
                dimOverlay.setVisible(true);
                for (var child : gameRoot.getChildren()) {
                    if (child != cardContainer && child != dimOverlay) {
                        child.setEffect(new GaussianBlur(12));
                    }
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

    private static Button buildDiffButton(String labelText, String bgColor) {
        Button btn = new Button(labelText);
        btn.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 12 16 12 16;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0.3, 0, 2);" +
                        "-fx-alignment: center;" +
                        "-fx-text-alignment: center;"
        );
        btn.setPrefWidth(125);
        btn.setPrefHeight(95);
        btn.setWrapText(true);

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: derive(" + bgColor + ", -15%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 12 16 12 16;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0.4, 0, 3);" +
                        "-fx-alignment: center;" +
                        "-fx-text-alignment: center;" +
                        "-fx-scale-x: 1.05;" +
                        "-fx-scale-y: 1.05;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 12 16 12 16;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0.3, 0, 2);" +
                        "-fx-alignment: center;" +
                        "-fx-text-alignment: center;"
        ));

        return btn;
    }

    private static void closeOverlay(StackPane gameRoot, Rectangle dimOverlay, StackPane cardContainer) {
        gameRoot.getChildren().remove(cardContainer);
        gameRoot.getChildren().remove(dimOverlay);
        for (var child : gameRoot.getChildren()) {
            child.setEffect(null);
        }
    }

    private static String cardImageFor(TileDefinition tile) {
        if (tile == null) return "/img/cardQuestionBlue.png";

        if ("THEME".equals(tile.getType()) && tile.getTheme() != null) {
            return switch (tile.getTheme().trim().toUpperCase()) {
                case "BLUE", "TECH", "IT", "COMPUTING", "INFORMATICS", "COMPUTER SCIENCE", "IT & PROGRAMMING" -> "/img/cardQuestionBlue.png";
                case "GREEN", "WORLD", "TOURISM", "TOURISM & TRAVEL" -> "/img/cardQuestionGreen.png";
                case "ORANGE", "ENTERTAINMENT" -> "/img/cardQuestionOrange.png";
                case "YELLOW", "STAR WARS" -> "/img/cardQuestionYellow.png";
                default -> "/img/cardQuestionBlue.png";
            };
        }

        if (tile.getType() != null) {
            return switch (tile.getType().toUpperCase()) {
                case "START", "DARK_VADOR", "VADER" -> "/img/cardQuestionYellow.png";
                case "ALL_IN" -> "/img/cardQuestionBlue.png";
                default -> "/img/cardQuestionBlue.png";
            };
        }

        return "/img/cardQuestionBlue.png";
    }

    public interface AnswerListener {
        void onAnswer(QuestionDefinition question, boolean isCorrect);
    }
}
