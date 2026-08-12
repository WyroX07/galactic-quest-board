package com.controller;

import com.service.MusicPlayer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainController {
    private final Stage stage;

    public MainController(Stage stage) {
        this.stage = stage;
    }

    public void showMenu() throws Exception {
        MusicPlayer.playMenu();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Menu.fxml"));
        Parent root = loader.load();

        MenuController menu = loader.getController();
        menu.setMainController(this);

        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.show();
    }

    public void showGame() throws Exception {
        // Menu music keeps playing through ship selection; GameController.startGame() handles the switch.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Game.fxml"));
        Parent root = loader.load();

        GameController game = loader.getController();
        game.setMainController(this);

        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
        stage.show();
    }

    /** Opens the settings modal (from menu or in-game). */
    public void showSettings(Parent root) throws Exception {
        root.setEffect(new GaussianBlur(10));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Settings.fxml"));
        Parent settingsRoot = loader.load();

        SettingsController controller = loader.getController();
        controller.setMainController(this);

        Scene scene = new Scene(settingsRoot);
        scene.setFill(Color.TRANSPARENT);

        Stage settingsStage = new Stage();
        settingsStage.initOwner(root.getScene().getWindow());
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.initStyle(StageStyle.TRANSPARENT);
        settingsStage.setResizable(false);
        settingsStage.setScene(scene);

        settingsStage.setOnHidden(e -> root.setEffect(null));
        settingsStage.show();
    }

    /** Opens settings before the game. Hides "Back to Menu" and launches the game on close. */
    public void showSettingsBeforeGame(Parent root) throws Exception {
        root.setEffect(new GaussianBlur(10));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Settings.fxml"));
        Parent settingsRoot = loader.load();

        SettingsController controller = loader.getController();
        controller.setMainController(this);
        controller.setPreGameMode(true);

        Scene scene = new Scene(settingsRoot);
        scene.setFill(Color.TRANSPARENT);

        Stage settingsStage = new Stage();
        settingsStage.initOwner(root.getScene().getWindow());
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.initStyle(StageStyle.TRANSPARENT);
        settingsStage.setResizable(false);
        settingsStage.setScene(scene);

        settingsStage.setOnHidden(e -> root.setEffect(null));
        settingsStage.showAndWait();

        showGame();
    }

    /** Opens the in-game settings modal (music volume only). */
    public void showSettingsGame(Parent root) throws Exception {
        root.setEffect(new GaussianBlur(10));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SettingsGame.fxml"));
        Parent settingsRoot = loader.load();

        Scene scene = new Scene(settingsRoot);
        scene.setFill(Color.TRANSPARENT);

        Stage settingsStage = new Stage();
        settingsStage.initOwner(root.getScene().getWindow());
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.initStyle(StageStyle.TRANSPARENT);
        settingsStage.setResizable(false);
        settingsStage.setScene(scene);

        settingsStage.setOnHidden(e -> root.setEffect(null));
        settingsStage.show();
    }
}
