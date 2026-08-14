package com.demoMapProjet.model;

public class TileDefinition {

    private int id;

    // relative position on the planet (0..1)
    private double rx;
    private double ry;

    private String type;   // START, THEME, ALL_IN, DARK_VADOR
    private String theme;  // ORANGE, BLUE, GREEN, YELLOW when type is THEME

    public int getId() {
        return id;
    }
    public double getRx() {
        return rx;
    }
    public double getRy() {
        return ry;
    }
    public String getType() {
        return type;
    }
    public String getTheme() {
        return theme;
    }
}