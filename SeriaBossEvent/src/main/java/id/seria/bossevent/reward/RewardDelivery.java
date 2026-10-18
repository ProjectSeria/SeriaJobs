package id.seria.bossevent.reward;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface RewardDelivery {

    /**
     * Delivers resolved reward commands to a player.
     *
     * @param player online player or null if offline
     * @param uuid player UUID
     * @param commands resolved commands to execute
     * @param rewardId identifier of the reward
     */
    void deliverReward(@Nullable Player player, UUID uuid, List<String> commands, String rewardId);
}
