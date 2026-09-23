package id.seria.gacha.editor.layouts.crate.animations.opening.data;

import com.google.common.collect.Lists;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.inventory.CustomInventory;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.ColorUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.menus.SelectOptionMenu;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.opening.data.ColoredOpeningPhaseData;
import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

@Deprecated
public class ColoredPhaseDataBuilderMenu extends CustomInventory {
   private final CrateType crateType;
   private final ColoredOpeningPhaseData initialData;
   private final Consumer<Player> onCancel;
   private final Consumer<ColoredOpeningPhaseData> onCreate;
   private boolean ignoreNextInventoryClose;

   public ColoredPhaseDataBuilderMenu(
      PhoenixCrates plugin, CrateType crateType, ColoredOpeningPhaseData initialData, Consumer<Player> onCancel, Consumer<ColoredOpeningPhaseData> onCreate
   ) {
      super(plugin, Translations.t("editor.titles.crate.opening-animation", "%crate%", StringUtil.humanize(crateType.getIdentifier()))[0], 4);
      this.crateType = crateType;
      this.initialData = initialData;
      this.onCancel = onCancel;
      this.onCreate = onCreate;
   }

   @Override
   public void setup(Player player, Inventory inventory) {
      ColoredPhaseDataBuilderMenu.EditorColoredOpeningPhaseHolder holder = (ColoredPhaseDataBuilderMenu.EditorColoredOpeningPhaseHolder)inventory.getHolder();

      for (int i = 1; i <= 4; i++) {
         this.setItem(i, 4, ItemBuilder.of(XMaterial.RED_STAINED_GLASS_PANE).setDisplayName("\u00a7cCancel"), __ -> {
            player.closeInventory();
            this.onCancel.accept(player);
         });
      }

      this.setItem(5, 4, ItemBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE).setDisplayName("\u00a7r"));

      for (int i = 6; i <= 9; i++) {
         this.setItem(i, 4, ItemBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE).setDisplayName("\u00a7aCreate"), __ -> {
            player.closeInventory();
            this.onCreate.accept(new ColoredOpeningPhaseData(holder.getColor()));
         });
      }

      this.setItem(
         5,
         2,
         ItemBuilder.of(XMaterial.WHITE_DYE.or(XMaterial.BONE_MEAL))
            .setDisplayName("\u00a7aColor")
            .setLore(
               "\u00a77By clicking here you will be able",
               "\u00a77to change the color of this phase",
               "",
               "\u00a7fColor: "
                  + this.applyHexColor(holder.color)
                  + ColorUtil.getColorDisplayName(holder.color)
                  + " \u00a77("
                  + ColorUtil.getHexColor(holder.color)
                  + ")",
               "",
               "\u00a76Left-Click: \u00a7eEdit color."
            ),
         __ -> {
            List<SelectOptionMenu.OptionItem<Color>> options = Lists.newArrayList();

            for (Color color : ColorUtil.getColors()) {
               options.add(
                  new SelectOptionMenu.OptionItem<>(
                     color,
                     XMaterial.BOOK,
                     Translations.t("editor.edit-standby-effect.color-select.title", "%name%", this.applyHexColor(color) + ColorUtil.getColorDisplayName(color))[0],
                     Translations.t("editor.edit-standby-effect.color-select.description")
                  )
               );
            }

            SelectOptionMenu<Color> optionSelector = new SelectOptionMenu<>(this.plugin, this.getTitle(), options, object -> {
               holder.setColor(object);
               this.open(player, holder);
            });
            optionSelector.setBackConsumer(event -> this.open(player, holder));
            this.ignoreNextInventoryClose = true;
            optionSelector.open(player);
         }
      );
   }

   private String applyHexColor(Color color) {
      return Utilities.getNMSFactory().translateColorCodes("&" + ColorUtil.getHexColor(color));
   }

   @Override
   public void open(Player player) {
      this.open(player, new ColoredPhaseDataBuilderMenu.EditorColoredOpeningPhaseHolder(this, this.initialData));
   }

   @EventHandler
   @Override
   public void onMenuClose(InventoryCloseEvent event) {
      if (this.ignoreNextInventoryClose) {
         this.ignoreNextInventoryClose = false;
      } else {
         Player player = (Player)event.getPlayer();
         this.onCancel.accept(player);
      }
   }

   public static class EditorColoredOpeningPhaseHolder extends CustomInventory.PaginationWrapper {
      private Color color = Color.WHITE;

      public EditorColoredOpeningPhaseHolder(CustomInventory sustainer, ColoredOpeningPhaseData data) {
         super(sustainer);
         if (data != null) {
            this.color = data.getColor();
         }
      }

      @Generated
      public Color getColor() {
         return this.color;
      }

      @Generated
      public void setColor(Color color) {
         this.color = color;
      }
   }
}
