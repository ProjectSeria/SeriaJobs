package id.seria.gacha.managers.crates.animations.opening.utility;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.api.sdk.text.Text;
import id.seria.gacha.placeholders.Placeholder;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public final class RewardDisplayCache {
   private final Map<BaseReward, RewardDisplayCache.Entry> cache = new IdentityHashMap<>();

   public RewardDisplayCache() {
   }

   public RewardDisplayCache.Entry get(BaseReward reward, Player viewer, List<? extends BaseReward> availableRewards, AnimationProperties properties) {
      return this.cache.computeIfAbsent(reward, r -> resolve(r, viewer, availableRewards, properties));
   }

   private static RewardDisplayCache.Entry resolve(
      BaseReward reward, Player viewer, List<? extends BaseReward> availableRewards, AnimationProperties properties
   ) {
      RItemStack rItem = RItemStack.of(reward.getDisplayItem().build());
      Placeholder.Context<BaseReward> ctx = Placeholder.context(reward).with("viewer", viewer).with("rewards", availableRewards);
      String displayName = ctx.parse(rItem.displayName().toPlainText());
      List<String> lore = new ArrayList<>(ctx.parse(rItem.lore().stream().map(Text::toPlainText).collect(Collectors.toList())));
      lore.addAll(ctx.parse(GuiAnimationHelper.getChanceLore(properties)));
      return new RewardDisplayCache.Entry(displayName, lore);
   }

   public static final class Entry {
      public final String displayName;
      public final List<String> lore;

      private Entry(String displayName, List<String> lore) {
         this.displayName = displayName;
         this.lore = lore;
      }
   }
}
