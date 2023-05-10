package edu.uob;
import java.util.*;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;

/* The game map class allows me to place the entities into their correct class types and put those class types in
 their correct locations. This class also allows me to get the location of the storeroom, get the start location
 and, it also allows me to update the game state of the map */

public class GameMap {

    /* this variable allows use to keep track of the game state via a GameMap*/
    private Map<String, StagLocation> map;
    /* this variable represents the different entities in the game and is created when the map gets initialized*/
    private Set<String> entitySet;

    public GameMap(List<Graph> locations, List<Edge> paths) throws ParseException {
        map = new LinkedHashMap<>();
        entitySet = new HashSet<>();
        for (Graph locationGraph : locations) {

            Node locationDetails = locationGraph.getNodes(false).get(0);
            String locationName = locationDetails.getId().getId().toLowerCase();
            String locationDescription = locationDetails.getAttribute("description");
            StagLocation location = new StagLocation(locationName, locationDescription);
            location.setEntityType();
            entitySet.add(locationName);

            List<Graph> subgraphs = new ArrayList<>();
            for (Graph subgraph : locationGraph.getSubgraphs()) {
                subgraphs.add(subgraph);
            }

            for (Graph subgraph : locationGraph.getSubgraphs()) {
                if (subgraph.getId().getId().equals("characters")) {
                    for (Node node : subgraph.getNodes(false)) {
                        String id = node.getId().getId().toLowerCase();
                        String description = node.getAttribute("description");
                        StagCharacter character = new StagCharacter(id, description);
                        character.setType();
                        location.addCharacter(id, character);
                        entitySet.add(id);
                    }
                }
                else if (subgraph.getId().getId().equals("artefacts")) {
                    for (Node node : subgraph.getNodes(false)) {
                        String id = node.getId().getId().toLowerCase();
                        String description = node.getAttribute("description");
                        StagArtefact artefact = new StagArtefact(id, description);
                        artefact.setType();
                        location.addArtefact(id, artefact);
                        entitySet.add(id);
                    }
                } else if (subgraph.getId().getId().equals("furniture")) {
                    for (Node node : subgraph.getNodes(false)) {
                        String id = node.getId().getId().toLowerCase();
                        String description = node.getAttribute("description");
                        StagFurniture furniture = new StagFurniture(id, description);
                        furniture.setType();
                        location.addFurniture(id, furniture);
                        entitySet.add(id);
                    }
                }
            }
            map.put(locationName, location);
        }

        for (Edge path : paths) {
            String sourceLocationName = path.getSource().getNode().getId().getId().toLowerCase();
            String destinationLocationName = path.getTarget().getNode().getId().getId().toLowerCase();
            StagLocation sourceLocation = map.get(sourceLocationName);
            StagLocation destinationLocation = map.get(destinationLocationName);
            sourceLocation.addExit(destinationLocationName, destinationLocation);
            entitySet.add(sourceLocationName);
            entitySet.add(destinationLocationName);
        }
    }

    public StagLocation getStoreroom() {
        return map.get("storeroom");
    }

    public Collection<StagLocation> values() {
        return map.values();
    }

    public StagArtefact getStagArtefact(String name) {

        for (StagLocation location : map.values()) {
            for (StagArtefact artefact : location.getArtefacts().values()) {
                if (artefact.getName().equals(name)) {
                    return artefact;
                }
            }
        }
        return null;
    }

    public StagCharacter getStagCharacter(String name) {
        for (StagLocation location : map.values()) {
            for (StagCharacter character : location.getCharacters().values()) {
                if (character.getName().equals(name)) {
                    return character;
                }
            }
        }
        return null;
    }

    public StagFurniture getStagFurniture(String name) {
        for (StagLocation location : map.values()) {
            for (StagFurniture furniture : location.getFurniture().values()) {
                if (furniture.getName().equals(name)) {
                    return furniture;
                }
            }
        }
        return null;
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public StagLocation getLocation(String locationName) {
        return map.get(locationName);
    }


    public StagLocation getFirstLocation() {
        if (map.isEmpty()) {
            return null;
        }
        return map.values().iterator().next();
    }

    public List<String> getEntities() {
        List<String> entities = new ArrayList<>();
        for (String entity : entitySet) {
            entities.add(entity);
        }
        return entities;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
