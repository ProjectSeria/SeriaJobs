package id.seria.gacha.api.session;

import id.seria.gacha.api.key.Key;
import id.seria.gacha.api.reward.Reward;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class OpeningContext {
   protected final UUID playerId;
   protected Map<? extends Key, Integer> keysAmount;

   private OpeningContext(Player player) {
      if (player == null) {
         throw new IllegalArgumentException("Player cannot be null");
      }

      this.playerId = player.getUniqueId();
   }

   public Player getPlayer() {
      Player resolvedPlayer = Bukkit.getPlayer(this.playerId);
      if (resolvedPlayer == null) {
         throw new IllegalStateException("Player " + this.playerId + " is not available for this opening context");
      } else {
         return resolvedPlayer;
      }
   }

   public abstract int getRequiredKeys();

   public static OpeningContext random(Player player, boolean requireKeyInMainHand, int openingQuantity) {
      return new OpeningContext.RandomOpeningContext(player, requireKeyInMainHand, openingQuantity);
   }

   public static <T extends Reward> OpeningContext selective(Player player, T selectedReward) {
      return new OpeningContext.SelectiveOpeningContext(player, selectedReward);
   }

   public static <T extends Reward> OpeningContext massSelective(Player player, List<T> selectedRewards) {
      return new OpeningContext.MassSelectiveOpeningContext(player, selectedRewards);
   }

   @Generated
   public UUID getPlayerId() {
      return this.playerId;
   }

   @Generated
   public Map<? extends Key, Integer> getKeysAmount() {
      return this.keysAmount;
   }

   @Generated
   public void setKeysAmount(Map<? extends Key, Integer> keysAmount) {
      this.keysAmount = keysAmount;
   }

   public static class MassSelectiveOpeningContext extends OpeningContext {
      private final List<? extends Reward> selectedRewards;

      public MassSelectiveOpeningContext(Player player, List<? extends Reward> selectedRewards) {
         super(player);
         if (selectedRewards != null && !selectedRewards.isEmpty()) {
            this.selectedRewards = Collections.unmodifiableList(selectedRewards);
         } else {
            throw new IllegalArgumentException("Selected rewards cannot be null or empty");
         }
      }

      @Override
      public int getRequiredKeys() {
         return this.selectedRewards.size();
      }

      @Generated
      public List<? extends Reward> getSelectedRewards() {
         return this.selectedRewards;
      }
   }

   public static class RandomOpeningContext extends OpeningContext {
      private final boolean requireKeyInMainHand;
      private final int openingQuantity;

      public RandomOpeningContext(Player player, boolean requireKeyInMainHand, int openingQuantity) {
         super(player);
         this.requireKeyInMainHand = requireKeyInMainHand;
         this.openingQuantity = openingQuantity;
      }

      @Override
      public int getRequiredKeys() {
         return Math.max(1, this.openingQuantity);
      }

      @Generated
      public boolean isRequireKeyInMainHand() {
         return this.requireKeyInMainHand;
      }

      @Generated
      public int getOpeningQuantity() {
         return this.openingQuantity;
      }
   }

   public static class SelectiveOpeningContext extends OpeningContext {
      private final Reward selectedReward;

      public SelectiveOpeningContext(Player player, Reward selectedReward) {
         super(player);
         if (selectedReward == null) {
            throw new IllegalArgumentException("Selected reward cannot be null");
         }

         this.selectedReward = selectedReward;
      }

      @Override
      public int getRequiredKeys() {
         return Math.max(1, this.selectedReward.getRequiredKeys());
      }

      @Generated
      public Reward getSelectedReward() {
         return this.selectedReward;
      }
   }
}
