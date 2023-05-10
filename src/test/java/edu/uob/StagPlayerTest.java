package edu.uob;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class StagPlayerTest {


    @Test
    void testSetHealth() {
        StagPlayer player = new StagPlayer("player1", "description", 3);
        player.setHealth(2);
        assertEquals(2, player.getHealth());
    }

    @Test
    void testAddToInventory() {
        StagArtefact artefact = new StagArtefact("artefact", "description");
        StagPlayer player = new StagPlayer("player", "description", 3);
        player.addToInventory(artefact);
        assertTrue(player.getInventory().containsValue(artefact));
    }

    @Test
    void testGetHealth() {
        StagPlayer player = new StagPlayer("player", "description", 3);
        assertEquals(3, player.getHealth());
    }

    @Test
    void testHasArtefact() {
        StagArtefact artefact = new StagArtefact("artefact", "description");
        StagPlayer player = new StagPlayer("player", "description", 3);
        player.addToInventory(artefact);
        assertTrue(player.hasArtefact("artefact"));
    }

    @Test
    void testRemoveFromInventory() {
        StagArtefact artefact = new StagArtefact("artefact", "description");
        StagPlayer player = new StagPlayer("player", "description", 3);
        player.addToInventory(artefact);
        player.removeFromInventory("artefact");
        assertFalse(player.getInventory().containsValue(artefact));
    }

    @Test
    void testGetLocation() {
        StagLocation location = new StagLocation("location1", "description1");
        StagPlayer player = new StagPlayer("player1", "description", 3);
        player.setLocation(location);
        assertEquals(location, player.getLocation());
    }

    @Test
    void testGetArtefactByName() {
        StagArtefact artefact = new StagArtefact("artefact", "description");
        StagPlayer player = new StagPlayer("player", "description", 3);
        player.addToInventory(artefact);
        assertEquals(artefact, player.getArtefactByName("artefact"));
    }

    @Test
    void testDropInventoryInLocation() {
        StagArtefact artefact1 = new StagArtefact("artefact1", "description1");
        StagArtefact artefact2 = new StagArtefact("artefact2", "description2");
        StagLocation location = new StagLocation("location1", "description1");
        StagPlayer player = new StagPlayer("player", "description", 3);
        player.addToInventory(artefact1);
        player.addToInventory(artefact2);
        player.setLocation(location);
        player.dropInventoryInLocation();
        assertFalse(player.getInventory().containsValue(artefact1));
        assertFalse(player.getInventory().containsValue(artefact2));
        assertTrue(location.getArtefacts().containsValue(artefact1));
        assertTrue(location.getArtefacts().containsValue(artefact2));
    }
}
