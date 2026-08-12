package com.controller;

import com.demoMapProjet.model.GameSettings;
import com.service.MusicPlayer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class SettingsController {

    @FXML private AnchorPane root;
    @FXML private ImageView backgroundImage;
    @FXML private Label playerCountLabel;
    @FXML private Label timerLabel;
    @FXML private Button backToMenuButton;
    @FXML private Slider musicSlider;

    private MainController mainController;

    @FXML
    private Label difficultyLabel;

    private final String[] difficulties = {"EASY", "MEDIUM", "HARD"};
    private int difficultyIndex = 1;


    @FXML
    public void initialize() {
        playerCountLabel.setText(String.valueOf(GameSettings.getNumberOfPlayers()));
        timerLabel.setText(GameSettings.getTimerSeconds() + "s");

        difficultyIndex = switch (GameSettings.getDifficulty()) {
            case "easy" -> 0;
            case "hard" -> 2;
            default -> 1;
        };
        difficultyLabel.setText(difficulties[difficultyIndex]);

        // Wire music slider — reflect current volume and update it on change
        if (musicSlider != null) {
            musicSlider.setValue(MusicPlayer.getVolume() * 100);
            musicSlider.valueProperty().addListener(
                    (obs, o, n) -> MusicPlayer.setVolume(n.doubleValue() / 100.0));
        }
    }

    // ── Back to Menu visibility ───────────────────────────────────────────
    /** When true, hides "Back to Menu" because the game starts right after closing. */
    public void setPreGameMode(boolean preGame) {
        if (backToMenuButton != null) {
            backToMenuButton.setVisible(!preGame);
            backToMenuButton.setManaged(!preGame);
        }
    }

    // ── PLAYERS ───────────────────────────────────────────────────────────
    @FXML
    private void onMinusPlayer() {
        int count = GameSettings.getNumberOfPlayers();
        if (count > 2) {
            count--;
            GameSettings.setNumberOfPlayers(count);
            playerCountLabel.setText(String.valueOf(count));
        }
    }

    @FXML
    private void onPlusPlayer() {
        int count = GameSettings.getNumberOfPlayers();
        if (count < 4) {
            count++;
            GameSettings.setNumberOfPlayers(count);
            playerCountLabel.setText(String.valueOf(count));
        }
    }

    // ── TIMER ─────────────────────────────────────────────────────────────
    @FXML
    private void onMinusTimer() {
        int time = GameSettings.getTimerSeconds();
        if (time > 10) {
            time -= 5;
            GameSettings.setTimerSeconds(time);
            timerLabel.setText(time + "s");
        }
    }

    @FXML
    private void onPlusTimer() {
        int time = GameSettings.getTimerSeconds();
        if (time < 120) {
            time += 5;
            GameSettings.setTimerSeconds(time);
            timerLabel.setText(time + "s");
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────
    @FXML
    private void onClose() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onBackToMenu() throws Exception {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
        mainController.showMenu();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public int getDifficultyIndex() {
        return difficultyIndex;
    }

    public void setDifficultyIndex(int difficultyIndex) {
        this.difficultyIndex = difficultyIndex;
    }

    @FXML
    private void onMinusDifficulty() {
        difficultyIndex--;

        if (difficultyIndex < 0) {
            difficultyIndex = difficulties.length - 1;
        }

        updateDifficultyLabel();
    }

    @FXML
    private void onPlusDifficulty() {
        difficultyIndex++;

        if (difficultyIndex >= difficulties.length) {
            difficultyIndex = 0;
        }

        updateDifficultyLabel();
    }

    private void updateDifficultyLabel() {
        difficultyLabel.setText(difficulties[difficultyIndex]);
        GameSettings.setDifficulty(difficulties[difficultyIndex].toLowerCase());
    }
}
