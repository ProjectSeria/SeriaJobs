package id.seria.gacha.editor.layouts;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Addons;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.Ui;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.OpenViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.NavigableComponent;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.PremiumEditorKeys;
import id.seria.gacha.registries.EditorRegistry;
import id.seria.gacha.registries.InformationRegistry;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class EditorMainLayout extends EditorFacade.EditorLayout {
   public EditorMainLayout() {
      super(Translatable.key("editor.titles.main", "%version%", PhoenixCrates.getLegacyInstance().getCurrentVersion().get()), null);
   }

   @Override
   public void onOpen(OpenViewContext context) {
      context.setContainerTitle(this.t(this.getTitle())[0]);
      context.setContainerType(ContainerType.CONTAINER_9X5);
   }

   @Override
   public void onPrepare(ContainerView view) {
      boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
      this.addComponent(
         view,
         new NavigableComponent(
            SlotCompound.from(2, 2),
            this.createData(XMaterial.CHEST, Translatable.key("editor.main.crates.title"), Translatable.key("editor.main.crates.description")),
            new EditorCratesLayout(this)
         )
      );
      this.addComponent(
         view,
         new NavigableComponent(
            SlotCompound.from(3, 2),
            this.createData(XMaterial.TRIPWIRE_HOOK, Translatable.key("editor.main.keys.title"), Translatable.key("editor.main.keys.description")),
            new EditorKeysLayout(this)
         )
      );
      if (lite) {
         this.addComponent(
            view,
            new NavigableComponent(
                  SlotCompound.from(5, 2),
                  this.createData(
                     ItemBuilder.of(XMaterial.CAKE).addItemFlag(ItemFlag.HIDE_ATTRIBUTES),
                     Translatable.key("editor.addons.title"),
                     Translatable.key("editor.addons.description")
                  ),
                  new EditorMainLayout.DummyLayout(this)
               )
               .addWarning(Translatable.key("plans.upgrade-at"))
               .setLocked(ctx -> true)
         );
         this.addComponent(
            view,
            new NavigableComponent(
                  SlotCompound.from(7, 2),
                  this.createData(
                     ItemBuilder.of(XMaterial.DIAMOND).addItemFlag(ItemFlag.HIDE_ATTRIBUTES),
                     Translatable.key("editor.addons.rarities.title"),
                     Translatable.key("editor.addons.rarities.description")
                  ),
                  new EditorMainLayout.DummyLayout(this)
               )
               .addError(Translatable.key("plans.requires-addon"))
               .addWarning(Translatable.key("plans.upgrade-at"))
               .setLocked(ctx -> true)
         );
         this.addComponent(
            view,
            new NavigableComponent(
                  SlotCompound.from(8, 2),
                  this.createData(
                     XMaterial.ENDER_CHEST, Translatable.key("editor.addons.crate-claim.title"), Translatable.key("editor.addons.crate-claim.description")
                  ),
                  new EditorMainLayout.DummyLayout(this)
               )
               .addError(Translatable.key("plans.requires-addon"))
               .addWarning(Translatable.key("plans.upgrade-at"))
               .setLocked(ctx -> true)
         );
         this.addComponent(
            view,
            this.createComponent(
               SlotCompound.from(2, 3), this.createData(XMaterial.GRAY_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
            )
         );
         this.addComponent(
            view,
            this.createComponent(
               SlotCompound.from(3, 3), this.createData(XMaterial.GRAY_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
            )
         );
         this.addComponent(
            view,
            this.createComponent(
               SlotCompound.from(7, 3), this.createData(XMaterial.GRAY_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
            )
         );
      } else {
         this.addComponent(
            view,
            new NavigableComponent(
               SlotCompound.from(5, 2),
               this.createData(
                  ItemBuilder.of(XMaterial.CAKE.or(XMaterial.FIREWORK_ROCKET)).addItemFlags(Constants.ITEM_FLAGS),
                  Translatable.key("editor.main.addons.title"),
                  Translatable.key("editor.main.addons.description")
               ),
               EditorRegistry.get(PremiumEditorKeys.ADDONS_MARKETPLACE, this)
            )
         );
         this.addComponent(
            view,
            new NavigableComponent(
                  SlotCompound.from(7, 2),
                  this.createData(
                     ItemBuilder.of(XMaterial.DIAMOND).addItemFlag(ItemFlag.HIDE_ATTRIBUTES),
                     Constants.addon("Rarities"),
                     Translatable.key("editor.main.rarities.description")
                  ),
                  EditorRegistry.get(Addons.RARITIES, this)
               )
               .addWarning(InformationRegistry.isEnabled(Addons.RARITIES), Ui.hex("&#2ECC71", "&a") + "Rarity Addon is installed!")
               .addError(!InformationRegistry.isEnabled(Addons.RARITIES), Translatable.key("unlock-addon", "%addon%", "Rarities"))
         );
         this.addComponent(
            view,
            new NavigableComponent(
                  SlotCompound.from(8, 2),
                  this.createData(XMaterial.ENDER_CHEST, Constants.addon("Crate Claim"), Translatable.key("editor.settings.crate-claim.description")),
                  EditorRegistry.get(Addons.CLAIM, this)
               )
               .addWarning(InformationRegistry.isEnabled(Addons.CLAIM), Ui.hex("&#2ECC71", "&a") + "Crate Claim Addon is installed!")
               .addError(!InformationRegistry.isEnabled(Addons.CLAIM), Translatable.key("unlock-addon", "%addon%", "Crate Claim"))
         );
         this.addComponent(
            view,
            new NavigableComponent(
                  SlotCompound.from(7, 3),
                  this.createData(
                     XMaterial.RESIN_BRICK.or(XMaterial.STRING),
                     Constants.addon("Mass Opening"),
                     Translatable.literal("&7Configure mass opening", "&7global settings.")
                  ),
                  EditorRegistry.get(Addons.MASS_OPENING_SETTINGS, this)
               )
               .addWarning(InformationRegistry.isEnabled(Addons.MASS_OPENING), Ui.hex("&#2ECC71", "&a") + "Mass Opening Addon is installed!")
               .addError(!InformationRegistry.isEnabled(Addons.MASS_OPENING), Translatable.key("unlock-addon", "%addon%", "Mass Opening"))
         );
         this.addComponent(
            view,
            new NavigableComponent(
                  SlotCompound.from(2, 3),
                  this.createData(
                     XMaterial.CLOCK,
                     Constants.addon("Reward On Loop"),
                     Translatable.literal("&7Configure recurring rewards", "&7delivered to online players.")
                  ),
                  EditorRegistry.get(Addons.REWARD_ON_LOOP, this)
               )
               .addWarning(InformationRegistry.isEnabled(Addons.REWARD_ON_LOOP), Ui.hex("&#2ECC71", "&a") + "Reward On Loop Addon is installed!")
               .addError(!InformationRegistry.isEnabled(Addons.REWARD_ON_LOOP), Translatable.key("unlock-addon", "%addon%", "Reward On Loop"))
         );
         this.addComponent(
            view,
            new NavigableComponent(
                  SlotCompound.from(3, 3),
                  this.createData(XMaterial.REDSTONE, Constants.addon("IP Limit"), Translatable.key("editor.main.ip-limit.description")),
                  EditorRegistry.get(Addons.IP_LIMIT, this)
               )
               .addWarning(InformationRegistry.isEnabled(Addons.IP_LIMIT), Ui.hex("&#2ECC71", "&a") + "IP Limit Addon is installed!")
               .addError(!InformationRegistry.isEnabled(Addons.IP_LIMIT), Translatable.key("unlock-addon", "%addon%", "IP Limit"))
         );
      }

      this.addComponent(
         view,
         this.createComponent(
            SlotCompound.from(5, 3), this.createData(XMaterial.GRAY_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
         )
      );
      this.addComponent(
         view,
         new NavigableComponent(
               SlotCompound.from(8, 3),
               this.createData(XMaterial.COMPARATOR, Translatable.key("editor.main.settings.title"), Translatable.key("editor.main.settings.description")),
               new EditorSettingsLayout(this)
            )
            .setFixedPosition(true)
      );

      for (int x = 1; x <= 9; x++) {
         this.addComponent(
            view,
            this.createComponent(
                  SlotCompound.from(x, 5), this.createData(XMaterial.BLACK_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
               )
               .setFixedPosition(true)
         );
      }

      this.addComponent(
         view,
         new NavigableComponent(
               SlotCompound.from(3, 5),
               this.createData(
                  XMaterial.GUSTER_BANNER_PATTERN.or(XMaterial.BONE_MEAL),
                  Translatable.key("editor.main.migrations.title"),
                  Translatable.key("editor.main.migrations.description")
               ),
               new EditorMigrationsLayout(this)
            )
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         this.createComponent(
            SlotCompound.from(5, 5),
            this.createData(XMaterial.BARRIER, this.label("leave"), Translatable.literal(new ArrayList())),
            Lists.newArrayList(new ClickAction[]{ClickAction.legacy(ClickAction.ClickType.LEFT, "Close")}),
            context -> context.getViewer().closeInventory()
         )
      );
      this.addComponent(
         view,
         this.createComponent(
            SlotCompound.from(7, 5),
            this.createData(XMaterial.EMERALD, Translatable.key("editor.main.reload.title"), Translatable.key("editor.main.reload.description")),
            Lists.newArrayList(new ClickAction[]{ClickAction.RELOAD}),
            context -> {
               Player player = context.getViewer();

               try {
                  PhoenixCrates.getLegacyInstance().reload();
                  Translations.send(player, Translations.t("plugin-reloaded"));
               } catch (Exception e) {
                  Translations.error(player, e);
                  player.closeInventory();
               }
            }
         )
      );
   }

   public static class DummyLayout extends EditorFacade.EditorLayout {
      public DummyLayout(EditorFacade.EditorLayout parent) {
         super(Translatable.literal("Dummy"), parent);
      }

      @Override
      public void onPrepare(ContainerView view) {
      }
   }
}
