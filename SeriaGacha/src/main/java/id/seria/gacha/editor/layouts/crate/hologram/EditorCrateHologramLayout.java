package id.seria.gacha.editor.layouts.crate.hologram;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeDisplayEntity;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.SimpleComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableStringList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableBoolean;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableString;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.hologram.HologramProperties;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

public class EditorCrateHologramLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;

   public EditorCrateHologramLayout(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.hologram"), parent);
      this.crateType = crateType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      final HologramProperties properties = this.crateType.getHologramProperties();
      this.addComponent(
         view,
         new EditableStringList(
               SlotCompound.from(4, 2),
               this.createData(
                  XMaterial.BOOK, Translatable.key("editor.crate.hologram.lines.title"), Translatable.key("editor.crate.hologram.lines.description")
               ),
               this.label("navigate"),
               properties.getOrSetDefault("lines", new ArrayList<>())
            )
            .setOnValueChange(value -> {
               properties.set("lines", value);
               this.save(view.getViewer());
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(6, 2),
               this.createData(
                  XMaterial.FLINT, Translatable.key("editor.crate.hologram.offset.title"), Translatable.key("editor.crate.hologram.offset.description")
               ),
               properties.getOffset()
            )
            .setOnValueChange(value -> {
               properties.setOffset(value.doubleValue());

               for (Crate crate : Crates.getCratesManager().getCratesByType(this.crateType)) {
                  crate.getHologram().moveTo(crate.getHologramLocation());
               }

               this.save(view.getViewer());
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(2, 3),
               this.createData(
                  XMaterial.STRING, Translatable.key("editor.crate.hologram.line-gap.title"), Translatable.key("editor.crate.hologram.line-gap.description")
               ),
               this.label("line-gap"),
               properties.getLineGap()
            )
            .addModifier(value -> Math.round(value.doubleValue() * 100.0) / 100.0)
            .setOnValueChange(value -> {
               properties.setLineGap(value.doubleValue());
               this.applyStyle();
               this.save(view.getViewer());
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(3, 3),
               this.createData(
                  XMaterial.NETHER_STAR, Translatable.key("editor.crate.hologram.scale.title"), Translatable.key("editor.crate.hologram.scale.description")
               ),
               this.label("hologram-scale"),
               properties.getScale()
            )
            .addModifier(value -> Math.round(value.doubleValue() * 10.0) / 10.0)
            .setOnValueChange(value -> {
               properties.setScale(value.doubleValue());
               this.applyStyle();
               this.save(view.getViewer());
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(4, 3),
               this.createData(
                  XMaterial.ENDER_EYE,
                  Translatable.key("editor.crate.hologram.text-opacity.title"),
                  Translatable.key("editor.crate.hologram.text-opacity.description")
               ),
               this.label("text-opacity"),
               properties.getTextOpacity()
            )
            .addModifier(value -> MathUtil.clamp(value.intValue(), 0, 255))
            .setOnValueChange(value -> {
               properties.setTextOpacity(value.intValue());
               this.applyStyle();
               this.save(view.getViewer());
            })
      );
      this.addComponent(
         view,
         (new SimpleComponent(
               SlotCompound.from(5, 3),
               this.createData(
                  XMaterial.ITEM_FRAME,
                  Translatable.key("editor.crate.hologram.billboard.title", "%billboard%", StringUtil.humanize(properties.getBillboard())),
                  Translatable.key("editor.crate.hologram.billboard.description", "%billboard%", StringUtil.humanize(properties.getBillboard()))
               )
            ) {
               @Override
               public List<String> getRenderDisplay(RenderViewContext context) {
                  List<String> displayedModes = new ArrayList<>();

                  for (FakeDisplayEntity.Billboard target : FakeDisplayEntity.Billboard.values()) {
                     displayedModes.add(
                        properties.getBillboard().equals(target)
                           ? "\u00a7a\u25aa " + StringUtil.humanize(target)
                           : "\u00a77\u25ab " + StringUtil.humanize(target)
                     );
                  }

                  return displayedModes;
               }
            })
            .setClickAction(ClickAction.CHANGE(ClickAction.ClickType.LEFT, this.label("billboard")), context -> {
               FakeDisplayEntity.Billboard[] values = FakeDisplayEntity.Billboard.values();
               properties.setBillboard(values[(properties.getBillboard().ordinal() + 1) % values.length]);
               this.applyStyle();
               this.save(context.getViewer());
            })
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(6, 3),
               this.createData(
                  XMaterial.SPYGLASS,
                  Translatable.key("editor.crate.hologram.see-through.title"),
                  Translatable.key("editor.crate.hologram.see-through.description")
               ),
               this.label("see-through"),
               properties.isSeeThrough()
            )
            .setOnValueChange(value -> {
               properties.setSeeThrough(value);
               this.applyStyle();
               this.save(view.getViewer());
            })
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(7, 3),
               this.createData(
                  XMaterial.BLACK_DYE, Translatable.key("editor.crate.hologram.shadow.title"), Translatable.key("editor.crate.hologram.shadow.description")
               ),
               this.label("shadow"),
               properties.isShadow()
            )
            .setOnValueChange(value -> {
               properties.setShadow(value);
               this.applyStyle();
               this.save(view.getViewer());
            })
      );
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(8, 3),
               this.createData(
                  XMaterial.RED_DYE,
                  Translatable.key("editor.crate.hologram.background-color.title"),
                  Translatable.key("editor.crate.hologram.background-color.description")
               ),
               this.label("background-color"),
               properties.getRawBackgroundColor()
            )
            .setOnValueChange(input -> {
               properties.setRawBackgroundColor(input);
               this.applyStyle();
               this.save(view.getViewer());
            })
      );
   }

   private void applyStyle() {
      for (Crate crate : Crates.getCratesManager().getCratesByType(this.crateType)) {
         if (crate.getHologram() != null) {
            crate.getHologram().applyStyle();
         }
      }
   }

   @Override
   public Translatable getTitle() {
      return this.crateType.isRegistered() ? Translatable.key("editor.titles.crate.show") : Translatable.key("editor.titles.crate.new");
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
