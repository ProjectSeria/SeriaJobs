package id.seria.gacha.lib.common.nms.interfaces.packets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import lombok.Generated;
import org.bukkit.entity.Player;

public class FakePacketsBundle implements Iterable<FakePacket>, FakePacket {
   private final List<FakePacket> packets;

   public FakePacketsBundle() {
      this.packets = new ArrayList<>();
   }

   public FakePacketsBundle(List<FakePacket> packets) {
      this.packets = packets;
   }

   public FakePacketsBundle(FakePacket... packets) {
      this.packets = new ArrayList<>(Arrays.asList(packets));
   }

   public void addPacket(FakePacket packet) {
      synchronized (this.packets) {
         this.packets.add(packet);
      }
   }

   public void addPackets(FakePacket... packets) {
      synchronized (packets) {
         this.packets.addAll(Arrays.asList(packets));
      }
   }

   @Override
   public void sendPacket(Player player) {
      synchronized (this.packets) {
         for (FakePacket packet : this.packets) {
            packet.sendPacket(player);
         }
      }
   }

   @Override
   public Iterator<FakePacket> iterator() {
      return this.packets.iterator();
   }

   @Generated
   public List<FakePacket> getPackets() {
      return this.packets;
   }
}
