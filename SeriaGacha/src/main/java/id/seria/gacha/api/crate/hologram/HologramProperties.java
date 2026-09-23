package id.seria.gacha.api.crate.hologram;

public interface HologramProperties {
   HologramType getType();

   void setType(HologramType var1);

   double getOffset();

   void setOffset(double var1);

   Object get(String var1);

   void set(String var1, Object var2);
}
