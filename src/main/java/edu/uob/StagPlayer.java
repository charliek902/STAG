package edu.uob;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/* This class represents the attributes and methods of a player in the game*/
public class StagPlayer extends GameEntity {
    private int health;
    private HashMap<String, StagArtefact> inventory;
    private StagLocation currentLocation;

    public StagPlayer(String name, String description, int health) {
        super(name, description);
        this.health = health;
        this.inventory = new HashMap<>();
        this.name = name;
        this.description = description;
    }

    public StagLocation getLocation() {
        return currentLocation;
    }

    public void setLocation(StagLocation location) {
        this.currentLocation = location;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public HashMap<String, StagArtefact> getInventory() {
        return inventory;
    }

    public void addToInventory(StagArtefact artefact) {
        inventory.put(artefact.getName(), artefact);
    }

    public void removeFromInventory(String entity) {

        Iterator<Map.Entry<String, StagArtefact>> iterator = inventory.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, StagArtefact> entry = iterator.next();
            if (entry.getKey().equals(entity) || entry.getValue().getName().equals(entity)) {
                iterator.remove();
                return;
            }
        }
    }

    public boolean hasArtefact(String artefactName) {
        return inventory.containsKey(artefactName);
    }

    public StagArtefact getArtefactByName(String name) {
        return inventory.get(name);
    }

    public void dropInventoryInLocation() {
        currentLocation.addArtefacts(inventory);
        inventory.clear();
    }
}

