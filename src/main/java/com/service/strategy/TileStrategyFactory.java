package com.service.strategy;

import com.demoMapProjet.model.TileDefinition;

/**
 * Factory that creates the appropriate TileQuestionStrategy
 * based on the type and theme of a given tile.
 *
 * Tile type -> Strategy mapping:
 *   BLUE   (THEME) -> ThemeQuestionStrategy("IT & Programming")
 *   YELLOW (THEME) -> ThemeQuestionStrategy("Star Wars")
 *   GREEN  (THEME) -> ThemeQuestionStrategy("Tourism & Travel")
 *   ORANGE (THEME) -> ThemeQuestionStrategy("Entertainment")
 *   START          -> ThemeQuestionStrategy("Star Wars")  [same as YELLOW]
 *   VADER          -> VaderQuestionStrategy
 *   ALL_IN         -> ThemeQuestionStrategy("Entertainment") [any question, fallback]
 */
public class TileStrategyFactory {

    // Theme string constants matching questions.json
    public static final String THEME_BLUE   = "IT & Programming";
    public static final String THEME_YELLOW = "Star Wars";
    public static final String THEME_GREEN  = "Tourism & Travel";
    public static final String THEME_ORANGE = "Entertainment";

    /**
     * Returns the strategy that should be used for the given tile.
     *
     * @param tile the tile the player just landed on (must not be null)
     * @return a TileQuestionStrategy instance (never null)
     */
    public static TileQuestionStrategy getStrategy(TileDefinition tile) {
        if (tile == null) {
            // Default: use Star Wars (same as YELLOW/START)
            return new ThemeQuestionStrategy(THEME_YELLOW);
        }

        String type = tile.getType() != null ? tile.getType().toUpperCase() : "";

        // VADER tile: special difficulty-4 rule
        if ("DARK_VADOR".equals(type) || "VADER".equals(type)) {
            return new VaderQuestionStrategy();
        }

        // START tile: treated as YELLOW (Star Wars)
        if ("START".equals(type)) {
            return new ThemeQuestionStrategy(THEME_YELLOW);
        }

        // THEME tiles: dispatch by colour/theme
        if ("THEME".equals(type) && tile.getTheme() != null) {
            return switch (tile.getTheme().toUpperCase()) {
                case "BLUE"   -> new ThemeQuestionStrategy(THEME_BLUE);
                case "YELLOW" -> new ThemeQuestionStrategy(THEME_YELLOW);
                case "GREEN"  -> new ThemeQuestionStrategy(THEME_GREEN);
                case "ORANGE" -> new ThemeQuestionStrategy(THEME_ORANGE);
                default       -> new ThemeQuestionStrategy(THEME_YELLOW);
            };
        }

        // ALL_IN or unknown: default to YELLOW
        return new ThemeQuestionStrategy(THEME_YELLOW);
    }
}
