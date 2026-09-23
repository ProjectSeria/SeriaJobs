package id.seria.gacha.sdk.platforms.server.hooks;

import id.seria.gacha.api.sdk.plugin.hooks.Hook;
import id.seria.gacha.sdk.platforms.server.registries.ServerHookRegistry;
import lombok.Generated;

public abstract class ServerHook implements Hook {
   private final ServerHook.Unsafe unsafe = new ServerHook.Unsafe();
   private ServerHookRegistry hookRegistry;
   private AbstractHookProvider provider;
   private boolean enabled;

   public ServerHook() {
   }

   public ServerHook.Unsafe unsafe() {
      return this.unsafe;
   }

   @Generated
   public void setHookRegistry(ServerHookRegistry hookRegistry) {
      this.hookRegistry = hookRegistry;
   }

   @Generated
   public void setProvider(AbstractHookProvider provider) {
      this.provider = provider;
   }

   @Generated
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Generated
   public ServerHookRegistry getHookRegistry() {
      return this.hookRegistry;
   }

   @Generated
   public AbstractHookProvider getProvider() {
      return this.provider;
   }

   @Generated
   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   public class Unsafe {
      public Unsafe() {
      }

      public void initialize(ServerHookRegistry hookRegistry) throws Exception {
         ServerHook.this.hookRegistry = hookRegistry;
         ServerHook.this.init();
      }

      public void setEnabled(boolean state) {
         ServerHook.this.enabled = state;
      }

      public void setProvider(AbstractHookProvider provider) {
         ServerHook.this.provider = provider;
      }
   }
}
