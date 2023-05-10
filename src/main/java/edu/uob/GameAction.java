package edu.uob;

import java.util.List;

public class GameAction {
    final private List<String> triggers;
    final private List<String> subjects;
    final private List<String> consumedEntities;
    final private List<String> producedEntities;
    final private String narration;

    public GameAction(List<String> triggers, List<String> subjects,
                      List<String> consumedEntities, List<String> producedEntities,
                      String narration) {
        this.triggers = triggers;
        this.subjects = subjects;
        this.consumedEntities = consumedEntities;
        this.producedEntities = producedEntities;
        this.narration = narration;
    }

    public List<String> getSubjects() {
        return this.subjects;
    }

    public List<String> getConsumedEntities() {
        return this.consumedEntities;
    }

    public String getNarration(){
        return this.narration;
    }

    public List<String> getProducedEntities() {
        return this.producedEntities;
    }
}
