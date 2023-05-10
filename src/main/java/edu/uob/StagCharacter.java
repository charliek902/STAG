package edu.uob;

/* This class represents the attributes and methods of characters in the game*/
public class StagCharacter extends GameEntity{
    private String name;
    private String description;
    private EntityType type;

    public StagCharacter(String name, String description) {
        super(name, description);
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public EntityType getType(String entity) {
        return EntityType.CHARACTER;
    }

    public void setType(){
        this.type = EntityType.CHARACTER;
    }



}
