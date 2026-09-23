package id.seria.gacha.managers.crates.rerolling;

import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import lombok.Generated;

public class RerollGroup implements Cloneable {
   @ConfigField
   private String namespace = "generic";
   @ConfigField
   private int rerolls = 1;

   public String getNamespace() {
      return this.namespace.replace(" ", "_").toLowerCase();
   }

   public String getPermission() {
      return "phoenixcrates.reroll." + this.namespace;
   }

   public RerollGroup clone() {
      return new RerollGroup(this.namespace, this.rerolls);
   }

   @Generated
   public void setNamespace(String namespace) {
      this.namespace = namespace;
   }

   @Generated
   public void setRerolls(int rerolls) {
      this.rerolls = rerolls;
   }

   @Generated
   public int getRerolls() {
      return this.rerolls;
   }

   @Generated
   public RerollGroup() {
   }

   @Generated
   public RerollGroup(String namespace, int rerolls) {
      this.namespace = namespace;
      this.rerolls = rerolls;
   }
}
