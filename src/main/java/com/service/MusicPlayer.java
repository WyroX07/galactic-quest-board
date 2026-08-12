package com.service;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

/**
 * Singleton music player. Looks for MP3 files in src/main/resources/audio/.
 * Stays silent if a file is missing.
 */
public class MusicPlayer {

    private static MediaPlayer current;
    private static double volume = 0.5;   // default 50 %

    /** Starts (or restarts) the menu background music. */
    public static void playMenu() {
        play("/audio/menu_music.mp3");
    }

    /** Starts (or restarts) the in-game background music. */
    public static void playGame() {
        play("/audio/game_music.mp3");
    }

    /** Stop all music immediately. */
    public static void stop() {
        if (current != null) {
            current.stop();
            current.dispose();
            current = null;
        }
    }

    /** Plays a short "correct answer" sound effect, without touching the background music. */
    public static void playCorrectSound() {
        playSfx("/audio/correct.mp3");
    }

    /** Plays a short "wrong answer" sound effect, without touching the background music. */
    public static void playWrongSound() {
        playSfx("/audio/wrong.mp3");
    }

    /** Plays a short one-shot sound effect on its own player, disposed once finished. */
    private static void playSfx(String resourcePath) {
        URL url = MusicPlayer.class.getResource(resourcePath);
        if (url == null) return;

        try {
            Media media = new Media(url.toExternalForm());
            MediaPlayer sfx = new MediaPlayer(media);
            sfx.setVolume(volume);
            sfx.setOnEndOfMedia(sfx::dispose);
            sfx.play();
        } catch (Exception e) {
            System.err.println("[MusicPlayer] Could not play sound effect: " + e.getMessage());
        }
    }

    /** Sets the volume. Value must be between 0.0 and 1.0. */
    public static void setVolume(double vol) {
        volume = Math.max(0.0, Math.min(1.0, vol));
        if (current != null) {
            current.setVolume(volume);
        }
    }

    /** Returns the current volume (0.0 to 1.0). */
    public static double getVolume() {
        return volume;
    }

    private static void play(String resourcePath) {
        stop();

        URL url = MusicPlayer.class.getResource(resourcePath);
        if (url == null) {
            System.out.println("[MusicPlayer] File not found — place an MP3 at: "
                    + "src/main/resources" + resourcePath);
            return;
        }

        try {
            Media media = new Media(url.toExternalForm());
            current = new MediaPlayer(media);
            current.setCycleCount(MediaPlayer.INDEFINITE);
            current.setVolume(volume);
            current.play();
            System.out.println("[MusicPlayer] Playing: " + resourcePath);
        } catch (Exception e) {
            System.err.println("[MusicPlayer] Could not start playback: " + e.getMessage());
        }
    }
}
