package id.seria.gacha.lib.common.uicomponents.newest.actions;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import java.util.Objects;
import lombok.Generated;
import org.bukkit.ChatColor;

public class ClickAction {
   public static ClickAction NAVIGATE = new ClickAction(ClickAction.ClickType.LEFT, Translatable.key("labels.navigate"));
   public static ClickAction RELOAD = new ClickAction(ClickAction.ClickType.LEFT, Translatable.key("labels.reload"));
   private ClickAction.ClickType clickType;
   private Translatable displayAction;

   @Deprecated
   public static ClickAction legacy(ClickAction.ClickType clickType, String displayAction) {
      return new ClickAction(clickType, Translatable.literal(displayAction));
   }

   public static ClickAction CUSTOM(String action, ClickAction.ClickType clickType, Translatable description) {
      return new ClickAction(clickType, Translatable.key("layout.actions.description." + action, "%value%", f(description)));
   }

   public static ClickAction ADD(ClickAction.ClickType clickType, Translatable description) {
      return new ClickAction(clickType, Translatable.key("layout.actions.description.add", "%value%", f(description)));
   }

   public static ClickAction CHOOSE(ClickAction.ClickType clickType, Translatable description) {
      return new ClickAction(clickType, Translatable.key("layout.actions.description.choose", "%value%", f(description)));
   }

   public static ClickAction SELECT(ClickAction.ClickType clickType, Translatable description) {
      return new ClickAction(clickType, Translatable.key("layout.actions.description.select", "%value%", f(description)));
   }

   public static ClickAction SET(ClickAction.ClickType clickType, Translatable description) {
      return new ClickAction(clickType, Translatable.key("layout.actions.description.set", "%value%", f(description)));
   }

   public static ClickAction CREATE(ClickAction.ClickType clickType, Translatable description) {
      return new ClickAction(clickType, Translatable.key("layout.actions.description.create", "%value%", f(description)));
   }

   public static ClickAction CHANGE(ClickAction.ClickType clickType, Translatable description) {
      return new ClickAction(clickType, Translatable.key("layout.actions.description.change", "%value%", f(description)));
   }

   public static ClickAction REMOVE(ClickAction.ClickType clickType, Translatable description) {
      return new ClickAction(clickType, Translatable.key("layout.actions.description.remove", "%value%", f(description)));
   }

   public static ClickAction EDIT(ClickAction.ClickType clickType, Translatable description) {
      return new ClickAction(clickType, Translatable.key("layout.actions.description.edit", "%value%", f(description)));
   }

   public static ClickAction DELETE(ClickAction.ClickType clickType, Translatable description) {
      return new ClickAction(clickType, Translatable.key("layout.actions.description.delete", "%value%", f(description)));
   }

   public static ClickAction CLEAR(ClickAction.ClickType clickType, Translatable description) {
      return new ClickAction(clickType, Translatable.key("layout.actions.description.clear", "%value%", f(description)));
   }

   public static ClickAction REPLACE(ClickAction.ClickType clickType, Translatable description) {
      return new ClickAction(clickType, Translatable.key("layout.actions.description.replace", "%value%", f(description)));
   }

   public static ClickAction SWAP(ClickAction.ClickType clickType, Translatable description) {
      return new ClickAction(clickType, Translatable.key("layout.actions.description.swap", "%value%", f(description)));
   }

   private static Translatable f(Translatable key) {
      return key.clone().postProcessor(str -> ChatColor.stripColor(str.toLowerCase()));
   }

   public static ClickAction RECEIVE(ClickAction.ClickType clickType, Translatable description) {
      return new ClickAction(clickType, Translatable.key("layout.actions.description.receive", "%value%", f(description)));
   }

   public ClickAction(ClickAction.ClickType clickType) {
      this.clickType = clickType;
      this.displayAction = Translatable.empty();
   }

   public ClickAction(ClickAction.ClickType clickType, Translatable displayAction) {
      this.clickType = clickType;
      this.displayAction = displayAction;
   }

   public Translatable format() {
      return this.clickType.getFormat().arguments("%action%", this.displayAction);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.clickType);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         ClickAction that = (ClickAction)obj;
         return this.clickType == that.clickType;
      } else {
         return false;
      }
   }

   @Generated
   @Override
   public String toString() {
      return "ClickAction(clickType=" + this.getClickType() + ", displayAction=" + this.getDisplayAction() + ")";
   }

   @Generated
   public ClickAction.ClickType getClickType() {
      return this.clickType;
   }

   @Generated
   public Translatable getDisplayAction() {
      return this.displayAction;
   }

   @Generated
   public void setClickType(ClickAction.ClickType clickType) {
      this.clickType = clickType;
   }

   @Generated
   public void setDisplayAction(Translatable displayAction) {
      this.displayAction = displayAction;
   }

   public enum ClickType {
      LEFT("layout.actions.left-click"),
      RIGHT("layout.actions.right-click"),
      SHIFT_LEFT("layout.actions.shift-left-click"),
      SHIFT_RIGHT("layout.actions.shift-right-click"),
      LEFT_AND_RIGHT("layout.actions.left-right-click"),
      DRAG_DROP("layout.actions.drag-drop-click"),
      DROP("layout.actions.drop-click");

      private Translatable format;

      ClickType(String key) {
         this.format = Translatable.key(key);
      }

      public boolean isLeftClick() {
         return this == LEFT || this == SHIFT_LEFT;
      }

      public boolean isRightClick() {
         return this == RIGHT || this == SHIFT_RIGHT;
      }

      public boolean isShiftClick() {
         return this == SHIFT_RIGHT || this == SHIFT_LEFT;
      }

      public boolean similar(org.bukkit.event.inventory.ClickType bukkit) {
         ClickAction.ClickType clickType = from(bukkit);
         return this.equals(clickType);
      }

      public static ClickAction.ClickType from(org.bukkit.event.inventory.ClickType other) {
         if (other.isLeftClick()) {
            return other.isShiftClick() ? SHIFT_LEFT : LEFT;
         } else if (other.isRightClick()) {
            return other.isShiftClick() ? SHIFT_RIGHT : RIGHT;
         } else {
            return other == org.bukkit.event.inventory.ClickType.DROP ? DROP : LEFT;
         }
      }

      @Generated
      public Translatable getFormat() {
         return this.format;
      }
   }
}
