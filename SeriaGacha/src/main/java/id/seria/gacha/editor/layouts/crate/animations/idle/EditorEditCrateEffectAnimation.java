package id.seria.gacha.editor.layouts.crate.animations.idle;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.nms.interfaces.particles.PropertyType;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.inventory.CustomInventory;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.ColorUtil;
import id.seria.gacha.lib.common.utils.utility.DoubleUtil;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.menus.SelectOptionMenu;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectData;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectType;
import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

@Deprecated
public class EditorEditCrateEffectAnimation extends CustomInventory {
   private final CrateType crateType;
   private final IdleEffect effect;
   private final CustomInventory parent;

   public EditorEditCrateEffectAnimation(CrateType crateType, IdleEffect effect, CustomInventory parent) {
      super(parent.getPlugin(), Translations.t("editor.titles.crate.effect", "%crate%", StringUtil.humanize(crateType.getIdentifier()))[0], 4);
      this.crateType = crateType;
      this.effect = effect;
      this.parent = parent;
   }

   @Override
   public void setup(Player player, Inventory inventory) {
      EditorEditCrateEffectAnimation.EffectEditorWrapper holder = (EditorEditCrateEffectAnimation.EffectEditorWrapper)inventory.getHolder();
      ItemBuilder cancelItem = ItemBuilder.of(XMaterial.RED_STAINED_GLASS_PANE).setDisplayName("\u00a7c" + Translations.t("labels.cancel")[0]);

      for (int y = 1; y <= 4; y++) {
         this.setItem(1, y, cancelItem, event -> this.parent.open(player));
      }

      cancelItem = ItemBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE).setDisplayName("\u00a7a" + Translations.t("labels.save")[0]);

      for (int y = 1; y <= 4; y++) {
         this.setItem(
            9,
            y,
            cancelItem,
            event -> {
               if (holder.creating) {
                  this.crateType
                     .getIdleEffects()
                     .add(
                        new IdleEffect(
                           holder.effectType,
                           new IdleEffectData(
                              holder.particleType, holder.color, holder.offsetX, holder.offsetY, holder.offsetZ, holder.spread, holder.velocity, holder.amount
                           )
                        )
                     );
               } else {
                  this.effect.setType(holder.effectType);
                  this.effect.getData().setParticle(holder.particleType);
                  this.effect.getData().setOffsetX(holder.offsetX);
                  this.effect.getData().setOffsetY(holder.offsetY);
                  this.effect.getData().setOffsetZ(holder.offsetZ);
                  this.effect.getData().setColor(holder.color);
                  this.effect.getData().setSpread(holder.spread);
                  this.effect.getData().setVelocity(holder.velocity);
                  this.effect.getData().setAmount(holder.amount);
               }

               try {
                  CratesManager manager = Crates.getCratesManager();
                  if (this.crateType.isRegistered()) {
                     manager.saveCrateType(this.crateType);
                  } else {
                     manager.registerCrateType(this.crateType, true);
                  }

                  this.parent.open(player);
               } catch (Exception e) {
                  Translations.error(player, e);
                  player.closeInventory();
               }
            }
         );
      }

      List<String> displayedEffects = Lists.newArrayList();

      for (IdleEffectType type : IdleEffectType.values()) {
         if (holder.effectType.equals(type)) {
            displayedEffects.add("\u00a7a\u25aa " + StringUtil.humanize(type));
         } else {
            displayedEffects.add("\u00a77\u25ab " + StringUtil.humanize(type));
         }
      }

      this.setItem(
         3,
         2,
         ItemBuilder.of(XMaterial.COMPARATOR)
            .setDisplayName(Translations.t("editor.edit-standby-effect.effect-type.title", "%effect_type%", StringUtil.humanize(holder.effectType))[0])
            .setLore(Translations.t("editor.edit-standby-effect.effect-type.description", "%effects%", displayedEffects)),
         event -> {
            if (event.isLeftClick()) {
               IdleEffectType[] values = IdleEffectType.values();
               holder.effectType = values[(holder.effectType.ordinal() + 1) % values.length];
               this.updateInventorySync(player);
            } else if (event.isRightClick()) {
               IdleEffectType[] values = IdleEffectType.values();
               int newValue = holder.effectType.ordinal() - 1;
               if (newValue < 0) {
                  newValue = values.length - 1;
               }

               holder.effectType = values[newValue];
               this.updateInventorySync(player);
            }
         }
      );
      String particleName = StringUtil.humanize(holder.particleType.name());
      this.setItem(
         4,
         2,
         ItemBuilder.of(XMaterial.REDSTONE)
            .setDisplayName(Translations.t("editor.edit-standby-effect.particle-type.title", "%particle_type%", particleName)[0])
            .setLore(Translations.t("editor.edit-standby-effect.particle-type.description", "%particle_type%", particleName)),
         event -> {
            List<SelectOptionMenu.OptionItem<ParticleEffect>> options = Lists.newArrayList();

            for (ParticleEffect effect : ParticleEffect.values()) {
               if (!effect.getFieldName().equalsIgnoreCase("NONE")) {
                  List<PropertyType> properties = Lists.newArrayList(effect.getProperties());
                  if (!properties.contains(PropertyType.REQUIRES_BLOCK) && !properties.contains(PropertyType.REQUIRES_ITEM)) {
                     options.add(
                        new SelectOptionMenu.OptionItem<>(
                           effect,
                           XMaterial.BOOK,
                           Translations.t("editor.edit-standby-effect.particle-select.title", "%name%", StringUtil.humanize(effect.name()))[0],
                           Translations.t(
                              "editor.edit-standby-effect.particle-select.description",
                              "%properties%",
                              properties.isEmpty() ? "None" : properties.stream().map(p -> StringUtil.humanize(p.name())).collect(Collectors.joining(", "))
                           )
                        )
                     );
                  }
               }
            }

            SelectOptionMenu<ParticleEffect> optionSelector = new SelectOptionMenu<>(this.plugin, this.getTitle(), options, object -> {
               holder.particleType = object;
               this.open(player, holder);
            });
            optionSelector.setBackConsumer(__ -> this.open(player, holder));
            optionSelector.open(player);
         }
      );
      String displayColor = this.getHexColor(holder.color) + ColorUtil.getColorDisplayName(holder.color);
      this.setItem(
         6,
         2,
         ItemBuilder.of(XMaterial.WHITE_DYE.or(XMaterial.BONE_MEAL))
            .setDisplayName(Translations.t("editor.edit-standby-effect.color.title", "%color%", displayColor)[0])
            .setLore(Translations.t("editor.edit-standby-effect.color.description", "%color%", displayColor)),
         event -> {
            List<SelectOptionMenu.OptionItem<Color>> options = Lists.newArrayList();
            Set<String> alreadyAdded = new HashSet<>();

            for (Color color : ColorUtil.getColors()) {
               String displayName = ColorUtil.getColorDisplayName(color);
               if (!alreadyAdded.contains(displayName)) {
                  alreadyAdded.add(displayName);
                  options.add(
                     new SelectOptionMenu.OptionItem<>(
                        color,
                        XMaterial.BOOK,
                        Translations.t("editor.edit-standby-effect.color-select.title", "%name%", this.getHexColor(color) + displayName)[0],
                        Translations.t("editor.edit-standby-effect.color-select.description")
                     )
                  );
               }
            }

            SelectOptionMenu<Color> optionSelector = new SelectOptionMenu<>(this.plugin, this.getTitle(), options, object -> {
               holder.color = object;
               this.open(player, holder);
            });
            optionSelector.setBackConsumer(__ -> this.open(player, holder));
            optionSelector.open(player);
         }
      );
      String displaySpread = Constants.DECIMAL1_FORMAT.format(holder.spread);
      this.setItem(
         6,
         2,
         ItemBuilder.of(XMaterial.SLIME_BALL)
            .setDisplayName(Translations.t("editor.edit-standby-effect.spread.title", "%spread%", displaySpread)[0])
            .setLore(Translations.t("editor.edit-standby-effect.spread.description", "%spread%", displaySpread)),
         event -> {
            if (event.isLeftClick()) {
               holder.spread = holder.spread + (event.isShiftClick() ? 1.0 : 0.1);
            } else if (event.isRightClick()) {
               holder.spread = holder.spread - (event.isShiftClick() ? 1.0 : 0.1);
            }

            holder.spread = MathUtil.clamp(DoubleUtil.fix(holder.spread), 0.0, 5.0);
            this.updateInventorySync(player);
         }
      );
      String displayVelocity = Constants.DECIMAL1_FORMAT.format(holder.velocity);
      this.setItem(
         6,
         2,
         ItemBuilder.of(XMaterial.COMPASS)
            .setDisplayName(Translations.t("editor.edit-standby-effect.particle-velocity.title", "%particle_velocity%", displayVelocity)[0])
            .setLore(Translations.t("editor.edit-standby-effect.particle-velocity.description", "%particle_velocity%", displayVelocity)),
         event -> {
            if (event.isLeftClick()) {
               holder.velocity = holder.velocity + (event.isShiftClick() ? 1.0 : 0.1);
            } else if (event.isRightClick()) {
               holder.velocity = holder.velocity - (event.isShiftClick() ? 1.0 : 0.1);
            }

            holder.velocity = MathUtil.clamp(DoubleUtil.fix(holder.velocity), 0.0, 4.0);
            this.updateInventorySync(player);
         }
      );
      String displayAmount = Constants.DECIMAL1_FORMAT.format(holder.amount);
      this.setItem(
         3,
         3,
         ItemBuilder.of(XMaterial.CLAY_BALL)
            .setDisplayName(Translations.t("editor.edit-standby-effect.amount.title", "%amount%", displayAmount)[0])
            .setLore(Translations.t("editor.edit-standby-effect.amount.description", "%amount%", displayAmount)),
         event -> {
            if (event.isLeftClick()) {
               holder.amount++;
            } else if (event.isRightClick()) {
               holder.amount--;
            }

            holder.amount = MathUtil.clamp(holder.amount, 1, 10);
            this.updateInventorySync(player);
         }
      );
      this.setItem(4, 3, ItemBuilder.of(XMaterial.GRAY_STAINED_GLASS_PANE).setDisplayName("\u00a7a"));
      String displayOffsetX = Constants.DECIMAL1_FORMAT.format(holder.offsetX);
      this.setItem(
         5,
         3,
         ItemBuilder.of(XMaterial.FLINT)
            .setDisplayName(Translations.t("editor.edit-standby-effect.offset-x.title", "%offset_x%", displayOffsetX)[0])
            .setLore(Translations.t("editor.edit-standby-effect.offset-x.description", "%offset_x%", displayOffsetX)),
         event -> {
            if (event.isLeftClick()) {
               holder.offsetX = holder.offsetX + (event.isShiftClick() ? 1.0 : 0.1);
            } else if (event.isRightClick()) {
               holder.offsetX = holder.offsetX - (event.isShiftClick() ? 1.0 : 0.1);
            }

            holder.offsetX = MathUtil.clamp(DoubleUtil.fix(holder.offsetX), -5.0, 5.0);
            this.updateInventorySync(player);
         }
      );
      String displayOffsetY = Constants.DECIMAL1_FORMAT.format(holder.offsetY);
      this.setItem(
         6,
         3,
         ItemBuilder.of(XMaterial.FLINT)
            .setDisplayName(Translations.t("editor.edit-standby-effect.offset-y.title", "%offset_y%", displayOffsetY)[0])
            .setLore(Translations.t("editor.edit-standby-effect.offset-y.description", "%offset_y%", displayOffsetY)),
         event -> {
            if (event.isLeftClick()) {
               holder.offsetY = holder.offsetY + (event.isShiftClick() ? 1.0 : 0.1);
            } else if (event.isRightClick()) {
               holder.offsetY = holder.offsetY - (event.isShiftClick() ? 1.0 : 0.1);
            }

            holder.offsetY = MathUtil.clamp(DoubleUtil.fix(holder.offsetY), -5.0, 5.0);
            this.updateInventorySync(player);
         }
      );
      String displayOffsetZ = Constants.DECIMAL1_FORMAT.format(holder.offsetZ);
      this.setItem(
         7,
         3,
         ItemBuilder.of(XMaterial.FLINT)
            .setDisplayName(Translations.t("editor.edit-standby-effect.offset-z.title", "%offset_z%", displayOffsetZ)[0])
            .setLore(Translations.t("editor.edit-standby-effect.offset-z.description", "%offset_z%", displayOffsetZ)),
         event -> {
            if (event.isLeftClick()) {
               holder.offsetZ = holder.offsetZ + (event.isShiftClick() ? 1.0 : 0.1);
            } else if (event.isRightClick()) {
               holder.offsetZ = holder.offsetZ - (event.isShiftClick() ? 1.0 : 0.1);
            }

            holder.offsetZ = MathUtil.clamp(DoubleUtil.fix(holder.offsetZ), -5.0, 5.0);
            this.updateInventorySync(player);
         }
      );
   }

   private String getHexColor(Color color) {
      return Utilities.getNMSFactory().translateColorCodes("&" + ColorUtil.getHexColor(color));
   }

   @Override
   public void open(Player player) {
      super.open(player, new EditorEditCrateEffectAnimation.EffectEditorWrapper(this));
   }

   public void open(Player player, EditorEditCrateEffectAnimation.EffectEditorWrapper wrapper) {
      super.open(player, wrapper);
   }

   public class EffectEditorWrapper extends EditorFacade.EditorHolderWrapper {
      protected boolean creating;
      protected ParticleEffect particleType;
      protected IdleEffectType effectType;
      protected Color color = Color.WHITE;
      protected double offsetX;
      protected double offsetY;
      protected double offsetZ;
      protected double spread;
      protected double velocity;
      protected int amount;

      public EffectEditorWrapper(final EditorEditCrateEffectAnimation sustainer) {
         super(sustainer);
         this.effectType = IdleEffectType.DEFAULT;
         this.particleType = ParticleEffect.FLAME;
         this.amount = 1;
         IdleEffect effect = sustainer.effect;
         this.creating = true;
         if (effect != null) {
            this.creating = false;
            this.particleType = effect.getData().getParticle();
            this.effectType = effect.getType();
            this.color = effect.getData().getColor();
            this.offsetX = effect.getData().getOffsetX();
            this.offsetY = effect.getData().getOffsetY();
            this.offsetZ = effect.getData().getOffsetZ();
            this.spread = effect.getData().getSpread();
            this.velocity = effect.getData().getVelocity();
            this.amount = effect.getData().getAmount();
         }
      }
   }
}
