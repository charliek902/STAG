package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.*;

/* In this class I test the whole game using the extended entities and actions files*/

public class TestWholeGame {

    private GameServer server;

    // Create a new server _before_ every @Test

    @BeforeEach
    void setup() throws Exception {
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
    }

    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    // this test goes through the whole game, doing tests on the way. If it does the whole test, the player should
    // be able to complete the game
    @Test
    void WholeGameTest() {

        sendCommandToServer("Peter: look");
        String look1 = sendCommandToServer("Simon: look");
        assertTrue(look1.contains("potion"));
        assertTrue(look1.contains("axe"));
        assertTrue(look1.contains("coin"));
        assertTrue(look1.contains("peter"));
        assertFalse(look1.contains("simon"));
        sendCommandToServer("Simon: get potion");
        sendCommandToServer("Simon: get axe");
        sendCommandToServer("Simon: get coin");
        String inv = sendCommandToServer("Simon: inv");
        assertTrue(inv.contains("potion"));
        assertTrue(inv.contains("axe"));
        assertTrue(inv.contains("coin"));
        String gotoCommand = sendCommandToServer("Simon: goto forest");
        assertEquals(gotoCommand, "You go into a forest");
        String look2 = sendCommandToServer("Simon: look");
        assertTrue(look2.contains("tree"));
        assertTrue(look2.contains("key"));
        sendCommandToServer("Simon: get key");
        String inv2 = sendCommandToServer("Simon: inventory");
        assertTrue(inv2.contains("key"));
        sendCommandToServer("Simon: cut down the tree with an axe");
        sendCommandToServer("Simon: get log");
        String inv3 = sendCommandToServer("Simon: inv");
        assertTrue(inv3.contains("log"));
        sendCommandToServer("Simon: goto cabin");
        sendCommandToServer("Simon: open the trapdoor with key");
        sendCommandToServer("Simon: goto cellar");
        String look3 = sendCommandToServer("Simon: look");
        assertTrue(look3.contains("elf"));
        sendCommandToServer("Simon: fight the elf");
        String health = sendCommandToServer("Simon: health");
        assertEquals(health, "You have 2 health points left");
        sendCommandToServer("Simon: drink the potion");
        String inv4 = sendCommandToServer("Simon: inv");
        assertFalse(inv4.contains("potion"));
        String health2 = sendCommandToServer("Simon: health");
        assertEquals(health2, "You have 3 health points left");
        sendCommandToServer("Simon: pay the elf");
        sendCommandToServer("Simon: get the shovel");
        String inv5 = sendCommandToServer("Simon: inv");
        assertTrue(inv5.contains("shovel"));
        sendCommandToServer("Simon: goto cabin");
        sendCommandToServer("Simon: goto forest");
        sendCommandToServer("Simon: goto riverbank");
        sendCommandToServer("Simon: drop the log");
        String inv6 = sendCommandToServer("Simon: inventory");
        assertFalse(inv6.contains("log"));
        String bridgeRiver = sendCommandToServer("Simon: bridge the river with the log ");
        assertEquals(bridgeRiver, "You bridge the river with the log and can now reach the other side");
        String look4 = sendCommandToServer("Simon: goto clearing");
        assertEquals(look4, "You go into a clearing");
        String finalCommand = sendCommandToServer("Simon: dig up the earth with a shovel");
        assertEquals(finalCommand, "You dig into the soft ground and unearth a pot of gold !!!");
        // game over
    }
}
