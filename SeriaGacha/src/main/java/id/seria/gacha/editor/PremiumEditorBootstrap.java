package id.seria.gacha.editor;

import id.seria.gacha.editor.layouts.EditorAddonsLayout;
import id.seria.gacha.editor.layouts.crate.EditorCrateItemLayout;
import id.seria.gacha.editor.layouts.crate.cost.EditorCrateExcellentEconomyCostMenu;
import id.seria.gacha.editor.layouts.crate.rerolls.EditorCrateRerollingLayout;
import id.seria.gacha.editor.layouts.crate.rewards.EditorCrateAddMultipleRewardsLayout;
import id.seria.gacha.editor.layouts.crate.rewards.EditorCrateRarityGroupLayout;
import id.seria.gacha.limbo.PremiumEditorKeys;
import id.seria.gacha.registries.EditorRegistry;

public final class PremiumEditorBootstrap {
   private PremiumEditorBootstrap() {
   }

   public static void registerEditors() {
      EditorRegistry.register(PremiumEditorKeys.ADDONS_MARKETPLACE, EditorAddonsLayout::new);
      EditorRegistry.register(PremiumEditorKeys.ITEM_EDITOR, EditorCrateItemLayout::new);
      EditorRegistry.register(PremiumEditorKeys.REROLLING, EditorCrateRerollingLayout::new);
      EditorRegistry.register(PremiumEditorKeys.EXCELLENT_ECONOMY_COST, EditorCrateExcellentEconomyCostMenu::new);
      EditorRegistry.register(PremiumEditorKeys.ADD_MULTIPLE_REWARDS, EditorCrateAddMultipleRewardsLayout::new);
      EditorRegistry.registerRarityGroup(PremiumEditorKeys.RARITY_GROUP, EditorCrateRarityGroupLayout::new);
   }
}
