package com.controller;

import com.service.MusicPlayer;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/** In-game settings controller. Music volume only — quitting the game has its own dedicated button. */
public class SettingsGameController {

    @FXML
    private AnchorPane root;

    @FXML
    public Slider musicSlider;

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
}
