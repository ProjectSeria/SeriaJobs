package id.seria.gacha.lib.common.services.services.hologram;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeDisplayEntity;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Hologram {
   private final ServerPlugin plugin;
   private Location location;
   private final Map<UUID, PacketHologram> packetHolograms = new ConcurrentHashMap<>();
   private Hologram.HologramParser parser;
   private int despawnRange;
   private double lineGap;
   private FakeDisplayEntity.Billboard billboard;
   private int textOpacity;
   private boolean seeThrough;
   private boolean shadow;
   private float scale;
   private Color backgroundColor;
   private String[] lines;
   private Set<Player> ignoredPlayers = ConcurrentHashMap.newKeySet();
   private boolean spawned;

   public Hologram(ServerPlugin plugin) {
      this.parser = (player, raw) -> raw;
      this.despawnRange = 10;
      this.lines = new String[0];
      this.lineGap = 0.3;
      this.billboard = null;
      this.textOpacity = 255;
      this.seeThrough = false;
      this.shadow = true;
      this.scale = 1.0F;
      this.backgroundColor = new Color(0, 0, 0, 64);
      this.plugin = plugin;
   }

   public boolean spawn(Location location) {
      return this.spawn(location, true);
   }

   public boolean spawn(Location location, boolean show) {
      if (location.getWorld() == null) {
         return false;
      }

      if (this.spawned) {
         return false;
      }

      this.spawned = true;
      this.location = location;
      if (show) {
         location.getWorld().getPlayers().forEach(player -> this.show(player));
      }

      return true;
   }

   public void despawn() {
      if (this.spawned) {
         this.spawned = false;
         synchronized (this.packetHolograms) {
            this.packetHolograms.values().forEach(PacketHologram::despawn);
            this.packetHolograms.clear();
         }

         this.ignoredPlayers.clear();
      }
   }

   public void show(Player player) {
      if (!this.spawned) {
         throw new IllegalArgumentException("Can not show to player! Hologram not created!");
      }

      synchronized (this.packetHolograms) {
         if (!this.packetHolograms.containsKey(player.getUniqueId())) {
            this.ignoredPlayers.remove(player);
            PacketHologram packetHologram = this.preparePacketHologram(player);
            packetHologram.setLines(this.getParserdLines(player));
            packetHologram.spawn();
            this.packetHolograms.put(player.getUniqueId(), packetHologram);
         }
      }
   }

   public void hide(Player player) {
      if (!this.spawned) {
         throw new IllegalArgumentException("Can not hide to player! Hologram not created!");
      }

      synchronized (this.packetHolograms) {
         PacketHologram packetHologram = this.packetHolograms.remove(player.getUniqueId());
         if (packetHologram != null) {
            this.ignoredPlayers.add(player);
            packetHologram.despawn();
         }
      }
   }

   public void setLines(String... lines) {
      if (this.lines.hashCode() != lines.hashCode()) {
         this.lines = lines;
         this.refreshLines();
      }
   }

   public void setLines(Collection<String> lines) {
      this.setLines(lines.toArray(new String[0]));
   }

   public void addLine(int pos, String line) {
      this.lines = (String[])ArrayUtils.add(this.lines, pos, line);
      this.refreshLines();
   }

   public void addLine(String line) {
      this.addLine(this.lines.length, line);
   }

   public void teleport(Location location) {
      if (!this.location.equals(location)) {
         Location oldLocation = this.location.clone();
         this.location = location.clone();
         synchronized (this.packetHolograms) {
            if (!oldLocation.getWorld().equals(location.getWorld())) {
               for (PacketHologram holo : this.packetHolograms.values()) {
                  holo.despawn();
               }

               this.packetHolograms.clear();
               location.getWorld().getPlayers().forEach(player -> this.show(player));
            } else {
               for (PacketHologram holo : this.packetHolograms.values()) {
                  holo.teleport(location);
               }
            }
         }
      }
   }

   public void refreshLines() {
      synchronized (this.packetHolograms) {
         for (PacketHologram holo : this.packetHolograms.values()) {
            if (holo.getPlayer().isOnline()) {
               holo.setLines(this.getParserdLines(holo.getPlayer()));
            }
         }
      }
   }

   public void refreshStyle() {
      synchronized (this.packetHolograms) {
         for (PacketHologram holo : this.packetHolograms.values()) {
            if (holo.getPlayer().isOnline()) {
               holo.refresh();
            }
         }
      }
   }

   protected String[] getParserdLines(Player player) {
      return this.parser.accept(player, Arrays.copyOf(this.lines, this.lines.length));
   }

   protected PacketHologram preparePacketHologram(Player player) {
      return new ModernPacketHologram(this, player);
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public Location getLocation() {
      return this.location;
   }

   @Generated
   public Map<UUID, PacketHologram> getPacketHolograms() {
      return this.packetHolograms;
   }

   @Generated
   public Hologram.HologramParser getParser() {
      return this.parser;
   }

   @Generated
   public int getDespawnRange() {
      return this.despawnRange;
   }

   @Generated
   public double getLineGap() {
      return this.lineGap;
   }

   @Generated
   public FakeDisplayEntity.Billboard getBillboard() {
      return this.billboard;
   }

   @Generated
   public int getTextOpacity() {
      return this.textOpacity;
   }

   @Generated
   public boolean isSeeThrough() {
      return this.seeThrough;
   }

   @Generated
   public boolean isShadow() {
      return this.shadow;
   }

   @Generated
   public float getScale() {
      return this.scale;
   }

   @Generated
   public Color getBackgroundColor() {
      return this.backgroundColor;
   }

   @Generated
   public String[] getLines() {
      return this.lines;
   }

   @Generated
   public Set<Player> getIgnoredPlayers() {
      return this.ignoredPlayers;
   }

   @Generated
   public boolean isSpawned() {
      return this.spawned;
   }

   @Generated
   public void setLocation(Location location) {
      this.location = location;
   }

   @Generated
   public void setParser(Hologram.HologramParser parser) {
      this.parser = parser;
   }

   @Generated
   public void setDespawnRange(int despawnRange) {
      this.despawnRange = despawnRange;
   }

   @Generated
   public void setLineGap(double lineGap) {
      this.lineGap = lineGap;
   }

   @Generated
   public void setBillboard(FakeDisplayEntity.Billboard billboard) {
      this.billboard = billboard;
   }

   @Generated
   public void setTextOpacity(int textOpacity) {
      this.textOpacity = textOpacity;
   }

   @Generated
   public void setSeeThrough(boolean seeThrough) {
      this.seeThrough = seeThrough;
   }

   @Generated
   public void setShadow(boolean shadow) {
      this.shadow = shadow;
   }

   @Generated
   public void setScale(float scale) {
      this.scale = scale;
   }

   @Generated
   public void setBackgroundColor(Color backgroundColor) {
      this.backgroundColor = backgroundColor;
   }

   public interface HologramParser {
      String[] accept(Player var1, String[] var2);
   }
}
