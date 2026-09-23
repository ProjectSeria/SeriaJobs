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

public class SecondPhaseCSGOGuiAnimation extends PhaseRunnerImpl {
   private static final int[] TRACK_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16};
   private static final int RESULT_HOLD_TICKS = 50;
   private static final float[] CASINO_MELODY_INTRO = new float[]{0.84F, 0.95F, 1.06F, 0.95F, 0.9F, 0.95F, 1.06F, 0.95F};
   private static final float[] CASINO_MELODY_MID = new float[]{0.9F, 1.0F, 1.12F, 1.0F, 0.95F, 1.06F, 1.12F, 1.0F};
   private static final float[] CASINO_MELODY_BUILD = new float[]{0.95F, 1.06F, 1.19F, 1.06F, 1.0F, 1.12F, 1.26F, 1.12F};
   private SecondPhaseCSGOGuiAnimation.CSGOGuiContainer container;
   private final RewardDisplayCache displayCache = new RewardDisplayCache();
   private List<? extends BaseReward> availableRewards;
   private final List<BaseReward> reel = new ArrayList<>();
   private BaseReward winningReward;
   private int targetTrackIndex;
   private int pointer;
   private int stopPointer;
   private int tickGate;
   private int settleTicks;
   private int randomTailSlowSteps;
   private int melodyTickGate;
   private int melodyStep;
   private int moveInterval;
   private int movedSteps;
   private boolean stopped;
   private boolean allowReopenOnClose;

   public SecondPhaseCSGOGuiAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
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

         this.targetTrackIndex = ThreadLocalRandom.current().nextInt(TRACK_SLOTS.length);
         this.buildReel();
         this.pointer = 0;
         this.tickGate = 0;
         this.moveInterval = 1;
         this.movedSteps = 0;
         this.stopped = false;
         this.allowReopenOnClose = true;
         this.settleTicks = 0;
         this.randomTailSlowSteps = ThreadLocalRandom.current().nextInt(2, 6);
         this.melodyTickGate = 0;
         this.melodyStep = 0;
         this.container = new SecondPhaseCSGOGuiAnimation.CSGOGuiContainer(this.handler.getPlugin(), this);
         this.container.open(this.player);
         this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HAT, 0.3F, 1.6F);
      } else {
         this.nextPhase();
      }
   }

   private void buildReel() {
      int winnerReelIndex = 82;
      this.reel.clear();
      this.reel.addAll(GuiAnimationHelper.buildDedupedReel(this.availableRewards, 140, 82, this.winningReward));
      this.stopPointer = Math.max(0, 82 - this.targetTrackIndex);
   }

   @Override
   public void tick() {
      if (this.container == null) {
         this.nextPhase();
      } else if (this.stopped) {
         this.settleTicks++;
         if (this.settleTicks % 3 == 0) {
            this.container.updateView(this.player);
         }

         if (this.settleTicks >= 50) {
            this.nextPhase();
         }
      } else {
         this.tickMelody();
         this.tickGate++;
         if (this.tickGate >= this.moveInterval) {
            this.tickGate = 0;
            if (this.pointer < this.stopPointer) {
               this.pointer++;
               this.movedSteps++;
               this.updateSpeedCurve();
               this.container.updateView(this.player);
               this.playSound(this.animationLocation, XSound.UI_BUTTON_CLICK, 0.12F, 1.1F);
            }

            if (this.pointer >= this.stopPointer) {
               this.stopped = true;
               this.container.updateView(this.player);
               this.playWinSound();
            }
         }
      }
   }

   private void playWinSound() {
      this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HARP, 0.36F, 1.76F);
      this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HARP, 0.36F, 2.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_PLAYER_LEVELUP, 0.45F, 1.6F);
   }

   private void tickMelody() {
      if (this.stopPointer > 0) {
         double progress = Math.max(0.0, Math.min(1.0, (double)this.movedSteps / this.stopPointer));
         int beatTicks;
         if (progress < 0.6) {
            beatTicks = 2;
         } else if (progress < 0.9) {
            beatTicks = 3;
         } else {
            beatTicks = 4;
         }

         this.melodyTickGate++;
         if (this.melodyTickGate >= beatTicks) {
            this.melodyTickGate = 0;
            this.playProgressMelody();
         }
      }
   }

   private void playProgressMelody() {
      if (this.stopPointer > 0) {
         double progress = Math.max(0.0, Math.min(1.0, (double)this.movedSteps / this.stopPointer));
         float[] notes;
         if (progress < 0.45) {
            notes = CASINO_MELODY_INTRO;
            float volume = 0.14F;
         } else if (progress < 0.82) {
            notes = CASINO_MELODY_MID;
            float volume = 0.16F;
         } else {
            notes = CASINO_MELODY_BUILD;
            float volume = 0.18F;
         }

         int noteIndex = this.melodyStep % notes.length;
         float pitch = notes[noteIndex];
         this.melodyStep++;
      }
   }

   private void updateSpeedCurve() {
      if (this.stopPointer <= 0) {
         this.moveInterval = 1;
      } else {
         int remainingSteps = this.stopPointer - this.pointer;
         if (remainingSteps <= this.randomTailSlowSteps) {
            int tailIndex = this.randomTailSlowSteps - remainingSteps;
            int interval = 13 + tailIndex * 3;
            if (remainingSteps <= 2) {
               interval += 5;
            }

            this.moveInterval = interval;
         } else {
            double progress = (double)this.movedSteps / this.stopPointer;
            if (progress < 0.5) {
               this.moveInterval = 1;
            } else if (progress < 0.72) {
               this.moveInterval = 2;
            } else if (progress < 0.84) {
               this.moveInterval = 3;
            } else if (progress < 0.92) {
               this.moveInterval = 4;
            } else if (progress < 0.97) {
               this.moveInterval = 6;
            } else if (progress < 0.985) {
               this.moveInterval = 10;
            } else if (progress < 0.995) {
               this.moveInterval = 12;
            } else {
               this.moveInterval = 14;
            }
         }
      }
   }

   private void render(RenderViewContext context) {
      context.fillSlots(GuiAnimationHelper.buildFiller(this.properties));
      boolean flickerOrange = this.stopped && this.settleTicks / 2 % 2 == 0;
      ItemStack marker = flickerOrange ? GuiAnimationHelper.buildMarkerFlicker(this.properties) : GuiAnimationHelper.buildMarkerNormal(this.properties);
      int markerColumn = TRACK_SLOTS[this.targetTrackIndex];

      for (int slot : new int[]{markerColumn - 9, markerColumn + 9}) {
         context.setSlot(slot, marker.clone());
      }

      if (!this.reel.isEmpty()) {
         for (int i = 0; i < TRACK_SLOTS.length; i++) {
            int reelIndex = this.clamp(this.pointer + i, 0, this.reel.size() - 1);
            BaseReward reward = this.reel.get(reelIndex);
            RItemStack rItem = RItemStack.of(reward.getDisplayItem().build());
            RewardDisplayCache.Entry cached = this.displayCache.get(reward, this.player, this.availableRewards, this.properties);
            new VirtualItem()
               .withSlot(TRACK_SLOTS[i])
               .withItem(rItem)
               .withDisplayName(cached.displayName)
               .withLore(new ArrayList<>(cached.lore))
               .render(context);
         }
      }
   }

   private int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   @Override
   public void unload() {
      this.allowReopenOnClose = false;
      if (this.player != null && this.player.isOnline() && this.container != null && this.container.getViewersContext().containsKey(this.player.getUniqueId())) {
         this.player.closeInventory();
      }

      this.container = null;
      this.reel.clear();
   }

   private static class CSGOGuiContainer extends AbstractContainer {
      private final SecondPhaseCSGOGuiAnimation animation;

      private CSGOGuiContainer(ServerPlugin plugin, SecondPhaseCSGOGuiAnimation animation) {
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
