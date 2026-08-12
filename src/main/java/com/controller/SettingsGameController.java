package com.controller;

import com.service.MusicPlayer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/** In-game settings controller. Handles the music slider and back-to-menu button. */
public class SettingsGameController {

    @FXML
    private AnchorPane root;

    @FXML
    public Button backToMenuButton;

    @FXML
    public Slider musicSlider;

    private MainController mainController;

    @FXML
    private void onBackToMenu() throws Exception {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
        if (mainController != null) {
            mainController.showMenu();
        }
    }

    @FXML
    public void onClose() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void initialize() {
        if (musicSlider != null) {
            musicSlider.setValue(MusicPlayer.getVolume() * 100);
            musicSlider.valueProperty().addListener(
                    (obs, o, n) -> MusicPlayer.setVolume(n.doubleValue() / 100.0));
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
