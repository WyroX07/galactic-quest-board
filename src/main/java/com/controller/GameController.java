package com.controller;

import com.demoMapProjet.model.*;
import com.io.BoardLoader;
import com.io.QuestLoader;
import com.io.QuestionLoader;
import com.service.MusicPlayer;
import com.service.QuestService;
import com.service.QuestionService;
import com.ui.PlanetBoardView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import com.ui.ShipSelectionView;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.ArrayDeque;

public class GameController {

    private boolean gameOver = false;

    // Quest service for each player
    private Map<PlayerToken, QuestService> questServices = new HashMap<>();

    private MainController mainController;

    // FXML nodes
    @FXML private StackPane root;
    @FXML private ImageView backgroundImage;
    @FXML private Pane gamePane;

    // HUD buttons
    @FXML private Button helpButton;
    @FXML private Button settingsIGButton;
    @FXML private Button exitButton;

    // HelpModal overlay — only dynamic labels (static rule text is baked into HelpModal.png)
    @FXML private StackPane helpModalOverlay;
    @FXML private Label helpPlayerLabel;
    @FXML private Label helpQuestLabel;

    // Mini-HUD — current player + quest progress, always visible (no need to open Help)
    @FXML private Label hudPlayerLabel;
    @FXML private Label hudQuestLabel;

    // Game state
    private BoardDefinition board;
    private PlanetBoardView boardView;
    private List<PlayerToken> playerTokens = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private QuestionService questionService;
    private PlayerToken pendingQuestionPlayer;

    // Type of the tile the pending question was asked on, captured BEFORE
    // the answer is processed. Correct Vader answers move the player to a
    // freshly-chosen tile before this listener runs, so re-reading the
    // player's current tile at that point would see the new tile instead
    // of the one the question was actually about.
    private String pendingTileType;
    private final Queue<PlayerToken> initialStartQuestionQueue = new ArrayDeque<>();
    private boolean showingInitialStartQuestions = false;
    private boolean continuePromptShown = false;

    // Player currently shown in the HUD/Help modal — only changes when a new
    // turn actually starts, unlike currentPlayerIndex which advances right
    // after an answer, before the "Next player" button is even clicked.
    private PlayerToken activePlayer;

    @FXML
    public void initialize() {
        backgroundImage.fitWidthProperty().bind(root.widthProperty());
        backgroundImage.fitHeightProperty().bind(root.heightProperty());

        gamePane.prefWidthProperty().bind(root.widthProperty());
        gamePane.prefHeightProperty().bind(root.heightProperty());

        board = BoardLoader.load("/maps/mapboard.json");

        QuestionPack questionPack = QuestionLoader.load("/questions/questions.json");
        questionService = new QuestionService(questionPack);

        QuestLoader.load("/quests/quests.json");

        ShipSelectionView selection = new ShipSelectionView(GameSettings.getNumberOfPlayers(), this::startGame);
        root.getChildren().add(selection);

        setupDemoShortcut();
        preloadHyperspaceVideo();
    }

    private javafx.scene.media.MediaPlayer hyperspacePlayer;

    /**
     * Prepares the hyperspace video player as soon as the game screen loads,
     * while the player is still busy picking ships — by the time it's
     * actually needed, the underlying media engine has had time to warm up
     * instead of racing to get ready right when we call play().
     */
    private void preloadHyperspaceVideo() {
        java.net.URL videoUrl = getClass().getResource("/audio/hyperspace.mp4");
        if (videoUrl == null) return;
        try {
            hyperspacePlayer = new javafx.scene.media.MediaPlayer(
                    new javafx.scene.media.Media(videoUrl.toExternalForm()));
            hyperspacePlayer.setMute(true);
        } catch (Exception e) {
            System.err.println("[Hyperspace] Could not preload video: " + e.getMessage());
            hyperspacePlayer = null;
        }
    }

    /** F9 toggles a hidden demo panel to jump the active player to any tile type — for presentations only. */
    private void setupDemoShortcut() {
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                        javafx.scene.input.KeyCombination.keyCombination("F9"),
                        this::toggleDemoPanel);
            }
        });
    }

    private Node demoPanel;

    // Which player the demo panel will jump next — explicit, so the
    // presenter always knows who's about to be teleported.
    private PlayerToken demoSelectedPlayer;

    private void toggleDemoPanel() {
        if (demoPanel != null) {
            root.getChildren().remove(demoPanel);
            demoPanel = null;
            return;
        }
        if (demoSelectedPlayer == null && !playerTokens.isEmpty()) {
            demoSelectedPlayer = activePlayer != null ? activePlayer : playerTokens.get(currentPlayerIndex);
        }
        demoPanel = buildDemoPanel();
        root.getChildren().add(demoPanel);
    }

    /** Rebuilds the demo panel in place — used after changing the selected player. */
    private void refreshDemoPanel() {
        if (demoPanel == null) return;
        root.getChildren().remove(demoPanel);
        demoPanel = buildDemoPanel();
        root.getChildren().add(demoPanel);
    }

    private Node buildDemoPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setMaxWidth(220);
        panel.setStyle("-fx-background-color: rgba(10,10,15,0.92); "
                + "-fx-background-radius: 10; -fx-border-radius: 10; "
                + "-fx-border-color: #4ECDC4; -fx-border-width: 1.5; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 16, 0, 0, 4);");
        StackPane.setAlignment(panel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(panel, new Insets(0, 0, 24, 24));

        Label title = new Label("⚙ DEMO PANEL");
        title.setTextFill(Color.web("#4ECDC4"));
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
        panel.getChildren().add(title);

        // Who's about to be teleported — explicit and switchable.
        Label playerHint = new Label("JUMP THIS PLAYER");
        playerHint.setTextFill(Color.web("#888888"));
        playerHint.setFont(Font.font("Consolas", FontWeight.BOLD, 9));
        panel.getChildren().add(playerHint);

        HBox playerRow = new HBox(6);
        for (PlayerToken p : playerTokens) {
            boolean selected = p == demoSelectedPlayer;
            Button pBtn = new Button(p.getPlayerName());
            pBtn.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
            pBtn.setStyle(demoBtnStyle(selected ? "#4ECDC4" : "#333333", selected ? "#0a0a0a" : "#cccccc"));
            pBtn.setOnAction(e -> {
                demoSelectedPlayer = p;
                refreshDemoPanel();
            });
            playerRow.getChildren().add(pBtn);
        }
        panel.getChildren().add(playerRow);

        Label tileHint = new Label("JUMP TO TILE");
        tileHint.setTextFill(Color.web("#888888"));
        tileHint.setFont(Font.font("Consolas", FontWeight.BOLD, 9));
        panel.getChildren().add(tileHint);

        VBox tileButtons = new VBox(5);
        tileButtons.getChildren().addAll(
                demoJumpButton("Blue", "THEME", "BLUE", "#4CC9F0"),
                demoJumpButton("Green", "THEME", "GREEN", "#80ED99"),
                demoJumpButton("Orange", "THEME", "ORANGE", "#F4A261"),
                demoJumpButton("Yellow", "THEME", "YELLOW", "#FFD60A"),
                demoJumpButton("ALL-IN", "ALL_IN", null, "#C77DFF"),
                demoJumpButton("VADER", "DARK_VADOR", null, "#9B2226"),
                demoJumpButton("START — Final Assault", "START", null, "#D6D6D6")
        );
        panel.getChildren().add(tileButtons);

        Label afterHint = new Label("After answering, the normal turn\nflow resumes for the next player.");
        afterHint.setTextFill(Color.web("#666666"));
        afterHint.setFont(Font.font("Consolas", 9));
        panel.getChildren().add(afterHint);

        return panel;
    }

    private String demoBtnStyle(String bgColor, String textColor) {
        return "-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; "
                + "-fx-background-radius: 5; -fx-padding: 5 12; -fx-cursor: hand;";
    }

    private Button demoJumpButton(String label, String type, String theme, String bgColor) {
        Button btn = new Button(label);
        btn.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(demoBtnStyle(bgColor, "#0a0a0a"));
        btn.setOnAction(e -> demoJumpTo(type, theme));
        return btn;
    }

    /**
     * Jumps the selected player straight to a tile type and asks its
     * question. Also syncs the normal turn order to that player first, so
     * everything that happens afterward (the "Next player" button, whose
     * turn comes next) behaves exactly like a regular turn — fully
     * predictable, nothing demo-specific to remember.
     */
    private void demoJumpTo(String type, String theme) {
        if (playerTokens.isEmpty() || boardView == null || demoSelectedPlayer == null) return;

        currentPlayerIndex = playerTokens.indexOf(demoSelectedPlayer);
        pendingQuestionPlayer = demoSelectedPlayer;
        activePlayer = demoSelectedPlayer;
        pendingTileType = type;
        updateMiniHud();

        root.getChildren().remove(demoPanel);
        demoPanel = null;

        boardView.teleportToTileType(demoSelectedPlayer, type, theme);
    }

    private void startGame(List<ShipSelectionView.PlayerSelection> players) {
        root.getChildren().removeIf(node -> node instanceof ShipSelectionView);

        // Stop menu music
        MusicPlayer.stop();

        if (hyperspacePlayer == null) {
            // No video available (missing file or failed to preload) — skip straight to the game.
            MusicPlayer.playGame();
            proceedWithGameSetup(players);
            return;
        }

        javafx.scene.media.MediaView mv = new javafx.scene.media.MediaView(hyperspacePlayer);
        mv.fitWidthProperty().bind(root.widthProperty());
        mv.fitHeightProperty().bind(root.heightProperty());
        mv.setPreserveRatio(false);
        root.getChildren().add(mv);

        final boolean[] proceeded = {false};
        Runnable finishVideo = () -> {
            if (proceeded[0]) return;
            proceeded[0] = true;
            root.getChildren().remove(mv);
            mv.fitWidthProperty().unbind();
            mv.fitHeightProperty().unbind();
            hyperspacePlayer.setMute(true);
            MusicPlayer.playGame();
            proceedWithGameSetup(players);
        };

        hyperspacePlayer.setOnEndOfMedia(finishVideo);
        hyperspacePlayer.setOnError(finishVideo);
        hyperspacePlayer.setMute(false);
        hyperspacePlayer.seek(javafx.util.Duration.ZERO);
        hyperspacePlayer.play();

        // Safety net: never let a stuck video block the game from starting.
        javafx.animation.PauseTransition timeout = new javafx.animation.PauseTransition(
                javafx.util.Duration.seconds(6));
        timeout.setOnFinished(e -> finishVideo.run());
        timeout.play();
    }

    private void proceedWithGameSetup(List<ShipSelectionView.PlayerSelection> players) {

        boardView = new PlanetBoardView(board, questionService);
        boardView.setOnAnimationFinished(this::continueAfterResolvedTurn);

        boardView.setAnswerListener((question, isCorrect) -> {

            PlayerToken current = pendingQuestionPlayer != null
                    ? pendingQuestionPlayer
                    : playerTokens.get(currentPlayerIndex);
            pendingQuestionPlayer = null;
            QuestService qs = questServices.get(current);

            boolean isAllIn = "ALL_IN".equalsIgnoreCase(pendingTileType);
            boolean isVader = "DARK_VADOR".equalsIgnoreCase(pendingTileType)
                    || "VADER".equalsIgnoreCase(pendingTileType);
            boolean specialTileAlreadyMoved = isAllIn || isVader;

            currentPlayerIndex = (currentPlayerIndex + 1) % playerTokens.size();

            if (isCorrect && question != null) {
                int level = question.getDifficulte();
                String theme = QuestService.normalizeTheme(question.getTheme());

                qs.addCorrectAnswer(theme);
                System.out.println(current.getPlayerName() + " +" + level
                        + " tiles! [theme: " + theme + "]");

                if (qs.isActiveQuestCompleted()) {
                    System.out.println(current.getPlayerName() + " completed their quest! Return to START.");
                }

                // Refresh the HUD right away — no need to wait for the next
                // turn to see the objective progress update.
                updateMiniHud();

                // ALL_IN and VADER tiles already trigger their own movement in
                // PlanetBoardView — moving again here would risk starting the
                // next turn while the current player's animation is still playing.
                if (!specialTileAlreadyMoved) {
                    javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(
                            javafx.util.Duration.millis(400));
                    delay.setOnFinished(evt -> boardView.moveTokenBySteps(current, level));
                    delay.play();
                }

            } else {
                System.out.println(current.getPlayerName() + " wrong answer, stays in place.");

                // ALL_IN and VADER both retreat with an animation on a wrong
                // answer, so we wait for the animation-finished callback
                // instead of moving on right away.
                if (!specialTileAlreadyMoved) {
                    continueAfterResolvedTurn();
                }
            }
        });

        boardView.setGameRoot(root);

        // Tell PlanetBoardView which players have completed their quest
        boardView.setQuestCompletedChecker(token -> {
            QuestService qs = questServices.get(token);
            return qs != null && qs.isActiveQuestCompleted();
        });

        // Final assault win handler
        boardView.setOnFinalAssaultWin(() -> {
            PlayerToken winner = pendingQuestionPlayer != null
                    ? pendingQuestionPlayer
                    : playerTokens.get(currentPlayerIndex);
            pendingQuestionPlayer = null;
            currentPlayerIndex = (currentPlayerIndex + 1) % playerTokens.size();
            gameOver = true;
            System.out.println(winner.getPlayerName() + " WON THE FINAL ASSAULT!");
            showWinnerScreen(winner);
        });

        gamePane.getChildren().clear();
        gamePane.getChildren().add(boardView);
        boardView.prefWidthProperty().bind(gamePane.widthProperty());
        boardView.prefHeightProperty().bind(gamePane.heightProperty());

        QuestPack questPack = QuestLoader.load("/quests/quests.json");

        // Each player gets their own independently-drawn quest
        for (ShipSelectionView.PlayerSelection p : players) {
            PlayerToken token = new PlayerToken(p.name, p.shipNumber);
            playerTokens.add(token);
            boardView.ajouterToken(token);
            QuestService playerQuestService = new QuestService(questPack, GameSettings.getDifficulty());
            questServices.put(token, playerQuestService);
            System.out.println(token.getPlayerName() + " quest: "
                    + playerQuestService.getActiveQuest().getDescription());
        }

        initialStartQuestionQueue.clear();
        initialStartQuestionQueue.addAll(playerTokens);
        showingInitialStartQuestions = true;
        javafx.application.Platform.runLater(this::showNextInitialStartQuestion);
    }

    private void continueAfterResolvedTurn() {
        if (gameOver || continuePromptShown) return;
        continuePromptShown = true;
        if (showingInitialStartQuestions) {
            showContinueButton("Next player ▶", this::showNextInitialStartQuestion);
        } else {
            showContinueButton("Next player ▶", this::startNextTurn);
        }
    }

    private void startNextTurn() {
        if (gameOver) return;
        PlayerToken current = playerTokens.get(currentPlayerIndex);
        pendingQuestionPlayer = current;
        activePlayer = current;
        pendingTileType = tileTypeOf(current);
        updateMiniHud();
        boardView.mooveToken(current);
    }

    private void showNextInitialStartQuestion() {
        if (gameOver) return;
        PlayerToken nextPlayer = initialStartQuestionQueue.poll();
        if (nextPlayer == null) {
            showingInitialStartQuestions = false;
            pendingQuestionPlayer = null;
            startNextTurn();
            return;
        }
        pendingQuestionPlayer = nextPlayer;
        activePlayer = nextPlayer;
        pendingTileType = tileTypeOf(nextPlayer);
        updateMiniHud();
        boardView.askQuestionForCurrentTile(nextPlayer);
    }

    /** The type of tile a player is currently standing on (before any movement). */
    private String tileTypeOf(PlayerToken token) {
        TileDefinition tile = boardView.getCurrentTile(token);
        return tile != null ? tile.getType() : null;
    }

    /** Updates the always-visible mini-HUD with the active player's name and quest progress. */
    private void updateMiniHud() {
        if (activePlayer == null) {
            hudPlayerLabel.setText("");
            hudQuestLabel.setText("");
            return;
        }
        hudPlayerLabel.setText(activePlayer.getPlayerName().toUpperCase());
        hudQuestLabel.setText(questStatusText(activePlayer, false));
    }

    /** Quest status text: description + progress, or a completed message. */
    private String questStatusText(PlayerToken token, boolean detailed) {
        QuestService qs = questServices.get(token);
        if (qs == null || qs.getActiveQuest() == null) return "";
        if (qs.isActiveQuestCompleted()) {
            return detailed
                    ? "✅ QUEST COMPLETE!\nReturn to the START tile\nfor the Final Assault (difficulty 4)!"
                    : "Quest complete! Return to START";
        }
        return qs.getActiveQuest().getDescription() + "\n" + qs.getProgressText();
    }

    /**
     * Shows a "continue" button and waits for the player to click it before
     * running the next step, instead of an automatic delay — turn pacing is
     * up to the players, not a timer.
     */
    private void showContinueButton(String label, Runnable onContinue) {
        Button continueBtn = new Button(label);
        continueBtn.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        continueBtn.setStyle(
                "-fx-background-color: #4ECDC4; -fx-text-fill: #0a0a0a; "
                        + "-fx-padding: 14 40; -fx-cursor: hand; -fx-background-radius: 6; "
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 4);");
        StackPane.setAlignment(continueBtn, Pos.BOTTOM_CENTER);
        StackPane.setMargin(continueBtn, new Insets(0, 0, 60, 0));

        continueBtn.setOnAction(e -> {
            root.getChildren().remove(continueBtn);
            continuePromptShown = false;
            onContinue.run();
        });

        root.getChildren().add(continueBtn);
    }

    /**
     * Opens the HelpModal showing the current player's name and active quest.
     * The HelpModal.png image already contains all static rule text —
     * we only populate the two dynamic value fields.
     */
    @FXML
    private void onHelp() {
        if (activePlayer != null) {
            helpPlayerLabel.setText(activePlayer.getPlayerName().toUpperCase());
            String status = questStatusText(activePlayer, true);
            helpQuestLabel.setText(status.isEmpty() ? "—" : status);
        } else {
            helpPlayerLabel.setText("—");
            helpQuestLabel.setText("—");
        }
        helpModalOverlay.setVisible(true);
    }

    /** Closes the HelpModal overlay. */
    @FXML
    private void onCloseHelp() {
        helpModalOverlay.setVisible(false);
    }

    /** Opens the in-game Settings modal (music volume only). */
    @FXML
    private void onSettingsIG() throws Exception {
        mainController.showSettingsGame(root);
    }

    /** Shows an in-game confirmation overlay (no separate window, stays in fullscreen) before quitting. */
    @FXML
    private void onExitGame() {
        gamePane.setEffect(new GaussianBlur(10));

        Label title = new Label("QUIT GAME?");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#ff6b6b"));

        Label message = new Label("All progress will be lost.");
        message.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        message.setTextFill(Color.WHITE);

        Button cancelBtn = new Button("CANCEL");
        cancelBtn.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        cancelBtn.setStyle("-fx-background-color: #444; -fx-text-fill: white; "
                + "-fx-padding: 12 30; -fx-cursor: hand; -fx-background-radius: 4;");

        Button quitBtn = new Button("QUIT");
        quitBtn.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        quitBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: #0a0a0a; "
                + "-fx-padding: 12 30; -fx-cursor: hand; -fx-background-radius: 4;");

        HBox buttonsRow = new HBox(20, cancelBtn, quitBtn);
        buttonsRow.setAlignment(Pos.CENTER);

        VBox box = new VBox(20, title, message, buttonsRow);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(500);
        box.setMaxHeight(220);
        box.setStyle("-fx-background-color: rgba(0,0,0,0.75); "
                + "-fx-border-color: #ff6b6b; -fx-border-width: 2; "
                + "-fx-background-radius: 12; -fx-border-radius: 12; -fx-padding: 30;");

        StackPane overlay = new StackPane(box);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.35);");
        overlay.setAlignment(Pos.CENTER);

        cancelBtn.setOnAction(e -> {
            root.getChildren().remove(overlay);
            gamePane.setEffect(null);
        });

        quitBtn.setOnAction(e -> {
            root.getChildren().remove(overlay);
            gamePane.setEffect(null);
            gameOver = true;
            MusicPlayer.stop();
            try {
                mainController.showMenu();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        root.getChildren().add(overlay);
    }

    /**
     * Displays the end-of-game scoreboard: the winner, plus every player
     * ranked by their total correct answers.
     */
    private void showWinnerScreen(PlayerToken winner) {
        gamePane.setEffect(new GaussianBlur(10));

        Label winnerTitle = new Label("VICTORY");
        winnerTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 42));
        winnerTitle.setTextFill(Color.web("#4ECDC4"));

        Label winnerText = new Label(winner.getPlayerName().toUpperCase() + " HAS WON THE GAME!");
        winnerText.setFont(Font.font("Consolas", FontWeight.BOLD, 22));
        winnerText.setTextFill(Color.WHITE);

        List<PlayerToken> ranking = new ArrayList<>(playerTokens);
        ranking.sort((a, b) -> questServices.get(b).getTotalCorrectAnswers()
                - questServices.get(a).getTotalCorrectAnswers());

        VBox rankingBox = new VBox(6);
        rankingBox.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < ranking.size(); i++) {
            PlayerToken player = ranking.get(i);
            int score = questServices.get(player).getTotalCorrectAnswers();
            boolean isWinner = player == winner;

            Label row = new Label((i + 1) + ". " + player.getPlayerName()
                    + " — " + score + " correct answer" + (score == 1 ? "" : "s"));
            row.setFont(Font.font("Consolas", isWinner ? FontWeight.BOLD : FontWeight.NORMAL, 16));
            row.setTextFill(isWinner ? Color.web("#4ECDC4") : Color.web("#cccccc"));
            rankingBox.getChildren().add(row);
        }

        Button backButton = new Button();
        try {
            ImageView backImg = new ImageView(new Image(
                    getClass().getResourceAsStream("/img/BackToMenuSettings.png")));
            backImg.setFitWidth(260);
            backImg.setFitHeight(50);
            backImg.setPreserveRatio(false);
            backButton.setGraphic(backImg);
            backButton.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-cursor: hand;");
        } catch (Exception e) {
            backButton.setText("BACK TO MENU");
            backButton.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
            backButton.setStyle("-fx-background-color: #4ECDC4; -fx-text-fill: #0a0a0a; "
                    + "-fx-padding: 12 40; -fx-cursor: hand; -fx-background-radius: 4;");
        }
        backButton.setOnAction(e -> {
            gamePane.setEffect(null);
            try { mainController.showMenu(); } catch (Exception ex) { throw new RuntimeException(ex); }
        });

        VBox box = new VBox(20, winnerTitle, winnerText, rankingBox, backButton);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(650);
        box.setMaxHeight(420);
        box.setStyle("-fx-background-color: rgba(0,0,0,0.75); "
                + "-fx-border-color: #4ECDC4; -fx-border-width: 2; "
                + "-fx-background-radius: 12; -fx-border-radius: 12; -fx-padding: 40;");

        StackPane overlay = new StackPane(box);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.35);");
        overlay.setAlignment(Pos.CENTER);
        root.getChildren().add(overlay);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
