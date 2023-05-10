package edu.uob;

import java.util.*;

/*This class handles the basic commands given by the user*/

public class BasicCommandHandler {
    private final StagPlayer player;
    private HashMap<String, StagArtefact> inventory;
    private HashMap<String, StagArtefact> artefactsInLocation;
    private final GameMap gameMap;

    public BasicCommandHandler(StagPlayer player, GameMap map){
        this.player = player; this.gameMap = map;
    }

    /* method handles any basic inbuilt commands*/
    public String handleCommand(String command) {
        String[] tokens = command.split(" ");
        String[] basicCommands = {"inventory", "inv", "get", "drop", "goto", "look", "health"};
        int verbIndex = checkForBasicCommand(basicCommands, tokens);
        String inBuiltCommand = tokens[verbIndex];
        String entity = getEntityFromCommand(command);

        if (verbIndex == -1) {
            return "Invalid command- Unknown command.";
        }

        switch (inBuiltCommand) {
            case "inventory", "inv" -> {
                if(entitiesPresent(command)){
                    return "Invalid command- Do not include other entities when looking up player's inventory";
                }
                return handleInventory();
            }
            case "get" -> {
                if (entity.isEmpty()) {
                    return "Invalid command- Please specify an artefact to get.";
                }
                if(checkForMultipleEntities(command)){
                    return "Invalid command- You cannot pick up this/these items";
                }
                return handleGet(entity);
            }
            case "drop" -> {
                if (entity.isEmpty()) {
                    return "Invalid command- Please specify an artefact to drop.";
                }
                if(checkForMultipleEntities(command)){
                    return "Invalid command- You either cannot drop two items at once OR you do not have that entity to drop";
                }
                return handleDrop(entity);
            }
            case "goto" -> {
                if (entity.isEmpty()) {
                    return "Invalid command- Please specify a location to go to.";
                }
                if(checkForMultipleEntities(command)){
                    return "Invalid command- You can only go to one location at a time";
                }
                return handleGoto(entity);
            }
            case "look" -> {
                if(entitiesPresent(command)){
                    return "Invalid command- Do not include other entities when looking around";
                }
                return handleLook();
            }
            case "health" -> {
                if(entitiesPresent(command)){
                    return "Invalid command- Do not include other entities when getting your health";
                }
                return handleHealth();
            }
            default -> {
                return "Invalid command- Unknown command.";
            }
        }
    }

    /* handles the inventory command*/
    private String handleInventory() {
        inventory = player.getInventory();
        StringBuilder inventoryString = new StringBuilder();
        for (Map.Entry<String, StagArtefact> entry : inventory.entrySet()) {
            inventoryString.append(entry.getKey());
            inventoryString.append(": ");
            inventoryString.append(entry.getValue().getDescription());
            inventoryString.append("\n");
        }
        if(inventory == null){
            return "You have no items in your inventory";
        }
        return inventoryString.toString();
    }

    /* handles the get command*/
    private String handleGet(String entity) {

        artefactsInLocation = player.getLocation().getArtefacts();
        if(artefactsInLocation.containsKey(entity)){
            StagArtefact artefact = artefactsInLocation.get(entity);
            player.addToInventory(artefact);
            player.getLocation().removeArtefact(entity);
            return "You picked up a " + entity + " and placed it into your player's inventory";
        }
        return "Invalid command- You cannot pick up this/these items";
    }

    /* handles the drop command*/
    private String handleDrop(String entity) {

        if(player.hasArtefact(entity)){
            StagArtefact artefact = player.getArtefactByName(entity);
            player.getLocation().addArtefact(entity, artefact);
            player.removeFromInventory(entity);
            return "You dropped a " + entity + " in your current location";
        }
        return "Invalid command- You either cannot drop two items at once OR you do not have that entity to drop";
    }

    /* this method handles the goto command*/
    private String handleGoto(String entity) {

        // need to first check whether the exit is in the location the player is in
        if(player.getLocation().containsExit(entity)){
            player.getLocation().removePlayer(player.getName());
            StagLocation newLocation = player.getLocation().getExit(entity);
            player.setLocation(newLocation);
            newLocation.addPlayer(player.getName(), player);
            return "You go into a " + newLocation.getName();
        }
        return "Invalid command- You cannot go to that Location OR you cannot go to multiple locations at a time";
    }

    /* this method handles the look command. It will remove the player and their description before the response
    * goes back to the user. This way users will only see other players*/
    private String handleLook() {

        String look = player.getLocation().toString();
        String playerName = player.getName();
        String description = player.getDescription();

        if (look.contains(playerName)) {
            look = look.replace(playerName, "");
            look = look.replace(description, "");
        }

        return look;
    }

    /* this method handles the health command*/
    private String handleHealth(){

        int health = player.getHealth();
        return "You have " + health + " health points left";
    }


    /* this method will get the entity from the basic command*/
    public String getEntityFromCommand(String command) {
        List<String> entities = gameMap.getEntities();
        HashMap<String, StagArtefact> inventory = player.getInventory();
        List<String> allEntities = new ArrayList<>();
        allEntities.addAll(entities);
        allEntities.addAll(inventory.keySet());

        String entity = null;
        String[] tokens = command.split("\\s+|:\\s*");
        for (String token : tokens) {
            if (allEntities.contains(token)) {
                if (entity != null) {
                    return "invalid";
                }
                entity = token;
            }
        }
        return entity != null ? entity : "invalid";
    }

    /* this method will locate the location of the basic command in the user command*/
    public int checkForBasicCommand(String[] basicCommands, String[] tokens) {
        int verbIndex = -1;
        for (int i = 0; i < tokens.length; i++) {
            if (Arrays.asList(basicCommands).contains(tokens[i])) {
                verbIndex = i;
                break;
            }
        }
        return verbIndex;
    }


    /* this method checks if there are entities present inside the command*/
    public boolean entitiesPresent(String command) {
        List<String> entities = gameMap.getEntities();
        for (String entity : entities) {
            if (command.contains(entity)) {
                return true;
            }
        }
        return false;
    }

   /* this method checks for the presence of multiple entities in the user command*/
    /* if there are, the command will be rejected and an appropriate response will be given back*/
    public boolean checkForMultipleEntities(String command) {
        Set<String> uniqueEntities = new HashSet<>(gameMap.getEntities());
        List<String> newEntities = new ArrayList<>(uniqueEntities);
        int count = 0;
        Set<String> countedEntities = new HashSet<>();
        for (String entity : newEntities) {
            if (entity.equals(command) && !countedEntities.contains(entity)) {
                count++;
                countedEntities.add(entity);
                if (count > 1) {
                    return true;
                }
            }
        }
        return false;
    }

}
