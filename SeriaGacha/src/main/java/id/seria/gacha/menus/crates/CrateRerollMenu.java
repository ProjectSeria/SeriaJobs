package id.seria.gacha.menus.crates;

import com.google.common.collect.Lists;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.actions.Actions;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.configurable.ConfigurableLayout;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.CloseViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.OpenViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.objects.ComplexMaterial;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.session.CrateOpeningSession;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Generated;
import org.apache.commons.lang3.Validate;
import org.bukkit.inventory.ItemStack;

public class CrateRerollMenu extends ConfigurableLayout {
   private final CrateOpeningSession openingSession;
   private boolean ignoreNextClose;

   public CrateRerollMenu(PhoenixCrates plugin, CrateRerollMenu.CrateRerollMenuConfiguration configuration, CrateOpeningSession openingSession) {
      super(plugin, Translations.getProvider(), configuration, Actions.getFactory());
      this.openingSession = openingSession;
      Validate.isTrue(!openingSession.getRewardsGiven().isEmpty(), "Rewards were not given", new Object[0]);
   }

   @Override
   public void onPrepare(ContainerView ignored) {
   }

   @Override
   public void onOpen(OpenViewContext context) {
      super.onOpen(context);
      context.setContainerTitle(Placeholders.setPlaceholders(this.getConfiguration().getSettings().getTitle(), context.getViewer()));
      context.setContainerType(ContainerType.fromRows(this.getConfiguration().getSettings().getRows()));
   }

   @Override
   public void onRender(RenderViewContext context) {
      super.onRender(context);
      CrateRerollMenu.CrateRerollMenuConfiguration containerConfiguration = (CrateRerollMenu.CrateRerollMenuConfiguration)this.getConfiguration();
      this.setItem(context, containerConfiguration.getRewardSlot(), this.openingSession.getRewardsGiven().get(0).getDisplayItem().build(), click -> {});
      this.setItem(context, containerConfiguration.getClaimItem(), click -> {
         this.openingSession.claim();
         this.ignoreNextClose = true;
         context.getViewer().closeInventory();
         this.plugin.getScheduler().runSync(context.getViewer(), () -> context.getViewer().updateInventory(), 1L);
      });
      this.setItem(context, this.r(containerConfiguration.getRerollItem(), "%rerolls_left%", this.openingSession.getRerollsLeft()), click -> {
         if (this.openingSession.getRerollsLeft() != 0) {
            this.openingSession.reroll();
            this.ignoreNextClose = true;
            if (this.openingSession.getRerollsLeft() == 0) {
               context.getViewer().closeInventory();
               this.plugin.getScheduler().runSync(context.getViewer(), () -> context.getViewer().updateInventory(), 1L);
            } else if (this.openingSession.getCrate().getType().getRerolling().isReplayAnimation()) {
               context.getViewer().closeInventory();
               this.openingSession.replayAnimation(() -> this.openingSession.openRerollMenu());
            } else {
               context.getContainerView().updateView();
            }
         }
      });
   }

   @Override
   public void onClose(CloseViewContext context) {
      if (this.ignoreNextClose) {
         this.ignoreNextClose = false;
      } else {
         this.openingSession.claim();
      }
   }

   @Override
   public void onRenderSlot(RenderViewContext context, VirtualItem item) {
      if (item.getDisplayName() != null) {
         item.withDisplayName(Placeholders.setPlaceholders(item.getDisplayName(), context.getViewer()));
      }

      if (item.getLore() != null) {
         item.withLore(item.getLore().stream().map(text -> Placeholders.setPlaceholders(text, context.getViewer())).collect(Collectors.toList()));
      }
   }

   @Deprecated
   protected void setItem(RenderViewContext context, SlotCompound slot, ItemStack item, Consumer<ClickViewContext> consumer) {
      if (slot.length() != 0 && slot.getFirst() >= 0) {
         new VirtualItem().withSlots(slot).withItem(item).whenClick(consumer).render(context);
      }
   }

   @Deprecated
   protected void setItem(RenderViewContext context, CrateRerollMenu.RerollItemConfiguration item, Consumer<ClickViewContext> consumer) {
      this.prepareItem(item, consumer).render(context);
   }

   @Deprecated
   protected VirtualItem prepareItem(CrateRerollMenu.RerollItemConfiguration item, Consumer<ClickViewContext> consumer) {
      if (item.getSlot().length() != 0 && item.getSlot().getFirst() >= 0) {
         String displayName = item.getDisplayName().isEmpty() ? "\u00a7r" : item.getDisplayName();
         return new VirtualItem()
            .withSlots(item.getSlot())
            .withItem(item.getMaterial())
            .withCustomModelData(item.getCustomModelData())
            .withItemModel(item.getItemModel())
            .withDisplayName(displayName)
            .withLore(item.getLore())
            .withFlag(Constants.ITEM_FLAGS)
            .whenClick(consumer);
      } else {
         return new VirtualItem();
      }
   }

   @Deprecated
   protected CrateRerollMenu.RerollItemConfiguration r(CrateRerollMenu.RerollItemConfiguration item, Object... values) {
      CrateRerollMenu.RerollItemConfiguration clone = item.clone();
      clone.setDisplayName(Translations.r(item.getDisplayName(), values));
      clone.setLore(Translations.r(item.getLore(), values));
      return clone;
   }

   @Generated
   public CrateOpeningSession getOpeningSession() {
      return this.openingSession;
   }

   @Generated
   public boolean isIgnoreNextClose() {
      return this.ignoreNextClose;
   }

   public static class CrateRerollMenuConfiguration extends ContainerConfiguration {
      @ConfigField
      protected SlotCompound rewardSlot;
      @ConfigField(path = "review")
      protected final CrateRerollMenu.RerollItemConfiguration claimItem;
      @ConfigField(path = "reroll", legacyPaths = "milestone")
      protected final CrateRerollMenu.RerollItemConfiguration rerollItem;

      public CrateRerollMenuConfiguration() {
         this.settings.setType("CRATE_REROLL");
         this.settings.setTitle("Claim this reward(s)?");
         this.settings.setRows(3);
         this.settings.setRefreshEnabled(false);
         this.settings.setRefreshRate(20);
         this.items.clear();
         this.rewardSlot = SlotCompound.from(5, 2);
         this.claimItem = new CrateRerollMenu.RerollItemConfiguration(
            SlotCompound.fromGrid(10, 1, 2),
            new ComplexMaterial(XMaterial.GREEN_STAINED_GLASS_PANE),
            1,
            0,
            "",
            true,
            true,
            "&aClaim",
            Lists.newArrayList(new String[]{"&7Click here to claim this reward(s).", "", "&6Left-Click: &eClaim."})
         );
         this.rerollItem = new CrateRerollMenu.RerollItemConfiguration(
            SlotCompound.fromGrid(15, 1, 2),
            new ComplexMaterial(XMaterial.PURPLE_STAINED_GLASS_PANE),
            1,
            0,
            "",
            true,
            true,
            "&aReroll",
            Lists.newArrayList(
               new String[]{"&7&7Click here to reroll this reward(s).", "&7You have &e%rerolls_left% &7rerolls left.", "", "&6Left-Click: &eReroll."}
            )
         );
      }

      public CrateRerollMenu.CrateRerollMenuConfiguration clone() {
         return new CrateRerollMenu.CrateRerollMenuConfiguration();
      }

      @Generated
      public SlotCompound getRewardSlot() {
         return this.rewardSlot;
      }

      @Generated
      public CrateRerollMenu.RerollItemConfiguration getClaimItem() {
         return this.claimItem;
      }

      @Generated
      public CrateRerollMenu.RerollItemConfiguration getRerollItem() {
         return this.rerollItem;
      }
   }

   public static class RerollItemConfiguration implements Cloneable {
      @ConfigField
      protected SlotCompound slot = new SlotCompound(0, 0);
      @ConfigField
      protected ComplexMaterial material;
      @ConfigField
      protected int amount;
      @ConfigField
      protected int customModelData;
      @ConfigField
      protected String itemModel;
      @ConfigField
      protected boolean glow;
      @ConfigField
      protected boolean hideAttributes;
      @ConfigField
      protected String displayName;
      @ConfigField
      protected List<String> lore;

      public RerollItemConfiguration(
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
         this.amount = 1;
         this.glow = glow;
         this.hideAttributes = hideAttributes;
         this.slot = slot;
         this.material = material;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.displayName = displayName;
         this.lore = lore;
      }

      public CrateRerollMenu.RerollItemConfiguration clone() {
         return new CrateRerollMenu.RerollItemConfiguration(
            this.slot.clone(),
            new ComplexMaterial(this.material.getMaterial(), this.material.getTexture()),
            this.amount,
            this.customModelData,
            this.itemModel,
            this.glow,
            this.hideAttributes,
            this.displayName,
            new ArrayList<>(this.lore)
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
      public RerollItemConfiguration() {
      }
   }
}
