package id.seria.gacha.managers.crates.animations.opening.phases.second;

import com.google.common.collect.Lists;
import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.objects.ComplexMaterial;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.lib.xseries.XSound;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

public class CSGOGuiAnimationConfiguration extends Configuration {
   @ConfigField(path = "title")
   private String title = "&8&lOpening...";
   @ConfigField(path = "sounds.start")
   private XSound startSound = XSound.BLOCK_NOTE_BLOCK_HAT;
   @ConfigField(path = "sounds.tick")
   private XSound tickSound = XSound.UI_BUTTON_CLICK;
   @ConfigField(path = "sounds.win-chime")
   private XSound winChimeSound = XSound.BLOCK_NOTE_BLOCK_HARP;
   @ConfigField(path = "sounds.win")
   private XSound winSound = XSound.ENTITY_PLAYER_LEVELUP;
   @ConfigField(path = "filler")
   private CSGOGuiAnimationConfiguration.ItemAppearance fillerItem = new CSGOGuiAnimationConfiguration.ItemAppearance(
      new ComplexMaterial(XMaterial.BLACK_STAINED_GLASS_PANE), 1, 0, "", false, false, "&r", Lists.newArrayList(), true
   );
   @ConfigField(path = "marker.normal")
   private CSGOGuiAnimationConfiguration.ItemAppearance markerNormalItem = new CSGOGuiAnimationConfiguration.ItemAppearance(
      new ComplexMaterial(XMaterial.YELLOW_STAINED_GLASS_PANE), 1, 0, "", false, false, "&r", Lists.newArrayList(), false
   );
   @ConfigField(path = "marker.flicker")
   private CSGOGuiAnimationConfiguration.ItemAppearance markerFlickerItem = new CSGOGuiAnimationConfiguration.ItemAppearance(
      new ComplexMaterial(XMaterial.ORANGE_STAINED_GLASS_PANE), 1, 0, "", false, false, "&r", Lists.newArrayList(), false
   );
   @ConfigField(path = "reward-item.chance-lore")
   private List<String> chanceLore = Lists.newArrayList(new String[]{"", "&eYour chance: &6%chance%%"});

   public CSGOGuiAnimationConfiguration() {
   }

   public CSGOGuiAnimationConfiguration clone() {
      return new CSGOGuiAnimationConfiguration();
   }

   @Generated
   public String getTitle() {
      return this.title;
   }

   @Generated
   public XSound getStartSound() {
      return this.startSound;
   }

   @Generated
   public XSound getTickSound() {
      return this.tickSound;
   }

   @Generated
   public XSound getWinChimeSound() {
      return this.winChimeSound;
   }

   @Generated
   public XSound getWinSound() {
      return this.winSound;
   }

   @Generated
   public CSGOGuiAnimationConfiguration.ItemAppearance getFillerItem() {
      return this.fillerItem;
   }

   @Generated
   public CSGOGuiAnimationConfiguration.ItemAppearance getMarkerNormalItem() {
      return this.markerNormalItem;
   }

   @Generated
   public CSGOGuiAnimationConfiguration.ItemAppearance getMarkerFlickerItem() {
      return this.markerFlickerItem;
   }

   @Generated
   public List<String> getChanceLore() {
      return this.chanceLore;
   }

   public static class ItemAppearance implements Cloneable {
      @ConfigField
      private ComplexMaterial material = new ComplexMaterial(XMaterial.ENDER_PEARL);
      @ConfigField
      private int amount = 1;
      @ConfigField(path = "custom-model-data")
      private int customModelData = 0;
      @ConfigField(path = "item-model")
      private String itemModel = "";
      @ConfigField
      private boolean glow = false;
      @ConfigField(path = "hide-attributes")
      private boolean hideAttributes = false;
      @ConfigField(path = "display-name")
      private String displayName = "&r";
      @ConfigField
      private List<String> lore = Lists.newArrayList();
      @ConfigField(path = "hide-tooltip")
      private boolean hideTooltip = false;

      public CSGOGuiAnimationConfiguration.ItemAppearance clone() {
         return new CSGOGuiAnimationConfiguration.ItemAppearance(
            this.material == null ? null : this.material.clone(),
            this.amount,
            this.customModelData,
            this.itemModel,
            this.glow,
            this.hideAttributes,
            this.displayName,
            new ArrayList<>(this.lore),
            this.hideTooltip
         );
      }

      @Generated
      public void setMaterial(ComplexMaterial material) {
         this.material = material;
      }

      @Generated
      public void setAmount(int amount) {
         this.amount = amount;
      }

      @Generated
      public void setCustomModelData(int customModelData) {
         this.customModelData = customModelData;
      }

      @Generated
      public void setItemModel(String itemModel) {
         this.itemModel = itemModel;
      }

      @Generated
      public void setGlow(boolean glow) {
         this.glow = glow;
      }

      @Generated
      public void setHideAttributes(boolean hideAttributes) {
         this.hideAttributes = hideAttributes;
      }

      @Generated
      public void setDisplayName(String displayName) {
         this.displayName = displayName;
      }

      @Generated
      public void setLore(List<String> lore) {
         this.lore = lore;
      }

      @Generated
      public void setHideTooltip(boolean hideTooltip) {
         this.hideTooltip = hideTooltip;
      }

      @Generated
      public ComplexMaterial getMaterial() {
         return this.material;
      }

      @Generated
      public int getAmount() {
         return this.amount;
      }

      @Generated
      public int getCustomModelData() {
         return this.customModelData;
      }

      @Generated
      public String getItemModel() {
         return this.itemModel;
      }

      @Generated
      public boolean isGlow() {
         return this.glow;
      }

      @Generated
      public boolean isHideAttributes() {
         return this.hideAttributes;
      }

      @Generated
      public String getDisplayName() {
         return this.displayName;
      }

      @Generated
      public List<String> getLore() {
         return this.lore;
      }

      @Generated
      public boolean isHideTooltip() {
         return this.hideTooltip;
      }

      @Generated
      public ItemAppearance() {
      }

      @Generated
      public ItemAppearance(
         ComplexMaterial material,
         int amount,
         int customModelData,
         String itemModel,
         boolean glow,
         boolean hideAttributes,
         String displayName,
         List<String> lore,
         boolean hideTooltip
      ) {
         this.material = material;
         this.amount = amount;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.hideAttributes = hideAttributes;
         this.displayName = displayName;
         this.lore = lore;
         this.hideTooltip = hideTooltip;
      }
   }
}
