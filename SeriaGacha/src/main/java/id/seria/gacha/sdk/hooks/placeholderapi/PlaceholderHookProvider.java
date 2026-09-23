package id.seria.gacha.sdk.hooks.placeholderapi;

import id.seria.gacha.sdk.hooks.placeholderapi.expansions.PlaceholderRequester;
import id.seria.gacha.sdk.platforms.server.hooks.PluginHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Generated;

public class PlaceholderHookProvider extends PluginHookProvider {
   private final Map<String, PlaceholderRequester> requesters = new LinkedHashMap<>();

   public PlaceholderHookProvider(PlaceholderRequester... requesters) {
      super("PlaceholderAPI");

      for (int index = 0; index < requesters.length; index++) {
         this.registerRequester("generic" + (index + 1), requesters[index]);
      }

      this.setDescription("If you want to support more placeholders in this plugin install it.");
      this.setUrl("https://www.spigotmc.org/resources/placeholderapi.6245/");
   }

   public void registerRequester(String key, PlaceholderRequester requester) {
      String normalizedKey = Objects.requireNonNull(key, "key").trim();
      if (normalizedKey.isEmpty()) {
         throw new IllegalArgumentException("key cannot be blank");
      }

      this.requesters.put(normalizedKey, Objects.requireNonNull(requester, "requester"));
   }

   public PlaceholderRequester unregisterRequester(String key) {
      String normalizedKey = Objects.requireNonNull(key, "key").trim();
      return normalizedKey.isEmpty() ? null : this.requesters.remove(normalizedKey);
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      PlaceholderHook hook = new PlaceholderHook();
      hook.unsafe().setProvider(this);
      return hook;
   }

   @Generated
   public Map<String, PlaceholderRequester> getRequesters() {
      return this.requesters;
   }
}
