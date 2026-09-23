package id.seria.gacha.managers.crates.engine.vanilla_model;

import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.engine.EngineData;
import id.seria.gacha.managers.crates.engine.EngineType;
import lombok.Generated;

public class VanillaModelEngineData extends EngineData implements Cloneable {
   @ConfigField
   private XMaterial material = XMaterial.FEATHER;
   @ConfigField
   private int customModelData = 0;
   @ConfigField
   private String itemModel = null;
   @ConfigField
   private double offsetX = 0.0;
   @ConfigField
   private double offsetY = 0.0;
   @ConfigField
   private double offsetZ = 0.0;
   @ConfigField
   private int rotationDegrees = 0;
   @ConfigField
   private double scale = 1.0;

   @Override
   public EngineType getType() {
      return EngineType.valueOf("VANILLA_MODEL");
   }

   public VanillaModelEngineData clone() {
      return new VanillaModelEngineData(
         this.material, this.customModelData, this.itemModel, this.offsetX, this.offsetY, this.offsetZ, this.rotationDegrees, this.scale
      );
   }

   @Generated
   public XMaterial getMaterial() {
      return this.material;
   }

   @Generated
   public int getCustomModelData() {
      return this.customModelData;
   }

   @Generated
   public String getItemModel() {
      return this.itemModel;
   }

   @Generated
   public double getOffsetX() {
      return this.offsetX;
   }

   @Generated
   public double getOffsetY() {
      return this.offsetY;
   }

   @Generated
   public double getOffsetZ() {
      return this.offsetZ;
   }

   @Generated
   public int getRotationDegrees() {
      return this.rotationDegrees;
   }

   @Generated
   public double getScale() {
      return this.scale;
   }

   @Generated
   public void setMaterial(XMaterial material) {
      this.material = material;
   }

   @Generated
   public void setCustomModelData(int customModelData) {
      this.customModelData = customModelData;
   }

   @Generated
   public void setItemModel(String itemModel) {
      this.itemModel = itemModel;
   }

   @Generated
   public void setOffsetX(double offsetX) {
      this.offsetX = offsetX;
   }

   @Generated
   public void setOffsetY(double offsetY) {
      this.offsetY = offsetY;
   }

   @Generated
   public void setOffsetZ(double offsetZ) {
      this.offsetZ = offsetZ;
   }

   @Generated
   public void setRotationDegrees(int rotationDegrees) {
      this.rotationDegrees = rotationDegrees;
   }

   @Generated
   public void setScale(double scale) {
      this.scale = scale;
   }

   @Generated
   public VanillaModelEngineData() {
   }

   @Generated
   public VanillaModelEngineData(
      XMaterial material, int customModelData, String itemModel, double offsetX, double offsetY, double offsetZ, int rotationDegrees, double scale
   ) {
      this.material = material;
      this.customModelData = customModelData;
      this.itemModel = itemModel;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.offsetZ = offsetZ;
      this.rotationDegrees = rotationDegrees;
      this.scale = scale;
   }
}
