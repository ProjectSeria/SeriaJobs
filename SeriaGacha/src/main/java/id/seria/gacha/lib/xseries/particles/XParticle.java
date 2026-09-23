package id.seria.gacha.lib.xseries.particles;

import id.seria.gacha.lib.xseries.base.XBase;
import id.seria.gacha.lib.xseries.base.XRegistry;
import id.seria.gacha.lib.xseries.base.annotations.XMerge;
import id.seria.gacha.lib.xseries.base.annotations.XMerges;
import java.util.Optional;
import java.util.function.Function;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;

public enum XParticle implements XBase<XParticle, Particle> {
   ANGRY_VILLAGER("VILLAGER_ANGRY"),
   ASH(),
   @XMerge(version = "1.20.5", name = "BLOCK_DUST")
   BLOCK(name -> name.equalsIgnoreCase("BLOCK_CRACK") ? ParticleDisplay.ParticleMaterialData.class : ParticleDisplay.ParticleBlockData.class, "BLOCK_CRACK"),
   BLOCK_CRUMBLE(),
   @XMerge(version = "1.17", name = "LIGHT")
   BLOCK_MARKER("BARRIER"),
   BUBBLE("WATER_BUBBLE"),
   BUBBLE_COLUMN_UP(),
   BUBBLE_POP(),
   CAMPFIRE_COSY_SMOKE(),
   CAMPFIRE_SIGNAL_SMOKE(),
   @XMerges({@XMerge(version = "1.20", name = "FALLING_CHERRY_LEAVES"), @XMerge(version = "1.20", name = "LANDING_CHERRY_LEAVES")})
   CHERRY_LEAVES("DRIPPING_CHERRY_LEAVES"),
   CLOUD(),
   COMPOSTER(),
   CRIMSON_SPORE(),
   CRIT(),
   CURRENT_DOWN(),
   DAMAGE_INDICATOR(),
   DOLPHIN(),
   DRAGON_BREATH(ParticleDisplay.ConstantParticleData.class),
   DRIPPING_DRIPSTONE_LAVA(),
   DRIPPING_DRIPSTONE_WATER(),
   DRIPPING_HONEY(),
   DRIPPING_LAVA("DRIP_LAVA"),
   DRIPPING_OBSIDIAN_TEAR(),
   DRIPPING_WATER("DRIP_WATER"),
   DUST("REDSTONE"),
   DUST_COLOR_TRANSITION(),
   DUST_PILLAR(),
   DUST_PLUME(),
   EFFECT("SPELL"),
   EGG_CRACK(),
   ELDER_GUARDIAN("MOB_APPEARANCE"),
   ELECTRIC_SPARK(),
   ENCHANT("ENCHANTMENT_TABLE"),
   ENCHANTED_HIT("CRIT_MAGIC"),
   END_ROD(),
   @XMerge(version = "1.20.5", name = "SPELL_MOB_AMBIENT")
   ENTITY_EFFECT("SPELL_MOB"),
   EXPLOSION("EXPLOSION_LARGE"),
   EXPLOSION_EMITTER("EXPLOSION_HUGE"),
   FALLING_DRIPSTONE_LAVA(),
   FALLING_DRIPSTONE_WATER(),
   FALLING_DUST(),
   FALLING_HONEY(),
   FALLING_LAVA(),
   FALLING_NECTAR(),
   FALLING_OBSIDIAN_TEAR(),
   FALLING_SPORE_BLOSSOM(),
   FALLING_WATER(),
   FIREFLY(),
   FIREWORK("FIREWORKS_SPARK"),
   FISHING("WATER_WAKE"),
   FLAME(),
   FLASH(),
   @Deprecated
   FOOTSTEP(),
   GLOW(),
   GLOW_SQUID_INK(),
   GUST(),
   GUST_EMITTER_LARGE(),
   GUST_EMITTER_SMALL(),
   HAPPY_VILLAGER("VILLAGER_HAPPY"),
   HEART(),
   INFESTED(),
   INSTANT_EFFECT("SPELL_INSTANT"),
   ITEM(ParticleDisplay.ParticleItemData.class, "ITEM_CRACK"),
   ITEM_COBWEB(),
   ITEM_SLIME("SLIME"),
   @XMerge(version = "1.20.5", name = "SNOW_SHOVEL")
   ITEM_SNOWBALL("SNOWBALL"),
   @Deprecated
   ITEM_TAKE(),
   LANDING_HONEY(),
   LANDING_LAVA(),
   LANDING_OBSIDIAN_TEAR(),
   LARGE_SMOKE("SMOKE_LARGE"),
   LAVA(),
   MYCELIUM("TOWN_AURA"),
   NAUTILUS(),
   NOTE(),
   OMINOUS_SPAWNING(),
   PALE_OAK_LEAVES(),
   POOF("EXPLOSION_NORMAL"),
   PORTAL(),
   RAID_OMEN(),
   RAIN("WATER_DROP"),
   REVERSE_PORTAL(),
   SCRAPE(),
   SCULK_CHARGE(),
   SCULK_CHARGE_POP(),
   SCULK_SOUL(),
   SHRIEK(),
   SMALL_FLAME(),
   SMALL_GUST(),
   SMOKE("SMOKE_NORMAL"),
   SNEEZE(),
   SNOWFLAKE(),
   SONIC_BOOM(),
   SOUL(),
   SOUL_FIRE_FLAME(),
   SPIT(),
   SPLASH("WATER_SPLASH"),
   SPORE_BLOSSOM_AIR(),
   SQUID_INK(),
   SWEEP_ATTACK(),
   TINTED_LEAVES(),
   TOTEM_OF_UNDYING("TOTEM"),
   TRAIL(),
   TRIAL_OMEN(),
   TRIAL_SPAWNER_DETECTION(),
   TRIAL_SPAWNER_DETECTION_OMINOUS(),
   @XMerge(version = "1.20.5", name = "SUSPENDED_DEPTH")
   UNDERWATER("SUSPENDED"),
   VAULT_CONNECTION(),
   VIBRATION(),
   WARPED_SPORE(),
   WAX_OFF(),
   WAX_ON(),
   WHITE_ASH(),
   WHITE_SMOKE(),
   WITCH("SPELL_WITCH"),
   COPPER_FIRE_FLAME(),
   PAUSE_MOB_GROWTH(),
   RESET_MOB_GROWTH();

   public static final XRegistry<XParticle, Particle> REGISTRY = XParticle.Data.REGISTRY;
   private final Particle particle;
   private final Class<? extends ParticleDisplay.ParticleData> dataType;

   XParticle(@Nullable Function<String, Class<? extends ParticleDisplay.ParticleData>> dataType, @NotNull String... names) {
      this.particle = XParticle.Data.REGISTRY.stdEnum(this, names);
      this.dataType = dataType == null ? null : dataType.apply(this.particle.name());
   }

   XParticle(@Nullable Class<? extends ParticleDisplay.ParticleData> dataType, @NotNull String... names) {
      this(dataType == null ? null : x -> dataType, names);
   }

   XParticle(String... names) {
      this((Function<String, Class<? extends ParticleDisplay.ParticleData>>)null, names);
   }

   @Nullable
   @Experimental
   public Class<? extends ParticleDisplay.ParticleData> getDataType() {
      return this.dataType;
   }

   @Override
   public String[] getNames() {
      return new String[]{this.name()};
   }

   public Particle get() {
      return this.particle;
   }

   public static XParticle of(Particle particle) {
      return REGISTRY.getByBukkitForm(particle);
   }

   public static Optional<XParticle> of(String particle) {
      return REGISTRY.getByName(particle);
   }

   static {
      REGISTRY.discardMetadata();
   }

   private static final class Data {
      private static final XRegistry<XParticle, Particle> REGISTRY = new XRegistry<>(Particle.class, XParticle.class, x$0 -> new XParticle[x$0]);

      private Data() {
      }
   }
}
