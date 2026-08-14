module com.example.demomapprojet {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.google.gson;
    requires javafx.graphics;

    // Needed for Gson to access private fields
    opens com.demoMapProjet.model to com.google.gson;

    opens com.controller to javafx.fxml;
    opens com.example.demomapprojet to javafx.fxml;
    opens com.ui to javafx.fxml;

    exports com.example.demomapprojet;
    exports com.ui;
    exports com.controller;
    exports com.service;
    exports com.demoMapProjet.model;
}