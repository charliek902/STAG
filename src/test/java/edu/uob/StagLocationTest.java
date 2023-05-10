package edu.uob;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class StagLocationTest {

    @Test
    void addAndRemovePlayer() {
        StagLocation location = new StagLocation("test location", "a test location");
        StagPlayer player = new StagPlayer("test player", "a test player", 3);
        location.addPlayer(player.getName(), player);
        assertTrue(location.getPlayers().containsKey(player.getName()));
        location.removePlayer(player.getName());
        assertFalse(location.getPlayers().containsKey(player.getName()));
    }

    @Test
    void addFurniture() {
        StagLocation location = new StagLocation("test location", "a test location");
        StagFurniture furniture = new StagFurniture("test furniture", "a test furniture");
        location.addFurniture(furniture.getName(), furniture);
        assertTrue(location.getFurniture().containsKey(furniture.getName()));
    }

    @Test
    void addCharacter() {
        StagLocation location = new StagLocation("test location", "a test location");
        StagCharacter character = new StagCharacter("test character", "a test character");
        location.addCharacter(character.getName(), character);
        assertTrue(location.getCharacters().containsKey(character.getName()));
    }

    @Test
    void addArtefact() {
        StagLocation location = new StagLocation("test location", "a test location");
        StagArtefact artefact = new StagArtefact("test artefact", "a test artefact");
        location.addArtefact(artefact.getName(), artefact);
        assertTrue(location.getArtefacts().containsKey(artefact.getName()));
    }

    @Test
    void addExit() {
        StagLocation location1 = new StagLocation("test location 1", "a test location");
        StagLocation location2 = new StagLocation("test location 2", "a test location");
        location1.addExit("exit", location2);
        assertTrue(location1.getExits().containsKey("exit"));
        assertEquals(location2, location1.getExit("exit"));
    }

    @Test
    void containsEntity() {
        StagLocation location = new StagLocation("test location", "a test location");
        StagFurniture furniture = new StagFurniture("test furniture", "a test furniture");
        location.addFurniture(furniture.getName(), furniture);
        assertTrue(location.containsEntity(furniture.getName()));
    }

    @Test
    void removeEntity() {
        StagLocation location = new StagLocation("test location", "a test location");
        StagFurniture furniture = new StagFurniture("test furniture", "a test furniture");
        location.addFurniture(furniture.getName(), furniture);
        location.removeEntity(furniture.getName());
        assertFalse(location.containsEntity(furniture.getName()));
    }

    @Test
    void getType() {
        StagLocation location = new StagLocation("test location", "a test location");
        StagFurniture furniture = new StagFurniture("test furniture", "a test furniture");
        location.addFurniture(furniture.getName(), furniture);
        assertEquals(EntityType.FURNITURE, location.getType(furniture.getName()));
    }










}
