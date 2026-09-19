package id.seria.jobs.hook;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AuraSkillsHook {

    private AuraSkillsApi api;

    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("AuraSkills") == null) {
            return false;
        }
        try {
            api = AuraSkillsApi.get();
            return api != null;
        } catch (Throwable e) {
            return false;
        }
    }

    public void addSkillXp(Player player, String skillName, double amount) {
        if (api == null || player == null || amount <= 0) return;
        try {
            SkillsUser user = api.getUser(player.getUniqueId());
            if (user == null) return;

            NamespacedId id = NamespacedId.of("auraskills", skillName.toLowerCase());
            Skill skill = api.getGlobalRegistry().getSkill(id);
            if (skill != null) {
                user.addSkillXp(skill, amount);
            }
        } catch (Throwable t) {
            Bukkit.getLogger().warning("[SeriaJobs] Failed to add AuraSkills XP for " + player.getName() + ": " + t.getMessage());
        }
    }
}
