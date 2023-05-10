package edu.uob;

import java.util.*;

/* This class carries out any non-built in commands (action commands) from the user.
 If the command passes the checks within the executeAction command, it then proceeds to perform the
 action and alter the game state and player state. After this, it will then return the narration of the action back
 to the player. If the action does not pass the checks, the class will return an error message back to the user */

public class ActionExecutor {
    private final HashMap<String, HashSet<GameAction>> actionsMap;
    private final GameMap map;
    public ActionExecutor(HashMap<String, HashSet<GameAction>>actionsMap, GameMap gameMap) {
        this.actionsMap = actionsMap;
        this.map = gameMap;
    }

    public String executeAction(String trigger, StagPlayer currentPlayer, String command) {
        /* returns the actions associated with the trigger in the user command*/
        HashSet<GameAction> actions = actionsMap.get(trigger);
        /* Check if actions is null and return an error message */
        if (actions == null) {
            return "Invalid command- This action could not be performed in this game";
        }
        /* If there are more than one possible action with the same trigger in the location, this method
        * will set the matching action to null. It will then fail the first check in the next method */
        GameAction matchingAction = findMatchingAction(actions, currentPlayer, command);
        /* The matching command then goes into the checker which will then check the rest of the command and
        * if it is a valid one, it will return valid. If not, then it will return an appropriate response error*/
        String check = checks(matchingAction, command);
        if(check.equals("Valid")){
            return performAction(matchingAction, currentPlayer);
        }
        return check;
    }

    /* This method  searches through the GameActions to find any actions that are located in the
    player's current location or inventory and have a matching subject to the given command.
    If there is only one matching action, it returns that action.
    If there are multiple matching actions, it uses the containsAllWords method to choose the best matching action
    based on the words in the command. If no matching actions are found, it returns null. */

    private GameAction findMatchingAction(HashSet<GameAction> actions, StagPlayer currentPlayer, String command) {

        List<List<String>> matchingSubjects = new ArrayList<>();
        for (GameAction action : actions) {
            if (inPlayerLocationAndInventory(action, currentPlayer) && hasSubject(action, command)) {
                matchingSubjects.add(action.getSubjects());
            }
        }
        if (matchingSubjects.isEmpty()) {
            return null;
        }
        else if (matchingSubjects.size() == 1) {
            return getActionBySubject(actions, matchingSubjects.get(0));
        }
        else {
            String[] userCommandParts = command.split(" ");
            for (List<String> subjects : matchingSubjects) {
                if (containsAllSubjects(subjects, userCommandParts)) {
                    return getActionBySubject(actions, subjects);
                }
            }
            return null;
        }
    }

    /* this tests if all the subjects of an action are contained in a user command*/
    private boolean containsAllSubjects(List<String> subjects, String[] words) {
        for (String subject : subjects) {
            boolean found = false;
            for (String word : words) {
                if (subject.equals(word)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }


    /* this method gets an action based on certain subjects*/
    private GameAction getActionBySubject(HashSet<GameAction> actions, List<String> subjects) {
        for (GameAction action : actions) {
            if (action.getSubjects().equals(subjects)) {
                return action;
            }
        }
        return null;
    }

    /* this method does some checks on the action and if the check is valid and passes all the checks, this method will
    * return valid*/

    private String checks(GameAction action, String command){
        /* null can occur if there are no matching actions or there are too many matching actions*/
        if (action == null) {
            return "Invalid command- Please try and make the command more specific to match with an action";
        }
        /* The last check removes any possibility of commands either being composite or extraneous. If there
        * are entities in the user command, the command throws an error and if the user tries to access two commands
        * at the same time with some different subjects, this will also throw an error*/
        if (!isValidEntities(action, command)) {
            return "Invalid command- Carry out one sensible command at a time please OR do not include \n" +
                    "entities in the game that would have nothing to do with the action you want to take";
        }
        return "Valid";
    }

    /*this method checks for whether the player can actually carry out the action- it checks if the subjects
    of the action exist in either the player's location, in the player's inventory or the subject which is a location
    is the location which the player is in*/
    private boolean inPlayerLocationAndInventory(GameAction action, StagPlayer currentPlayer) {
        List<String> subjects = action.getSubjects();
        boolean allSubjectsFound = true;
        for (String subject : subjects) {
            if (!currentPlayer.getInventory().containsKey(subject)
                    && !currentPlayer.getLocation().containsEntityExcludingExits(subject)
                    && !currentPlayer.getLocation().getName().equals(subject)) {
                allSubjectsFound = false;
                break;
            }
        }
        return allSubjectsFound;
    }

    /* This method checks that there is at least one subject of the action within the command given by the user */
    private boolean hasSubject(GameAction action, String command) {
        String[] words = command.split(" ");
        for (String word : words) {
            if (action.getSubjects().contains(word)) {
                return true;
            }
        }
        return false;
    }

    /* the purpose of this method is to check whether there are other entities in the command other than
    * the subjects of the trigger of the action within the user*/
    /* this method helps check for composite commands and extraneous entities within the user command*/

    private boolean isValidEntities(GameAction action, String command) {
        List<String> entities = map.getEntities();
        String[] words = command.split(" ");
        List<String> actionSubjects = action.getSubjects();
        for (String word : words) {
            if (entities.contains(word) && !actionSubjects.contains(word)) {
                return false;
            }
        }
        return true;
    }

    /* this method will perform the action providing the action satisfies the checks above
    * it will manipulate the game state as a result of the valid action being processed and send
    * a narration back to the user */

    private String performAction(GameAction action, StagPlayer currentPlayer) {
        String result = consumeEntity(action, currentPlayer);
        produceEntity(action, currentPlayer);
        if(result.contains("you died")){
            return result;
        }
        return action.getNarration();
    }


    /* this method will consume the entity. If a player dies, this will be returned in the response string*/
    private String consumeEntity(GameAction action, StagPlayer currentPlayer){
        for (String consumedEntity : action.getConsumedEntities()) {
            if (consumedEntity.equals("health")) {
                String response = consumeHealth(currentPlayer);
                return response;
            }
            else{
                if(currentPlayer.getInventory().containsKey(consumedEntity)) {
                    consumeInventory(consumedEntity, map, currentPlayer);
                }
                StagLocation location = currentPlayer.getLocation();
                if (location.containsEntity(consumedEntity)) {
                    consumeEntityinLocation(consumedEntity, location);
                }
            }
        }
        return "";
    }

    /* this method consumes the player health and if a player dies, returns the narration that they died*/

    private String consumeHealth(StagPlayer currentPlayer){
        int health = currentPlayer.getHealth() - 1;
        if (health == 0) {
            currentPlayer.setHealth(3);
            currentPlayer.dropInventoryInLocation();
            currentPlayer.setLocation(map.getFirstLocation());
            return "you died and lost all of your items, you must return to the start of the game";
        }
        else {
            currentPlayer.setHealth(health);
        }
        return "";
    }

    /* if an artefact is consumed in the player's inventory, this code will remove the artefact from the inventory*/
    private void consumeInventory(String consumedEntity, GameMap map, StagPlayer currentPlayer){
        StagArtefact artefact = currentPlayer.getArtefactByName(consumedEntity);
        StagLocation storeroom = map.getStoreroom();
        storeroom.addArtefact(consumedEntity, artefact);
        currentPlayer.removeFromInventory(consumedEntity);
    }


    /* the code below will get the type of entity the removed entity is and if it is not
     * a location, then it will find its type and put it in the correct place within the
     * storeroom alongside its other entity types; this will help for future retrieval from
     * the storeroom and this code below would remove any paths to the removed location */

    private void consumeEntityinLocation(String consumedEntity, StagLocation location){
        location.removeEntity(consumedEntity);
        EntityType type = location.getType(consumedEntity);
        if (type == EntityType.ARTEFACT || type == EntityType.CHARACTER || type == EntityType.FURNITURE) {
            StagLocation storeroom = map.getStoreroom();
            if (type == EntityType.ARTEFACT) {
                StagArtefact artefact = map.getStagArtefact(consumedEntity);
                storeroom.addArtefact(consumedEntity, artefact);
            }
            else if (type == EntityType.CHARACTER) {
                StagCharacter character = map.getStagCharacter(consumedEntity);
                storeroom.addCharacter(consumedEntity, character);
            }
            else if (type == EntityType.FURNITURE) {
                StagFurniture furniture = map.getStagFurniture(consumedEntity);
                storeroom.addFurniture(consumedEntity, furniture);
            }
        }
    }

    /* this method will produce the entity whether it is health or other entity within the location that
    * the player is in*/


    // needs to be refactored...
    private void produceEntity(GameAction action, StagPlayer currentPlayer){
        for (String producedEntity : action.getProducedEntities()) {
            if (producedEntity.equals("health")) {
                if (currentPlayer.getHealth() == 3) {
                    break;
                }
                currentPlayer.setHealth(currentPlayer.getHealth() + 1);
            }
            else if (map.containsKey(producedEntity)) {
                StagLocation producedLocation = map.getLocation(producedEntity);
                currentPlayer.getLocation().addExit(producedEntity, producedLocation);
            }
            else {
                StagLocation storeroom = map.getStoreroom();
                if (storeroom.containsEntity(producedEntity)) {
                    EntityType type = storeroom.getType(producedEntity);
                    if (type == EntityType.ARTEFACT) {
                        StagArtefact artefact = map.getStagArtefact(producedEntity);
                        currentPlayer.getLocation().addArtefact(producedEntity, artefact);
                        storeroom.removeEntity(producedEntity);
                    }
                    else if (type == EntityType.CHARACTER) {
                        StagCharacter character = map.getStagCharacter(producedEntity);
                        currentPlayer.getLocation().addCharacter(producedEntity, character);
                        storeroom.removeEntity(producedEntity);
                    }
                    else if (type == EntityType.FURNITURE) {
                        StagFurniture furniture = map.getStagFurniture(producedEntity);
                        currentPlayer.getLocation().addFurniture(producedEntity, furniture);
                        storeroom.removeEntity(producedEntity);
                    }
                }
            }
        }
    }
}
