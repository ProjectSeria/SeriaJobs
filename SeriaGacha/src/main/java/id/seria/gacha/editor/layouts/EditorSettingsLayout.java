package id.seria.gacha.editor.layouts;

import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.sdk.locales.Locale;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.internal.language.Language;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableListOption;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableBoolean;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers.ObjectModifiers;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.ArrayList;
import java.util.Arrays;

public class EditorSettingsLayout extends EditorFacade.EditorLayout {
   public EditorSettingsLayout(EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.settings"), parent);
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         new EditableListOption<>(
               SlotCompound.from(2, 2),
               this.createData(XMaterial.BOOK, Translatable.key("editor.settings.language.title"), Translatable.key("editor.settings.language.description")),
               Arrays.asList(Language.values()),
               Crates.getConfiguration().getLanguage()
            )
            .setItemFormatter(language -> Translatable.literal(language.getLocalName()))
            .setItemSelection(
               language -> language != Language.UNDEFINED
                  && Translations.getProvider().hasTranslation(new Locale(language.getSymbol(), language.getLocalName()))
            )
            .setOnOptionChange(value -> {
               Crates.getConfiguration().setLanguage(value);
               this.getPlugin().getConfigurationWorker().queueSave();

               try {
                  PhoenixCrates.getLegacyInstance().switchLanguage(value);
                  view.updateView();
               } catch (Exception e) {
                  Translations.error(view.getViewer(), e);
                  view.getViewer().closeInventory();
               }
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(3, 2),
               this.createData(
                  XMaterial.WIND_CHARGE.or(XMaterial.LEAD),
                  Translatable.key("editor.settings.crate-knockback.title"),
                  Translatable.key("editor.settings.crate-knockback.description")
               ),
               this.label("multiplier"),
               Crates.getConfiguration().getCratesKnockback()
            )
            .addModifier(ObjectModifiers.POSITIVE_NUMBER)
            .setOnValueChange(value -> {
               Crates.getConfiguration().setCratesKnockback(value.doubleValue());
               this.getPlugin().getConfigurationWorker().queueSave();
            })
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(5, 2),
               this.createData(
                  XMaterial.TRIPWIRE_HOOK, Translatable.key("editor.settings.key-mainhand.title"), Translatable.key("editor.settings.key-mainhand.description")
               ),
               this.label("enabled"),
               Crates.getConfiguration().isMainHandKey()
            )
            .setOnValueChange(value -> {
               Crates.getConfiguration().setMainHandKey(value);
               this.getPlugin().getConfigurationWorker().queueSave();
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(7, 2),
               this.createData(
                  XMaterial.ENDER_EYE,
                  Translatable.key("editor.settings.crate-despawn-range.title"),
                  Translatable.key("editor.settings.crate-despawn-range.description")
               ),
               this.label("range"),
               Crates.getConfiguration().getCratesRenderRadius()
            )
            .addModifier(ObjectModifiers.POSITIVE_NUMBER)
            .setOnValueChange(value -> {
               Crates.getConfiguration().setCratesRenderRadius(value.intValue());
               this.getPlugin().getConfigurationWorker().queueSave();

               try {
                  Crates.getCratesManager().reload();
               } catch (Exception e) {
                  Translations.error(view.getViewer(), e);
               }
            })
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(8, 2),
               this.createData(
                  XMaterial.FEATHER,
                  Translatable.key("editor.settings.crate-quick-open.title"),
                  Translatable.key("editor.settings.crate-quick-open.description")
               ),
               this.label("enabled"),
               Crates.getConfiguration().isCratesQuickOpen()
            )
            .setOnValueChange(value -> {
               Crates.getConfiguration().setCratesQuickOpen(value);
               this.getPlugin().getConfigurationWorker().queueSave();
            })
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(5, 3),
               this.createData(
                  XMaterial.BARRIER,
                  Translatable.key("editor.settings.disable-confirmation-menus.title"),
                  Translatable.key("editor.settings.disable-confirmation-menus.description")
               ),
               this.label("enabled"),
               Crates.getConfiguration().isDisableConfirmationMenus()
            )
            .setOnValueChange(value -> {
               Crates.getConfiguration().setDisableConfirmationMenus(value);
               this.getPlugin().getConfigurationWorker().queueSave();
            })
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
      this.addComponent(
         view,
         this.createComponent(
            SlotCompound.from(8, 3), this.createData(XMaterial.GRAY_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
         )
      );
   }
}
