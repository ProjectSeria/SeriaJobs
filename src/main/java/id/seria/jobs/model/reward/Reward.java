package id.seria.jobs.model.reward;

import id.seria.jobs.model.Job;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Reward {

    /**
     * Executes the reward for the player.
     * Guaranteed to be executed on the Main Thread for safety.
     *
     * @param player The recipient player
     * @param job The job context
     */
    void apply(@NotNull Player player, @NotNull Job job);

    /**
     * Executes the reward for the player with a multiplier applied.
     * Guaranteed to be executed on the Main Thread for safety.
     *
     * @param player The recipient player
     * @param job The job context
     * @param multiplier The reward multiplier (e.g. 1.25)
     */
    default void apply(@NotNull Player player, @NotNull Job job, double multiplier) {
        apply(player, job);
    }

    /**
     * Returns the formatted string for displaying in GUI lores.
     *
     * @return MiniMessage or color formatted text
     */
    @NotNull
    String getFormattedDisplay();

    /**
     * Returns the formatted string for displaying in GUI lores with a multiplier applied.
     *
     * @param multiplier The reward multiplier (e.g. 1.25)
     * @return MiniMessage or color formatted text
     */
    @NotNull
    default String getFormattedDisplay(double multiplier) {
        return getFormattedDisplay();
    }

    /**
     * Optional custom display string defined in YAML.
     *
     * @return Custom display or null if using default formatting
     */
    default String getCustomDisplay() {
        return null;
    }
}
