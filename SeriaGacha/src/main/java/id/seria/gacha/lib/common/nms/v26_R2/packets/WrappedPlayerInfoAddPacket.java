package id.seria.gacha.lib.common.nms.v26_R2.packets;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumSet;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import sun.misc.Unsafe;

public class WrappedPlayerInfoAddPacket extends WrappedPacket {
   private static final Unsafe UNSAFE;
   private static final Field ACTIONS_FIELD;
   private static final Field ENTRIES_FIELD;
   private final Packet<?> packet;

   public WrappedPlayerInfoAddPacket(ServerPlayer player) {
      GameProfile profile = player.getGameProfile();
      Entry entry = new Entry(profile.getId(), profile, false, 0, GameType.SURVIVAL, null, true, 0, null);

      try {
         ClientboundPlayerInfoUpdatePacket instance = (ClientboundPlayerInfoUpdatePacket)UNSAFE.allocateInstance(ClientboundPlayerInfoUpdatePacket.class);
         ACTIONS_FIELD.set(instance, EnumSet.of(Action.ADD_PLAYER));
         ENTRIES_FIELD.set(instance, Collections.singletonList(entry));
         this.packet = instance;
      } catch (ReflectiveOperationException e) {
         throw new IllegalStateException("Failed to build player info add packet", e);
      }
   }

   @Generated
   @Override
   public Packet<?> getPacket() {
      return this.packet;
   }

   static {
      try {
         Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
         unsafeField.setAccessible(true);
         UNSAFE = (Unsafe)unsafeField.get(null);
         ACTIONS_FIELD = ClientboundPlayerInfoUpdatePacket.class.getDeclaredField("actions");
         ACTIONS_FIELD.setAccessible(true);
         ENTRIES_FIELD = ClientboundPlayerInfoUpdatePacket.class.getDeclaredField("entries");
         ENTRIES_FIELD.setAccessible(true);
      } catch (ReflectiveOperationException e) {
         throw new ExceptionInInitializerError(e);
      }
   }
}
