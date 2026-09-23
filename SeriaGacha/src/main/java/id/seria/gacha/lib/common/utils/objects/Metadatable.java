package id.seria.gacha.lib.common.utils.objects;

import com.google.common.primitives.Primitives;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Color;
import org.bukkit.Material;

public class Metadatable {
   private final Map<String, Metadatable.MetadataValue> hashMap = new HashMap<>();

   public Metadatable() {
   }

   public Metadatable.MetadataValue getMetadata(String key) {
      return new Metadatable.MetadataValue(this.hashMap.get(key) == null ? null : this.hashMap.get(key).object);
   }

   public Metadatable.MetadataValue getMetadataOrDefault(String key, Object defaultVaue) {
      return new Metadatable.MetadataValue(this.hashMap.get(key) == null ? defaultVaue : this.hashMap.get(key).object);
   }

   public Metadatable setMetadata(String key, Object value) {
      this.hashMap.put(key, new Metadatable.MetadataValue(value));
      return this;
   }

   public Metadatable removeMetadata(String key) {
      this.hashMap.remove(key);
      return this;
   }

   public void importData(Metadatable metadata) {
      this.hashMap.putAll(metadata.getHashMap());
   }

   @Generated
   public Map<String, Metadatable.MetadataValue> getHashMap() {
      return this.hashMap;
   }

   public class MetadataValue {
      private final Object object;

      public <T> T as(Class<T> clazz) {
         return (T)(this.object == null ? null : Primitives.wrap(clazz).cast(this.object));
      }

      public String asString() {
         return this.object == null ? null : this.object.toString();
      }

      public int asInt() {
         return this.object == null ? 0 : (Integer)this.object;
      }

      public double asDouble() {
         return this.object == null ? 0.0 : (Double)this.object;
      }

      public short asShort() {
         return this.object == null ? 0 : (Short)this.object;
      }

      public byte asByte() {
         return this.object == null ? 0 : (Byte)this.object;
      }

      public Color asColor() {
         return this.object == null ? null : (Color)this.object;
      }

      public Material asMaterial() {
         return this.object == null ? null : (Material)this.object;
      }

      public Object asObject() {
         return this.object;
      }

      @Generated
      public MetadataValue(final Object object) {
         this.object = object;
      }
   }
}
