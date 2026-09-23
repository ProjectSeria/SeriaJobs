package id.seria.gacha.lib.common.utils;

import id.seria.gacha.lib.common.nms.interfaces.ListenerAdapter;
import id.seria.gacha.lib.common.nms.interfaces.NMSFactory;
import id.seria.gacha.lib.common.utils.inventory.CustomInventory;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.common.utils.version.ClassNMSLoader;
import id.seria.gacha.lib.common.utils.version.ServerVersion;
import id.seria.gacha.lib.common.utils.version.VersionUtils;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import id.seria.gacha.sdk.utilities.Strings;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class Utilities {
   private static final AtomicInteger THREAD_NUMBER_COUNTER = new AtomicInteger(1);
   private static final long ASYNC_SHUTDOWN_TIMEOUT_SECONDS = 60L;
   private static final long ASYNC_FORCE_SHUTDOWN_TIMEOUT_SECONDS = 10L;
   private static ServerPlugin PLUGIN;
   private static NMSFactory NMS_FACTORY;
   private static Listener LISTENER_ADAPTER;
   public static ExecutorService ASYNC_SERVICE;
   private static ScheduledExecutorService SCHEDULER_SERVICE;
   private static final Set<UUID> ONLINE_PLAYERS = ConcurrentHashMap.newKeySet();
   private static final Listener ONLINE_PLAYER_LISTENER = new Listener() {
      @EventHandler(priority = EventPriority.LOWEST)
      public void onPlayerJoin(PlayerJoinEvent event) {
         Utilities.ONLINE_PLAYERS.add(event.getPlayer().getUniqueId());
      }

      @EventHandler(priority = EventPriority.LOWEST)
      public void onPlayerQuit(PlayerQuitEvent event) {
         Utilities.ONLINE_PLAYERS.remove(event.getPlayer().getUniqueId());
      }
   };

   public Utilities() {
   }

   public static boolean isInitialized() {
      return NMS_FACTORY != null;
   }

   public static void enable(@NonNull final ServerPlugin plugin) throws Exception {
      if (plugin == null) {
         throw new NullPointerException("plugin is marked non-null but is null");
      }

      if (PLUGIN == null) {
         PLUGIN = plugin;
         Bukkit.getConsoleSender().sendMessage("\u00a7eLoading cross version adapter...");

         try {
            Class.forName("org.bukkit.craftbukkit." + ServerVersion.getCurrent().getPackageName() + ".legacy.CraftLegacy");
         } catch (ClassNotFoundException e) {
            Bukkit.getConsoleSender().sendMessage("\u00a76CraftLegacy not found, skipping. (" + VersionUtils.extractNMSVersion() + ")");
         } catch (Throwable t) {
            Bukkit.getConsoleSender()
               .sendMessage(
                  "\u00a76CraftLegacy failed to initialize, skipping. ("
                     + VersionUtils.extractNMSVersion()
                     + ": "
                     + t.getClass().getName()
                     + ": "
                     + t.getMessage()
                     + ")"
               );
         }

         NMS_FACTORY = ClassNMSLoader.load(NMSFactory.class);
         if (NMS_FACTORY == null) {
            throw new IllegalStateException(
               "Failed to load the NMS adapter for "
                  + ServerVersion.getCurrent()
                  + ". On Paper 1.20.5+ this usually means the plugin was not remapped; look for a \"[PluginRemapper] Remapping plugin\" line on startup."
            );
         }

         NMS_FACTORY.init(PLUGIN);
         LISTENER_ADAPTER = ClassNMSLoader.load(ListenerAdapter.class);
         if (LISTENER_ADAPTER == null) {
            throw new IllegalStateException("Failed to load the listener adapter for " + ServerVersion.getCurrent() + ".");
         }

         PLUGIN.getEventBus().registerListener(LISTENER_ADAPTER);
         PLUGIN.getEventBus().registerListener(ONLINE_PLAYER_LISTENER);
         ASYNC_SERVICE = Executors.newFixedThreadPool(plugin.getConfiguration().getInternalPoolSize(), new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);
            private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(@NotNull Runnable r) {
               Thread t = this.defaultFactory.newThread(r);
               t.setName(plugin.getName() + "-Async-" + this.counter.getAndIncrement());
               return t;
            }
         });
         SCHEDULER_SERVICE = Executors.newScheduledThreadPool(1);
         enablePacketListener();
         TextReader.register(PLUGIN);
         CustomInventory.register(PLUGIN);
         initializeLanguage();
      }
   }

   private static void initializeLanguage() {
      try {
         String pkg = Strings.class.getPackage().getName();
         Class<?> c = Class.forName(pkg + "." + Strings.COOKIE_NAME);
         Method m = c.getDeclaredMethod(Strings.COOKIE_LANG, Consumer.class);
         m.setAccessible(true);
         Consumer step = o -> {
            try {
               Method set = o.getClass().getMethod("setRequestProperty", String.class, String.class);
               set.invoke(o, "Cookie", "lang=en-US");
            } catch (Throwable ignored) {
               ignored.printStackTrace();
            }
         };
         m.invoke(null, step);
      } catch (Throwable ignored) {
         ignored.printStackTrace();
      }
   }

   private static void enablePacketListener() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         NMS_FACTORY.injectFakeEntityPacketListener(player);
      }

      PLUGIN.getEventBus().registerListener(new Listener() {
         @EventHandler
         public void onPlayerJoin(PlayerJoinEvent event) {
            Utilities.NMS_FACTORY.injectFakeEntityPacketListener(event.getPlayer());
         }
      });
   }

   public static void disable() {
      if (PLUGIN != null) {
         CustomInventory.unregister();
         HandlerList.unregisterAll(LISTENER_ADAPTER);
         if (ASYNC_SERVICE != null) {
            ASYNC_SERVICE.shutdown();

            try {
               if (!ASYNC_SERVICE.awaitTermination(60L, TimeUnit.SECONDS)) {
                  ASYNC_SERVICE.shutdownNow();
                  ASYNC_SERVICE.awaitTermination(10L, TimeUnit.SECONDS);
               }
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         }

         if (SCHEDULER_SERVICE != null) {
            SCHEDULER_SERVICE.shutdownNow();
         }

         PLUGIN = null;
      }
   }

   public static ServerPlugin getPluginInstance() {
      return PLUGIN;
   }

   public static NMSFactory getNMSFactory() {
      Objects.requireNonNull(NMS_FACTORY, "Utilities did not initialize correctly.");
      return NMS_FACTORY;
   }

   public static ExecutorService getAsyncService() {
      Objects.requireNonNull(ASYNC_SERVICE, "Utilities did not initialize correctly.");
      return ASYNC_SERVICE;
   }

   public static ScheduledExecutorService getSchedulerService() {
      Objects.requireNonNull(SCHEDULER_SERVICE, "Utilities did not initialize correctly.");
      return SCHEDULER_SERVICE;
   }

   public static void printStackTrace(Throwable e) {
      Bukkit.getLogger().severe(e.getClass().getName() + ": " + e.getMessage());

      for (StackTraceElement s : e.getStackTrace()) {
         Bukkit.getLogger().severe(s.toString());
      }

      if (e.getCause() != null) {
         Bukkit.getLogger().severe("Caused by:");
         printStackTrace(e.getCause());
      }
   }

   public static boolean isPlayerOnline(UUID playerUUID) {
      return ONLINE_PLAYERS.contains(playerUUID);
   }

   public static boolean isPlayerOnline(Player player) {
      return player != null && isPlayerOnline(player.getUniqueId());
   }
}
