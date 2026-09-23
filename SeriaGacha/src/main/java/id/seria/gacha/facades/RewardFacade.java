package id.seria.gacha.facades;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.api.crate.CrateInstance;
import id.seria.gacha.api.crate.events.CrateRewardWinItemsEvent;
import id.seria.gacha.api.events.items.ItemDeliverySource;
import id.seria.gacha.api.internal.Item;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.api.reward.Reward;
import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.utils.utility.ListUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.managers.players.PlayerData;
import id.seria.gacha.managers.reward.CustomAlternativeReward;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.managers.reward.RewardMessageType;
import id.seria.gacha.managers.reward.WrappedReward;
import id.seria.gacha.placeholders.Placeholder;
import id.seria.gacha.registries.rewards.PipelineContext;
import id.seria.gacha.registries.rewards.PipelineRegistry;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RewardFacade {
   public RewardFacade() {
   }

   public static void giveReward(BaseReward reward, Player player) {
      giveReward(null, reward, player);
   }

   public static void giveReward(@Nullable CrateInstance crate, BaseReward reward, Player player) {
      Crates.getScheduler().execute(player, () -> {
         List<ItemStack> items = ListUtil.cap(buildWinItems(crate, reward, player), reward.getMaxWinItemAmount());
         if (!items.isEmpty()) {
            for (ItemStack item : items) {
               setLorePlaceholders(reward, player, item);
            }

            PlayerData playerData = Crates.getPlayersManager().getCachedDataNow(player);
            ItemDeliveryFacade.DeliveryResult result = ItemDeliveryFacade.deliver(playerData, player, items, ItemDeliverySource.REWARD_GRANT);
            if (result.hasDroppedItems()) {
               Translations.send(player, Translations.t("editor.errors.inventory-full-reward"));
            }
         }

         for (String rawCommand : ListUtil.cap(new ArrayList<>(reward.getWinCommands()), reward.getMaxWinCommandAmount())) {
            String command = rawCommand.replace("%player%", player.getName());
            if (rawCommand.startsWith("*")) {
               Bukkit.dispatchCommand(player, Placeholders.setPlaceholders(command.substring(1), player));
            } else {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Placeholders.setPlaceholders(command, player));
            }
         }
      });
   }

   public static List<ItemStack> buildWinItems(@Nullable CrateInstance crate, BaseReward reward, Player player) {
      List<ItemStack> items = reward.getWinItems()
         .stream()
         .filter(Objects::nonNull)
         .map(Item::build)
         .<ItemStack>map(ItemStack::clone)
         .collect(Collectors.toCollection(ArrayList::new));

      for (ItemStack item : items) {
         setLorePlaceholders(reward, player, item);
      }

      if (crate == null) {
         return items;
      }

      CrateRewardWinItemsEvent itemsEvent = new CrateRewardWinItemsEvent(crate, reward, player, items);
      Bukkit.getPluginManager().callEvent(itemsEvent);
      List<ItemStack> eventItems = itemsEvent.getWinItems();
      return eventItems != null && !eventItems.isEmpty()
         ? eventItems.stream().filter(Objects::nonNull).<ItemStack>map(ItemStack::clone).collect(Collectors.toCollection(ArrayList::new))
         : new ArrayList<>();
   }

   private static void setLorePlaceholders(BaseReward reward, Player player, ItemStack item) {
      if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
         Map<String, String> tokens = Placeholder.context(reward).with("viewer", player).resolvedTokens();
         tokens.put("%player%", player.getName());
         if (containsAnyToken(item.getItemMeta().getLore(), tokens.keySet())) {
            RItemStack rItem = RItemStack.of(item).replaceLoreTokens(tokens);
            item.setItemMeta(rItem.bukkit().getItemMeta());
         }
      }
   }

   private static boolean containsAnyToken(List<String> lore, Set<String> tokens) {
      if (lore == null) {
         return false;
      }

      for (String line : lore) {
         if (line != null) {
            for (String token : tokens) {
               if (line.contains(token)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static CustomReward buildEmpty() {
      return new CustomReward(
         true,
         false,
         "reward_" + CustomReward.ID_COUNTER++,
         BaseItem.from(XMaterial.AIR),
         new ArrayList<>(),
         100.0,
         false,
         false,
         false,
         new ArrayList<>(),
         -1,
         -1,
         0L,
         0L,
         0L,
         0L,
         -1,
         1,
         new ArrayList<>(),
         new CustomAlternativeReward(false, BaseItem.vanilla(XMaterial.DIAMOND_PICKAXE, "&aAlternative Item"), false, false, true, false, new ArrayList<>())
      );
   }

   public static CustomReward buildDefault() {
      return new CustomReward(
         true,
         false,
         "reward_" + CustomReward.ID_COUNTER++,
         BaseItem.vanilla(XMaterial.DIRT, "&7Dirt"),
         Lists.newArrayList(new Item[]{BaseItem.from(XMaterial.DIRT)}),
         100.0,
         false,
         false,
         false,
         new ArrayList<>(),
         -1,
         -1,
         0L,
         0L,
         0L,
         0L,
         -1,
         1,
         new ArrayList<>(),
         new CustomAlternativeReward(false, BaseItem.vanilla(XMaterial.DIAMOND_PICKAXE, "&aAlternative Item"), false, false, true, false, new ArrayList<>())
      );
   }

   public static CustomReward buildDefault(ItemStack itemStack) {
      return new CustomReward(
         true,
         false,
         "reward_" + CustomReward.ID_COUNTER++,
         BaseItem.from(itemStack),
         Lists.newArrayList(new Item[]{BaseItem.from(itemStack)}),
         100.0,
         false,
         false,
         false,
         new ArrayList<>(),
         -1,
         -1,
         0L,
         0L,
         0L,
         0L,
         -1,
         1,
         new ArrayList<>(),
         new CustomAlternativeReward(false, BaseItem.vanilla(XMaterial.DIAMOND_PICKAXE, "&aAlternative Item"), false, false, true, false, new ArrayList<>())
      );
   }

   public static WrappedReward wrap(Reward reward) {
      return new WrappedReward(
         reward.getIdentifier(),
         reward.getDisplayItem(),
         reward.getWinItems(),
         reward.getWinCommands(),
         reward.getWeight(),
         reward.isBroadcast(),
         reward.isSharePlayersOnline(),
         reward.isAllowWinMessage(),
         reward instanceof CustomReward ? ((CustomReward)reward).getRewardMessageType() : RewardMessageType.CHAT,
         false,
         reward.getMaxWinItemAmount(),
         reward.getMaxWinCommandAmount()
      );
   }

   public static double calculateChance(BaseReward reward, List<? extends BaseReward> rewards, Player viewer) {
      PipelineContext<BaseReward> context = PipelineContext.of(reward).put("availableRewards", rewards).put("viewer", viewer);
      return PipelineRegistry.calculate(BaseReward.class, PipelineRegistry.getRewardChanceTargetId(), context);
   }
}
