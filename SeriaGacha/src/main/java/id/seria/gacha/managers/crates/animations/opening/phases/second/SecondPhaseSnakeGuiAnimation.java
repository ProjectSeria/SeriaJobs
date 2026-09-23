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
import org.bukkit.inventory.ItemStack;

public class SecondPhaseSnakeGuiAnimation extends PhaseRunnerImpl {
   private static final int WINNER_SLOT = 13;
   private static final int[] SNAKE1_PATH = new int[]{18, 19, 20, 21, 22, 23, 24, 25, 26, 17, 16, 15, 14};
   private static final int[] SNAKE2_PATH = new int[]{8, 7, 6, 5, 4, 3, 2, 1, 0, 9, 10, 11, 12};
   private static final int TICKS_PER_STEP = 2;
   private static final int PAUSE_TICKS = 6;
   private static final int RESULT_HOLD_TICKS = 45;
   private SecondPhaseSnakeGuiAnimation.SnakeContainer container;
   private final RewardDisplayCache displayCache = new RewardDisplayCache();
   private List<? extends BaseReward> availableRewards;
   private BaseReward winningReward;
   private int snake1Pos;
   private int snake2Pos;
   private boolean snake1Done;
   private boolean snake2Done;
   private SecondPhaseSnakeGuiAnimation.Phase phase;
   private int frameCounter;
   private int pauseTickCount;
   private boolean winnerRevealed;
   private int settleTicks;
   private boolean allowReopenOnClose;

   public SecondPhaseSnakeGuiAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
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

         this.snake1Pos = -1;
         this.snake2Pos = -1;
         this.snake1Done = false;
         this.snake2Done = false;
         this.phase = SecondPhaseSnakeGuiAnimation.Phase.SCANNING;
         this.frameCounter = 0;
         this.pauseTickCount = 0;
         this.winnerRevealed = false;
         this.settleTicks = 0;
         this.allowReopenOnClose = true;
         this.container = new SecondPhaseSnakeGuiAnimation.SnakeContainer(this.handler.getPlugin(), this);
         this.container.open(this.player);
         this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HAT, 0.3F, 0.7F);
      } else {
         this.nextPhase();
      }
   }

   @Override
   public void tick() {
      if (this.container == null) {
         this.nextPhase();
      } else if (this.winnerRevealed) {
         this.settleTicks++;
         if (this.settleTicks % 2 == 0) {
            this.container.updateView(this.player);
         }

         if (this.settleTicks >= 45) {
            this.nextPhase();
         }
      } else if (this.phase == SecondPhaseSnakeGuiAnimation.Phase.REVEAL) {
         this.winnerRevealed = true;
         this.container.updateView(this.player);
         this.playWinSound();
      } else {
         this.frameCounter++;
         if (this.phase == SecondPhaseSnakeGuiAnimation.Phase.PAUSE) {
            this.pauseTickCount++;
            this.container.updateView(this.player);
            if (this.pauseTickCount >= 6) {
               this.phase = SecondPhaseSnakeGuiAnimation.Phase.REVEAL;
            }
         } else {
            boolean advanceStep = this.frameCounter % 2 == 0;
            if (advanceStep) {
               if (!this.snake1Done) {
                  this.snake1Pos++;
                  if (this.snake1Pos >= SNAKE1_PATH.length - 1) {
                     this.snake1Done = true;
                  }
               }

               if (!this.snake2Done) {
                  this.snake2Pos++;
                  if (this.snake2Pos >= SNAKE2_PATH.length - 1) {
                     this.snake2Done = true;
                  }
               }

               int step = this.frameCounter / 2;
               float pitch = 0.7F + step * 0.04F;
               this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HAT, 0.22F, Math.min(pitch, 2.0F));
            }

            this.container.updateView(this.player);
            if (this.snake1Done && this.snake2Done) {
               this.phase = SecondPhaseSnakeGuiAnimation.Phase.PAUSE;
               this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_PLING, 0.55F, 1.5F);
               this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_PLING, 0.35F, 1.75F);
            }
         }
      }
   }

   private void playWinSound() {
      this.playSound(this.animationLocation, XSound.ENTITY_PLAYER_LEVELUP, 0.6F, 1.4F);
      this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_BELL, 0.7F, 2.0F);
   }

   private void render(RenderViewContext context) {
      context.fillSlots(GuiAnimationHelper.buildWhiteGlass(this.properties));
      ItemStack blackGlass = GuiAnimationHelper.buildBlackGlass(this.properties);
      ItemStack yellowGlass = GuiAnimationHelper.buildYellowGlass(this.properties);
      int trail1End = this.snake1Done ? this.snake1Pos : this.snake1Pos - 1;

      for (int i = 0; i <= trail1End; i++) {
         int slot = SNAKE1_PATH[i];
         if (slot != 13) {
            context.setSlot(slot, blackGlass.clone());
         }
      }

      int trail2End = this.snake2Done ? this.snake2Pos : this.snake2Pos - 1;

      for (int i = 0; i <= trail2End; i++) {
         int slot = SNAKE2_PATH[i];
         if (slot != 13) {
            context.setSlot(slot, blackGlass.clone());
         }
      }

      if (this.phase == SecondPhaseSnakeGuiAnimation.Phase.SCANNING && !this.snake1Done && this.snake1Pos >= 0) {
         int headSlot = SNAKE1_PATH[this.snake1Pos];
         if (headSlot != 13) {
            context.setSlot(headSlot, yellowGlass.clone());
         }
      }

      if (this.phase == SecondPhaseSnakeGuiAnimation.Phase.SCANNING && !this.snake2Done && this.snake2Pos >= 0) {
         int headSlot = SNAKE2_PATH[this.snake2Pos];
         if (headSlot != 13) {
            context.setSlot(headSlot, yellowGlass.clone());
         }
      }

      BaseReward displayed = this.winnerRevealed ? this.winningReward : this.availableRewards.get(this.frameCounter % this.availableRewards.size());
      this.renderReward(context, 13, displayed);
      if (this.winnerRevealed) {
         ItemStack marker = this.settleTicks / 4 % 2 == 0
            ? GuiAnimationHelper.buildMarkerNormal(this.properties)
            : GuiAnimationHelper.buildMarkerFlicker(this.properties);
         context.setSlot(4, marker.clone());
         context.setSlot(22, marker.clone());
      }
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

   private enum Phase {
      SCANNING,
      PAUSE,
      REVEAL;

      Phase() {
      }
   }

   private static class SnakeContainer extends AbstractContainer {
      private final SecondPhaseSnakeGuiAnimation animation;

      private SnakeContainer(ServerPlugin plugin, SecondPhaseSnakeGuiAnimation animation) {
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
