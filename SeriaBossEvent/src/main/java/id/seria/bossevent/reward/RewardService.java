package id.seria.bossevent.reward;

import id.seria.bossevent.SeriaBossEventPlugin;
import id.seria.bossevent.config.EventConfig;
import id.seria.bossevent.config.RewardConfig;
import id.seria.bossevent.leaderboard.FightResult;
import id.seria.bossevent.leaderboard.PlayerContribution;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class RewardService {

    private final SeriaBossEventPlugin plugin;
    private final DirectRewardDelivery directDelivery;
    private final StashRewardDelivery stashDelivery;

    public RewardService(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
        this.directDelivery = new DirectRewardDelivery(plugin);
        this.stashDelivery = new StashRewardDelivery(plugin);
    }

    public Map<UUID, List<String>> processRewards(EventConfig eventConfig, FightResult result) {
        Map<UUID, List<String>> rewardMessages = new HashMap<>();
        List<RewardConfig> rewardConfigs = eventConfig.getRewards();
        if (rewardConfigs.isEmpty()) return rewardMessages;

        String mobName = result.getMobName();
        UUID killerUuid = result.getKillerUuid();

        for (PlayerContribution contribution : result.getRankings()) {
            UUID uuid = contribution.getUuid();
            Player onlinePlayer = Bukkit.getPlayer(uuid);
            String playerName = contribution.getName();
            String group = plugin.getPlayerInfoService().getPrimaryGroup(uuid);

            boolean isTopDps = (contribution.getRank() == 1);
            boolean isLastHit = (killerUuid != null && killerUuid.equals(uuid));

            List<String> playerMessages = new ArrayList<>();

            for (RewardConfig reward : rewardConfigs) {
                // Check only-mobs
                if (!reward.getOnlyMobs().isEmpty()) {
                    boolean matched = false;
                    for (String m : reward.getOnlyMobs()) {
                        if (m.equalsIgnoreCase(mobName)) {
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) continue;
                }

                // Check min-damage-percent
                if (contribution.getPercent() < reward.getMinDamagePercent()) {
                    continue;
                }

                // Calculate base amount & group bonus
                int baseAmount = getRandomRange(reward.getAmountMin(), reward.getAmountMax());
                int groupBonus = 0;
                boolean hasGroupBonus = false;

                if (!reward.getGroupAmount().isEmpty()) {
                    for (Map.Entry<String, Integer> ge : reward.getGroupAmount().entrySet()) {
                        if (ge.getKey().equalsIgnoreCase(group)) {
                            int gAmount = ge.getValue();
                            groupBonus = Math.max(0, gAmount - reward.getAmountMin());
                            baseAmount = gAmount;
                            hasGroupBonus = true;
                            break;
                        }
                    }
                }

                // Calculate additional bonus for Top DPS and Last Hit
                int extraBonus = 0;
                if (isTopDps && (reward.getBonusTopDpsMin() > 0 || reward.getBonusTopDpsMax() > 0)) {
                    extraBonus += getRandomRange(reward.getBonusTopDpsMin(), reward.getBonusTopDpsMax());
                }
                if (isLastHit && (reward.getBonusLastHitMin() > 0 || reward.getBonusLastHitMax() > 0)) {
                    extraBonus += getRandomRange(reward.getBonusLastHitMin(), reward.getBonusLastHitMax());
                }

                int finalAmount = Math.max(1, baseAmount + extraBonus);

                // Build tags
                List<String> tagList = new ArrayList<>();
                if (reward.getMinDamagePercent() > 0) {
                    tagList.add((int) reward.getMinDamagePercent() + "% DMG");
                }
                if (isTopDps) {
                    tagList.add("TOP DPS");
                }
                if (isLastHit) {
                    tagList.add("LAST HIT");
                }
                String tagsFormatted = String.join(" + ", tagList);

                // Group bonus string
                String groupBonusStr;
                if (hasGroupBonus) {
                    groupBonusStr = reward.getGroupBonusMessage()
                            .replace("<bonus>", String.valueOf(groupBonus))
                            .replace("{bonus}", String.valueOf(groupBonus))
                            .replace("<group>", group)
                            .replace("{group}", group);
                } else {
                    groupBonusStr = reward.getDefaultBonusMessage();
                }

                // Format commands
                List<String> resolvedCommands = new ArrayList<>();
                for (String cmd : reward.getCommands()) {
                    String resolved = cmd
                            .replace("<player>", playerName)
                            .replace("{player}", playerName)
                            .replace("<amount>", String.valueOf(finalAmount))
                            .replace("{amount}", String.valueOf(finalAmount))
                            .replace("<group>", group)
                            .replace("{group}", group)
                            .replace("<rank>", String.valueOf(contribution.getRank()))
                            .replace("{rank}", String.valueOf(contribution.getRank()))
                            .replace("<damage>", plugin.getLeaderboardService().formatNumber(contribution.getDamage(), eventConfig.getNumberFormatLocale()))
                            .replace("{damage}", plugin.getLeaderboardService().formatNumber(contribution.getDamage(), eventConfig.getNumberFormatLocale()))
                            .replace("<percent>", String.valueOf(contribution.getPercent()))
                            .replace("{percent}", String.valueOf(contribution.getPercent()));
                    resolvedCommands.add(resolved);
                }

                // Format message
                if (reward.getMessage() != null && !reward.getMessage().isBlank()) {
                    String msg = reward.getMessage()
                            .replace("<amount>", String.valueOf(finalAmount))
                            .replace("{amount}", String.valueOf(finalAmount))
                            .replace("<player>", playerName)
                            .replace("{player}", playerName)
                            .replace("<tags>", tagsFormatted)
                            .replace("{tags}", tagsFormatted)
                            .replace("<group-bonus>", groupBonusStr)
                            .replace("{group-bonus}", groupBonusStr)
                            .replace("<group>", group)
                            .replace("{group}", group);
                    playerMessages.add(msg);
                }

                // Deliver reward
                if (onlinePlayer != null && onlinePlayer.isOnline()) {
                    directDelivery.deliverReward(onlinePlayer, uuid, resolvedCommands, reward.getId());
                } else {
                    stashDelivery.deliverReward(null, uuid, resolvedCommands, reward.getId());
                }
            }

            if (!playerMessages.isEmpty()) {
                rewardMessages.put(uuid, playerMessages);
            }
        }

        return rewardMessages;
    }

    private int getRandomRange(int min, int max) {
        if (min >= max) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public DirectRewardDelivery getDirectDelivery() {
        return directDelivery;
    }

    public StashRewardDelivery getStashDelivery() {
        return stashDelivery;
    }
}
