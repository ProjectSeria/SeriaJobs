package id.seria.bossevent.reward;

import id.seria.bossevent.SeriaBossEventPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class DirectRewardDelivery implements RewardDelivery {

    private final SeriaBossEventPlugin plugin;

    public DirectRewardDelivery(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void deliverReward(@Nullable Player player, UUID uuid, List<String> commands, String rewardId) {
        if (commands == null || commands.isEmpty()) return;

        Runnable action = () -> {
            for (String cmd : commands) {
                if (cmd != null && !cmd.isBlank()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
        };

        if (Bukkit.isPrimaryThread()) {
            action.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, action);
        }
    }
}
