package id.seria.counter.placeholder;

import id.seria.counter.SeriaCounterPlugin;
import id.seria.counter.model.Counter;
import id.seria.counter.model.CounterStep;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CounterPAPIExpansion extends PlaceholderExpansion {

    private final SeriaCounterPlugin plugin;

    public CounterPAPIExpansion(SeriaCounterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "seriacounter";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Seria";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        // Expected format: %seriacounter_<id>_<property>%
        int lastUnder = params.lastIndexOf('_');
        if (lastUnder == -1) {
            return null;
        }

        String id = params.substring(0, lastUnder).toLowerCase().trim();
        String prop = params.substring(lastUnder + 1).toLowerCase().trim();

        Counter counter = plugin.getCounterLoader().getCounter(id);
        if (counter == null) {
            return "0";
        }

        CounterStep step = counter.getCurrentStep();

        switch (prop) {
            case "value":
                return String.valueOf(counter.getCurrentValue());
            case "goal":
                return String.valueOf(counter.getGoal());
            case "step":
                return String.valueOf(step != null ? step.getStepNumber() : 1);
            case "step_name":
            case "stepname":
                return step != null ? step.getName() : "";
            case "total_steps":
            case "max_steps":
            case "steps":
                return String.valueOf(counter.getTotalSteps());
            case "remaining":
                return String.valueOf(counter.getRemaining());
            case "progress":
                return String.format("%.1f", counter.getProgress());
            case "progress_rounded":
                return String.valueOf(Math.round(counter.getProgress()));
            case "name":
                return counter.getDisplayName();
            default:
                return null;
        }
    }
}
