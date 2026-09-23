package id.seria.gacha.lib.xseries;

import id.seria.gacha.lib.xseries.base.XModule;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public final class XGameRule<T> extends XModule<XGameRule<T>, String> {
   private static final boolean SUPPORTS_GameRule;
   private static final MethodHandle GameRule_getByName;
   private static final MethodHandle World_getGameRuleValue;
   private static final MethodHandle World_setGameRuleValue;
   public static final XGameRule<Boolean> COMMAND_BLOCK_OUTPUT;
   public static final XGameRule<Boolean> ADVANCE_TIME;
   public static final XGameRule<Boolean> ENTITY_DROPS;
   public static final XGameRule<Boolean> MOB_DROPS;
   public static final XGameRule<Boolean> SPAWN_MOBS;
   public static final XGameRule<Boolean> BLOCK_DROPS;
   public static final XGameRule<Boolean> KEEP_INVENTORY;
   public static final XGameRule<Boolean> LOG_ADMIN_COMMANDS;
   public static final XGameRule<Boolean> MOB_GRIEFING;
   public static final XGameRule<Boolean> NATURAL_HEALTH_REGENERATION;
   public static final XGameRule<Boolean> REDUCED_DEBUG_INFO;
   public static final XGameRule<Boolean> SEND_COMMAND_FEEDBACK;
   public static final XGameRule<Boolean> SHOW_DEATH_MESSAGES;
   public static final XGameRule<Integer> RANDOM_TICK_SPEED;
   public static final XGameRule<Boolean> SPECTATORS_GENERATE_CHUNKS;
   public static final XGameRule<Boolean> ELYTRA_MOVEMENT_CHECK;
   public static final XGameRule<Integer> RESPAWN_RADIUS;
   public static final XGameRule<Boolean> ADVANCE_WEATHER;
   public static final XGameRule<Integer> MAX_ENTITY_CRAMMING;
   public static final XGameRule<Boolean> LIMITED_CRAFTING;
   public static final XGameRule<Boolean> SHOW_ADVANCEMENT_MESSAGES;
   public static final XGameRule<Integer> MAX_COMMAND_SEQUENCE_LENGTH;
   @Deprecated
   public static final XGameRule<String> GAME_LOOP_FUNCTION;
   public static final XGameRule<Boolean> RAIDS;
   public static final XGameRule<Boolean> SPAWN_PHANTOMS;
   public static final XGameRule<Boolean> IMMEDIATE_RESPAWN;
   public static final XGameRule<Boolean> DROWNING_DAMAGE;
   public static final XGameRule<Boolean> FALL_DAMAGE;
   public static final XGameRule<Boolean> FIRE_DAMAGE;
   public static final XGameRule<Boolean> SPAWN_PATROLS;
   public static final XGameRule<Boolean> SPAWN_WANDERING_TRADERS;
   public static final XGameRule<Boolean> FORGIVE_DEAD_PLAYERS;
   public static final XGameRule<Boolean> UNIVERSAL_ANGER;
   public static final XGameRule<Boolean> FREEZE_DAMAGE;
   public static final XGameRule<Integer> PLAYERS_SLEEPING_PERCENTAGE;
   public static final XGameRule<Boolean> SPAWN_WARDENS;
   public static final XGameRule<Boolean> BLOCK_EXPLOSION_DROP_DECAY;
   public static final XGameRule<Boolean> MOB_EXPLOSION_DROP_DECAY;
   public static final XGameRule<Boolean> TNT_EXPLOSION_DROP_DECAY;
   public static final XGameRule<Boolean> WATER_SOURCE_CONVERSION;
   public static final XGameRule<Boolean> LAVA_SOURCE_CONVERSION;
   public static final XGameRule<Boolean> GLOBAL_SOUND_EVENTS;
   public static final XGameRule<Integer> MAX_SNOW_ACCUMULATION_HEIGHT;
   public static final XGameRule<Boolean> SPREAD_VINES;
   public static final XGameRule<Integer> MAX_BLOCK_MODIFICATIONS;
   public static final XGameRule<Boolean> ENDER_PEARLS_VANISH_ON_DEATH;
   public static final XGameRule<Boolean> PROJECTILES_CAN_BREAK_BLOCKS;
   public static final XGameRule<Integer> MAX_COMMAND_FORKS;
   public static final XGameRule<Integer> PLAYERS_NETHER_PORTAL_DEFAULT_DELAY;
   public static final XGameRule<Integer> PLAYERS_NETHER_PORTAL_CREATIVE_DELAY;
   @Deprecated
   public static final XGameRule<Integer> SPAWN_CHUNK_RADIUS;
   public static final XGameRule<Boolean> PLAYER_MOVEMENT_CHECK;
   public static final XGameRule<Integer> MAX_MINECART_SPEED;
   public static final XGameRule<Boolean> TNT_EXPLODES;
   @Deprecated
   public static final XGameRule<Boolean> ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER;
   public static final XGameRule<Boolean> LOCATOR_BAR;
   public static final XGameRule<Boolean> PVP;
   public static final XGameRule<Boolean> ALLOW_ENTERING_NETHER_USING_PORTALS;
   public static final XGameRule<Boolean> SPAWN_MONSTERS;
   public static final XGameRule<Boolean> COMMAND_BLOCKS_WORK;
   public static final XGameRule<Boolean> SPAWNER_BLOCKS_WORK;
   public static final XGameRule<Integer> FIRE_SPREAD_RADIUS_AROUND_PLAYER;
   @Deprecated
   public static final XGameRule<Boolean> DO_FIRE_TICK;
   @NotNull
   private final Class<?> type;
   @Nullable
   private final Object gamerule;

   private XGameRule(@NotNull Class<T> type, String usableName, Object bukkitObject, @NotNull String... names) {
      super(usableName, names);
      this.type = type;
      this.gamerule = bukkitObject;
      if (usableName != null) {
         XGameRule.Data.NAME_MAPPING.put(usableName.toLowerCase(Locale.ENGLISH), this);
      }

      if (bukkitObject != null) {
         XGameRule.Data.OBJECT_MAPPING.put((GameRule<?>)bukkitObject, this);
      }

      Arrays.stream(names).forEach(x -> XGameRule.Data.NAME_MAPPING.put(x.toLowerCase(Locale.ENGLISH), this));
   }

   private static <T> XGameRule<T> std(@NotNull Class<T> type, @NotNull String... names) {
      if (names != null && names.length != 0) {
         Objects.requireNonNull(type, () -> "Type of " + names[0] + " is null");
         Object bukkitObject;
         String usableName;
         if (XGameRule.Data.SUPPORTS_Registry_GAME_RULE) {
            GameRule<?> gameRule = Arrays.stream(names).map(x$0 -> XGameRule.Data.getGameRule(x$0)).filter(Objects::nonNull).findAny().orElse(null);
            bukkitObject = gameRule;
            if (gameRule != null) {
               usableName = gameRule.getName();
            } else {
               usableName = null;
            }

            if (gameRule != null && type != gameRule.getType()) {
               new IllegalStateException("Game rule type mismatch: " + names[0] + " (" + type + ") != " + gameRule + " (" + gameRule.getType() + ')')
                  .printStackTrace();
            }
         } else if (GameRule_getByName != null) {
            GameRule<?> gameRule = Arrays.stream(names).map(x -> {
               try {
                  return (GameRule)GameRule_getByName.invokeExact((String)x);
               } catch (Throwable e) {
                  throw new RuntimeException(e);
               }
            }).filter(Objects::nonNull).findAny().orElse(null);
            bukkitObject = gameRule;
            usableName = gameRule.getName();
         } else {
            World world = (World)Bukkit.getWorlds().get(0);
            usableName = Arrays.stream(names).filter(world::isGameRule).findAny().orElse(null);
            bukkitObject = null;
         }

         return new XGameRule<>(type, usableName, bukkitObject, names);
      } else {
         throw new IllegalArgumentException("No name provided for rule with type: " + type);
      }
   }

   @Override
   public boolean isSupported() {
      return super.isSupported() && (!SUPPORTS_GameRule || this.gamerule != null);
   }

   @NotNull
   public Class<?> getType() {
      return this.type;
   }

   @Contract(pure = true)
   @Nullable
   public T getValue(@NotNull World world) {
      Objects.requireNonNull(world, "World is null");
      if (!this.isSupported()) {
         throw new UnsupportedOperationException("Game rule " + this.name() + " not supported on this version!");
      }

      if (SUPPORTS_GameRule) {
         GameRule<T> rule = (GameRule<T>)this.gamerule;
         return (T)world.getGameRuleValue(rule);
      }

      String value;
      try {
         value = (String)World_getGameRuleValue.invokeExact((World)world, (String)this.get());
      } catch (Throwable e) {
         throw new RuntimeException(e);
      }

      if (this.type == Boolean.class) {
         return (T)Boolean.valueOf(Boolean.parseBoolean(value));
      } else if (this.type == Integer.class) {
         return (T)Integer.valueOf(Integer.parseInt(value));
      } else if (this.type == String.class) {
         return (T)value;
      } else {
         throw new IllegalStateException("Unhandled type for " + this + " with value '" + value + '\'');
      }
   }

   @Contract(mutates = "param1")
   public void setValue(@NotNull World world, @NotNull T value) {
      Objects.requireNonNull(world, "World is null");
      Objects.requireNonNull(world, "Value is null");
      if (!this.isSupported()) {
         throw new UnsupportedOperationException("Game rule " + this.name() + " not supported on this version!");
      }

      if (!this.type.isInstance(value)) {
         throw new IllegalArgumentException(
            "Invalid type for GameRule " + this.name() + ": " + value + " (" + value.getClass() + ") expected " + this.type.getName()
         );
      }

      boolean returnVal;
      if (SUPPORTS_GameRule) {
         GameRule<T> rule = (GameRule<T>)this.gamerule;
         returnVal = world.setGameRule(rule, value);
      } else {
         try {
            returnVal = (boolean)World_setGameRuleValue.invokeExact((World)world, (String)this.get(), (String)value.toString());
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      if (!returnVal) {
         throw new AssertionError(this + " returned false for world '" + world.getName() + "' with value '" + value + '\'');
      }
   }

   @NotNull
   @Contract(pure = true)
   public static <T> XGameRule<T> of(@NotNull GameRule<T> bukkit) {
      return (XGameRule<T>)XGameRule.Data.OBJECT_MAPPING.get(bukkit);
   }

   @NotNull
   @Contract(pure = true)
   public static Optional<XGameRule<?>> of(@NotNull String bukkit) {
      return Optional.ofNullable(XGameRule.Data.NAME_MAPPING.get(bukkit.toLowerCase(Locale.ENGLISH)));
   }

   @NotNull
   @Contract(pure = true)
   public static @Unmodifiable Collection<XGameRule<?>> getValues() {
      return Collections.unmodifiableCollection(XGameRule.Data.OBJECT_MAPPING.values());
   }

   static {
      boolean supportsGameRuleClass = true;
      MethodHandle getByName = null;
      MethodHandle getGameRuleValue = null;
      MethodHandle setGameRuleValue = null;
      Lookup methodHandles = MethodHandles.lookup();

      try {
         getByName = methodHandles.findStatic(GameRule.class, "getByName", MethodType.methodType(GameRule.class, String.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
         throw new IllegalStateException(e);
      } catch (NoClassDefFoundError e) {
         supportsGameRuleClass = false;

         try {
            getGameRuleValue = methodHandles.findVirtual(World.class, "getGameRuleValue", MethodType.methodType(String.class, String.class));
            setGameRuleValue = methodHandles.findVirtual(World.class, "setGameRuleValue", MethodType.methodType(boolean.class, String.class, String.class));
         } catch (NoSuchMethodException | IllegalAccessException ex) {
            throw new IllegalStateException("Game rules are not supported by your server", ex);
         }
      }

      SUPPORTS_GameRule = supportsGameRuleClass;
      GameRule_getByName = getByName;
      World_getGameRuleValue = getGameRuleValue;
      World_setGameRuleValue = setGameRuleValue;
      COMMAND_BLOCK_OUTPUT = std(Boolean.class, "command_block_output", "commandBlockOutput");
      ADVANCE_TIME = std(Boolean.class, "advance_time", "doDaylightCycle");
      ENTITY_DROPS = std(Boolean.class, "entity_drops", "doEntityDrops");
      MOB_DROPS = std(Boolean.class, "mob_drops", "doMobLoot");
      SPAWN_MOBS = std(Boolean.class, "spawn_mobs", "doMobSpawning");
      BLOCK_DROPS = std(Boolean.class, "block_drops", "doTileDrops");
      KEEP_INVENTORY = std(Boolean.class, "keep_inventory", "keepInventory");
      LOG_ADMIN_COMMANDS = std(Boolean.class, "log_admin_commands", "logAdminCommands");
      MOB_GRIEFING = std(Boolean.class, "mob_griefing", "mobGriefing");
      NATURAL_HEALTH_REGENERATION = std(Boolean.class, "natural_health_regeneration", "naturalRegeneration");
      REDUCED_DEBUG_INFO = std(Boolean.class, "reduced_debug_info", "reducedDebugInfo");
      SEND_COMMAND_FEEDBACK = std(Boolean.class, "send_command_feedback", "sendCommandFeedback");
      SHOW_DEATH_MESSAGES = std(Boolean.class, "show_death_messages", "showDeathMessages");
      RANDOM_TICK_SPEED = std(Integer.class, "random_tick_speed", "randomTickSpeed");
      SPECTATORS_GENERATE_CHUNKS = std(Boolean.class, "spectators_generate_chunks", "spectatorsGenerateChunks");
      ELYTRA_MOVEMENT_CHECK = std(Boolean.class, "elytra_movement_check", "disableElytraMovementCheck");
      RESPAWN_RADIUS = std(Integer.class, "respawn_radius", "spawnRadius");
      ADVANCE_WEATHER = std(Boolean.class, "advance_weather", "doWeatherCycle");
      MAX_ENTITY_CRAMMING = std(Integer.class, "max_entity_cramming", "maxEntityCramming");
      LIMITED_CRAFTING = std(Boolean.class, "limited_crafting", "doLimitedCrafting");
      SHOW_ADVANCEMENT_MESSAGES = std(Boolean.class, "show_advancement_messages", "announceAdvancements");
      MAX_COMMAND_SEQUENCE_LENGTH = std(Integer.class, "max_command_sequence_length", "maxCommandChainLength");
      GAME_LOOP_FUNCTION = std(String.class, "gameLoopFunction");
      RAIDS = std(Boolean.class, "raids", "disableRaids");
      SPAWN_PHANTOMS = std(Boolean.class, "spawn_phantoms", "doInsomnia");
      IMMEDIATE_RESPAWN = std(Boolean.class, "immediate_respawn", "doImmediateRespawn");
      DROWNING_DAMAGE = std(Boolean.class, "drowning_damage", "drowningDamage");
      FALL_DAMAGE = std(Boolean.class, "fall_damage", "fallDamage");
      FIRE_DAMAGE = std(Boolean.class, "fire_damage", "fireDamage");
      SPAWN_PATROLS = std(Boolean.class, "spawn_patrols", "doPatrolSpawning");
      SPAWN_WANDERING_TRADERS = std(Boolean.class, "spawn_wandering_traders", "doTraderSpawning");
      FORGIVE_DEAD_PLAYERS = std(Boolean.class, "forgive_dead_players", "forgiveDeadPlayers");
      UNIVERSAL_ANGER = std(Boolean.class, "universal_anger", "universalAnger");
      FREEZE_DAMAGE = std(Boolean.class, "freeze_damage", "freezeDamage");
      PLAYERS_SLEEPING_PERCENTAGE = std(Integer.class, "players_sleeping_percentage", "playersSleepingPercentage");
      SPAWN_WARDENS = std(Boolean.class, "spawn_wardens", "doWardenSpawning");
      BLOCK_EXPLOSION_DROP_DECAY = std(Boolean.class, "block_explosion_drop_decay", "blockExplosionDropDecay");
      MOB_EXPLOSION_DROP_DECAY = std(Boolean.class, "mob_explosion_drop_decay", "mobExplosionDropDecay");
      TNT_EXPLOSION_DROP_DECAY = std(Boolean.class, "tnt_explosion_drop_decay", "tntExplosionDropDecay");
      WATER_SOURCE_CONVERSION = std(Boolean.class, "water_source_conversion", "waterSourceConversion");
      LAVA_SOURCE_CONVERSION = std(Boolean.class, "lava_source_conversion", "lavaSourceConversion");
      GLOBAL_SOUND_EVENTS = std(Boolean.class, "global_sound_events", "globalSoundEvents");
      MAX_SNOW_ACCUMULATION_HEIGHT = std(Integer.class, "max_snow_accumulation_height", "snowAccumulationHeight");
      SPREAD_VINES = std(Boolean.class, "spread_vines", "doVinesSpread");
      MAX_BLOCK_MODIFICATIONS = std(Integer.class, "max_block_modifications", "commandModificationBlockLimit");
      ENDER_PEARLS_VANISH_ON_DEATH = std(Boolean.class, "ender_pearls_vanish_on_death", "enderPearlsVanishOnDeath");
      PROJECTILES_CAN_BREAK_BLOCKS = std(Boolean.class, "projectiles_can_break_blocks", "projectilesCanBreakBlocks");
      MAX_COMMAND_FORKS = std(Integer.class, "max_command_forks", "maxCommandForkCount");
      PLAYERS_NETHER_PORTAL_DEFAULT_DELAY = std(Integer.class, "players_nether_portal_default_delay", "playersNetherPortalDefaultDelay");
      PLAYERS_NETHER_PORTAL_CREATIVE_DELAY = std(Integer.class, "players_nether_portal_creative_delay", "playersNetherPortalCreativeDelay");
      SPAWN_CHUNK_RADIUS = std(Integer.class, "spawnChunkRadius");
      PLAYER_MOVEMENT_CHECK = std(Boolean.class, "player_movement_check", "disablePlayerMovementCheck");
      MAX_MINECART_SPEED = std(Integer.class, "max_minecart_speed", "minecartMaxSpeed");
      TNT_EXPLODES = std(Boolean.class, "tnt_explodes", "tntExplodes");
      ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER = std(Boolean.class, "allowFireTicksAwayFromPlayer");
      LOCATOR_BAR = std(Boolean.class, "locator_bar", "locatorBar");
      PVP = std(Boolean.class, "pvp");
      ALLOW_ENTERING_NETHER_USING_PORTALS = std(Boolean.class, "allow_entering_nether_using_portals", "allowEnteringNetherUsingPortals");
      SPAWN_MONSTERS = std(Boolean.class, "spawn_monsters", "spawnMonsters");
      COMMAND_BLOCKS_WORK = std(Boolean.class, "command_blocks_work", "commandBlocksEnabled");
      SPAWNER_BLOCKS_WORK = std(Boolean.class, "spawner_blocks_work", "spawnerBlocksEnabled");
      FIRE_SPREAD_RADIUS_AROUND_PLAYER = std(Integer.class, "fire_spread_radius_around_player");
      DO_FIRE_TICK = std(Boolean.class, "doFireTick");
   }

   private static final class Data {
      private static final boolean SUPPORTS_Registry_GAME_RULE;
      private static final Map<String, XGameRule<?>> NAME_MAPPING = new HashMap<>();
      private static final Map<GameRule<?>, XGameRule<?>> OBJECT_MAPPING = new HashMap<>();

      private Data() {
      }

      @Nullable
      private static GameRule<?> getGameRule(String name) {
         return GameRule.getByName(name);
      }

      static {
         SUPPORTS_Registry_GAME_RULE = false;
      }
   }
}
