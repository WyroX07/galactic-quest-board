package com.ui;

import com.demoMapProjet.model.QuestionDefinition;
import com.demoMapProjet.model.TileDefinition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.Cursor;

/**
 * Question pop-up overlay.
 *
 * All four colour variants (BLUE, GREEN, ORANGE, YELLOW) and all tile types
 * (THEME, START, ALL_IN, DARK_VADOR) share the exact same fixed dimensions:
 * CARD_WIDTH x CARD_HEIGHT.
 *
 * The question text and choices are wrapped properly so no text is cut off
 * with ellipsis ("..."). The VBox grows with its content and a ScrollPane
 * is used as a safety net for extremely long questions.
 */
public class QuestionCardView {

    // Unique fixed size used by EVERY variant — no more per-tile branching
    private static final double CARD_WIDTH  = 780;
    private static final double CARD_HEIGHT = 640;

    // Horizontal padding inside the card (left / right)
    private static final double PAD_LEFT  = 95;
    private static final double PAD_RIGHT = 40;

    // Usable content width for labels and radio buttons
    private static final double CONTENT_WIDTH = CARD_WIDTH - PAD_LEFT - PAD_RIGHT;



    /** Show question card without tile context (e.g. called by Vader strategy). */
    public static void show(QuestionDefinition question, StackPane gameRoot,
                            String playerName,java.util.function.Consumer<Boolean> onAnswer) {
        show(question, null, gameRoot, playerName, onAnswer);
    }

    /** Show question card with tile context (called by DifficultyCardView). */
    public static void show(QuestionDefinition question, TileDefinition tile,
                            StackPane gameRoot,
                            String playerName,java.util.function.Consumer<Boolean> onAnswer) {
        showInternal(question, gameRoot, playerName, onAnswer);
    }

    /** Kept for backward compatibility — delegates to the main path. */
    public static void showLarge(QuestionDefinition question, StackPane gameRoot,
                                 String playerName,java.util.function.Consumer<Boolean> onAnswer) {
        showInternal(question, gameRoot,playerName,onAnswer);
    }


    private static void showInternal(QuestionDefinition question, StackPane gameRoot,
                                     String playerName,java.util.function.Consumer<Boolean> onAnswer) {
        if (gameRoot == null) return;

        if (question == null) {
            showNoQuestion(gameRoot);
            return;
        }

        // Blur the current game content
        for (var child : gameRoot.getChildren()) {
            child.setEffect(new GaussianBlur(12));
        }

        // Semi-transparent overlay
        Rectangle dimOverlay = new Rectangle();
        dimOverlay.setFill(Color.rgb(0, 0, 20, 0.55));
        dimOverlay.widthProperty().bind(gameRoot.widthProperty());
        dimOverlay.heightProperty().bind(gameRoot.heightProperty());
        dimOverlay.setMouseTransparent(false);

        // Card container — same size for every theme
        String cardImagePath = cardImageFor(question);

        StackPane cardContainer = new StackPane();
        cardContainer.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        cardContainer.setMinSize(CARD_WIDTH, CARD_HEIGHT);

        // Card background image
        ImageView cardBg = new ImageView();
        try {
            Image cardImg = new Image(
                    QuestionCardView.class.getResourceAsStream(cardImagePath)
            );
            cardBg.setImage(cardImg);
        } catch (Exception e) {
            System.err.println("Unable to load card image: " + cardImagePath);
        }
        cardBg.setFitWidth(CARD_WIDTH);
        cardBg.setFitHeight(CARD_HEIGHT);
        cardBg.setPreserveRatio(false);

        Label playerLabel = new Label("C'est au tour de : " + playerName);
        playerLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-effect: dropshadow(gaussian, black, 4, 0.5, 0, 1);"
        );
        playerLabel.setWrapText(true);
        playerLabel.setMaxWidth(CONTENT_WIDTH);

        // --- Title ---
        Label titleLabel = new Label(question.getTitle());
        titleLabel.setStyle(
                "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-effect: dropshadow(gaussian, black, 4, 0.5, 0, 1);"
        );
        // wrapText + no maxHeight → label grows as needed, never truncates
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(CONTENT_WIDTH);
        titleLabel.setMinHeight(Region.USE_PREF_SIZE);
        titleLabel.setMaxHeight(Double.MAX_VALUE);

        // --- Question text ---
        Label questionLabel = new Label(question.getQuestion());
        questionLabel.setWrapText(true);
        questionLabel.setMaxWidth(CONTENT_WIDTH);
        // Remove any height cap so the full text is always visible
        questionLabel.setMinHeight(Region.USE_PREF_SIZE);
        questionLabel.setMaxHeight(Double.MAX_VALUE);
        questionLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: white;" +
                        "-fx-effect: dropshadow(gaussian, black, 3, 0.4, 0, 1);"
        );

        // --- Answer choices ---
        ToggleGroup group = new ToggleGroup();
        VBox choicesBox = new VBox(6);
        choicesBox.setAlignment(Pos.CENTER_LEFT);
        choicesBox.setMaxWidth(CONTENT_WIDTH);

        if (question.getChoices() != null) {
            for (String choice : question.getChoices()) {
                RadioButton rb = new RadioButton(choice);
                rb.setToggleGroup(group);
                rb.setWrapText(true);
                rb.setMaxWidth(CONTENT_WIDTH);
                rb.setStyle(
                        "-fx-text-fill: white;" +
                                "-fx-font-size: 14px;" +
                                "-fx-effect: dropshadow(gaussian, black, 2, 0.4, 0, 1);"
                );
                choicesBox.getChildren().add(rb);
            }
        }

        // --- Result label ---
        Label resultLabel = new Label();
        resultLabel.setWrapText(true);
        resultLabel.setMaxWidth(CONTENT_WIDTH);
        resultLabel.setMinHeight(Region.USE_PREF_SIZE);
        resultLabel.setMaxHeight(Double.MAX_VALUE);
        resultLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");


        // Countdown
        javafx.animation.Timeline countdown = new javafx.animation.Timeline();
        final int[] timeLeft = {com.demoMapProjet.model.GameSettings.getTimerSeconds()};

        // --- Confirm button ---
        String confirmImagePath = confirmImageFor(question);
        Button validateBtn = new Button();
        try {
            ImageView confirmImg = new ImageView(
                    new Image(QuestionCardView.class.getResourceAsStream(confirmImagePath))
            );
            confirmImg.setFitWidth(260);
            confirmImg.setFitHeight(1000);
            confirmImg.setPreserveRatio(true);
            validateBtn.setGraphic(confirmImg);
            validateBtn.setStyle("-fx-background-color: transparent; -fx-padding: 5; -fx-cursor: hand;");
        } catch (Exception e) {
            validateBtn.setText("Confirm");
            validateBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        }

        final boolean[] answerConfirmed = {false};

        // Tracks the "back to card" button created when the player hides the
        // card with the X button, so it can be removed no matter how the
        // overlay ends up closing (validate, X, or timeout).
        final Button[] hiddenBackButton = {null};

        validateBtn.setOnAction(e -> {
            if (answerConfirmed[0]) return;
            countdown.stop();
            if (group.getSelectedToggle() == null) {
                resultLabel.setText("Choose an answer!");
                resultLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 13px; -fx-font-weight: bold;");
                return;
            }

            answerConfirmed[0] = true;
            choicesBox.setDisable(true);
            validateBtn.setDisable(true);

            RadioButton selected = (RadioButton) group.getSelectedToggle();
            boolean isCorrect = selected.getText().equalsIgnoreCase(question.getAnswer());

            if (isCorrect) {
                resultLabel.setText("Correct answer!");
                resultLabel.setStyle("-fx-text-fill: lightgreen; -fx-font-size: 13px; -fx-font-weight: bold;");
            } else {
                resultLabel.setText("Wrong answer. Correct answer: " + question.getAnswer());
                resultLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 13px; -fx-font-weight: bold;");
            }

            PauseTransition closeDelay = new PauseTransition(Duration.seconds(1.5));
            closeDelay.setOnFinished(event -> {
                closeOverlay(gameRoot, dimOverlay, cardContainer, hiddenBackButton[0]);
                onAnswer.accept(isCorrect);
            });
            closeDelay.play();
        });

        // --- Close (X) button ---
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
            countdown.stop();
            for (var child : gameRoot.getChildren()) {
                if (child != cardContainer && child != dimOverlay) {
                    child.setEffect(null);
                }
            }

            Image backBtnImage = new Image(QuestionCardView.class.getResourceAsStream("/img/QuestionButton.png"));
            ImageView backBtnView = new ImageView(backBtnImage);
            backBtnView.setFitWidth(270);
            backBtnView.setPreserveRatio(true);
            backBtnView.setCursor(Cursor.HAND);

            // Hover effect
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
            hiddenBackButton[0] = backBtn;

            backBtn.setOnAction(ev -> {
                gameRoot.getChildren().remove(backBtn);
                hiddenBackButton[0] = null;
                cardContainer.setVisible(true);
                dimOverlay.setVisible(true);
                for (var child : gameRoot.getChildren()) {
                    if (child != cardContainer && child != dimOverlay) {
                        child.setEffect(new GaussianBlur(12));
                    }
                }
            });
        });

        // --- Content VBox ---

        // --- Timer ---
        int totalSeconds = com.demoMapProjet.model.GameSettings.getTimerSeconds();
        Label timerLabel = new Label(" ⏳ " + totalSeconds + "s");
        timerLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );
        timerLabel.setAlignment(Pos.CENTER);

        javafx.animation.KeyFrame tick = new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(1),
                event -> {
                    timeLeft[0]--;
                    String hourglass = (timeLeft[0] % 2 == 0) ? "⏳" : "⌛";
                    timerLabel.setText(hourglass + " " + timeLeft[0] + "s");

                    if (timeLeft[0] <= 5) {
                        timerLabel.setStyle(
                                "-fx-font-size: 20px;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-text-fill: #ff4444;"
                        );
                    }

                    if (timeLeft[0] <= 0) {
                        countdown.stop();
                        resultLabel.setText("Temps écoulé !");
                        resultLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 13px; -fx-font-weight: bold;");
                        choicesBox.setDisable(true);
                        validateBtn.setDisable(true);

                        PauseTransition closeDelay2 = new PauseTransition(Duration.seconds(1.5));
                        closeDelay2.setOnFinished(ev -> {
                            closeOverlay(gameRoot, dimOverlay, cardContainer, hiddenBackButton[0]);
                            onAnswer.accept(false);
                        });
                        closeDelay2.play();
                    }
                }
        );

        countdown.getKeyFrames().add(tick);
        countdown.setCycleCount(totalSeconds);
        countdown.play();

        // principal layout
        VBox topContent = new VBox(10, playerLabel, timerLabel, titleLabel, questionLabel, choicesBox);
        topContent.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(topContent, Priority.ALWAYS);

        // Horizontally centered button
        HBox validateRow = new HBox(validateBtn);
        validateRow.setAlignment(Pos.CENTER);
        validateRow.setPickOnBounds(false);

        // Left-aligned result label—transparent at clicks so as not to block the buttonn
        resultLabel.setMaxWidth(CARD_WIDTH - PAD_LEFT - PAD_RIGHT);
        resultLabel.setMouseTransparent(true);

        // Low area: StackPane superposes centered button + result on the left
        StackPane bottomZone = new StackPane();
        bottomZone.setPrefHeight(70);
        bottomZone.setPickOnBounds(false);
        StackPane.setAlignment(validateRow, Pos.CENTER);
        StackPane.setAlignment(resultLabel, Pos.BOTTOM_LEFT);
        bottomZone.getChildren().addAll(validateRow, resultLabel);

        VBox content = new VBox(10, topContent, bottomZone);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(50, PAD_RIGHT, 20, PAD_LEFT));
        content.setMaxWidth(CARD_WIDTH);
        content.setMaxHeight(CARD_HEIGHT - 20);
        content.setMinHeight(Region.USE_PREF_SIZE);

        StackPane.setAlignment(content, Pos.TOP_LEFT);

        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        StackPane innerCard = new StackPane(cardBg, content, closeBtn);
        StackPane.setMargin(closeBtn, new Insets(10, 10, 0, 0));

        cardContainer.getChildren().add(innerCard);

        gameRoot.getChildren().add(dimOverlay);
        gameRoot.getChildren().add(cardContainer);
    }



    /**
     * Closes the question overlay. strayBackButton is the "back to card"
     * button created when the player hid the card with X — it must be
     * removed here too, otherwise it stays stranded on screen and the next
     * question card ends up visually stacked on top of it.
     */
    private static void closeOverlay(StackPane gameRoot, Rectangle dimOverlay, Node cardContainer,
                                     Button strayBackButton) {
        gameRoot.getChildren().remove(cardContainer);
        gameRoot.getChildren().remove(dimOverlay);
        if (strayBackButton != null) {
            gameRoot.getChildren().remove(strayBackButton);
        }
        for (var child : gameRoot.getChildren()) {
            child.setEffect(null);
        }
    }

    private static void showNoQuestion(StackPane gameRoot) {
        for (var child : gameRoot.getChildren()) {
            child.setEffect(new GaussianBlur(8));
        }

        Rectangle dimOverlay = new Rectangle();
        dimOverlay.setFill(Color.rgb(0, 0, 20, 0.55));
        dimOverlay.widthProperty().bind(gameRoot.widthProperty());
        dimOverlay.heightProperty().bind(gameRoot.heightProperty());

        Label msg = new Label("No question available for this tile.");
        msg.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");

        VBox box = new VBox(20, msg, closeBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));
        box.setStyle(
                "-fx-background-color: rgba(30, 60, 114, 0.92);" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 2;"
        );
        box.setMaxSize(420, 200);

        closeBtn.setOnAction(e -> {
            gameRoot.getChildren().remove(box);
            gameRoot.getChildren().remove(dimOverlay);
            for (var child : gameRoot.getChildren()) {
                child.setEffect(null);
            }
        });

        gameRoot.getChildren().add(dimOverlay);
        gameRoot.getChildren().add(box);
    }

    private static String cardImageFor(QuestionDefinition question) {
        if (question.getTheme() == null) {
            String type = question.getType();
            if (type != null) {
                return switch (type.toUpperCase()) {
                    case "ALL_IN"     -> "/img/cardQuestionBlue.png";
                    case "DARK_VADOR" -> "/img/cardQuestionOrange.png";
                    default           -> "/img/cardQuestionBlue.png";
                };
            }
            return "/img/cardQuestionBlue.png";
        }
        return switch (question.getTheme().trim().toUpperCase()) {
            case "BLUE", "TECH", "IT", "COMPUTING", "INFORMATICS",
                 "COMPUTER SCIENCE", "IT & PROGRAMMING"             -> "/img/cardQuestionBlue.png";
            case "GREEN", "WORLD", "TOURISM", "TOURISM & TRAVEL"   -> "/img/cardQuestionGreen.png";
            case "ORANGE", "ENTERTAINMENT"                          -> "/img/cardQuestionOrange.png";
            case "YELLOW", "STAR WARS"                              -> "/img/cardQuestionYellow.png";
            default                                                 -> "/img/cardQuestionBlue.png";
        };
    }

    private static String confirmImageFor(QuestionDefinition question) {
        if (question.getTheme() == null) {
            return "/img/btnConfirmBlue.png";
        }
        return switch (question.getTheme().trim().toUpperCase()) {
            case "BLUE", "TECH", "IT", "COMPUTING", "INFORMATICS",
                 "COMPUTER SCIENCE", "IT & PROGRAMMING"             -> "/img/btnConfirmBlue.png";
            case "GREEN", "WORLD", "TOURISM", "TOURISM & TRAVEL"   -> "/img/btnConfirmGreen.png";
            case "ORANGE", "ENTERTAINMENT"                          -> "/img/btnConfirmOrange.png";
            case "YELLOW", "STAR WARS"                              -> "/img/btnConfirmYellow.png";
            default                                                 -> "/img/btnConfirmBlue.png";
        };
    }
}
