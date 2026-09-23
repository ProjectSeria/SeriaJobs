package id.seria.gacha.lib.common.nms.interfaces.entity;

import java.awt.Color;

public interface FakeTextDisplayEntity extends FakeDisplayEntity {
   FakeTextDisplayEntity.TextAlignment getAlignment();

   Color getBackgroundColor();

   int getLineWidth();

   String getText();

   byte getTextOpacity();

   boolean isDefaultBackground();

   boolean isSeeThrough();

   boolean isShadowed();

   FakeTextDisplayEntity setAlignment(FakeTextDisplayEntity.TextAlignment var1);

   FakeTextDisplayEntity setBackgroundColor(Color var1);

   FakeTextDisplayEntity setDefaultBackground(boolean var1);

   FakeTextDisplayEntity setLineWidth(int var1);

   FakeTextDisplayEntity setSeeThrough(boolean var1);

   FakeTextDisplayEntity setShadowed(boolean var1);

   FakeTextDisplayEntity setText(String var1);

   FakeTextDisplayEntity setTextOpacity(byte var1);

   FakeTextDisplayEntity setScale(float var1);

   enum TextAlignment {
      CENTER,
      LEFT,
      RIGHT;

      TextAlignment() {
      }
   }
}
