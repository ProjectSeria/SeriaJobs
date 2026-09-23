package id.seria.gacha.editor.layouts.crate.cost;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.SimpleComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableString;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.menus.SelectOptionMenu;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.cost.CostEngineType;
import id.seria.gacha.sdk.hooks.excellenteconomy.ExcellentEconomyHook;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.entity.Player;

public class EditorCrateExcellentEconomyCostMenu extends EditorFacade.EditorLayout {
   private final CrateType crateType;

   public EditorCrateExcellentEconomyCostMenu(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.open-cost"), parent);
      this.crateType = crateType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(4, 2),
               this.createData(
                  XMaterial.GOLD_INGOT,
                  Translatable.key("editor.crate.open-cost-amount.title"),
                  Translatable.key("editor.crate.open-cost-amount.description", "%amount%", this.formatAmount())
               ),
               this.formatAmount()
            )
            .addValidator(NumberUtils::isNumber, Translatable.key("editor.errors.invalid-input"))
            .setOnValueChange(value -> {
               this.crateType.setOpenMoneyCost(this.normalizeAmount(Math.max(0.0, Double.parseDouble(value))));
               this.save(view.getViewer());
            })
      );
      List<String> currencyDescription = Lists.newArrayList(this.t(Translatable.key("editor.crate.open-cost-currency.description")));
      currencyDescription.add("");
      currencyDescription.add("\u00c2\u00a7fCurrency: \u00c2\u00a77" + this.currencyDisplay());
      this.addComponent(
         view,
         new SimpleComponent(
               SlotCompound.from(6, 2),
               this.createData(XMaterial.SUNFLOWER, Translatable.key("editor.crate.open-cost-currency.title"), Translatable.literal(currencyDescription))
            )
            .setClickAction(
               new ClickAction(ClickAction.ClickType.LEFT, this.label("select")),
               context -> {
                  List<String> currencyIds = PhoenixCrates.getLegacyInstance()
                     .getHookRegistry()
                     .getHook(ExcellentEconomyHook.class)
                     .getCurrencyIds()
                     .stream()
                     .sorted(String::compareToIgnoreCase)
                     .collect(Collectors.toList());
                  List<SelectOptionMenu.OptionItem<String>> options = Lists.newArrayList();

                  for (String currencyId : currencyIds) {
                     options.add(
                        new SelectOptionMenu.OptionItem<>(
                           currencyId,
                           XMaterial.SUNFLOWER,
                           Translations.r("&a%name%", "%name%", currencyId),
                           Translations.r(Lists.newArrayList(new String[]{"", "&6Left-Click: &eSelect currency."}))
                        )
                     );
                  }

                  SelectOptionMenu<String> selector = new SelectOptionMenu<>(
                     this.plugin, this.t("editor.titles.crate.open-cost")[0], options, 3, currencyIdx -> {
                        this.crateType.setOpenCostCurrency(currencyIdx);
                        this.save(context.getViewer());
                     }
                  );
                  selector.setBackConsumer(__ -> this.open(context.getViewer()));
                  selector.open(context.getViewer());
               }
            )
      );
   }

   private String formatAmount() {
      CostEngineType costEngine = this.crateType.getOpenCostEngine();
      double amount = this.crateType.getOpenMoneyCost();
      return costEngine.isWholeNumber(this.crateType.getOpenCostCurrency()) ? String.valueOf((long)amount) : String.valueOf(amount);
   }

   private double normalizeAmount(double amount) {
      CostEngineType costEngine = this.crateType.getOpenCostEngine();
      return costEngine.isWholeNumber(this.crateType.getOpenCostCurrency()) ? Math.floor(amount) : amount;
   }

   private String currencyDisplay() {
      String currencyId = this.crateType.getOpenCostCurrency();
      return currencyId != null && !currencyId.isEmpty() ? currencyId : "None";
   }

   private void save(Player player) {
      try {
         CratesManager manager = Crates.getCratesManager();
         if (this.crateType.isRegistered()) {
            manager.saveCrateType(this.crateType);
         } else {
            manager.registerCrateType(this.crateType, true);
         }

         if (this.getViewersContext().containsKey(player.getUniqueId())) {
            this.updateView(player);
         } else {
            this.open(player);
         }
      } catch (Exception e) {
         Translations.error(player, e);
         player.closeInventory();
      }
   }
}
