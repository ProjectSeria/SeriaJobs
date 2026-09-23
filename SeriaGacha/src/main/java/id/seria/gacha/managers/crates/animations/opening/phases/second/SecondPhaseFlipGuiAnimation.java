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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.inventory.ItemStack;

public class SecondPhaseFlipGuiAnimation extends PhaseRunnerImpl {
   private static final int TOTAL_SLOTS = 27;
   private static final int RESULT_HOLD_TICKS = 55;
   private SecondPhaseFlipGuiAnimation.FlipContainer container;
   private final RewardDisplayCache displayCache = new RewardDisplayCache();
   private List<? extends BaseReward> availableRewards;
   private BaseReward winningReward;
   private int winnerSlot;
   private List<Integer> revealOrder;
   private List<Integer> vanishOrder;
   private Map<Integer, BaseReward> revealedRewards;
   private Map<Integer, Boolean> visibleSlots;
   private int currentRevealIndex;
   private int currentVanishIndex;
   private int tickGate;
   private int actionInterval;
   private int settleTicks;
   private SecondPhaseFlipGuiAnimation.Phase phase;
   private boolean allowReopenOnClose;

   public SecondPhaseFlipGuiAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
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

         this.winnerSlot = ThreadLocalRandom.current().nextInt(27);
         this.revealOrder = new ArrayList<>();

         for (int i = 0; i < 27; i++) {
            if (i != this.winnerSlot) {
               this.revealOrder.add(i);
            }
         }

         Collections.shuffle(this.revealOrder);
         this.revealOrder.add(this.winnerSlot);
         this.revealedRewards = new HashMap<>();
         this.visibleSlots = new HashMap<>();
         this.currentRevealIndex = 0;
         this.currentVanishIndex = 0;
         this.tickGate = 0;
         this.actionInterval = 1;
         this.settleTicks = 0;
         this.phase = SecondPhaseFlipGuiAnimation.Phase.REVEALING;
         this.allowReopenOnClose = true;
         this.container = new SecondPhaseFlipGuiAnimation.FlipContainer(this.handler.getPlugin(), this);
         this.container.open(this.player);
         this.playSound(this.animationLocation, XSound.UI_BUTTON_CLICK, 0.4F, 0.8F);
      } else {
         this.nextPhase();
      }
   }

   @Override
   public void tick() {
      if (this.container == null) {
         this.nextPhase();
      } else if (this.phase == SecondPhaseFlipGuiAnimation.Phase.FINISHED) {
         this.settleTicks++;
         if (this.settleTicks % 3 == 0) {
            this.container.updateView(this.player);
         }

         if (this.settleTicks >= 55) {
            this.nextPhase();
         }
      } else {
         this.tickGate++;
         if (this.tickGate >= this.actionInterval) {
            this.tickGate = 0;
            if (this.phase == SecondPhaseFlipGuiAnimation.Phase.REVEALING) {
               this.tickReveal();
            } else if (this.phase == SecondPhaseFlipGuiAnimation.Phase.VANISHING) {
               this.tickVanish();
            }
         }
      }
   }

   private void tickReveal() {
      if (this.currentRevealIndex >= this.revealOrder.size()) {
         this.startVanishPhase();
      } else {
         int slot = this.revealOrder.get(this.currentRevealIndex);
         BaseReward reward = slot == this.winnerSlot ? this.winningReward : MathUtil.random(this.availableRewards);
         this.revealedRewards.put(slot, reward);
         this.visibleSlots.put(slot, true);
         this.currentRevealIndex++;
         this.updateRevealInterval();
         this.container.updateView(this.player);
         this.playSound(this.animationLocation, XSound.UI_BUTTON_CLICK, 0.15F, 1.1F);
      }
   }

   private void startVanishPhase() {
      this.phase = SecondPhaseFlipGuiAnimation.Phase.VANISHING;
      this.currentVanishIndex = 0;
      this.actionInterval = 1;
      this.vanishOrder = new ArrayList<>();

      for (int i = 0; i < 27; i++) {
         if (i != this.winnerSlot) {
            this.vanishOrder.add(i);
         }
      }

      Collections.shuffle(this.vanishOrder);
   }

   private void playWinSound() {
      this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 1.76F);
      this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 2.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.4F);
   }

   private void tickVanish() {
      if (this.currentVanishIndex >= this.vanishOrder.size()) {
         this.phase = SecondPhaseFlipGuiAnimation.Phase.FINISHED;
         this.container.updateView(this.player);
         this.playWinSound();
      } else {
         int slot = this.vanishOrder.get(this.currentVanishIndex);
         this.visibleSlots.put(slot, false);
         this.currentVanishIndex++;
         this.updateVanishInterval();
         this.container.updateView(this.player);
         int remaining = this.vanishOrder.size() - this.currentVanishIndex;
         if (remaining <= 3) {
            float pitch = 0.9F + (3 - remaining) * 0.15F;
            this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_BASS, 0.35F, pitch);
         } else {
            this.playSound(this.animationLocation, XSound.UI_BUTTON_CLICK, 0.1F, 0.7F);
         }
      }
   }

   private void updateRevealInterval() {
      int remaining = this.revealOrder.size() - this.currentRevealIndex;
      if (remaining > 18) {
         this.actionInterval = 1;
      } else if (remaining > 10) {
         this.actionInterval = 2;
      } else {
         this.actionInterval = 1;
      }
   }

   private void updateVanishInterval() {
      int remaining = this.vanishOrder.size() - this.currentVanishIndex;
      if (remaining > 10) {
         this.actionInterval = 1;
      } else if (remaining > 6) {
         this.actionInterval = 2;
      } else if (remaining > 3) {
         this.actionInterval = 4;
      } else if (remaining > 1) {
         this.actionInterval = 9;
      } else {
         this.actionInterval = 15;
      }
   }

   private void render(RenderViewContext context) {
      if (this.phase == SecondPhaseFlipGuiAnimation.Phase.FINISHED) {
         ItemStack highlight = this.settleTicks / 4 % 2 == 0
            ? GuiAnimationHelper.buildMarkerNormal(this.properties)
            : GuiAnimationHelper.buildMarkerFlicker(this.properties);
         context.fillSlots(highlight);
         this.renderReward(context, this.winnerSlot, this.winningReward);
      } else {
         context.fillSlots(GuiAnimationHelper.buildMaterial(this.properties, "filler", "LIGHT_GRAY_STAINED_GLASS_PANE"));

         for (Entry<Integer, Boolean> entry : this.visibleSlots.entrySet()) {
            if (entry.getValue()) {
               BaseReward reward = this.revealedRewards.get(entry.getKey());
               if (reward != null) {
                  this.renderReward(context, entry.getKey(), reward);
               }
            }
         }
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

   private static class FlipContainer extends AbstractContainer {
      private final SecondPhaseFlipGuiAnimation animation;

      private FlipContainer(ServerPlugin plugin, SecondPhaseFlipGuiAnimation animation) {
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

   private enum Phase {
      REVEALING,
      VANISHING,
      FINISHED;

      Phase() {
      }
   }
}
