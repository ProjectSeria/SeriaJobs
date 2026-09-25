package id.seria.jobs.model.reward;

import id.seria.jobs.model.Job;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandReward implements Reward {

    private final String executor; // "CONSOLE" or "PLAYER"
    private final String command;
    private final String customDisplay;

    public CommandReward(String executor, String command) {
        this(executor, command, null);
    }

    public CommandReward(String executor, String command, String customDisplay) {
        this.executor = executor != null ? executor.toUpperCase() : "CONSOLE";
        this.command = command;
        this.customDisplay = customDisplay;
    }

    public String getExecutor() {
        return executor;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String getCustomDisplay() {
        return customDisplay;
    }

    @Override
    public void apply(@NotNull Player player, @NotNull Job job) {
        if (command == null || command.isEmpty()) return;

        String cmd = command.replace("%player%", player.getName())
                .replace("%job%", job.getId());

        if ("PLAYER".equalsIgnoreCase(executor)) {
            player.performCommand(cmd);
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }

    @NotNull
    @Override
    public String getFormattedDisplay() {
        if (customDisplay != null && !customDisplay.trim().isEmpty()) {
            return customDisplay;
        }
        return "<gray>+ Bonus Spesial Server</gray>";
    }
}
