package id.seria.bossevent.reward;

import id.seria.bossevent.SeriaBossEventPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class StashRewardDelivery implements RewardDelivery {

    private final SeriaBossEventPlugin plugin;

    public StashRewardDelivery(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void deliverReward(@Nullable Player player, UUID uuid, List<String> commands, String rewardId) {
        if (uuid == null || commands == null || commands.isEmpty()) return;

        try {
            // SBE offline command delivery:
            // Stored in ResultStore pending rewards and safely executed when player logs back in
            plugin.getResultStore().addPendingReward(uuid, commands);
            plugin.getLogger().info("[SeriaBossEvent] Queued " + commands.size() + " reward commands for offline player " + uuid + " (" + rewardId + ").");
        } catch (Exception e) {
            plugin.getLogger().severe("[SeriaBossEvent] Failed to queue reward for " + uuid + ": " + e.getMessage());
            // Fallback ensure reward is stored
            plugin.getResultStore().addPendingReward(uuid, commands);
        }
    }
}
