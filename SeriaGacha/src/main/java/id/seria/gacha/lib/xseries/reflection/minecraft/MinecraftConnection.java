package id.seria.gacha.lib.xseries.reflection.minecraft;

import id.seria.gacha.lib.xseries.reflection.XReflection;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MinecraftConnection {
   public static final MinecraftClassHandle ServerPlayer = XReflection.ofMinecraft()
      .inPackage(MinecraftPackage.NMS, "server.level")
      .map(MinecraftMapping.MOJANG, "ServerPlayer")
      .map(MinecraftMapping.SPIGOT, "EntityPlayer");
   public static final MinecraftClassHandle CraftPlayer = XReflection.ofMinecraft().inPackage(MinecraftPackage.CB, "entity").named("CraftPlayer");
   public static final MinecraftClassHandle ServerPlayerConnection = XReflection.ofMinecraft()
      .inPackage(MinecraftPackage.NMS, "server.network")
      .map(MinecraftMapping.MOJANG, "ServerPlayerConnection")
      .map(MinecraftMapping.SPIGOT, "PlayerConnection");
   public static final MinecraftClassHandle ServerGamePacketListenerImpl = XReflection.ofMinecraft()
      .inPackage(MinecraftPackage.NMS, "server.network")
      .map(MinecraftMapping.MOJANG, "ServerGamePacketListenerImpl")
      .map(MinecraftMapping.SPIGOT, "PlayerConnection");
   public static final MinecraftClassHandle Packet = XReflection.ofMinecraft()
      .inPackage(MinecraftPackage.NMS, "network.protocol")
      .map(MinecraftMapping.SPIGOT, "Packet");
   private static final MethodHandle PLAYER_CONNECTION = ServerPlayer.field()
      .getter()
      .returns(ServerGamePacketListenerImpl)
      .map(MinecraftMapping.MOJANG, "connection")
      .map(MinecraftMapping.OBFUSCATED, XReflection.v(21, 6, "g").v(21, 2, "f").v(20, "c").v(17, "b").orElse("playerConnection"))
      .unreflect();
   private static final MethodHandle GET_HANDLE = CraftPlayer.method().named("getHandle").returns(ServerPlayer).unreflect();
   private static final MethodHandle SEND_PACKET = ServerPlayerConnection.method()
      .returns(void.class)
      .parameters(Packet)
      .map(MinecraftMapping.MOJANG, "send")
      .map(MinecraftMapping.OBFUSCATED, XReflection.v(20, 2, "b").v(18, "a").orElse("sendPacket"))
      .unreflect();

   public MinecraftConnection() {
   }

   @NotNull
   public static Object getHandle(@NotNull Player player) {
      Objects.requireNonNull(player, "Cannot get handle of null player");

      try {
         return (Object)GET_HANDLE.invoke((Player)player);
      } catch (Throwable throwable) {
         throw XReflection.throwCheckedException(throwable);
      }
   }

   @Nullable
   public static Object getConnection(@NotNull Player player) {
      Objects.requireNonNull(player, "Cannot get connection of null player");

      try {
         Object handle = (Object)GET_HANDLE.invoke((Player)player);
         return (Object)PLAYER_CONNECTION.invoke((Object)handle);
      } catch (Throwable throwable) {
         throw XReflection.throwCheckedException(throwable);
      }
   }

   @NotNull
   public static void sendPacket(@NotNull Player player, @NotNull Object... packets) {
      Objects.requireNonNull(player, () -> "Can't send packet to null player: " + Arrays.toString(packets));
      Objects.requireNonNull(packets, () -> "Can't send null packets to player: " + player);

      try {
         Object handle = (Object)GET_HANDLE.invoke((Player)player);
         Object connection = (Object)PLAYER_CONNECTION.invoke((Object)handle);
         if (connection != null) {
            for (Object packet : packets) {
               Objects.requireNonNull(packet, "Null packet detected between packets array");
               SEND_PACKET.invoke((Object)connection, (Object)packet);
            }
         }
      } catch (Throwable throwable) {
         throw new IllegalStateException("Failed to send packet to " + player + ": " + Arrays.toString(packets), throwable);
      }
   }
}
