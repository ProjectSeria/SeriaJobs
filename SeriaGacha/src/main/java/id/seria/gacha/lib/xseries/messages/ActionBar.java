package id.seria.gacha.lib.xseries.messages;

import com.google.common.base.Strings;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftConnection;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftPackage;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Objects;
import java.util.concurrent.Callable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ActionBar {
   private static final boolean USE_SPIGOT_API = XReflection.of(Player.class)
      .inner("public static class Spigot")
      .method("public void sendMessage(net.md_5.bungee.api.ChatMessageType position,net.md_5.bungee.api.chat.BaseComponent component)")
      .exists();
   private static final MethodHandle CHAT_COMPONENT_TEXT;
   private static final MethodHandle PACKET_PLAY_OUT_CHAT;
   private static final Object CHAT_MESSAGE_TYPE;
   private static final char TIME_SPECIFIER_START = '^';
   private static final char TIME_SPECIFIER_END = '|';

   private ActionBar() {
   }

   public static void sendActionBar(@NotNull Plugin plugin, @NotNull Player player, @Nullable String message) {
      if (!Strings.isNullOrEmpty(message) && message.charAt(0) == '^') {
         int end = message.indexOf(124);
         if (end != -1) {
            int time = 0;

            try {
               time = Integer.parseInt(message.substring(1, end)) * 20;
            } catch (NumberFormatException var6) {
            }

            if (time >= 0) {
               sendActionBar(plugin, player, MessageComponents.fromLegacy(message.substring(end + 1)), time);
            }
         }
      }

      sendActionBar(player, message);
   }

   public static void sendActionBar(@NotNull Player player, @Nullable String message) {
      Objects.requireNonNull(player, "Cannot send action bar to null player");
      Objects.requireNonNull(message, "Cannot send null actionbar message");
      if (USE_SPIGOT_API) {
         player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
      } else {
         try {
            Object component = (Object)CHAT_COMPONENT_TEXT.invoke((String)("{\"text\":\"" + message.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}"));
            Object packet = (Object)PACKET_PLAY_OUT_CHAT.invoke((Object)component, (Object)CHAT_MESSAGE_TYPE);
            MinecraftConnection.sendPacket(player, packet);
         } catch (Throwable throwable) {
            throwable.printStackTrace();
         }
      }
   }

   public static void sendActionBar(@NotNull Player player, @Nullable BaseComponent message) {
      Objects.requireNonNull(player, "Cannot send action bar to null player");
      Objects.requireNonNull(message, "Cannot send null actionbar message");
      if (USE_SPIGOT_API) {
         player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
      } else {
         try {
            Object component = (Object)CHAT_COMPONENT_TEXT.invoke(
               (String)("{\"text\":\"" + message.toPlainText().replace("\\", "\\\\").replace("\"", "\\\"") + "\"}")
            );
            Object packet = (Object)PACKET_PLAY_OUT_CHAT.invoke((Object)component, (Object)CHAT_MESSAGE_TYPE);
            MinecraftConnection.sendPacket(player, packet);
         } catch (Throwable throwable) {
            throwable.printStackTrace();
         }
      }
   }

   public static void sendActionBar(@NotNull Plugin plugin, @NotNull final Player player, @Nullable final BaseComponent message, final long duration) {
      if (duration >= 1L) {
         Objects.requireNonNull(plugin, "Cannot send consistent actionbar with null plugin");
         Objects.requireNonNull(player, "Cannot send actionbar to null player");
         Objects.requireNonNull(message, "Cannot send null actionbar message");
         (new BukkitRunnable() {
            long repeater = duration;

            public void run() {
               ActionBar.sendActionBar(player, message);
               this.repeater -= 40L;
               if (this.repeater - 40L < -20L) {
                  this.cancel();
               }
            }
         }).runTaskTimerAsynchronously(plugin, 0L, 40L);
      }
   }

   @Deprecated
   public static void sendPlayersActionBar(@Nullable String message) {
      for (Player player : Bukkit.getOnlinePlayers()) {
         sendActionBar(player, message);
      }
   }

   public static void clearActionBar(@NotNull Player player) {
      sendActionBar(player, " ");
   }

   @Deprecated
   public static void clearPlayersActionBar() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         clearActionBar(player);
      }
   }

   public static void sendActionBarWhile(
      @NotNull Plugin plugin, @NotNull final Player player, @Nullable final BaseComponent message, @NotNull final Callable<Boolean> callable
   ) {
      (new BukkitRunnable() {
         public void run() {
            try {
               if (!callable.call()) {
                  this.cancel();
                  return;
               }
            } catch (Exception ex) {
               ex.printStackTrace();
            }

            ActionBar.sendActionBar(player, message);
         }
      }).runTaskTimerAsynchronously(plugin, 0L, 40L);
   }

   public static void sendActionBarWhile(
      @NotNull Plugin plugin, @NotNull final Player player, @Nullable final Callable<BaseComponent> message, @NotNull final Callable<Boolean> callable
   ) {
      (new BukkitRunnable() {
         public void run() {
            try {
               if (!callable.call()) {
                  this.cancel();
                  return;
               }

               ActionBar.sendActionBar(player, message.call());
            } catch (Exception ex) {
               ex.printStackTrace();
            }
         }
      }).runTaskTimerAsynchronously(plugin, 0L, 40L);
   }

   static {
      MethodHandle packet = null;
      MethodHandle chatComp = null;
      Object chatMsgType = null;
      if (!USE_SPIGOT_API) {
         Lookup lookup = MethodHandles.lookup();
         Class<?> packetPlayOutChatClass = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.protocol.game")
            .named("PacketPlayOutChat")
            .unreflect();
         Class<?> iChatBaseComponentClass = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "network.chat").named("IChatBaseComponent").unreflect();
         Class<?> ChatSerializerClass = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.chat")
            .named("IChatBaseComponent$ChatSerializer")
            .unreflect();

         try {
            chatComp = lookup.findStatic(ChatSerializerClass, "a", MethodType.methodType(iChatBaseComponentClass, String.class));
            Class<?> chatMessageTypeClass = Class.forName(XReflection.NMS_PACKAGE + XReflection.v(17, "network.chat").orElse("") + "ChatMessageType");
            MethodType type = MethodType.methodType(void.class, iChatBaseComponentClass, chatMessageTypeClass);

            for (Object obj : chatMessageTypeClass.getEnumConstants()) {
               String name = obj.toString();
               if (name.equals("GAME_INFO") || name.equalsIgnoreCase("ACTION_BAR")) {
                  chatMsgType = obj;
                  break;
               }
            }

            packet = lookup.findConstructor(packetPlayOutChatClass, type);
         } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException ignored) {
            try {
               chatMsgType = (byte)2;
               packet = lookup.findConstructor(packetPlayOutChatClass, MethodType.methodType(void.class, iChatBaseComponentClass, byte.class));
            } catch (NoSuchMethodException | IllegalAccessException ex) {
               ex.printStackTrace();
            }
         }
      }

      CHAT_MESSAGE_TYPE = chatMsgType;
      CHAT_COMPONENT_TEXT = chatComp;
      PACKET_PLAY_OUT_CHAT = packet;
   }
}
