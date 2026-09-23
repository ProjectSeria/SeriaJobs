package id.seria.gacha.registries;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import id.seria.gacha.api.crate.animation.AnimationEnvironment;
import id.seria.gacha.api.crate.animation.AnimationPhase;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.lib.common.utils.version.ServerVersion;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.managers.crates.animations.opening.OpeningPhase;
import id.seria.gacha.managers.crates.animations.opening.data.ColoredOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.EmptyOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.KeybasedOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.phases.RandomPhaseRunner;
import id.seria.gacha.managers.crates.animations.opening.phases.first.FirstPhaseBlastingAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.first.FirstPhaseCrackAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.first.FirstPhaseDefaultAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.first.FirstPhaseFireAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.first.FirstPhaseKeyOpenerAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.first.FirstPhaseLightningAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.first.FirstPhaseSonicBoomAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.second.SecondPhaseBallAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.second.SecondPhaseCSGOGuiAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.second.SecondPhaseCompactAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.second.SecondPhaseFireAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.second.SecondPhaseFlipGuiAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.second.SecondPhaseNoneAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.second.SecondPhaseRainbowGuiAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.second.SecondPhaseRotatingHeadAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.second.SecondPhaseSnakeGuiAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.second.SecondPhaseSpinnerAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.second.SecondPhaseSwirlAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.third.ThirdPhaseDefaultAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.third.ThirdPhaseFireSpiralAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.third.ThirdPhaseFireworkAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.third.ThirdPhaseHelixAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.third.ThirdPhaseNoneAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.third.ThirdPhasePhysicalItemAnimation;
import id.seria.gacha.managers.crates.animations.opening.phases.third.ThirdPhaseSmokeSpiralAnimation;
import id.seria.gacha.managers.crates.type.list.OpeningAnimation;
import id.seria.gacha.registries.animation.AddonLockedAnimationPhaseImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.OpeningAnimationPreset;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.Generated;
import org.apache.commons.lang3.NotImplementedException;

public final class AnimationsRegistry implements id.seria.gacha.api.registries.AnimationsRegistry {
   private static final AnimationsRegistry INSTANCE = new AnimationsRegistry();
   private static final String ORBIT_ROULETTE_ADDON_URL = "https://www.phoenixplugins.com/product/orbit-roulette-animation";
   private static final String ASTRO_BURST_ADDON_URL = "https://www.phoenixplugins.com/product/astro-burst-animation";
   private static final String BLACK_HOLE_ADDON_URL = "https://www.phoenixplugins.com/product/black-hole-animation";
   private static final String CSGO_ADDON_URL = "https://www.phoenixplugins.com/product/physical-csgo-animation";
   private static final String HALLOWEEN_ADDON_URL = "https://www.phoenixplugins.com/product/halloween-animation";
   private static final String CHRISTMAS_ADDON_URL = "https://www.phoenixplugins.com/product/christmas-animation";
   private static final String LOOTBOX_ADDON_URL = "https://www.phoenixplugins.com/product/lootboxes";
   private final Multimap<PhaseType, AnimationPhase> registry = ArrayListMultimap.create();
   private final Map<String, OpeningAnimationPreset> presets = new LinkedHashMap<>();

   public static AnimationsRegistry getInstance() {
      return INSTANCE;
   }

   private static AnimationPhaseImpl.OpeningPhaseSupplier reflectiveSupplier(String simpleClassName, String phasePackage) {
      String className = "id.seria.gacha.managers.crates.animations.opening.phases." + phasePackage + "." + simpleClassName;
      return (animation, type, properties) -> {
         try {
            return (PhaseRunnerImpl)Class.forName(className)
               .getDeclaredConstructor(OpeningAnimationHandlerImpl.class, AnimationPhaseImpl.class, AnimationProperties.class)
               .newInstance((OpeningAnimationHandlerImpl)animation, type, properties);
         } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not instantiate " + className, e);
         }
      };
   }

   private AnimationsRegistry() {
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.PRE_OPENING,
               "DEFAULT",
               "Default",
               XMaterial.FLINT.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new FirstPhaseDefaultAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setEnvironment(AnimationEnvironment.GUI)
            .setDescription("Standard crate opening")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
            Edition.PREMIUM,
            PhaseType.PRE_OPENING,
            "RANDOM",
            "Random",
            XMaterial.ENDER_EYE.parseItem(),
            ServerVersion.v1_20_R4,
            (animation, type, properties) -> new RandomPhaseRunner((OpeningAnimationHandlerImpl)animation, type, properties),
            EmptyOpeningPhaseData::new
         )
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.PRE_OPENING,
               "CRACK",
               "Crack",
               XMaterial.MELON_SEEDS.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new FirstPhaseCrackAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Crate breaks apart")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.PRE_OPENING,
               "LIGHTNING",
               "Lighting",
               XMaterial.STICK.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new FirstPhaseLightningAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Lightning strikes the crate")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.PRE_OPENING,
               "KEY_OPENER",
               "Key Opener",
               XMaterial.TRIPWIRE_HOOK.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new FirstPhaseKeyOpenerAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               KeybasedOpeningPhaseData::new
            )
            .setDescription("Key unlocks the crate")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.PRE_OPENING,
               "FIRE",
               "Fire",
               XMaterial.BLAZE_POWDER.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new FirstPhaseFireAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Fiery eruption from the crate")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.PRE_OPENING,
               "BLASTING",
               "Blasting",
               XMaterial.FIREWORK_STAR.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new FirstPhaseBlastingAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Firework blast on the crate")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.PRE_OPENING,
               "SONIC_BOOM",
               "Sonic Boom",
               XMaterial.WARDEN_SPAWN_EGG.or(XMaterial.SQUID_SPAWN_EGG).parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new FirstPhaseSonicBoomAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Warden-style sonic shockwave")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.PREMIUM,
               PhaseType.PRE_OPENING,
               "CYCLONE_HEART",
               "Cyclone Heart",
               XMaterial.RED_DYE.parseItem(),
               ServerVersion.v1_20_R4,
               reflectiveSupplier("FirstPhaseCycloneHeartAnimation", "first"),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Hearts spiral into a stunning vortex")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.PREMIUM,
               PhaseType.PRE_OPENING,
               "RAINBOW",
               "Rainbow",
               XMaterial.AMETHYST_SHARD.or(XMaterial.FIREWORK_STAR).parseItem(),
               ServerVersion.v1_20_R4,
               reflectiveSupplier("FirstPhaseRainbowAnimation", "first"),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Dazzling rainbow arc spectacle")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.PREMIUM,
               PhaseType.PRE_OPENING,
               "FROGGO_BOOM",
               "Froggo Boom",
               XMaterial.FROG_SPAWN_EGG.or(XMaterial.LILY_PAD).parseItem(),
               ServerVersion.v1_20_R4,
               reflectiveSupplier("FirstPhaseFroggoBoomAnimation", "first"),
               EmptyOpeningPhaseData::new
            )
            .setDescription("A frog leaps and triggers an explosion")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.PREMIUM,
               PhaseType.PRE_OPENING,
               "CHICKEN_JOCKEY",
               "Chicken Jockey",
               XMaterial.ZOMBIE_HEAD.parseItem(),
               ServerVersion.v1_20_R4,
               reflectiveSupplier("FirstPhaseChickenJockeyAnimation", "first"),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Zombie rides a chicken into chaos")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
            Edition.PREMIUM, PhaseType.PRE_OPENING, "ERUPTION", "Eruption (Lootbox)", XMaterial.BARRIER.parseItem(), ServerVersion.v1_20_R4, (a, t, p) -> {
               throw new NotImplementedException("Only available with the Lootbox addon.");
            }, EmptyOpeningPhaseData::new, "Lootbox Addon", "https://www.phoenixplugins.com/product/lootboxes"
         )
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
            Edition.PREMIUM, PhaseType.PRE_OPENING, "PULSE", "Pulse (Lootbox)", XMaterial.BARRIER.parseItem(), ServerVersion.v1_20_R4, (a, t, p) -> {
               throw new NotImplementedException("Only available with the Lootbox addon.");
            }, EmptyOpeningPhaseData::new, "Lootbox Addon", "https://www.phoenixplugins.com/product/lootboxes"
         )
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
            Edition.PREMIUM, PhaseType.PRE_OPENING, "ARCANE", "Arcane (Lootbox)", XMaterial.BARRIER.parseItem(), ServerVersion.v1_20_R4, (a, t, p) -> {
               throw new NotImplementedException("Only available with the Lootbox addon.");
            }, EmptyOpeningPhaseData::new, "Lootbox Addon", "https://www.phoenixplugins.com/product/lootboxes"
         )
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.PRE_OPENING,
               "HALLOWEEN_BOUNCING_JACK",
               "Bouncing Jack (Halloween)",
               XMaterial.CARVED_PUMPKIN.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Halloween Animation",
               "https://www.phoenixplugins.com/product/halloween-animation"
            )
            .setDescription("A jack-o-lantern bounces ominously")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.PRE_OPENING,
               "HALLOWEEN_CAULDRON_MADNESS",
               "Cauldron Madness (Halloween)",
               XMaterial.BONE.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Halloween Animation",
               "https://www.phoenixplugins.com/product/halloween-animation"
            )
            .setDescription("Bubbling cauldron brews the reward")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.PRE_OPENING,
               "HALLOWEEN_HAUNTING_VORTEX",
               "Haunting Vortex (Halloween)",
               XMaterial.DRIED_KELP.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Halloween Animation",
               "https://www.phoenixplugins.com/product/halloween-animation"
            )
            .setDescription("Ghostly vortex swirls around the crate")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.PRE_OPENING,
               "HALLOWEEN_WITCH_BREW_BOOM",
               "Witch's Brew Boom (Halloween)",
               XMaterial.WITHER_ROSE.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Halloween Animation",
               "https://www.phoenixplugins.com/product/halloween-animation"
            )
            .setDescription("Witch potion explodes into the reward")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.PRE_OPENING,
               "CHRISTMAS_BOUNCING_PRESENT",
               "Bouncing Present (Christmas)",
               XMaterial.CHEST.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Christmas Animation",
               "https://www.phoenixplugins.com/product/christmas-animation"
            )
            .setDescription("A bouncing gift box full of surprises")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.PRE_OPENING,
               "CHRISTMAS_TREE",
               "Christmas Tree (Christmas)",
               XMaterial.SPRUCE_SAPLING.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Christmas Animation",
               "https://www.phoenixplugins.com/product/christmas-animation"
            )
            .setDescription("Festive tree lights up before the reveal")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.PRE_OPENING,
               "CHRISTMAS_SANTA_CLAUS",
               "Santa Claus (Christmas)",
               XMaterial.PLAYER_HEAD.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Christmas Animation",
               "https://www.phoenixplugins.com/product/christmas-animation"
            )
            .setDescription("Santa delivers your reward personally")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.OPENING,
               "NONE",
               "None",
               XMaterial.BARRIER.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new SecondPhaseNoneAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setEnvironment(AnimationEnvironment.GUI)
            .setDescription("Skips this phase")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
            Edition.PREMIUM,
            PhaseType.OPENING,
            "RANDOM",
            "Random",
            XMaterial.ENDER_EYE.parseItem(),
            ServerVersion.v1_20_R4,
            (animation, type, properties) -> new RandomPhaseRunner((OpeningAnimationHandlerImpl)animation, type, properties),
            EmptyOpeningPhaseData::new
         )
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.OPENING,
               "BALL",
               "Ball",
               XMaterial.SLIME_BALL.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new SecondPhaseBallAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Bouncing ball reveals reward")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.OPENING,
               "SWIRL",
               "Swirl",
               XMaterial.NETHER_STAR.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new SecondPhaseSwirlAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               ColoredOpeningPhaseData::new
            )
            .setDescription("Particles swirl around the crate")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.OPENING,
               "FIRE",
               "Fire",
               XMaterial.BLAZE_ROD.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new SecondPhaseFireAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Fiery eruption from the crate")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.OPENING,
               "COMPACT",
               "Compact",
               XMaterial.ENDER_PEARL.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new SecondPhaseCompactAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Skull rises from the crate")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.OPENING,
               "ROTATING_HEAD",
               "Rotating Head",
               XMaterial.LEATHER.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new SecondPhaseRotatingHeadAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Head orbits the crate")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.OPENING,
               "SPINNER",
               "Spinner",
               XMaterial.GOLD_NUGGET.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new SecondPhaseSpinnerAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Slot machine item spin")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.OPENING,
               "CSGO_GUI",
               "CSGO (GUI)",
               XMaterial.CHEST.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new SecondPhaseCSGOGuiAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setEnvironment(AnimationEnvironment.GUI)
            .setDescription("Horizontal scrolling roulette")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.PREMIUM,
               PhaseType.OPENING,
               "COLORED_CSGO_GUI",
               "Colored CSGO (GUI)",
               XMaterial.MAGENTA_BANNER.parseItem(),
               ServerVersion.v1_20_R4,
               reflectiveSupplier("SecondPhaseColoredCSGOGuiAnimation", "second"),
               EmptyOpeningPhaseData::new
            )
            .setEnvironment(AnimationEnvironment.GUI)
            .setDescription("Rarity-colored roulette")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.PREMIUM,
               PhaseType.OPENING,
               "SPOTLIGHT_GUI",
               "Spotlight (GUI)",
               XMaterial.REDSTONE_LAMP.parseItem(),
               ServerVersion.v1_20_R4,
               reflectiveSupplier("SecondPhaseSpotlightGuiAnimation", "second"),
               EmptyOpeningPhaseData::new
            )
            .setEnvironment(AnimationEnvironment.GUI)
            .setDescription("Scanning beam finds reward")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.OPENING,
               "RAINBOW_GUI",
               "Rainbow (GUI)",
               XMaterial.PINK_DYE.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new SecondPhaseRainbowGuiAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setEnvironment(AnimationEnvironment.GUI)
            .setDescription("Rainbow locks pairs inward")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.OPENING,
               "FLIP_GUI",
               "Flip Cards (GUI)",
               XMaterial.MAP.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new SecondPhaseFlipGuiAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setEnvironment(AnimationEnvironment.GUI)
            .setDescription("Cards flip to reveal reward")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.PREMIUM,
               PhaseType.OPENING,
               "BLACKOUT_GUI",
               "Blackout (GUI)",
               XMaterial.DRAGON_EGG.parseItem(),
               ServerVersion.v1_20_R4,
               reflectiveSupplier("SecondPhaseBlackoutGuiAnimation", "second"),
               EmptyOpeningPhaseData::new
            )
            .setEnvironment(AnimationEnvironment.GUI)
            .setDescription("Slots light up one by one")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.OPENING,
               "SNAKE_GUI",
               "Snake (GUI)",
               XMaterial.CACTUS.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new SecondPhaseSnakeGuiAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setEnvironment(AnimationEnvironment.GUI)
            .setDescription("Snakes converge on the reward")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.PREMIUM,
               PhaseType.OPENING,
               "RING_CAROUSEL_GUI",
               "Ring Carousel (GUI)",
               XMaterial.CLOCK.parseItem(),
               ServerVersion.v1_20_R4,
               reflectiveSupplier("SecondPhaseRingCarouselGuiAnimation", "second"),
               EmptyOpeningPhaseData::new
            )
            .setEnvironment(AnimationEnvironment.GUI)
            .setDescription("Dual carousel spins to the reward")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.OPENING,
               "FLASH_CYCLE_GUI",
               "Flash Cycle (GUI)",
               XMaterial.MAGENTA_GLAZED_TERRACOTTA.parseItem(),
               ServerVersion.v1_20_R4,
               reflectiveSupplier("SecondPhaseFlashCycleGuiAnimation", "second"),
               EmptyOpeningPhaseData::new
            )
            .setEnvironment(AnimationEnvironment.GUI)
            .setDescription("Colors flash fast while the reward locks in")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.OPENING,
               "CREEPER",
               "Creeper",
               XMaterial.CREEPER_HEAD.parseItem(),
               ServerVersion.v1_20_R4,
               reflectiveSupplier("SecondPhaseCreeperAnimation", "second"),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Creeper explodes dropping loot")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
            Edition.PREMIUM, PhaseType.OPENING, "VORTEX", "Vortex (Lootbox)", XMaterial.BARRIER.parseItem(), ServerVersion.v1_20_R4, (a, t, p) -> {
               throw new NotImplementedException("Only available with the Lootbox addon.");
            }, EmptyOpeningPhaseData::new, "Lootbox Addon", "https://www.phoenixplugins.com/product/lootboxes"
         )
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
            Edition.PREMIUM, PhaseType.OPENING, "TORNADO", "Tornado (Lootbox)", XMaterial.BARRIER.parseItem(), ServerVersion.v1_20_R4, (a, t, p) -> {
               throw new NotImplementedException("Only available with the Lootbox addon.");
            }, EmptyOpeningPhaseData::new, "Lootbox Addon", "https://www.phoenixplugins.com/product/lootboxes"
         )
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.OPENING,
               "ORBIT_ROULETTE",
               "Orbit Roulette",
               XMaterial.GOLD_INGOT.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Orbit Roulette Animation",
               "https://www.phoenixplugins.com/product/orbit-roulette-animation"
            )
            .setDescription("Rewards orbit the crate like planets")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.OPENING,
               "ASTRO_BURST",
               "Astro Burst",
               XMaterial.WIND_CHARGE.or(XMaterial.BONE).parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Astro Burst Animation",
               "https://www.phoenixplugins.com/product/astro-burst-animation"
            )
            .setDescription("Cosmic shockwave reveals the prize")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.OPENING,
               "BLACK_HOLE",
               "Black Hole",
               XMaterial.BLACK_DYE.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Black Hole Animation",
               "https://www.phoenixplugins.com/product/black-hole-animation"
            )
            .setDescription("Items get sucked into a void vortex")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.OPENING,
               "CSGO",
               "CSGO (Vertical)",
               XMaterial.GUNPOWDER.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "CSGO Animation",
               "https://www.phoenixplugins.com/product/physical-csgo-animation"
            )
            .setDescription("Vertical spinning reel in the world")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.OPENING,
               "HALLOWEEN_SPOOKY_ASCENSION",
               "Spooky Ascension (Halloween)",
               XMaterial.WITHER_SKELETON_SKULL.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Halloween Animation",
               "https://www.phoenixplugins.com/product/halloween-animation"
            )
            .setDescription("Eerie rise from the depths below")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.OPENING,
               "HALLOWEEN_PUMPKIN_GLOW",
               "Pumpkin's Glow (Halloween)",
               XMaterial.JACK_O_LANTERN.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Halloween Animation",
               "https://www.phoenixplugins.com/product/halloween-animation"
            )
            .setDescription("Glowing pumpkin illuminates the prize")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.OPENING,
               "HALLOWEEN_BAT_EXODUS",
               "Bat Exodus (Halloween)",
               XMaterial.BAT_SPAWN_EGG.or(XMaterial.COAL).parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Halloween Animation",
               "https://www.phoenixplugins.com/product/halloween-animation"
            )
            .setDescription("Swarm of bats erupts from the crate")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.OPENING,
               "HALLOWEEN_GHOST_RISE",
               "Ghost Rise (Halloween)",
               XMaterial.GHAST_SPAWN_EGG.or(XMaterial.BONE).parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Halloween Animation",
               "https://www.phoenixplugins.com/product/halloween-animation"
            )
            .setDescription("A ghost emerges and reveals the reward")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.OPENING,
               "CHRISTMAS_PRESENT_CANNON",
               "Present Cannon (Christmas)",
               XMaterial.DISPENSER.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Christmas Animation",
               "https://www.phoenixplugins.com/product/christmas-animation"
            )
            .setDescription("Presents launch from a cannon barrage")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.OPENING,
               "CHRISTMAS_MAGIC_ELF",
               "Magic Elf (Christmas)",
               XMaterial.VILLAGER_SPAWN_EGG.or(XMaterial.EMERALD).parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Christmas Animation",
               "https://www.phoenixplugins.com/product/christmas-animation"
            )
            .setDescription("An elf conjures your reward with magic")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.OPENING,
               "CHRISTMAS_FLYING_ORNAMENTS",
               "Flying Ornaments (Christmas)",
               XMaterial.RED_DYE.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Christmas Animation",
               "https://www.phoenixplugins.com/product/christmas-animation"
            )
            .setDescription("Ornaments swirl in a festive dance")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.DISPLAYING,
               "NONE",
               "None",
               XMaterial.BARRIER.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new ThirdPhaseNoneAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setEnvironment(AnimationEnvironment.GUI)
            .setDescription("Skips this phase")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.DISPLAYING,
               "DEFAULT",
               "Default",
               XMaterial.FLINT.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new ThirdPhaseDefaultAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               ColoredOpeningPhaseData::new
            )
            .setDescription("Standard crate opening")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
            Edition.PREMIUM,
            PhaseType.DISPLAYING,
            "RANDOM",
            "Random",
            XMaterial.ENDER_EYE.parseItem(),
            ServerVersion.v1_20_R4,
            (animation, type, properties) -> new RandomPhaseRunner((OpeningAnimationHandlerImpl)animation, type, properties),
            EmptyOpeningPhaseData::new
         )
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.DISPLAYING,
               "HELIX",
               "Helix",
               XMaterial.NETHER_STAR.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new ThirdPhaseHelixAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Double helix particle trail")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.DISPLAYING,
               "SMOKE_SPIRAL",
               "Smoke Spiral",
               XMaterial.FIREWORK_STAR.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new ThirdPhaseSmokeSpiralAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               ColoredOpeningPhaseData::new
            )
            .setDescription("Spiraling smoke effect")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.DISPLAYING,
               "FIRE_SPIRAL",
               "Fire Spiral",
               XMaterial.MAGMA_CREAM.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new ThirdPhaseFireSpiralAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Spiraling flame effect")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.DISPLAYING,
               "PHYSICAL_ITEM",
               "Physical Item",
               XMaterial.CLAY_BALL.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new ThirdPhasePhysicalItemAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Items drop on the ground")
      );
      this.registerPhase(
         new AnimationPhaseImpl(
               Edition.LITE,
               PhaseType.DISPLAYING,
               "FIREWORK",
               "Firework",
               XMaterial.FIREWORK_ROCKET.parseItem(),
               ServerVersion.v1_20_R4,
               (animation, type, properties) -> new ThirdPhaseFireworkAnimation((OpeningAnimationHandlerImpl)animation, type, properties),
               EmptyOpeningPhaseData::new
            )
            .setDescription("Firework celebration")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
            Edition.PREMIUM,
            PhaseType.DISPLAYING,
            "TREASURE_CASCADE",
            "Treasure Cascade (Lootbox)",
            XMaterial.BARRIER.parseItem(),
            ServerVersion.v1_20_R4,
            (a, t, p) -> {
               throw new NotImplementedException("Only available with the Lootbox addon.");
            },
            EmptyOpeningPhaseData::new,
            "Lootbox Addon",
            "https://www.phoenixplugins.com/product/lootboxes"
         )
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.DISPLAYING,
               "HALLOWEEN_EERIE_UNVEILING",
               "Eerie Unveiling (Halloween)",
               XMaterial.WITHER_SKELETON_SKULL.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               ColoredOpeningPhaseData::new,
               "Halloween Animation",
               "https://www.phoenixplugins.com/product/halloween-animation"
            )
            .setDescription("Spooky unveiling with dark particles")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.DISPLAYING,
               "HALLOWEEN_SPECTRAL_SURGE",
               "Spectral Surge (Halloween)",
               XMaterial.NETHERITE_SCRAP.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Halloween Animation",
               "https://www.phoenixplugins.com/product/halloween-animation"
            )
            .setDescription("Spectral energy surges around the prize")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.DISPLAYING,
               "HALLOWEEN_PUMPKIN_ESSENCE",
               "Pumpkin Essence (Halloween)",
               XMaterial.PUMPKIN.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Halloween Animation",
               "https://www.phoenixplugins.com/product/halloween-animation"
            )
            .setDescription("Pumpkin essence wraps around the reward")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.DISPLAYING,
               "HALLOWEEN_DARK_EMANATION",
               "Dark Emanation (Halloween)",
               XMaterial.BLACK_DYE.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Halloween Animation",
               "https://www.phoenixplugins.com/product/halloween-animation"
            )
            .setDescription("Dark energy emanates from the prize")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.DISPLAYING,
               "CHRISTMAS_ESSENCE",
               "Christmas Essence (Christmas)",
               XMaterial.SWEET_BERRIES.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Christmas Animation",
               "https://www.phoenixplugins.com/product/christmas-animation"
            )
            .setDescription("Warm holiday glow reveals the prize")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.DISPLAYING,
               "CHRISTMAS_MERRY_CYCLONE",
               "Merry Cyclone (Christmas)",
               XMaterial.RED_DYE.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Christmas Animation",
               "https://www.phoenixplugins.com/product/christmas-animation"
            )
            .setDescription("Merry whirlwind of festive particles")
      );
      this.registerPhase(
         new AddonLockedAnimationPhaseImpl(
               Edition.PRO,
               PhaseType.DISPLAYING,
               "CHRISTMAS_SNOWFALL",
               "Snow Fall (Christmas)",
               XMaterial.SNOWBALL.parseItem(),
               ServerVersion.v1_20_R4,
               (a, t, p) -> {
                  throw new NotImplementedException("Only available as addon.");
               },
               EmptyOpeningPhaseData::new,
               "Christmas Animation",
               "https://www.phoenixplugins.com/product/christmas-animation"
            )
            .setDescription("Gentle snowfall as the reward appears")
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "NONE",
            "None",
            XMaterial.BARRIER.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "DEFAULT", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "NONE", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "NONE", new EmptyOpeningPhaseData().asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "CLASSIC",
            "Classic",
            XMaterial.NETHER_STAR.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "DEFAULT", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "SWIRL", new ColoredOpeningPhaseData(Color.decode("#F2F2F2")).asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "DEFAULT", new ColoredOpeningPhaseData(Color.decode("#F2F2F2")).asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "BLAZING",
            "Blazing",
            XMaterial.BLAZE_POWDER.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "FIRE", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "FIRE", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "FIRE_SPIRAL", new EmptyOpeningPhaseData().asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "KEYSTORM",
            "Keystorm",
            XMaterial.TRIPWIRE_HOOK.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "KEY_OPENER", new KeybasedOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "SWIRL", new ColoredOpeningPhaseData(Color.decode("#FFC857")).asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "HELIX", new EmptyOpeningPhaseData().asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "ARCADE",
            "Arcade",
            XMaterial.ENDER_PEARL.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "CRACK", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "COMPACT", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "DEFAULT", new ColoredOpeningPhaseData(Color.decode("#7FDBFF")).asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "BOUNCY",
            "Bouncy",
            XMaterial.SLIME_BALL.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "DEFAULT", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "BALL", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "HELIX", new EmptyOpeningPhaseData().asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "LIGHTNING_STORM",
            "Lightning Storm",
            XMaterial.STICK.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "LIGHTNING", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "SWIRL", new ColoredOpeningPhaseData(Color.decode("#B0C4DE")).asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "SMOKE_SPIRAL", new ColoredOpeningPhaseData(Color.decode("#B0C4DE")).asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "PHANTOM",
            "Phantom",
            XMaterial.LEATHER.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "BLASTING", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "ROTATING_HEAD", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "HELIX", new EmptyOpeningPhaseData().asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "CSGO_STYLE",
            "CSGO",
            XMaterial.CHEST.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "DEFAULT", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "CSGO_GUI", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "NONE", new EmptyOpeningPhaseData().asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "RING_CAROUSEL",
            "Ring Carousel",
            XMaterial.CLOCK.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "DEFAULT", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "RING_CAROUSEL_GUI", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "NONE", new EmptyOpeningPhaseData().asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "FLASH_CYCLE",
            "Flash Cycle",
            XMaterial.MAGENTA_GLAZED_TERRACOTTA.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "DEFAULT", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "FLASH_CYCLE_GUI", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "NONE", new EmptyOpeningPhaseData().asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "FIREWORKS",
            "Fireworks",
            XMaterial.FIREWORK_ROCKET.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "DEFAULT", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "SWIRL", new ColoredOpeningPhaseData(Color.decode("#FFD700")).asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "FIREWORK", new EmptyOpeningPhaseData().asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "VOID",
            "Void",
            XMaterial.ENDER_EYE.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "CRACK", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "COMPACT", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "PHYSICAL_ITEM", new EmptyOpeningPhaseData().asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "SMOKY",
            "Smoky",
            XMaterial.GRAY_DYE.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "DEFAULT", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "BALL", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "SMOKE_SPIRAL", new ColoredOpeningPhaseData(Color.decode("#888888")).asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "SONIC",
            "Sonic",
            XMaterial.WIND_CHARGE.or(XMaterial.FEATHER).parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "SONIC_BOOM", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "SWIRL", new ColoredOpeningPhaseData(Color.decode("#00CED1")).asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "FIRE_SPIRAL", new EmptyOpeningPhaseData().asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "KEY_MASTER",
            "Key Master",
            XMaterial.TRIPWIRE_HOOK.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "KEY_OPENER", new KeybasedOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "COMPACT", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "DEFAULT", new ColoredOpeningPhaseData(Color.decode("#FFD700")).asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "ELECTRIC",
            "Electric",
            XMaterial.LIGHTNING_ROD.or(XMaterial.STICK).parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "LIGHTNING", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "BALL", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "DEFAULT", new ColoredOpeningPhaseData(Color.decode("#FFFF00")).asProperties())
         )
      );
      this.registerPreset(
         new OpeningAnimationPreset(
            "CREEPER",
            "Creeper",
            XMaterial.CREEPER_HEAD.parseItem(),
            this.presetPhase(PhaseType.PRE_OPENING, "LIGHTNING", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.OPENING, "CREEPER", new EmptyOpeningPhaseData().asProperties()),
            this.presetPhase(PhaseType.DISPLAYING, "NONE", new EmptyOpeningPhaseData().asProperties())
         )
      );
   }

   @Override
   public Optional<AnimationPhase> getPhaseById(PhaseType type, String id) {
      if (type != null && id != null && !id.isEmpty()) {
         synchronized (this.registry) {
            for (AnimationPhase phase : this.registry.get(type)) {
               if (phase.getId().equalsIgnoreCase(id)) {
                  return Optional.of(phase);
               }
            }
         }

         return Optional.empty();
      } else {
         throw new IllegalArgumentException("Type and ID must not be null or empty");
      }
   }

   @Override
   public void registerPhase(AnimationPhase phase) {
      if (phase == null) {
         throw new IllegalArgumentException("Phase must not be null");
      }

      synchronized (this.registry) {
         this.registry.get(phase.getType()).removeIf(existing -> existing.getId().equalsIgnoreCase(phase.getId()));
         this.registry.put(phase.getType(), phase);
      }
   }

   @Override
   public void unregisterPhase(AnimationPhase phase) {
      if (phase == null) {
         throw new IllegalArgumentException("Phase must not be null");
      }

      synchronized (this.registry) {
         this.registry.remove(phase.getType(), phase);
      }
   }

   @Override
   public Collection<AnimationPhase> getPhases(PhaseType type) {
      if (type == null) {
         throw new IllegalArgumentException("Type must not be null");
      }

      synchronized (this.registry) {
         Collection<AnimationPhase> phases = this.registry.get(type);
         return Collections.unmodifiableCollection(phases);
      }
   }

   public void registerPreset(OpeningAnimationPreset preset) {
      if (preset == null) {
         throw new IllegalArgumentException("Preset must not be null");
      }

      synchronized (this.presets) {
         this.presets.put(preset.getId().toUpperCase(Locale.ROOT), preset);
      }
   }

   public Optional<OpeningAnimationPreset> getPresetById(String id) {
      if (id != null && !id.isEmpty()) {
         synchronized (this.presets) {
            return Optional.ofNullable(this.presets.get(id.toUpperCase(Locale.ROOT)));
         }
      } else {
         return Optional.empty();
      }
   }

   public Optional<OpeningAnimationPreset> getPresetByAnimation(OpeningAnimation animation) {
      if (animation == null) {
         return Optional.empty();
      }

      synchronized (this.presets) {
         return this.presets.values().stream().filter(preset -> preset.matches(animation)).findFirst();
      }
   }

   public Collection<OpeningAnimationPreset> getPresets() {
      synchronized (this.presets) {
         return Collections.unmodifiableCollection(this.presets.values());
      }
   }

   private OpeningPhase presetPhase(PhaseType type, String phaseId, AnimationProperties properties) {
      AnimationPhase phase = this.getPhaseById(type, phaseId)
         .orElseThrow(() -> new IllegalStateException("Unknown animation phase for preset: " + type + ":" + phaseId));
      return new OpeningPhase(phase, properties.clone());
   }

   @Generated
   public Multimap<PhaseType, AnimationPhase> getRegistry() {
      return this.registry;
   }
}
