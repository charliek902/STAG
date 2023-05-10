package edu.uob;

/* This class represents the attributes and methods of furniture in the game*/
public class StagFurniture extends GameEntity{
    private String description;
    private EntityType type;

    public StagFurniture(String name, String description) {

        super(name, description);
        this.name = name;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public EntityType getType(String entity) {
        return EntityType.FURNITURE;
    }

    public void setType(){
        this.type = EntityType.FURNITURE;
    }
}
