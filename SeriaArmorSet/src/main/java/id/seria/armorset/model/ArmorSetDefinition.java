package id.seria.armorset.model;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ArmorSetDefinition {

    private final String id;
    private final int piecesRequired;
    private final Map<String, ConfigurationSection> skills = new HashMap<>();

    public ArmorSetDefinition(@NotNull String id, int piecesRequired) {
        this.id = id.trim().toUpperCase(Locale.ROOT);
        this.piecesRequired = Math.max(1, piecesRequired);
    }

    public void addSkill(@NotNull String skillId, @Nullable ConfigurationSection config) {
        skills.put(skillId.trim().toLowerCase(Locale.ROOT), config);
    }

    public @NotNull String getId() {
        return id;
    }

    public int getPiecesRequired() {
        return piecesRequired;
    }

    public boolean hasSkill(@NotNull String skillId) {
        return skills.containsKey(skillId.trim().toLowerCase(Locale.ROOT));
    }

    public @Nullable ConfigurationSection getSkillConfig(@NotNull String skillId) {
        return skills.get(skillId.trim().toLowerCase(Locale.ROOT));
    }

    public @NotNull Set<String> getSkillIds() {
        return Collections.unmodifiableSet(skills.keySet());
    }

    public @NotNull Map<String, ConfigurationSection> getSkills() {
        return Collections.unmodifiableMap(skills);
    }
}