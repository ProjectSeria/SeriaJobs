package id.seria.gacha.lib.xseries;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.StaticClassHandle;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.TreeSpecies;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Cat;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.GlowSquid;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.Husk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Mob;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Spellcaster;
import org.bukkit.entity.Strider;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Axolotl.Variant;
import org.bukkit.entity.Cat.Type;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.entity.Llama.Color;
import org.bukkit.entity.Panda.Gene;
import org.bukkit.entity.Spellcaster.Spell;
import org.bukkit.entity.TropicalFish.Pattern;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class XEntity {
   public static final Set<EntityType> UNDEAD;
   private static final boolean SUPPORTS_DELAYED_SPAWN;
   private static final MethodHandle DELAYED_SPAWN_1_17;
   private static final MethodHandle DELAYED_SPAWN_1_16_5;
   private static final MethodHandle DELAYED_SPAWN_1_11;
   private static final Object REGISTRY_CAT_VARIANT = supportsRegistry("CAT_VARIANT");
   private static Object REGISTRY_DEFAULT_CAT_VARIANT;
   private static final Map<Class<?>, BiConsumer<Entity, ConfigurationSection>> MAPPING;
   private static final boolean SUPPORTS_Villager_setVillagerLevel;
   private static final boolean SUPPORTS_Villager_setVillagerExperience;
   private static final boolean SUPPORTS_Villager_setVillagerType;

   private static <T extends Entity> void register(Class<T> entityType, BiConsumer<T, ConfigurationSection> handler) {
      MAPPING.put(entityType, cast(handler));
   }

   private static void mapObjectToConfig(Class<? extends Entity> entityClass) {
      List<XEntity.MappedConfigObject> mappedConfigObjects = new ArrayList<>();
      Lookup lookup = MethodHandles.lookup();

      for (Method method : entityClass.getDeclaredMethods()) {
         String name = method.getName();
         if (name.startsWith("set")) {
            String configEntry = name.substring(3).replaceAll("[A-Z]", "-");
            if (configEntry.startsWith("-")) {
               configEntry = name.charAt(3) + configEntry.substring(1);
            }

            MethodHandle setter;
            try {
               setter = lookup.unreflect(method);
            } catch (IllegalAccessException e) {
               throw new IllegalStateException(e);
            }

            mappedConfigObjects.add(new XEntity.MappedConfigObject(configEntry, setter, null));
         }
      }
   }

   private XEntity() {
   }

   private static Object supportsRegistry(String name) {
      try {
         Class<?> registryClass = XReflection.ofMinecraft().inPackage("org.bukkit").named("Registry").reflect();
         return (Object)XReflection.of(registryClass).field().asStatic().getter().named(name).returns(registryClass).reflect().invoke();
      } catch (Throwable ex) {
         return null;
      }
   }

   private static <T> T getRegistryOrEnum(Class<T> typeClass, Object registry, String name, Object defaultValue) {
      if (Strings.isNullOrEmpty(name)) {
         return (T)defaultValue;
      }

      T type;
      if (registry != null) {
         type = cast(((Registry)registry).get(fromConfig(name)));
      } else {
         type = cast(Enums.getIfPresent(cast(typeClass), name.toUpperCase(Locale.ENGLISH)).orNull());
      }

      return (T)(type == null ? defaultValue : type);
   }

   private static <T> T cast(Object something) {
      return (T)something;
   }

   private static NamespacedKey fromConfig(String name) {
      NamespacedKey namespacedKey;
      if (!name.contains(":")) {
         namespacedKey = NamespacedKey.minecraft(name.toLowerCase(Locale.ENGLISH));
      } else {
         namespacedKey = NamespacedKey.fromString(name.toLowerCase(Locale.ENGLISH));
      }

      return Objects.requireNonNull(namespacedKey, () -> "Invalid namespace key: " + name);
   }

   @NotNull
   private static Type getCatVariant(@Nullable String name) {
      if (REGISTRY_DEFAULT_CAT_VARIANT == null) {
         REGISTRY_DEFAULT_CAT_VARIANT = getRegistryOrEnum(Type.class, REGISTRY_CAT_VARIANT, "TABBY", null);
      }

      return getRegistryOrEnum(Type.class, REGISTRY_CAT_VARIANT, name, REGISTRY_DEFAULT_CAT_VARIANT);
   }

   public static boolean isUndead(@Nullable EntityType type) {
      return type != null && UNDEAD.contains(type);
   }

   @Nullable
   public static Entity spawn(@NotNull Location location, @NotNull ConfigurationSection config) {
      Objects.requireNonNull(location, "Cannot spawn entity at a null location.");
      Objects.requireNonNull(config, "Cannot spawn entity from a null configuration section");
      String typeStr = config.getString("type");
      if (typeStr == null) {
         return null;
      }

      Optional<XEntityType> type = XEntityType.of(typeStr);
      if (!type.isPresent()) {
         return null;
      }

      XEntityType finalType = type.get().or(XEntityType.ZOMBIE);
      return !finalType.isSupported() ? null : spawn(location, finalType.get().getEntityClass(), false, entity -> edit(entity, config));
   }

   @NotNull
   public static <T extends Entity> T spawn(@NotNull Location location, @NotNull Class<T> clazz, boolean randomizeData, @Nullable Consumer<? super T> function) {
      if (SUPPORTS_DELAYED_SPAWN) {
         return (T)location.getWorld().spawn(location, clazz, randomizeData, function);
      }

      if (DELAYED_SPAWN_1_17 != null) {
         try {
            return (T)(Entity)DELAYED_SPAWN_1_17.invoke(
               (World)location.getWorld(), (Location)location, (Class)clazz, (boolean)randomizeData, (org.bukkit.util.Consumer)toBukkitConsumer(function)
            );
         } catch (Throwable t) {
            throw new RuntimeException(t);
         }
      } else if (DELAYED_SPAWN_1_11 != null) {
         try {
            return (T)(Entity)DELAYED_SPAWN_1_11.invoke(
               (World)location.getWorld(), (Location)location, (Class)clazz, (org.bukkit.util.Consumer)toBukkitConsumer(function)
            );
         } catch (Throwable t) {
            throw new RuntimeException(t);
         }
      } else {
         T entity = (T)location.getWorld().spawn(location, clazz);
         if (function != null) {
            function.accept(entity);
         }

         return entity;
      }
   }

   @NotNull
   public static <T extends LivingEntity> T spawn(
      @NotNull Location location, @NotNull Class<T> clazz, @NotNull SpawnReason spawnReason, boolean randomizeData, @Nullable Consumer<? super T> function
   ) {
      if (SUPPORTS_DELAYED_SPAWN) {
         return (T)location.getWorld().spawn(location, clazz, spawnReason, randomizeData, function);
      }

      if (!randomizeData && DELAYED_SPAWN_1_17 != null) {
         try {
            return (T)(LivingEntity)DELAYED_SPAWN_1_17.invoke(
               (World)location.getWorld(), (Location)location, (Class)clazz, (boolean)randomizeData, (org.bukkit.util.Consumer)toBukkitConsumer(function)
            );
         } catch (Throwable t) {
            throw new RuntimeException(t);
         }
      } else if (DELAYED_SPAWN_1_16_5 != null) {
         try {
            return (T)(LivingEntity)DELAYED_SPAWN_1_16_5.invoke(
               (World)location.getWorld(), (Location)location, (Class)clazz, (SpawnReason)spawnReason, (org.bukkit.util.Consumer)toBukkitConsumer(function)
            );
         } catch (Throwable t) {
            throw new RuntimeException(t);
         }
      } else if (DELAYED_SPAWN_1_11 != null) {
         try {
            return (T)(LivingEntity)DELAYED_SPAWN_1_11.invoke(
               (World)location.getWorld(), (Location)location, (Class)clazz, (org.bukkit.util.Consumer)toBukkitConsumer(function)
            );
         } catch (Throwable t) {
            throw new RuntimeException(t);
         }
      } else {
         T entity = (T)location.getWorld().spawn(location, clazz);
         if (function != null) {
            function.accept(entity);
         }

         return entity;
      }
   }

   @Nullable
   @Contract("!null -> !null")
   private static <T> Consumer<T> toBukkitConsumer(@Nullable final Consumer<T> consumer) {
      return consumer == null ? null : new org.bukkit.util.Consumer<T>() {
         public void accept(T t) {
            consumer.accept(t);
         }
      };
   }

   private static ItemStack deserialize(ConfigurationSection config) {
      return XItemStack.deserializer().fromConfig(config).deserialize();
   }

   private static void map(Class<?> target, Entity entity, ConfigurationSection config) {
      if (target != Entity.class) {
         BiConsumer<Entity, ConfigurationSection> mapping = MAPPING.get(target);
         if (mapping != null) {
            mapping.accept(entity, config);
         }

         Class<?> superclass = target.getSuperclass();
         if (superclass != null) {
            map(superclass, entity, config);
         }

         for (Class<?> interf : target.getInterfaces()) {
            map(interf, entity, config);
         }
      }
   }

   @NotNull
   public static Entity edit(@NotNull Entity entity, @NotNull ConfigurationSection config) {
      Objects.requireNonNull(entity, "Cannot edit properties of a null entity");
      Objects.requireNonNull(config, "Cannot edit an entity from a null configuration section");
      String name = config.getString("name");
      if (name != null) {
         entity.setCustomName(ChatColor.translateAlternateColorCodes('&', name));
         entity.setCustomNameVisible(true);
      }

      if (config.isSet("glowing")) {
         entity.setGlowing(config.getBoolean("glowing"));
      }

      if (config.isSet("gravity")) {
         entity.setGravity(config.getBoolean("gravity"));
      }

      if (config.isSet("silent")) {
         entity.setSilent(config.getBoolean("silent"));
      }

      entity.setFireTicks(config.getInt("fire-ticks"));
      entity.setFallDistance(config.getInt("fall-distance"));
      if (config.isSet("invulnerable")) {
         entity.setInvulnerable(config.getBoolean("invulnerable"));
      }

      int live = config.getInt("ticks-lived");
      if (live > 0) {
         entity.setTicksLived(live);
      }

      if (config.isSet("portal-cooldown")) {
         entity.setPortalCooldown(config.getInt("portal-cooldown", -1));
      }

      if (XReflection.supports(13) && entity instanceof Boss) {
         Boss boss = (Boss)entity;
         ConfigurationSection bossBarSection = config.getConfigurationSection("bossbar");
         if (bossBarSection != null) {
            BossBar bossBar = boss.getBossBar();
            editBossBar(bossBar, bossBarSection);
         }
      }

      if (entity instanceof Vehicle && entity instanceof Boat) {
         Boat boat = (Boat)entity;
         String speciesName = config.getString("tree-species");
         if (speciesName != null) {
            com.google.common.base.Optional<TreeSpecies> species = Enums.getIfPresent(TreeSpecies.class, speciesName);
            if (species.isPresent()) {
               boat.setWoodType((TreeSpecies)species.get());
            }
         }
      }

      if (entity instanceof LivingEntity) {
         LivingEntity living = (LivingEntity)entity;
         if (config.isSet("health")) {
            double hp = config.getDouble("health");
            living.getAttribute(XAttribute.MAX_HEALTH.get()).setBaseValue(hp);
            living.setHealth(hp);
         }

         if (XReflection.supports(14)) {
            living.setAbsorptionAmount(config.getInt("absorption"));
         }

         if (config.isSet("AI")) {
            living.setAI(config.getBoolean("AI"));
         }

         if (config.isSet("can-pickup-items")) {
            living.setCanPickupItems(config.getBoolean("can-pickup-items"));
         }

         if (config.isSet("collidable")) {
            living.setCollidable(config.getBoolean("collidable"));
         }

         if (config.isSet("gliding")) {
            living.setGliding(config.getBoolean("gliding"));
         }

         if (config.isSet("remove-when-far-away")) {
            living.setRemoveWhenFarAway(config.getBoolean("remove-when-far-away"));
         }

         if (XReflection.supports(13) && config.isSet("swimming")) {
            living.setSwimming(config.getBoolean("swimming"));
         }

         if (config.isSet("max-air")) {
            living.setMaximumAir(config.getInt("max-air"));
         }

         if (config.isSet("no-damage-ticks")) {
            living.setNoDamageTicks(config.getInt("no-damage-ticks"));
         }

         if (config.isSet("remaining-air")) {
            living.setRemainingAir(config.getInt("remaining-air"));
         }

         XPotion.addEffects(living, config.getStringList("effects"));
         ConfigurationSection equip = config.getConfigurationSection("equipment");
         if (equip != null) {
            EntityEquipment equipment = living.getEquipment();
            boolean isMob = entity instanceof Mob;
            ConfigurationSection helmet = equip.getConfigurationSection("helmet");
            if (helmet != null) {
               equipment.setHelmet(deserialize(helmet.getConfigurationSection("item")));
               if (isMob) {
                  equipment.setHelmetDropChance(helmet.getInt("drop-chance"));
               }
            }

            ConfigurationSection chestplate = equip.getConfigurationSection("chestplate");
            if (chestplate != null) {
               equipment.setChestplate(deserialize(chestplate.getConfigurationSection("item")));
               if (isMob) {
                  equipment.setChestplateDropChance(chestplate.getInt("drop-chance"));
               }
            }

            ConfigurationSection leggings = equip.getConfigurationSection("leggings");
            if (leggings != null) {
               equipment.setLeggings(deserialize(leggings.getConfigurationSection("item")));
               if (isMob) {
                  equipment.setLeggingsDropChance(leggings.getInt("drop-chance"));
               }
            }

            ConfigurationSection boots = equip.getConfigurationSection("boots");
            if (boots != null) {
               equipment.setBoots(deserialize(boots.getConfigurationSection("item")));
               if (isMob) {
                  equipment.setBootsDropChance(boots.getInt("drop-chance"));
               }
            }

            ConfigurationSection mainHand = equip.getConfigurationSection("main-hand");
            if (mainHand != null) {
               equipment.setItemInMainHand(deserialize(mainHand.getConfigurationSection("item")));
               if (isMob) {
                  equipment.setItemInMainHandDropChance(mainHand.getInt("drop-chance"));
               }
            }

            ConfigurationSection offHand = equip.getConfigurationSection("off-hand");
            if (offHand != null) {
               equipment.setItemInOffHand(deserialize(offHand.getConfigurationSection("item")));
               if (isMob) {
                  equipment.setItemInOffHandDropChance(offHand.getInt("drop-chance"));
               }
            }
         }

         if (living instanceof Ageable) {
            Ageable ageable = (Ageable)living;
            if (config.isSet("breed")) {
               ageable.setBreed(config.getBoolean("breed"));
            }

            if (config.isSet("baby")) {
               if (config.getBoolean("baby")) {
                  ageable.setBaby();
               } else {
                  ageable.setAdult();
               }
            }

            int age = config.getInt("age", 0);
            if (age > 0) {
               ageable.setAge(age);
            }

            if (config.isSet("age-lock")) {
               ageable.setAgeLock(config.getBoolean("age-lock"));
            }

            if (living instanceof Animals) {
               Animals animals = (Animals)living;
               int loveModeTicks = config.getInt("love-mode");
               if (loveModeTicks != 0) {
                  animals.setLoveModeTicks(loveModeTicks);
               }

               if (living instanceof Tameable) {
                  Tameable tam = (Tameable)living;
                  tam.setTamed(config.getBoolean("tamed"));
               }
            }
         }

         if (living instanceof Sittable) {
            Sittable sit = (Sittable)living;
            sit.setSitting(config.getBoolean("sitting"));
         }

         if (living instanceof Spellcaster) {
            Spellcaster caster = (Spellcaster)living;
            String spell = config.getString("spell");
            if (spell != null) {
               caster.setSpell((Spell)Enums.getIfPresent(Spell.class, spell).or(Spell.NONE));
            }
         }

         if (living instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse)living;
            if (config.isSet("domestication")) {
               horse.setDomestication(config.getInt("domestication"));
            }

            if (config.isSet("jump-strength")) {
               horse.setJumpStrength(config.getDouble("jump-strength"));
            }

            if (config.isSet("max-domestication")) {
               horse.setMaxDomestication(config.getInt("max-domestication"));
            }

            ConfigurationSection items = config.getConfigurationSection("items");
            if (items != null) {
               Inventory inventory = horse.getInventory();

               for (String key : items.getKeys(false)) {
                  ConfigurationSection itemSec = items.getConfigurationSection(key);
                  int slot = itemSec.getInt("slot", -1);
                  if (slot != -1) {
                     ItemStack item = deserialize(itemSec);
                     if (item != null) {
                        inventory.setItem(slot, item);
                     }
                  }
               }
            }

            if (living instanceof ChestedHorse) {
               ChestedHorse chested = (ChestedHorse)living;
               boolean hasChest = config.getBoolean("has-chest");
               if (hasChest) {
                  chested.setCarryingChest(true);
               }
            }
         }

         map(entity.getClass(), entity, config);
         if (living instanceof Villager) {
            Villager villager = (Villager)living;
            if (SUPPORTS_Villager_setVillagerLevel) {
               villager.setVillagerLevel(config.getInt("level"));
            }

            if (SUPPORTS_Villager_setVillagerExperience) {
               villager.setVillagerExperience(config.getInt("xp"));
            }
         } else if (living instanceof Enderman) {
            Enderman enderman = (Enderman)living;
            String block = config.getString("carrying");
            if (block != null) {
               Optional<XMaterial> mat = XMaterial.matchXMaterial(block);
               if (mat.isPresent()) {
                  ItemStack item = mat.get().parseItem();
                  if (item != null) {
                     enderman.setCarriedMaterial(item.getData());
                  }
               }
            }
         } else if (living instanceof Sheep) {
            Sheep sheep = (Sheep)living;
            boolean sheared = config.getBoolean("sheared");
            if (sheared) {
               sheep.setSheared(true);
            }
         } else if (living instanceof Rabbit) {
            Rabbit rabbit = (Rabbit)living;
            rabbit.setRabbitType(
               (org.bukkit.entity.Rabbit.Type)Enums.getIfPresent(org.bukkit.entity.Rabbit.Type.class, config.getString("color"))
                  .or(org.bukkit.entity.Rabbit.Type.WHITE)
            );
         } else if (living instanceof Bat) {
            Bat bat = (Bat)living;
            if (!config.getBoolean("awake")) {
               bat.setAwake(false);
            }
         } else if (living instanceof Wolf) {
            Wolf wolf = (Wolf)living;
            wolf.setAngry(config.getBoolean("angry"));
            wolf.setCollarColor((DyeColor)Enums.getIfPresent(DyeColor.class, config.getString("color")).or(DyeColor.GREEN));
         } else if (living instanceof Creeper) {
            Creeper creeper = (Creeper)living;
            creeper.setExplosionRadius(config.getInt("explosion-radius"));
            creeper.setMaxFuseTicks(config.getInt("max-fuse-ticks"));
            creeper.setPowered(config.getBoolean("powered"));
         } else if (XReflection.supports(10) && XReflection.supports(11)) {
            if (living instanceof Llama) {
               Llama llama = (Llama)living;
               if (config.isSet("strength")) {
                  llama.setStrength(config.getInt("strength"));
               }

               com.google.common.base.Optional<Color> color = Enums.getIfPresent(Color.class, config.getString("color"));
               if (color.isPresent()) {
                  llama.setColor((Color)color.get());
               }
            } else if (XReflection.supports(12)) {
               if (living instanceof Parrot) {
                  Parrot parrot = (Parrot)living;
                  parrot.setVariant(
                     (org.bukkit.entity.Parrot.Variant)Enums.getIfPresent(org.bukkit.entity.Parrot.Variant.class, config.getString("color"))
                        .or(org.bukkit.entity.Parrot.Variant.RED)
                  );
               }

               if (XReflection.supports(13)) {
                  thirteen(entity, config);
               }

               if (XReflection.supports(14)) {
                  fourteen(entity, config);
               }

               if (XReflection.supports(15)) {
                  fifteen(entity, config);
               }

               if (XReflection.supports(16)) {
                  sixteen(entity, config);
               }

               if (XReflection.supports(17)) {
                  seventeen(entity, config);
               }
            }
         }
      } else if (entity instanceof EnderSignal) {
         EnderSignal signal = (EnderSignal)entity;
         signal.setDespawnTimer(config.getInt("despawn-timer"));
         signal.setDropItem(config.getBoolean("drop-item"));
      } else if (entity instanceof ExperienceOrb) {
         ExperienceOrb orb = (ExperienceOrb)entity;
         orb.setExperience(config.getInt("exp"));
      } else if (entity instanceof Explosive) {
         Explosive explosive = (Explosive)entity;
         explosive.setYield((float)config.getDouble("yield"));
         explosive.setIsIncendiary(config.getBoolean("incendiary"));
      } else if (entity instanceof EnderCrystal) {
         EnderCrystal crystal = (EnderCrystal)entity;
         crystal.setShowingBottom(config.getBoolean("show-bottom"));
      }

      return entity;
   }

   private static void fourteen(Entity entity, ConfigurationSection config) {
      if (entity instanceof Raider) {
         Raider raider = (Raider)entity;
         if (config.isSet("can-join-raid")) {
            raider.setCanJoinRaid(config.getBoolean("can-join-raid"));
         }

         if (config.isSet("is-patrol-leader")) {
            raider.setCanJoinRaid(config.getBoolean("is-patrol-leader"));
         }
      } else if (entity instanceof Cat) {
         Cat cat = (Cat)entity;
         cat.setCatType(getCatVariant(config.getString("variant")));
         cat.setCollarColor((DyeColor)Enums.getIfPresent(DyeColor.class, config.getString("color")).or(DyeColor.GREEN));
      } else if (entity instanceof Fox) {
         Fox fox = (Fox)entity;
         fox.setCrouching(config.getBoolean("crouching"));
         fox.setSleeping(config.getBoolean("sleeping"));
         fox.setFoxType(
            (org.bukkit.entity.Fox.Type)Enums.getIfPresent(org.bukkit.entity.Fox.Type.class, config.getString("color")).or(org.bukkit.entity.Fox.Type.RED)
         );
      } else if (entity instanceof Panda) {
         Panda panda = (Panda)entity;
         panda.setHiddenGene((Gene)Enums.getIfPresent(Gene.class, config.getString("hidden-gene")).or(Gene.PLAYFUL));
         panda.setMainGene((Gene)Enums.getIfPresent(Gene.class, config.getString("main-gene")).or(Gene.NORMAL));
      } else if (entity instanceof MushroomCow) {
         MushroomCow mooshroom = (MushroomCow)entity;
         mooshroom.setVariant(
            (org.bukkit.entity.MushroomCow.Variant)Enums.getIfPresent(org.bukkit.entity.MushroomCow.Variant.class, config.getString("color"))
               .or(org.bukkit.entity.MushroomCow.Variant.RED)
         );
      }
   }

   private static void thirteen(Entity entity, ConfigurationSection config) {
      if (entity instanceof Husk) {
         Husk husk = (Husk)entity;
         husk.setConversionTime(config.getInt("conversion-time"));
      } else if (entity instanceof Vex) {
         Vex vex = (Vex)entity;
         vex.setCharging(config.getBoolean("charging"));
      } else if (entity instanceof PufferFish) {
         PufferFish pufferFish = (PufferFish)entity;
         pufferFish.setPuffState(config.getInt("puff-state"));
      } else if (entity instanceof TropicalFish) {
         TropicalFish tropicalFish = (TropicalFish)entity;
         tropicalFish.setBodyColor((DyeColor)Enums.getIfPresent(DyeColor.class, config.getString("color")).or(DyeColor.WHITE));
         tropicalFish.setPattern((Pattern)Enums.getIfPresent(Pattern.class, config.getString("pattern")).or(Pattern.BETTY));
         tropicalFish.setPatternColor((DyeColor)Enums.getIfPresent(DyeColor.class, config.getString("pattern-color")).or(DyeColor.WHITE));
      } else if (entity instanceof EnderDragon) {
         EnderDragon dragon = (EnderDragon)entity;
         dragon.setPhase((Phase)Enums.getIfPresent(Phase.class, config.getString("phase")).or(Phase.ROAR_BEFORE_ATTACK));
      } else if (entity instanceof Phantom) {
         Phantom phantom = (Phantom)entity;
         phantom.setSize(config.getInt("size"));
      }
   }

   private static void fifteen(Entity entity, ConfigurationSection config) {
      if (entity instanceof Bee) {
         Bee bee = (Bee)entity;
         bee.setAnger(config.getInt("anger") * 20);
         bee.setHasNectar(config.getBoolean("nectar"));
         bee.setHasStung(config.getBoolean("stung"));
         bee.setCannotEnterHiveTicks(config.getInt("disallow-hive") * 20);
      }
   }

   private static void sixteen(Entity entity, ConfigurationSection config) {
      if (entity instanceof Hoglin) {
         Hoglin hoglin = (Hoglin)entity;
         hoglin.setConversionTime(config.getInt("conversation") * 20);
         hoglin.setImmuneToZombification(config.getBoolean("zombification-immunity"));
         hoglin.setIsAbleToBeHunted(config.getBoolean("can-be-hunted"));
      } else if (entity instanceof Piglin) {
         Piglin piglin = (Piglin)entity;
         piglin.setConversionTime(config.getInt("conversation") * 20);
         piglin.setImmuneToZombification(config.getBoolean("zombification-immunity"));
      } else if (entity instanceof Strider) {
         Strider strider = (Strider)entity;
         strider.setShivering(config.getBoolean("shivering"));
      }
   }

   private static void frog(Entity entity, ConfigurationSection config) {
      Frog frog = (Frog)entity;
      frog.setVariant((org.bukkit.entity.Frog.Variant)Registry.FROG_VARIANT.get(fromConfig(config.getString("variant"))));
   }

   private static boolean seventeen(Entity entity, ConfigurationSection config) {
      if (entity instanceof Axolotl) {
         Axolotl axolotl = (Axolotl)entity;
         String variantStr = config.getString("variant");
         if (Strings.isNullOrEmpty(variantStr)) {
            com.google.common.base.Optional<Variant> variant = Enums.getIfPresent(Variant.class, variantStr);
            if (variant.isPresent()) {
               axolotl.setVariant((Variant)variant.get());
            }
         }

         if (config.isSet("playing-dead")) {
            axolotl.setPlayingDead(config.getBoolean("playing-dead"));
         }

         return true;
      } else if (entity instanceof Goat) {
         Goat goat = (Goat)entity;
         if (config.isSet("screaming")) {
            goat.setScreaming(config.getBoolean("screaming"));
         }

         return true;
      } else if (entity instanceof GlowSquid) {
         GlowSquid glowSquid = (GlowSquid)entity;
         if (config.isSet("dark-ticks-remaining")) {
            glowSquid.setDarkTicksRemaining(config.getInt("dark-ticks-remaining"));
         }

         return true;
      } else {
         return false;
      }
   }

   public static void editBossBar(BossBar bossBar, ConfigurationSection section) {
      String title = section.getString("title");
      if (title != null) {
         bossBar.setTitle(ChatColor.translateAlternateColorCodes('&', title));
      }

      String colorStr = section.getString("color");
      if (colorStr != null) {
         com.google.common.base.Optional<BarColor> color = Enums.getIfPresent(BarColor.class, colorStr.toUpperCase(Locale.ENGLISH));
         if (color.isPresent()) {
            bossBar.setColor((BarColor)color.get());
         }
      }

      String styleStr = section.getString("style");
      if (styleStr != null) {
         com.google.common.base.Optional<BarStyle> style = Enums.getIfPresent(BarStyle.class, styleStr.toUpperCase(Locale.ENGLISH));
         if (style.isPresent()) {
            bossBar.setStyle((BarStyle)style.get());
         }
      }

      List<String> flagList = section.getStringList("flags");
      if (!flagList.isEmpty()) {
         Set<BarFlag> flags = EnumSet.noneOf(BarFlag.class);

         for (String flagName : flagList) {
            BarFlag flag = (BarFlag)Enums.getIfPresent(BarFlag.class, flagName.toUpperCase(Locale.ENGLISH)).orNull();
            if (flag != null) {
               flags.add(flag);
            }
         }

         for (BarFlag flag : BarFlag.values()) {
            if (flags.contains(flag)) {
               bossBar.addFlag(flag);
            } else {
               bossBar.removeFlag(flag);
            }
         }
      }
   }

   static {
      MethodHandle delayedSpawn1_17 = null;
      MethodHandle delayedSpawn1_16_5 = null;
      MethodHandle delayedSpawn1_11 = null;

      boolean supportsDelayedSpawn;
      try {
         World.class.getMethod("spawn", Location.class, Class.class, boolean.class, Consumer.class);
         supportsDelayedSpawn = true;
      } catch (NoSuchMethodException ex) {
         supportsDelayedSpawn = false;
      }

      Lookup lookup = MethodHandles.lookup();

      try {
         delayedSpawn1_17 = lookup.unreflect(World.class.getMethod("spawn", Location.class, Class.class, boolean.class, org.bukkit.util.Consumer.class));
      } catch (Throwable var8) {
      }

      try {
         delayedSpawn1_16_5 = lookup.unreflect(World.class.getMethod("spawn", Location.class, Class.class, SpawnReason.class, org.bukkit.util.Consumer.class));
      } catch (Throwable var7) {
      }

      try {
         delayedSpawn1_11 = lookup.unreflect(World.class.getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class));
      } catch (Throwable var6) {
      }

      SUPPORTS_DELAYED_SPAWN = supportsDelayedSpawn;
      DELAYED_SPAWN_1_17 = delayedSpawn1_17;
      DELAYED_SPAWN_1_16_5 = delayedSpawn1_16_5;
      DELAYED_SPAWN_1_11 = delayedSpawn1_11;
      MAPPING = new HashMap<>(20);
      if (XReflection.supports(19)) {
         register(Frog.class, XEntity::frog);
      }

      StaticClassHandle villager = XReflection.of(Villager.class);
      SUPPORTS_Villager_setVillagerLevel = villager.method("void setVillagerLevel(int var1);").exists();
      SUPPORTS_Villager_setVillagerExperience = villager.method("void setVillagerExperience(int xp);").exists();
      SUPPORTS_Villager_setVillagerType = villager.method()
         .named("setVillagerType")
         .returns(void.class)
         .parameters(villager.inner(XReflection.ofMinecraft().named("Type")))
         .exists();
      Set<EntityType> undead = EnumSet.of(
         EntityType.SKELETON,
         EntityType.ZOMBIE,
         EntityType.GIANT,
         EntityType.ZOMBIE_VILLAGER,
         EntityType.WITHER,
         EntityType.WITHER_SKELETON,
         EntityType.ZOMBIE_HORSE
      );
      if (XReflection.supports(10)) {
         undead.add(EntityType.HUSK);
         undead.add(EntityType.STRAY);
         if (XReflection.supports(11)) {
            undead.add(EntityType.SKELETON_HORSE);
            if (XReflection.supports(13)) {
               undead.add(EntityType.DROWNED);
               undead.add(EntityType.PHANTOM);
               if (XReflection.supports(16)) {
                  undead.add(EntityType.ZOGLIN);
                  undead.add(EntityType.PIGLIN);
                  undead.add(EntityType.ZOMBIFIED_PIGLIN);
               }
            }
         }
      }

      if (!XReflection.supports(16)) {
         undead.add(EntityType.valueOf("PIG_ZOMBIE"));
      }

      UNDEAD = Collections.unmodifiableSet(undead);
   }

   private static final class MappedConfigObject {
      private final String configEntry;
      private final MethodHandle setter;
      private final Function<ConfigurationSection, Object> configurationValue;

      private MappedConfigObject(String configEntry, MethodHandle setter, Function<ConfigurationSection, Object> configurationValue) {
         this.configEntry = configEntry;
         this.setter = setter;
         this.configurationValue = configurationValue;
      }

      private void handle(Entity entity, ConfigurationSection config) {
         if (config.isSet(this.configEntry)) {
            try {
               this.setter.invoke((MethodHandle)this.setter, (Object)this.configurationValue.apply(config));
            } catch (Throwable e) {
               throw new IllegalStateException(e);
            }
         }
      }
   }
}
