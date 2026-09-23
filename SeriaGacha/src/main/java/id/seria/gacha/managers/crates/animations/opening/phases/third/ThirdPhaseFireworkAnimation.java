package id.seria.gacha.managers.crates.animations.opening.phases.third;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.internal.RewardsDisplay;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.RegularColor;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.awt.Color;
import java.util.List;
import java.util.Random;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class ThirdPhaseFireworkAnimation extends PhaseRunnerImpl {
   private List<? extends BaseReward> rewards;
   private final RewardsDisplay rewardsDisplay = new RewardsDisplay(
      this, this.animationLocation.clone().add(this.r(this.crate.getType().getExtraSettings().getDisplayItemPosition().clone()))
   );
   private Location baseLocation;
   private int alpha;
   private double delta;
   private double theta;

   public ThirdPhaseFireworkAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.baseLocation = this.animationLocation.clone().add(this.r(this.crate.getType().getExtraSettings().getDisplayItemPosition().clone()));
      ParticleEffect.SMOKE_NORMAL.display(this.baseLocation, 0.2F, 0.2F, 0.2F, 0.4F, 20, null, this.getAudience());
      ParticleEffect.EXPLOSION_NORMAL.display(this.baseLocation, 0.0F, 0.0F, 0.0F, 0.0F, 1, null, this.getAudience());
      ParticleEffect.SPELL_MOB_AMBIENT.display(this.baseLocation, 0.0F, 0.0F, 0.0F, 0.5F, 10, new RegularColor(Color.DARK_GRAY), this.getAudience());
      ParticleEffect.EXPLOSION_LARGE.display(this.baseLocation, this.getAudience());
      this.playSound(this.animationLocation, XSound.ENTITY_ENDER_DRAGON_GROWL, 0.3F, 1.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
      this.rewards = this.handler.generateRewards();
      this.rewardsDisplay.create(this.rewards);
      this.session.giveRewards(this.rewards);
   }

   @Override
   public void tick() {
      if (this.alpha % 20 == 0) {
         this.spawnFirework(this.centerLocation.add(MathUtil.random(-0.15, 0.15), 0.5, MathUtil.random(-0.15, 0.15)));
      }

      if (this.alpha > 60) {
         this.nextPhase();
      }

      this.alpha++;
   }

   @Override
   public void unload() {
      this.rewardsDisplay.destroy();
      this.closeCrate();
      this.playSound(this.animationLocation, XSound.ENTITY_CHICKEN_EGG, 1.0F, 0.2F);
   }

   public void spawnFirework(Location loc) {
      FakeEntity entity = Utilities.getNMSFactory().createFakeEntity(PhoenixCrates.getLegacyInstance(), FakeEntityType.FIREWORK, loc);
      entity.setAudience(this.getAudience());
      Firework fw = (Firework)entity.getBukkitEntity();
      FireworkMeta fwm = fw.getFireworkMeta();
      Random r = new Random();
      int rt = MathUtil.random(1, 4);
      Type type = Type.BALL;
      if (rt == 1) {
         type = Type.BALL;
      } else if (rt == 2) {
         type = Type.BALL_LARGE;
      } else if (rt == 3) {
         type = Type.CREEPER;
      } else if (rt == 4) {
         type = Type.STAR;
      }

      FireworkEffect effect = FireworkEffect.builder()
         .flicker(r.nextBoolean())
         .withColor(randomColor())
         .withFade(randomColor())
         .with(type)
         .trail(r.nextBoolean())
         .build();
      fwm.addEffect(effect);
      fwm.setPower(1);
      fw.setFireworkMeta(fwm);
      entity.prepareBundlePacket().sendPacket(this.getAudience());
      this.playSound(loc, XSound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 1.0F);
      Crates.getScheduler().runSync(this.animationLocation, () -> {
         entity.prepareEventPacket((byte)17).sendPacket(this.getAudience());
         entity.prepareDestroyPacket().sendPacket(this.getAudience());
      }, 20 + MathUtil.random(5, 15));
   }

   private static org.bukkit.Color randomColor() {
      return MathUtil.random(
         Lists.newArrayList(
            new org.bukkit.Color[]{
               org.bukkit.Color.AQUA,
               org.bukkit.Color.BLUE,
               org.bukkit.Color.FUCHSIA,
               org.bukkit.Color.GREEN,
               org.bukkit.Color.LIME,
               org.bukkit.Color.MAROON,
               org.bukkit.Color.NAVY,
               org.bukkit.Color.OLIVE,
               org.bukkit.Color.ORANGE,
               org.bukkit.Color.PURPLE,
               org.bukkit.Color.RED,
               org.bukkit.Color.TEAL,
               org.bukkit.Color.YELLOW
            }
         )
      );
   }
}
