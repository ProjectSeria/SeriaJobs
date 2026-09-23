package id.seria.gacha.sdk.platforms.server.registries;

import id.seria.gacha.api.sdk.plugin.hooks.HookProvider;
import id.seria.gacha.api.sdk.plugin.hooks.HookRegistry;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import id.seria.gacha.sdk.platforms.server.hooks.AbstractHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.NamespacedItemHook;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ServerHookRegistry implements HookRegistry {
   private final ServerPlugin plugin;
   private final Set<AbstractHookProvider> providers = new HashSet<>();
   private final Set<ServerHook> registeredHooks = new HashSet<>();
   private final List<Runnable> onLoadTasks = new ArrayList<>();

   public ServerHookRegistry(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   public void registerProviders(AbstractHookProvider... providers) {
      Arrays.stream(providers).forEach(this::registerProvider);
   }

   public void registerProvider(AbstractHookProvider provider) {
      provider.setRegistry(this);
      this.providers.add(provider);
   }

   public void registerHooks() {
      for (AbstractHookProvider provider : this.providers) {
         if (!provider.isAvailable()) {
            if (!provider.isSilent()) {
               Bukkit.getConsoleSender().sendMessage("\u00a77" + provider.getName() + " is not installed. " + provider.getDescription());
               if (!provider.getUrl().isEmpty()) {
                  Bukkit.getConsoleSender().sendMessage("\u00a77-> See more at: " + provider.getUrl());
               }
            }
         } else if (!provider.isEnabled()) {
            Bukkit.getConsoleSender()
               .sendMessage("\u00a77" + provider.getName() + " founded but didn't loaded correctly. (" + provider.getName() + " is disabled)");
         } else if (!provider.isCompatible()) {
            Bukkit.getConsoleSender().sendMessage("\u00a77" + provider.getName() + " found but not supported! Make sure to upgrade.");
         } else {
            Bukkit.getConsoleSender().sendMessage("\u00a77" + provider.getName() + " founded. Preparing hook...");

            try {
               ServerHook hook = provider.createHook();
               this.initHook(hook);
               this.registeredHooks.add(hook);
            } catch (Exception e) {
               this.plugin.getLogger().error("Error initializing hook " + provider.getName(), e);
            }
         }
      }
   }

   private void initHook(ServerHook hook) {
      try {
         hook.unsafe().initialize(this);
         hook.init();
      } catch (Exception e) {
         this.plugin.getLogger().error("Error while initializing hook", e);
      }
   }

   public void loadHooks() {
      for (ServerHook hook : this.registeredHooks) {
         this.loadHook(hook);
      }

      List<Runnable> tasksToRun;
      synchronized (this.onLoadTasks) {
         if (this.onLoadTasks.isEmpty()) {
            return;
         }

         tasksToRun = new ArrayList<>(this.onLoadTasks);
         this.onLoadTasks.clear();
      }

      for (Runnable task : tasksToRun) {
         try {
            task.run();
         } catch (Exception e) {
            this.plugin.getLogger().error("Error while executing onLoad task", e);
         }
      }
   }

   public void unloadHooks() {
      for (ServerHook hook : this.registeredHooks) {
         this.unloadHook(hook);
      }
   }

   public void loadHook(ServerHook hook) {
      if (!hook.isEnabled()) {
         try {
            hook.load();
            hook.unsafe().setEnabled(true);
         } catch (Exception e) {
            this.plugin.getLogger().error("Error while loading hook", e);
            this.unloadHook(hook);
         }
      }
   }

   public void unloadHook(ServerHook hook) {
      if (hook.isEnabled()) {
         try {
            hook.unload();
         } catch (Exception e) {
            this.plugin.getLogger().error("Error while unloading hook", e);
         } finally {
            hook.unsafe().setEnabled(false);
         }
      }
   }

   public boolean isHookEnabled(Class<? extends AbstractHookProvider> clazz) {
      for (ServerHook hook : this.registeredHooks) {
         if (clazz.isInstance(hook.getProvider()) && hook.isEnabled()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public <P extends HookProvider> P getProvider(Class<P> clazz) {
      for (AbstractHookProvider provider : this.providers) {
         if (clazz.isInstance(provider)) {
            return clazz.cast(provider);
         }
      }

      throw new IllegalStateException("No provider registered for " + clazz.getName());
   }

   public <H extends ServerHook> H getHook(Class<H> clazz) {
      for (ServerHook hook : this.registeredHooks) {
         if (clazz.isInstance(hook) && hook.isEnabled()) {
            return (H)hook;
         }
      }

      throw new IllegalStateException("No hook registered for " + clazz.getName());
   }

   @Nullable
   public NamespacedItemHook getNamespacedHook(String namespace) {
      for (ServerHook hook : this.registeredHooks) {
         if (hook instanceof NamespacedItemHook && hook.isEnabled() && ((NamespacedItemHook)hook).getNamespace().equalsIgnoreCase(namespace)) {
            return (NamespacedItemHook)hook;
         }
      }

      return null;
   }

   @Nullable
   public ItemStack resolveNamespacedMaterial(String hookId) {
      if (hookId == null) {
         return null;
      }

      int sep = hookId.indexOf(58);
      if (sep < 0) {
         return null;
      }

      String namespace = hookId.substring(0, sep).toLowerCase();
      String id = hookId.substring(sep + 1);
      NamespacedItemHook hook = this.getNamespacedHook(namespace);
      return hook == null ? null : hook.getItemFromId(id);
   }

   public void onLoad(Runnable runnable) {
      if (runnable != null) {
         synchronized (this.onLoadTasks) {
            this.onLoadTasks.add(runnable);
         }
      }
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   @Override
   public Set<AbstractHookProvider> getProviders() {
      return this.providers;
   }

   @Generated
   public Set<ServerHook> getRegisteredHooks() {
      return this.registeredHooks;
   }

   @Generated
   public List<Runnable> getOnLoadTasks() {
      return this.onLoadTasks;
   }
}
