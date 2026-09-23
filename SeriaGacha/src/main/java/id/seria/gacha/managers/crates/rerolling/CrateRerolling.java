package id.seria.gacha.managers.crates.rerolling;

import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;

public class CrateRerolling implements Cloneable {
   @ConfigField
   private boolean enabled = false;
   @ConfigField
   private int freeRolls = 0;
   @ConfigField
   private List<RerollGroup> groups = new ArrayList<>();
   @ConfigField
   private boolean replayAnimation;
   @ConfigField
   private boolean skipSameReward;

   public CrateRerolling() {
      this.groups.add(new RerollGroup("common", 3));
      this.groups.add(new RerollGroup("rare", 5));
      this.groups.add(new RerollGroup("legendary", 10));
      this.replayAnimation = false;
      this.skipSameReward = false;
   }

   public CrateRerolling clone() {
      CrateRerolling cloned = new CrateRerolling();
      cloned.setEnabled(this.enabled);
      cloned.setFreeRolls(this.freeRolls);
      cloned.setGroups(this.groups.stream().map(RerollGroup::clone).collect(Collectors.toList()));
      cloned.setReplayAnimation(this.replayAnimation);
      cloned.setSkipSameReward(this.skipSameReward);
      return cloned;
   }

   @Generated
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Generated
   public void setFreeRolls(int freeRolls) {
      this.freeRolls = freeRolls;
   }

   @Generated
   public void setGroups(List<RerollGroup> groups) {
      this.groups = groups;
   }

   @Generated
   public void setReplayAnimation(boolean replayAnimation) {
      this.replayAnimation = replayAnimation;
   }

   @Generated
   public void setSkipSameReward(boolean skipSameReward) {
      this.skipSameReward = skipSameReward;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public int getFreeRolls() {
      return this.freeRolls;
   }

   @Generated
   public List<RerollGroup> getGroups() {
      return this.groups;
   }

   @Generated
   public boolean isReplayAnimation() {
      return this.replayAnimation;
   }

   @Generated
   public boolean isSkipSameReward() {
      return this.skipSameReward;
   }
}
