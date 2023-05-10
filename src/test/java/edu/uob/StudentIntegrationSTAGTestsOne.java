package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/* The files which are put into the entities and actions files are the extended actions and entities files
  but with added actions to allow me to test the full range of the functionality of the program*/

/* The Integrative tests in this file build on the ExampleSTAGTests*/

/* I split these integration tests into 2 classes in order not to assert a Timeout Preemptively */

/* This file fully tests INBUILT commands- GET, DROP, HEALTH, INV/INVENTORY, LOOK and GOTO
* This file also tests if other players can see each other in the map, it tests whether the correct
* actions occur in the gameState once a player dies. It also tests usernames of players and whether players
* enter into the same starting location
* */

class StudentIntegrationSTAGTestsOne {

    private GameServer server;

    // Create a new server _before_ every @Test

    @BeforeEach
    void setup() throws Exception {
        File entitiesFile = Paths.get("config" + File.separator + "student-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "student-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
    }

    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    /* this test tests whether commands from the user are case-insensitive*/
    @Test
    void CaseInsensitivity() {
        String response1 = sendCommandToServer("SIMON: GOTO forEST");
        assertTrue(response1.contains("You go into a forest"));
    }

    /* this test, tests whether the inventory, look, drop and get command can be decorative*/
    /* For built-in commands the word order does not matter. Decorative tests are the same as
    * testing the word order for inbuilt commands*/
    @Test
    void INBUILTAndDecorative() {
        sendCommandToServer("SIMON: GOTO FOREST");
        String response1 = sendCommandToServer("simon: please look around");
        assertTrue(response1.contains("key"));
        String response2 = sendCommandToServer("simon: could the player please walk around and look at the surroundings");
        assertTrue(response2.contains("key"));
        String response3 = sendCommandToServer("simon: please get the key");
        assertEquals(response3, "You picked up a key and placed it into your player's inventory");
        String response4 = sendCommandToServer("simon: please drop the key");
        assertEquals(response4, "You dropped a key in your current location");
        String response5 = sendCommandToServer("simon: could you look please");
        assertTrue(response5.contains("tree"));
        assertTrue(response5.contains("cabin"));
    }

    /* This tests whether artefacts are put into a player's inventory once they are got
    * and tests whether that artefact still exists in the location once got by the user */

    @Test
    void testInventory() {
        sendCommandToServer("SIMON: GOTO forEST");
        sendCommandToServer("simon: please get the key");
        // the key is now in Simon's inventory
        String response5 = sendCommandToServer("simon: shOW Me mY INVentory");
        assertTrue(response5.contains("key"));
        // the key is no longer in the location Simon was in...
        String response6 = sendCommandToServer("simon: look please");
        assertFalse(response6.contains("key"));
        // now will test the drop command and will test that the key is in the location
        // and no longer within Simon's inventory
        sendCommandToServer("simon: please drop the key");
        String response7 = sendCommandToServer("simon: show me my inv");
        assertFalse(response7.contains("key"));

    }

    /* this tests the get command where it tests if players can get paths and furniture*/
    @Test
    void GETPathsAndFurniture() {
        sendCommandToServer("SIMON: GOTO FOREST");
        sendCommandToServer("simon: please get the key");
        // this should return an error, which will contain "Invalid command"
        // as tree is regarded as furniture which cannot be picked up by any players
        String response1 = sendCommandToServer("simon: get the tree");
        assertEquals(response1, "Invalid command- You cannot pick up this/these items");
        // this proves that characters cannot pick up exits
        String response2 = sendCommandToServer("simon: get cabin");
        assertEquals(response2, "Invalid command- You cannot pick up this/these items");
    }

    /* This test verifies whether the 'goto' command restricts the player from moving to locations that
    are not currently accessible from their current location due to the absence of any connecting paths. */
    @Test
    void testGOTO() {
        sendCommandToServer("SIMON: GOTO FOREST");
        sendCommandToServer("simon: please get the key");
        sendCommandToServer("simon: goto cabin");
        String response1 = sendCommandToServer("simon: look");
        assertTrue(response1.contains("forest"));
        assertFalse(response1.contains("river"));
        // user will not be able to go to the river
        String response2 = sendCommandToServer("simon: goto river");
        assertEquals(response2, "Invalid command- You cannot go to that Location OR you cannot go to multiple locations at a time");
        // user will be able to go to the forest
        String response3 = sendCommandToServer("simon: goto forest");
        assertEquals(response3, "You go into a forest");
    }


    /* Here I test four usernames out to show that username validity is working*/
    @Test
    void testUsername() {
        // simon would satisfy a username
        String response1 = sendCommandToServer("SIMON: GOTO FOREST");
        assertEquals(response1, "You go into a forest");
        // "Karn_PK3" would not satisfy username validity due to presence of underscore
        String response2 = sendCommandToServer("Karn_PK3: GOTO FOREST");
        assertEquals(response2, "Invalid command- That is an invalid username");
        String response3 = sendCommandToServer("The$ilentAssassin");
        assertEquals(response3, "Invalid command- That is an invalid username");
        String response4 = sendCommandToServer("GOTO: look");
        assertEquals(response4, "Invalid command- That is an invalid username");

    }

    /* this test, tests if players can see each other in the starting location*/
    /* It also tests if users start at the same location*/
    @Test
    void StartingLocation() {

        String response1 = sendCommandToServer("SIMON: LOOK");
        assertTrue(response1.contains("trapdoor"));
        String response2 = sendCommandToServer("Peter: look");
        assertTrue(response2.contains("simon"));
        assertTrue(response2.contains("trapdoor"));
    }

    /* Here I test if players can see each other not just in the starting location*/
    /* this test makes Simon open the trapdoor and go into the cellar allowing Peter
    * to join him in there*/

    @Test
    void PlayersCanSeeEachOther() {

        sendCommandToServer("Simon: goto forest");
        sendCommandToServer("Simon: get key");
        sendCommandToServer("Simon: goto cabin");
        sendCommandToServer("Simon: open trapdoor with key");
        sendCommandToServer("Simon: goto cellar");
        sendCommandToServer("Peter: goto cellar");
        String response1 = sendCommandToServer("Peter: look");
        assertTrue(response1.contains("simon"));
        assertFalse(response1.contains("peter"));
        String response2 = sendCommandToServer("Simon: look");
        assertTrue(response2.contains("peter"));
        assertFalse(response2.contains("Joe"));
        assertFalse(response2.contains("simon"));
    }

    /* this tests the get command where it tests if players can get other players*/
    @Test
    void PlayersGETPlayers() {

        sendCommandToServer("Simon: goto forest");
        sendCommandToServer("Peter: goto forest");
        String response1 = sendCommandToServer("Peter: get Simon");
        assertEquals(response1, "Invalid command- You cannot pick up this/these items");
    }

    /* this tests the get command where it tests if players can get characters*/
    @Test
    void PlayersGETCharacters() {
        sendCommandToServer("Simon: goto forest");
        sendCommandToServer("Simon: get key");
        sendCommandToServer("Simon: goto cabin");
        sendCommandToServer("Simon: open trapdoor with key");
        sendCommandToServer("Simon: goto cellar");
        String response1 = sendCommandToServer("Simon: get elf");
        assertEquals(response1, "Invalid command- You cannot pick up this/these items");
    }

    /* This test will test the player's starting health and if a player can take damage to their health*/
    @Test
    void testHealth() {
        String response1 = sendCommandToServer("Peter: health");
        assertTrue(response1.equals("You have 3 health points left"));
        sendCommandToServer("Peter: goto forest");
        sendCommandToServer("Peter: get key");
        sendCommandToServer("Peter: goto cabin");
        sendCommandToServer("Peter: open trapdoor with key");
        sendCommandToServer("Peter: goto cellar");
        String response2 = sendCommandToServer("Peter: fight the elf");
        assertTrue(response2.contains("You attack the elf, but he fights back and you lose some health"));
        // I will now show Peter's health reduce using the inbuilt health command
        String response3 = sendCommandToServer("Peter: health");
        assertTrue(response3.equals("You have 2 health points left"));
        String response4 = sendCommandToServer("Peter: fight the elf");
        // despite Peter having an axe, he gets hurt from health
        assertTrue(response4.contains("You attack the elf, but he fights back and you lose some health"));
        // I will now show Peter's health reduce using the inbuilt health command
        String response5 = sendCommandToServer("Peter: health");
        assertTrue(response5.equals("You have 1 health points left"));
    }

    /* in this test, Peter will go and fight the elf 3 times, lose all his health and respawn at the
    * starting location. We know it is the starting location because the piece of furniture, the trapdoor,
    * cannot move in the game from the starting location. (there is no other trapdoor in the game) */
    @Test
    void testRespawn() {
        String response1 = sendCommandToServer("Peter: look");
        assertTrue(response1.contains("trapdoor"));
        sendCommandToServer("Peter: goto forest");
        sendCommandToServer("Peter: get key");
        sendCommandToServer("Peter: goto cabin");
        sendCommandToServer("Peter: open trapdoor with key");
        sendCommandToServer("Peter: goto cellar");
        sendCommandToServer("Peter: fight the elf");
        sendCommandToServer("Peter: fight the elf");
        //at this point the player will die
        String response2 = sendCommandToServer("Peter: fight the elf");
        assertEquals(response2, "you died and lost all of your items, you must return to the start of the game");
        String response3 = sendCommandToServer("Peter: look");
        assertTrue(response3.contains("trapdoor"));
    }

    /* this tests if Peter loses everything in his inventory and tests whether his inventory is now in the cellar */
    @Test
    void DropINVInLocation() {
        sendCommandToServer("Peter: get axe");
        sendCommandToServer("Peter: get potion");
        sendCommandToServer("Peter: goto forest");
        String response1 = sendCommandToServer("Peter: inventory");
        assertTrue(response1.contains("potion"));
        assertTrue(response1.contains("axe"));
        sendCommandToServer("Peter: get key");
        sendCommandToServer("Peter: goto cabin");
        sendCommandToServer("Peter: open trapdoor with key");
        sendCommandToServer("Peter: goto cellar");
        sendCommandToServer("Peter: fight the elf");
        sendCommandToServer("Peter: fight the elf");
        //at this point the player will die
        String response2 = sendCommandToServer("Peter: fight the elf");
        assertEquals(response2, "you died and lost all of your items, you must return to the start of the game");
        String response3 = sendCommandToServer("Peter: look");
        assertTrue(response3.contains("trapdoor"));
        String response4 = sendCommandToServer("Peter: inv");
        // here we can see Peter has nothing in his inventory
        assertEquals(response4, "");
        // now to go back to the cellar and observe that the potion and axe are there
        sendCommandToServer("Peter: goto cellar");
        String response5 = sendCommandToServer("Peter: look");
        assertTrue(response5.contains("axe"));
        assertTrue(response5.contains("potion"));
    }

    @Test
    void CompositeGETAndDROP() {
        String response1 = sendCommandToServer("Peter: look");
        assertTrue(response1.contains("axe"));
        assertTrue(response1.contains("potion"));
        String response2 = sendCommandToServer("Peter: get axe and potion");
        assertEquals(response2, "Invalid command- You cannot pick up this/these items");
        sendCommandToServer("Peter: get axe");
        sendCommandToServer("Peter: get potion");
        String response3 = sendCommandToServer("Peter: drop axe and potion");
        assertEquals(response3, "Invalid command- You either cannot drop two items at once OR you do not have that entity to drop");
        sendCommandToServer("Peter: drop axe");
        sendCommandToServer("Peter: drop potion");
        String response4 = sendCommandToServer("Peter: inv");
        // here we can see Peter has nothing in his inventory
        assertEquals(response4, "");
    }

    /* You cannot test drop as extraneous entities would not be in the inventory anyway...*/
    @Test
    void ExtraneousGET() {
        String response1 = sendCommandToServer("Peter: look");
        assertFalse(response1.contains("gold"));
        String response2 = sendCommandToServer("Peter: get gold");
        assertEquals(response2, "Invalid command- You cannot pick up this/these items");
    }

    /* test health production command, where health gets produced and the player's health increase */

    @Test
    void HealthIncrease() {
        String response1 = sendCommandToServer("Peter: health");
        assertTrue(response1.equals("You have 3 health points left"));
        sendCommandToServer("Peter: goto forest");
        sendCommandToServer("Peter: get key");
        sendCommandToServer("Peter: goto cabin");
        sendCommandToServer("Peter: open trapdoor with key");
        sendCommandToServer("Peter: goto cellar");
        String response2 = sendCommandToServer("Peter: fight the elf");
        assertTrue(response2.contains("You attack the elf, but he fights back and you lose some health"));
        // I will now show Peter's health reduce using the inbuilt health command
        String response3 = sendCommandToServer("Peter: health");
        assertTrue(response3.equals("You have 2 health points left"));
        // here the health of the player has reduced
        sendCommandToServer("Peter: goto cabin");
        String response4 = sendCommandToServer("Peter: drink the potion");
        assertEquals(response4, "You drink the potion and your health improves");
        // now to test whether Peter's health has gone up to 3
        String response5 = sendCommandToServer("Peter: health");
        assertEquals(response5, "You have 3 health points left");
    }

    @Test
    void MultipleGOTO() {
        sendCommandToServer("Peter: goto forest");
        String response = sendCommandToServer("Peter: goto riverbank and cabin");
        assertEquals(response, "Invalid command- You cannot go to that Location OR you cannot go to multiple locations at a time");
    }

    @Test
    void ExtraneousDROP() {
        sendCommandToServer("Peter: goto forest");
        String response = sendCommandToServer("Peter: drop potion");
        assertEquals(response, "Invalid command- You either cannot drop two items at once OR you do not have that entity to drop");
    }

}
