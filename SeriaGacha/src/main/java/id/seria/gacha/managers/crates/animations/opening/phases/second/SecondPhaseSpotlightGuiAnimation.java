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

public class SecondPhaseSpotlightGuiAnimation extends PhaseRunnerImpl {
   private static final int TOTAL_COLUMNS = 9;
   private static final int RESULT_HOLD_TICKS = 60;
   private static final int SCAN_LAPS = 8;
   private SecondPhaseSpotlightGuiAnimation.SpotlightContainer container;
   private final RewardDisplayCache displayCache = new RewardDisplayCache();
   private List<? extends BaseReward> availableRewards;
   private BaseReward winningReward;
   private BaseReward[] columnRewards;
   private int targetColumn;
   private int beamStep;
   private int stopStep;
   private int tickGate;
   private int moveInterval;
   private int movedSteps;
   private int settleTicks;
   private int randomTailSlowSteps;
   private boolean stopped;
   private boolean allowReopenOnClose;

   public SecondPhaseSpotlightGuiAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
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

         this.targetColumn = ThreadLocalRandom.current().nextInt(9);
         this.columnRewards = new BaseReward[9];

         for (int i = 0; i < 9; i++) {
            this.columnRewards[i] = i == this.targetColumn ? this.winningReward : MathUtil.random(this.availableRewards);
         }

         this.randomTailSlowSteps = ThreadLocalRandom.current().nextInt(2, 5);
         this.stopStep = 72 + this.targetColumn;
         this.beamStep = 0;
         this.tickGate = 0;
         this.moveInterval = 1;
         this.movedSteps = 0;
         this.settleTicks = 0;
         this.stopped = false;
         this.allowReopenOnClose = true;
         this.container = new SecondPhaseSpotlightGuiAnimation.SpotlightContainer(this.handler.getPlugin(), this);
         this.container.open(this.player);
         this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_BIT, 0.4F, 0.8F);
      } else {
         this.nextPhase();
      }
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

         if (this.settleTicks >= 60) {
            this.nextPhase();
         }
      } else {
         this.tickGate++;
         if (this.tickGate >= this.moveInterval) {
            this.tickGate = 0;
            if (this.beamStep < this.stopStep) {
               this.beamStep++;
               this.movedSteps++;
               this.shufflePassedColumn();
               this.updateSpeedCurve();
               this.container.updateView(this.player);
               float scanPitch = 0.8F + 0.6F * this.movedSteps / this.stopStep;
               this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_BIT, 0.25F, Math.min(scanPitch, 2.0F));
            }

            if (this.beamStep >= this.stopStep) {
               this.stopped = true;
               this.container.updateView(this.player);
               this.playWinSound();
            }
         }
      }
   }

   private void playWinSound() {
      this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_BELL, 0.5F, 1.2F);
      this.playSound(this.animationLocation, XSound.ENTITY_PLAYER_LEVELUP, 0.45F, 1.6F);
   }

   private void shufflePassedColumn() {
      int previousCol = (this.beamStep - 1 + 9) % 9;
      if (previousCol != this.targetColumn || this.beamStep <= this.stopStep - 9) {
         this.columnRewards[previousCol] = MathUtil.random(this.availableRewards);
         if (this.beamStep == this.stopStep - 9) {
            this.columnRewards[this.targetColumn] = this.winningReward;
         }
      }
   }

   private void updateSpeedCurve() {
      if (this.stopStep <= 0) {
         this.moveInterval = 1;
      } else {
         int remainingSteps = this.stopStep - this.beamStep;
         if (remainingSteps <= this.randomTailSlowSteps) {
            int tailIndex = this.randomTailSlowSteps - remainingSteps;
            int interval = 10 + tailIndex * 4;
            if (remainingSteps <= 1) {
               interval += 6;
            }

            this.moveInterval = interval;
         } else {
            double progress = (double)this.movedSteps / this.stopStep;
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
            } else if (progress < 0.99) {
               this.moveInterval = 9;
            } else {
               this.moveInterval = 12;
            }
         }
      }
   }

   private void render(RenderViewContext context) {
      ItemStack blackGlass = GuiAnimationHelper.buildBlackGlass(this.properties);
      context.fillSlots(blackGlass);
      int currentCol = this.beamStep % 9;

      for (int col = 0; col < 9; col++) {
         if (col != currentCol) {
            int winnerSlot = col + 9;
            this.renderReward(context, winnerSlot, this.columnRewards[col]);
         }
      }

      ItemStack beamItem;
      if (this.stopped) {
         beamItem = this.settleTicks / 4 % 2 == 0
            ? GuiAnimationHelper.buildMarkerNormal(this.properties)
            : GuiAnimationHelper.buildMarkerFlicker(this.properties);
      } else {
         beamItem = GuiAnimationHelper.buildYellowGlass(this.properties);
      }

      setColumn(context, currentCol, beamItem);
      if (this.stopped && this.winningReward != null) {
         this.renderReward(context, this.targetColumn + 9, this.winningReward);
      }
   }

   private void renderReward(RenderViewContext context, int slot, BaseReward reward) {
      RItemStack rItem = RItemStack.of(reward.getDisplayItem().build());
      RewardDisplayCache.Entry cached = this.displayCache.get(reward, this.player, this.availableRewards, this.properties);
      new VirtualItem().withSlot(slot).withItem(rItem).withDisplayName(cached.displayName).withLore(new ArrayList<>(cached.lore)).render(context);
   }

   private static void setColumn(RenderViewContext context, int col, ItemStack item) {
      context.setSlot(col, item.clone());
      context.setSlot(col + 9, item.clone());
      context.setSlot(col + 18, item.clone());
   }

   @Override
   public void unload() {
      this.allowReopenOnClose = false;
      if (this.player != null && this.player.isOnline() && this.container != null && this.container.getViewersContext().containsKey(this.player.getUniqueId())) {
         this.player.closeInventory();
      }

      this.container = null;
   }

   private static class SpotlightContainer extends AbstractContainer {
      private final SecondPhaseSpotlightGuiAnimation animation;

      private SpotlightContainer(ServerPlugin plugin, SecondPhaseSpotlightGuiAnimation animation) {
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
