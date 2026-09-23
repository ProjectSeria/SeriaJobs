package id.seria.gacha.lib.xseries.messages;

import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftClassHandle;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftConnection;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftPackage;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Titles {
   private static final Object TITLE_ACTION_TITLE;
   private static final Object TITLE_ACTION_SUBTITLE;
   private static final Object TITLE_ACTION_TIMES;
   private static final Object TITLE_ACTION_CLEAR;
   private static final MethodHandle PACKET_PLAY_OUT_TITLE;
   private static final MethodHandle CHAT_COMPONENT_TEXT;
   private static final MethodHandle ClientboundSetTitlesAnimationPacket;
   private static final MethodHandle ClientboundSetTitleTextPacket;
   private static final MethodHandle ClientboundSetSubtitleTextPacket;
   @Nullable
   private MessageComponents.MessageText title;
   @Nullable
   private MessageComponents.MessageText subtitle;
   private final int fadeIn;
   private final int stay;
   private final int fadeOut;
   private static final boolean SUPPORTS_TITLES;
   private static final boolean USE_TEXT_COMPONENTS;

   public Titles(MessageComponents.MessageText title, MessageComponents.MessageText subtitle, int fadeIn, int stay, int fadeOut) {
      this.title = title;
      this.subtitle = subtitle;
      this.fadeIn = fadeIn;
      this.stay = stay;
      this.fadeOut = fadeOut;
   }

   public Titles(BaseComponent title, BaseComponent subtitle, int fadeIn, int stay, int fadeOut) {
      this(MessageComponents.ofNullable(title), MessageComponents.ofNullable(subtitle), fadeIn, stay, fadeOut);
   }

   public Titles(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
      this(MessageComponents.ofNullable(title), MessageComponents.ofNullable(subtitle), fadeIn, stay, fadeOut);
   }

   public Titles copy() {
      return new Titles(this.title, this.subtitle, this.fadeIn, this.stay, this.fadeOut);
   }

   public void send(Player player) {
      sendTitle(player, this.fadeIn, this.stay, this.fadeOut, this.title, this.subtitle);
   }

   public static void sendTitle(@NotNull Player player, int fadeIn, int stay, int fadeOut, @Nullable String title, @Nullable String subtitle) {
      sendTitle(player, fadeIn, stay, fadeOut, MessageComponents.ofNullable(title), MessageComponents.ofNullable(subtitle));
   }

   public static void sendTitle(
      @NotNull Player player,
      int fadeIn,
      int stay,
      int fadeOut,
      @Nullable MessageComponents.MessageText title,
      @Nullable MessageComponents.MessageText subtitle
   ) {
      Objects.requireNonNull(player, "Cannot send title to null player");
      if (title != null || subtitle != null) {
         if (USE_TEXT_COMPONENTS) {
            List<Object> packets = new ArrayList<>(3);

            try {
               packets.add((Object)ClientboundSetTitlesAnimationPacket.invoke((int)fadeIn, (int)stay, (int)fadeOut));
               if (title != null) {
                  packets.add((Object)ClientboundSetTitleTextPacket.invoke((Object)MessageComponents.bungeeToVanilla(title.asComponent())));
               }

               if (subtitle != null) {
                  packets.add((Object)ClientboundSetSubtitleTextPacket.invoke((Object)MessageComponents.bungeeToVanilla(subtitle.asComponent())));
               }
            } catch (Throwable ex) {
               throw new IllegalStateException("Failed to create packets with title: " + title + " and subtitle: " + subtitle, ex);
            }

            MinecraftConnection.sendPacket(player, packets.toArray(new Object[0]));
         } else if (SUPPORTS_TITLES) {
            player.sendTitle(title.asString(), subtitle.asString(), fadeIn, stay, fadeOut);
         } else {
            try {
               List<Object> packets = new ArrayList<>(3);
               Object titleComponent = (Object)CHAT_COMPONENT_TEXT.invoke((Object)(title == null ? null : title.asString()));
               packets.add((Object)PACKET_PLAY_OUT_TITLE.invoke((Object)TITLE_ACTION_TIMES, (Object)titleComponent, (int)fadeIn, (int)stay, (int)fadeOut));
               if (title != null) {
                  packets.add((Object)PACKET_PLAY_OUT_TITLE.invoke((Object)TITLE_ACTION_TITLE, (Object)titleComponent, (int)fadeIn, (int)stay, (int)fadeOut));
               }

               if (subtitle != null) {
                  Object subtitleComponent = (Object)CHAT_COMPONENT_TEXT.invoke((String)subtitle.asString());
                  packets.add(
                     (Object)PACKET_PLAY_OUT_TITLE.invoke((Object)TITLE_ACTION_SUBTITLE, (Object)subtitleComponent, (int)fadeIn, (int)stay, (int)fadeOut)
                  );
               }

               MinecraftConnection.sendPacket(player, packets.toArray(new Object[0]));
            } catch (Throwable ex) {
               throw new IllegalStateException("Failed to send packets for title: " + title + " and subtitle: " + subtitle, ex);
            }
         }
      }
   }

   public static void sendTitle(@NotNull Player player, @NotNull String title, @NotNull String subtitle) {
      sendTitle(player, 10, 20, 10, title, subtitle);
   }

   public static Titles sendTitle(@NotNull Player player, @NotNull ConfigurationSection config) {
      Titles titles = parseTitle(config, null);
      titles.send(player);
      return titles;
   }

   public static Titles parseTitle(@NotNull ConfigurationSection config) {
      return parseTitle(config, null);
   }

   public static Titles parseTitle(@NotNull ConfigurationSection config, @Nullable Function<String, String> transformers) {
      String title = config.getString("title");
      String subtitle = config.getString("subtitle");
      if (transformers != null) {
         title = transformers.apply(title);
         subtitle = transformers.apply(subtitle);
      }

      int fadeIn = config.getInt("fade-in");
      int stay = config.getInt("stay");
      int fadeOut = config.getInt("fade-out");
      if (fadeIn < 1) {
         fadeIn = 10;
      }

      if (stay < 1) {
         stay = 20;
      }

      if (fadeOut < 1) {
         fadeOut = 10;
      }

      return new Titles(title, subtitle, fadeIn, stay, fadeOut);
   }

   public MessageComponents.MessageText getTitle() {
      return this.title;
   }

   public MessageComponents.MessageText getSubtitle() {
      return this.subtitle;
   }

   public void setTitle(String title) {
      this.title = MessageComponents.ofNullable(title);
   }

   public void setSubtitle(String subtitle) {
      this.subtitle = MessageComponents.ofNullable(subtitle);
   }

   public void setTitle(BaseComponent title) {
      this.title = MessageComponents.ofNullable(title);
   }

   public void setSubtitle(BaseComponent subtitle) {
      this.subtitle = MessageComponents.ofNullable(subtitle);
   }

   public static void clearTitle(@NotNull Player player) {
      Objects.requireNonNull(player, "Cannot clear title from null player");
      if (XReflection.supports(11)) {
         player.resetTitle();
      } else {
         Object clearPacket;
         try {
            clearPacket = (Object)PACKET_PLAY_OUT_TITLE.invoke((Object)TITLE_ACTION_CLEAR, (Void)null, (int)-1, (int)-1, (int)-1);
         } catch (Throwable throwable) {
            throwable.printStackTrace();
            return;
         }

         MinecraftConnection.sendPacket(player, clearPacket);
      }
   }

   public static void sendTabList(@NotNull String header, @NotNull String footer, Player... players) {
      Objects.requireNonNull(players, "Cannot send tab title to null players");
      Objects.requireNonNull(header, "Tab title header cannot be null");
      Objects.requireNonNull(footer, "Tab title footer cannot be null");
      if (XReflection.supports(13)) {
         for (Player player : players) {
            player.setPlayerListHeaderFooter(header, footer);
         }
      } else {
         try {
            Class<?> IChatBaseComponent = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "network.chat").named("IChatBaseComponent").unreflect();
            Class<?> PacketPlayOutPlayerListHeaderFooter = XReflection.ofMinecraft()
               .inPackage(MinecraftPackage.NMS, "network.protocol.game")
               .named("PacketPlayOutPlayerListHeaderFooter")
               .unreflect();
            Method chatComponentBuilderMethod = IChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class);
            Object tabHeader = chatComponentBuilderMethod.invoke(null, "{\"text\":\"" + header + "\"}");
            Object tabFooter = chatComponentBuilderMethod.invoke(null, "{\"text\":\"" + footer + "\"}");
            Object packet = PacketPlayOutPlayerListHeaderFooter.getConstructor().newInstance();
            Field headerField = PacketPlayOutPlayerListHeaderFooter.getDeclaredField("a");
            Field footerField = PacketPlayOutPlayerListHeaderFooter.getDeclaredField("b");
            headerField.setAccessible(true);
            headerField.set(packet, tabHeader);
            footerField.setAccessible(true);
            footerField.set(packet, tabFooter);

            for (Player player : players) {
               MinecraftConnection.sendPacket(player, packet);
            }
         } catch (Exception ex) {
            throw new IllegalStateException("Failed to send tablist: " + header + " - " + footer, ex);
         }
      }
   }

   static {
      MethodHandle packetCtor = null;
      MethodHandle chatComp = null;
      MethodHandle animationCtor = null;
      MethodHandle titleCtor = null;
      MethodHandle subtitleCtor = null;
      Object times = null;
      Object title = null;
      Object subtitle = null;
      Object clear = null;
      MinecraftClassHandle IChatBaseComponentClass = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "network.chat").named("IChatBaseComponent");

      boolean useTextComponents;
      try {
         animationCtor = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.protocol.game")
            .named("ClientboundSetTitlesAnimationPacket")
            .constructor(int.class, int.class, int.class)
            .reflect();
         titleCtor = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.protocol.game")
            .named("ClientboundSetTitleTextPacket")
            .constructor(IChatBaseComponentClass)
            .reflect();
         subtitleCtor = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.protocol.game")
            .named("ClientboundSetSubtitleTextPacket")
            .constructor(IChatBaseComponentClass)
            .reflect();
         useTextComponents = true;
      } catch (Throwable ex) {
         useTextComponents = false;
      }

      boolean supportsTitles;
      try {
         Player.class.getDeclaredMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
         supportsTitles = true;
      } catch (NoSuchMethodException e) {
         supportsTitles = false;
      }

      SUPPORTS_TITLES = supportsTitles;
      if (!SUPPORTS_TITLES) {
         MinecraftClassHandle chatComponentText = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS).named("ChatComponentText");
         MinecraftClassHandle packet = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS).named("PacketPlayOutTitle");
         Class<?> titleTypes = packet.unreflect().getDeclaredClasses()[0];

         for (Object type : titleTypes.getEnumConstants()) {
            switch (type.toString()) {
               case "TIMES":
                  times = type;
                  break;
               case "TITLE":
                  title = type;
                  break;
               case "SUBTITLE":
                  subtitle = type;
                  break;
               case "CLEAR":
                  clear = type;
            }
         }

         try {
            chatComp = chatComponentText.constructor(String.class).reflect();
            packetCtor = packet.constructor(titleTypes, IChatBaseComponentClass.unreflect(), int.class, int.class, int.class).reflect();
         } catch (ReflectiveOperationException e) {
            e.printStackTrace();
         }
      }

      TITLE_ACTION_TITLE = title;
      TITLE_ACTION_SUBTITLE = subtitle;
      TITLE_ACTION_TIMES = times;
      TITLE_ACTION_CLEAR = clear;
      PACKET_PLAY_OUT_TITLE = packetCtor;
      CHAT_COMPONENT_TEXT = chatComp;
      USE_TEXT_COMPONENTS = useTextComponents;
      ClientboundSetTitlesAnimationPacket = animationCtor;
      ClientboundSetTitleTextPacket = titleCtor;
      ClientboundSetSubtitleTextPacket = subtitleCtor;
   }
}
