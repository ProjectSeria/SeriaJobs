package id.seria.gacha.actions;

import id.seria.gacha.actions.executors.CommandAction;
import id.seria.gacha.actions.executors.GoBackAction;
import id.seria.gacha.actions.executors.MessageAction;
import id.seria.gacha.actions.executors.OpenCrateAction;
import id.seria.gacha.actions.executors.PotionEffectAction;
import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionBuilderRegistry;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types.CommandActionBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types.DelayActionBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types.GamemodeActionBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types.MessageActionBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types.PotionEffectActionBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types.SoundActionBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types.TitleActionBuilderLayout;
import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionsFactory;
import id.seria.gacha.lib.common.utils.actions.common.ActionBarAction;
import id.seria.gacha.lib.common.utils.actions.common.ActionPlaceholders;
import id.seria.gacha.lib.common.utils.actions.common.CloseInventoryAction;
import id.seria.gacha.lib.common.utils.actions.common.GamemodeAction;
import id.seria.gacha.lib.common.utils.actions.common.SoundAction;
import id.seria.gacha.lib.common.utils.actions.common.TitleAction;
import id.seria.gacha.lib.xseries.XMaterial;

public final class Actions {
   private static final ActionPlaceholders PLACEHOLDERS = Placeholders::setPlaceholders;
   private static final ActionsFactory INSTANCE = new ActionsFactory();
   private static final ActionBuilderRegistry BUILDERS = new ActionBuilderRegistry(INSTANCE);

   public Actions() {
   }

   public static ActionsFactory getFactory() {
      return INSTANCE;
   }

   public static ActionBuilderRegistry getBuilders() {
      return BUILDERS;
   }

   public static Action unserialize(String format) {
      return INSTANCE.unserialize(format);
   }

   public static String serialize(Action action) {
      return INSTANCE.serialize(action);
   }

   static {
      INSTANCE.registerAction("ACTION_BAR", arguments -> new ActionBarAction(arguments, PLACEHOLDERS)).icon(XMaterial.NAME_TAG);
      INSTANCE.registerAction("CLOSE_INVENTORY", arguments -> new CloseInventoryAction()).icon(XMaterial.BARRIER);
      INSTANCE.registerAction("COMMAND", arguments -> new CommandAction(arguments)).icon(XMaterial.COMMAND_BLOCK);
      INSTANCE.registerAction("GAMEMODE", arguments -> new GamemodeAction(arguments)).icon(XMaterial.DIAMOND_PICKAXE);
      INSTANCE.registerAction("MESSAGE", arguments -> new MessageAction(arguments)).icon(XMaterial.PAPER);
      INSTANCE.registerAction("POTION_EFFECT", arguments -> new PotionEffectAction(arguments)).icon(XMaterial.POTION);
      INSTANCE.registerAction("SOUND", arguments -> new SoundAction(arguments)).icon(XMaterial.NOTE_BLOCK);
      INSTANCE.registerAction("TITLE", arguments -> new TitleAction(arguments, PLACEHOLDERS)).icon(XMaterial.OAK_SIGN);
      INSTANCE.registerAction("OPEN", arguments -> new OpenCrateAction()).icon(XMaterial.ENDER_CHEST);
      INSTANCE.registerAction("GOBACK", arguments -> new GoBackAction()).icon(XMaterial.ARROW);
      INSTANCE.getTypeByNameOrNull("DELAY").icon(XMaterial.CLOCK);
      BUILDERS.register("ACTION_BAR", MessageActionBuilderLayout::new);
      BUILDERS.register("MESSAGE", MessageActionBuilderLayout::new);
      BUILDERS.register("COMMAND", CommandActionBuilderLayout::new);
      BUILDERS.register("GAMEMODE", GamemodeActionBuilderLayout::new);
      BUILDERS.register("POTION_EFFECT", PotionEffectActionBuilderLayout::new);
      BUILDERS.register("SOUND", SoundActionBuilderLayout::new);
      BUILDERS.register("TITLE", TitleActionBuilderLayout::new);
      BUILDERS.register("DELAY", DelayActionBuilderLayout::new);
      BUILDERS.registerInstant("CLOSE_INVENTORY");
      BUILDERS.registerInstant("OPEN");
      BUILDERS.registerInstant("GOBACK");
   }
}
