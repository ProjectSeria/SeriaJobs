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
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.managers.crates.animations.opening.utility.GuiAnimationHelper;
import id.seria.gacha.managers.crates.animations.opening.utility.RewardDisplayCache;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.inventory.ItemStack;

public class SecondPhaseBlackoutGuiAnimation extends PhaseRunnerImpl {
   private static final int WINNER_SLOT = 13;
   private static final int CHAOS_DURATION = 30;
   private static final int RING_LOCK_INTERVAL = 8;
   private static final int PAUSE_DURATION = 20;
   private static final int WINNER_REVEAL_TICK = 90;
   private static final int RESULT_HOLD_TICKS = 55;
   private static final int LOCK_FLASH_TICKS = 4;
   private static final int[][] LOCK_RINGS = new int[][]{
      {0, 8, 18, 26}, {1, 7, 9, 17, 19, 25}, {2, 6, 10, 16, 20, 24}, {3, 5, 11, 15, 21, 23}, {4, 12, 14, 22}
   };
   private SecondPhaseBlackoutGuiAnimation.BlackoutContainer container;
   private final RewardDisplayCache displayCache = new RewardDisplayCache();
   private List<? extends BaseReward> availableRewards;
   private BaseReward winningReward;
   private int[] slotOffsets;
   private int[] slotLockTick;
   private final boolean[] locked = new boolean[27];
   private int globalTick;
   private int frameCounter;
   private int frameGate;
   private boolean winnerRevealed;
   private int settleTicks;
   private boolean allowReopenOnClose;

   public SecondPhaseBlackoutGuiAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
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

         for (int i = 0; i < 27; i++) {
            this.slotOffsets[i] = ThreadLocalRandom.current().nextInt(rewardCount);
         }

         this.slotLockTick = new int[27];
         Arrays.fill(this.slotLockTick, Integer.MAX_VALUE);

         for (int ring = 0; ring < LOCK_RINGS.length; ring++) {
            int lockTick = 30 + ring * 8;

            for (int slot : LOCK_RINGS[ring]) {
               this.slotLockTick[slot] = lockTick;
            }
         }

         Arrays.fill(this.locked, false);
         this.globalTick = 0;
         this.frameCounter = 0;
         this.frameGate = 0;
         this.winnerRevealed = false;
         this.settleTicks = 0;
         this.allowReopenOnClose = true;
         this.container = new SecondPhaseBlackoutGuiAnimation.BlackoutContainer(this.handler.getPlugin(), this);
         this.container.open(this.player);
         this.playSound(this.animationLocation, XSound.BLOCK_ENDER_CHEST_OPEN, 0.4F, 0.7F);
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
         if (this.winnerRevealed) {
            this.settleTicks++;
            if (this.settleTicks % 3 == 0) {
               this.container.updateView(this.player);
            }

            if (this.settleTicks >= 55) {
               this.nextPhase();
            }
         } else {
            boolean frameAdvanced = false;
            this.frameGate++;
            if (this.frameGate >= this.getFrameInterval()) {
               this.frameGate = 0;
               this.frameCounter++;
               frameAdvanced = true;
            }

            for (int ring = 0; ring < LOCK_RINGS.length; ring++) {
               int lockTick = 30 + ring * 8;
               if (this.globalTick == lockTick) {
                  for (int slot : LOCK_RINGS[ring]) {
                     this.locked[slot] = true;
                  }

                  float pitch = 0.45F + ring * 0.14F;
                  this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_BIT, 0.55F, pitch);
                  frameAdvanced = true;
               }
            }

            this.playTickSound(frameAdvanced);
            if (this.globalTick >= 90) {
               this.winnerRevealed = true;
               this.container.updateView(this.player);
               this.playWinSound();
            } else {
               if (frameAdvanced) {
                  this.container.updateView(this.player);
               }
            }
         }
      }
   }

   private void playWinSound() {
      this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 1.76F);
      this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 2.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.4F);
   }

   private int getFrameInterval() {
      int pauseStart = 30 + LOCK_RINGS.length * 8;
      if (this.globalTick < pauseStart) {
         return 1;
      } else {
         int pauseProgress = this.globalTick - pauseStart;
         if (pauseProgress < 5) {
            return 2;
         } else if (pauseProgress < 10) {
            return 4;
         } else {
            return pauseProgress < 15 ? 6 : 8;
         }
      }
   }

   private void playTickSound(boolean frameAdvanced) {
      int pauseStart = 30 + LOCK_RINGS.length * 8;
      if (this.globalTick < pauseStart) {
         if (this.globalTick % 2 == 0) {
            this.playSound(this.animationLocation, XSound.UI_BUTTON_CLICK, 0.05F, 1.1F);
         }
      } else if (frameAdvanced) {
         float pitch = 1.1F + (this.globalTick - pauseStart) * 0.04F;
         this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HAT, 0.35F, Math.min(pitch, 2.0F));
      }
   }

   private void render(RenderViewContext context) {
      for (int slot = 0; slot < 27; slot++) {
         if (slot == 13) {
            this.renderCenterSlot(context);
         } else if (this.locked[slot]) {
            context.setSlot(slot, this.getLockedGlass(slot));
         } else {
            this.renderCyclingReward(context, slot);
         }
      }

      if (this.winnerRevealed) {
         ItemStack highlight = this.settleTicks / 4 % 2 == 0
            ? GuiAnimationHelper.buildMarkerNormal(this.properties)
            : GuiAnimationHelper.buildMarkerFlicker(this.properties);
         context.setSlot(4, highlight.clone());
         context.setSlot(22, highlight.clone());
      }
   }

   private void renderCenterSlot(RenderViewContext context) {
      if (this.winnerRevealed) {
         this.renderReward(context, 13, this.winningReward);
      } else {
         this.renderCyclingReward(context, 13);
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

   private ItemStack getLockedGlass(int slot) {
      int lockTick = this.slotLockTick[slot];
      int elapsed = this.globalTick - lockTick;
      if (elapsed < 4) {
         return elapsed % 2 == 0 ? GuiAnimationHelper.buildYellowGlass(this.properties) : GuiAnimationHelper.buildWhiteGlass(this.properties);
      } else {
         return GuiAnimationHelper.buildBlackGlass(this.properties);
      }
   }

   @Override
   public void unload() {
      this.allowReopenOnClose = false;
      if (this.player != null && this.player.isOnline() && this.container != null && this.container.getViewersContext().containsKey(this.player.getUniqueId())) {
         this.player.closeInventory();
      }

      this.container = null;
   }

   private static class BlackoutContainer extends AbstractContainer {
      private final SecondPhaseBlackoutGuiAnimation animation;

      private BlackoutContainer(ServerPlugin plugin, SecondPhaseBlackoutGuiAnimation animation) {
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
