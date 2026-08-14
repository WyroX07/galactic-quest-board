package com.example.demomapprojet;

import com.controller.MainController;
import javafx.application.Application;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception{
        MainController mainController = new MainController(stage);
        mainController.showMenu();
    }
}
