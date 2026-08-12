package com.io;

import com.demoMapProjet.model.QuestPack;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


public class QuestLoader {

    public static QuestPack load(String path) {
        try {
            InputStream inputStream = QuestLoader.class.getResourceAsStream(path);

            if (inputStream == null) {
                throw new RuntimeException("Quest file not found: " + path);
            }

            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            Gson gson = new Gson();

            return gson.fromJson(reader, QuestPack.class);

        } catch (Exception e) {
            throw new RuntimeException("Error while loading quests: " + e.getMessage(), e);
        }
    }
}