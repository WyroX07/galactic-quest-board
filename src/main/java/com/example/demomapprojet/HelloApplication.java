package com.example.demomapprojet;
//test commit push
//test
import com.controller.MainController;
import com.demoMapProjet.model.BoardDefinition;
import com.io.BoardLoader;
import com.ui.PlanetBoardView;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception{
        MainController mainController = new MainController(stage);
        mainController.showMenu();
    }
}



//    @Override
//    public void start(Stage stage) {
//
//        // ✅ Taille de l’écran (zone visible, sans la barre des tâches)
//        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
//        double screenW = bounds.getWidth();
//        double screenH = bounds.getHeight();
//
//        BoardDefinition board = BoardLoader.load("/maps/galactic_planet_square.json");
//        PlanetBoardView view = new PlanetBoardView(board);
//
//        StackPane root = new StackPane();
//
//        // ✅ Pane du background qui suivra la taille de la fenêtre
//        StackPane backgroundPane = new StackPane();
//        backgroundPane.prefWidthProperty().bind(root.widthProperty());
//        backgroundPane.prefHeightProperty().bind(root.heightProperty());
//
//        try {
//            Image bgImg = new Image(new FileInputStream("src/main/resources/img/background.png"));
//
//            BackgroundSize bgSize = new BackgroundSize(
//                    100, 100,
//                    true, true,   // largeur/hauteur en %
//                    true,         // contain = true
//                    true          // cover = true  ✅ IMPORTANT
//            );
//
//            BackgroundImage bg = new BackgroundImage(
//                    bgImg,
//                    BackgroundRepeat.NO_REPEAT,
//                    BackgroundRepeat.NO_REPEAT,
//                    BackgroundPosition.CENTER,
//                    bgSize
//            );
//
//            backgroundPane.setBackground(new Background(bg));
//
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException("Image de fond introuvable", e);
//        }
//
//        // ✅ Ordre des couches
//        root.getChildren().addAll(backgroundPane, view);
//
//        // ✅ Scene dimensionnée selon l’écran
//        Scene scene = new Scene(root, screenW, screenH);
//
//        stage.setTitle(board.getName());
//        stage.setScene(scene);
//
//        // ✅ Place la fenêtre dans la zone visible
//        stage.setX(bounds.getMinX());
//        stage.setY(bounds.getMinY());
//        stage.setWidth(screenW);
//        stage.setHeight(screenH);
//
//        // Optionnel (si tu veux empêcher trop petit)
//        stage.setMinWidth(900);
//        stage.setMinHeight(600);
//
//        stage.show();
//    }
