package id.seria.gacha.lib.common.nms.interfaces.particles;

import id.seria.gacha.lib.common.nms.interfaces.particles.data.ParticleData;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.VibrationData;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.DustColorTransitionData;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.DustData;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.ParticleColor;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.RegularColor;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.texture.BlockTexture;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.texture.ItemTexture;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleFunction;
import lombok.Generated;

public enum Particle {
   ASH(version -> version < 16.0 ? "NONE" : "ash"),
   BARRIER(version -> !(version < 8.0) && !(version > 17.0) ? (version < 13.0 ? "BARRIER" : "barrier") : "NONE"),
   BLOCK_CRACK(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "BLOCK_CRACK" : "block"), PropertyType.REQUIRES_BLOCK),
   BLOCK_DUST(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "BLOCK_DUST" : "falling_dust"), PropertyType.DIRECTIONAL, PropertyType.REQUIRES_BLOCK),
   BUBBLE_COLUMN_UP(version -> version < 13.0 ? "NONE" : "bubble_column_up", PropertyType.DIRECTIONAL),
   BLOCK_MARKER(version -> version < 18.0 ? "NONE" : "block_marker", PropertyType.REQUIRES_BLOCK),
   BUBBLE_POP(version -> version < 13.0 ? "NONE" : "bubble_pop", PropertyType.DIRECTIONAL),
   CAMPFIRE_COSY_SMOKE(version -> version < 14.0 ? "NONE" : "campfire_cosy_smoke", PropertyType.DIRECTIONAL),
   CAMPFIRE_SIGNAL_SMOKE(version -> version < 14.0 ? "NONE" : "campfire_signal_smoke", PropertyType.DIRECTIONAL),
   CLOUD(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "CLOUD" : "cloud"), PropertyType.DIRECTIONAL),
   COMPOSTER(version -> version < 14.0 ? "NONE" : "composter"),
   CRIMSON_SPORE(version -> version < 16.0 ? "NONE" : "crimson_spore"),
   CRIT(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "CRIT" : "crit"), PropertyType.DIRECTIONAL),
   CRIT_MAGIC(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "CRIT_MAGIC" : "enchanted_hit"), PropertyType.DIRECTIONAL),
   CURRENT_DOWN(version -> version < 13.0 ? "NONE" : "current_down"),
   DAMAGE_INDICATOR(version -> version < 9.0 ? "NONE" : (version < 13.0 ? "DAMAGE_INDICATOR" : "damage_indicator"), PropertyType.DIRECTIONAL),
   DOLPHIN(version -> version < 13.0 ? "NONE" : "dolphin"),
   DRAGON_BREATH(version -> version < 9.0 ? "NONE" : (version < 13.0 ? "DRAGON_BREATH" : "dragon_breath"), PropertyType.DIRECTIONAL),
   DRIP_LAVA(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "DRIP_LAVA" : "dripping_lava")),
   DRIP_WATER(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "DRIP_WATER" : "dripping_water")),
   DRIPPING_DRIPSTONE_LAVA(version -> version < 17.0 ? "NONE" : "dripping_dripstone_lava"),
   DRIPPING_DRIPSTONE_WATER(version -> version < 17.0 ? "NONE" : "dripping_dripstone_water"),
   DRIPPING_HONEY(version -> version < 15.0 ? "NONE" : "dripping_honey"),
   DRIPPING_OBSIDIAN_TEAR(version -> version < 16.0 ? "NONE" : "dripping_obsidian_tear"),
   DUST_COLOR_TRANSITION(version -> version < 17.0 ? "NONE" : "dust_color_transition", PropertyType.COLORABLE, PropertyType.DUST),
   ELECTRIC_SPARK(version -> version < 17.0 ? "NONE" : "electric_spark", PropertyType.DIRECTIONAL),
   ENCHANTMENT_TABLE(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "ENCHANTMENT_TABLE" : "enchant"), PropertyType.DIRECTIONAL),
   END_ROD(version -> version < 9.0 ? "NONE" : (version < 13.0 ? "END_ROD" : "end_rod"), PropertyType.DIRECTIONAL),
   EXPLOSION_HUGE(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "EXPLOSION_HUGE" : "explosion_emitter")),
   EXPLOSION_LARGE(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "EXPLOSION_LARGE" : "explosion")),
   EXPLOSION_NORMAL(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "EXPLOSION_NORMAL" : "poof"), PropertyType.DIRECTIONAL),
   FALLING_DRIPSTONE_LAVA(version -> version < 17.0 ? "NONE" : "falling_dripstone_lava"),
   FALLING_DRIPSTONE_WATER(version -> version < 17.0 ? "NONE" : "falling_dripstone_water"),
   FALLING_DUST(version -> version < 10.0 ? "NONE" : (version < 13.0 ? "FALLING_DUST" : "falling_dust"), PropertyType.REQUIRES_BLOCK),
   FALLING_HONEY(version -> version < 15.0 ? "NONE" : "falling_honey"),
   FALLING_NECTAR(version -> version < 15.0 ? "NONE" : "falling_nectar"),
   FALLING_OBSIDIAN_TEAR(version -> version < 16.0 ? "NONE" : "falling_obsidian_tear"),
   FALLING_SPORE_BLOSSOM(version -> version < 17.0 ? "NONE" : "falling_spore_blossom"),
   FIREWORKS_SPARK(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "FIREWORKS_SPARK" : "firework"), PropertyType.DIRECTIONAL),
   FLAME(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "FLAME" : "flame"), PropertyType.DIRECTIONAL),
   FLASH(version -> version < 14.0 ? "NONE" : "flash"),
   FOOTSTEP(version -> version > 8.0 && version < 13.0 ? "FOOTSTEP" : "NONE"),
   GLOW(version -> version < 17.0 ? "NONE" : "glow", PropertyType.DIRECTIONAL),
   GLOW_SQUID_INK(version -> version < 17.0 ? "NONE" : "glow_squid_ink", PropertyType.DIRECTIONAL),
   HEART(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "HEART" : "heart")),
   ITEM_CRACK(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "ITEM_CRACK" : "item"), PropertyType.DIRECTIONAL, PropertyType.REQUIRES_ITEM),
   LANDING_HONEY(version -> version < 15.0 ? "NONE" : "landing_honey"),
   LANDING_OBSIDIAN_TEAR(version -> version < 16.0 ? "NONE" : "landing_obsidian_tear"),
   LAVA(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "LAVA" : "lava")),
   LIGHT(version -> version != 17.0 ? "NONE" : "light"),
   MOB_APPEARANCE(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "MOB_APPEARANCE" : "elder_guardian")),
   NAUTILUS(version -> version < 13.0 ? "NONE" : "nautilus", PropertyType.DIRECTIONAL),
   NOTE(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "NOTE" : "note")),
   PORTAL(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "PORTAL" : "portal"), PropertyType.DIRECTIONAL),
   REDSTONE(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "REDSTONE" : "dust"), PropertyType.COLORABLE, PropertyType.DUST),
   REVERSE_PORTAL(version -> version < 16.0 ? "NONE" : "reverse_portal", PropertyType.DIRECTIONAL),
   SCRAPE(version -> version < 17.0 ? "NONE" : "scrape", PropertyType.DIRECTIONAL),
   SCULK_CHARGE(version -> version < 19.0 ? "NONE" : "sculk_charge", PropertyType.DIRECTIONAL),
   SCULK_CHARGE_POP(version -> version < 19.0 ? "NONE" : "sculk_charge_pop", PropertyType.DIRECTIONAL),
   SCULK_SOUL(version -> version < 19.0 ? "NONE" : "sculk_soul", PropertyType.DIRECTIONAL),
   SHRIEK(version -> version < 19.0 ? "NONE" : "shriek"),
   SLIME(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "SLIME" : "item_slime")),
   SMALL_FLAME(version -> version < 17.0 ? "NONE" : "small_flame", PropertyType.DIRECTIONAL),
   SMOKE_LARGE(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "SMOKE_LARGE" : "large_smoke"), PropertyType.DIRECTIONAL),
   SMOKE_NORMAL(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "SMOKE_NORMAL" : "smoke"), PropertyType.DIRECTIONAL),
   SNEEZE(version -> version < 14.0 ? "NONE" : "sneeze", PropertyType.DIRECTIONAL),
   SNOWBALL(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "SNOWBALL" : "item_snowball")),
   SNOWFLAKE(version -> version < 17.0 ? "NONE" : "snowflake", PropertyType.DIRECTIONAL),
   SNOW_SHOVEL(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "SNOW_SHOVEL" : "poof"), PropertyType.DIRECTIONAL),
   SONIC_BOOM(version -> version < 19.0 ? "NONE" : "sonic_boom"),
   SOUL(version -> version < 16.0 ? "NONE" : "soul", PropertyType.DIRECTIONAL),
   SOUL_FIRE_FLAME(version -> version < 16.0 ? "NONE" : "soul_fire_flame", PropertyType.DIRECTIONAL),
   SPELL(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "SPELL" : "effect")),
   SPELL_INSTANT(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "SPELL_INSTANT" : "instant_effect")),
   SPELL_MOB(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "SPELL_MOB" : "entity_effect"), PropertyType.COLORABLE),
   SPELL_MOB_AMBIENT(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "SPELL_MOB_AMBIENT" : "ambient_entity_effect"), PropertyType.COLORABLE),
   SPELL_WITCH(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "SPELL_WITCH" : "witch")),
   SPIT(version -> version < 11.0 ? "NONE" : (version < 13.0 ? "SPIT" : "spit")),
   SPORE_BLOSSOM_AIR(version -> version < 17.0 ? "NONE" : "spore_blossom_air"),
   SQUID_INK(version -> version < 13.0 ? "NONE" : "squid_ink", PropertyType.DIRECTIONAL),
   SUSPENDED(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "SUSPENDED" : "underwater"), PropertyType.REQUIRES_WATER),
   SUSPENDED_DEPTH(version -> version > 8.0 && version < 13.0 ? "SUSPENDED_DEPTH" : "NONE", PropertyType.DIRECTIONAL),
   SWEEP_ATTACK(version -> version < 9.0 ? "NONE" : (version < 13.0 ? "SWEEP_ATTACK" : "sweep_attack"), PropertyType.RESIZEABLE),
   TOTEM(version -> version < 11.0 ? "NONE" : (version < 13.0 ? "TOTEM" : "totem_of_undying"), PropertyType.DIRECTIONAL),
   TOWN_AURA(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "TOWN_AURA" : "mycelium"), PropertyType.DIRECTIONAL),
   VIBRATION(version -> version < 17.0 ? "NONE" : "vibration", PropertyType.DIRECTIONAL),
   VILLAGER_ANGRY(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "VILLAGER_ANGRY" : "angry_villager")),
   VILLAGER_HAPPY(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "VILLAGER_HAPPY" : "happy_villager"), PropertyType.DIRECTIONAL),
   WARPED_SPORE(version -> version < 16.0 ? "NONE" : "warped_spore"),
   WATER_BUBBLE(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "WATER_BUBBLE" : "bubble"), PropertyType.DIRECTIONAL, PropertyType.REQUIRES_WATER),
   WATER_DROP(version -> version > 8.0 && version < 13.0 ? "WATER_DROP" : "NONE"),
   WATER_SPLASH(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "WATER_SPLASH" : "splash"), PropertyType.DIRECTIONAL),
   WATER_WAKE(version -> version < 8.0 ? "NONE" : (version < 13.0 ? "WATER_WAKE" : "fishing"), PropertyType.DIRECTIONAL),
   WAX_OFF(version -> version < 17.0 ? "NONE" : "wax_off", PropertyType.DIRECTIONAL),
   WAX_ON(version -> version < 17.0 ? "NONE" : "wax_on", PropertyType.DIRECTIONAL),
   WHITE_ASH(version -> version < 16.0 ? "NONE" : "white_ash"),
   RAIN(version -> version < 21.0 ? "NONE" : "rain"),
   CHERRY_LEAVES(version -> version < 20.0 ? "NONE" : "cherry_leaves"),
   EGG_CRACK(version -> version < 20.0 ? "NONE" : "egg_crack"),
   DUST_PLUME(version -> version < 21.0 ? "NONE" : "dust_plume"),
   TRIAL_SPAWNER_DETECTION(version -> version < 21.0 ? "NONE" : "trial_spawner_detection"),
   TRIAL_SPAWNER_DETECTION_OMINOUS(version -> version < 21.0 ? "NONE" : "trial_spawner_detection_ominous"),
   TRIAL_OMEN(version -> version < 21.0 ? "NONE" : "trial_omen"),
   VAULT_CONNECTION(version -> version < 21.0 ? "NONE" : "vault_connection"),
   GUST(version -> version < 21.0 ? "NONE" : "gust"),
   SMALL_GUST(version -> version < 21.0 ? "NONE" : "small_gust"),
   GUST_EMITTER_LARGE(version -> version < 21.0 ? "NONE" : "gust_emitter_large"),
   GUST_EMITTER_SMALL(version -> version < 21.0 ? "NONE" : "gust_emitter_small");

   public static final List<Particle> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
   private final DoubleFunction<String> fieldNameMapper;
   private final List<PropertyType> properties;

   Particle(DoubleFunction<String> fieldNameMapper, PropertyType... properties) {
      this.fieldNameMapper = fieldNameMapper;
      this.properties = Collections.unmodifiableList(Arrays.asList(properties));
   }

   public boolean hasProperty(PropertyType propertyType) {
      return propertyType != null && this.properties.contains(propertyType);
   }

   public boolean isCorrectData(ParticleData data) {
      if (data == null) {
         return true;
      } else if (data instanceof ParticleColor) {
         return this.isCorrectColor((ParticleColor)data);
      } else if (data instanceof BlockTexture) {
         return this.hasProperty(PropertyType.REQUIRES_BLOCK);
      } else {
         return data instanceof ItemTexture ? this.hasProperty(PropertyType.REQUIRES_ITEM) : data instanceof VibrationData && this == VIBRATION;
      }
   }

   public boolean isCorrectColor(ParticleColor color) {
      if (color instanceof DustColorTransitionData) {
         return this == DUST_COLOR_TRANSITION;
      } else {
         return color instanceof DustData ? this.hasProperty(PropertyType.DUST) : this.hasProperty(PropertyType.COLORABLE) && color instanceof RegularColor;
      }
   }

   @Generated
   public DoubleFunction<String> getFieldNameMapper() {
      return this.fieldNameMapper;
   }

   @Generated
   public List<PropertyType> getProperties() {
      return this.properties;
   }
}
