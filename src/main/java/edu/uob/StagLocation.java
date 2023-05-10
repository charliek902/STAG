package edu.uob;
import java.util.HashMap;

/* This class represents the attributes and methods of locations in the game*/
public class StagLocation extends GameEntity {
    private HashMap<String, StagPlayer> players;
    private HashMap<String, StagFurniture> furniture;
    private HashMap<String, StagCharacter> characters;
    private HashMap<String, StagArtefact> artefacts;
    /* the hash map below adds new Entities which get produced */
    private HashMap<String, StagLocation> exits;
    private EntityType type;

    public StagLocation(String name, String description) {
        super(name, description);
        this.players = new HashMap<>();
        this.furniture = new HashMap<>();
        this.characters = new HashMap<>();
        this.artefacts = new HashMap<>();
        this.exits = new HashMap<>();
        this.type = EntityType.LOCATION;
        this.name = name;
        this.description = description;
    }

    public void addPlayer(String name, StagPlayer player) {
        this.players.put(name, player);
    }

    public void removePlayer(String name) {
        if (players.containsKey(name)) {
            players.remove(name);
        }
    }

    public void addFurniture(String furnitureName, StagFurniture furniture) {
        this.furniture.put(furnitureName, furniture);
    }

    public void addCharacter(String characterName, StagCharacter character) {
        this.characters.put(characterName, character);
    }

    public void addArtefact(String artefactName, StagArtefact artefact) {
        this.artefacts.put(artefactName, artefact);
    }

    public void addExit(String exit, StagLocation exitToLocation) {
        this.exits.put(exit, exitToLocation);
    }


    public boolean containsEntity(String name) {
        if (furniture.containsKey(name) || characters.containsKey(name) || artefacts.containsKey(name)
        || exits.containsKey(name)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean containsEntityExcludingExits(String name) {
        if (furniture.containsKey(name) || characters.containsKey(name) || artefacts.containsKey(name)) {
            return true;
        } else {
            return false;
        }
    }

    public void removeEntity(String entity) {
        if (furniture.containsKey(entity)) {
            furniture.remove(entity);
        } else if (characters.containsKey(entity)) {
            characters.remove(entity);
        } else if (artefacts.containsKey(entity)) {
            artefacts.remove(entity);
        }
        else if(exits.containsKey(entity)){
            exits.remove(entity);
        }
    }

    @Override
    public EntityType getType(String entity)
    {
        if (furniture.containsKey(entity)) {
            return EntityType.FURNITURE;
        } else if (characters.containsKey(entity)) {
            return EntityType.CHARACTER;
        } else if (artefacts.containsKey(entity)) {
            return EntityType.ARTEFACT;
        }
        else if(exits.containsKey(entity)){
            return EntityType.LOCATION;
        }
        return null;
    }

    public void setEntityType() {
        this.type = EntityType.LOCATION;
    }

    public HashMap<String, StagArtefact> getArtefacts() {
        return artefacts;
    }

    public HashMap<String, StagFurniture> getFurniture() {
        return furniture;
    }

    public HashMap<String, StagLocation> getExits() {
        return exits;
    }

    public HashMap<String, StagCharacter> getCharacters() {
        return characters;
    }

    public boolean containsExit(String exit) {
        return exits.containsKey(exit);
    }

    public StagLocation getExit(String exit) {
        return exits.get(exit);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Location Name: ").append(this.name).append("\n");
        sb.append("Location Description: ").append(this.description).append("\n");
        sb.append("Characters:\n");
        for (StagCharacter character : this.characters.values()) {
            sb.append(character.getName()).append(": ").append(character.getDescription()).append("\n");
        }
        sb.append("Artefacts:\n");
        for (StagArtefact artefact : this.artefacts.values()) {
            sb.append(artefact.getName()).append(": ").append(artefact.getDescription()).append("\n");
        }
        sb.append("Furniture:\n");
        for (StagFurniture furniture : this.furniture.values()) {
            sb.append(furniture.getName()).append(": ").append(furniture.getDescription()).append("\n");
        }
        sb.append("Players:\n");
        for (StagPlayer player : this.players.values()) {
            if (!player.getLocation().equals(player.getName())) {
                sb.append(player.getName()).append(": ").append(player.getDescription()).append("\n");
            }
        }
        sb.append("Exits:\n");
        sb.append(this.exits.keySet()).append("\n");

        return sb.toString();
    }

    public void removeArtefact(String entity) {
        artefacts.remove(entity);
    }

    public void addArtefacts(HashMap<String, StagArtefact> newArtefacts) {
        artefacts.putAll(newArtefacts);
    }

    public HashMap<String, StagPlayer> getPlayers(){
        return this.players;
    }



}










