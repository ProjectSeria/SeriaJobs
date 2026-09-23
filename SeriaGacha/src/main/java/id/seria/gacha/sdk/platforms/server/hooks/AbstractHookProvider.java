package id.seria.gacha.sdk.platforms.server.hooks;

import id.seria.gacha.api.sdk.plugin.hooks.HookProvider;
import id.seria.gacha.api.sdk.plugin.hooks.HookRegistry;
import id.seria.gacha.api.sdk.utilities.Validate;
import id.seria.gacha.sdk.platforms.server.registries.ServerHookRegistry;
import java.util.Objects;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractHookProvider implements HookProvider {
   protected final String name;
   protected String description = "";
   protected String url = "";
   private boolean silent;
   protected ServerHookRegistry registry;

   public AbstractHookProvider(String name) {
      this.name = name;
   }

   public ServerHookRegistry getRegistry() {
      return Objects.requireNonNull(this.registry, "Hook not registered");
   }

   @Override
   public void setRegistry(@NotNull HookRegistry registry) {
      Validate.isTrue(registry instanceof ServerHookRegistry, "Incompatible HookRegistry instance");
      Validate.isTrue(this.registry == null, "Hook already registered");
      this.registry = (ServerHookRegistry)registry;
   }

   public abstract ServerHook createHook();

   @Generated
   @Override
   public String getName() {
      return this.name;
   }

   @Generated
   @Override
   public String getDescription() {
      return this.description;
   }

   @Generated
   @Override
   public String getUrl() {
      return this.url;
   }

   @Generated
   public boolean isSilent() {
      return this.silent;
   }

   @Generated
   @Override
   public void setDescription(String description) {
      this.description = description;
   }

   @Generated
   @Override
   public void setUrl(String url) {
      this.url = url;
   }

   @Generated
   public void setSilent(boolean silent) {
      this.silent = silent;
   }
}
