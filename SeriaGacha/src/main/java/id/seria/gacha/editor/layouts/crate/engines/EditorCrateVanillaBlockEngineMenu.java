package id.seria.gacha.editor.layouts.crate.engines;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit.EditableMaterial;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableBoolean;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.objects.Base64;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.engine.vanilla_block.VanillaBlockEngineData;
import id.seria.gacha.managers.crates.type.CrateBlockType;
import java.net.MalformedURLException;
import java.net.URL;

public class EditorCrateVanillaBlockEngineMenu extends EditorFacade.EditorLayout {
   private final CrateType crateType;

   public EditorCrateVanillaBlockEngineMenu(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.empty(), parent);
      this.crateType = crateType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      view.setCancelOnOtherInventoryClick(false);
      this.addComponent(
         view,
         new EditableMaterial(
               SlotCompound.from(4, 2),
               this.createData(
                  this.crateType.getBlockMaterial(),
                  Translatable.key("editor.crate.display-settings.block-material.title"),
                  Translatable.key("editor.crate.display-settings.block-material.description")
               ),
               Constants.AVAILABLE_MATERIALS,
               this.crateType.getBlockMaterial()
            )
            .setClickAction(new ClickAction(ClickAction.ClickType.RIGHT, this.label("update-block")), click -> {
               for (Crate crate : Crates.getCratesManager().getCrates().values()) {
                  crate.unload(false);
                  crate.load(true);
               }
            })
            .setOnValueChange(
               value -> {
                  if (value.equals(XMaterial.PLAYER_HEAD)) {
                     Translations.send(view.getViewer(), "\u00a7aWrite the minecraft texture url/url id or base64 in the chat.");
                     Translations.send(view.getViewer(), "\u00a7aIf you are using texture url id make sure you put a \u00a76\"-\" \u00a7aon the start.");
                     Translations.send(view.getViewer(), "\u00a7aExample: \u00a76-\u00a7e28dc3c84491c4ff103de6ea01880e2d83432cd4878b84d6b43253cc51fd978e7");
                     Crates.getScheduler().runSync(view.getViewer(), () -> view.getViewer().closeInventory(), 5L);
                     TextReader.askForInput(
                        view.getViewer(),
                        input -> {
                           if (input != null) {
                              if (input.replace(" ", "").isEmpty()) {
                                 Translations.send(view.getViewer(), this.t("editor.errors.invalid-input"));
                              } else {
                                 if (input.startsWith("-")) {
                                    this.crateType
                                       .setBlockType(
                                          new CrateBlockType(
                                             XMaterial.PLAYER_HEAD,
                                             Utilities.getNMSFactory()
                                                .convertTextureUrlToBase64("https://textures.minecraft.net/texture/" + input.substring(1, input.length()))
                                          )
                                       );
                                 } else if (Base64.isValid(input)) {
                                    this.crateType.setBlockType(new CrateBlockType(XMaterial.PLAYER_HEAD, input));
                                 } else {
                                    if (this.isMalformedUrl(input)) {
                                       Translations.send(view.getViewer(), "\u00a7cInvalid input.");
                                       this.open(view);
                                       return;
                                    }

                                    this.crateType
                                       .setBlockType(new CrateBlockType(XMaterial.PLAYER_HEAD, Utilities.getNMSFactory().convertTextureUrlToBase64(input)));
                                 }

                                 EditorFacade.saveCrate(view, this.crateType);
                                 this.updateBlocks();
                              }
                           }

                           this.open(view);
                        }
                     );
                  } else {
                     this.crateType.setBlockType(new CrateBlockType(value));
                     EditorFacade.saveCrate(view, this.crateType);
                     this.updateBlocks();
                  }
               }
            )
      );
      VanillaBlockEngineData engineData = (VanillaBlockEngineData)this.crateType.getEngineData();
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(6, 2),
               this.createData(
                  XMaterial.LIME_DYE,
                  Translatable.key("editor.crate.display-settings.block-opening-animation.title"),
                  Translatable.key("editor.crate.display-settings.block-opening-animation.description")
               ),
               this.label("enabled"),
               engineData.isBlockOpeningAnimationEnabled()
            )
            .setOnValueChange(value -> {
               engineData.setBlockOpeningAnimationEnabled(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
   }

   @Override
   public Translatable getTitle() {
      return this.crateType.isRegistered() ? Translatable.key("editor.titles.crate.show") : Translatable.key("editor.titles.crate.new");
   }

   private void updateBlocks() {
      for (Crate crate : Crates.getCratesManager().getCratesByType(this.crateType)) {
         crate.unload(false);
         crate.load(true);
      }
   }

   private boolean isMalformedUrl(String input) {
      try {
         new URL(input);
         return false;
      } catch (MalformedURLException e) {
         return true;
      }
   }
}
