package com.service.strategy;

import com.demoMapProjet.model.TileDefinition;

/**
 * Factory that resolves the correct TileQuestionStrategy for a given tile.
 *
 * Tile type -> Strategy mapping:
 *   BLUE   (THEME) -> ThemeTileQuestionStrategy  (IT & Programming)
 *   YELLOW (THEME) -> ThemeTileQuestionStrategy  (Star Wars)
 *   GREEN  (THEME) -> ThemeTileQuestionStrategy  (Tourism & Travel)
 *   ORANGE (THEME) -> ThemeTileQuestionStrategy  (Entertainment)
 *   START          -> ThemeTileQuestionStrategy  (Star Wars, same as YELLOW)
 *   DARK_VADOR     -> VaderTileQuestionStrategy  (forced difficulty 4)
 *   ALL_IN         -> AllInTileQuestionStrategy  (Star Wars, custom move rules)
 */
public class TileQuestionStrategyFactory {

    private static final TileQuestionStrategy THEME_STRATEGY  = new ThemeTileQuestionStrategy();
    private static final TileQuestionStrategy VADER_STRATEGY  = new VaderTileQuestionStrategy();
    private static final TileQuestionStrategy ALL_IN_STRATEGY = new AllInTileQuestionStrategy();

    /**
     * Returns the appropriate strategy for the given tile.
     *
     * @param tile the tile the player just landed on (may be null)
     * @return a non-null TileQuestionStrategy
     */
    public TileQuestionStrategy getStrategy(TileDefinition tile) {
        if (tile == null || tile.getType() == null) {
            return THEME_STRATEGY;
        }

        return switch (tile.getType().toUpperCase()) {
            case "DARK_VADOR", "VADER" -> VADER_STRATEGY;
            case "ALL_IN"              -> ALL_IN_STRATEGY;
            default                    -> THEME_STRATEGY;
        };
    }
}
