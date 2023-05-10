package edu.uob;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;


/* this class represents the gameEngine of the game, which takes in the gamemap, games actions
* and the current players of the game. The game engine takes in a method which receives the user command,
* which then alters the game state and returns the appropriate response back to the user*/

public class GameEngine {
    private final GameMap gameMap;
    private final HashMap<String, HashSet<GameAction>> actionsMap;
    private HashMap<String, StagPlayer> players;

    public GameEngine(GameMap gameMap, HashMap<String, HashSet<GameAction>> actionsMap) {
        this.gameMap = gameMap;
        this.actionsMap = actionsMap;
        this.players = new HashMap<>();
    }

    /* this method will first get the player's game state information if they are already in the
     game. If they are not in the game, they will be added to the starting location of the map.
     This method then checks if the user command is a basic command (and not an action command)
     and if it is, it will process that command and return the result back to the user. If it is an action
     command it does the same but with actions. If the command is neither a basic command or an action
     command, it returns to the user that the command could not be carried out */

    public String handleCommand(String command, GameMap map) {

        String result = "Invalid command- you either have not entered a command or the action cannot be done in this game";
        /* the two functions below will get the username from the command and check if the username is valid */
        String username = getUsername(command);
        if(!isValidPlayerName(username)){
            return "Invalid command- That is an invalid username";
        }
        if(command.equals("")){
            return "You need to enter a username and a command";
        }
        /* now will get the player and its information depending on the username given */
        StagPlayer player = getPlayer(map, username);
        /* Two conditions check if the command is a basic or action command. Will proceed to interpret them*/
        if(isValidBasicCommand(command) && !hasActionTrigger(command)){
            BasicCommandHandler inBuilt = new BasicCommandHandler(player, gameMap);
            result = inBuilt.handleCommand(command);
            return result;
        }
        else if(hasActionTrigger(command) && !isValidBasicCommand(command)){
            ActionExecutor action = new ActionExecutor(actionsMap, gameMap);
            result = action.executeAction(getTrigger(command), player, command);
        }
        else if(hasActionTrigger(command) && isValidBasicCommand(command)){
            return "Invalid command- you can only do one command at a time";
        }
        return result;
    }


    /*gets the players information from a hashmap which stores players and their information,
    * if player does not exist, player gets added to hashmap*/
    private StagPlayer getPlayer(GameMap map, String playerName){

        StagPlayer player = players.get(playerName);
        if(player == null){
            player = addPlayerToMap(map, playerName);
        }
        return player;
    }

    /* method adds the player to a hashmap if they did not already exist in the game*/

    private StagPlayer addPlayerToMap(GameMap map, String playerName){

        /*initialises the new player and gives them health */
        StagPlayer player = new StagPlayer(playerName, "player", 3);
        /*gets the starting location of the map */
        StagLocation start = startingLocation(map);
        /*sets the location of the player to the starting location */
        player.setLocation(start);
            /*we need to add the player to the location they are in as well so that other characters can
            see them*/
        start.addPlayer(player.getName(), player);
        /*places the new player into a hashmap of players */
        players.put(player.getName(), player);
        return player;
    }

    /* this method returns the starting location of the game map */

    private StagLocation startingLocation(GameMap map){
        if (map.isEmpty()) {
            return null;
        }
        return map.values().iterator().next();
    }

    /* this method tests whether the command is a valid basic command that has only one basic command
    * in the command string*/

    private boolean isValidBasicCommand(String command) {
        String[] tokens = command.split(" ");
        int numMatches = 0;

        for (String token : tokens) {
            if (token.equals("inventory") || token.equals("inv") || token.equals("get")
                    || token.equals("drop") || token.equals("goto") || token.equals("look") || token.equals("health")) {
                numMatches++;
                if (numMatches > 1) {
                    return false;
                }
            }
        }
        return numMatches == 1;
    }

    /* this method checks that a command has a trigger inside it. If there are multiple triggers
     related to other actions inside the command then, there will be errors within the ActionExcecutor class*/

    private boolean hasActionTrigger(String command) {
        for (String action : actionsMap.keySet()) {
            if (command.contains(action)) {
                return true;
            }
        }
        return false;
    }

    /* this method will get the trigger from the command If there are multiple triggers related to other actions
      inside the command then, there will be errors within the ActionExcecutor class. If there are multiple triggers
      related to the same action, there will be no errors*/

    private String getTrigger(String command){
        for (String action : actionsMap.keySet()) {
            if (command.contains(action)) {
                return action;
            }
        }
        return "";
    }

   /* method checks that the string pattern contains the correct sort of characters adn it also tests that
    the player names are not a reserved keyword*/

    private static boolean isValidPlayerName(String name) {

        String[] substrings = name.split(":");
        if(substrings.length > 1){
            return false;
        }
        String pattern = "^[A-Za-z\\s'-]+$";
        HashSet<String> disallowedWords = new HashSet<>(Arrays.asList("inventory", "inv", "get", "drop", "goto", "look", "health"));
        return name.matches(pattern) && !disallowedWords.contains(name);
    }

    private String getUsername(String command){
        String[] parts = command.split(": | ");
        String playerName = parts[0];
        return playerName;
    }

}
