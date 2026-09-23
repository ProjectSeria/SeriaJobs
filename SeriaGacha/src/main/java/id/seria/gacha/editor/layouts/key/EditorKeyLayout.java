package id.seria.gacha.editor.layouts.key;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.facades.KeyFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.LinkedComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.NavigableComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.SimpleComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit.EditableItem;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableBoolean;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableString;
import id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers.ObjectModifiers;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.limbo.menus.ConfirmationMenu;
import id.seria.gacha.managers.keys.Key;
import java.util.ArrayList;
import java.util.Set;
import lombok.Generated;
import org.bukkit.entity.Player;

public class EditorKeyLayout extends EditorFacade.EditorLayout {
   private final Key key;

   public EditorKeyLayout(Key key, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.key.show"), parent);
      this.key = key;
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(2, 2),
               this.createData(
                  XMaterial.NAME_TAG, Translatable.key("editor.keys.key.identifier.title"), Translatable.key("editor.keys.key.identifier.description")
               ),
               this.key.getIdentifier()
            )
            .addValidator(input -> !input.replace(" ", "").isEmpty(), Translatable.key("editor.errors.invalid-input"))
            .addValidator(input -> !input.contains(" "), Translatable.key("editor.errors.no-spaces"))
            .addModifier(input -> ObjectModifiers.ID_NORMALIZER.process(input))
            .addValidator(input -> Crates.getKeysManager().getKeyByIdentifier(input) == null, Translatable.key("editor.errors.identifier-already-in-use"))
            .setOnValueChange(value -> {
               this.key.setIdentifier(value);
               EditorFacade.saveKey(view, this.key);
            })
      );
      this.addComponent(
         view,
         new EditableItem(
               SlotCompound.from(3, 2),
               this.createData(
                  this.key.getPlainItem().build(), Translatable.key("editor.keys.key.item.title"), Translatable.key("editor.keys.key.item.description")
               ),
               this.key.getPlainItem().build()
            )
            .setClickAction(
               ClickAction.EDIT(ClickAction.ClickType.LEFT, Translatable.key("labels.item")),
               (object, context) -> new EditorKeyDisplayItemLayout(this.key, this).open(context.getViewer())
            )
            .setOnValueChange(value -> {
               this.key.setPlainItem(BaseItem.from(value));
               EditorFacade.saveKey(view, this.key);
            })
      );
      this.addComponent(
         view,
         new NavigableComponent(
               SlotCompound.from(5, 2),
               this.createData(
                  XMaterial.CHEST, Translatable.key("editor.keys.key.link-crates.title"), Translatable.key("editor.keys.key.link-crates.description")
               ),
               new EditorKeyLinkedCratesLayout(this.key, this)
            )
            .addAttribute(this.label("crates-linked"), KeyFacade.getLinkedCrates(this.key).size())
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(7, 2),
               this.createData(
                  XMaterial.ENCHANTED_BOOK,
                  Translatable.literal("\u00a7aGlowing Key"),
                  Translatable.literal("&7By clicking here you will be able", "&7to add glowing effect on the key.")
               ),
               this.label("enabled"),
               this.key.isGlowing()
            )
            .setOnValueChange(value -> {
               this.key.setGlowing(value);
               EditorFacade.saveKey(view, this.key);
            })
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(8, 2),
               this.createData(
                  XMaterial.EXPERIENCE_BOTTLE,
                  Translatable.literal("\u00a7aVirtual Key"),
                  Translatable.literal(
                     "&7By clicking here you will be able", "&7to set this key as virtual.", "", "&a&nThis means players will not", "&a&nneed physical keys."
                  )
               ),
               this.label("enabled"),
               this.key.isVirtual()
            )
            .setOnValueChange(value -> {
               this.key.setVirtual(value);
               EditorFacade.saveKey(view, this.key);
            })
      );
      this.addComponent(
         view,
         new LinkedComponent(
               SlotCompound.from(5, 3),
               this.createData(
                  XMaterial.DIAMOND,
                  Constants.addon("Guarantee Rarity Reward"),
                  Translatable.literal("By clicking here you can select", "rarities for the key, which will", "guarantee a reward of these rarities.")
               ),
               "crate.key.rarity.editor"
            )
            .addAttribute(this.label("rarity"), "\u00a7cNot Set")
            .withMetadata("key", this.key)
            .setFallbackError(Translatable.key("unlock-addon", "%addon%", "Rarity"))
            .setClickAction(ClickAction.CHOOSE(ClickAction.ClickType.LEFT, this.label("rarity")))
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(3, 5),
               this.createData(XMaterial.LIME_DYE, Translatable.key("editor.keys.key.enabled.title"), Translatable.key("editor.keys.key.enabled.description")),
               this.label("enabled"),
               this.key.isEnabled()
            )
            .setOnValueChange(value -> {
               this.key.setEnabled(value);
               EditorFacade.saveKey(view, this.key);
            })
            .setFixedPosition(true)
      );
      if (Crates.getPlugin().getEdition() == Edition.LITE) {
         this.addComponent(
            view,
            new SimpleComponent(
                  SlotCompound.from(7, 5),
                  this.createData(
                     XMaterial.OAK_SIGN,
                     Translatable.literal("Clone Key \u00a78(Premium)"),
                     Translatable.literal(Lists.newArrayList(new String[]{"By clicking here you will be", "able to clone this key."}))
                  )
               )
               .setClickAction(ClickAction.legacy(ClickAction.ClickType.LEFT, "Duplicate key"), (editable, context) -> {})
               .addError(Translatable.key("plans.unlock-premium"))
               .addError(Translatable.key("plans.upgrade-at"))
               .setFixedPosition(true)
         );
      } else {
         this.addComponent(
            view,
            new SimpleComponent(
                  SlotCompound.from(7, 5),
                  this.createData(
                     XMaterial.OAK_SIGN, Translatable.literal("Clone Key"), Translatable.literal("By clicking here you will be", "able to clone this key.")
                  )
               )
               .setClickAction(
                  ClickAction.legacy(ClickAction.ClickType.LEFT, "Duplicate key"),
                  (editable, context) -> {
                     Player player = context.getViewer();
                     if (!this.key.isRegistered()) {
                        this.getParent().open(player);
                     } else {
                        ConfirmationMenu confirmation = new ConfirmationMenu(
                           this.plugin,
                           "Do you want to clone this key?",
                           () -> {
                              Key clone = this.key.clone();
                              String newIdentifier = StringUtil.duplicate(this.key.getIdentifier());
                              int r = 1000;

                              for (Set<String> currentIdentifiers = PhoenixCrates.getLegacyInstance().getKeysManager().getKeysIdentifier();
                                 currentIdentifiers.contains(newIdentifier) && r > 0;
                                 r--
                              ) {
                                 newIdentifier = StringUtil.duplicate(newIdentifier);
                              }

                              if (r <= 0) {
                                 newIdentifier = newIdentifier + MathUtil.random(1, 9999);
                              }

                              clone.setIdentifier(newIdentifier);

                              try {
                                 Crates.getKeysManager().registerKey(clone, true);
                                 Translations.send(context.getViewer(), "\u00a7aKey successfully duplicated. Key Identifier: " + clone.getIdentifier());
                                 this.getParent().open(player);
                              } catch (Exception e) {
                                 Translations.send(context.getViewer(), "\u00a7cAn error has occurred:", e.getMessage());
                              }
                           },
                           () -> this.open(player)
                        );
                        confirmation.open(player);
                     }
                  }
               )
               .setFixedPosition(true)
         );
      }

      this.addComponent(
         view,
         this.createComponent(
               SlotCompound.from(8, 5),
               this.createData(XMaterial.BARRIER, Translatable.key("editor.keys.key.delete.title"), Translatable.key("editor.keys.key.delete.description")),
               Lists.newArrayList(new ClickAction[]{ClickAction.DELETE(ClickAction.ClickType.LEFT, this.label("key"))}),
               context -> {
                  Player player = context.getViewer();
                  if (!this.key.isRegistered()) {
                     this.getParent().open(player);
                  } else {
                     ConfirmationMenu confirmation = new ConfirmationMenu(this.plugin, "Do you want to delete this key?", () -> {
                        try {
                           Crates.getKeysManager().unregisterCrateKey(this.key, true);
                           Translations.send(player, Translations.t("key-deleted"));
                           player.closeInventory();
                           this.getParent().open(player);
                        } catch (Exception e) {
                           Translations.error(player, e);
                           player.closeInventory();
                        }
                     }, () -> this.open(player));
                     confirmation.open(player);
                  }
               }
            )
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new SimpleComponent(
            SlotCompound.from(2, 3), this.createData(XMaterial.GRAY_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
         )
      );
      this.addComponent(
         view,
         new SimpleComponent(
            SlotCompound.from(3, 3), this.createData(XMaterial.GRAY_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
         )
      );
      this.addComponent(
         view,
         new SimpleComponent(
            SlotCompound.from(7, 3), this.createData(XMaterial.GRAY_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
         )
      );
      this.addComponent(
         view,
         new SimpleComponent(
            SlotCompound.from(8, 3), this.createData(XMaterial.GRAY_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
         )
      );
   }

   @Generated
   public Key getKey() {
      return this.key;
   }
}
