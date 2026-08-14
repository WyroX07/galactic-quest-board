package com.demoMapProjet.model;


public class PlayerToken {
    private String playerName;
    private int currentPosition = 0;
    private int shipNumber;

    public PlayerToken(String playerName, int shipNumber) {
        this.shipNumber = shipNumber;
        this.playerName = playerName;
    }

    public String getPlayerName() {

        return playerName;
    }

    public int getCurrentPosition() {

        return currentPosition;
    }

    public void setCurrentPosition(int position) {

        this.currentPosition = position;
    }

    public int getShipNumber() {
        return shipNumber;
    }
}
