package id.seria.gacha.lib.common.utils.actions;

import id.seria.gacha.lib.xseries.XMaterial;
import lombok.Generated;

public abstract class ActionType {
   protected final String name;
   protected boolean legacy;
   protected XMaterial icon;

   public abstract Action createAction(String[] var1);

   public void legacy() {
      this.legacy = true;
   }

   public ActionType icon(XMaterial icon) {
      this.icon = icon;
      return this;
   }

   @Generated
   @Override
   public String toString() {
      return "ActionType(name=" + this.getName() + ", legacy=" + this.isLegacy() + ", icon=" + this.getIcon() + ")";
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public boolean isLegacy() {
      return this.legacy;
   }

   @Generated
   public XMaterial getIcon() {
      return this.icon;
   }

   @Generated
   public ActionType(String name) {
      this.name = name;
   }
}
