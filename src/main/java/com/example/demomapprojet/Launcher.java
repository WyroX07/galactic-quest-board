package com.example.demomapprojet;

import com.google.gson.Gson;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {

        Application.launch(HelloApplication.class, args);
        System.out.println(new Gson().toJson("ok"));

    }
}
