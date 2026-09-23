package id.seria.gacha.lib.common.services.services.animator;

import java.util.HashMap;
import java.util.Map;
import lombok.Generated;

public class AnimationEntity {
   private String name;
   private Map<String, AnimationBone> bones = new HashMap<>();

   public AnimationEntity(String name) {
      this.name = name;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public Map<String, AnimationBone> getBones() {
      return this.bones;
   }
}
