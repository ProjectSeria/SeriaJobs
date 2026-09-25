package id.seria.jobs.model.reward;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AuraSkillsXPReward implements Reward {

    private final String skillName;
    private final double xp;
    private final String customDisplay;

    public AuraSkillsXPReward(String skillName, double xp) {
        this(skillName, xp, null);
    }

    public AuraSkillsXPReward(String skillName, double xp, String customDisplay) {
        this.skillName = skillName;
        this.xp = xp;
        this.customDisplay = customDisplay;
    }

    public String getSkillName() {
        return skillName;
    }

    public double getXp() {
        return xp;
    }

    @Override
    public String getCustomDisplay() {
        return customDisplay;
    }

    @Override
    public void apply(@NotNull Player player, @NotNull Job job) {
        if (SeriaJobs.getInstance().getHookManager().isAuraSkillsLoaded()) {
            SeriaJobs.getInstance().getHookManager().getAuraSkillsHook().addSkillXp(player, skillName, xp);
        }
    }

    @NotNull
    @Override
    public String getFormattedDisplay() {
        if (customDisplay != null && !customDisplay.trim().isEmpty()) {
            String skillTitle = skillName != null && !skillName.isEmpty()
                    ? skillName.substring(0, 1).toUpperCase() + skillName.substring(1)
                    : "";
            return customDisplay
                    .replace("{amount}", String.valueOf((int) xp))
                    .replace("{xp}", String.valueOf((int) xp))
                    .replace("{skill}", skillTitle)
                    .replace("{skill_raw}", skillName != null ? skillName : "");
        }
        String skillTitle = skillName != null && !skillName.isEmpty()
                ? skillName.substring(0, 1).toUpperCase() + skillName.substring(1)
                : "";
        return "<aqua>+ " + (int) xp + " " + skillTitle + " Skill XP</aqua>";
    }
}
