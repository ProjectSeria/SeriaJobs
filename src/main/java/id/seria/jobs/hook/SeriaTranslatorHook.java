package id.seria.jobs.hook;

import id.seria.translator.api.Triton;
import id.seria.translator.api.TritonAPI;
import id.seria.translator.api.language.Language;
import id.seria.translator.api.language.TranslationResult;
import id.seria.translator.api.players.LanguagePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SeriaTranslatorHook {

    private boolean available = false;

    public boolean setup() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("SeriaTranslator");
        if (plugin != null && plugin.isEnabled()) {
            try {
                Triton instance = TritonAPI.getInstance();
                if (instance != null) {
                    available = true;
                    return true;
                }
            } catch (Throwable t) {
                available = true;
                return true;
            }
        }
        return false;
    }

    public boolean isAvailable() {
        return available;
    }

    public String translate(Player player, String text) {
        if (!available || player == null || text == null || text.isEmpty()) {
            return text;
        }

        try {
            Triton triton = TritonAPI.getInstance();
            if (triton == null) return text;

            LanguagePlayer lp = triton.getPlayerManager().get(player.getUniqueId());
            Language lang = (lp != null && lp.getLanguage() != null)
                    ? lp.getLanguage()
                    : triton.getLanguageManager().getMainLanguage();

            if (lang == null) return text;

            TranslationResult<String> result = triton.getMessageParser().translateString(
                    text,
                    lang,
                    triton.getConfig().getChatSyntax()
            );

            if (result.isChanged()) {
                return result.getResult().orElse(text);
            }
            return text;
        } catch (Throwable t) {
            return text;
        }
    }

    public String getPlayerLanguage(Player player) {
        if (!available || player == null) return "id_ID";
        try {
            Triton triton = TritonAPI.getInstance();
            LanguagePlayer lp = triton.getPlayerManager().get(player.getUniqueId());
            if (lp != null && lp.getLanguage() != null) {
                return lp.getLanguage().getName();
            }
        } catch (Throwable ignored) {}
        return "id_ID";
    }
}
