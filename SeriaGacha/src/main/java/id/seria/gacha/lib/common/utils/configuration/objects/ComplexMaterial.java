package id.seria.gacha.lib.common.utils.configuration.objects;

import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.objects.Base64;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.sdk.platforms.server.internal.Skulls;
import java.util.Objects;
import java.util.function.Function;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;

public class ComplexMaterial {
   @ConfigField
   private XMaterial material;
   @ConfigField
   private String texture;
   private String hookMaterial;
   private static Function<String, ItemStack> hookResolver;

   public static void registerHookResolver(Function<String, ItemStack> resolver) {
      hookResolver = resolver;
   }

   public ComplexMaterial(XMaterial material) {
      this.material = material;
   }

   public ComplexMaterial(XMaterial material, String texture) {
      this.material = material;
      this.texture = texture;
   }

   public ComplexMaterial clone() {
      ComplexMaterial cloned = new ComplexMaterial(this.material, this.texture);
      cloned.hookMaterial = this.hookMaterial;
      return cloned;
   }

   public ItemStack parseItem() {
      if (this.hookMaterial != null) {
         if (hookResolver != null) {
            ItemStack resolved = hookResolver.apply(this.hookMaterial);
            if (resolved != null) {
               return resolved;
            }
         }

         return this.material != null ? this.material.parseItem() : XMaterial.BARRIER.parseItem();
      } else {
         Objects.requireNonNull(this.material);
         if (!this.isTextureValid()) {
            return this.material.parseItem();
         } else {
            return Base64.isValid(this.texture) ? Utilities.getNMSFactory().createTexturedSkullFromBase64(this.texture) : Skulls.getPlayerHead(this.texture);
         }
      }
   }

   public boolean isTextureValid() {
      return this.texture != null && this.texture.length() > 0;
   }

   @Generated
   public XMaterial getMaterial() {
      return this.material;
   }

   @Generated
   public String getTexture() {
      return this.texture;
   }

   @Generated
   public String getHookMaterial() {
      return this.hookMaterial;
   }

   @Generated
   public void setMaterial(XMaterial material) {
      this.material = material;
   }

   @Generated
   public void setTexture(String texture) {
      this.texture = texture;
   }

   @Generated
   public void setHookMaterial(String hookMaterial) {
      this.hookMaterial = hookMaterial;
   }

   @Generated
   public ComplexMaterial() {
   }
}
