package edu.uob;

public abstract class GameEntity
{
    protected String name;
    protected String description;
    protected EntityType type;

    public GameEntity(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public EntityType getType(String entity) {
        return type;
    }


}
