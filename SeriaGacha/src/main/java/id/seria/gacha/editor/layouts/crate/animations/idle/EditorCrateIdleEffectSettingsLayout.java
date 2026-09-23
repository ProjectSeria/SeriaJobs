package id.seria.gacha.editor.layouts.crate.animations.idle;

import com.google.common.collect.Lists;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.lib.common.nms.interfaces.particles.PropertyType;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditablePopupList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableString;
import id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers.ObjectModifiers;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.ColorUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectData;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EditorCrateIdleEffectSettingsLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;
   private final IdleEffect effect;

   public EditorCrateIdleEffectSettingsLayout(CrateType crateType, IdleEffect effect, EditorFacade.EditorLayout parent) {
      super(Translatable.literal("\u00a78Effect Settings"), parent);
      this.crateType = crateType;
      this.effect = effect;
   }

   @Override
   public void onPrepare(ContainerView view) {
      IdleEffectData data = this.effect.getData();
      boolean colorable = data.getParticle().hasProperty(PropertyType.COLORABLE);
      Color color = data.getColor();
      List<ParticleEffect> particles = Arrays.stream(ParticleEffect.values())
         .filter(p -> !p.getFieldName().equalsIgnoreCase("NONE"))
         .filter(p -> !p.hasProperty(PropertyType.REQUIRES_BLOCK) && !p.hasProperty(PropertyType.REQUIRES_ITEM))
         .collect(Collectors.toList());
      this.addComponent(
         view,
         new EditablePopupList<>(
               SlotCompound.from(2, 2),
               this.createData(
                  XMaterial.REDSTONE,
                  Translatable.literal("\u00a7aParticle"),
                  Translatable.literal("\u00a77The particle type to display.", "\u00a77Current: \u00a7f" + StringUtil.humanize(data.getParticle().name()))
               ),
               this.label("particle"),
               particles,
               data.getParticle()
            )
            .setParser(
               p -> {
                  List<PropertyType> props = Lists.newArrayList(p.getProperties());
                  return new EditablePopupList.ListItem<>(
                     p,
                     XMaterial.BOOK,
                     StringUtil.humanize(p.name()),
                     Lists.newArrayList(
                        new String[]{
                           "\u00a77"
                              + (
                                 props.isEmpty()
                                    ? "No special properties"
                                    : props.stream().map(pt -> StringUtil.humanize(pt.name())).collect(Collectors.joining(", "))
                              )
                        }
                     )
                  );
               }
            )
            .setOnValueChange(v -> {
               data.setParticle(v);
               this.save(view);
            })
      );
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(5, 2),
               this.createData(
                  ItemBuilder.of(XMaterial.WHITE_DYE.or(XMaterial.BONE_MEAL)),
                  Translatable.literal("\u00a7aColor"),
                  Translatable.literal("\u00a77Format: R,G,B (0-255 each).")
               ),
               colorToString(color)
            )
            .setFormatter(v -> {
               Color parsed = parseColorOrNull(v);
               return Translatable.literal(parsed != null ? "\u00a7f" + ColorUtil.getColorDisplayName(parsed) : "\u00a7c" + v);
            })
            .addValidator(v -> parseColorOrNull(v) != null, Translatable.literal("\u00a7cInvalid format, use R,G,B (0-255 each)."))
            .setOnValueChange(v -> {
               data.setColor(parseColorOrNull(v));
               this.save(view);
            })
            .setLocked(obj -> !colorable)
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(6, 2),
               this.createData(XMaterial.CLAY_BALL, Translatable.literal("\u00a7aAmount"), Translatable.literal("\u00a77Particles per tick (1-10).")),
               this.label("amount"),
               (double)data.getAmount()
            )
            .addModifier(ObjectModifiers.CLAMP(1, 10))
            .setOnValueChange(v -> {
               data.setAmount(v.intValue());
               this.save(view);
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(7, 2),
               this.createData(XMaterial.COMPASS, Translatable.literal("\u00a7aVelocity"), Translatable.literal("\u00a77Movement speed (0-4).")),
               this.label("velocity"),
               data.getVelocity()
            )
            .addModifier(ObjectModifiers.CLAMP(0, 4))
            .setOnValueChange(v -> {
               data.setVelocity(v.doubleValue());
               this.save(view);
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(2, 3),
               this.createData(XMaterial.SLIME_BALL, Translatable.literal("\u00a7aSpread"), Translatable.literal("\u00a77Random spread radius (0-5).")),
               this.label("spread"),
               data.getSpread()
            )
            .addModifier(ObjectModifiers.CLAMP(0, 5))
            .setOnValueChange(v -> {
               data.setSpread(v.doubleValue());
               this.save(view);
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(5, 3),
               this.createData(XMaterial.FLINT, Translatable.literal("\u00a7aOffset X"), Translatable.literal("\u00a77Horizontal offset (-5 to 5).")),
               this.label("offset-x"),
               data.getOffsetX()
            )
            .addModifier(ObjectModifiers.CLAMP(-5, 5))
            .setOnValueChange(v -> {
               data.setOffsetX(v.doubleValue());
               this.save(view);
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(6, 3),
               this.createData(XMaterial.FLINT, Translatable.literal("\u00a7aOffset Y"), Translatable.literal("\u00a77Vertical offset (-5 to 5).")),
               this.label("offset-y"),
               data.getOffsetY()
            )
            .addModifier(ObjectModifiers.CLAMP(-5, 5))
            .setOnValueChange(v -> {
               data.setOffsetY(v.doubleValue());
               this.save(view);
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(7, 3),
               this.createData(XMaterial.FLINT, Translatable.literal("\u00a7aOffset Z"), Translatable.literal("\u00a77Depth offset (-5 to 5).")),
               this.label("offset-z"),
               data.getOffsetZ()
            )
            .addModifier(ObjectModifiers.CLAMP(-5, 5))
            .setOnValueChange(v -> {
               data.setOffsetZ(v.doubleValue());
               this.save(view);
            })
      );
   }

   private static String colorToString(Color color) {
      return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
   }

   private static Color parseColorOrNull(String value) {
      if (value == null) {
         return null;
      }

      String[] parts = value.split(",");
      if (parts.length != 3) {
         return null;
      }

      try {
         int red = Integer.parseInt(parts[0].trim());
         int green = Integer.parseInt(parts[1].trim());
         int blue = Integer.parseInt(parts[2].trim());
         return red >= 0 && red <= 255 && green >= 0 && green <= 255 && blue >= 0 && blue <= 255 ? new Color(red, green, blue) : null;
      } catch (NumberFormatException e) {
         return null;
      }
   }

   private void save(ContainerView view) {
      EditorFacade.saveCrate(view, this.crateType);
   }
}
