package com.service;

import com.demoMapProjet.model.Quest;
import com.demoMapProjet.model.QuestPack;
import com.demoMapProjet.model.Requirement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Manages quest selection and per-player progress, tracked by theme. */
public class QuestService {

    private Quest activeQuest;

    // Key = normalized theme name, value = number of correct answers
    private final Map<String, Integer> correctAnswersByTheme = new HashMap<>();

    /** Picks a random quest from the difficulty pool. */
    public QuestService(QuestPack questPack, String difficulty) {
        List<Quest> quests = questPack.getQuests().get(difficulty.toLowerCase());

        if (quests == null || quests.isEmpty()) {
            throw new IllegalArgumentException("No quest found for difficulty: " + difficulty);
        }

        activeQuest = quests.get(new Random().nextInt(quests.size()));
    }

    /** Creates a QuestService tracking a specific pre-selected quest. */
    public QuestService(Quest quest) {
        this.activeQuest = quest;
    }

    public Quest getActiveQuest() {
        return activeQuest;
    }

    /** Records a correct answer for the given theme (normalized before storing). */
    public void addCorrectAnswer(String rawTheme) {
        String canonical = normalizeTheme(rawTheme);
        correctAnswersByTheme.put(canonical,
                correctAnswersByTheme.getOrDefault(canonical, 0) + 1);
    }

    /** Total correct answers across every theme, used to rank players on the scoreboard. */
    public int getTotalCorrectAnswers() {
        return correctAnswersByTheme.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Returns true when every quest requirement is satisfied. */
    public boolean isActiveQuestCompleted() {
        return activeQuest.isCompleted(correctAnswersByTheme);
    }

    /** Returns a progress string for the Help modal, e.g. "2/3 Tech/IT  •  0/1 Star Wars". */
    public String getProgressText() {
        if (activeQuest == null) return "";
        StringBuilder sb = new StringBuilder();
        for (Requirement req : activeQuest.getRequirements()) {
            int done = Math.min(
                    correctAnswersByTheme.getOrDefault(req.getTheme(), 0),
                    req.getCount());
            if (sb.length() > 0) sb.append("  •  ");
            sb.append(done).append("/").append(req.getCount())
              .append(" ").append(shortName(req.getTheme()));
        }
        return sb.toString();
    }

    /** Converts raw theme strings from questions.json to the canonical names used in quests.json. */
    public static String normalizeTheme(String rawTheme) {
        if (rawTheme == null) return "";
        return switch (rawTheme.trim().toUpperCase()) {
            case "IT & PROGRAMMING", "TECH", "IT", "COMPUTING",
                 "INFORMATICS", "COMPUTER SCIENCE"             -> "IT & Programming";
            case "STAR WARS"                                   -> "Star Wars";
            case "TOURISM & TRAVEL", "WORLD", "TOURISM"       -> "Tourism & Travel";
            case "ENTERTAINMENT"                               -> "Entertainment";
            default                                            -> rawTheme.trim();
        };
    }

    /** Short label used in progress text. */
    private static String shortName(String canonical) {
        return switch (canonical) {
            case "IT & Programming"  -> "Tech/IT";
            case "Star Wars"         -> "Star Wars";
            case "Tourism & Travel"  -> "Tourism";
            case "Entertainment"     -> "Entertain.";
            default                  -> canonical;
        };
    }
}
