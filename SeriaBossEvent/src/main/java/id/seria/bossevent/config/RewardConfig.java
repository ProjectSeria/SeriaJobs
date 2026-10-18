package id.seria.bossevent.config;

import java.util.*;

public class RewardConfig {

    private final String id;
    private final List<String> onlyMobs;
    private final double minDamagePercent;
    private final List<String> commands;
    private final int amountMin;
    private final int amountMax;
    private final Map<String, Integer> groupAmount;
    private final int bonusTopDpsMin;
    private final int bonusTopDpsMax;
    private final int bonusLastHitMin;
    private final int bonusLastHitMax;
    private final String message;
    private final String groupBonusMessage;
    private final String defaultBonusMessage;

    public RewardConfig(String id,
                        List<String> onlyMobs,
                        double minDamagePercent,
                        List<String> commands,
                        int amountMin,
                        int amountMax,
                        Map<String, Integer> groupAmount,
                        int bonusTopDpsMin,
                        int bonusTopDpsMax,
                        int bonusLastHitMin,
                        int bonusLastHitMax,
                        String message,
                        String groupBonusMessage,
                        String defaultBonusMessage) {
        this.id = id;
        this.onlyMobs = onlyMobs != null ? onlyMobs : Collections.emptyList();
        this.minDamagePercent = minDamagePercent;
        this.commands = commands != null ? commands : Collections.emptyList();
        this.amountMin = amountMin;
        this.amountMax = amountMax;
        this.groupAmount = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (groupAmount != null) {
            this.groupAmount.putAll(groupAmount);
        }
        this.bonusTopDpsMin = bonusTopDpsMin;
        this.bonusTopDpsMax = bonusTopDpsMax;
        this.bonusLastHitMin = bonusLastHitMin;
        this.bonusLastHitMax = bonusLastHitMax;
        this.message = message;
        this.groupBonusMessage = groupBonusMessage;
        this.defaultBonusMessage = defaultBonusMessage;
    }

    public String getId() {
        return id;
    }

    public List<String> getOnlyMobs() {
        return onlyMobs;
    }

    public double getMinDamagePercent() {
        return minDamagePercent;
    }

    public List<String> getCommands() {
        return commands;
    }

    public int getAmountMin() {
        return amountMin;
    }

    public int getAmountMax() {
        return amountMax;
    }

    public Map<String, Integer> getGroupAmount() {
        return groupAmount;
    }

    public int getBonusTopDpsMin() {
        return bonusTopDpsMin;
    }

    public int getBonusTopDpsMax() {
        return bonusTopDpsMax;
    }

    public int getBonusLastHitMin() {
        return bonusLastHitMin;
    }

    public int getBonusLastHitMax() {
        return bonusLastHitMax;
    }

    public String getMessage() {
        return message;
    }

    public String getGroupBonusMessage() {
        return groupBonusMessage;
    }

    public String getDefaultBonusMessage() {
        return defaultBonusMessage;
    }
}
