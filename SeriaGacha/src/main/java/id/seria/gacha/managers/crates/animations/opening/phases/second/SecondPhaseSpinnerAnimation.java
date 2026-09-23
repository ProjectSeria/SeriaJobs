package id.seria.gacha.managers.crates.animations.opening.phases.second;

import com.google.common.collect.Iterables;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.RegularColor;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SecondPhaseSpinnerAnimation extends PhaseRunnerImpl {
   private static final float ITEM_SCALE = 2.0F;
   private static final int STOP_STEPS = 52;
   private static final int RESULT_HOLD_TICKS = 50;
   private ItemDisplay display;
   private List<? extends BaseReward> availableRewards;
   private List<ItemStack> rewardItems;
   private BaseReward winningReward;
   private int currentIndex;
   private int tickGate;
   private int moveInterval;
   private int totalSteps;
   private boolean stopped;
   private int settleTicks;

   public SecondPhaseSpinnerAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
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

         this.rewardItems = this.availableRewards.stream().map(r -> r.getDisplayItem().build()).collect(Collectors.toList());
         this.currentIndex = 0;
         this.moveInterval = 1;
         this.display = (ItemDisplay)this.displayLocation.getWorld().spawn(this.displayLocation, ItemDisplay.class, entity -> {
            entity.setItemStack(this.rewardItems.get(0));
            entity.setItemDisplayTransform(ItemDisplayTransform.GROUND);
            entity.setBillboard(Billboard.CENTER);
            entity.setTransformation(new Transformation(new Vector3f(0.0F, 0.0F, 0.0F), new Quaternionf(), new Vector3f(2.0F, 2.0F, 2.0F), new Quaternionf()));
            entity.setPersistent(false);
         });
         this.openCrate();
         this.playSound(this.animationLocation, XSound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.7F, 1.5F);
         ParticleEffect.EXPLOSION_LARGE.display(this.displayLocation, 0.3F, 0.3F, 0.3F, 0.0F, 2, null, this.getAudience());
      } else {
         this.nextPhase();
      }
   }

   @Override
   public void tick() {
      if (this.stopped) {
         this.settleTicks++;
         if (this.settleTicks % 4 == 0) {
            double angle = this.settleTicks * 0.3;
            double radius = 0.8;
            ParticleEffect.SPELL_MOB_AMBIENT
               .display(
                  this.displayLocation.clone().add(MathUtil.sin(angle) * 0.8, 0.3 + MathUtil.sin(this.settleTicks * 0.15) * 0.3, MathUtil.cos(angle) * 0.8),
                  0.0F,
                  0.0F,
                  0.0F,
                  0.5F,
                  1,
                  new RegularColor(new Color(255, 215, 0)),
                  this.getAudience()
               );
         }

         if (this.settleTicks % 8 == 0) {
            ParticleEffect.VILLAGER_HAPPY.display(this.displayLocation, 0.5F, 0.4F, 0.5F, 0.0F, 2, null, this.getAudience());
         }

         if (this.settleTicks >= 50) {
            this.nextPhase();
         }
      } else {
         this.tickGate++;
         if (this.tickGate >= this.moveInterval) {
            this.tickGate = 0;
            this.totalSteps++;
            if (this.totalSteps >= 52) {
               this.display.setItemStack(this.winningReward.getDisplayItem().build());
               this.stopped = true;
               ParticleEffect.EXPLOSION_LARGE.display(this.displayLocation, 0.5F, 0.5F, 0.5F, 0.0F, 4, null, this.getAudience());
               ParticleEffect.SPELL_MOB_AMBIENT
                  .display(this.displayLocation, 0.6F, 0.6F, 0.6F, 0.5F, 20, new RegularColor(new Color(80, 255, 80)), this.getAudience());
               ParticleEffect.FIREWORKS_SPARK.display(this.displayLocation, 0.4F, 0.4F, 0.4F, 0.08F, 12, null, this.getAudience());
               this.playSound(this.animationLocation, XSound.ENTITY_PLAYER_LEVELUP, 0.6F, 1.3F);
               this.playSound(this.animationLocation, XSound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5F, 0.75F);
            } else {
               this.currentIndex = (this.currentIndex + 1) % this.rewardItems.size();
               this.display.setItemStack(this.rewardItems.get(this.currentIndex));
               this.updateInterval();
               this.playTickSound();
               double progress = this.totalSteps / 52.0;
               if (this.moveInterval <= 2) {
                  double angle = this.totalSteps * 0.5;
                  double radius = 0.6 + progress * 0.4;
                  ParticleEffect.CRIT
                     .display(
                        this.displayLocation.clone().add(MathUtil.sin(angle) * radius, MathUtil.cos(angle) * 0.3, MathUtil.cos(angle) * radius),
                        0.0F,
                        0.0F,
                        0.0F,
                        0.0F,
                        1,
                        null,
                        this.getAudience()
                     );
               }

               if (progress > 0.7 && this.totalSteps % 2 == 0) {
                  float hue = this.totalSteps * 12 % 360 / 360.0F;
                  Color sparkColor = Color.getHSBColor(hue, 0.8F, 1.0F);
                  ParticleEffect.REDSTONE
                     .display(
                        this.displayLocation.clone().add(MathUtil.random(-0.5, 0.5), MathUtil.random(-0.2, 0.5), MathUtil.random(-0.5, 0.5)),
                        0.0F,
                        0.0F,
                        0.0F,
                        0.0F,
                        1,
                        new RegularColor(sparkColor),
                        this.getAudience()
                     );
               }
            }
         }
      }
   }

   private void updateInterval() {
      double progress = this.totalSteps / 52.0;
      if (progress < 0.5) {
         this.moveInterval = 1;
      } else if (progress < 0.65) {
         this.moveInterval = 2;
      } else if (progress < 0.75) {
         this.moveInterval = 3;
      } else if (progress < 0.82) {
         this.moveInterval = 5;
      } else if (progress < 0.88) {
         this.moveInterval = 8;
      } else if (progress < 0.93) {
         this.moveInterval = 12;
      } else if (progress < 0.97) {
         this.moveInterval = 16;
      } else {
         this.moveInterval = 22;
      }
   }

   private void playTickSound() {
      if (this.moveInterval <= 1) {
         this.playSound(this.animationLocation, XSound.UI_BUTTON_CLICK, 0.1F, 1.3F);
      } else if (this.moveInterval <= 5) {
         float pitch = 0.9F + (this.moveInterval - 2) * 0.08F;
         this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HAT, 0.3F, Math.min(pitch, 2.0F));
      } else {
         float pitch = 0.5F + (float)(this.totalSteps / 52.0) * 0.8F;
         this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_BASS, 0.4F, Math.min(pitch, 2.0F));
      }
   }

   @Override
   public void unload() {
      if (this.display != null && !this.display.isDead()) {
         this.display.remove();
      }

      this.display = null;
   }
}
