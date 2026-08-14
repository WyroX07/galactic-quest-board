package com.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class MenuController {

    @FXML
    private StackPane root;

    @FXML
    private ImageView backgroundImage;

    @FXML
    private StackPane rulesModalOverlay;

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

    /** Shows the rules overlay — reuses the same HelpModal.png image as the in-game Help button. */
    @FXML
    public void onRules() {
        rulesModalOverlay.setVisible(true);
    }

    @FXML
    public void onCloseRules() {
        rulesModalOverlay.setVisible(false);
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }
}
