package com.demoMapProjet.model;

import java.util.List;

public class BoardDefinition {

    private int tileSize;
    private List<TileDefinition> tiles;

    public int getTileSize() {
        return tileSize;
    }
    public List<TileDefinition> getTiles() {
        return tiles;
    }
}