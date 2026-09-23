package id.seria.gacha.lib.common.utils.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class BukkitSerialization {
   public BukkitSerialization() {
   }

   public static String serializeInventoryToBase64(PlayerInventory inventory) {
      try {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
         dataOutput.writeInt(inventory.getSize());

         for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
               dataOutput.writeObject(item.serialize());
            } else {
               dataOutput.writeObject(null);
            }
         }

         dataOutput.close();
         return Base64.getMimeEncoder().encodeToString(outputStream.toByteArray());
      } catch (Exception e) {
         throw new IllegalStateException("Unable to serialize inventory", e);
      }
   }

   public static String serializeInventoryToBase64(ItemStack[] items) {
      try {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
         org.apache.commons.lang.Validate.isTrue(items.length == 41, "Expected array size 41, founded " + items.length);
         dataOutput.writeInt(41);

         for (int i = 0; i < 41; i++) {
            ItemStack item = items[i];
            if (item != null) {
               dataOutput.writeObject(item.serialize());
            } else {
               dataOutput.writeObject(null);
            }
         }

         dataOutput.close();
         return Base64.getMimeEncoder().encodeToString(outputStream.toByteArray());
      } catch (Exception e) {
         throw new IllegalStateException("Unable to serialize inventory", e);
      }
   }

   public static String serializeListToBase64(List<ItemStack> items) {
      try {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
         dataOutput.writeInt(items.size());

         for (ItemStack item : items) {
            if (item != null) {
               dataOutput.writeObject(item.serialize());
            } else {
               dataOutput.writeObject(null);
            }
         }

         dataOutput.close();
         return Base64.getMimeEncoder().encodeToString(outputStream.toByteArray());
      } catch (Exception e) {
         throw new IllegalStateException("Unable to serialize inventory", e);
      }
   }

   public static List<ItemStack> deserializeListFromBase64(String base64) {
      try {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getMimeDecoder().decode(base64));

         List var12;
         try {
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            try {
               int count = dataInput.readInt();
               List<ItemStack> items = new ArrayList<>(count);

               for (int i = 0; i < count; i++) {
                  Map<String, Object> stack = (Map<String, Object>)dataInput.readObject();
                  items.add(stack == null ? null : ItemStack.deserialize(stack));
               }

               var12 = items;
            } catch (Throwable var9) {
               try {
                  dataInput.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }

               throw var9;
            }

            dataInput.close();
         } catch (Throwable var10) {
            try {
               inputStream.close();
            } catch (Throwable var7) {
               var10.addSuppressed(var7);
            }

            throw var10;
         }

         inputStream.close();
         return var12;
      } catch (Exception e) {
         throw new IllegalStateException("Unable to deserialize inventory", e);
      }
   }

   public static ItemStack[] deserializeInventoryFromBase64(String base64) {
      try {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getMimeDecoder().decode(base64));
         BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
         ItemStack[] items = new ItemStack[dataInput.readInt()];

         for (int i = 0; i < items.length; i++) {
            Map<String, Object> stack = (Map<String, Object>)dataInput.readObject();
            if (stack != null) {
               items[i] = ItemStack.deserialize(stack);
            } else {
               items[i] = null;
            }
         }

         dataInput.close();
         return items;
      } catch (Exception e) {
         throw new IllegalStateException("Unable to deserialize inventory", e);
      }
   }

   public static String serializePotionsToBase64(Collection<PotionEffect> potions) {
      try {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
         dataOutput.writeInt(potions.size());

         for (PotionEffect potion : potions) {
            dataOutput.writeObject(potion.serialize());
         }

         dataOutput.close();
         return Base64.getMimeEncoder().encodeToString(outputStream.toByteArray());
      } catch (Exception e) {
         throw new IllegalStateException("Unable to serialize inventory", e);
      }
   }

   public static Collection<PotionEffect> deserializePotionsFromBase64(String base64) {
      try {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getMimeDecoder().decode(base64));
         BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
         List<PotionEffect> potions = new ArrayList<>();
         int size = dataInput.readInt();

         for (int i = 0; i < size; i++) {
            Map<String, Object> potion = (Map<String, Object>)dataInput.readObject();
            potions.add(new PotionEffect(potion));
         }

         return potions;
      } catch (Exception e) {
         throw new IllegalStateException("Unable to deserialize potions", e);
      }
   }
}
