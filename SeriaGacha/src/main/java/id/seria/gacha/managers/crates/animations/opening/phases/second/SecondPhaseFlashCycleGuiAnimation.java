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
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.inventory.ItemStack;

public class SecondPhaseFlashCycleGuiAnimation extends PhaseRunnerImpl {
   private static final int WINNER_SLOT = 13;
   private static final int[] MARKER_SLOTS = new int[]{4, 22};
   private static final int SLOT_COUNT = 27;
   private static final int CHAOS_TICKS = 15;
   private static final int SLOW_TICKS = 55;
   private static final int WINNER_REVEAL_TICK = 70;
   private static final int RESULT_HOLD_TICKS = 60;
   private static final int COLOR_INTERVAL_TICKS = 4;
   private SecondPhaseFlashCycleGuiAnimation.FlashCycleContainer container;
   private final RewardDisplayCache displayCache = new RewardDisplayCache();
   private List<? extends BaseReward> availableRewards;
   private BaseReward winningReward;
   private int[] colorOffsets;
   private int[] centerOffset;
   private int globalTick;
   private int centerFrame;
   private int centerFrameGate;
   private int colorTick;
   private boolean winnerRevealed;
   private int settleTicks;
   private boolean allowReopenOnClose;

   public SecondPhaseFlashCycleGuiAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
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

         this.colorOffsets = new int[27];

         for (int i = 0; i < 27; i++) {
            this.colorOffsets[i] = ThreadLocalRandom.current().nextInt(GuiAnimationHelper.RAINBOW_PALETTE.length);
         }

         this.centerOffset = new int[]{ThreadLocalRandom.current().nextInt(this.availableRewards.size())};
         this.globalTick = 0;
         this.centerFrame = 0;
         this.centerFrameGate = 0;
         this.colorTick = 0;
         this.winnerRevealed = false;
         this.settleTicks = 0;
         this.allowReopenOnClose = true;
         this.container = new SecondPhaseFlashCycleGuiAnimation.FlashCycleContainer(this.handler.getPlugin(), this);
         this.container.open(this.player);
         this.playSound(this.animationLocation, XSound.BLOCK_ENDER_CHEST_OPEN, 0.4F, 1.2F);
         this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_BELL, 0.3F, 1.8F);
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
         this.colorTick++;
         boolean colorsShifted = this.colorTick >= 4;
         if (colorsShifted) {
            this.colorTick = 0;
         }

         if (this.winnerRevealed) {
            this.settleTicks++;
            if (colorsShifted || this.settleTicks % 3 == 0) {
               this.container.updateView(this.player);
            }

            if (colorsShifted && this.settleTicks % 8 == 0) {
               this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_CHIME, 0.25F, 1.4F + ThreadLocalRandom.current().nextFloat() * 0.4F);
            }

            if (this.settleTicks >= 60) {
               this.nextPhase();
            }
         } else {
            boolean frameAdvanced = false;
            this.centerFrameGate++;
            if (this.centerFrameGate >= this.getFrameInterval()) {
               this.centerFrameGate = 0;
               this.centerFrame++;
               frameAdvanced = true;
            }

            if (frameAdvanced && this.globalTick >= 70) {
               this.winnerRevealed = true;
               this.container.updateView(this.player);
               this.playWinSound();
            } else {
               if (frameAdvanced) {
                  this.playSound(this.animationLocation, XSound.UI_BUTTON_CLICK, 0.1F, 1.1F + this.globalTick * 0.01F);
               }

               if (frameAdvanced || colorsShifted) {
                  this.container.updateView(this.player);
               }
            }
         }
      }
   }

   private int getFrameInterval() {
      if (this.globalTick < 15) {
         return 1;
      } else {
         int slowProgress = this.globalTick - 15;
         if (slowProgress < 4) {
            return 2;
         } else if (slowProgress < 8) {
            return 3;
         } else if (slowProgress < 14) {
            return 5;
         } else if (slowProgress < 22) {
            return 7;
         } else if (slowProgress < 32) {
            return 9;
         } else {
            return slowProgress < 44 ? 12 : 20;
         }
      }
   }

   private void playWinSound() {
      this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HARP, 0.4F, 1.8F);
      this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_BELL, 0.45F, 1.6F);
      this.playSound(this.animationLocation, XSound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
   }

   private void render(RenderViewContext context) {
      int colorStep = this.globalTick / 4;

      for (int slot = 0; slot < 27; slot++) {
         if (slot != 13 && !this.isMarkerSlot(slot)) {
            int colorIndex = (colorStep + this.colorOffsets[slot]) % GuiAnimationHelper.RAINBOW_PALETTE.length;
            context.setSlot(slot, GuiAnimationHelper.buildRainbowPane(GuiAnimationHelper.RAINBOW_PALETTE[colorIndex]));
         }
      }

      if (this.winnerRevealed) {
         this.renderReward(context, 13, this.winningReward);
      } else {
         int idx = (this.centerFrame + this.centerOffset[0]) % this.availableRewards.size();
         this.renderReward(context, 13, this.availableRewards.get(idx));
      }

      boolean flicker = this.winnerRevealed && this.settleTicks / 2 % 2 == 0;
      ItemStack marker = flicker ? GuiAnimationHelper.buildMarkerFlicker(this.properties) : GuiAnimationHelper.buildMarkerNormal(this.properties);

      for (int slot : MARKER_SLOTS) {
         context.setSlot(slot, marker.clone());
      }
   }

   private boolean isMarkerSlot(int slot) {
      for (int marker : MARKER_SLOTS) {
         if (marker == slot) {
            return true;
         }
      }

      return false;
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

   private static class FlashCycleContainer extends AbstractContainer {
      private final SecondPhaseFlashCycleGuiAnimation animation;

      private FlashCycleContainer(ServerPlugin plugin, SecondPhaseFlashCycleGuiAnimation animation) {
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
