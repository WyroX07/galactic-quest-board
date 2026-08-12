package com.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class MenuController {

    @FXML
    private StackPane root;

    @FXML
    private ImageView backgroundImage;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        backgroundImage.fitWidthProperty().bind(root.widthProperty());
        backgroundImage.fitHeightProperty().bind(root.heightProperty());
    }

    @FXML
    private void onPlay() throws Exception {
        // Opens settings for player count, then launches the game
        mainController.showSettingsBeforeGame(root);
    }

    @FXML
    public void onOptions(ActionEvent actionEvent) throws Exception {
        mainController.showSettings(root);
    }

    @FXML
    public void onRules() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Les règles du jeu seront disponibles ici.", ButtonType.OK);
        alert.setTitle("Règles — Galactic Trials");
        alert.setHeaderText("📖  Règles du jeu");
        alert.showAndWait();
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }
}
