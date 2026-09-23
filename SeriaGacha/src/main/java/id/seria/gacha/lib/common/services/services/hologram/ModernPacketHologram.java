package id.seria.gacha.lib.common.services.services.hologram;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeTextDisplayEntity;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.sdk.platforms.server.internal.RichTextConverter;
import id.seria.gacha.sdk.platforms.server.internal.ServerUtilities;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ModernPacketHologram implements PacketHologram {
   private final Hologram hologram;
   private final Player player;
   private Location location;
   private final List<FakeTextDisplayEntity> textDisplays = new CopyOnWriteArrayList<>();
   private final List<String> lines = new CopyOnWriteArrayList<>();
   private boolean spawned;

   protected ModernPacketHologram(Hologram hologram, Player player) {
      this.hologram = hologram;
      this.player = player;
      this.location = hologram.getLocation().clone();
   }

   @Override
   public void setLines(String... newLines) {
      this.setLines(Arrays.asList(newLines));
   }

   @Override
   public void setLines(List<String> newLines) {
      if (this.lines.hashCode() != newLines.hashCode()) {
         synchronized (this.textDisplays) {
            this.lines.clear();
            this.lines.addAll(newLines);
         }

         if (this.spawned) {
            this.executeOnEntityThread(() -> {
               if (this.spawned) {
                  int neededEntities = this.countNeededTextDisplays(this.lines);
                  if (this.textDisplays.size() == neededEntities) {
                     this.updateInternal();
                  } else {
                     this.despawnInternal();
                     this.spawnInternal();
                  }
               }
            });
         }
      }
   }

   @Override
   public void spawn() {
      this.executeOnEntityThread(this::spawnInternal);
   }

   private void spawnInternal() {
      if (!this.spawned && this.canViewerSee()) {
         this.spawned = true;
         synchronized (this.textDisplays) {
            this.textDisplays.clear();
            Location temp = this.location.clone();
            float scale = this.hologram.getScale();

            for (int i = this.lines.size() - 1; i >= 0; i--) {
               String line = this.lines.get(i);
               if (line.length() > 0) {
                  FakeTextDisplayEntity textDisplay = (FakeTextDisplayEntity)Utilities.getNMSFactory()
                     .createFakeEntity(this.hologram.getPlugin(), FakeEntityType.TEXT_DISPLAY, temp);
                  this.applyText(textDisplay, line);
                  if (this.hologram.getBillboard() != null) {
                     textDisplay.setBillboard(this.hologram.getBillboard());
                  }

                  textDisplay.setTextOpacity((byte)this.hologram.getTextOpacity());
                  textDisplay.setSeeThrough(this.hologram.isSeeThrough());
                  textDisplay.setShadowed(this.hologram.isShadow());
                  textDisplay.setScale(scale);
                  if (this.hologram.getBackgroundColor() != null) {
                     textDisplay.setBackgroundColor(this.hologram.getBackgroundColor());
                  }

                  textDisplay.prepareSpawnPacket().sendPacket(this.player);
                  textDisplay.prepareMetadataPacket().sendPacket(this.player);
                  this.textDisplays.add(textDisplay);
               }

               if (i > 0) {
                  double offset = (line.length() == 0 ? 0.13 : this.hologram.getLineGap() * countRows(line)) * scale;
                  temp.add(0.0, offset, 0.0);
               }
            }
         }
      }
   }

   @Override
   public void teleport(Location newLocation) {
      Location target = newLocation.clone();
      this.executeOnEntityThread(() -> this.teleportInternal(target));
   }

   private void teleportInternal(Location newLocation) {
      if (!this.spawned) {
         this.location = newLocation.clone();
         if (this.canViewerSee(newLocation)) {
            this.spawnInternal();
         }
      } else {
         synchronized (this.textDisplays) {
            for (FakeTextDisplayEntity textDisplay : this.textDisplays) {
               Location diff = textDisplay.getLocation().subtract(this.location);
               Location displayLoc = newLocation.clone().add(diff);
               textDisplay.setPosition(displayLoc.getX(), displayLoc.getY(), displayLoc.getZ());
               textDisplay.prepareTeleportPacket().sendPacket(this.player);
            }
         }

         this.location = newLocation.clone();
      }
   }

   @Override
   public void despawn() {
      this.executeOnEntityThread(this::despawnInternal);
   }

   private void despawnInternal() {
      if (this.spawned) {
         this.spawned = false;
         synchronized (this.textDisplays) {
            for (FakeTextDisplayEntity textDisplay : this.textDisplays) {
               textDisplay.prepareDestroyPacket().sendPacket(this.player);
            }

            this.textDisplays.clear();
         }
      }
   }

   @Override
   public void refresh() {
      this.executeOnEntityThread(this::updateInternal);
   }

   private void updateInternal() {
      if (this.spawned) {
         synchronized (this.textDisplays) {
            if (this.countNeededTextDisplays(this.lines) != this.textDisplays.size()) {
               this.despawnInternal();
               this.spawnInternal();
            } else {
               float scale = this.hologram.getScale();
               double[] heightAboveBase = new double[this.lines.size()];
               double cumulative = 0.0;

               for (int i = this.lines.size() - 1; i >= 0; i--) {
                  heightAboveBase[i] = cumulative;
                  if (i > 0) {
                     String line = this.lines.get(i);
                     cumulative += (line.length() == 0 ? 0.13 : this.hologram.getLineGap() * countRows(line)) * scale;
                  }
               }

               int t = 0;

               for (int i = 0; i < this.lines.size(); i++) {
                  String line = this.lines.get(i);
                  if (line.length() > 0) {
                     FakeTextDisplayEntity textDisplay = this.textDisplays.get(t);
                     Location pos = this.location.clone().add(0.0, heightAboveBase[i], 0.0);
                     this.applyText(textDisplay, line);
                     textDisplay.setPosition(pos.getX(), pos.getY(), pos.getZ());
                     if (this.hologram.getBillboard() != null) {
                        textDisplay.setBillboard(this.hologram.getBillboard());
                     }

                     textDisplay.setTextOpacity((byte)this.hologram.getTextOpacity());
                     textDisplay.setSeeThrough(this.hologram.isSeeThrough());
                     textDisplay.setShadowed(this.hologram.isShadow());
                     if (this.hologram.getBackgroundColor() != null) {
                        textDisplay.setBackgroundColor(this.hologram.getBackgroundColor());
                     }

                     textDisplay.prepareMetadataPacket().sendPacket(this.player);
                     textDisplay.prepareTeleportPacket().sendPacket(this.player);
                     t++;
                  }
               }
            }
         }
      }
   }

   private void applyText(FakeTextDisplayEntity textDisplay, String line) {
      RichTextConverter.applyRichText(textDisplay.getBukkitEntity(), line);
      textDisplay.setMetadataChanged(true);
   }

   private void executeOnEntityThread(Runnable runnable) {
      if (ServerUtilities.isFoliaServer() && this.hologram.getPlugin().isEnabled()) {
         Location schedulerLocation = this.location;
         if (schedulerLocation != null && schedulerLocation.getWorld() != null) {
            this.hologram.getPlugin().getScheduler().runSync(schedulerLocation.clone(), runnable);
         }
      } else {
         runnable.run();
      }
   }

   private static int countRows(String line) {
      int rows = 1;

      for (int i = 0; i < line.length(); i++) {
         if (line.charAt(i) == '\n') {
            rows++;
         }
      }

      return rows;
   }

   private int countNeededTextDisplays(List<String> lines) {
      int amount = 0;

      for (String line : lines) {
         if (line.length() > 0) {
            amount++;
         }
      }

      return amount;
   }

   @Override
   public boolean canViewerSee() {
      return this.canViewerSee(this.location);
   }

   private boolean canViewerSee(Location location) {
      return location.getWorld().equals(this.player.getWorld()) && this.player.getLocation().distance(location) <= this.hologram.getDespawnRange();
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ModernPacketHologram)) {
         return false;
      }

      ModernPacketHologram casted = (ModernPacketHologram)obj;
      return this.player.getUniqueId().equals(casted.player.getUniqueId()) && this.location.equals(casted.location);
   }

   @Generated
   @Override
   public Hologram getHologram() {
      return this.hologram;
   }

   @Generated
   @Override
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   @Override
   public Location getLocation() {
      return this.location;
   }

   @Generated
   public List<FakeTextDisplayEntity> getTextDisplays() {
      return this.textDisplays;
   }

   @Generated
   public List<String> getLines() {
      return this.lines;
   }

   @Generated
   public boolean isSpawned() {
      return this.spawned;
   }
}
