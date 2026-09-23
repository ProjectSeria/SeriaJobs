package id.seria.gacha.commands.tabcompletes;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.plugin.commands.components.TabCompletion;
import id.seria.gacha.commands.subcommands.SetLanguageCommand;
import id.seria.gacha.internal.language.Language;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;

public class CommandTabCompletes {
   public static final TabCompletion<NodeInvocationContext> KEYS = context -> Crates.getKeysManager()
      .getRegisteredKeys()
      .stream()
      .map(Key::getIdentifier)
      .collect(Collectors.toList());
   public static final TabCompletion<NodeInvocationContext> CRATES = context -> Crates.getCratesManager()
      .getRegisteredTypes()
      .stream()
      .map(CrateType::getIdentifier)
      .collect(Collectors.toList());
   public static final TabCompletion<NodeInvocationContext> ENABLED_CRATES = context -> Crates.getCratesManager()
      .getRegisteredTypes()
      .stream()
      .filter(CrateType::isEnabled)
      .map(CrateType::getIdentifier)
      .collect(Collectors.toList());
   public static final TabCompletion<NodeInvocationContext> OPEN_COMMAND_CRATES = context -> Crates.getCratesManager()
      .getRegisteredTypes()
      .stream()
      .filter(CrateType::isEnabled)
      .filter(CrateType::isAllowOpenCommand)
      .map(CrateType::getIdentifier)
      .collect(Collectors.toList());
   public static final TabCompletion<NodeInvocationContext> REWARDS = context -> {
      CrateType crate = context.getComputedParameter("crate", CrateType.class);
      return crate == null ? null : crate.getAvailableRewards().stream().map(CustomReward::getIdentifier).collect(Collectors.toList());
   };
   public static final TabCompletion<NodeInvocationContext> TIME_LIMITED_REWARDS = context -> {
      CrateType crate = context.getComputedParameter("crate", CrateType.class);
      if (crate == null) {
         return null;
      }

      List<String> identifiers = new ArrayList<>();
      identifiers.add("*");
      crate.getRegisteredRewards().stream().filter(reward -> reward.getTimeLimitedCooldown() > 0L).map(CustomReward::getIdentifier).forEach(identifiers::add);
      return identifiers;
   };
   public static final TabCompletion<NodeInvocationContext> RANDOM_IDENTIFIER = context -> {
      String randomIdentifier = "crate_" + (int)(Math.random() * 9000.0 + 1000.0);
      return Collections.singletonList(randomIdentifier);
   };
   public static final TabCompletion<NodeInvocationContext> WORLDS = context -> Bukkit.getWorlds().stream().map(w -> w.getName()).collect(Collectors.toList());
   public static final TabCompletion<NodeInvocationContext> VIRTUAL_KEYS = context -> Crates.getKeysManager()
      .getRegisteredKeys()
      .stream()
      .filter(Key::isVirtual)
      .map(Key::getIdentifier)
      .collect(Collectors.toList());
   public static final TabCompletion<NodeInvocationContext> FACING_DIRECTIONS = context -> Arrays.asList("SOUTH", "NORTH", "EAST", "WEST");
   public static final TabCompletion<NodeInvocationContext> LANGUAGES = context -> Arrays.stream(Language.values())
      .filter(SetLanguageCommand.Languages::isAvailable)
      .map(language -> language.name().toLowerCase())
      .collect(Collectors.toList());

   public CommandTabCompletes() {
   }
}
