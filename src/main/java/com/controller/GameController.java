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
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    }

    private void startGame(List<ShipSelectionView.PlayerSelection> players) {
        root.getChildren().removeIf(node -> node instanceof ShipSelectionView);

        // Stop menu music
        MusicPlayer.stop();

        // Play hyperspace video if the file exists
        java.net.URL videoUrl = getClass().getResource("/audio/hyperspace.mp4");
        if (videoUrl != null) {
            javafx.scene.media.Media video = new javafx.scene.media.Media(videoUrl.toExternalForm());
            javafx.scene.media.MediaPlayer vp = new javafx.scene.media.MediaPlayer(video);
            javafx.scene.media.MediaView mv = new javafx.scene.media.MediaView(vp);

            mv.fitWidthProperty().bind(root.widthProperty());
            mv.fitHeightProperty().bind(root.heightProperty());
            mv.setPreserveRatio(false);

            root.getChildren().add(mv);

            vp.setOnEndOfMedia(() -> {
                root.getChildren().remove(mv);
                mv.fitWidthProperty().unbind();
                mv.fitHeightProperty().unbind();
                vp.dispose();
                MusicPlayer.playGame();
                proceedWithGameSetup(players);
            });

            vp.setOnError(() -> {
                // Fallback if video fails to play
                root.getChildren().remove(mv);
                vp.dispose();
                MusicPlayer.playGame();
                proceedWithGameSetup(players);
            });

            // Wait for the player to be fully ready before playing — calling
            // play() right after creation is a race: the video track isn't
            // always prepared yet, so only the audio would start and the
            // image would stay frozen on the first frame.
            vp.setOnReady(vp::play);
            return;
        }

        // No video file found, start directly
        MusicPlayer.playGame();
        proceedWithGameSetup(players);
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

            TileDefinition currentTile = boardView.getCurrentTile(current);
            boolean isAllIn = currentTile != null
                    && currentTile.getType() != null
                    && "ALL_IN".equalsIgnoreCase(currentTile.getType());
            boolean isVader = currentTile != null
                    && currentTile.getType() != null
                    && ("DARK_VADOR".equalsIgnoreCase(currentTile.getType())
                    || "VADER".equalsIgnoreCase(currentTile.getType()));
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

                // Les cases ALL_IN et VADER lancent déjà leur propre déplacement
                // dans PlanetBoardView. On ne doit surtout pas relancer un
                // déplacement ici, sinon le tour suivant peut démarrer pendant
                // l'animation du joueur actuel.
                if (!specialTileAlreadyMoved) {
                    javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(
                            javafx.util.Duration.millis(400));
                    delay.setOnFinished(evt -> boardView.moveTokenBySteps(current, level));
                    delay.play();
                }

            } else {
                System.out.println(current.getPlayerName() + " wrong answer, stays in place.");

                // ALL_IN faux recule avec une animation : on attend le callback
                // d'animation. VADER faux retourne au départ instantanément, donc
                // on peut passer au tour suivant après le délai normal.
                if (!isAllIn) {
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
        updateMiniHud();
        boardView.askQuestionForCurrentTile(nextPlayer);
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

        Button backButton = new Button("BACK TO MENU");
        backButton.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        backButton.setStyle("-fx-background-color: #4ECDC4; -fx-text-fill: #0a0a0a; "
                + "-fx-padding: 12 40; -fx-cursor: hand; -fx-background-radius: 4;");
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
