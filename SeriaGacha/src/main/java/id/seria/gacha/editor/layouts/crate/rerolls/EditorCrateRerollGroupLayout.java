package id.seria.gacha.editor.layouts.crate.rerolls;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableString;
import id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers.ObjectModifiers;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.rerolling.RerollGroup;
import lombok.Generated;
import org.bukkit.entity.Player;

public class EditorCrateRerollGroupLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;
   private final RerollGroup rerollGroup;

   public EditorCrateRerollGroupLayout(CrateType crateType, RerollGroup rerollGroup, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.rerolling"), parent);
      this.crateType = crateType;
      this.rerollGroup = rerollGroup;
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(4, 2),
               this.createData(
                  XMaterial.NAME_TAG,
                  Translatable.literal("Namespace"),
                  Translatable.literal(
                     "&7By clicking here you will be able", "&7to define the namespace of this permission.", "", "&fEx: &ephoenixcrates.reroll.your_namespace"
                  )
               ),
               this.rerollGroup.getNamespace()
            )
            .addValidator(input -> !input.replace(" ", "").isEmpty(), Translatable.key("editor.errors.invalid-input"))
            .addValidator(input -> !input.contains(" "), Translatable.key("editor.errors.no-spaces"))
            .addModifier(input -> ObjectModifiers.ID_NORMALIZER(true).process(input))
            .setOnValueChange(value -> {
               this.rerollGroup.setNamespace(value);
               this.save(view);
            })
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(6, 2),
               this.createData(
                  XMaterial.NETHER_STAR,
                  Translatable.literal("&aRerolls"),
                  Translatable.literal("&7By clicking here you are able to", "&7define how many rerolls a player", "&7in this group have.")
               ),
               this.label("rerolls"),
               this.rerollGroup.getRerolls()
            )
            .addModifier(ObjectModifiers.GREATHER_THEN_ONE)
            .setOnValueChange(value -> {
               this.rerollGroup.setRerolls(value.intValue());
               this.save(view);
            })
      );
   }

   private void save(ContainerView view) {
      Player player = view.getViewer();

      try {
         CratesManager manager = Crates.getCratesManager();
         if (this.crateType.isRegistered()) {
            manager.saveCrateType(this.crateType);
         } else {
            manager.registerCrateType(this.crateType, true);
         }
      } catch (Exception e) {
         Translations.error(player, e);
         player.closeInventory();
      }
   }

   @Generated
   public CrateType getCrateType() {
      return this.crateType;
   }

   @Generated
   public RerollGroup getRerollGroup() {
      return this.rerollGroup;
   }
}
