package id.seria.gacha.editor.layouts.crate.rewards.reward;

import com.google.common.collect.Lists;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.CloseViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.dialog.DialogUtil;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.SimpleComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditablePopupList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableStringList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableBoolean;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableString;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ArmorTrimMaterial;
import id.seria.gacha.lib.common.utils.inventory.ArmorTrimPattern;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.inventory.ItemStackUtil;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XItemFlag;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.engine.vanilla_model.VanillaModelDisplayComponent;
import id.seria.gacha.managers.reward.CustomReward;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Generated;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public class EditorCrateRewardDisplayItemAdvancedLayout extends EditorFacade.EditorLayout {
   private static final boolean GLINT_OVERRIDE_SUPPORTED;
   private static final boolean TOOLTIP_STYLE_SUPPORTED;
   private static final boolean ARMOR_TRIM_SUPPORTED;
   private final CrateType crateType;
   private final CustomReward reward;

   public EditorCrateRewardDisplayItemAdvancedLayout(CrateType crateType, CustomReward reward, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.reward"), parent);
      this.crateType = crateType;
      this.reward = reward;
      this.setViewPersistentOnGoBack(true);
   }

   @Override
   public void onPrepare(ContainerView view) {
      BaseItem item = this.reward.getDisplayItem();
      Material type = item != null ? item.getType() : null;
      Color rgb = item != null ? item.getCustomRgb() : null;
      this.addComponent(
         view,
         new SimpleComponent(
               SlotCompound.from(3, 2),
               this.createData(
                  ItemBuilder.of(XMaterial.LEATHER_CHESTPLATE).addItemFlags(Constants.ITEM_FLAGS),
                  Translatable.literal("\u00a7aRGB Color"),
                  Translatable.literal(
                     "\u00a77Set leather/potion/beacon color.",
                     "\u00a77Current: \u00a7f" + (rgb != null ? rgb.getRed() + ", " + rgb.getGreen() + ", " + rgb.getBlue() : "None")
                  )
               )
            )
            .setClickAction(
               ClickAction.legacy(ClickAction.ClickType.LEFT, "Edit Color"),
               (editable, context) -> {
                  if (this.reward.getDisplayItem() != null) {
                     Color current = this.reward.getDisplayItem().getCustomRgb();
                     Player player = context.getViewer();

                     try {
                        DialogUtil.askForInputs(
                           player,
                           "RGB Color",
                           "Set the color channels (0-255)",
                           Arrays.asList("red", "green", "blue"),
                           Arrays.asList("<red>Red", "<green>Green", "<blue>Blue"),
                           Arrays.asList(
                              current != null ? String.valueOf(current.getRed()) : "0",
                              current != null ? String.valueOf(current.getGreen()) : "0",
                              current != null ? String.valueOf(current.getBlue()) : "0"
                           ),
                           results -> {
                              if (results == null) {
                                 this.open(player);
                              } else {
                                 this.applyRgbResults(results, view);
                                 this.open(player);
                              }
                           }
                        );
                     } catch (Throwable ignored) {
                        player.closeInventory();
                        player.sendMessage("\u00a7eWrite the RGB color in chat (format: R,G,B e.g. 255,0,128)");
                        TextReader.askForInput(player, input -> {
                           if (input != null) {
                              this.applyRgbResults(Arrays.asList(input.split(",")), view);
                           }

                           this.open(player);
                        });
                     }
                  }
               }
            )
            .setClickAction(ClickAction.REMOVE(ClickAction.ClickType.RIGHT, this.label("color")), (editable, context) -> {
               if (this.reward.getDisplayItem() != null) {
                  this.reward.getDisplayItem().setCustomRgb(null);
                  EditorFacade.saveCrate(view, this.crateType, this.reward);
                  view.updateView();
               }
            })
            .addError(() -> !supportsColor(type), "\u00a7cThis item does not support RGB color.")
      );
      List<XItemFlag> allFlags = Arrays.asList(XItemFlag.values());
      this.addComponent(
         view,
         new EditablePopupList<>(
               SlotCompound.from(4, 2),
               this.createData(XMaterial.SHIELD, Translatable.literal("\u00a7aAdd Item Flag"), Translatable.literal(flagDescription(item))),
               this.label("flag"),
               allFlags,
               allFlags.get(0)
            )
            .setParser(flag -> new EditablePopupList.ListItem<>(flag, XMaterial.PAPER, StringUtil.humanize(flag.name()), Lists.newArrayList()))
            .setOnValueChange(flag -> {
               if (flag != null && this.reward.getDisplayItem() != null) {
                  List<XItemFlag> current = this.reward.getDisplayItem().getCustomFlags();
                  if (current == null) {
                     current = new ArrayList<>();
                  }

                  if (current.contains(flag)) {
                     current.remove(flag);
                  } else {
                     current.add(flag);
                  }

                  this.reward.getDisplayItem().setCustomFlags(current.isEmpty() ? null : current);
                  EditorFacade.saveCrate(view, this.crateType, this.reward);
               }
            })
            .setLocked(ctx -> this.reward.getDisplayItem() == null)
      );
      this.addComponent(
         view,
         new EditableStringList(
               SlotCompound.from(5, 2),
               this.createData(
                  ItemBuilder.of(XMaterial.WHITE_BANNER.or(XMaterial.WHITE_WOOL)).addItemFlags(Constants.ITEM_FLAGS),
                  Translatable.literal("\u00a7aBanner Patterns"),
                  Translatable.literal("\u00a77Format: COLOR;PATTERN_TYPE", "\u00a77e.g. RED;STRIPE_TOP")
               ),
               this.label("pattern"),
               ItemStackUtil.bannerPatternsToStrings(item != null ? item.getCustomBannerPatterns() : null)
            )
            .setOnValueChange(v -> {
               this.reward.getDisplayItem().setCustomBannerPatterns(ItemStackUtil.parseBannerPatterns(v));
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .addError(() -> !isBanner(type), "\u00a7cThis item is not a banner.")
            .setLocked(ctx -> this.reward.getDisplayItem() == null)
      );
      this.addComponent(
         view,
         new EditableStringList(
               SlotCompound.from(6, 2),
               this.createData(
                  XMaterial.ENCHANTING_TABLE,
                  Translatable.literal("\u00a7aEnchantments"),
                  Translatable.literal("\u00a77Format: ENCHANT_NAME:LEVEL", "\u00a77e.g. UNBREAKING:3")
               ),
               this.label("enchant"),
               ItemStackUtil.enchantsToStrings(item != null ? item.getCustomEnchants() : null)
            )
            .setOnValueChange(v -> {
               this.reward.getDisplayItem().setCustomEnchants(ItemStackUtil.parseEnchants(v));
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .setLocked(ctx -> this.reward.getDisplayItem() == null)
      );
      boolean itemModelSupported = VanillaModelDisplayComponent.ITEM_MODEL_SUPPORTED;
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(7, 2),
               this.createData(
                  XMaterial.RABBIT_FOOT,
                  Translatable.literal(itemModelSupported ? "\u00a7aCustom Model Data / Item Model" : "\u00a7aCustom Model Data"),
                  itemModelSupported
                     ? Translatable.literal("\u00a77Type a number for CMD,", "\u00a77or a namespace:key for Item Model.", "\u00a77Right-click to clear.")
                     : Translatable.literal("\u00a77Type a number for CMD.", "\u00a77Right-click to clear.")
               ),
               this.label("value"),
               formatModelValue(item)
            )
            .setFormatter(v -> v != null && !v.isEmpty() ? Translatable.literal(v) : Translatable.key("labels.none"))
            .setClickAction(
               ClickAction.REMOVE(ClickAction.ClickType.RIGHT, this.label("model-data")),
               (editable, ctx) -> editable.safeSetValue(ctx.getContainerView(), "", true)
            )
            .setOnValueChange(v -> {
               if (this.reward.getDisplayItem() != null) {
                  if (v != null && !v.isEmpty()) {
                     try {
                        int cmd = Integer.parseInt(v.trim());
                        this.reward.getDisplayItem().setCustomModelData(cmd == 0 ? null : cmd);
                        this.reward.getDisplayItem().setItemModel(null);
                     } catch (NumberFormatException e) {
                        if (itemModelSupported) {
                           this.reward.getDisplayItem().setItemModel(v.trim());
                           this.reward.getDisplayItem().setCustomModelData(null);
                        }
                     }
                  } else {
                     this.reward.getDisplayItem().setCustomModelData(null);
                     this.reward.getDisplayItem().setItemModel(null);
                  }

                  EditorFacade.saveCrate(view, this.crateType, this.reward);
               }
            })
            .setLocked(ctx -> this.reward.getDisplayItem() == null)
      );
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(3, 3),
               this.createData(
                  XMaterial.NAME_TAG,
                  Translatable.literal("\u00a7aTooltip Style"),
                  Translatable.literal("\u00a77Format: namespace:key", "\u00a77Right-click to clear.")
               ),
               this.label("tooltip-style"),
               item != null && item.getCustomTooltipStyle() != null ? item.getCustomTooltipStyle().toString() : ""
            )
            .setFormatter(v -> v != null && !v.isEmpty() ? Translatable.literal(v) : Translatable.key("labels.none"))
            .setClickAction(
               ClickAction.REMOVE(ClickAction.ClickType.RIGHT, this.label("tooltip-style")),
               (editable, ctx) -> editable.safeSetValue(ctx.getContainerView(), "", true)
            )
            .setOnValueChange(v -> {
               this.reward.getDisplayItem().setCustomTooltipStyle(ItemStackUtil.parseNamespacedKey(v));
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .addError(!TOOLTIP_STYLE_SUPPORTED, "\u00a7cNot supported on this server version.")
            .setLocked(ctx -> this.reward.getDisplayItem() == null)
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(4, 3),
               this.createData(
                  XMaterial.GLOWSTONE_DUST, Translatable.literal("\u00a7aEnchantment Glint"), Translatable.literal("\u00a77Force enchantment glow on/off.")
               ),
               this.label("enabled"),
               item != null && Boolean.TRUE.equals(item.getCustomEnchantmentGlintOverride())
            )
            .setOnValueChange(v -> {
               this.reward.getDisplayItem().setCustomEnchantmentGlintOverride(v ? Boolean.TRUE : null);
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .addError(!GLINT_OVERRIDE_SUPPORTED, "\u00a7cNot supported on this server version.")
            .setLocked(ctx -> this.reward.getDisplayItem() == null)
      );
      this.addComponent(
         view,
         new EditableStringList(
               SlotCompound.from(5, 3),
               this.createData(
                  XMaterial.POTION,
                  Translatable.literal("\u00a7aPotion Effects"),
                  Translatable.literal("\u00a77Format: type;duration;amplifier", "\u00a77e.g. speed;200;0")
               ),
               this.label("effect"),
               ItemStackUtil.potionEffectsToStrings(item != null ? item.getCustomPotionEffects() : null)
            )
            .setOnValueChange(v -> {
               this.reward.getDisplayItem().setCustomPotionEffects(ItemStackUtil.parsePotionEffects(v));
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .addError(() -> !supportsPotionEffects(type), "\u00a7cThis item does not support potion effects.")
            .setLocked(ctx -> this.reward.getDisplayItem() == null)
      );
      this.addComponent(
         view,
         new EditablePopupList<>(
               SlotCompound.from(6, 3),
               this.createData(
                  XMaterial.NETHERITE_INGOT, Translatable.literal("\u00a7aTrim Material"), Translatable.literal("\u00a77Select the armor trim material.")
               ),
               this.label("trim-material"),
               Arrays.asList(ArmorTrimMaterial.values()),
               item != null && item.getCustomTrimMaterial() != null ? item.getCustomTrimMaterial() : ArmorTrimMaterial.values()[0]
            )
            .setParser(mat -> new EditablePopupList.ListItem<>(mat, trimMaterialIcon(mat), StringUtil.humanize(mat.name()), Lists.newArrayList()))
            .setFilterable(false)
            .setOnValueChange(v -> {
               this.reward.getDisplayItem().setCustomTrimMaterial(v);
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .setClickAction(
               ClickAction.REMOVE(ClickAction.ClickType.RIGHT, this.label("trim-material")),
               (editable, ctx) -> editable.safeSetValue(ctx.getContainerView(), null, true)
            )
            .addError(!ARMOR_TRIM_SUPPORTED, "\u00a7cNot supported on this server version.")
            .addError(() -> ARMOR_TRIM_SUPPORTED && !isArmor(type), "\u00a7cThis item is not an armor piece.")
            .setLocked(ctx -> this.reward.getDisplayItem() == null)
      );
      this.addComponent(
         view,
         new EditablePopupList<>(
               SlotCompound.from(7, 3),
               this.createData(
                  ItemBuilder.of(XMaterial.NETHERITE_UPGRADE_SMITHING_TEMPLATE.or(XMaterial.IRON_BLOCK)).addItemFlags(Constants.ITEM_FLAGS),
                  Translatable.literal("\u00a7aTrim Pattern"),
                  Translatable.literal("\u00a77Select the armor trim pattern.")
               ),
               this.label("trim-pattern"),
               Arrays.asList(ArmorTrimPattern.values()),
               item != null && item.getCustomTrimPattern() != null ? item.getCustomTrimPattern() : ArmorTrimPattern.values()[0]
            )
            .setParser(pat -> new EditablePopupList.ListItem<>(pat, trimPatternIcon(pat), StringUtil.humanize(pat.name()), Lists.newArrayList()))
            .setFilterable(false)
            .setOnValueChange(v -> {
               this.reward.getDisplayItem().setCustomTrimPattern(v);
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .setClickAction(
               ClickAction.REMOVE(ClickAction.ClickType.RIGHT, this.label("trim-pattern")),
               (editable, ctx) -> editable.safeSetValue(ctx.getContainerView(), null, true)
            )
            .addError(!ARMOR_TRIM_SUPPORTED, "\u00a7cNot supported on this server version.")
            .addError(() -> ARMOR_TRIM_SUPPORTED && !isArmor(type), "\u00a7cThis item is not an armor piece.")
            .setLocked(ctx -> this.reward.getDisplayItem() == null)
      );
   }

   @Override
   public void onClose(CloseViewContext context) {
      EditorFacade.saveCrate(context.getHolder(), this.crateType, this.reward);
   }

   private static boolean supportsColor(Material type) {
      if (type == null) {
         return true;
      }

      String name = type.name();
      return name.startsWith("LEATHER_")
         || name.contains("POTION")
         || type == Material.TIPPED_ARROW
         || name.equals("FIREWORK_STAR")
         || name.equals("FILLED_MAP");
   }

   private static boolean supportsPotionEffects(Material type) {
      if (type == null) {
         return true;
      }

      String name = type.name();
      return name.contains("POTION") || type == Material.TIPPED_ARROW;
   }

   private static boolean isArmor(Material type) {
      if (type == null) {
         return false;
      }

      String name = type.name();
      return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") || name.equals("TURTLE_HELMET");
   }

   private static boolean isBanner(Material type) {
      return type == null ? true : type.name().endsWith("_BANNER");
   }

   private void applyRgbResults(List<String> parts, ContainerView view) {
      if (this.reward.getDisplayItem() != null && parts != null && parts.size() >= 3) {
         try {
            int r = Math.max(0, Math.min(255, Integer.parseInt(parts.get(0).trim())));
            int g = Math.max(0, Math.min(255, Integer.parseInt(parts.get(1).trim())));
            int b = Math.max(0, Math.min(255, Integer.parseInt(parts.get(2).trim())));
            this.reward.getDisplayItem().setCustomRgb(r == 0 && g == 0 && b == 0 ? null : Color.fromRGB(r, g, b));
         } catch (NumberFormatException ignored) {
            this.reward.getDisplayItem().setCustomRgb(null);
         }

         EditorFacade.saveCrate(view, this.crateType, this.reward);
      }
   }

   private static String formatModelValue(BaseItem item) {
      if (item == null) {
         return "";
      } else if (item.getItemModel() != null && !item.getItemModel().isEmpty()) {
         return item.getItemModel();
      } else {
         return item.getCustomModelData() != null && item.getCustomModelData() != 0 ? String.valueOf(item.getCustomModelData()) : "";
      }
   }

   private static String[] flagDescription(BaseItem item) {
      List<String> lines = new ArrayList<>();
      lines.add("\u00a77Select a flag to add/remove.");
      lines.add("");
      lines.add("\u00a7fFlags:");
      if (item != null && item.getCustomFlags() != null && !item.getCustomFlags().isEmpty()) {
         for (XItemFlag flag : item.getCustomFlags()) {
            lines.add("\u00a77  - \u00a7e" + flag.name());
         }
      } else {
         lines.add("\u00a77  None");
      }

      return lines.toArray(new String[0]);
   }

   private static XMaterial trimMaterialIcon(ArmorTrimMaterial mat) {
      switch (mat) {
         case QUARTZ:
            return XMaterial.QUARTZ;
         case IRON:
            return XMaterial.IRON_INGOT;
         case NETHERITE:
            return XMaterial.NETHERITE_INGOT;
         case REDSTONE:
            return XMaterial.REDSTONE;
         case COPPER:
            return XMaterial.COPPER_INGOT;
         case GOLD:
            return XMaterial.GOLD_INGOT;
         case EMERALD:
            return XMaterial.EMERALD;
         case DIAMOND:
            return XMaterial.DIAMOND;
         case LAPIS:
            return XMaterial.LAPIS_LAZULI;
         case AMETHYST:
            return XMaterial.AMETHYST_SHARD;
         case RESIN:
            return XMaterial.RESIN_CLUMP.or(XMaterial.HONEYCOMB);
         default:
            return XMaterial.IRON_INGOT;
      }
   }

   private static XMaterial trimPatternIcon(ArmorTrimPattern pat) {
      switch (pat) {
         case SENTRY:
            return XMaterial.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case DUNE:
            return XMaterial.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case COAST:
            return XMaterial.COAST_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case WILD:
            return XMaterial.WILD_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case WARD:
            return XMaterial.WARD_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case EYE:
            return XMaterial.EYE_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case VEX:
            return XMaterial.VEX_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case TIDE:
            return XMaterial.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case SNOUT:
            return XMaterial.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case RIB:
            return XMaterial.RIB_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case SPIRE:
            return XMaterial.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case WAYFINDER:
            return XMaterial.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case SHAPER:
            return XMaterial.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case SILENCE:
            return XMaterial.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case RAISER:
            return XMaterial.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case HOST:
            return XMaterial.HOST_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case FLOW:
            return XMaterial.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         case BOLT:
            return XMaterial.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE.or(XMaterial.PAPER);
         default:
            return XMaterial.PAPER;
      }
   }

   @Generated
   public CrateType getCrateType() {
      return this.crateType;
   }

   @Generated
   public CustomReward getReward() {
      return this.reward;
   }

   static {
      boolean glint = false;
      boolean tooltip = false;
      boolean trim = false;

      try {
         glint = ItemMeta.class.getMethod("setEnchantmentGlintOverride", Boolean.class) != null;
      } catch (Exception var6) {
      }

      try {
         tooltip = ItemMeta.class.getMethod("setTooltipStyle", NamespacedKey.class) != null;
      } catch (Exception var5) {
      }

      try {
         Class.forName("org.bukkit.inventory.meta.ArmorMeta");
         trim = true;
      } catch (Exception var4) {
      }

      GLINT_OVERRIDE_SUPPORTED = glint;
      TOOLTIP_STYLE_SUPPORTED = tooltip;
      ARMOR_TRIM_SUPPORTED = trim;
   }
}
