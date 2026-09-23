package id.seria.gacha.managers.crates;

import id.seria.gacha.api.internal.Item;
import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeArmorStandEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeItemEntity;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.BukkitUtil;
import id.seria.gacha.limbo.Operations;
import id.seria.gacha.managers.reward.CustomReward;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PreviewItem {
   private final Crate crate;
   private FakeArmorStandEntity holderEntity;
   private FakeItemEntity itemEntity;
   private int previewRewardCurrentIndex;
   private boolean loaded;

   public PreviewItem(Crate crate) {
      this.crate = crate;
   }

   public void load() {
      this.unload();
      this.loaded = true;
      this.createComponents();
   }

   private void createComponents() {
      Location location = this.getPreviewRewardLocation();
      this.holderEntity = Utilities.getNMSFactory().createFakeArmorStand(this.crate.getPlugin().getOriginal(), location);
      this.holderEntity.setGravity(false);
      this.holderEntity.setInvisible(true);
      this.holderEntity.setMarker(true);
      this.itemEntity = Utilities.getNMSFactory().createFakeItem(this.crate.getPlugin().getOriginal(), location);
      this.itemEntity.setPickupDelay(32767);
      this.holderEntity.setPassenger(this.itemEntity);
      if (!this.crate.getType().getAvailableRewards().isEmpty()) {
         this.holderEntity.setBeingWatched(true);
         this.itemEntity.setBeingWatched(true);
         this.updatePreviewRewardItem();
         this.updateAudience();
      }
   }

   public void unload() {
      this.loaded = false;
      if (this.itemEntity != null) {
         this.itemEntity.setAudience(new ArrayList<>());
         this.itemEntity.setBeingWatched(false);
         this.itemEntity = null;
      }

      if (this.holderEntity != null) {
         this.holderEntity.setAudience(new ArrayList<>());
         this.holderEntity.setBeingWatched(false);
         this.holderEntity = null;
      }
   }

   public void tick() {
      if (this.loaded) {
         if (this.crate.delta % 20 == 0) {
            this.updateAudience();
         }

         if (this.crate.delta % 40 == 0) {
            this.previewRewardCurrentIndex = this.crate.getType().getAvailableRewards().isEmpty()
               ? 0
               : (this.previewRewardCurrentIndex + 1) % this.crate.getType().getAvailableRewards().size();
            this.updatePreviewRewardItem();
         }
      }
   }

   private void updateAudience() {
      List<Player> audience = new ArrayList<>(this.crate.getNearbyPlayers());
      audience.removeAll(this.crate.getIdleAnimation().getIgnoredPlayers());
      if (this.holderEntity != null) {
         this.holderEntity.setAudience(audience);
      }

      if (this.itemEntity != null) {
         this.itemEntity.setAudience(audience);
         this.holderEntity.setPassenger(this.itemEntity);
         this.holderEntity.prepareSetPassengersPacket().sendPacket(audience);
      }
   }

   private void updatePreviewRewardItem() {
      if (this.itemEntity != null) {
         if (this.crate.getType().getAvailableRewards().isEmpty()) {
            this.itemEntity.setCustomNameVisible(false);
            this.itemEntity.setAudience(new ArrayList<>());
            this.holderEntity.setAudience(new ArrayList<>());
         } else {
            CustomReward reward = this.crate
               .getType()
               .getAvailableRewards()
               .get(this.previewRewardCurrentIndex % this.crate.getType().getAvailableRewards().size());
            if (!reward.isDisplayValid()) {
               this.itemEntity.setCustomNameVisible(false);
               this.itemEntity.setAudience(new ArrayList<>());
               this.holderEntity.setAudience(new ArrayList<>());
            } else {
               ItemStack displayItem = Operations.o(reward.getDisplayItem()).map(Item::build).orElse(CustomReward.INVALID_REWARD);
               this.itemEntity.setItemStack(displayItem);
               String displayName = ItemBuilder.of(displayItem).getDisplayName();
               if (ChatColor.stripColor(displayName).isEmpty()) {
                  this.itemEntity.setCustomNameVisible(false);
               } else {
                  this.itemEntity
                     .setCustomName(
                        Placeholders.setPlaceholders(
                           Translations.t("crate.reward-got", "%amount%", displayItem.getAmount(), "%reward%", BukkitUtil.translateColorCodes(displayName))[0]
                        )
                     );
                  this.itemEntity.setCustomNameVisible(true);
               }

               this.itemEntity.prepareMetadataPacket().sendPacket(this.itemEntity.getAudience());
               this.itemEntity.setMetadataChanged(false);
            }
         }
      }
   }

   public Location getPreviewRewardLocation() {
      return this.crate
         .getLocation()
         .clone()
         .add(0.5, this.crate.getDisplayComponent().getDisplayHeight() + 0.1 + this.crate.getType().getHologramOffset(), 0.5);
   }

   @Generated
   public Crate getCrate() {
      return this.crate;
   }

   @Generated
   public FakeArmorStandEntity getHolderEntity() {
      return this.holderEntity;
   }

   @Generated
   public FakeItemEntity getItemEntity() {
      return this.itemEntity;
   }

   @Generated
   public int getPreviewRewardCurrentIndex() {
      return this.previewRewardCurrentIndex;
   }

   @Generated
   public boolean isLoaded() {
      return this.loaded;
   }
}
