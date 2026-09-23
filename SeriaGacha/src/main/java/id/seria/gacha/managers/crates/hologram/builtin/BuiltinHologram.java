package id.seria.gacha.managers.crates.hologram.builtin;

import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.facades.KeyFacade;
import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.services.Services;
import id.seria.gacha.lib.common.services.services.hologram.Hologram;
import id.seria.gacha.lib.common.utils.utility.TimeUtil;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.hologram.CrateHologram;
import id.seria.gacha.managers.crates.hologram.HologramProperties;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.managers.players.PlayerData;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BuiltinHologram extends CrateHologram {
   private Hologram hologram;
   private List<String> rawLines;

   public BuiltinHologram(Crate crate) {
      super(crate);
      this.rawLines = crate.getType().getHologramProperties().get("lines", List.class);
      if (this.rawLines == null) {
         this.rawLines = new ArrayList<>();
      }

      this.initHologram();
   }

   private void initHologram() {
      this.hologram = new Hologram(PhoenixCrates.getLegacyInstance());
      this.hologram.spawn(this.crate.getHologramLocation());
      this.applyStyle();
      this.hologram.setDespawnRange(Crates.getConfiguration().getCratesRenderRadius());
      this.hologram.setLines(Translations.r(this.rawLines, "%name%", this.crate.getType().getFormattedDisplayName(null)));
      this.hologram
         .setParser(
            (viewer, lines) -> {
               if (lines.length == 0) {
                  return lines;
               }

               String displayCooldown;
               if (this.crate.getType().getOpenCooldownSeconds() == 0) {
                  displayCooldown = Translations.t("labels.no-cooldown")[0];
               } else {
                  PlayerData playerRecord = Crates.getPlayersManager().getPlayerIfCached(viewer, false);
                  if (playerRecord == null) {
                     displayCooldown = Translations.t("crate.cooldown-display.counting", "%cooldown%", "?")[0];
                  } else {
                     long currentCooldown = playerRecord.getCrateCooldown(this.crate.getType().getIdentifier())
                        + (this.crate.getType().getOpenCooldownSeconds() + 1) * 1000L;
                     long leftMillis = Math.max(0L, currentCooldown - System.currentTimeMillis());
                     int seconds = (int)(leftMillis / 1000.0);
                     if (seconds == 0) {
                        displayCooldown = Translations.t("crate.cooldown-display.over")[0];
                     } else {
                        displayCooldown = Translations.t("crate.cooldown-display.counting", "%cooldown%", TimeUtil.formatSecondsShort(seconds))[0];
                     }
                  }
               }

               PlayerData playerRecord = Crates.getPlayersManager().getPlayerIfCached(viewer, false);
               int virtualKeysCount = 0;
               if (playerRecord != null) {
                  for (Key key : KeyFacade.getCrateKeys(this.crate.getType())) {
                     if (key.isVirtual()) {
                        virtualKeysCount += playerRecord.getVirtualKeys(key.getIdentifier());
                     }
                  }
               }

               return Placeholders.setPlaceholders(
                  Translations.r(lines, "%cooldown%", displayCooldown, "%virtual_keys%", playerRecord == null ? "?" : virtualKeysCount), viewer
               );
            }
         );
      Services.getHologramService().addHologram(this.hologram);
   }

   @Override
   public void applyStyle() {
      if (this.hologram != null) {
         HologramProperties properties = this.crate.getType().getHologramProperties();
         this.hologram.setLineGap(properties.getLineGap());
         this.hologram.setBillboard(properties.getBillboard());
         this.hologram.setTextOpacity(properties.getTextOpacity());
         this.hologram.setSeeThrough(properties.isSeeThrough());
         this.hologram.setShadow(properties.isShadow());
         this.hologram.setBackgroundColor(properties.getBackgroundColor());
         this.hologram.setScale((float)properties.getScale());
         this.hologram.refreshStyle();
      }
   }

   @Override
   public void show(Player player) {
      if (this.hologram != null) {
         this.hologram.show(player);
      }
   }

   @Override
   public void hide(Player player) {
      if (this.hologram != null) {
         this.hologram.hide(player);
      }
   }

   @Override
   public void moveTo(Location location) {
      if (this.hologram != null) {
         this.hologram.teleport(location);
      }
   }

   @Override
   public void refresh() {
      if (this.hologram != null) {
         this.hologram.refreshLines();
      }
   }

   @Override
   public void despawn() {
      if (this.hologram != null) {
         this.hologram.despawn();
         Services.getHologramService().removeHologram(this.hologram);
      }
   }
}
