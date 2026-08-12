package com.demoMapProjet.model;

import java.util.List;
import java.util.Map;

/**
 * This class represents the whole JSON structure.
 * It groups all quests by difficulty (easy, medium, hard).
 */
public class QuestPack {

    // Key = difficulty ("easy", "medium", "hard")
    // Value = list of quests for that difficulty
    private Map<String, List<Quest>> quests;

    public Map<String, List<Quest>> getQuests() {
        return quests;
    }
}