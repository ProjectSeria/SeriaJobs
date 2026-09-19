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
     * Returns the formatted string for displaying in GUI lores.
     *
     * @return MiniMessage or color formatted text
     */
    @NotNull
    String getFormattedDisplay();
}
