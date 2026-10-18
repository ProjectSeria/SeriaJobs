package id.seria.counter.model;

import java.util.Collections;
import java.util.List;

public class CounterStep {

    private final int index;
    private final String name;
    private final String description;
    private final long goal;
    private final List<String> actions;
    private final List<String> onceActions;

    public CounterStep(int index, String name, String description, long goal, List<String> actions, List<String> onceActions) {
        this.index = index;
        this.name = name != null ? name : "Step " + (index + 1);
        this.description = description != null ? description : "";
        this.goal = goal;
        this.actions = actions != null ? List.copyOf(actions) : Collections.emptyList();
        this.onceActions = onceActions != null ? List.copyOf(onceActions) : Collections.emptyList();
    }

    public int getIndex() {
        return index;
    }

    public int getStepNumber() {
        return index + 1;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getGoal() {
        return goal;
    }

    public List<String> getActions() {
        return actions;
    }

    public List<String> getOnceActions() {
        return onceActions;
    }
}
