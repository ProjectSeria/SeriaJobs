package id.seria.jobs.config;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LanguageManager {

    private final SeriaJobs plugin;
    private FileConfiguration langConfig;
    private FileConfiguration fallbackConfig;
    private FileConfiguration idConfig;
    private String prefix = "";

    public LanguageManager(SeriaJobs plugin) {
        this.plugin = plugin;
    }

    public void load() {
        String langFileName = plugin.getConfigManager().getLanguageFileName();

        // Ensure both base English and Indonesian files exist on disk
        saveResourceIfNotExists("messages_en.yml");
        saveResourceIfNotExists("messages_id.yml");

        File langFile = new File(plugin.getDataFolder(), langFileName);
        if (!langFile.exists()) {
            langFile = new File(plugin.getDataFolder(), "messages_en.yml");
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Fallback default English from jar
        InputStream enStream = plugin.getResource("messages_en.yml");
        if (enStream != null) {
            fallbackConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(enStream, StandardCharsets.UTF_8));
            langConfig.setDefaults(fallbackConfig);
        }

        // Load Indonesian config for player locale & SeriaTranslator resolution
        File idFile = new File(plugin.getDataFolder(), "messages_id.yml");
        if (idFile.exists()) {
            idConfig = YamlConfiguration.loadConfiguration(idFile);
        } else {
            InputStream idStream = plugin.getResource("messages_id.yml");
            if (idStream != null) {
                idConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(idStream, StandardCharsets.UTF_8));
            }
        }

        prefix = langConfig.getString("prefix", "");
    }

    private void saveResourceIfNotExists(String resourceName) {
        File file = new File(plugin.getDataFolder(), resourceName);
        if (!file.exists()) {
            try {
                plugin.saveResource(resourceName, false);
            } catch (Exception ignored) {}
        }
    }

    @NotNull
    public Component getMessage(@Nullable Player player, @NotNull String path, @NotNull String... replacements) {
        String raw = resolveRawString(player, path);
        if (raw == null) {
            return TextUtil.parse("<red>Missing message: " + path + "</red>");
        }

        String full = prefix + raw;
        for (int i = 0; i < replacements.length - 1; i += 2) {
            full = full.replace(replacements[i], replacements[i + 1]);
        }

        return TextUtil.parseForPlayer(player, full);
    }

    @NotNull
    public Component getRawMessage(@Nullable Player player, @NotNull String path, @NotNull String... replacements) {
        String raw = resolveRawString(player, path);
        if (raw == null) raw = "";

        for (int i = 0; i < replacements.length - 1; i += 2) {
            raw = raw.replace(replacements[i], replacements[i + 1]);
        }
        return TextUtil.parseForPlayer(player, raw);
    }

    @NotNull
    public String getRawString(@NotNull String path, @NotNull String def) {
        String res = langConfig != null ? langConfig.getString(path) : null;
        if (res != null) return res;
        if (fallbackConfig != null && fallbackConfig.contains(path)) return fallbackConfig.getString(path);
        return def;
    }

    private String resolveRawString(@Nullable Player player, @NotNull String path) {
        boolean prefersIndonesian = isIndonesianPlayer(player);

        if (prefersIndonesian && idConfig != null && idConfig.contains(path)) {
            return idConfig.getString(path);
        }

        if (langConfig != null && langConfig.contains(path)) {
            return langConfig.getString(path);
        }

        if (fallbackConfig != null && fallbackConfig.contains(path)) {
            return fallbackConfig.getString(path);
        }

        if (idConfig != null && idConfig.contains(path)) {
            return idConfig.getString(path);
        }

        return null;
    }

    public boolean isIndonesianPlayer(@Nullable Player player) {
        if (player == null) {
            return plugin.getConfigManager().getLanguageFileName().toLowerCase().contains("id");
        }

        if (plugin.getHookManager().isSeriaTranslatorLoaded()) {
            String tritonLang = plugin.getHookManager().getSeriaTranslatorHook().getPlayerLanguage(player);
            if (tritonLang != null) {
                String lower = tritonLang.toLowerCase();
                if (lower.startsWith("id") || lower.contains("indonesia")) {
                    return true;
                }
                if (lower.startsWith("en")) {
                    return false;
                }
            }
        }

        String locale = player.getLocale();
        if (locale != null) {
            String lower = locale.toLowerCase();
            if (lower.startsWith("id") || lower.contains("indonesia")) {
                return true;
            }
        }

        return plugin.getConfigManager().getLanguageFileName().toLowerCase().contains("id");
    }

    public String getPrefix() {
        return prefix;
    }
}
