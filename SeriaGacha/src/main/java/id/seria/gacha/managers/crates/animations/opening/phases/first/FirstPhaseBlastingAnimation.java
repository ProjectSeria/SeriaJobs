package id.seria.gacha.managers.crates.animations.opening.phases.first;

import com.google.common.collect.Lists;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.util.Random;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FirstPhaseBlastingAnimation extends PhaseRunnerImpl {
   private final Color color = Color.fromRGB(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue());
   private int alpha;
   private int delta;

   public FirstPhaseBlastingAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.delta = 20;
   }

   @Override
   public void tick() {
      if (this.alpha % 2 == 0) {
         this.detonateFirework(this.animationLocation.clone().add(0.0, this.delta, 0.0), Type.BURST);
         this.delta = (int)(this.delta - 1.8);
      }

      if (this.delta < 0) {
         this.nextPhase();
         this.detonateFirework(this.centerLocation, Type.BALL);
         this.playSound(this.centerLocation, XSound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
      }

      this.alpha++;
   }

   private void detonateFirework(Location location, Type type) {
      FakeEntity entity = Utilities.getNMSFactory().createFakeEntity(PhoenixCrates.getLegacyInstance(), FakeEntityType.FIREWORK, location);
      entity.setAudience(this.getAudience());
      Firework fw = (Firework)entity.getBukkitEntity();
      FireworkMeta fwm = fw.getFireworkMeta();
      Random r = new Random();
      FireworkEffect effect = FireworkEffect.builder()
         .flicker(r.nextBoolean())
         .withColor(this.color)
         .withFade(randomColor())
         .with(type)
         .trail(r.nextBoolean())
         .build();
      fwm.setPower(0);
      fwm.addEffect(effect);
      fw.setFireworkMeta(fwm);
      entity.prepareBundlePacket().sendPacket(this.getAudience());
      entity.prepareEventPacket((byte)17).sendPacket(this.getAudience());
      entity.prepareDestroyPacket().sendPacket(this.getAudience());
      this.playSound(location, XSound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 1.0F);
   }

   private static Color randomColor() {
      return MathUtil.random(
         Lists.newArrayList(
            new Color[]{
               Color.AQUA,
               Color.BLUE,
               Color.FUCHSIA,
               Color.GREEN,
               Color.LIME,
               Color.MAROON,
               Color.NAVY,
               Color.OLIVE,
               Color.ORANGE,
               Color.PURPLE,
               Color.RED,
               Color.TEAL,
               Color.YELLOW
            }
         )
      );
   }

   private java.awt.Color getColor() {
      return this.properties.getColorOrDefault("color", java.awt.Color.WHITE);
   }

   @Override
   public void unload() {
   }

   @Override
   public void nextPhase() {
      this.openCrate();
      super.nextPhase();
   }
}
