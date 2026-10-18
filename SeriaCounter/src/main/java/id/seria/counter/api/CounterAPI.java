package id.seria.counter.api;

import id.seria.counter.SeriaCounterPlugin;
import id.seria.counter.model.Counter;
import id.seria.counter.model.CounterStep;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public final class CounterAPI {

    private CounterAPI() {}

    /**
     * Adds an amount to the specified counter.
     */
    public static boolean add(String id, long amount) {
        Counter counter = getCounter(id).orElse(null);
        if (counter == null) return false;
        return counter.add(amount, null);
    }

    /**
     * Directly sets the value of the specified counter.
     */
    public static boolean set(String id, long value) {
        Counter counter = getCounter(id).orElse(null);
        if (counter == null) return false;
        return counter.set(value, null);
    }

    /**
     * Gets the current value of the counter. Returns 0 if counter is not found.
     */
    public static long get(String id) {
        return getCounter(id).map(Counter::getCurrentValue).orElse(0L);
    }

    /**
     * Gets the active 1-based step number of the counter. Returns 1 if not found.
     */
    public static int getStep(String id) {
        return getCounter(id).map(c -> {
            CounterStep step = c.getCurrentStep();
            return step != null ? step.getStepNumber() : 1;
        }).orElse(1);
    }

    /**
     * Gets the active step's display name.
     */
    public static String getStepName(String id) {
        return getCounter(id).map(c -> {
            CounterStep step = c.getCurrentStep();
            return step != null ? step.getName() : "";
        }).orElse("");
    }

    /**
     * Gets the active step's goal. Returns 0 if counter not found.
     */
    public static long getGoal(String id) {
        return getCounter(id).map(Counter::getGoal).orElse(0L);
    }

    /**
     * Gets progress percentage (0.0 to 100.0).
     */
    public static double getProgress(String id) {
        return getCounter(id).map(Counter::getProgress).orElse(0.0);
    }

    /**
     * Gets remaining amount to reach current step's goal.
     */
    public static long getRemaining(String id) {
        return getCounter(id).map(Counter::getRemaining).orElse(0L);
    }

    /**
     * Resets the counter to 0, step 1, and clears once-actions.
     */
    public static boolean reset(String id) {
        Counter counter = getCounter(id).orElse(null);
        if (counter == null) return false;
        counter.reset();
        return true;
    }

    /**
     * Gets an Optional containing the Counter instance.
     */
    public static Optional<Counter> getCounter(String id) {
        if (id == null || SeriaCounterPlugin.getInstance() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(SeriaCounterPlugin.getInstance().getCounterLoader().getCounter(id));
    }

    /**
     * Returns all registered counter IDs.
     */
    public static Collection<String> getRegisteredIds() {
        if (SeriaCounterPlugin.getInstance() == null) return Collections.emptyList();
        return SeriaCounterPlugin.getInstance().getCounterLoader().getCounters().keySet();
    }
}
