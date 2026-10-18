package id.seria.counter.model;

import id.seria.counter.SeriaCounterPlugin;
import id.seria.counter.api.events.CounterChangeEvent;
import id.seria.counter.api.events.CounterCompleteEvent;
import id.seria.counter.api.events.CounterStepCompleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Counter {

    private final String id;
    private final String displayName;
    private final List<CounterStep> steps;

    private final AtomicLong currentValue = new AtomicLong(0);
    private final AtomicInteger currentStepIndex = new AtomicInteger(0);
    private final Set<Integer> executedOnceSteps = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean reentrancyLock = new AtomicBoolean(false);

    public Counter(String id, String displayName, List<CounterStep> steps) {
        this.id = id.toLowerCase().trim();
        this.displayName = displayName != null ? displayName : id;
        this.steps = steps != null && !steps.isEmpty() ? List.copyOf(steps) : Collections.emptyList();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<CounterStep> getSteps() {
        return steps;
    }

    public int getTotalSteps() {
        return steps.size();
    }

    public long getCurrentValue() {
        return currentValue.get();
    }

    public int getCurrentStepIndex() {
        return currentStepIndex.get();
    }

    public CounterStep getCurrentStep() {
        int idx = currentStepIndex.get();
        if (idx >= 0 && idx < steps.size()) {
            return steps.get(idx);
        }
        return steps.isEmpty() ? null : steps.get(steps.size() - 1);
    }

    public Set<Integer> getExecutedOnceSteps() {
        return Collections.unmodifiableSet(executedOnceSteps);
    }

    /**
     * Initializes runtime state from persistent storage.
     */
    public void loadState(long value, int stepIndex, Set<Integer> onceExecuted) {
        this.currentValue.set(value);
        int clampedStep = Math.max(0, Math.min(stepIndex, steps.size() - 1));
        this.currentStepIndex.set(clampedStep);
        this.executedOnceSteps.clear();
        if (onceExecuted != null) {
            this.executedOnceSteps.addAll(onceExecuted);
        }
    }

    /**
     * Adds an amount to the counter, evaluating goals sequentially in Option A (cumulative) mode.
     */
    public synchronized boolean add(long amount, CommandSender source) {
        if (steps.isEmpty()) return false;

        // Prevent recursive loop if an action invokes /counter add on this counter
        if (reentrancyLock.get()) {
            Bukkit.getLogger().warning("[SeriaCounter] Prevented recursive action trigger on counter '" + id + "'");
            return false;
        }

        long oldVal = currentValue.get();
        long targetVal = oldVal + amount;

        // Bukkit Event: CounterChangeEvent
        CounterChangeEvent changeEvent = new CounterChangeEvent(this, oldVal, targetVal, source);
        Bukkit.getPluginManager().callEvent(changeEvent);
        if (changeEvent.isCancelled()) {
            return false;
        }

        long finalVal = changeEvent.getNewValue();
        currentValue.set(finalVal);

        evaluateProgression(source);
        return true;
    }

    /**
     * Directly sets the counter's current value.
     */
    public synchronized boolean set(long value, CommandSender source) {
        if (steps.isEmpty()) return false;

        if (reentrancyLock.get()) {
            Bukkit.getLogger().warning("[SeriaCounter] Prevented recursive action trigger on counter '" + id + "'");
            return false;
        }

        long oldVal = currentValue.get();
        CounterChangeEvent changeEvent = new CounterChangeEvent(this, oldVal, value, source);
        Bukkit.getPluginManager().callEvent(changeEvent);
        if (changeEvent.isCancelled()) {
            return false;
        }

        long finalVal = changeEvent.getNewValue();
        currentValue.set(finalVal);

        evaluateProgression(source);
        return true;
    }

    /**
     * Evaluates steps against current value. If step goals are reached, executes actions.
     * When the highest step is completed, auto-resets value and step to 0.
     */
    private void evaluateProgression(CommandSender source) {
        if (steps.isEmpty()) return;

        reentrancyLock.set(true);
        try {
            boolean stepAdvanced = true;
            while (stepAdvanced) {
                stepAdvanced = false;
                int currentIdx = currentStepIndex.get();
                if (currentIdx < 0 || currentIdx >= steps.size()) break;

                CounterStep activeStep = steps.get(currentIdx);
                long currentVal = currentValue.get();

                if (currentVal >= activeStep.getGoal()) {
                    // 1. Execute repeatable actions for this step
                    SeriaCounterPlugin.getInstance().getActionService()
                            .executeActions(this, activeStep, activeStep.getActions(), source);

                    // 2. Execute once-actions if never executed
                    if (!executedOnceSteps.contains(activeStep.getIndex())) {
                        executedOnceSteps.add(activeStep.getIndex());
                        SeriaCounterPlugin.getInstance().getActionService()
                                .executeActions(this, activeStep, activeStep.getOnceActions(), source);
                    }

                    // 3. Fire step complete event
                    CounterStepCompleteEvent stepEvent = new CounterStepCompleteEvent(this, activeStep, currentVal);
                    Bukkit.getPluginManager().callEvent(stepEvent);

                    // 4. Check if this is the final (highest) step
                    if (currentIdx >= steps.size() - 1) {
                        // Fire complete event
                        CounterCompleteEvent completeEvent = new CounterCompleteEvent(this, steps.size(), currentVal);
                        Bukkit.getPluginManager().callEvent(completeEvent);

                        // Auto-reset to 0 and first step
                        currentValue.set(0);
                        currentStepIndex.set(0);

                        if (SeriaCounterPlugin.getInstance().getConfigManager().isDebug()) {
                            Bukkit.getLogger().info("[SeriaCounter] Counter '" + id + "' completed final step and auto-reset to 0.");
                        }
                        break;
                    } else {
                        // Advance to next step (Option A: cumulative, counter value keeps going)
                        currentStepIndex.incrementAndGet();
                        stepAdvanced = true; // Loop again to check if the new step's goal is also already met
                    }
                }
            }
        } finally {
            reentrancyLock.set(false);
        }
    }

    /**
     * Resets the counter completely back to 0, step 1, and clears executed once-actions.
     */
    public synchronized void reset() {
        currentValue.set(0);
        currentStepIndex.set(0);
        executedOnceSteps.clear();
    }

    public long getGoal() {
        CounterStep step = getCurrentStep();
        return step != null ? step.getGoal() : 0;
    }

    public long getRemaining() {
        long goal = getGoal();
        long val = currentValue.get();
        return Math.max(0, goal - val);
    }

    public double getProgress() {
        long goal = getGoal();
        if (goal <= 0) return 100.0;
        double pct = ((double) currentValue.get() / goal) * 100.0;
        return Math.max(0.0, Math.min(100.0, pct));
    }
}
