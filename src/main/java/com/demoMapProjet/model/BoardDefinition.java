package com.demoMapProjet.model;

import java.util.List;

public class BoardDefinition {

    private String name;
    private int tileSize;
    private Planet planet;
    private List<TileDefinition> tiles;

    public String getName() {
        return name;
    }
    public int getTileSize() {
        return tileSize;
    }
    public Planet getPlanet() {
        return planet;
    }
    public List<TileDefinition> getTiles() {
        return tiles;
    }

    public static class Planet {
        private String image;   // ex: "/img/planet.png"
        private double scale;   // ex: 0.78

        public String getImage() {
            return image;
        }
        public double getScale() {
            return scale;
        }
    }
}