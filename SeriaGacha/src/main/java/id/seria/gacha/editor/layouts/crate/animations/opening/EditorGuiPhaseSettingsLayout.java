package id.seria.gacha.editor.layouts.crate.animations.opening;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit.EditableItemAppearance;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableStringList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableString;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.opening.OpeningPhase;
import id.seria.gacha.managers.crates.type.list.OpeningAnimation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EditorGuiPhaseSettingsLayout extends EditorFacade.EditorLayout {
   private static final String DEFAULT_TITLE = "&8&lOpening...";
   private static final String DEFAULT_FILLER = "BLACK_STAINED_GLASS_PANE";
   private static final String DEFAULT_MARKER_NORMAL = "YELLOW_STAINED_GLASS_PANE";
   private static final String DEFAULT_MARKER_FLICKER = "ORANGE_STAINED_GLASS_PANE";
   private static final List<String> DEFAULT_CHANCE_LORE = Arrays.asList("", "&eYour chance: &6%chance%%");
   private final CrateType crateType;
   private final PhaseType phaseType;

   public EditorGuiPhaseSettingsLayout(CrateType crateType, PhaseType phaseType, EditorFacade.EditorLayout parent) {
      super(Translatable.literal("\u00a78Phase Settings"), parent);
      this.crateType = crateType;
      this.phaseType = phaseType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      AnimationProperties props = this.getProperties();
      String animId = this.getAnimationId();
      int slot = 2;
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(slot++, 2),
               this.createData(
                  XMaterial.NAME_TAG,
                  Translatable.literal("\u00a7aTitle"),
                  Translatable.literal("\u00a77The title shown on the GUI", "\u00a77during this animation phase.")
               ),
               this.label("title"),
               props.getPropertyOrDefault("title", "&8&lOpening...")
            )
            .setOnValueChange(value -> {
               this.getProperties().addProperty("title", value);
               this.save(view);
            })
      );
      if (usesFiller(animId)) {
         String fillerDefault = getFillerDefault(animId);
         this.addComponent(
            view,
            new EditableItemAppearance(
                  SlotCompound.from(slot++, 2),
                  this.createData(
                     XMaterial.BLACK_STAINED_GLASS_PANE,
                     Translatable.literal("\u00a7aFiller"),
                     Translatable.literal("\u00a77The background glass pane", "\u00a77used to fill empty slots.")
                  ),
                  materialFromProps(props, "filler", fillerDefault)
               )
               .setOnValueChange(value -> {
                  materialToProps(this.getProperties(), "filler", value);
                  this.save(view);
               })
         );
      }

      this.addComponent(
         view,
         new EditableItemAppearance(
               SlotCompound.from(slot++, 2),
               this.createData(
                  XMaterial.YELLOW_STAINED_GLASS_PANE,
                  Translatable.literal("\u00a7aMarker Normal"),
                  Translatable.literal("\u00a77The marker item shown during", "\u00a77the reward highlight phase.")
               ),
               materialFromProps(props, "markerNormal", "YELLOW_STAINED_GLASS_PANE")
            )
            .setOnValueChange(value -> {
               materialToProps(this.getProperties(), "markerNormal", value);
               this.save(view);
            })
      );
      this.addComponent(
         view,
         new EditableItemAppearance(
               SlotCompound.from(slot++, 2),
               this.createData(
                  XMaterial.ORANGE_STAINED_GLASS_PANE,
                  Translatable.literal("\u00a7aMarker Flicker"),
                  Translatable.literal("\u00a77The alternating marker item", "\u00a77shown during the flicker.")
               ),
               materialFromProps(props, "markerFlicker", "ORANGE_STAINED_GLASS_PANE")
            )
            .setOnValueChange(value -> {
               materialToProps(this.getProperties(), "markerFlicker", value);
               this.save(view);
            })
      );
      if (usesBlackGlass(animId)) {
         this.addComponent(
            view,
            new EditableItemAppearance(
                  SlotCompound.from(slot++, 2),
                  this.createData(
                     XMaterial.BLACK_STAINED_GLASS_PANE, Translatable.literal("\u00a7aBlack Glass"), Translatable.literal("\u00a77The dark background glass.")
                  ),
                  materialFromProps(props, "blackGlass", "BLACK_STAINED_GLASS_PANE")
               )
               .setOnValueChange(value -> {
                  materialToProps(this.getProperties(), "blackGlass", value);
                  this.save(view);
               })
         );
      }

      if (usesYellowGlass(animId)) {
         this.addComponent(
            view,
            new EditableItemAppearance(
                  SlotCompound.from(slot++, 2),
                  this.createData(
                     XMaterial.YELLOW_STAINED_GLASS_PANE, Translatable.literal("\u00a7aYellow Glass"), Translatable.literal("\u00a77The highlight/head glass.")
                  ),
                  materialFromProps(props, "yellowGlass", "YELLOW_STAINED_GLASS_PANE")
               )
               .setOnValueChange(value -> {
                  materialToProps(this.getProperties(), "yellowGlass", value);
                  this.save(view);
               })
         );
      }

      if (usesWhiteGlass(animId)) {
         this.addComponent(
            view,
            new EditableItemAppearance(
                  SlotCompound.from(slot++, 2),
                  this.createData(
                     XMaterial.WHITE_STAINED_GLASS_PANE, Translatable.literal("\u00a7aWhite Glass"), Translatable.literal("\u00a77The base background glass.")
                  ),
                  materialFromProps(props, "whiteGlass", "WHITE_STAINED_GLASS_PANE")
               )
               .setOnValueChange(value -> {
                  materialToProps(this.getProperties(), "whiteGlass", value);
                  this.save(view);
               })
         );
      }

      if (usesPurpleGlass(animId)) {
         this.addComponent(
            view,
            new EditableItemAppearance(
                  SlotCompound.from(slot++, 2),
                  this.createData(
                     XMaterial.PURPLE_STAINED_GLASS_PANE,
                     Translatable.literal("\u00a7aPurple Glass"),
                     Translatable.literal("\u00a77The interior background glass.")
                  ),
                  materialFromProps(props, "purpleGlass", "PURPLE_STAINED_GLASS_PANE")
               )
               .setOnValueChange(value -> {
                  materialToProps(this.getProperties(), "purpleGlass", value);
                  this.save(view);
               })
         );
      }

      this.addComponent(
         view,
         new EditableStringList(
               SlotCompound.from(5, 3),
               this.createData(
                  XMaterial.WRITABLE_BOOK,
                  Translatable.literal("\u00a7aChance Lore"),
                  Translatable.literal("\u00a77Extra lore lines appended to", "\u00a77each reward showing the chance.", "\u00a77Use %chance% as placeholder.")
               ),
               this.label("line"),
               new ArrayList<>(getChanceLore(props))
            )
            .setOnValueChange(value -> {
               this.getProperties().addProperty("chanceLore", value);
               this.save(view);
            })
      );
   }

   private String getAnimationId() {
      OpeningAnimation animation = this.crateType.getOpeningAnimations().list().get(0);
      OpeningPhase phase;
      switch (this.phaseType) {
         case PRE_OPENING:
            phase = animation.getPreOpenPhase();
            break;
         case OPENING:
            phase = animation.getOpenPhase();
            break;
         case DISPLAYING:
            phase = animation.getDisplayPhase();
            break;
         default:
            return "";
      }

      return phase != null && phase.getPhase() != null ? phase.getPhase().getId() : "";
   }

   private AnimationProperties getProperties() {
      OpeningAnimation animation = this.crateType.getOpeningAnimations().list().get(0);
      OpeningPhase phase;
      switch (this.phaseType) {
         case PRE_OPENING:
            phase = animation.getPreOpenPhase();
            break;
         case OPENING:
            phase = animation.getOpenPhase();
            break;
         case DISPLAYING:
            phase = animation.getDisplayPhase();
            break;
         default:
            return AnimationProperties.empty();
      }

      return phase != null ? phase.getProperties() : AnimationProperties.empty();
   }

   private static boolean usesBlackGlass(String animId) {
      return "SPOTLIGHT_GUI".equals(animId) || "BLACKOUT_GUI".equals(animId) || "SNAKE_GUI".equals(animId);
   }

   private static boolean usesYellowGlass(String animId) {
      return "SPOTLIGHT_GUI".equals(animId) || "SNAKE_GUI".equals(animId) || "BLACKOUT_GUI".equals(animId);
   }

   private static boolean usesWhiteGlass(String animId) {
      return "SNAKE_GUI".equals(animId) || "BLACKOUT_GUI".equals(animId) || "RAINBOW_GUI".equals(animId) || "RING_CAROUSEL_GUI".equals(animId);
   }

   private static boolean usesFiller(String animId) {
      return "CSGO_GUI".equals(animId) || "FLIP_GUI".equals(animId) || "RING_CAROUSEL_GUI".equals(animId);
   }

   private static boolean usesPurpleGlass(String animId) {
      return "RING_CAROUSEL_GUI".equals(animId);
   }

   private static String getFillerDefault(String animId) {
      return "FLIP_GUI".equals(animId) ? "LIGHT_GRAY_STAINED_GLASS_PANE" : "BLACK_STAINED_GLASS_PANE";
   }

   private static List<String> getChanceLore(AnimationProperties props) {
      Object raw = props.getProperty("chanceLore");
      return raw instanceof List ? (List)raw : new ArrayList<>(DEFAULT_CHANCE_LORE);
   }

   private static ItemStack materialFromProps(AnimationProperties props, String prefix, String defaultMaterial) {
      String matName = props.getPropertyOrDefault(prefix, defaultMaterial);

      try {
         XMaterial xMat = XMaterial.valueOf(matName);
         ItemStack item = xMat.parseItem();
         if (item == null) {
            return new ItemStack(Material.BARRIER);
         }

         int cmd = props.getPropertyOrDefault(prefix + "Cmd", 0);
         String model = props.getPropertyOrDefault(prefix + "Model", "");
         ItemBuilder builder = ItemBuilder.of(item);
         if (cmd > 0) {
            builder.setCustomModelData(cmd);
         }

         if (model != null && !model.isEmpty()) {
            builder.setItemModel(model);
         }

         return builder.build();
      } catch (Exception e) {
         return XMaterial.BLACK_STAINED_GLASS_PANE.parseItem();
      }
   }

   private static void materialToProps(AnimationProperties props, String prefix, ItemStack item) {
      if (item != null && item.getType() != Material.AIR) {
         XMaterial xMat = XMaterial.matchXMaterial(item);
         props.addProperty(prefix, xMat.name());
         ItemBuilder builder = ItemBuilder.of(item);
         int cmd = builder.getCustomModelData();
         if (cmd > 0) {
            props.addProperty(prefix + "Cmd", cmd);
         } else {
            props.removeProperty(prefix + "Cmd");
         }

         String model = builder.getItemModel();
         if (model != null && !model.isEmpty()) {
            props.addProperty(prefix + "Model", model);
         } else {
            props.removeProperty(prefix + "Model");
         }
      } else {
         props.removeProperty(prefix);
         props.removeProperty(prefix + "Cmd");
         props.removeProperty(prefix + "Model");
      }
   }

   private void save(ContainerView view) {
      EditorFacade.saveCrate(view, this.crateType);
   }
}
