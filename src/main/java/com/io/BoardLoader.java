package com.io;

import com.demoMapProjet.model.BoardDefinition;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class BoardLoader {

    public static BoardDefinition load(String path) {
        InputStream is = BoardLoader.class.getResourceAsStream(path);
        if (is == null) throw new RuntimeException("JSON introuvable: " + path);

        return new Gson().fromJson(
                new InputStreamReader(is, StandardCharsets.UTF_8),
                BoardDefinition.class
        );
    }
}