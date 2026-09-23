package id.seria.gacha.managers.crates.animations.opening.phases.second;

import com.google.common.collect.Iterables;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.container.AbstractContainer;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.CloseViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.OpenViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.managers.crates.animations.opening.utility.GuiAnimationHelper;
import id.seria.gacha.managers.crates.animations.opening.utility.RewardDisplayCache;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.inventory.ItemStack;

public class SecondPhaseRainbowGuiAnimation extends PhaseRunnerImpl {
   private static final int WINNER_SLOT = 13;
   private static final int CHAOS_TICKS = 18;
   private static final int PAIR_CYCLE_TICKS = 10;
   private static final int WINNER_SLOW_TICKS = 18;
   private static final int WINNER_REVEAL_TICK = 76;
   private static final int RESULT_HOLD_TICKS = 45;
   private static final int LOCK_FLASH_TICKS = 5;
   private static final int[][] LOCK_PAIRS = new int[][]{{9, 17}, {10, 16}, {11, 15}, {12, 14}};
   private SecondPhaseRainbowGuiAnimation.RainbowContainer container;
   private final RewardDisplayCache displayCache = new RewardDisplayCache();
   private List<? extends BaseReward> availableRewards;
   private BaseReward winningReward;
   private int[] slotOffsets;
   private final int[] lockFlashRemaining = new int[27];
   private final Map<Integer, BaseReward> lockedRewards = new HashMap<>();
   private final boolean[] locked = new boolean[27];
   private int globalTick;
   private int frameCounter;
   private int frameGate;
   private boolean winnerRevealed;
   private int settleTicks;
   private boolean allowReopenOnClose;

   public SecondPhaseRainbowGuiAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.availableRewards = this.crate.getAvailableRewards(this.player);
      if (this.availableRewards != null && !this.availableRewards.isEmpty()) {
         this.winningReward = (BaseReward)Iterables.getFirst(this.handler.generateRewards(), null);
         if (this.winningReward == null) {
            this.winningReward = MathUtil.random(this.availableRewards);
         }

         int rewardCount = this.availableRewards.size();
         this.slotOffsets = new int[27];

         for (int s = 9; s <= 17; s++) {
            this.slotOffsets[s] = ThreadLocalRandom.current().nextInt(rewardCount);
         }

         this.allowReopenOnClose = true;
         this.container = new SecondPhaseRainbowGuiAnimation.RainbowContainer(this.handler.getPlugin(), this);
         this.container.open(this.player);
         this.playSound(this.animationLocation, XSound.BLOCK_ENDER_CHEST_OPEN, 0.4F, 0.8F);
      } else {
         this.nextPhase();
      }
   }

   @Override
   public void tick() {
      if (this.container == null) {
         this.nextPhase();
      } else {
         this.globalTick++;

         for (int s = 9; s <= 17; s++) {
            if (this.lockFlashRemaining[s] > 0) {
               this.lockFlashRemaining[s]--;
            }
         }

         if (this.winnerRevealed) {
            this.settleTicks++;
            if (this.settleTicks % 2 == 0) {
               this.container.updateView(this.player);
            }

            if (this.settleTicks >= 45) {
               this.nextPhase();
            }
         } else {
            this.frameGate++;
            if (this.frameGate >= this.getFrameInterval()) {
               this.frameGate = 0;
               this.frameCounter++;
            }

            for (int pair = 0; pair < LOCK_PAIRS.length; pair++) {
               int lockTick = 18 + (pair + 1) * 10;
               if (this.globalTick == lockTick) {
                  for (int slot : LOCK_PAIRS[pair]) {
                     this.locked[slot] = true;
                     this.lockFlashRemaining[slot] = 5;
                     this.lockedRewards.put(slot, MathUtil.random(this.availableRewards));
                  }

                  float pitch = 0.55F + pair * 0.22F;
                  this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_PLING, 0.65F, pitch);
                  if (pair >= 2) {
                     this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HAT, 0.3F, 1.5F + pair * 0.25F);
                  }
               }
            }

            int winnerSlowStart = 58;
            if (this.globalTick < 58 && this.globalTick % 3 == 0) {
               this.playSound(this.animationLocation, XSound.UI_BUTTON_CLICK, 0.04F, 1.2F);
            }

            if (this.globalTick >= 58 && this.globalTick < 76 && this.frameGate == 0) {
               float pitch = 1.0F + (this.globalTick - 58) * 0.05F;
               this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HAT, 0.35F, Math.min(pitch, 2.0F));
            }

            if (this.globalTick >= 76) {
               this.winnerRevealed = true;
               this.container.updateView(this.player);
               this.playWinSound();
            } else {
               this.container.updateView(this.player);
            }
         }
      }
   }

   private void playWinSound() {
      this.playSound(this.animationLocation, XSound.ENTITY_PLAYER_LEVELUP, 0.6F, 1.4F);
      this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_BELL, 0.7F, 2.0F);
   }

   private int getFrameInterval() {
      int winnerSlowStart = 58;
      if (this.globalTick < 58) {
         return 1;
      } else {
         int slowProgress = this.globalTick - 58;
         if (slowProgress < 5) {
            return 2;
         } else if (slowProgress < 10) {
            return 4;
         } else {
            return slowProgress < 14 ? 7 : 10;
         }
      }
   }

   private void render(RenderViewContext context) {
      if (this.winnerRevealed) {
         ItemStack white = GuiAnimationHelper.buildWhiteGlass(this.properties);

         for (int col = 0; col < 9; col++) {
            context.setSlot(col, white.clone());
            context.setSlot(col + 18, white.clone());
         }

         ItemStack marker = this.settleTicks / 4 % 2 == 0
            ? GuiAnimationHelper.buildMarkerNormal(this.properties)
            : GuiAnimationHelper.buildMarkerFlicker(this.properties);
         context.setSlot(4, marker.clone());
         context.setSlot(22, marker.clone());
      } else {
         int bgOffset = this.globalTick / 2;

         for (int col = 0; col < 9; col++) {
            XMaterial color = GuiAnimationHelper.RAINBOW_PALETTE[(bgOffset + col) % GuiAnimationHelper.RAINBOW_PALETTE.length];
            context.setSlot(col, GuiAnimationHelper.buildRainbowPane(color));
            context.setSlot(col + 18, GuiAnimationHelper.buildRainbowPane(color));
         }
      }

      for (int s = 9; s <= 17; s++) {
         if (s == 13) {
            this.renderCenterSlot(context);
         } else if (this.locked[s]) {
            this.renderLockedSlot(context, s);
         } else {
            this.renderCyclingReward(context, s);
         }
      }
   }

   private void renderCenterSlot(RenderViewContext context) {
      if (this.winnerRevealed) {
         this.renderReward(context, 13, this.winningReward);
      } else {
         this.renderCyclingReward(context, 13);
      }
   }

   private void renderLockedSlot(RenderViewContext context, int slot) {
      if (this.lockFlashRemaining[slot] > 0) {
         ItemStack flash = this.lockFlashRemaining[slot] % 2 == 0
            ? GuiAnimationHelper.buildRainbowPane(XMaterial.LIME_STAINED_GLASS_PANE)
            : GuiAnimationHelper.buildWhiteGlass(this.properties);
         context.setSlot(slot, flash);
      } else {
         BaseReward reward = this.lockedRewards.get(slot);
         if (reward != null) {
            this.renderReward(context, slot, reward);
         }
      }
   }

   private void renderCyclingReward(RenderViewContext context, int slot) {
      int idx = (this.frameCounter + this.slotOffsets[slot]) % this.availableRewards.size();
      this.renderReward(context, slot, this.availableRewards.get(idx));
   }

   private void renderReward(RenderViewContext context, int slot, BaseReward reward) {
      RItemStack rItem = RItemStack.of(reward.getDisplayItem().build());
      RewardDisplayCache.Entry cached = this.displayCache.get(reward, this.player, this.availableRewards, this.properties);
      new VirtualItem().withSlot(slot).withItem(rItem).withDisplayName(cached.displayName).withLore(new ArrayList<>(cached.lore)).render(context);
   }

   @Override
   public void unload() {
      this.allowReopenOnClose = false;
      if (this.player != null && this.player.isOnline() && this.container != null && this.container.getViewersContext().containsKey(this.player.getUniqueId())) {
         this.player.closeInventory();
      }

      this.container = null;
   }

   private static class RainbowContainer extends AbstractContainer {
      private final SecondPhaseRainbowGuiAnimation animation;

      private RainbowContainer(ServerPlugin plugin, SecondPhaseRainbowGuiAnimation animation) {
         super(plugin);
         this.animation = animation;
      }

      @Override
      public void onInitialize(ContainerView view) {
      }

      @Override
      public void onOpen(OpenViewContext context) {
         context.setContainerTitle(Translations.r(GuiAnimationHelper.getTitle(this.animation.properties)));
         context.setContainerType(ContainerType.CONTAINER_9X3);
      }

      @Override
      public void onRender(RenderViewContext context) {
         this.animation.render(context);
      }

      @Override
      public void onClose(CloseViewContext context) {
         if (this.animation.allowReopenOnClose && context.getViewer().isOnline()) {
            if (this.animation.crate.getType().getExtraSettings().isSkipOnEsc()) {
               this.animation.allowReopenOnClose = false;
               this.animation.playWinSound();
               this.animation.nextPhase();
            } else {
               this.plugin.getScheduler().runSync(context.getViewer(), () -> {
                  if (this.animation.allowReopenOnClose && context.getViewer().isOnline()) {
                     this.open(context.getViewer());
                  }
               });
            }
         }
      }
   }
}
