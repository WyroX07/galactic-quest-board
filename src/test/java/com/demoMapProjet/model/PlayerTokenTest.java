package com.demoMapProjet.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the PlayerToken class.
 *
 * These tests verify:
 * - The constructor correctly initializes the player name and ship number
 * - The default position is set to 0
 * - The setter correctly updates the current position
 * - Multiple position updates behave correctly
 */

class PlayerTokenTest {

    @Test
    void testConstructorAndGetters() {
        // Create a PlayerToken object
        PlayerToken player = new PlayerToken("Alice", 3);

        // Check that constructor correctly sets values
        assertEquals("Alice", player.getPlayerName());
        assertEquals(3, player.getShipNumber());

        // Default position should be 0
        assertEquals(0, player.getCurrentPosition());
    }

    @Test
    void testSetCurrentPosition() {
        // Create a PlayerToken
        PlayerToken player = new PlayerToken("Bob", 1);

        // Change position
        player.setCurrentPosition(10);

        // Check that position is updated
        assertEquals(10, player.getCurrentPosition());
    }

    @Test
    void testMultiplePositionChanges() {
        // Create a PlayerToken
        PlayerToken player = new PlayerToken("Charlie", 2);

        // Change position multiple times
        player.setCurrentPosition(5);
        assertEquals(5, player.getCurrentPosition());

        player.setCurrentPosition(15);
        assertEquals(15, player.getCurrentPosition());
    }
}