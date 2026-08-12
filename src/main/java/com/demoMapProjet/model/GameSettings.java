package com.demoMapProjet.model;

public class GameSettings {
    private static int numberOfPlayers = 4;
    private static String difficulty = "easy";
    private static int timerSeconds = 30;
    public static int getNumberOfPlayers() {
        return numberOfPlayers;
    }
    public static void setNumberOfPlayers(int n) {
        numberOfPlayers = n;
    }

    public static String getDifficulty() {
        return difficulty;
    }
    public static void setDifficulty(String difficulty) {
        GameSettings.difficulty = difficulty;
    }

    public static int getTimerSeconds() { return timerSeconds; }
    public static void setTimerSeconds(int seconds) { timerSeconds = seconds; }
}
