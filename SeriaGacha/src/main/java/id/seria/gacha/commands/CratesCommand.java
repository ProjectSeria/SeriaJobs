package id.seria.gacha.commands;

import id.seria.gacha.Crates;
import id.seria.gacha.commands.subcommands.AttachCrateCommand;
import id.seria.gacha.commands.subcommands.CheckKeysCommand;
import id.seria.gacha.commands.subcommands.CrateCommandExecutable;
import id.seria.gacha.commands.subcommands.CreateCrateCommand;
import id.seria.gacha.commands.subcommands.DeleteCrateCommand;
import id.seria.gacha.commands.subcommands.DetachCrateCommand;
import id.seria.gacha.commands.subcommands.GiveCrateAllCommand;
import id.seria.gacha.commands.subcommands.GiveCrateCommand;
import id.seria.gacha.commands.subcommands.GiveCrateRerollCommand;
import id.seria.gacha.commands.subcommands.GiveCrateRewardCommand;
import id.seria.gacha.commands.subcommands.GiveKeyAllCommand;
import id.seria.gacha.commands.subcommands.GiveKeyCommand;
import id.seria.gacha.commands.subcommands.GiveRandomRewardCommand;
import id.seria.gacha.commands.subcommands.OpenCrateCommand;
import id.seria.gacha.commands.subcommands.PayKeyCommand;
import id.seria.gacha.commands.subcommands.PlaceCrateCommand;
import id.seria.gacha.commands.subcommands.PluginEditorCommand;
import id.seria.gacha.commands.subcommands.PreviewCrateCommand;
import id.seria.gacha.commands.subcommands.ReloadPluginCommand;
import id.seria.gacha.commands.subcommands.ResetCooldownCommand;
import id.seria.gacha.commands.subcommands.ResetGlobalWinLimitCommand;
import id.seria.gacha.commands.subcommands.ResetGlobalWinLimitCooldownCommand;
import id.seria.gacha.commands.subcommands.ResetKeyAllCommand;
import id.seria.gacha.commands.subcommands.ResetPlayerWinLimitCommand;
import id.seria.gacha.commands.subcommands.ResetPlayerWinLimitCooldownCommand;
import id.seria.gacha.commands.subcommands.RestartCooldownsCommand;
import id.seria.gacha.commands.subcommands.SetKeyCommand;
import id.seria.gacha.commands.subcommands.SetLanguageCommand;
import id.seria.gacha.commands.subcommands.SummonCrateCommand;
import id.seria.gacha.commands.subcommands.SummonKeyCommand;
import id.seria.gacha.commands.subcommands.SummonRewardCommand;
import id.seria.gacha.commands.subcommands.TakeCrateRerollCommand;
import id.seria.gacha.commands.subcommands.TakeKeyCommand;
import id.seria.gacha.commands.subcommands.VirtualKeysCommand;
import id.seria.gacha.commands.tabcompletes.CommandTabCompletes;
import id.seria.gacha.commands.validations.CrateValidations;
import id.seria.gacha.commands.validations.KeyValidations;
import id.seria.gacha.commands.validations.RewardValidations;
import id.seria.gacha.configurations.SettingsConfiguration;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.sdk.core.commands.CommandNode;
import id.seria.gacha.sdk.core.commands.NodeParameter;
import id.seria.gacha.sdk.platforms.server.commands.ServerCommand;
import id.seria.gacha.sdk.platforms.server.commands.TabCompletes;
import org.bukkit.entity.Player;

public class CratesCommand extends ServerCommand {
   public static CratesCommand INSTANCE;

   public CratesCommand() {
      super("phoenixcrates", "phoenixc", "pcrate", "pcrates", "crate", "crates");
      this.setDescription("Main command for Phoenix Crates");
      boolean premium = Crates.getPlugin().getEdition() == Edition.PREMIUM;
      if (premium) {
         this.registerNode(
            CommandNode.builder("createCrate")
               .description("Create crate type")
               .parameter("crate", CrateType.class, CommandTabCompletes.RANDOM_IDENTIFIER, CrateValidations.THROW_IF_PRESENT)
               .permission("phoenixcrates.command.create")
               .onExecute(new CreateCrateCommand())
         );
      }

      this.registerNode(
         CommandNode.builder("deleteCrate")
            .description("Delete crate type")
            .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
            .permission("phoenixcrates.command.delete")
            .onExecute(new DeleteCrateCommand())
      );
      this.registerNode(
         CommandNode.builder("giveCrate")
            .description("Give yourself a crate to be placed")
            .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
            .parameter("player", Player.class, TabCompletes.ONLINE_PLAYERS)
            .parameter("amount", Integer.class, true, 1)
            .parameter("-s", String.class, true, "")
            .permission("phoenixcrates.command.givecrate")
            .onExecute(new GiveCrateCommand())
      );
      this.registerNode(
         CommandNode.builder("giveCrateAll")
            .description("Give every online player a crate")
            .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
            .parameter("amount", Integer.class, true, 1)
            .parameter("-s", String.class, true, "")
            .permission("phoenixcrates.command.givecrateall")
            .onExecute(new GiveCrateAllCommand())
      );
      this.registerNode(
         CommandNode.builder("giveKey")
            .description("Give a player a key")
            .parameter("key", Key.class, CommandTabCompletes.KEYS, KeyValidations.THROW_IF_ABSENT)
            .parameter("player", Player.class, TabCompletes.ONLINE_PLAYERS)
            .parameter("amount", Integer.class, true, 1)
            .parameter("-s", String.class, true, "")
            .permission("phoenixcrates.command.givekey")
            .onExecute(new GiveKeyCommand())
      );
      this.registerNode(
         CommandNode.builder("giveKeyAll")
            .description("Give every online player a key")
            .parameter("key", Key.class, CommandTabCompletes.KEYS, KeyValidations.THROW_IF_ABSENT)
            .parameter("amount", Integer.class, true, 1)
            .parameter("-s", String.class, true, "")
            .permission("phoenixcrates.command.givekeyall")
            .onExecute(new GiveKeyAllCommand())
      );
      this.registerNode(
         CommandNode.builder("takeKey")
            .description("Set player virtual keys")
            .parameter("key", Key.class, CommandTabCompletes.KEYS, KeyValidations.THROW_IF_ABSENT)
            .parameter("player", Player.class)
            .parameter("amount", Integer.class, true, 1)
            .parameter("-s", String.class, true, "")
            .permission("phoenixcrates.command.takekey")
            .onExecute(new TakeKeyCommand())
      );
      if (premium) {
         this.registerNode(
            CommandNode.builder("resetKeyAll")
               .description("Reset a key for all players")
               .parameter("key", Key.class, CommandTabCompletes.KEYS, KeyValidations.THROW_IF_ABSENT)
               .permission("phoenixcrates.command.resetkeyall")
               .onExecute(new ResetKeyAllCommand())
         );
         this.registerNode(
            CommandNode.builder("setKey")
               .description("Set player virtual keys")
               .parameter("key", Key.class, CommandTabCompletes.KEYS, KeyValidations.THROW_IF_ABSENT)
               .parameter("player", Player.class)
               .parameter("amount", Integer.class)
               .permission("phoenixcrates.command.setkey")
               .onExecute(new SetKeyCommand())
         );
      }

      this.registerNode(
         CommandNode.builder("preview")
            .description("Allows you to preview crate rewards")
            .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
            .parameter("player", Player.class, true)
            .permission("phoenixcrates.command.preview")
            .onExecute(new PreviewCrateCommand())
      );
      if (premium) {
         this.registerNode(
            CommandNode.builder("giveReroll")
               .description("Give a player rerolls")
               .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
               .parameter("player", Player.class, TabCompletes.ONLINE_PLAYERS)
               .parameter("amount", Integer.class, true, 1)
               .permission("phoenixcrates.command.givereroll")
               .onExecute(new GiveCrateRerollCommand())
         );
         this.registerNode(
            CommandNode.builder("takeReroll")
               .description("Takes player rerolls")
               .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
               .parameter("player", Player.class)
               .parameter("amount", Integer.class, true)
               .permission("phoenixcrates.command.takereroll")
               .onExecute(new TakeCrateRerollCommand())
         );
         this.registerNode(
            CommandNode.builder("editCrate")
               .description("Edit crate")
               .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
               .permission("phoenixcrates.command.edit")
               .onExecute(newPremiumOnlyExecutable("EditCrateCommand"))
         );
         this.registerNode(
            CommandNode.builder("editKey")
               .description("Edit key")
               .parameter("key", Key.class, CommandTabCompletes.KEYS, KeyValidations.THROW_IF_ABSENT)
               .permission("phoenixcrates.command.edit")
               .onExecute(newPremiumOnlyExecutable("EditKeyCommand"))
         );
      }

      this.registerNode(
         CommandNode.builder("giveReward")
            .description("Give a player a crate reward")
            .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
            .parameter("reward", CustomReward.class, CommandTabCompletes.REWARDS, RewardValidations.THROW_IF_ABSENT)
            .parameter("player", Player.class, TabCompletes.ONLINE_PLAYERS)
            .permission("phoenixcrates.command.givereward")
            .onExecute(new GiveCrateRewardCommand())
      );
      this.registerNode(
         CommandNode.builder("giveRandomReward")
            .description("Give a player a random crate reward")
            .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
            .parameter("player", Player.class, TabCompletes.ONLINE_PLAYERS)
            .parameter("-s", String.class, true, "")
            .permission("phoenixcrates.command.giverandomreward")
            .onExecute(new GiveRandomRewardCommand())
      );
      this.registerNode(
         CommandNode.builder("editor")
            .description("Allows you to edit all crates in-game")
            .permission("phoenixcrates.command.editor")
            .onExecute(new PluginEditorCommand())
      );
      this.registerNode(
         CommandNode.builder("setLanguage")
            .description("Set the plugin default language")
            .parameter("language", String.class, CommandTabCompletes.LANGUAGES)
            .permission("phoenixcrates.command.setlanguage")
            .onExecute(new SetLanguageCommand())
      );
      this.registerNode(
         CommandNode.builder("reload")
            .description("Reload all PhoenixCrates configuration files")
            .permission("phoenixcrates.command.reload")
            .onExecute(new ReloadPluginCommand())
      );
      if (premium) {
         SettingsConfiguration config = Crates.getConfiguration();
         if (Boolean.TRUE.equals(config.getVirtualKeysCommandEnabled())) {
            this.registerNode(CommandNode.builder("virtualKeys").description("View your virtual keys").onExecute(new VirtualKeysCommand()));
            String vkAlternative = config.getVirtualKeysCommandAlternative();
            if (vkAlternative != null && !vkAlternative.isEmpty()) {
               this.registerNode(CommandNode.builder(vkAlternative).description("View your virtual keys").onExecute(new VirtualKeysCommand()));
            }
         }

         if (Boolean.TRUE.equals(config.getPayKeyCommandEnabled())) {
            this.registerNode(
               CommandNode.builder("payKey")
                  .description("Pay virtual keys to another player")
                  .parameter("key", Key.class, CommandTabCompletes.VIRTUAL_KEYS, KeyValidations.THROW_IF_ABSENT)
                  .parameter("player", Player.class, TabCompletes.ONLINE_PLAYERS)
                  .parameter("amount", Integer.class, true, 1)
                  .onExecute(new PayKeyCommand())
            );
            String pkAlternative = config.getPayKeyCommandAlternative();
            if (pkAlternative != null && !pkAlternative.isEmpty() && !pkAlternative.equals("payKey")) {
               this.registerNode(
                  CommandNode.builder(pkAlternative)
                     .description("Pay virtual keys to another player")
                     .parameter("key", Key.class, CommandTabCompletes.VIRTUAL_KEYS, KeyValidations.THROW_IF_ABSENT)
                     .parameter("player", Player.class, TabCompletes.ONLINE_PLAYERS)
                     .parameter("amount", Integer.class, true, 1)
                     .onExecute(new PayKeyCommand())
               );
            }
         }

         this.registerNode(
            CommandNode.builder("open")
               .description("Open a crate (player only)")
               .parameter("crate", CrateType.class, CommandTabCompletes.OPEN_COMMAND_CRATES, CrateValidations.THROW_IF_ABSENT)
               .permission("phoenixcrates.command.open")
               .onExecute(new OpenCrateCommand())
         );
      }

      this.registerNode(
         CommandNode.builder("checkKeys")
            .description("Check a player's virtual keys")
            .parameter("player", Player.class, TabCompletes.ONLINE_PLAYERS)
            .permission("phoenixcrates.command.checkkeys")
            .onExecute(new CheckKeysCommand())
      );
      if (premium) {
         this.registerNode(
            CommandNode.builder("resetGlobalWinLimit")
               .description("Reset the global win count for a crate reward")
               .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
               .parameter("reward", CustomReward.class, CommandTabCompletes.REWARDS, RewardValidations.THROW_IF_ABSENT)
               .permission("phoenixcrates.command.resetglobalwinlimit")
               .onExecute(new ResetGlobalWinLimitCommand())
         );
         this.registerNode(
            CommandNode.builder("resetCooldown")
               .description("Reset crate cooldown for a player")
               .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
               .parameter("player", Player.class)
               .parameter("-s", String.class, true, "")
               .permission("phoenixcrates.command.resetcooldown")
               .onExecute(new ResetCooldownCommand())
         );
         this.registerNode(
            CommandNode.builder("resetPlayerWinLimit")
               .description("Reset a player's win count for a crate reward")
               .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
               .parameter("reward", CustomReward.class, CommandTabCompletes.REWARDS, RewardValidations.THROW_IF_ABSENT)
               .parameter("player", Player.class)
               .permission("phoenixcrates.command.resetplayerwinlimit")
               .onExecute(new ResetPlayerWinLimitCommand())
         );
         this.registerNode(
            CommandNode.builder("resetPlayerWinLimitCooldown")
               .description("Reset a player's win limit cooldown for a crate reward")
               .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
               .parameter("reward", CustomReward.class, CommandTabCompletes.REWARDS, RewardValidations.THROW_IF_ABSENT)
               .parameter("player", Player.class)
               .permission("phoenixcrates.command.resetplayerwinlimitcooldown")
               .onExecute(new ResetPlayerWinLimitCooldownCommand())
         );
         this.registerNode(
            CommandNode.builder("resetGlobalWinLimitCooldown")
               .description("Reset the global win limit cooldown for a crate reward")
               .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
               .parameter("reward", CustomReward.class, CommandTabCompletes.REWARDS, RewardValidations.THROW_IF_ABSENT)
               .permission("phoenixcrates.command.resetglobalwinlimitcooldown")
               .onExecute(new ResetGlobalWinLimitCooldownCommand())
         );
         this.registerNode(
            CommandNode.builder("restartCooldowns")
               .description("Restart the time-limited cooldown for a crate reward")
               .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
               .parameter("reward", String.class, CommandTabCompletes.TIME_LIMITED_REWARDS)
               .permission("phoenixcrates.command.restartcooldowns")
               .onExecute(new RestartCooldownsCommand())
         );
      }

      if (premium) {
         this.registerNode(
            CommandNode.builder("summonCrate")
               .description("Summon a crate item at a world location")
               .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
               .parameter("world", String.class, CommandTabCompletes.WORLDS)
               .parameter("x", Integer.class)
               .parameter("y", Integer.class)
               .parameter("z", Integer.class)
               .permission("phoenixcrates.command.summon")
               .onExecute(new SummonCrateCommand())
         );
         this.registerNode(
            CommandNode.builder("summonKey")
               .description("Summon a key item at a world location")
               .parameter("key", Key.class, CommandTabCompletes.KEYS, KeyValidations.THROW_IF_ABSENT)
               .parameter("world", String.class, CommandTabCompletes.WORLDS)
               .parameter("x", Integer.class)
               .parameter("y", Integer.class)
               .parameter("z", Integer.class)
               .permission("phoenixcrates.command.summon")
               .onExecute(new SummonKeyCommand())
         );
         this.registerNode(
            CommandNode.builder("summonReward")
               .description("Summon a reward display item at a world location")
               .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
               .parameter("reward", CustomReward.class, CommandTabCompletes.REWARDS, RewardValidations.THROW_IF_ABSENT)
               .parameter("world", String.class, CommandTabCompletes.WORLDS)
               .parameter("x", Integer.class)
               .parameter("y", Integer.class)
               .parameter("z", Integer.class)
               .permission("phoenixcrates.command.summon")
               .onExecute(new SummonRewardCommand())
         );
      }

      this.registerNode(
         CommandNode.builder("attachCrate")
            .description("Attach a crate to the block you are looking at")
            .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
            .permission("phoenixcrates.command.attach")
            .onExecute(new AttachCrateCommand())
      );
      this.registerNode(
         CommandNode.builder("detachCrate")
            .description("Detach the crate from the block you are looking at")
            .permission("phoenixcrates.command.detach")
            .onExecute(new DetachCrateCommand())
      );
      if (premium) {
         this.registerNode(
            CommandNode.builder("placeCrate")
               .description("Place a crate at a specific world location")
               .parameter("crate", CrateType.class, CommandTabCompletes.CRATES, CrateValidations.THROW_IF_ABSENT)
               .parameter("world", String.class, CommandTabCompletes.WORLDS)
               .parameter("x", Integer.class)
               .parameter("y", Integer.class)
               .parameter("z", Integer.class)
               .parameters(
                  NodeParameter.builder("facing", String.class).optional().defaultValue("SOUTH").onTabComplete(CommandTabCompletes.FACING_DIRECTIONS).build()
               )
               .permission("phoenixcrates.command.place")
               .onExecute(new PlaceCrateCommand())
         );
      }

      INSTANCE = this;
   }

   private static CrateCommandExecutable newPremiumOnlyExecutable(String simpleClassName) {
      try {
         Class<?> clazz = Class.forName("id.seria.gacha.commands.subcommands." + simpleClassName);
         return (CrateCommandExecutable)clazz.getDeclaredConstructor().newInstance();
      } catch (ReflectiveOperationException e) {
         throw new IllegalStateException("Could not instantiate " + simpleClassName, e);
      }
   }
}
