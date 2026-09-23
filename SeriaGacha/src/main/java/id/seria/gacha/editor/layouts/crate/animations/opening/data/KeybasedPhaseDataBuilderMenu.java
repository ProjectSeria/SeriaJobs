package id.seria.gacha.editor.layouts.crate.animations.opening.data;

import com.google.common.collect.Lists;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.utils.inventory.CustomInventory;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.opening.data.KeybasedOpeningPhaseData;
import java.util.List;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

@Deprecated
public class KeybasedPhaseDataBuilderMenu extends CustomInventory {
   private final CrateType crateType;
   private final KeybasedOpeningPhaseData initialData;
   private final Consumer<Player> onCancel;
   private final Consumer<KeybasedOpeningPhaseData> onCreate;

   public KeybasedPhaseDataBuilderMenu(
      PhoenixCrates plugin, CrateType crateType, KeybasedOpeningPhaseData initialData, Consumer<Player> onCancel, Consumer<KeybasedOpeningPhaseData> onCreate
   ) {
      super(plugin, Translations.t("editor.titles.crate.opening-animation", "%crate%", StringUtil.humanize(crateType.getIdentifier()))[0], 4);
      this.crateType = crateType;
      this.initialData = initialData;
      this.onCancel = onCancel;
      this.onCreate = onCreate;
   }

   @Override
   public void setup(Player player, Inventory inventory) {
      KeybasedPhaseDataBuilderMenu.EditorKeybasedOpeningPhaseHolder holder = (KeybasedPhaseDataBuilderMenu.EditorKeybasedOpeningPhaseHolder)inventory.getHolder();

      for (int i = 1; i <= 4; i++) {
         this.setItem(i, 4, ItemBuilder.of(XMaterial.RED_STAINED_GLASS_PANE).setDisplayName("\u00a7cCancel"), __ -> this.onCancel.accept(player));
      }

      this.setItem(5, 4, ItemBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE).setDisplayName("\u00a7r"));

      for (int i = 6; i <= 9; i++) {
         this.setItem(
            i,
            4,
            ItemBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE).setDisplayName("\u00a7aCreate"),
            __ -> this.onCreate
               .accept(
                  new KeybasedOpeningPhaseData(holder.isUseKeyAsModel(), holder.getAlignment(), holder.getOffsetX(), holder.getOffsetY(), holder.getOffsetZ())
               )
         );
      }

      this.setItem(
         3,
         2,
         ItemBuilder.of(XMaterial.TRIPWIRE_HOOK)
            .setDisplayName("\u00a7aUse Key as Model")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "\u00a77This option allows you to define if the",
                        "\u00a77key itemstack will be used on this animation.",
                        "",
                        "\u00a7fIs Enabled: \u00a77%state%",
                        "",
                        "\u00a76Left-Click: \u00a7eSwitch state."
                     }
                  ),
                  "%state%",
                  holder.isUseKeyAsModel() ? "\u00a7a" + Translations.t("labels.yes-word")[0] : "\u00a7c" + Translations.t("labels.no-word")[0]
               )
            ),
         event -> {
            holder.useKeyAsModel = !holder.useKeyAsModel;
            this.updateInventorySync(player);
         }
      );
      List<String> displayedAlignments = Lists.newArrayList();

      for (KeybasedOpeningPhaseData.KeyAlignment target : KeybasedOpeningPhaseData.KeyAlignment.values()) {
         if (holder.alignment.equals(target)) {
            displayedAlignments.add("\u00a7a\u25aa " + StringUtil.humanize(target));
         } else {
            displayedAlignments.add("\u00a77\u25ab " + StringUtil.humanize(target));
         }
      }

      this.setItem(
         4,
         2,
         ItemBuilder.of(XMaterial.STRING)
            .setDisplayName("\u00a7aKey Alignment")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "\u00a77By clicking here you will be able",
                        "\u00a77change the key alignment direction",
                        "\u00a77that this animation will use",
                        "",
                        "\u00a7fAlignments:",
                        "%alignments%",
                        "",
                        "\u00a76Left-Click: \u00a7eSwap alignment."
                     }
                  ),
                  "%alignments%",
                  displayedAlignments
               )
            ),
         event -> {
            holder.alignment = KeybasedOpeningPhaseData.KeyAlignment.values()[(holder.alignment.ordinal() + 1)
               % KeybasedOpeningPhaseData.KeyAlignment.values().length];
            this.updateInventorySync(player);
         }
      );
      this.setItem(
         5,
         2,
         ItemBuilder.of(XMaterial.GUNPOWDER)
            .setDisplayName("\u00a7aOffset X")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "\u00a77This option allows you to adjust the",
                        "\u00a77key position based on the crate location.",
                        "",
                        "\u00a7fCurrent: \u00a77%offset%",
                        "",
                        "\u00a76Left-Click: \u00a7eAdd 0.1.",
                        "\u00a76Shift + Left-Click: \u00a7eAdd 1.",
                        "\u00a76Right-Click: \u00a7eRemove 0.1.",
                        "\u00a76Shift + Right-Click: \u00a7eRemove 1."
                     }
                  ),
                  "%offset%",
                  Constants.DECIMAL1_FORMAT.format(holder.offsetX)
               )
            ),
         event -> {
            double offset = holder.offsetX;
            if (event.isLeftClick()) {
               offset += event.isShiftClick() ? 1.0 : 0.1;
            } else if (event.isRightClick()) {
               offset -= event.isShiftClick() ? 1.0 : 0.1;
            }

            holder.offsetX = MathUtil.clamp(offset, -5.0, 5.0);
            this.updateInventorySync(player);
         }
      );
      this.setItem(
         6,
         2,
         ItemBuilder.of(XMaterial.GUNPOWDER)
            .setDisplayName("\u00a7aOffset Y")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "\u00a77This option allows you to adjust the",
                        "\u00a77key position based on the crate location.",
                        "",
                        "\u00a7fCurrent: \u00a77%offset%",
                        "",
                        "\u00a76Left-Click: \u00a7eAdd 0.1.",
                        "\u00a76Shift + Left-Click: \u00a7eAdd 1.",
                        "\u00a76Right-Click: \u00a7eRemove 0.1.",
                        "\u00a76Shift + Right-Click: \u00a7eRemove 1."
                     }
                  ),
                  "%offset%",
                  Constants.DECIMAL1_FORMAT.format(holder.offsetY)
               )
            ),
         event -> {
            double offset = holder.offsetY;
            if (event.isLeftClick()) {
               offset += event.isShiftClick() ? 1.0 : 0.1;
            } else if (event.isRightClick()) {
               offset -= event.isShiftClick() ? 1.0 : 0.1;
            }

            holder.offsetY = MathUtil.clamp(offset, -5.0, 5.0);
            this.updateInventorySync(player);
         }
      );
      this.setItem(
         7,
         2,
         ItemBuilder.of(XMaterial.GUNPOWDER)
            .setDisplayName("\u00a7aOffset Z")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "\u00a77This option allows you to adjust the",
                        "\u00a77key position based on the crate location.",
                        "",
                        "\u00a7fCurrent: \u00a77%offset%",
                        "",
                        "\u00a76Left-Click: \u00a7eAdd 0.1.",
                        "\u00a76Shift + Left-Click: \u00a7eAdd 1.",
                        "\u00a76Right-Click: \u00a7eRemove 0.1.",
                        "\u00a76Shift + Right-Click: \u00a7eRemove 1."
                     }
                  ),
                  "%offset%",
                  Constants.DECIMAL1_FORMAT.format(holder.offsetZ)
               )
            ),
         event -> {
            double offset = holder.offsetZ;
            if (event.isLeftClick()) {
               offset += event.isShiftClick() ? 1.0 : 0.1;
            } else if (event.isRightClick()) {
               offset -= event.isShiftClick() ? 1.0 : 0.1;
            }

            holder.offsetZ = MathUtil.clamp(offset, -5.0, 5.0);
            this.updateInventorySync(player);
         }
      );
   }

   @Override
   public void open(Player player) {
      this.open(player, new KeybasedPhaseDataBuilderMenu.EditorKeybasedOpeningPhaseHolder(this, this.initialData));
   }

   @EventHandler
   @Override
   public void onMenuClose(InventoryCloseEvent event) {
      Player player = (Player)event.getPlayer();
      this.onCancel.accept(player);
   }

   public static class EditorKeybasedOpeningPhaseHolder extends CustomInventory.PaginationWrapper {
      protected boolean useKeyAsModel = false;
      protected KeybasedOpeningPhaseData.KeyAlignment alignment = KeybasedOpeningPhaseData.KeyAlignment.HORIZONTAL;
      protected double offsetX = 0.0;
      protected double offsetY = 0.0;
      protected double offsetZ = 0.0;

      public EditorKeybasedOpeningPhaseHolder(CustomInventory sustainer, KeybasedOpeningPhaseData data) {
         super(sustainer);
         if (data != null) {
            this.useKeyAsModel = data.isUseKeyAsModel();
            this.alignment = data.getAlignment();
            this.offsetX = data.getOffsetX();
            this.offsetY = data.getOffsetY();
            this.offsetZ = data.getOffsetZ();
         }
      }

      @Generated
      public boolean isUseKeyAsModel() {
         return this.useKeyAsModel;
      }

      @Generated
      public KeybasedOpeningPhaseData.KeyAlignment getAlignment() {
         return this.alignment;
      }

      @Generated
      public double getOffsetX() {
         return this.offsetX;
      }

      @Generated
      public double getOffsetY() {
         return this.offsetY;
      }

      @Generated
      public double getOffsetZ() {
         return this.offsetZ;
      }

      @Generated
      public void setUseKeyAsModel(boolean useKeyAsModel) {
         this.useKeyAsModel = useKeyAsModel;
      }

      @Generated
      public void setAlignment(KeybasedOpeningPhaseData.KeyAlignment alignment) {
         this.alignment = alignment;
      }

      @Generated
      public void setOffsetX(double offsetX) {
         this.offsetX = offsetX;
      }

      @Generated
      public void setOffsetY(double offsetY) {
         this.offsetY = offsetY;
      }

      @Generated
      public void setOffsetZ(double offsetZ) {
         this.offsetZ = offsetZ;
      }
   }
}
