package id.seria.gacha.managers.crates.type;

import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.Serializable;
import id.seria.gacha.lib.common.utils.configuration.objects.ComplexMaterial;
import id.seria.gacha.lib.common.utils.objects.Base64;
import id.seria.gacha.lib.xseries.XMaterial;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Serializable
public class CrateBlockType implements id.seria.gacha.api.crate.CrateBlockType, Cloneable {
   @ConfigField
   private ComplexMaterial complexMaterial = new ComplexMaterial(XMaterial.CHEST);

   public CrateBlockType(ComplexMaterial complexMaterial) {
      this.complexMaterial = complexMaterial;
   }

   public CrateBlockType(XMaterial material, String texture) {
      this.complexMaterial = new ComplexMaterial(material, texture);
   }

   public CrateBlockType(XMaterial material) {
      this.complexMaterial = new ComplexMaterial(material);
   }

   @Override
   public Material getBukkitMaterial() {
      return this.getMaterial().parseMaterial();
   }

   @Override
   public Object getData() {
      return this.isTextureValid() ? this.getTexture() : this.getMaterial().getData();
   }

   @Override
   public boolean isDataBase64() {
      return this.isTextureValid() && Base64.isValid(this.getTexture());
   }

   @Override
   public boolean isDataByte() {
      return !this.isTextureValid();
   }

   public boolean isTextureValid() {
      return this.complexMaterial.getTexture() != null && this.complexMaterial.getTexture().length() > 0;
   }

   public XMaterial getMaterial() {
      return this.complexMaterial.getMaterial();
   }

   public String getTexture() {
      return this.complexMaterial.getTexture();
   }

   public ItemStack createItemStack() {
      return this.complexMaterial.parseItem();
   }

   public CrateBlockType clone() {
      CrateBlockType cloned = new CrateBlockType();
      cloned.getComplexMaterial().setMaterial(this.complexMaterial.getMaterial());
      cloned.getComplexMaterial().setTexture(this.complexMaterial.getTexture());
      return cloned;
   }

   @Generated
   public ComplexMaterial getComplexMaterial() {
      return this.complexMaterial;
   }

   @Generated
   public void setComplexMaterial(ComplexMaterial complexMaterial) {
      this.complexMaterial = complexMaterial;
   }

   @Generated
   public CrateBlockType() {
   }
}
