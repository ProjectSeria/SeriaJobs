package id.seria.gacha.editor.layouts.crate.animations.idle;

import id.seria.gacha.Crates;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.nms.interfaces.particles.PropertyType;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.inventory.CustomInventory;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.ColorUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectData;
import java.awt.Color;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Deprecated
public class EditorCrateIdleAnimation extends CustomInventory {
   private final CrateType crateType;
   private final EditorFacade.EditorLayout parent;

   public EditorCrateIdleAnimation(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(parent.getPlugin(), Translations.t("editor.titles.crate.idle-animation", "%crate%", StringUtil.humanize(crateType.getIdentifier()))[0], 4);
      this.crateType = crateType;
      this.parent = parent;
   }

   @Override
   public void setup(Player player, Inventory inventory) {
      CustomInventory.PaginationWrapper holder = (CustomInventory.PaginationWrapper)inventory.getHolder();
      List<IdleEffect> records = this.crateType.getIdleEffects();
      int maxItemsPerPage = 7;
      int maxPages = (int)Math.ceil((records.size() + 1) / 7.0);
      ItemStack glass = ItemBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE).setDisplayName("\u00a72").build();
      this.fillBottomBorder(glass);
      this.setItem(5, 4, ItemBuilder.of(XMaterial.FEATHER).setDisplayName("\u00a76" + Translations.t("labels.go-back")[0]), __ -> this.parent.open(player));
      if (holder.getCurrentPage() != 1) {
         int previousPage = holder.getCurrentPage() - 1;
         this.setItem(
            1,
            2,
            ItemBuilder.of(XMaterial.ARROW)
               .setDisplayName(Translations.t("layout.extras.previous-page.title", "%page%", previousPage)[0])
               .setLore(Translations.t("layout.extras.previous-page.description")),
            __ -> {
               holder.setCurrentPage(holder.getCurrentPage() - 1);
               this.updateInventorySync(player);
            }
         );
      }

      if (holder.getCurrentPage() != maxPages && maxPages != 1) {
         int nextPage = holder.getCurrentPage() + 1;
         this.setItem(
            9,
            2,
            ItemBuilder.of(XMaterial.ARROW)
               .setDisplayName(Translations.t("layout.extras.next-page.title", "%page%", nextPage)[0])
               .setLore(Translations.t("layout.extras.next-page.description")),
            __ -> {
               holder.setCurrentPage(holder.getCurrentPage() + 1);
               this.updateInventorySync(player);
            }
         );
      }

      int i = (holder.getCurrentPage() - 1) * 7;

      for (int x = 2; x <= 8; x++) {
         if (i >= records.size()) {
            if (i == records.size()) {
               this.setItem(
                  x,
                  2,
                  ItemBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE)
                     .setDisplayName(Translations.t("editor.edit-standby-animation.add-effect.title")[0])
                     .setLore(Translations.t("editor.edit-standby-animation.add-effect.description")),
                  __ -> new EditorEditCrateEffectAnimation(this.crateType, null, this).open(player)
               );
            }
            break;
         }

         IdleEffect effect = records.get(i);
         IdleEffectData data = effect.getData();
         this.setItem(
            x,
            2,
            ItemBuilder.of(XMaterial.NAME_TAG)
               .setDisplayName(Translations.t("editor.edit-standby-animation.edit-effect.title", "%id%", i + 1)[0])
               .setLore(
                  Translations.t(
                     "editor.edit-standby-animation.edit-effect.description",
                     "%effect_type%",
                     StringUtil.humanize(effect.getType()),
                     "%particle_type%",
                     StringUtil.humanize(data.getParticle()),
                     "%particle_velocity%",
                     Constants.DECIMAL1_FORMAT.format(data.getVelocity()),
                     "%color%",
                     data.getParticle().hasProperty(PropertyType.COLORABLE)
                        ? this.getHexColor(data.getColor()) + ColorUtil.getColorDisplayName(data.getColor())
                        : Translations.t("labels.not-colorable")[0],
                     "%offset%",
                     Constants.DECIMAL1_FORMAT.format(data.getOffsetX())
                        + ", "
                        + Constants.DECIMAL1_FORMAT.format(data.getOffsetY())
                        + ", "
                        + Constants.DECIMAL1_FORMAT.format(data.getOffsetZ()),
                     "%spread%",
                     Constants.DECIMAL1_FORMAT.format(data.getSpread()),
                     "%amount%",
                     data.getAmount()
                  )
               ),
            event -> {
               if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                  try {
                     this.crateType.getIdleEffects().remove(effect);
                     Crates.getCratesManager().saveCrateType(this.crateType);
                     this.updateInventorySync(player);
                  } catch (Exception e) {
                     Translations.error(player, e);
                     player.closeInventory();
                  }
               } else {
                  new EditorEditCrateEffectAnimation(this.crateType, effect, this).open(player);
               }
            }
         );
         i++;
      }
   }

   private String getHexColor(Color color) {
      return Utilities.getNMSFactory().translateColorCodes("&" + ColorUtil.getHexColor(color));
   }

   @Override
   public void open(Player player) {
      this.open(player, new EditorFacade.EditorPaginationWrapper(this));
   }
}
