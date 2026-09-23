package id.seria.gacha.api.sdk.text;

import lombok.Generated;

public class PlainText implements Text {
   private final String text;

   public PlainText(String text) {
      this.text = text;
   }

   @Override
   public String toPlainText() {
      return this.text;
   }

   @Generated
   public String getText() {
      return this.text;
   }
}
