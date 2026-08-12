package com.io;

import com.demoMapProjet.model.QuestionDefinition;
import com.demoMapProjet.model.QuestionPack;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class QuestionLoader {

    public static QuestionPack load(String path) {
        InputStream inputStream = QuestionLoader.class.getResourceAsStream(path);

        if (inputStream == null) {
            throw new RuntimeException("Question JSON file not found: " + path);
        }

        Gson gson = new Gson();
        JsonElement root = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        QuestionPack pack;

        if (root.isJsonArray()) {
            Type listType = new TypeToken<List<QuestionDefinition>>() {}.getType();
            List<QuestionDefinition> questions = gson.fromJson(root, listType);
            pack = new QuestionPack();
            pack.setQuestions(questions);
        } else {
            pack = gson.fromJson(root, QuestionPack.class);
        }

        if (pack == null) {
            throw new RuntimeException("Unable to parse question JSON file: " + path);
        }

        System.out.println("Question pack loaded: " + pack);
        System.out.println("Loaded questions: " + pack.getQuestions().size());

        return pack;
    }
}
