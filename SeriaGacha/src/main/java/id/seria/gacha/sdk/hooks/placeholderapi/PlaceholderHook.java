package id.seria.gacha.sdk.hooks.placeholderapi;

import id.seria.gacha.api.sdk.plugin.Plugin;
import id.seria.gacha.sdk.hooks.placeholderapi.expansions.PlaceholderRequester;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.NonNull;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderHook extends ServerHook {
   private static final String[] TIER_SUFFIXES = new String[]{"lite", "premium"};
   private final List<PlaceholderExpansion> expansions = new ArrayList<>();

   protected PlaceholderHook() {
   }

   @Override
   public void init() {
   }

   @Override
   public void load() {
      Plugin plugin = this.getHookRegistry().getPlugin();

      for (String identifier : this.getIdentifiers(plugin)) {
         PlaceholderExpansion expansion = this.createExpansion(plugin, identifier);
         expansion.register();
         this.expansions.add(expansion);
      }
   }

   private Collection<String> getIdentifiers(Plugin plugin) {
      LinkedHashSet<String> identifiers = new LinkedHashSet<>();
      String identifier = plugin.getName().toLowerCase().replace(" ", "_");
      identifiers.add(identifier);

      for (String suffix : TIER_SUFFIXES) {
         if (identifier.length() > suffix.length() && identifier.endsWith(suffix)) {
            identifiers.add(identifier.substring(0, identifier.length() - suffix.length()));
            break;
         }
      }

      return identifiers;
   }

   private PlaceholderExpansion createExpansion(final Plugin plugin, final String identifier) {
      return new PlaceholderExpansion() {
         @NotNull
         public String getIdentifier() {
            return identifier;
         }

         @NotNull
         public String getAuthor() {
            return plugin.getName().replace(" ", "");
         }

         @NotNull
         public String getVersion() {
            return "1.0.0";
         }

         public boolean persist() {
            return true;
         }

         public String onRequest(OfflinePlayer player, @NotNull String params) {
            for (PlaceholderRequester requester : PlaceholderHook.this.getProvider().getRequesters().values()) {
               String result = requester.onRequest(player, params);
               if (result != null) {
                  return result;
               }
            }

            return null;
         }
      };
   }

   @Override
   public void unload() {
      for (PlaceholderExpansion expansion : this.expansions) {
         expansion.unregister();
      }

      this.expansions.clear();
   }

   @Override
   public void reload() throws Exception {
      this.unload();
      this.load();
   }

   public PlaceholderHookProvider getProvider() {
      return (PlaceholderHookProvider)super.getProvider();
   }

   public String setPlaceholders(@NonNull String text, Player player) {
      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      } else {
         return PlaceholderAPI.setPlaceholders(player, text);
      }
   }
}
