package id.seria.gacha.managers.crates.animations.opening.phases.third;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeItemEntity;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.RegularColor;
import id.seria.gacha.lib.common.services.Services;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ThirdPhasePhysicalItemAnimation extends PhaseRunnerImpl {
   private final List<FakeItemEntity> rewardsEntity;
   private final RegularColor particleColor = new RegularColor(this.getColor());
   private Location baseLocation;
   private int alpha;
   private double delta;
   private double theta;

   public ThirdPhasePhysicalItemAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
      this.rewardsEntity = new ArrayList<>();
   }

   private Color getColor() {
      return this.properties.getColorOrDefault("color", Color.WHITE);
   }

   @Override
   public void load() {
      this.closeCrate();
      this.baseLocation = this.animationLocation.clone().add(this.r(this.crate.getType().getExtraSettings().getDisplayItemPosition().clone()));
      ParticleEffect.SMOKE_NORMAL.display(this.baseLocation, 0.2F, 0.2F, 0.2F, 0.4F, 20, null, this.getAudience());
      ParticleEffect.EXPLOSION_NORMAL.display(this.baseLocation, 0.0F, 0.0F, 0.0F, 0.0F, 1, null, this.getAudience());
      ParticleEffect.SPELL_MOB_AMBIENT.display(this.baseLocation, 0.0F, 0.0F, 0.0F, 0.5F, 10, new RegularColor(Color.DARK_GRAY), this.getAudience());
      ParticleEffect.EXPLOSION_LARGE.display(this.baseLocation, this.getAudience());
      this.playSound(this.animationLocation, XSound.ENTITY_CHICKEN_EGG, 1.0F, 0.2F);
      this.playSound(this.animationLocation, XSound.ENTITY_ENDER_DRAGON_GROWL, 0.3F, 1.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
      List<? extends BaseReward> rewards = this.handler.generateRewards();

      for (BaseReward reward : rewards) {
         ItemStack itemStack = reward.getDisplayItem().build();
         Vector direction = rewards.size() == 1
            ? new Vector(0.0, 0.2, 0.0)
            : new Vector(MathUtil.random(-0.05, 0.05), MathUtil.random(0.15, 0.2), MathUtil.random(-0.05, 0.05));
         FakeItemEntity entity = Utilities.getNMSFactory().createFakeItem(PhoenixCrates.getLegacyInstance().getOriginal(), this.baseLocation);
         entity.setItemStack(itemStack);
         entity.setCustomName(
            Placeholders.setPlaceholders(
               this.getCrate().getType().isSimultaneousOpeningsEnabled()
                  ? Translations.t("crate.reward-got", "%amount%", reward.getDisplayItem().getAmount(), "%reward%", reward.getDisplayItem().getDisplayName())[0]
                  : Translations.t(
                     "crate.player-got",
                     "%player%",
                     this.player.getName(),
                     "%reward%",
                     reward.getDisplayItem().getDisplayName(),
                     "%amount%",
                     reward.getDisplayItem().getAmount(),
                     "%crate%",
                     this.handler.getCrate().getType().getFormattedDisplayName(null)
                  )[0]
            )
         );
         entity.setCustomNameVisible(true);
         entity.setVelocity(direction);
         entity.setAudience(this.getAudience());
         Services.getWatcherService().startWatching(entity, false);
         this.rewardsEntity.add(entity);
      }

      this.session.giveRewards(rewards);
   }

   @Override
   public void tick() {
      ParticleEffect.REDSTONE.display(this.animationLocation, 0.6F, 0.0F, 0.6F, 0.0F, 2, this.particleColor, this.getAudience());
      if (this.alpha == 70) {
         this.nextPhase();
      }

      this.alpha++;
   }

   @Override
   public void unload() {
      for (FakeItemEntity entity : this.rewardsEntity) {
         Services.getWatcherService().stopWatching(entity);
      }

      this.rewardsEntity.clear();
      this.playSound(this.animationLocation, XSound.ENTITY_CHICKEN_EGG, 1.0F, 0.2F);
   }
}
