package id.seria.gacha.managers.crates;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.api.crate.animation.AnimationEnvironment;
import id.seria.gacha.api.crate.hologram.HologramType;
import id.seria.gacha.api.internal.Item;
import id.seria.gacha.api.reward.GuaranteedRewardType;
import id.seria.gacha.api.reward.RewardsMode;
import id.seria.gacha.api.sdk.text.Text;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.lib.common.uicomponents.newest.Ui;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.LoadFirst;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.limbo.item.CustomItem;
import id.seria.gacha.limbo.item.storage.ItemStorageCleaner;
import id.seria.gacha.limbo.item.storage.ItemStorageScope;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectData;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectType;
import id.seria.gacha.managers.crates.animations.opening.OpeningPhase;
import id.seria.gacha.managers.crates.cost.CostEngineType;
import id.seria.gacha.managers.crates.engine.EngineData;
import id.seria.gacha.managers.crates.engine.EngineType;
import id.seria.gacha.managers.crates.engine.vanilla_block.VanillaBlockEngineData;
import id.seria.gacha.managers.crates.hologram.HologramProperties;
import id.seria.gacha.managers.crates.rerolling.CrateRerolling;
import id.seria.gacha.managers.crates.type.CrateBlockType;
import id.seria.gacha.managers.crates.type.list.OpeningAnimation;
import id.seria.gacha.managers.crates.type.list.OpeningAnimationWrapper;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CrateType extends Configuration implements id.seria.gacha.api.crate.CrateType, ItemStorageScope {
   private static long ID_COUNTER = System.currentTimeMillis();
   private boolean registered = false;
   @ConfigField
   private boolean enabled = true;
   private boolean dirtyIdentifier;
   private String oldIdentifier;
   @ConfigField
   private String identifier = "crate_" + ID_COUNTER++;
   @ConfigField
   private BaseItem item = BaseItem.from(XMaterial.CHEST)
      .setCustomName(Ui.hex("&#CCFF00", "&a") + "Example Crate")
      .setCustomLore("&7This is an example crate, you can edit it as you wish!", "", "&eUse this crate to get awesome rewards!");
   @ConfigField
   private String displayName;
   @ConfigField(path = "block-material")
   private CrateBlockType blockType = new CrateBlockType(XMaterial.CHEST);
   @ConfigField(path = "engine")
   private EngineData engineData = new VanillaBlockEngineData();
   @ConfigField(path = "open-cost.engine")
   private CostEngineType openCostEngine = CostEngineType.VAULT;
   @ConfigField(path = "open-cost.amount", legacyPaths = "open-money-cost")
   private double openMoneyCost = 0.0;
   @ConfigField(path = "open-cost.currency")
   private String openCostCurrency = "";
   @ConfigField(path = "open-cooldown")
   private int openCooldownSeconds = 0;
   @ConfigField(path = "simultaneous-openings")
   private boolean simultaneousOpeningsEnabled = true;
   @ConfigField(path = "preview-item")
   private boolean previewItemEnabled = false;
   @ConfigField(path = "permission.required")
   private boolean permissionRequired = false;
   @ConfigField(path = "permission.permission")
   private String permission = "phoenixcrates.crate.example";
   @ConfigField(path = "idle-effects", legacyPaths = "animation.idle-effects")
   private List<IdleEffect> idleEffects;
   @ConfigField(path = "opening-animation", legacyPaths = {"animation.opening-phases", "animation.opening-animations"})
   private OpeningAnimationWrapper openingAnimations;
   @ConfigField(path = "hologram")
   private HologramProperties hologramProperties;
   @ConfigField(path = "menus.rewards", legacyPaths = {"menus.preview", "preview-menu"})
   private String rewardsMenuName;
   @ConfigField(path = "menus.select-reward", legacyPaths = "menus.selective-reward")
   private String selectRewardMenuName;
   @ConfigField(path = "menus.rerolls", legacyPaths = "reroll.menu-name")
   private String rerollsMenuName;
   @ConfigField(path = "menus.preview-actions", compact = true)
   private List<String> previewActions;
   @ConfigField
   private boolean keyRequired;
   @ConfigField
   private Set<String> linkedKeysIds;
   @ConfigField
   private boolean allowOpenCommand = false;
   @ConfigField(path = "broadcast.message", compact = true)
   private List<String> broadcastMessage;
   @ConfigField
   private int maxWinRewards;
   @ConfigField(path = "rewards-mode")
   private RewardsMode rewardsMode;
   @ConfigField(path = "rewards")
   private List<CustomReward> registeredRewards;
   private List<CustomReward> cachedValidRewards;
   @ConfigField
   private GuaranteedRewardType guaranteedRewardType;
   @ConfigField(path = "reroll")
   private CrateRerolling rerolling;
   @ConfigField
   private CrateExtraSettings extraSettings;
   @LoadFirst
   @ConfigField(path = "internal-storage.scope-id")
   private String storageScopeId;
   @ConfigField(path = "internal-storage.items")
   private Map<String, ItemStack> internalStorageItems;

   public CrateType(
      boolean registered,
      boolean enabled,
      String identifier,
      String displayName,
      CrateBlockType blockType,
      EngineData engineData,
      double openMoneyCost,
      int openCooldownSeconds,
      boolean simultaneousOpeningsEnabled,
      boolean previewItemEnabled,
      boolean permissionRequired,
      String permission,
      boolean allowOpenCommand,
      List<IdleEffect> idleEffects,
      OpeningAnimationWrapper openingAnimations,
      HologramProperties hologramProperties,
      String rewardsMenuName,
      String selectRewardMenuName,
      boolean keyRequired,
      Set<String> linkedKeysIds,
      List<String> broadcastMessage,
      RewardsMode rewardsMode,
      List<CustomReward> registeredRewards,
      GuaranteedRewardType guaranteedRewardType,
      CrateRerolling rerolling,
      CrateExtraSettings extraSettings
   ) {
      this.idleEffects = Lists.newArrayList(
         new IdleEffect[]{
            new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.REDSTONE, Color.RED, 0.0, 0.0, 0.0, 1.0, 0.1, 2)),
            new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.FLAME, Color.WHITE, 0.0, 0.0, 0.0, 0.0, 0.1, 2)),
            new IdleEffect(IdleEffectType.STAR, new IdleEffectData(ParticleEffect.SMOKE_NORMAL, Color.WHITE, 0.0, 0.0, 0.0, 0.0, 0.0, 2))
         }
      );
      this.openingAnimations = new OpeningAnimationWrapper();
      this.hologramProperties = new HologramProperties();
      this.rewardsMenuName = "default_rewards_preview";
      this.selectRewardMenuName = "default_select_reward";
      this.rerollsMenuName = "default_crate_reroll";
      this.previewActions = new ArrayList<>();
      this.keyRequired = true;
      this.linkedKeysIds = new HashSet<>();
      this.broadcastMessage = Lists.newArrayList(new String[]{"&ePlayer &a%player% &ejust got &6\"%reward%\" &ereward from the crate &6%crate%&e!"});
      this.rewardsMode = RewardsMode.RANDOM;
      this.registeredRewards = Lists.newArrayList();
      this.guaranteedRewardType = GuaranteedRewardType.SEQUENTIAL;
      this.rerolling = new CrateRerolling();
      this.extraSettings = new CrateExtraSettings();
      this.storageScopeId = UUID.randomUUID().toString().replace("-", "");
      this.internalStorageItems = new LinkedHashMap<>();
      this.registered = registered;
      this.enabled = enabled;
      this.oldIdentifier = identifier;
      this.identifier = identifier;
      this.displayName = displayName;
      this.blockType = blockType;
      this.engineData = engineData;
      this.openMoneyCost = openMoneyCost;
      this.openCooldownSeconds = openCooldownSeconds;
      this.simultaneousOpeningsEnabled = simultaneousOpeningsEnabled;
      this.previewItemEnabled = previewItemEnabled;
      this.permissionRequired = permissionRequired;
      this.allowOpenCommand = allowOpenCommand;
      this.permission = permission;
      this.idleEffects = idleEffects;
      this.openingAnimations = openingAnimations;
      this.hologramProperties = hologramProperties;
      this.rewardsMenuName = rewardsMenuName;
      this.selectRewardMenuName = selectRewardMenuName;
      this.keyRequired = keyRequired;
      this.linkedKeysIds = linkedKeysIds;
      this.broadcastMessage = broadcastMessage;
      this.rewardsMode = rewardsMode;
      this.registeredRewards = registeredRewards;
      this.guaranteedRewardType = guaranteedRewardType;
      this.rerolling = rerolling;
      this.extraSettings = extraSettings;
      this.storageScopeId = UUID.randomUUID().toString().replace("-", "");
      this.internalStorageItems = new LinkedHashMap<>();
   }

   public String getFormattedDisplayName(Player player) {
      return Placeholders.setPlaceholders(this.getDisplayName(), player);
   }

   @Override
   public String getDisplayName() {
      return this.item == null ? this.displayName : this.item.getDisplayName();
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
      if (this.item != null) {
         this.item.setCustomName(displayName);
      }
   }

   public void setItem(BaseItem item) {
      this.item = item;
      if (item != null) {
         this.displayName = item.getCustomName() != null ? item.getCustomName() : item.getDisplayName();
      }
   }

   public ItemStack createItemStack() {
      RItemStack rItem = RItemStack.of(this.item.build());
      String humanizedKey = StringUtil.humanize(this.identifier);
      List<String> updatedLore = this.item.getLore().stream().map(lore -> lore.replace("%crate%", humanizedKey)).collect(Collectors.toList());
      rItem.lore(Text.plain(updatedLore));
      rItem.flags(Constants.ITEM_FLAGS);
      ItemBuilder builder = ItemBuilder.of(rItem.bukkit());
      builder.setNbtTag("phoenixcrates:crate", this.identifier);
      return builder.build();
   }

   @Override
   public void onLoad() {
      this.registered = true;
      if (this.storageScopeId == null || this.storageScopeId.trim().isEmpty()) {
         this.storageScopeId = UUID.randomUUID().toString().replace("-", "");
      }

      if (this.internalStorageItems == null) {
         this.internalStorageItems = new LinkedHashMap<>();
      }

      if (this.previewActions == null) {
         this.previewActions = new ArrayList<>();
      }

      this.cleanupUnusedStorageItems();
   }

   public void cleanupUnusedStorageItems() {
      List<Item> referencedItems = new ArrayList<>();
      referencedItems.add(this.item);
      if (this.registeredRewards != null) {
         for (CustomReward reward : this.registeredRewards) {
            referencedItems.add(reward.getDisplayItem());
            referencedItems.add(reward.getAlternative() == null ? null : reward.getAlternative().getItem());
            if (reward.getWinItems() != null) {
               referencedItems.addAll(reward.getWinItems());
            }
         }
      }

      ItemStorageCleaner.removeUnused(this, referencedItems);
   }

   public void invalidateRewardsCache() {
      this.cachedValidRewards = null;
   }

   @Override
   public List<CustomReward> getRegisteredRewards() {
      return Collections.unmodifiableList(this.registeredRewards);
   }

   public HologramType getHologramType() {
      return this.getHologramProperties().getType();
   }

   @Override
   public List<CustomReward> getAvailableRewards() {
      if (this.cachedValidRewards == null) {
         this.cachedValidRewards = this.registeredRewards.stream().filter(reward -> reward.isValid() && reward.isEnabled()).collect(Collectors.toList());
      }

      return this.cachedValidRewards.stream().filter(reward -> !reward.isTimeLimitedExpired()).collect(Collectors.toList());
   }

   public void addReward(CustomReward reward) {
      reward.markRegistered();
      this.registeredRewards.add(reward);
      this.invalidateRewardsCache();
   }

   public void removeReward(CustomReward reward) {
      reward.unmarkRegistered();
      this.registeredRewards.remove(reward);
      this.invalidateRewardsCache();
      this.cleanupUnusedStorageItems();
   }

   public void setIdentifier(String newIdentifier) {
      this.oldIdentifier = this.identifier;
      this.identifier = ChatColor.stripColor(newIdentifier.replace(" ", "_"));
      this.dirtyIdentifier = true;
   }

   public XMaterial getBlockMaterial() {
      return this.blockType.getMaterial();
   }

   public EngineType getEngineType() {
      return this.engineData.getType();
   }

   public CostEngineType getCostEngineType() {
      return this.openCostEngine;
   }

   public CustomReward getRewardByIdentifier(String identifier) {
      return this.registeredRewards.stream().filter(r -> r.getIdentifier().equalsIgnoreCase(identifier)).findFirst().orElse(null);
   }

   public boolean isPreviewMenuEnabled() {
      return !this.rewardsMenuName.isEmpty();
   }

   public boolean hasPreviewActions() {
      return !this.previewActions.isEmpty();
   }

   public void markRegistered() {
      this.registered = true;
   }

   public void unmarkRegistered() {
      this.registered = false;
   }

   public boolean hasAnyWorldAnimationPhase() {
      if (this.openingAnimations == null) {
         return false;
      }

      List<OpeningAnimation> animations = this.openingAnimations.list();
      return animations.isEmpty()
         ? false
         : animations.stream()
            .flatMap(animation -> animation.map().values().stream())
            .filter(phase -> phase != null && phase.getPhase() != null)
            .anyMatch(phase -> phase.getPhase().getEnvironment() == AnimationEnvironment.WORLD);
   }

   public boolean hasVirtualOpenPhase() {
      if (this.openingAnimations == null) {
         return false;
      }

      List<OpeningAnimation> animations = this.openingAnimations.list();
      if (animations.isEmpty()) {
         return false;
      }

      OpeningPhase openPhase = animations.get(0).getOpenPhase();
      return openPhase != null && openPhase.getPhase() != null && openPhase.getPhase().getEnvironment() == AnimationEnvironment.GUI;
   }

   private static int maxWinRewardsCeiling() {
      return Crates.getPlugin().getEdition() == Edition.LITE ? 5 : 30;
   }

   public int getMaxWinRewards() {
      return MathUtil.clamp(this.maxWinRewards, 1, maxWinRewardsCeiling());
   }

   public void setMaxWinRewards(int maxWinRewards) {
      this.maxWinRewards = MathUtil.clamp(maxWinRewards, 1, maxWinRewardsCeiling());
   }

   @Override
   public Configuration clone() {
      CrateType clone = new CrateType();
      clone.registered = this.registered;
      clone.enabled = this.enabled;
      clone.dirtyIdentifier = this.dirtyIdentifier;
      clone.oldIdentifier = this.oldIdentifier;
      clone.identifier = this.identifier;
      clone.displayName = this.displayName;
      clone.blockType = this.blockType == null ? null : this.blockType.clone();
      clone.engineData = this.engineData == null ? null : this.engineData.clone();
      clone.openCostEngine = this.openCostEngine;
      clone.openMoneyCost = this.openMoneyCost;
      clone.openCostCurrency = this.openCostCurrency;
      clone.allowOpenCommand = this.allowOpenCommand;
      clone.openCooldownSeconds = this.openCooldownSeconds;
      clone.simultaneousOpeningsEnabled = this.simultaneousOpeningsEnabled;
      clone.previewItemEnabled = this.previewItemEnabled;
      clone.permissionRequired = this.permissionRequired;
      clone.permission = this.permission;
      clone.idleEffects = this.idleEffects == null ? new ArrayList<>() : this.idleEffects.stream().map(IdleEffect::clone).collect(Collectors.toList());
      clone.openingAnimations = this.openingAnimations == null ? null : this.openingAnimations.clone();
      clone.hologramProperties = this.hologramProperties == null ? null : this.hologramProperties.clone();
      clone.rewardsMenuName = this.rewardsMenuName;
      clone.selectRewardMenuName = this.selectRewardMenuName;
      clone.rerollsMenuName = this.rerollsMenuName;
      clone.previewActions = this.previewActions == null ? new ArrayList<>() : new ArrayList<>(this.previewActions);
      clone.keyRequired = this.keyRequired;
      clone.linkedKeysIds = this.linkedKeysIds == null ? new HashSet<>() : new HashSet<>(this.linkedKeysIds);
      clone.broadcastMessage = this.broadcastMessage == null ? new ArrayList<>() : new ArrayList<>(this.broadcastMessage);
      clone.maxWinRewards = this.maxWinRewards;
      clone.rewardsMode = this.rewardsMode;
      clone.registeredRewards = this.registeredRewards == null
         ? new ArrayList<>()
         : this.registeredRewards.stream().map(CustomReward::clone).collect(Collectors.toList());
      clone.cachedValidRewards = null;
      clone.guaranteedRewardType = this.guaranteedRewardType;
      clone.rerolling = this.rerolling == null ? null : this.rerolling.clone();
      clone.extraSettings = this.extraSettings == null ? null : this.extraSettings.clone();
      String newScopeId = UUID.randomUUID().toString().replace("-", "");
      clone.storageScopeId = newScopeId;
      clone.internalStorageItems = new LinkedHashMap<>();

      for (Entry<String, ItemStack> entry : this.internalStorageItems.entrySet()) {
         clone.internalStorageItems.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().clone());
      }

      remapStorageReferences(clone, this.storageScopeId, newScopeId);
      this.copyLoadedModulesTo(clone);
      return clone;
   }

   public void regenerateStorageScopeId() {
      String oldScope = this.storageScopeId;
      this.storageScopeId = UUID.randomUUID().toString().replace("-", "");
      remapStorageReferences(this, oldScope, this.storageScopeId);
   }

   private static void remapStorageReferences(CrateType type, String oldScope, String newScope) {
      if (oldScope != null && !oldScope.equals(newScope)) {
         if (type.registeredRewards != null) {
            for (CustomReward reward : type.registeredRewards) {
               remapItem(reward.getDisplayItem(), oldScope, newScope);
               remapItem(reward.getAlternative() == null ? null : reward.getAlternative().getItem(), oldScope, newScope);
               if (reward.getWinItems() != null) {
                  for (Item item : reward.getWinItems()) {
                     remapItem(item, oldScope, newScope);
                  }
               }
            }
         }

         if (type.item instanceof CustomItem) {
            CustomItem.unsafe().remapScope((CustomItem)type.item, oldScope, newScope);
         }
      }
   }

   private static void remapItem(Object item, String oldScope, String newScope) {
      if (item instanceof CustomItem) {
         CustomItem.unsafe().remapScope((CustomItem)item, oldScope, newScope);
      }
   }

   public void swapRewards(int fromIndex, int toIndex) {
      Collections.swap(this.registeredRewards, fromIndex, toIndex);
   }

   @Generated
   public boolean isRegistered() {
      return this.registered;
   }

   @Generated
   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public boolean isDirtyIdentifier() {
      return this.dirtyIdentifier;
   }

   @Generated
   public String getOldIdentifier() {
      return this.oldIdentifier;
   }

   @Generated
   @Override
   public String getIdentifier() {
      return this.identifier;
   }

   @Generated
   public BaseItem getItem() {
      return this.item;
   }

   @Generated
   public CrateBlockType getBlockType() {
      return this.blockType;
   }

   @Generated
   public EngineData getEngineData() {
      return this.engineData;
   }

   @Generated
   public CostEngineType getOpenCostEngine() {
      return this.openCostEngine;
   }

   @Generated
   @Override
   public double getOpenMoneyCost() {
      return this.openMoneyCost;
   }

   @Generated
   public String getOpenCostCurrency() {
      return this.openCostCurrency;
   }

   @Generated
   @Override
   public int getOpenCooldownSeconds() {
      return this.openCooldownSeconds;
   }

   @Generated
   @Override
   public boolean isSimultaneousOpeningsEnabled() {
      return this.simultaneousOpeningsEnabled;
   }

   @Generated
   @Override
   public boolean isPreviewItemEnabled() {
      return this.previewItemEnabled;
   }

   @Generated
   @Override
   public boolean isPermissionRequired() {
      return this.permissionRequired;
   }

   @Generated
   @Override
   public String getPermission() {
      return this.permission;
   }

   @Generated
   public List<IdleEffect> getIdleEffects() {
      return this.idleEffects;
   }

   @Generated
   public OpeningAnimationWrapper getOpeningAnimations() {
      return this.openingAnimations;
   }

   @Generated
   public HologramProperties getHologramProperties() {
      return this.hologramProperties;
   }

   @Generated
   @Override
   public String getRewardsMenuName() {
      return this.rewardsMenuName;
   }

   @Generated
   @Override
   public String getSelectRewardMenuName() {
      return this.selectRewardMenuName;
   }

   @Generated
   public String getRerollsMenuName() {
      return this.rerollsMenuName;
   }

   @Generated
   public List<String> getPreviewActions() {
      return this.previewActions;
   }

   @Generated
   @Override
   public boolean isKeyRequired() {
      return this.keyRequired;
   }

   @Generated
   @Override
   public Set<String> getLinkedKeysIds() {
      return this.linkedKeysIds;
   }

   @Generated
   public boolean isAllowOpenCommand() {
      return this.allowOpenCommand;
   }

   @Generated
   @Override
   public List<String> getBroadcastMessage() {
      return this.broadcastMessage;
   }

   @Generated
   public RewardsMode getRewardsMode() {
      return this.rewardsMode;
   }

   @Generated
   public List<CustomReward> getCachedValidRewards() {
      return this.cachedValidRewards;
   }

   @Generated
   @Override
   public GuaranteedRewardType getGuaranteedRewardType() {
      return this.guaranteedRewardType;
   }

   @Generated
   public CrateRerolling getRerolling() {
      return this.rerolling;
   }

   @Generated
   public CrateExtraSettings getExtraSettings() {
      return this.extraSettings;
   }

   @Generated
   @Override
   public String getStorageScopeId() {
      return this.storageScopeId;
   }

   @Generated
   @Override
   public Map<String, ItemStack> getInternalStorageItems() {
      return this.internalStorageItems;
   }

   @Generated
   public void setRegistered(boolean registered) {
      this.registered = registered;
   }

   @Generated
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Generated
   public void setDirtyIdentifier(boolean dirtyIdentifier) {
      this.dirtyIdentifier = dirtyIdentifier;
   }

   @Generated
   public void setOldIdentifier(String oldIdentifier) {
      this.oldIdentifier = oldIdentifier;
   }

   @Generated
   public void setBlockType(CrateBlockType blockType) {
      this.blockType = blockType;
   }

   @Generated
   public void setEngineData(EngineData engineData) {
      this.engineData = engineData;
   }

   @Generated
   public void setOpenCostEngine(CostEngineType openCostEngine) {
      this.openCostEngine = openCostEngine;
   }

   @Generated
   public void setOpenMoneyCost(double openMoneyCost) {
      this.openMoneyCost = openMoneyCost;
   }

   @Generated
   public void setOpenCostCurrency(String openCostCurrency) {
      this.openCostCurrency = openCostCurrency;
   }

   @Generated
   public void setOpenCooldownSeconds(int openCooldownSeconds) {
      this.openCooldownSeconds = openCooldownSeconds;
   }

   @Generated
   public void setSimultaneousOpeningsEnabled(boolean simultaneousOpeningsEnabled) {
      this.simultaneousOpeningsEnabled = simultaneousOpeningsEnabled;
   }

   @Generated
   public void setPreviewItemEnabled(boolean previewItemEnabled) {
      this.previewItemEnabled = previewItemEnabled;
   }

   @Generated
   public void setPermissionRequired(boolean permissionRequired) {
      this.permissionRequired = permissionRequired;
   }

   @Generated
   public void setPermission(String permission) {
      this.permission = permission;
   }

   @Generated
   public void setIdleEffects(List<IdleEffect> idleEffects) {
      this.idleEffects = idleEffects;
   }

   @Generated
   public void setOpeningAnimations(OpeningAnimationWrapper openingAnimations) {
      this.openingAnimations = openingAnimations;
   }

   @Generated
   public void setHologramProperties(HologramProperties hologramProperties) {
      this.hologramProperties = hologramProperties;
   }

   @Generated
   public void setRewardsMenuName(String rewardsMenuName) {
      this.rewardsMenuName = rewardsMenuName;
   }

   @Generated
   public void setSelectRewardMenuName(String selectRewardMenuName) {
      this.selectRewardMenuName = selectRewardMenuName;
   }

   @Generated
   public void setRerollsMenuName(String rerollsMenuName) {
      this.rerollsMenuName = rerollsMenuName;
   }

   @Generated
   public void setPreviewActions(List<String> previewActions) {
      this.previewActions = previewActions;
   }

   @Generated
   public void setKeyRequired(boolean keyRequired) {
      this.keyRequired = keyRequired;
   }

   @Generated
   public void setLinkedKeysIds(Set<String> linkedKeysIds) {
      this.linkedKeysIds = linkedKeysIds;
   }

   @Generated
   public void setAllowOpenCommand(boolean allowOpenCommand) {
      this.allowOpenCommand = allowOpenCommand;
   }

   @Generated
   public void setBroadcastMessage(List<String> broadcastMessage) {
      this.broadcastMessage = broadcastMessage;
   }

   @Generated
   public void setRewardsMode(RewardsMode rewardsMode) {
      this.rewardsMode = rewardsMode;
   }

   @Generated
   public void setRegisteredRewards(List<CustomReward> registeredRewards) {
      this.registeredRewards = registeredRewards;
   }

   @Generated
   public void setCachedValidRewards(List<CustomReward> cachedValidRewards) {
      this.cachedValidRewards = cachedValidRewards;
   }

   @Generated
   public void setGuaranteedRewardType(GuaranteedRewardType guaranteedRewardType) {
      this.guaranteedRewardType = guaranteedRewardType;
   }

   @Generated
   public void setRerolling(CrateRerolling rerolling) {
      this.rerolling = rerolling;
   }

   @Generated
   public void setExtraSettings(CrateExtraSettings extraSettings) {
      this.extraSettings = extraSettings;
   }

   @Generated
   public void setStorageScopeId(String storageScopeId) {
      this.storageScopeId = storageScopeId;
   }

   @Generated
   public void setInternalStorageItems(Map<String, ItemStack> internalStorageItems) {
      this.internalStorageItems = internalStorageItems;
   }

   @Generated
   public CrateType() {
      this.idleEffects = Lists.newArrayList(
         new IdleEffect[]{
            new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.REDSTONE, Color.RED, 0.0, 0.0, 0.0, 1.0, 0.1, 2)),
            new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.FLAME, Color.WHITE, 0.0, 0.0, 0.0, 0.0, 0.1, 2)),
            new IdleEffect(IdleEffectType.STAR, new IdleEffectData(ParticleEffect.SMOKE_NORMAL, Color.WHITE, 0.0, 0.0, 0.0, 0.0, 0.0, 2))
         }
      );
      this.openingAnimations = new OpeningAnimationWrapper();
      this.hologramProperties = new HologramProperties();
      this.rewardsMenuName = "default_rewards_preview";
      this.selectRewardMenuName = "default_select_reward";
      this.rerollsMenuName = "default_crate_reroll";
      this.previewActions = new ArrayList<>();
      this.keyRequired = true;
      this.linkedKeysIds = new HashSet<>();
      this.broadcastMessage = Lists.newArrayList(new String[]{"&ePlayer &a%player% &ejust got &6\"%reward%\" &ereward from the crate &6%crate%&e!"});
      this.rewardsMode = RewardsMode.RANDOM;
      this.registeredRewards = Lists.newArrayList();
      this.guaranteedRewardType = GuaranteedRewardType.SEQUENTIAL;
      this.rerolling = new CrateRerolling();
      this.extraSettings = new CrateExtraSettings();
      this.storageScopeId = UUID.randomUUID().toString().replace("-", "");
      this.internalStorageItems = new LinkedHashMap<>();
   }
}
