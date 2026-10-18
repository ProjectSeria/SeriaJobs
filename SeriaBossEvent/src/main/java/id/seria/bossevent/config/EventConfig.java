package id.seria.bossevent.config;

import java.util.*;

public class EventConfig {

    private final String name;

    // Game
    private final String displayName;
    private final long duration; // seconds (-1 = unlimited)
    private final boolean overrideMessages;
    private final boolean shutdownEventAfterDuration;
    private final boolean globalScore;

    // Countdown
    private final boolean countdownEnabled;
    private final int countdownDuration; // seconds
    private final List<Integer> notifyAt;
    private final int countdownDelay; // seconds

    // Task
    private final boolean taskEnabled;
    private final long taskPeriod; // ms
    private final long taskDelay; // ms

    // Bosses
    private final List<BossConfig> bosses;

    // Leaderboard
    private final boolean leaderboardEnabled;
    private final String trackingMode;
    private final long pollInterval; // ticks
    private final boolean attributeUnownedDamage;
    private final int displayTop;
    private final boolean displayCentered;
    private final int minPlayersForPercent;
    private final boolean showLastHit;
    private final boolean showPersonal;
    private final boolean showFightDuration;
    private final boolean showOnFail;
    private final String displayScope; // "server", "world", "participants"

    // Discord
    private final boolean discordEnabled;
    private final String discordChannel;
    private final int discordMaxEntries;
    private final String discordColor;

    // Number format
    private final String numberFormatLocale;

    // Rewards
    private final List<RewardConfig> rewards;

    // Custom messages map (when overrideMessages is true)
    private final Map<String, Object> customMessages;

    public EventConfig(String name,
                       String displayName,
                       long duration,
                       boolean overrideMessages,
                       boolean shutdownEventAfterDuration,
                       boolean globalScore,
                       boolean countdownEnabled,
                       int countdownDuration,
                       List<Integer> notifyAt,
                       int countdownDelay,
                       boolean taskEnabled,
                       long taskPeriod,
                       long taskDelay,
                       List<BossConfig> bosses,
                       boolean leaderboardEnabled,
                       String trackingMode,
                       long pollInterval,
                       boolean attributeUnownedDamage,
                       int displayTop,
                       boolean displayCentered,
                       int minPlayersForPercent,
                       boolean showLastHit,
                       boolean showPersonal,
                       boolean showFightDuration,
                       boolean showOnFail,
                       String displayScope,
                       boolean discordEnabled,
                       String discordChannel,
                       int discordMaxEntries,
                       String discordColor,
                       String numberFormatLocale,
                       List<RewardConfig> rewards,
                       Map<String, Object> customMessages) {
        this.name = name;
        this.displayName = displayName;
        this.duration = duration;
        this.overrideMessages = overrideMessages;
        this.shutdownEventAfterDuration = shutdownEventAfterDuration;
        this.globalScore = globalScore;
        this.countdownEnabled = countdownEnabled;
        this.countdownDuration = countdownDuration;
        this.notifyAt = notifyAt != null ? Collections.unmodifiableList(notifyAt) : Collections.emptyList();
        this.countdownDelay = countdownDelay;
        this.taskEnabled = taskEnabled;
        this.taskPeriod = taskPeriod;
        this.taskDelay = taskDelay;
        this.bosses = bosses != null ? Collections.unmodifiableList(bosses) : Collections.emptyList();
        this.leaderboardEnabled = leaderboardEnabled;
        this.trackingMode = trackingMode;
        this.pollInterval = pollInterval;
        this.attributeUnownedDamage = attributeUnownedDamage;
        this.displayTop = displayTop;
        this.displayCentered = displayCentered;
        this.minPlayersForPercent = minPlayersForPercent;
        this.showLastHit = showLastHit;
        this.showPersonal = showPersonal;
        this.showFightDuration = showFightDuration;
        this.showOnFail = showOnFail;
        this.displayScope = displayScope != null ? displayScope.toLowerCase() : "server";
        this.discordEnabled = discordEnabled;
        this.discordChannel = discordChannel;
        this.discordMaxEntries = Math.min(25, Math.max(1, discordMaxEntries));
        this.discordColor = discordColor != null ? discordColor : "#FFAA00";
        this.numberFormatLocale = numberFormatLocale != null ? numberFormatLocale : "id-ID";
        this.rewards = rewards != null ? Collections.unmodifiableList(rewards) : Collections.emptyList();
        this.customMessages = customMessages != null ? Collections.unmodifiableMap(customMessages) : Collections.emptyMap();
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isOverrideMessages() {
        return overrideMessages;
    }

    public boolean isShutdownEventAfterDuration() {
        return shutdownEventAfterDuration;
    }

    public boolean isGlobalScore() {
        return globalScore;
    }

    public boolean isCountdownEnabled() {
        return countdownEnabled;
    }

    public int getCountdownDuration() {
        return countdownDuration;
    }

    public List<Integer> getNotifyAt() {
        return notifyAt;
    }

    public int getCountdownDelay() {
        return countdownDelay;
    }

    public boolean isTaskEnabled() {
        return taskEnabled;
    }

    public long getTaskPeriod() {
        return taskPeriod;
    }

    public long getTaskDelay() {
        return taskDelay;
    }

    public List<BossConfig> getBosses() {
        return bosses;
    }

    public boolean isLeaderboardEnabled() {
        return leaderboardEnabled;
    }

    public String getTrackingMode() {
        return trackingMode;
    }

    public long getPollInterval() {
        return pollInterval;
    }

    public boolean isAttributeUnownedDamage() {
        return attributeUnownedDamage;
    }

    public int getDisplayTop() {
        return displayTop;
    }

    public boolean isDisplayCentered() {
        return displayCentered;
    }

    public int getMinPlayersForPercent() {
        return minPlayersForPercent;
    }

    public boolean isShowLastHit() {
        return showLastHit;
    }

    public boolean isShowPersonal() {
        return showPersonal;
    }

    public boolean isShowFightDuration() {
        return showFightDuration;
    }

    public boolean isShowOnFail() {
        return showOnFail;
    }

    public String getDisplayScope() {
        return displayScope;
    }

    public boolean isDiscordEnabled() {
        return discordEnabled;
    }

    public String getDiscordChannel() {
        return discordChannel;
    }

    public int getDiscordMaxEntries() {
        return discordMaxEntries;
    }

    public String getDiscordColor() {
        return discordColor;
    }

    public String getNumberFormatLocale() {
        return numberFormatLocale;
    }

    public List<RewardConfig> getRewards() {
        return rewards;
    }

    public Map<String, Object> getCustomMessages() {
        return customMessages;
    }
}
