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

/* This file aims to test actions based on command flexibility in workbook 8. It also tests the consumption and
* production of entities and tests how the consumption and production of entities affects the game state. It also tests
* location as a subject and tests what happens when a player takes an action where a subject is in another player's
* inventory*/


class StudentIntegrationSTAGTestsTwo {

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
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
                    return server.handleCommand(command);
                },
                "Server took too long to respond (probably stuck in an infinite loop)");
    }


    /*This class tests if multiple triggers for the same action would cause that action to occur */
    @Test
    void TriggersInSameAction() {
        sendCommandToServer("simon: get axe");
        sendCommandToServer("SIMON: GOTO forEST");
        String response1 = sendCommandToServer("simon: cut, chop and cut down tree with an axe");
        assertEquals(response1, "You cut down the tree with the axe");
    }

    /* In forest, the user only needs to do a partial command to cut down a tree as there is only 1 action to do so
    * containing 2 subjects. */
    @Test
    void testPartialCommand() {
        sendCommandToServer("simon: get axe");
        sendCommandToServer("SIMON: GOTO forEST");
        String response0 = sendCommandToServer("simon: look");
        // the correct subject entities are there for the action to occur...
        assertTrue(response0.contains("tree"));
        String responseInv = sendCommandToServer("simon: inv");
        assertTrue(responseInv.contains("axe"));
        String response1 = sendCommandToServer("simon: cut tree");
        assertEquals(response1, "You cut down the tree with the axe");
    }

    /* this test tests if commands can be both case-insensitive and decorative*/
    @Test
    void testDecorativeAndCase() {
        sendCommandToServer("simon: get AXe");
        sendCommandToServer("SIMON: GOTO forEST");
        String response1 = sendCommandToServer("simon: plEASE coulD you CUT the TRee using An axe");
        assertEquals(response1, "You cut down the tree with the axe");
    }

    /* Repeats the test above but with a different word order which will produce the same result*/
    @Test
    void testWordOrder() {
        sendCommandToServer("simon: get axe");
        sendCommandToServer("SIMON: GOTO forEST");
        String response1 = sendCommandToServer("simon: please, with an axe, could you go to the tree and cut it down");
        assertEquals(response1, "You cut down the tree with the axe");
    }

    /* This test tests whether a user can do an inbuilt command and an action at the same time*/
    @Test
    void CompositeCommands1() {
        sendCommandToServer("simon: get axe");
        sendCommandToServer("SIMON: GOTO forEST");
        String response1 = sendCommandToServer("simon: cut the tree down and get the key from the forest");
        assertEquals(response1, "Invalid command- you can only do one command at a time");
    }

    /* This next test will test whether users can do 2 actions at the same time*/
    @Test
    void CompositeCommands2() {
        sendCommandToServer("Simon: goto forest");
        sendCommandToServer("Simon: get key");
        sendCommandToServer("Simon: goto cabin");
        // I have modified the action file so that users can use a key to open a door and a trapdoor within the cabin
        String response1 = sendCommandToServer("using the key, open the door and the trapdoor");
        assertEquals(response1, "Invalid command- Please try and make the command more specific to match with an action");
    }

    /* the cut/chop/cut down method can be applied in the forest and will produce a log as a result.
    * This test observes that there is no log in the forest before, and the after the action, the
    * log gets produced and the user will be able to see the log in the forest */
    @Test
    void ProducedFurniture() {
        sendCommandToServer("simon: get axe");
        sendCommandToServer("Simon: goto forest");
        String response1 = sendCommandToServer("simon: look");
        assertFalse(response1.contains("log"));
        sendCommandToServer("simon: cut the tree down");
        String response2 = sendCommandToServer("simon: look");
        assertTrue(response2.contains("log"));
    }

    /* This method effectively does the opposite for consumed entities. Consumed entities get removed from the location*/
    @Test
    void ConsumedFurniture() {
        sendCommandToServer("simon: get axe");
        sendCommandToServer("Simon: goto forest");
        String response1 = sendCommandToServer("simon: look");
        assertTrue(response1.contains("tree"));
        sendCommandToServer("simon: cut the tree down");
        String response2 = sendCommandToServer("simon: look");
        assertFalse(response2.contains("tree"));
    }


    /* This test, tests what if the production and consumption of locations works correctly. I have added
    two actions to the end of the student-actions which allow me to add and remove paths
    * from the cabin to the cellar. This test also tests locations as subjects but another test does that as well
    below */


    @Test
    void testLocations() {
        // as there is no path initially from the cabin to the cellar I will show that this is true
        String response0 = sendCommandToServer("Simon: goto cellar");
        assertEquals(response0, "Invalid command- You cannot go to that Location OR you cannot go to multiple locations at a time");
        // I then make the path using one of the actions that I made in the file I created
        String response1 = sendCommandToServer("Simon: add a path in the cabin");
        assertEquals(response1, "You added a path to the cellar from the cabin");
        // Now I will goto the cellar
        String response2 = sendCommandToServer("Simon: goto cellar");
        assertEquals(response2, "You go into a cellar");
        String response3 = sendCommandToServer("Simon: goto cabin");
        // now we go back into the cabin
        assertEquals(response3, "You go into a cabin");
        // now I will use the remove action to remove the path to the cellar and test whether the user
        // can go back to the cellar once the path is removed
        String response4 = sendCommandToServer("Simon: remove the path in the cabin");
        assertEquals(response4, "You removed the path from the cabin to the cellar");
        // path is removed, user will not be able to go into the cellar
        String response5 = sendCommandToServer("Simon: goto cellar");
        assertEquals(response5, "Invalid command- You cannot go to that Location OR you cannot go to multiple locations at a time");
    }

    /* This test tests if produced characters appear in the player's current location*/

    @Test
    void testProducedChars() {
        sendCommandToServer("Simon: goto forest");
        String response0 = sendCommandToServer("Simon: goto riverbank");
        String response1 = sendCommandToServer("simon: look");
        assertTrue(response1.contains("horn"));
        // lumberjack does not appear before
        assertFalse(response1.contains("lumberjack"));
        String response2 = sendCommandToServer("Simon: blow horn");
        assertEquals(response2, "You blow the horn and as if by magic, a lumberjack appears !");
        String response3 = sendCommandToServer("Simon: look");
        assertTrue(response3.contains("lumberjack"));
    }

    /* This method effectively does the opposite for consumed characters. Consumed characters get removed from the location
    * and put into the storeroom*/

    @Test
    void testConsumedChars() {
        sendCommandToServer("Simon: goto forest");
        sendCommandToServer("Simon: goto riverbank");
        sendCommandToServer("Simon: blow horn");
        String response1 = sendCommandToServer("Simon: now whack that lumberjack with the horn");
        assertEquals(response1, "You whacked the lumberjack, making him disappear");
        String response3 = sendCommandToServer("Simon: look");
        assertFalse(response3.contains("lumberjack"));
    }

    /* This method tests the production and consumption of artefacts where the consumed artefacts disappear from the
    * location and move to the storeroom. It also tests whether an artefact is removed from the player's inventory once
    * consumed*/

    @Test
    void testArtefacts() {
        sendCommandToServer("Simon: get potion");
        sendCommandToServer("Simon: goto forest");
        sendCommandToServer("Simon: goto riverbank");

        String response1 = sendCommandToServer("Simon: tap the horn");
        assertEquals(response1, "You touched the horn, it disappeared but was then replaced by gold");
        // now I will test whether the horn remains and whether silver has appeared in the player's location
        String response2 = sendCommandToServer("Simon: look");
        assertTrue(response2.contains("gold"));
        assertFalse(response2.contains("horn"));
        //I will now have the player drink the potion and I will test whether the potion still remains in the player's
        // inventory afterwards (it should not)
        String response3 = sendCommandToServer("Simon: inv");
        assertTrue(response3.contains("potion"));

        sendCommandToServer("Simon: drink the potion");
        String response4 = sendCommandToServer("Simon: inv");
        // this displays that the potion is no longer in the player's inventory
        assertFalse(response4.contains("potion"));

    }

    /* Player will go into the cellar. Testing if "add" action would work in the cellar despite "cabin" being a required subject.*/
    @Test
    void LocationAsSubject() {

        // potion in starting location
        sendCommandToServer("Peter: get potion");
        sendCommandToServer("Peter: goto forest");
        sendCommandToServer("Peter: get key");
        sendCommandToServer("Peter: goto cabin");
        sendCommandToServer("Peter: open trapdoor with key");
        sendCommandToServer("Peter: goto cellar");
        String response1 = sendCommandToServer("Peter: add path to cabin");
        // As you can see, if the player is not in the cabin the action cannot be carried out
        assertEquals(response1, "Invalid command- Please try and make the command more specific to match with an action");
        // now I will move back to the cabin and the action will be successful
        sendCommandToServer("Peter: goto cabin");
        String response2 = sendCommandToServer("Peter: add path to cabin");
        assertEquals(response2, "You added a path to the cellar from the cabin");
    }

    /* This method tests whether a player can carry out an action if another player, who is in the same location,
    * has a subject required to carry out the action in their inventory*/
    @Test
    void SubjectInOtherINV() {

        // potion in starting location
        sendCommandToServer("Simon: get potion");
        String response = sendCommandToServer("Peter: look");
        // shows they are both in the same location
        assertTrue(response.contains("simon"));
        String response0 = sendCommandToServer("Peter: could you have my character drink the potion");
        assertEquals(response0, "Invalid command- Please try and make the command more specific to match with an action");
        String response1 = sendCommandToServer("Simon: drink the potion");
        assertEquals(response1, "You drink the potion and your health improves");
    }

    /* In the actions files and the entity files I ensured that within the cabin there would be a trapdoor and a
    * door which could both be opened with a key*/
    @Test
    void AmbiguousCommands() {

        // ensures that the user gets the key from the forest
        sendCommandToServer("Peter: goto forest");
        sendCommandToServer("Peter: get key");
        sendCommandToServer("Peter: goto cabin");
        String response0 = sendCommandToServer("Peter: look");
        assertTrue(response0.contains("door"));
        assertTrue(response0.contains("trapdoor"));
        // the command below is too ambiguous, it could apply to two entities within the location
        String response1 = sendCommandToServer("Peter: open with key");
        assertEquals(response1, "Invalid command- Please try and make the command more specific to match with an action");
        // now I will be more specific and specify the trapdoor within the command rather than door. In doing so this
        // will open the trapdoor and not open the door
        String response2 = sendCommandToServer("Peter: open trapdoor with key");
        assertEquals(response2, "You unlock the door and see steps leading down into a cellar");
        // as you can see when you make the command more specific after it was ambiguous, it then carries out the action
    }

    /*This test tests if  a user can do multiple triggers from multiple actions */
    @Test
    void MultipleCommands() {

        // axe in starting location
        sendCommandToServer("simon: get axe");
        sendCommandToServer("simon: get potion");
        sendCommandToServer("simon: goto forest");

        // this ensures that the player will have the correct items and be
        // in the correct location to carry out 2 different actions
        String response = sendCommandToServer("drink potion and cut down the tree with an axe");
        assertEquals(response, "Invalid command- Please try and make the command more specific to match with an action");
    }

    /* This will test if there are extraneous entities in the user command*/
    @Test
    void ExtraneousEntities() {

        // ensures that the user gets the key from the forest
        sendCommandToServer("Peter: goto forest");
        sendCommandToServer("Peter: get key");
        sendCommandToServer("Peter: goto cabin");
        // we have already established that the command without shovel would work in the previous
        // test in AmbiguousCommands
        // this command should not work as it is using an entity that is in another location in the map
        String response2 = sendCommandToServer("Peter: open trapdoor using a key and a shovel");
        assertEquals(response2, "Invalid command- Carry out one sensible command at a time please OR do not include \n" +
                "entities in the game that would have nothing to do with the action you want to take");
    }

    /* I will do another extraneous entity test but this time the extraneous entity will be in the player's inventory*/
    @Test
    void ExtraneousEntities2() {

        // potion now in the player's inventory (potion in starting location)
        sendCommandToServer("Peter: get potion");
        sendCommandToServer("Peter: goto forest");
        sendCommandToServer("Peter: get key");
        sendCommandToServer("Peter: goto cabin");
        // we have already established that the command without shovel would work in the previous
        // test in AmbiguousCommands
        // this command should not work as it is using an entity that is
        String response2 = sendCommandToServer("Peter: open trapdoor using a key and a potion");
        assertEquals(response2, "Invalid command- Carry out one sensible command at a time please OR do not include \n" +
                "entities in the game that would have nothing to do with the action you want to take");
    }
}
