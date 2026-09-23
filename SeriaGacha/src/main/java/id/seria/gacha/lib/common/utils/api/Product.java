package id.seria.gacha.lib.common.utils.api;

import lombok.Generated;

public class Product {
   private String url;
   private String name;
   private String description;
   private int popularityIndex;
   private double originalPrice;
   private double currentPrice;

   @Generated
   public void setUrl(String url) {
      this.url = url;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public void setDescription(String description) {
      this.description = description;
   }

   @Generated
   public void setPopularityIndex(int popularityIndex) {
      this.popularityIndex = popularityIndex;
   }

   @Generated
   public void setOriginalPrice(double originalPrice) {
      this.originalPrice = originalPrice;
   }

   @Generated
   public void setCurrentPrice(double currentPrice) {
      this.currentPrice = currentPrice;
   }

   @Generated
   public String getUrl() {
      return this.url;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public int getPopularityIndex() {
      return this.popularityIndex;
   }

   @Generated
   public double getOriginalPrice() {
      return this.originalPrice;
   }

   @Generated
   public double getCurrentPrice() {
      return this.currentPrice;
   }

   @Generated
   public Product(String url, String name, String description, int popularityIndex, double originalPrice, double currentPrice) {
      this.url = url;
      this.name = name;
      this.description = description;
      this.popularityIndex = popularityIndex;
      this.originalPrice = originalPrice;
      this.currentPrice = currentPrice;
   }

   @Generated
   @Override
   public String toString() {
      return "Product(url="
         + this.getUrl()
         + ", name="
         + this.getName()
         + ", description="
         + this.getDescription()
         + ", popularityIndex="
         + this.getPopularityIndex()
         + ", originalPrice="
         + this.getOriginalPrice()
         + ", currentPrice="
         + this.getCurrentPrice()
         + ")";
   }
}
