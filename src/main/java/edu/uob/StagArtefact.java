package edu.uob;

/* This class represents the attributes and methods of artefacts in the game*/

public class StagArtefact extends GameEntity{
    private String name;
    private String description;
    private EntityType type;
    public StagArtefact(String name, String description) {

        super(name, description);
        this.description = description;
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public EntityType getType(String entity) {
        return EntityType.ARTEFACT;
    }

    public void setType(){
        this.type = EntityType.ARTEFACT;
    }

}
