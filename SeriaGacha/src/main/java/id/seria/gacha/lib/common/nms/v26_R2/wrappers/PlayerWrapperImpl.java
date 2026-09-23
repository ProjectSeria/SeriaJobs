package id.seria.gacha.lib.common.nms.v26_R2.wrappers;

import id.seria.gacha.lib.common.nms.interfaces.NMSFactory;
import id.seria.gacha.lib.common.nms.interfaces.particles.Particle;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.ParticleData;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.SculkChargeData;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.ShriekData;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.VibrationData;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.DustColorTransitionData;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.DustData;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.NoteColor;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.RegularColor;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.texture.BlockTexture;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.texture.ItemTexture;
import id.seria.gacha.lib.common.nms.interfaces.wrappers.PlayerWrapper;
import id.seria.gacha.lib.common.utils.version.ServerVersion;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Vibration;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Vibration.Destination;
import org.bukkit.Vibration.Destination.BlockDestination;
import org.bukkit.Vibration.Destination.EntityDestination;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerWrapperImpl implements PlayerWrapper {
   private static final Map<Particle, org.bukkit.Particle> LAZY_PARTICLE_MAPPER = new HashMap<>();
   private final NMSFactory factory;
   private final Player player;
   private static final HashMap<UUID, Long> totemCooldownMap = new HashMap<>();

   public PlayerWrapperImpl(NMSFactory factory, Player player) {
      this.factory = factory;
      this.player = player;
   }

   @Override
   public NMSFactory getNMSFactory() {
      return this.factory;
   }

   @Override
   public Player getBukkit() {
      return this.player;
   }

   @Override
   public int getTotemCooldownTicks() {
      UUID playerId = this.player.getUniqueId();
      if (totemCooldownMap.containsKey(playerId)) {
         long lastConsumeTime = totemCooldownMap.get(playerId);
         long currentTime = System.currentTimeMillis();
         long elapsedTime = currentTime - lastConsumeTime;
         if (elapsedTime < 500L) {
            return (int)Math.ceil((500L - elapsedTime) / 50.0);
         }
      }

      return 0;
   }

   @Override
   public boolean consumeTotemOfUndying() {
      UUID playerId = this.player.getUniqueId();
      if (totemCooldownMap.containsKey(playerId)) {
         long lastConsumeTime = totemCooldownMap.get(playerId);
         long currentTime = System.currentTimeMillis();
         long elapsedTime = currentTime - lastConsumeTime;
         if (elapsedTime < 500L) {
            return false;
         }
      }

      EquipmentSlot slot = null;
      ItemStack item = null;
      if (this.player.getInventory().getItemInMainHand() != null && this.player.getInventory().getItemInMainHand().getType().equals(Material.TOTEM_OF_UNDYING)) {
         slot = EquipmentSlot.HAND;
         item = this.player.getInventory().getItemInMainHand();
      } else if (this.player.getInventory().getItemInOffHand() != null
         && this.player.getInventory().getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING)) {
         slot = EquipmentSlot.OFF_HAND;
         item = this.player.getInventory().getItemInOffHand();
      }

      if (slot == null) {
         return false;
      }

      EntityResurrectEvent event = new EntityResurrectEvent(this.player);
      Bukkit.getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         item.setAmount(item.getAmount() - 1);
         if (slot == EquipmentSlot.HAND) {
            this.player.getInventory().setItemInMainHand(item.getAmount() <= 0 ? new ItemStack(Material.AIR) : item);
         } else if (slot == EquipmentSlot.OFF_HAND) {
            this.player.getInventory().setItemInOffHand(item.getAmount() <= 0 ? new ItemStack(Material.AIR) : item);
         }

         this.player.setHealth(1.0);

         for (PotionEffectType type : new PotionEffectType[]{PotionEffectType.REGENERATION, PotionEffectType.ABSORPTION, PotionEffectType.FIRE_RESISTANCE}) {
            this.player.removePotionEffect(type);
         }

         this.player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1));
         this.player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
         this.player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 1));
         this.player.playEffect(EntityEffect.TOTEM_RESURRECT);
      }

      return !event.isCancelled();
   }

   @Override
   public void setCooldown(Material material, int ticks) {
      this.player.setCooldown(material, ticks);
   }

   @Override
   public ItemStack getItemInMainHand() {
      return this.player.getInventory().getItemInMainHand();
   }

   @Override
   public boolean setItemInMainHand(ItemStack item) {
      this.player.getInventory().setItemInMainHand(item);
      return true;
   }

   @Override
   public ItemStack getItemInOffHand() {
      return this.player.getInventory().getItemInOffHand();
   }

   @Override
   public boolean setItemInOffHand(ItemStack item) {
      this.player.getInventory().setItemInOffHand(item);
      return true;
   }

   @Override
   public int getPing() {
      return this.player.getPing();
   }

   @Override
   public double getBaseAttributeValue(PlayerWrapper.Attribute attribute) {
      return this.player.getAttribute(org.bukkit.attribute.Attribute.valueOf(attribute.name())).getBaseValue();
   }

   @Override
   public boolean spawnParticle(
      @NonNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, ParticleData data
   ) {
      if (particle == null) {
         throw new NullPointerException("particle is marked non-null but is null");
      }

      org.bukkit.Particle bukkitParticle = this.asBukkit(particle);
      if (bukkitParticle == null) {
         return false;
      }

      Object bukkitData = this.asBukkit(data);
      if (bukkitData == null && !bukkitParticle.getDataType().equals(Void.class)) {
         bukkitData = this.createDefaultData(bukkitParticle);
         if (bukkitData == null) {
            return false;
         }
      }

      return this.spawnBukkitParticle(bukkitParticle, x, y, z, count, offsetX, offsetY, offsetZ, extra, bukkitData);
   }

   private boolean spawnBukkitParticle(
      org.bukkit.Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, Object data
   ) {
      if (data != null) {
         if (particle.equals(org.bukkit.Particle.NOTE)) {
            if (!(data instanceof Integer)) {
               throw new IllegalArgumentException("Data expected to be Integer");
            }

            int note = (Integer)data;
            double value = note == 0 ? 0.0 : note / 24.0;
            this.player.spawnParticle(org.bukkit.Particle.NOTE, x, y, z, 0, value, 0.0, 0.0, 1.0);
            return true;
         }

         if (!particle.getDataType().isInstance(data)) {
            return false;
         }
      }

      this.player.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, data);
      return true;
   }

   private org.bukkit.Particle asBukkit(Particle effect) {
      String effectName = effect.getFieldNameMapper().apply(ServerVersion.MINECRAFT_VERSION);
      if (effectName.equalsIgnoreCase("NONE")) {
         return null;
      }

      org.bukkit.Particle bukkit = LAZY_PARTICLE_MAPPER.get(effect);
      if (bukkit == null) {
         try {
            bukkit = org.bukkit.Particle.valueOf(effect.name());
            LAZY_PARTICLE_MAPPER.put(effect, bukkit);
         } catch (Exception ignored1) {
            try {
               bukkit = org.bukkit.Particle.valueOf(effectName);
               LAZY_PARTICLE_MAPPER.put(effect, bukkit);
            } catch (Exception ignored2) {
               return null;
            }
         }
      }

      return bukkit;
   }

   private Object asBukkit(ParticleData data) {
      if (data == null) {
         return null;
      }

      if (data instanceof VibrationData cast) {
         Destination destination;
         if (cast.getBlockDestination() != null) {
            destination = new BlockDestination(cast.getBlockDestination());
         } else {
            if (cast.getEntityDestination() == null) {
               return null;
            }

            destination = new EntityDestination(cast.getEntityDestination());
         }

         return new Vibration(cast.getStart(), destination, cast.getTicks());
      } else if (data instanceof ShriekData cast) {
         return cast.getDelay();
      } else if (data instanceof SculkChargeData cast) {
         return cast.getRoll();
      } else if (data instanceof BlockTexture cast) {
         return Bukkit.createBlockData(cast.getMaterial());
      } else if (data instanceof ItemTexture cast) {
         return cast.getItemStack();
      } else if (data instanceof DustColorTransitionData cast) {
         return new DustTransition(
            Color.fromRGB((int)cast.getRed(), (int)cast.getGreen(), (int)cast.getBlue()),
            Color.fromRGB(cast.getFadeRed(), cast.getFadeGreen(), cast.getFadeBlue()),
            cast.getSize()
         );
      } else if (data instanceof DustData cast) {
         if (data.getEffect().equals(Particle.NOTE)) {
            return NoteColor.calculateNote(new java.awt.Color(cast.getRed(), cast.getGreen(), cast.getBlue()));
         } else {
            return !data.getEffect().equals(Particle.DUST_COLOR_TRANSITION) && !data.getEffect().equals(Particle.DUST_PLUME)
               ? Color.fromRGB((int)cast.getRed(), (int)cast.getGreen(), (int)cast.getBlue())
               : new DustOptions(Color.fromRGB((int)cast.getRed(), (int)cast.getGreen(), (int)cast.getBlue()), 1.0F);
         }
      } else if (data instanceof NoteColor cast) {
         return cast.getNote();
      } else if (data instanceof RegularColor cast) {
         return data.getEffect().equals(Particle.NOTE)
            ? NoteColor.calculateNote(new java.awt.Color(cast.getRed(), cast.getGreen(), cast.getBlue()))
            : new DustOptions(Color.fromRGB((int)cast.getRed(), (int)cast.getGreen(), (int)cast.getBlue()), 1.0F);
      } else {
         return null;
      }
   }

   private Object createDefaultData(org.bukkit.Particle particle) {
      Class<?> dataType = particle.getDataType();
      if (dataType.equals(DustOptions.class) || particle.equals(org.bukkit.Particle.ENTITY_EFFECT)) {
         return new DustOptions(Color.RED, 1.0F);
      } else if (dataType.equals(ItemStack.class)) {
         return new ItemStack(Material.BARRIER);
      } else if (dataType.equals(BlockData.class)) {
         return Bukkit.createBlockData(Material.BARRIER);
      } else if (dataType.equals(DustTransition.class)) {
         return new DustTransition(Color.WHITE, Color.BLACK, 1.0F);
      } else if (dataType.equals(Float.class)) {
         return 0;
      } else if (dataType.equals(Integer.class)) {
         return 0;
      } else if (dataType.equals(MaterialData.class)) {
         return new MaterialData(Material.BARRIER);
      } else {
         return dataType != null && !dataType.equals(Void.class) && !dataType.equals(Vibration.class) ? null : null;
      }
   }

   @Override
   public void setCollidable(boolean collidable) {
      this.player.setCollidable(collidable);
   }

   @Override
   public void setArrowsInBody(int count) {
      this.player.setArrowsInBody(count);
   }

   @Override
   public void stopSound(Sound sound) {
      this.getBukkit().stopSound(sound);
   }

   @Override
   public void openBook(ItemStack item) {
      this.player.openBook(item);
   }
}
