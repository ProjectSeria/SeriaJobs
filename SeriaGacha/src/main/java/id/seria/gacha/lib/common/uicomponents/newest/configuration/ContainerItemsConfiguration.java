package id.seria.gacha.lib.common.uicomponents.newest.configuration;

import com.google.common.collect.Lists;
import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.objects.ComplexMaterial;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Generated;

public abstract class ContainerItemsConfiguration extends Configuration {
   @ConfigField
   protected LinkedHashMap<String, ContainerItemsConfiguration.ItemConfiguration> items = new LinkedHashMap<>();

   public ContainerItemsConfiguration() {
   }

   @Generated
   public void setItems(LinkedHashMap<String, ContainerItemsConfiguration.ItemConfiguration> items) {
      this.items = items;
   }

   @Generated
   public LinkedHashMap<String, ContainerItemsConfiguration.ItemConfiguration> getItems() {
      return this.items;
   }

   public static class ItemConfiguration implements Cloneable {
      @ConfigField
      protected SlotCompound slot = new SlotCompound(0, 0);
      @ConfigField
      protected ComplexMaterial material = new ComplexMaterial(XMaterial.ENDER_PEARL);
      @ConfigField
      protected int amount = 1;
      @ConfigField
      protected int customModelData = 0;
      @ConfigField
      protected String itemModel = null;
      @ConfigField
      protected boolean glow = true;
      @ConfigField
      protected boolean hideAttributes = false;
      @ConfigField
      protected String displayName = "&aCustom Item";
      @ConfigField
      protected List<String> lore = Lists.newArrayList(
         new String[]{"&7This is a custom item", "&7click here to see something.", "", "&6Left-Click: &eSee something."}
      );
      @ConfigField
      protected List<String> actions = Lists.newArrayList(
         new String[]{
            "[CLOSE_INVENTORY]",
            "[MESSAGE] &7This menu through actions closed after you clicked on the item.",
            "[TITLE] &aCustom Action;&7This is a [TITLE] action.;10;20;10"
         }
      );
      @ConfigField
      protected String tooltipStyle = null;
      @ConfigField
      protected Boolean hideTooltip = null;
      @ConfigField
      protected Boolean enchantmentGlintOverride = null;
      @ConfigField
      protected List<String> bannerMeta = null;
      @ConfigField
      protected String trimMaterial = null;
      @ConfigField
      protected String trimPattern = null;
      @ConfigField
      protected List<String> potionEffects = null;
      @ConfigField
      protected String rgb = null;

      public ItemConfiguration(
         SlotCompound slot,
         ComplexMaterial material,
         int amount,
         int customModelData,
         String itemModel,
         boolean glow,
         String displayName,
         List<String> lore,
         List<String> actions
      ) {
         this.slot = slot;
         this.material = material;
         this.amount = amount;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.displayName = displayName;
         this.lore = lore;
         this.actions = actions;
      }

      @Deprecated
      public ItemConfiguration(
         SlotCompound slot,
         ComplexMaterial material,
         int amount,
         int customModelData,
         boolean glow,
         String displayName,
         List<String> lore,
         List<String> actions
      ) {
         this(slot, material, amount, customModelData, null, glow, displayName, lore, actions);
      }

      public ItemConfiguration(
         SlotCompound slot,
         ComplexMaterial material,
         int amount,
         int customModelData,
         String itemModel,
         boolean glow,
         boolean hideAttributes,
         String displayName,
         List<String> lore,
         List<String> actions
      ) {
         this.slot = slot;
         this.material = material;
         this.amount = amount;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.hideAttributes = hideAttributes;
         this.displayName = displayName;
         this.lore = lore;
         this.actions = actions;
      }

      public ContainerItemsConfiguration.ItemConfiguration clone() {
         return new ContainerItemsConfiguration.ItemConfiguration(
            this.slot.clone(),
            this.material == null ? null : this.material.clone(),
            this.amount,
            this.customModelData,
            this.itemModel,
            this.glow,
            this.hideAttributes,
            this.displayName,
            this.lore == null ? null : new ArrayList<>(this.lore),
            this.actions == null ? null : new ArrayList<>(this.actions),
            this.tooltipStyle,
            this.hideTooltip,
            this.enchantmentGlintOverride,
            this.bannerMeta == null ? null : new ArrayList<>(this.bannerMeta),
            this.trimMaterial,
            this.trimPattern,
            this.potionEffects == null ? null : new ArrayList<>(this.potionEffects),
            this.rgb
         );
      }

      @Generated
      public void setSlot(SlotCompound slot) {
         this.slot = slot;
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
      public void setActions(List<String> actions) {
         this.actions = actions;
      }

      @Generated
      public void setTooltipStyle(String tooltipStyle) {
         this.tooltipStyle = tooltipStyle;
      }

      @Generated
      public void setHideTooltip(Boolean hideTooltip) {
         this.hideTooltip = hideTooltip;
      }

      @Generated
      public void setEnchantmentGlintOverride(Boolean enchantmentGlintOverride) {
         this.enchantmentGlintOverride = enchantmentGlintOverride;
      }

      @Generated
      public void setBannerMeta(List<String> bannerMeta) {
         this.bannerMeta = bannerMeta;
      }

      @Generated
      public void setTrimMaterial(String trimMaterial) {
         this.trimMaterial = trimMaterial;
      }

      @Generated
      public void setTrimPattern(String trimPattern) {
         this.trimPattern = trimPattern;
      }

      @Generated
      public void setPotionEffects(List<String> potionEffects) {
         this.potionEffects = potionEffects;
      }

      @Generated
      public void setRgb(String rgb) {
         this.rgb = rgb;
      }

      @Generated
      public SlotCompound getSlot() {
         return this.slot;
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
      public List<String> getActions() {
         return this.actions;
      }

      @Generated
      public String getTooltipStyle() {
         return this.tooltipStyle;
      }

      @Generated
      public Boolean getHideTooltip() {
         return this.hideTooltip;
      }

      @Generated
      public Boolean getEnchantmentGlintOverride() {
         return this.enchantmentGlintOverride;
      }

      @Generated
      public List<String> getBannerMeta() {
         return this.bannerMeta;
      }

      @Generated
      public String getTrimMaterial() {
         return this.trimMaterial;
      }

      @Generated
      public String getTrimPattern() {
         return this.trimPattern;
      }

      @Generated
      public List<String> getPotionEffects() {
         return this.potionEffects;
      }

      @Generated
      public String getRgb() {
         return this.rgb;
      }

      @Generated
      public ItemConfiguration() {
      }

      @Generated
      public ItemConfiguration(
         SlotCompound slot,
         ComplexMaterial material,
         int amount,
         int customModelData,
         String itemModel,
         boolean glow,
         boolean hideAttributes,
         String displayName,
         List<String> lore,
         List<String> actions,
         String tooltipStyle,
         Boolean hideTooltip,
         Boolean enchantmentGlintOverride,
         List<String> bannerMeta,
         String trimMaterial,
         String trimPattern,
         List<String> potionEffects,
         String rgb
      ) {
         this.slot = slot;
         this.material = material;
         this.amount = amount;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.hideAttributes = hideAttributes;
         this.displayName = displayName;
         this.lore = lore;
         this.actions = actions;
         this.tooltipStyle = tooltipStyle;
         this.hideTooltip = hideTooltip;
         this.enchantmentGlintOverride = enchantmentGlintOverride;
         this.bannerMeta = bannerMeta;
         this.trimMaterial = trimMaterial;
         this.trimPattern = trimPattern;
         this.potionEffects = potionEffects;
         this.rgb = rgb;
      }
   }

   public static class ItemStateConfiguration implements Cloneable {
      @ConfigField
      protected SlotCompound slot = new SlotCompound();
      @ConfigField(path = "states.on.material")
      protected ComplexMaterial stateOnMaterial = new ComplexMaterial(XMaterial.ENDER_PEARL);
      @ConfigField(path = "states.on.custom-model-data")
      protected int stateOnCustomModelData = 0;
      @ConfigField(path = "states.on.item-model")
      protected String stateOnItemModel = null;
      @ConfigField(path = "states.on.glow")
      protected boolean stateOnGlow = false;
      @ConfigField(path = "states.off.material")
      protected ComplexMaterial stateOffMaterial = new ComplexMaterial(XMaterial.ENDER_PEARL);
      @ConfigField(path = "states.off.custom-model-data")
      protected int stateOffCustomModelData = 0;
      @ConfigField(path = "states.off.item-model")
      protected String stateOffItemModel = null;
      @ConfigField(path = "states.off.glow")
      protected boolean stateOffGlow = false;
      @ConfigField
      protected String displayName = "&aCustom Display Name";
      @ConfigField
      protected List<String> lore = Lists.newArrayList(new String[]{"&7This is a custom lore"});
      @ConfigField
      protected List<String> actions = Lists.newArrayList(new String[]{"[MESSAGE] &7This is a custom action"});

      @Deprecated
      public ItemStateConfiguration(
         SlotCompound slot,
         ComplexMaterial stateOnMaterial,
         int stateOnCustomModelData,
         boolean stateOnGlow,
         ComplexMaterial stateOffMaterial,
         int stateOffCustomModelData,
         boolean stateOffGlow,
         String displayName,
         List<String> lore,
         List<String> actions
      ) {
         this(
            slot,
            stateOnMaterial,
            stateOnCustomModelData,
            null,
            stateOnGlow,
            stateOffMaterial,
            stateOffCustomModelData,
            null,
            stateOffGlow,
            displayName,
            lore,
            actions
         );
      }

      public ContainerItemsConfiguration.ItemStateConfiguration clone() {
         return new ContainerItemsConfiguration.ItemStateConfiguration(
            this.slot,
            this.stateOnMaterial == null ? null : this.stateOnMaterial.clone(),
            this.stateOnCustomModelData,
            this.stateOnItemModel,
            this.stateOnGlow,
            this.stateOffMaterial == null ? null : this.stateOffMaterial.clone(),
            this.stateOffCustomModelData,
            this.stateOffItemModel,
            this.stateOffGlow,
            this.displayName,
            new ArrayList<>(this.lore),
            new ArrayList<>(this.actions)
         );
      }

      public ContainerItemsConfiguration.ItemConfiguration as(boolean state) {
         return state
            ? new ContainerItemsConfiguration.ItemConfiguration(
               this.slot.clone(),
               this.stateOnMaterial == null ? null : this.stateOnMaterial.clone(),
               1,
               this.stateOnCustomModelData,
               this.stateOnItemModel,
               this.stateOnGlow,
               this.displayName,
               new ArrayList<>(this.lore),
               new ArrayList<>(this.actions)
            )
            : new ContainerItemsConfiguration.ItemConfiguration(
               this.slot.clone(),
               this.stateOffMaterial == null ? null : this.stateOffMaterial.clone(),
               1,
               this.stateOffCustomModelData,
               this.stateOffItemModel,
               this.stateOffGlow,
               this.displayName,
               new ArrayList<>(this.lore),
               new ArrayList<>(this.actions)
            );
      }

      @Generated
      public void setSlot(SlotCompound slot) {
         this.slot = slot;
      }

      @Generated
      public void setStateOnMaterial(ComplexMaterial stateOnMaterial) {
         this.stateOnMaterial = stateOnMaterial;
      }

      @Generated
      public void setStateOnCustomModelData(int stateOnCustomModelData) {
         this.stateOnCustomModelData = stateOnCustomModelData;
      }

      @Generated
      public void setStateOnItemModel(String stateOnItemModel) {
         this.stateOnItemModel = stateOnItemModel;
      }

      @Generated
      public void setStateOnGlow(boolean stateOnGlow) {
         this.stateOnGlow = stateOnGlow;
      }

      @Generated
      public void setStateOffMaterial(ComplexMaterial stateOffMaterial) {
         this.stateOffMaterial = stateOffMaterial;
      }

      @Generated
      public void setStateOffCustomModelData(int stateOffCustomModelData) {
         this.stateOffCustomModelData = stateOffCustomModelData;
      }

      @Generated
      public void setStateOffItemModel(String stateOffItemModel) {
         this.stateOffItemModel = stateOffItemModel;
      }

      @Generated
      public void setStateOffGlow(boolean stateOffGlow) {
         this.stateOffGlow = stateOffGlow;
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
      public void setActions(List<String> actions) {
         this.actions = actions;
      }

      @Generated
      public SlotCompound getSlot() {
         return this.slot;
      }

      @Generated
      public ComplexMaterial getStateOnMaterial() {
         return this.stateOnMaterial;
      }

      @Generated
      public int getStateOnCustomModelData() {
         return this.stateOnCustomModelData;
      }

      @Generated
      public String getStateOnItemModel() {
         return this.stateOnItemModel;
      }

      @Generated
      public boolean isStateOnGlow() {
         return this.stateOnGlow;
      }

      @Generated
      public ComplexMaterial getStateOffMaterial() {
         return this.stateOffMaterial;
      }

      @Generated
      public int getStateOffCustomModelData() {
         return this.stateOffCustomModelData;
      }

      @Generated
      public String getStateOffItemModel() {
         return this.stateOffItemModel;
      }

      @Generated
      public boolean isStateOffGlow() {
         return this.stateOffGlow;
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
      public List<String> getActions() {
         return this.actions;
      }

      @Generated
      public ItemStateConfiguration() {
      }

      @Generated
      public ItemStateConfiguration(
         SlotCompound slot,
         ComplexMaterial stateOnMaterial,
         int stateOnCustomModelData,
         String stateOnItemModel,
         boolean stateOnGlow,
         ComplexMaterial stateOffMaterial,
         int stateOffCustomModelData,
         String stateOffItemModel,
         boolean stateOffGlow,
         String displayName,
         List<String> lore,
         List<String> actions
      ) {
         this.slot = slot;
         this.stateOnMaterial = stateOnMaterial;
         this.stateOnCustomModelData = stateOnCustomModelData;
         this.stateOnItemModel = stateOnItemModel;
         this.stateOnGlow = stateOnGlow;
         this.stateOffMaterial = stateOffMaterial;
         this.stateOffCustomModelData = stateOffCustomModelData;
         this.stateOffItemModel = stateOffItemModel;
         this.stateOffGlow = stateOffGlow;
         this.displayName = displayName;
         this.lore = lore;
         this.actions = actions;
      }
   }

   public static class NonActionsItemConfiguration implements Cloneable {
      @ConfigField
      protected SlotCompound slot = new SlotCompound(0, 0);
      @ConfigField
      protected ComplexMaterial material = new ComplexMaterial(XMaterial.ENDER_PEARL);
      @ConfigField
      protected int amount = 1;
      @ConfigField
      protected int customModelData = 0;
      @ConfigField
      protected String itemModel = null;
      @ConfigField
      protected boolean glow = true;
      @ConfigField
      protected boolean hideAttributes = false;
      @ConfigField
      protected String displayName = "&aCustom Item";
      @ConfigField
      protected List<String> lore = Lists.newArrayList(
         new String[]{"&7This is a custom item", "&7click here to see something.", "", "&6Left-Click: &eSee something."}
      );
      @ConfigField
      protected String tooltipStyle = null;
      @ConfigField
      protected Boolean hideTooltip = null;
      @ConfigField
      protected Boolean enchantmentGlintOverride = null;
      @ConfigField
      protected List<String> bannerMeta = null;
      @ConfigField
      protected String trimMaterial = null;
      @ConfigField
      protected String trimPattern = null;
      @ConfigField
      protected List<String> potionEffects = null;
      @ConfigField
      protected String rgb = null;

      public NonActionsItemConfiguration(
         SlotCompound slot, ComplexMaterial material, int amount, int customModelData, String itemModel, boolean glow, String displayName, List<String> lore
      ) {
         this.slot = slot;
         this.material = material;
         this.amount = amount;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.displayName = displayName;
         this.lore = lore;
      }

      @Deprecated
      public NonActionsItemConfiguration(
         SlotCompound slot, ComplexMaterial material, int amount, int customModelData, boolean glow, String displayName, List<String> lore
      ) {
         this(slot, material, amount, customModelData, null, glow, displayName, lore);
      }

      public NonActionsItemConfiguration(
         SlotCompound slot,
         ComplexMaterial material,
         int amount,
         int customModelData,
         String itemModel,
         boolean glow,
         boolean hideAttributes,
         String displayName,
         List<String> lore
      ) {
         this.slot = slot;
         this.material = material;
         this.amount = amount;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.hideAttributes = hideAttributes;
         this.displayName = displayName;
         this.lore = lore;
      }

      public ContainerItemsConfiguration.NonActionsItemConfiguration clone() {
         return new ContainerItemsConfiguration.NonActionsItemConfiguration(
            this.slot.clone(),
            this.material == null ? null : this.material.clone(),
            this.amount,
            this.customModelData,
            this.itemModel,
            this.glow,
            this.hideAttributes,
            this.displayName,
            this.lore == null ? null : new ArrayList<>(this.lore),
            this.tooltipStyle,
            this.hideTooltip,
            this.enchantmentGlintOverride,
            this.bannerMeta == null ? null : new ArrayList<>(this.bannerMeta),
            this.trimMaterial,
            this.trimPattern,
            this.potionEffects == null ? null : new ArrayList<>(this.potionEffects),
            this.rgb
         );
      }

      @Generated
      public void setSlot(SlotCompound slot) {
         this.slot = slot;
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
      public void setTooltipStyle(String tooltipStyle) {
         this.tooltipStyle = tooltipStyle;
      }

      @Generated
      public void setHideTooltip(Boolean hideTooltip) {
         this.hideTooltip = hideTooltip;
      }

      @Generated
      public void setEnchantmentGlintOverride(Boolean enchantmentGlintOverride) {
         this.enchantmentGlintOverride = enchantmentGlintOverride;
      }

      @Generated
      public void setBannerMeta(List<String> bannerMeta) {
         this.bannerMeta = bannerMeta;
      }

      @Generated
      public void setTrimMaterial(String trimMaterial) {
         this.trimMaterial = trimMaterial;
      }

      @Generated
      public void setTrimPattern(String trimPattern) {
         this.trimPattern = trimPattern;
      }

      @Generated
      public void setPotionEffects(List<String> potionEffects) {
         this.potionEffects = potionEffects;
      }

      @Generated
      public void setRgb(String rgb) {
         this.rgb = rgb;
      }

      @Generated
      public SlotCompound getSlot() {
         return this.slot;
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
      public String getTooltipStyle() {
         return this.tooltipStyle;
      }

      @Generated
      public Boolean getHideTooltip() {
         return this.hideTooltip;
      }

      @Generated
      public Boolean getEnchantmentGlintOverride() {
         return this.enchantmentGlintOverride;
      }

      @Generated
      public List<String> getBannerMeta() {
         return this.bannerMeta;
      }

      @Generated
      public String getTrimMaterial() {
         return this.trimMaterial;
      }

      @Generated
      public String getTrimPattern() {
         return this.trimPattern;
      }

      @Generated
      public List<String> getPotionEffects() {
         return this.potionEffects;
      }

      @Generated
      public String getRgb() {
         return this.rgb;
      }

      @Generated
      public NonActionsItemConfiguration() {
      }

      @Generated
      public NonActionsItemConfiguration(
         SlotCompound slot,
         ComplexMaterial material,
         int amount,
         int customModelData,
         String itemModel,
         boolean glow,
         boolean hideAttributes,
         String displayName,
         List<String> lore,
         String tooltipStyle,
         Boolean hideTooltip,
         Boolean enchantmentGlintOverride,
         List<String> bannerMeta,
         String trimMaterial,
         String trimPattern,
         List<String> potionEffects,
         String rgb
      ) {
         this.slot = slot;
         this.material = material;
         this.amount = amount;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.hideAttributes = hideAttributes;
         this.displayName = displayName;
         this.lore = lore;
         this.tooltipStyle = tooltipStyle;
         this.hideTooltip = hideTooltip;
         this.enchantmentGlintOverride = enchantmentGlintOverride;
         this.bannerMeta = bannerMeta;
         this.trimMaterial = trimMaterial;
         this.trimPattern = trimPattern;
         this.potionEffects = potionEffects;
         this.rgb = rgb;
      }
   }

   public static class NonActionsSingleSlotItemConfiguration implements Cloneable {
      @ConfigField
      protected int slot = 0;
      @ConfigField
      protected ComplexMaterial material = new ComplexMaterial(XMaterial.ENDER_PEARL);
      @ConfigField
      protected int customModelData = 0;
      @ConfigField
      protected String itemModel = null;
      @ConfigField
      protected boolean glow = false;
      @ConfigField
      protected String displayName = "&aCustom Display Name";
      @ConfigField
      protected List<String> lore = Lists.newArrayList(new String[]{"&7This is a custom lore"});
      @ConfigField
      protected String tooltipStyle = null;
      @ConfigField
      protected Boolean hideTooltip = null;
      @ConfigField
      protected Boolean enchantmentGlintOverride = null;
      @ConfigField
      protected List<String> bannerMeta = null;
      @ConfigField
      protected String trimMaterial = null;
      @ConfigField
      protected String trimPattern = null;
      @ConfigField
      protected List<String> potionEffects = null;
      @ConfigField
      protected String rgb = null;

      @Deprecated
      public NonActionsSingleSlotItemConfiguration(int slot, ComplexMaterial material, int customModelData, boolean glow, String displayName, List<String> lore) {
         this(slot, material, customModelData, null, glow, displayName, lore);
      }

      public NonActionsSingleSlotItemConfiguration(
         int slot, ComplexMaterial material, int customModelData, String itemModel, boolean glow, String displayName, List<String> lore
      ) {
         this.slot = slot;
         this.material = material;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.displayName = displayName;
         this.lore = lore;
      }

      public ContainerItemsConfiguration.NonActionsSingleSlotItemConfiguration clone() {
         return new ContainerItemsConfiguration.NonActionsSingleSlotItemConfiguration(
            this.slot,
            this.material == null ? null : this.material.clone(),
            this.customModelData,
            this.itemModel,
            this.glow,
            this.displayName,
            this.lore == null ? null : new ArrayList<>(this.lore),
            this.tooltipStyle,
            this.hideTooltip,
            this.enchantmentGlintOverride,
            this.bannerMeta == null ? null : new ArrayList<>(this.bannerMeta),
            this.trimMaterial,
            this.trimPattern,
            this.potionEffects == null ? null : new ArrayList<>(this.potionEffects),
            this.rgb
         );
      }

      @Generated
      public void setSlot(int slot) {
         this.slot = slot;
      }

      @Generated
      public void setMaterial(ComplexMaterial material) {
         this.material = material;
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
      public void setDisplayName(String displayName) {
         this.displayName = displayName;
      }

      @Generated
      public void setLore(List<String> lore) {
         this.lore = lore;
      }

      @Generated
      public void setTooltipStyle(String tooltipStyle) {
         this.tooltipStyle = tooltipStyle;
      }

      @Generated
      public void setHideTooltip(Boolean hideTooltip) {
         this.hideTooltip = hideTooltip;
      }

      @Generated
      public void setEnchantmentGlintOverride(Boolean enchantmentGlintOverride) {
         this.enchantmentGlintOverride = enchantmentGlintOverride;
      }

      @Generated
      public void setBannerMeta(List<String> bannerMeta) {
         this.bannerMeta = bannerMeta;
      }

      @Generated
      public void setTrimMaterial(String trimMaterial) {
         this.trimMaterial = trimMaterial;
      }

      @Generated
      public void setTrimPattern(String trimPattern) {
         this.trimPattern = trimPattern;
      }

      @Generated
      public void setPotionEffects(List<String> potionEffects) {
         this.potionEffects = potionEffects;
      }

      @Generated
      public void setRgb(String rgb) {
         this.rgb = rgb;
      }

      @Generated
      public int getSlot() {
         return this.slot;
      }

      @Generated
      public ComplexMaterial getMaterial() {
         return this.material;
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
      public String getDisplayName() {
         return this.displayName;
      }

      @Generated
      public List<String> getLore() {
         return this.lore;
      }

      @Generated
      public String getTooltipStyle() {
         return this.tooltipStyle;
      }

      @Generated
      public Boolean getHideTooltip() {
         return this.hideTooltip;
      }

      @Generated
      public Boolean getEnchantmentGlintOverride() {
         return this.enchantmentGlintOverride;
      }

      @Generated
      public List<String> getBannerMeta() {
         return this.bannerMeta;
      }

      @Generated
      public String getTrimMaterial() {
         return this.trimMaterial;
      }

      @Generated
      public String getTrimPattern() {
         return this.trimPattern;
      }

      @Generated
      public List<String> getPotionEffects() {
         return this.potionEffects;
      }

      @Generated
      public String getRgb() {
         return this.rgb;
      }

      @Generated
      public NonActionsSingleSlotItemConfiguration() {
      }

      @Generated
      public NonActionsSingleSlotItemConfiguration(
         int slot,
         ComplexMaterial material,
         int customModelData,
         String itemModel,
         boolean glow,
         String displayName,
         List<String> lore,
         String tooltipStyle,
         Boolean hideTooltip,
         Boolean enchantmentGlintOverride,
         List<String> bannerMeta,
         String trimMaterial,
         String trimPattern,
         List<String> potionEffects,
         String rgb
      ) {
         this.slot = slot;
         this.material = material;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.displayName = displayName;
         this.lore = lore;
         this.tooltipStyle = tooltipStyle;
         this.hideTooltip = hideTooltip;
         this.enchantmentGlintOverride = enchantmentGlintOverride;
         this.bannerMeta = bannerMeta;
         this.trimMaterial = trimMaterial;
         this.trimPattern = trimPattern;
         this.potionEffects = potionEffects;
         this.rgb = rgb;
      }
   }

   public static class NonDisplayBasicItemConfiguration implements Cloneable {
      @ConfigField
      protected String displayName = "&aCustom Display Name";
      @ConfigField
      protected List<String> lore = Lists.newArrayList(new String[]{"&7This is a custom lore"});

      public ContainerItemsConfiguration.NonDisplayBasicItemConfiguration clone() {
         return new ContainerItemsConfiguration.NonDisplayBasicItemConfiguration(this.displayName, new ArrayList<>(this.lore));
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
      public String getDisplayName() {
         return this.displayName;
      }

      @Generated
      public List<String> getLore() {
         return this.lore;
      }

      @Generated
      public NonDisplayBasicItemConfiguration() {
      }

      @Generated
      public NonDisplayBasicItemConfiguration(String displayName, List<String> lore) {
         this.displayName = displayName;
         this.lore = lore;
      }
   }

   public static class NonDisplayItemConfiguration implements Cloneable {
      @ConfigField
      protected String displayName = "&aCustom Display Name";
      @ConfigField
      protected List<String> lore = Lists.newArrayList(new String[]{"&7This is a custom lore"});
      @ConfigField
      protected List<String> actions = Lists.newArrayList(new String[]{"[MESSAGE] &7This is a custom action"});

      public ContainerItemsConfiguration.NonDisplayItemConfiguration clone() {
         return new ContainerItemsConfiguration.NonDisplayItemConfiguration(this.displayName, new ArrayList<>(this.lore), new ArrayList<>(this.actions));
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
      public void setActions(List<String> actions) {
         this.actions = actions;
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
      public List<String> getActions() {
         return this.actions;
      }

      @Generated
      public NonDisplayItemConfiguration() {
      }

      @Generated
      public NonDisplayItemConfiguration(String displayName, List<String> lore, List<String> actions) {
         this.displayName = displayName;
         this.lore = lore;
         this.actions = actions;
      }
   }

   public static class NonMaterialItemConfiguration implements Cloneable {
      @ConfigField
      protected SlotCompound slot = new SlotCompound(0);
      @ConfigField
      protected String displayName = "&aCustom Display Name";
      @ConfigField
      protected List<String> lore = Lists.newArrayList(new String[]{"&7This is a custom lore"});
      @ConfigField
      protected List<String> actions = Lists.newArrayList(
         new String[]{
            "[CLOSE_INVENTORY]",
            "[MESSAGE] &7This menu through actions closed after you clicked on the item.",
            "[TITLE] &aCustom Action;&7This is a [TITLE] action.;10;20;10"
         }
      );

      public ContainerItemsConfiguration.NonMaterialItemConfiguration clone() {
         return new ContainerItemsConfiguration.NonMaterialItemConfiguration(
            this.slot.clone(), this.displayName, new ArrayList<>(this.lore), new ArrayList<>(this.actions)
         );
      }

      @Generated
      public void setSlot(SlotCompound slot) {
         this.slot = slot;
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
      public void setActions(List<String> actions) {
         this.actions = actions;
      }

      @Generated
      public SlotCompound getSlot() {
         return this.slot;
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
      public List<String> getActions() {
         return this.actions;
      }

      @Generated
      public NonMaterialItemConfiguration() {
      }

      @Generated
      public NonMaterialItemConfiguration(SlotCompound slot, String displayName, List<String> lore, List<String> actions) {
         this.slot = slot;
         this.displayName = displayName;
         this.lore = lore;
         this.actions = actions;
      }
   }

   public static class NonSlotBasicItemConfiguration implements Cloneable {
      @ConfigField
      protected ComplexMaterial material = new ComplexMaterial(XMaterial.ENDER_PEARL);
      @ConfigField
      protected int customModelData = 0;
      @ConfigField
      protected String itemModel = null;
      @ConfigField
      protected boolean glow = false;
      @ConfigField
      protected String displayName = "&aCustom Display Name";
      @ConfigField
      protected List<String> lore = Lists.newArrayList(new String[]{"&7This is a custom lore"});
      @ConfigField
      protected String tooltipStyle = null;
      @ConfigField
      protected Boolean hideTooltip = null;
      @ConfigField
      protected Boolean enchantmentGlintOverride = null;
      @ConfigField
      protected List<String> bannerMeta = null;
      @ConfigField
      protected String trimMaterial = null;
      @ConfigField
      protected String trimPattern = null;
      @ConfigField
      protected List<String> potionEffects = null;
      @ConfigField
      protected String rgb = null;

      @Deprecated
      public NonSlotBasicItemConfiguration(ComplexMaterial material, int customModelData, boolean glow, String displayName, List<String> lore) {
         this(material, customModelData, null, glow, displayName, lore);
      }

      public NonSlotBasicItemConfiguration(ComplexMaterial material, int customModelData, String itemModel, boolean glow, String displayName, List<String> lore) {
         this.material = material;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.displayName = displayName;
         this.lore = lore;
      }

      public ContainerItemsConfiguration.NonSlotBasicItemConfiguration clone() {
         return new ContainerItemsConfiguration.NonSlotBasicItemConfiguration(
            this.material == null ? null : this.material.clone(),
            this.customModelData,
            this.itemModel,
            this.glow,
            this.displayName,
            this.lore == null ? null : new ArrayList<>(this.lore),
            this.tooltipStyle,
            this.hideTooltip,
            this.enchantmentGlintOverride,
            this.bannerMeta == null ? null : new ArrayList<>(this.bannerMeta),
            this.trimMaterial,
            this.trimPattern,
            this.potionEffects == null ? null : new ArrayList<>(this.potionEffects),
            this.rgb
         );
      }

      @Generated
      public void setMaterial(ComplexMaterial material) {
         this.material = material;
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
      public void setDisplayName(String displayName) {
         this.displayName = displayName;
      }

      @Generated
      public void setLore(List<String> lore) {
         this.lore = lore;
      }

      @Generated
      public void setTooltipStyle(String tooltipStyle) {
         this.tooltipStyle = tooltipStyle;
      }

      @Generated
      public void setHideTooltip(Boolean hideTooltip) {
         this.hideTooltip = hideTooltip;
      }

      @Generated
      public void setEnchantmentGlintOverride(Boolean enchantmentGlintOverride) {
         this.enchantmentGlintOverride = enchantmentGlintOverride;
      }

      @Generated
      public void setBannerMeta(List<String> bannerMeta) {
         this.bannerMeta = bannerMeta;
      }

      @Generated
      public void setTrimMaterial(String trimMaterial) {
         this.trimMaterial = trimMaterial;
      }

      @Generated
      public void setTrimPattern(String trimPattern) {
         this.trimPattern = trimPattern;
      }

      @Generated
      public void setPotionEffects(List<String> potionEffects) {
         this.potionEffects = potionEffects;
      }

      @Generated
      public void setRgb(String rgb) {
         this.rgb = rgb;
      }

      @Generated
      public ComplexMaterial getMaterial() {
         return this.material;
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
      public String getDisplayName() {
         return this.displayName;
      }

      @Generated
      public List<String> getLore() {
         return this.lore;
      }

      @Generated
      public String getTooltipStyle() {
         return this.tooltipStyle;
      }

      @Generated
      public Boolean getHideTooltip() {
         return this.hideTooltip;
      }

      @Generated
      public Boolean getEnchantmentGlintOverride() {
         return this.enchantmentGlintOverride;
      }

      @Generated
      public List<String> getBannerMeta() {
         return this.bannerMeta;
      }

      @Generated
      public String getTrimMaterial() {
         return this.trimMaterial;
      }

      @Generated
      public String getTrimPattern() {
         return this.trimPattern;
      }

      @Generated
      public List<String> getPotionEffects() {
         return this.potionEffects;
      }

      @Generated
      public String getRgb() {
         return this.rgb;
      }

      @Generated
      public NonSlotBasicItemConfiguration() {
      }

      @Generated
      public NonSlotBasicItemConfiguration(
         ComplexMaterial material,
         int customModelData,
         String itemModel,
         boolean glow,
         String displayName,
         List<String> lore,
         String tooltipStyle,
         Boolean hideTooltip,
         Boolean enchantmentGlintOverride,
         List<String> bannerMeta,
         String trimMaterial,
         String trimPattern,
         List<String> potionEffects,
         String rgb
      ) {
         this.material = material;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.displayName = displayName;
         this.lore = lore;
         this.tooltipStyle = tooltipStyle;
         this.hideTooltip = hideTooltip;
         this.enchantmentGlintOverride = enchantmentGlintOverride;
         this.bannerMeta = bannerMeta;
         this.trimMaterial = trimMaterial;
         this.trimPattern = trimPattern;
         this.potionEffects = potionEffects;
         this.rgb = rgb;
      }
   }

   public static class NonSlotItemConfiguration implements Cloneable {
      @ConfigField
      protected ComplexMaterial material = new ComplexMaterial(XMaterial.ENDER_PEARL);
      @ConfigField
      protected int customModelData = 0;
      @ConfigField
      protected String itemModel = null;
      @ConfigField
      protected boolean glow = false;
      @ConfigField
      protected String displayName = "&aCustom Display Name";
      @ConfigField
      protected List<String> lore = Lists.newArrayList(new String[]{"&7This is a custom lore"});
      @ConfigField
      protected List<String> actions = Lists.newArrayList(new String[]{"[MESSAGE] &7This is a custom action"});
      @ConfigField
      protected String tooltipStyle = null;
      @ConfigField
      protected Boolean hideTooltip = null;
      @ConfigField
      protected Boolean enchantmentGlintOverride = null;
      @ConfigField
      protected List<String> bannerMeta = null;
      @ConfigField
      protected String trimMaterial = null;
      @ConfigField
      protected String trimPattern = null;
      @ConfigField
      protected List<String> potionEffects = null;
      @ConfigField
      protected String rgb = null;

      @Deprecated
      public NonSlotItemConfiguration(ComplexMaterial material, int customModelData, boolean glow, String displayName, List<String> lore, List<String> actions) {
         this(material, customModelData, null, glow, displayName, lore, actions);
      }

      public NonSlotItemConfiguration(
         ComplexMaterial material, int customModelData, String itemModel, boolean glow, String displayName, List<String> lore, List<String> actions
      ) {
         this.material = material;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.displayName = displayName;
         this.lore = lore;
         this.actions = actions;
      }

      public ContainerItemsConfiguration.NonSlotItemConfiguration clone() {
         return new ContainerItemsConfiguration.NonSlotItemConfiguration(
            this.material == null ? null : this.material.clone(),
            this.customModelData,
            this.itemModel,
            this.glow,
            this.displayName,
            this.lore == null ? null : new ArrayList<>(this.lore),
            this.actions == null ? null : new ArrayList<>(this.actions),
            this.tooltipStyle,
            this.hideTooltip,
            this.enchantmentGlintOverride,
            this.bannerMeta == null ? null : new ArrayList<>(this.bannerMeta),
            this.trimMaterial,
            this.trimPattern,
            this.potionEffects == null ? null : new ArrayList<>(this.potionEffects),
            this.rgb
         );
      }

      @Generated
      public void setMaterial(ComplexMaterial material) {
         this.material = material;
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
      public void setDisplayName(String displayName) {
         this.displayName = displayName;
      }

      @Generated
      public void setLore(List<String> lore) {
         this.lore = lore;
      }

      @Generated
      public void setActions(List<String> actions) {
         this.actions = actions;
      }

      @Generated
      public void setTooltipStyle(String tooltipStyle) {
         this.tooltipStyle = tooltipStyle;
      }

      @Generated
      public void setHideTooltip(Boolean hideTooltip) {
         this.hideTooltip = hideTooltip;
      }

      @Generated
      public void setEnchantmentGlintOverride(Boolean enchantmentGlintOverride) {
         this.enchantmentGlintOverride = enchantmentGlintOverride;
      }

      @Generated
      public void setBannerMeta(List<String> bannerMeta) {
         this.bannerMeta = bannerMeta;
      }

      @Generated
      public void setTrimMaterial(String trimMaterial) {
         this.trimMaterial = trimMaterial;
      }

      @Generated
      public void setTrimPattern(String trimPattern) {
         this.trimPattern = trimPattern;
      }

      @Generated
      public void setPotionEffects(List<String> potionEffects) {
         this.potionEffects = potionEffects;
      }

      @Generated
      public void setRgb(String rgb) {
         this.rgb = rgb;
      }

      @Generated
      public ComplexMaterial getMaterial() {
         return this.material;
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
      public String getDisplayName() {
         return this.displayName;
      }

      @Generated
      public List<String> getLore() {
         return this.lore;
      }

      @Generated
      public List<String> getActions() {
         return this.actions;
      }

      @Generated
      public String getTooltipStyle() {
         return this.tooltipStyle;
      }

      @Generated
      public Boolean getHideTooltip() {
         return this.hideTooltip;
      }

      @Generated
      public Boolean getEnchantmentGlintOverride() {
         return this.enchantmentGlintOverride;
      }

      @Generated
      public List<String> getBannerMeta() {
         return this.bannerMeta;
      }

      @Generated
      public String getTrimMaterial() {
         return this.trimMaterial;
      }

      @Generated
      public String getTrimPattern() {
         return this.trimPattern;
      }

      @Generated
      public List<String> getPotionEffects() {
         return this.potionEffects;
      }

      @Generated
      public String getRgb() {
         return this.rgb;
      }

      @Generated
      public NonSlotItemConfiguration() {
      }

      @Generated
      public NonSlotItemConfiguration(
         ComplexMaterial material,
         int customModelData,
         String itemModel,
         boolean glow,
         String displayName,
         List<String> lore,
         List<String> actions,
         String tooltipStyle,
         Boolean hideTooltip,
         Boolean enchantmentGlintOverride,
         List<String> bannerMeta,
         String trimMaterial,
         String trimPattern,
         List<String> potionEffects,
         String rgb
      ) {
         this.material = material;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.displayName = displayName;
         this.lore = lore;
         this.actions = actions;
         this.tooltipStyle = tooltipStyle;
         this.hideTooltip = hideTooltip;
         this.enchantmentGlintOverride = enchantmentGlintOverride;
         this.bannerMeta = bannerMeta;
         this.trimMaterial = trimMaterial;
         this.trimPattern = trimPattern;
         this.potionEffects = potionEffects;
         this.rgb = rgb;
      }
   }

   public static class SingleSlotItemConfiguration implements Cloneable {
      @ConfigField
      protected int slot = 0;
      @ConfigField
      protected ComplexMaterial material = new ComplexMaterial(XMaterial.ENDER_PEARL);
      @ConfigField
      protected int customModelData = 0;
      @ConfigField
      protected String itemModel = null;
      @ConfigField
      protected boolean glow = false;
      @ConfigField
      protected String displayName = "&aCustom Display Name";
      @ConfigField
      protected List<String> lore = Lists.newArrayList(new String[]{"&7This is a custom lore"});
      @ConfigField
      protected List<String> actions = Lists.newArrayList(new String[]{"[MESSAGE] &7This is a custom action"});
      @ConfigField
      protected String tooltipStyle = null;
      @ConfigField
      protected Boolean hideTooltip = null;
      @ConfigField
      protected Boolean enchantmentGlintOverride = null;
      @ConfigField
      protected List<String> bannerMeta = null;
      @ConfigField
      protected String trimMaterial = null;
      @ConfigField
      protected String trimPattern = null;
      @ConfigField
      protected List<String> potionEffects = null;
      @ConfigField
      protected String rgb = null;

      public SingleSlotItemConfiguration(
         int slot, ComplexMaterial material, int customModelData, String itemModel, boolean glow, String displayName, List<String> lore, List<String> actions
      ) {
         this.slot = slot;
         this.material = material;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.displayName = displayName;
         this.lore = lore;
         this.actions = actions;
      }

      @Deprecated
      public SingleSlotItemConfiguration(
         int slot, ComplexMaterial material, int customModelData, boolean glow, String displayName, List<String> lore, List<String> actions
      ) {
         this(slot, material, customModelData, null, glow, displayName, lore, actions);
      }

      public ContainerItemsConfiguration.SingleSlotItemConfiguration clone() {
         return new ContainerItemsConfiguration.SingleSlotItemConfiguration(
            this.slot,
            this.material == null ? null : this.material.clone(),
            this.customModelData,
            this.itemModel,
            this.glow,
            this.displayName,
            this.lore == null ? null : new ArrayList<>(this.lore),
            this.actions == null ? null : new ArrayList<>(this.actions),
            this.tooltipStyle,
            this.hideTooltip,
            this.enchantmentGlintOverride,
            this.bannerMeta == null ? null : new ArrayList<>(this.bannerMeta),
            this.trimMaterial,
            this.trimPattern,
            this.potionEffects == null ? null : new ArrayList<>(this.potionEffects),
            this.rgb
         );
      }

      @Generated
      public void setSlot(int slot) {
         this.slot = slot;
      }

      @Generated
      public void setMaterial(ComplexMaterial material) {
         this.material = material;
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
      public void setDisplayName(String displayName) {
         this.displayName = displayName;
      }

      @Generated
      public void setLore(List<String> lore) {
         this.lore = lore;
      }

      @Generated
      public void setActions(List<String> actions) {
         this.actions = actions;
      }

      @Generated
      public void setTooltipStyle(String tooltipStyle) {
         this.tooltipStyle = tooltipStyle;
      }

      @Generated
      public void setHideTooltip(Boolean hideTooltip) {
         this.hideTooltip = hideTooltip;
      }

      @Generated
      public void setEnchantmentGlintOverride(Boolean enchantmentGlintOverride) {
         this.enchantmentGlintOverride = enchantmentGlintOverride;
      }

      @Generated
      public void setBannerMeta(List<String> bannerMeta) {
         this.bannerMeta = bannerMeta;
      }

      @Generated
      public void setTrimMaterial(String trimMaterial) {
         this.trimMaterial = trimMaterial;
      }

      @Generated
      public void setTrimPattern(String trimPattern) {
         this.trimPattern = trimPattern;
      }

      @Generated
      public void setPotionEffects(List<String> potionEffects) {
         this.potionEffects = potionEffects;
      }

      @Generated
      public void setRgb(String rgb) {
         this.rgb = rgb;
      }

      @Generated
      public int getSlot() {
         return this.slot;
      }

      @Generated
      public ComplexMaterial getMaterial() {
         return this.material;
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
      public String getDisplayName() {
         return this.displayName;
      }

      @Generated
      public List<String> getLore() {
         return this.lore;
      }

      @Generated
      public List<String> getActions() {
         return this.actions;
      }

      @Generated
      public String getTooltipStyle() {
         return this.tooltipStyle;
      }

      @Generated
      public Boolean getHideTooltip() {
         return this.hideTooltip;
      }

      @Generated
      public Boolean getEnchantmentGlintOverride() {
         return this.enchantmentGlintOverride;
      }

      @Generated
      public List<String> getBannerMeta() {
         return this.bannerMeta;
      }

      @Generated
      public String getTrimMaterial() {
         return this.trimMaterial;
      }

      @Generated
      public String getTrimPattern() {
         return this.trimPattern;
      }

      @Generated
      public List<String> getPotionEffects() {
         return this.potionEffects;
      }

      @Generated
      public String getRgb() {
         return this.rgb;
      }

      @Generated
      public SingleSlotItemConfiguration() {
      }

      @Generated
      public SingleSlotItemConfiguration(
         int slot,
         ComplexMaterial material,
         int customModelData,
         String itemModel,
         boolean glow,
         String displayName,
         List<String> lore,
         List<String> actions,
         String tooltipStyle,
         Boolean hideTooltip,
         Boolean enchantmentGlintOverride,
         List<String> bannerMeta,
         String trimMaterial,
         String trimPattern,
         List<String> potionEffects,
         String rgb
      ) {
         this.slot = slot;
         this.material = material;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.glow = glow;
         this.displayName = displayName;
         this.lore = lore;
         this.actions = actions;
         this.tooltipStyle = tooltipStyle;
         this.hideTooltip = hideTooltip;
         this.enchantmentGlintOverride = enchantmentGlintOverride;
         this.bannerMeta = bannerMeta;
         this.trimMaterial = trimMaterial;
         this.trimPattern = trimPattern;
         this.potionEffects = potionEffects;
         this.rgb = rgb;
      }
   }

   public static class SingleSlotItemStateConfiguration implements Cloneable {
      @ConfigField
      protected int slot = 0;
      @ConfigField(path = "states.on.material")
      protected ComplexMaterial stateOnMaterial = new ComplexMaterial(XMaterial.ENDER_PEARL);
      @ConfigField(path = "states.on.custom-model-data")
      protected int stateOnCustomModelData = 0;
      @ConfigField(path = "states.on.item-model")
      protected String stateOnItemModel = null;
      @ConfigField(path = "states.on.glow")
      protected boolean stateOnGlow = false;
      @ConfigField(path = "states.off.material")
      protected ComplexMaterial stateOffMaterial = new ComplexMaterial(XMaterial.ENDER_PEARL);
      @ConfigField(path = "states.off.custom-model-data")
      protected int stateOffCustomModelData = 0;
      @ConfigField(path = "states.off.item-model")
      protected String stateOffItemModel;
      @ConfigField(path = "states.off.glow")
      protected boolean stateOffGlow = false;
      @ConfigField
      protected String displayName = "&aCustom Display Name";
      @ConfigField
      protected List<String> lore = Lists.newArrayList(new String[]{"&7This is a custom lore"});
      @ConfigField
      protected List<String> actions = Lists.newArrayList(new String[]{"[MESSAGE] &7This is a custom action"});

      public ContainerItemsConfiguration.SingleSlotItemStateConfiguration clone() {
         return new ContainerItemsConfiguration.SingleSlotItemStateConfiguration(
            this.slot,
            this.stateOnMaterial == null ? null : this.stateOnMaterial.clone(),
            this.stateOnCustomModelData,
            this.stateOnItemModel,
            this.stateOnGlow,
            this.stateOffMaterial == null ? null : this.stateOffMaterial.clone(),
            this.stateOffCustomModelData,
            this.stateOffItemModel,
            this.stateOffGlow,
            this.displayName,
            new ArrayList<>(this.lore),
            new ArrayList<>(this.actions)
         );
      }

      public ContainerItemsConfiguration.SingleSlotItemConfiguration as(boolean state) {
         return state
            ? new ContainerItemsConfiguration.SingleSlotItemConfiguration(
               this.slot,
               this.stateOnMaterial == null ? null : this.stateOnMaterial.clone(),
               this.stateOnCustomModelData,
               this.stateOnItemModel,
               this.stateOnGlow,
               this.displayName,
               new ArrayList<>(this.lore),
               new ArrayList<>(this.actions)
            )
            : new ContainerItemsConfiguration.SingleSlotItemConfiguration(
               this.slot,
               this.stateOffMaterial == null ? null : this.stateOffMaterial.clone(),
               this.stateOffCustomModelData,
               this.stateOffItemModel,
               this.stateOffGlow,
               this.displayName,
               new ArrayList<>(this.lore),
               new ArrayList<>(this.actions)
            );
      }

      @Generated
      public void setSlot(int slot) {
         this.slot = slot;
      }

      @Generated
      public void setStateOnMaterial(ComplexMaterial stateOnMaterial) {
         this.stateOnMaterial = stateOnMaterial;
      }

      @Generated
      public void setStateOnCustomModelData(int stateOnCustomModelData) {
         this.stateOnCustomModelData = stateOnCustomModelData;
      }

      @Generated
      public void setStateOnItemModel(String stateOnItemModel) {
         this.stateOnItemModel = stateOnItemModel;
      }

      @Generated
      public void setStateOnGlow(boolean stateOnGlow) {
         this.stateOnGlow = stateOnGlow;
      }

      @Generated
      public void setStateOffMaterial(ComplexMaterial stateOffMaterial) {
         this.stateOffMaterial = stateOffMaterial;
      }

      @Generated
      public void setStateOffCustomModelData(int stateOffCustomModelData) {
         this.stateOffCustomModelData = stateOffCustomModelData;
      }

      @Generated
      public void setStateOffItemModel(String stateOffItemModel) {
         this.stateOffItemModel = stateOffItemModel;
      }

      @Generated
      public void setStateOffGlow(boolean stateOffGlow) {
         this.stateOffGlow = stateOffGlow;
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
      public void setActions(List<String> actions) {
         this.actions = actions;
      }

      @Generated
      public int getSlot() {
         return this.slot;
      }

      @Generated
      public ComplexMaterial getStateOnMaterial() {
         return this.stateOnMaterial;
      }

      @Generated
      public int getStateOnCustomModelData() {
         return this.stateOnCustomModelData;
      }

      @Generated
      public String getStateOnItemModel() {
         return this.stateOnItemModel;
      }

      @Generated
      public boolean isStateOnGlow() {
         return this.stateOnGlow;
      }

      @Generated
      public ComplexMaterial getStateOffMaterial() {
         return this.stateOffMaterial;
      }

      @Generated
      public int getStateOffCustomModelData() {
         return this.stateOffCustomModelData;
      }

      @Generated
      public String getStateOffItemModel() {
         return this.stateOffItemModel;
      }

      @Generated
      public boolean isStateOffGlow() {
         return this.stateOffGlow;
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
      public List<String> getActions() {
         return this.actions;
      }

      @Generated
      public SingleSlotItemStateConfiguration() {
      }

      @Generated
      public SingleSlotItemStateConfiguration(
         int slot,
         ComplexMaterial stateOnMaterial,
         int stateOnCustomModelData,
         String stateOnItemModel,
         boolean stateOnGlow,
         ComplexMaterial stateOffMaterial,
         int stateOffCustomModelData,
         String stateOffItemModel,
         boolean stateOffGlow,
         String displayName,
         List<String> lore,
         List<String> actions
      ) {
         this.slot = slot;
         this.stateOnMaterial = stateOnMaterial;
         this.stateOnCustomModelData = stateOnCustomModelData;
         this.stateOnItemModel = stateOnItemModel;
         this.stateOnGlow = stateOnGlow;
         this.stateOffMaterial = stateOffMaterial;
         this.stateOffCustomModelData = stateOffCustomModelData;
         this.stateOffItemModel = stateOffItemModel;
         this.stateOffGlow = stateOffGlow;
         this.displayName = displayName;
         this.lore = lore;
         this.actions = actions;
      }
   }
}
