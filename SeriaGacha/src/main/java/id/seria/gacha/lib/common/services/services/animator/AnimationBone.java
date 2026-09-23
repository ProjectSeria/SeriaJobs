package id.seria.gacha.lib.common.services.services.animator;

import lombok.Generated;

public class AnimationBone {
   private final String name;
   private final BoneFlag flag;

   public AnimationBone(String name, BoneFlag flag) {
      this.name = name;
      this.flag = flag;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public BoneFlag getFlag() {
      return this.flag;
   }
}
