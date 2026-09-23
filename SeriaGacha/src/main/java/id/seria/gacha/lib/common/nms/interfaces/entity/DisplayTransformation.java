package id.seria.gacha.lib.common.nms.interfaces.entity;

public final class DisplayTransformation implements Cloneable {
   private static final DisplayTransformation IDENTITY = new DisplayTransformation();
   private float translationX;
   private float translationY;
   private float translationZ;
   private float scaleX = 1.0F;
   private float scaleY = 1.0F;
   private float scaleZ = 1.0F;
   private float leftRotationX;
   private float leftRotationY;
   private float leftRotationZ;
   private float leftRotationW = 1.0F;
   private float rightRotationX;
   private float rightRotationY;
   private float rightRotationZ;
   private float rightRotationW = 1.0F;

   public DisplayTransformation() {
   }

   public static DisplayTransformation identity() {
      return IDENTITY.clone();
   }

   public static DisplayTransformation ofScale(float scale) {
      return new DisplayTransformation().setScale(scale, scale, scale);
   }

   public static DisplayTransformation ofTranslation(float x, float y, float z) {
      return new DisplayTransformation().setTranslation(x, y, z);
   }

   public float getTranslationX() {
      return this.translationX;
   }

   public float getTranslationY() {
      return this.translationY;
   }

   public float getTranslationZ() {
      return this.translationZ;
   }

   public DisplayTransformation setTranslation(float x, float y, float z) {
      this.translationX = x;
      this.translationY = y;
      this.translationZ = z;
      return this;
   }

   public float getScaleX() {
      return this.scaleX;
   }

   public float getScaleY() {
      return this.scaleY;
   }

   public float getScaleZ() {
      return this.scaleZ;
   }

   public DisplayTransformation setScale(float x, float y, float z) {
      this.scaleX = x;
      this.scaleY = y;
      this.scaleZ = z;
      return this;
   }

   public float getLeftRotationX() {
      return this.leftRotationX;
   }

   public float getLeftRotationY() {
      return this.leftRotationY;
   }

   public float getLeftRotationZ() {
      return this.leftRotationZ;
   }

   public float getLeftRotationW() {
      return this.leftRotationW;
   }

   public DisplayTransformation setLeftRotation(float x, float y, float z, float w) {
      this.leftRotationX = x;
      this.leftRotationY = y;
      this.leftRotationZ = z;
      this.leftRotationW = w;
      return this;
   }

   public float getRightRotationX() {
      return this.rightRotationX;
   }

   public float getRightRotationY() {
      return this.rightRotationY;
   }

   public float getRightRotationZ() {
      return this.rightRotationZ;
   }

   public float getRightRotationW() {
      return this.rightRotationW;
   }

   public DisplayTransformation setRightRotation(float x, float y, float z, float w) {
      this.rightRotationX = x;
      this.rightRotationY = y;
      this.rightRotationZ = z;
      this.rightRotationW = w;
      return this;
   }

   public DisplayTransformation setLeftRotationAxisAngle(double angleRadians, double axisX, double axisY, double axisZ) {
      double length = Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
      if (length == 0.0) {
         return this.setLeftRotation(0.0F, 0.0F, 0.0F, 1.0F);
      }

      double sin = Math.sin(angleRadians / 2.0);
      return this.setLeftRotation(
         (float)(axisX / length * sin), (float)(axisY / length * sin), (float)(axisZ / length * sin), (float)Math.cos(angleRadians / 2.0)
      );
   }

   public DisplayTransformation clone() {
      return new DisplayTransformation()
         .setTranslation(this.translationX, this.translationY, this.translationZ)
         .setScale(this.scaleX, this.scaleY, this.scaleZ)
         .setLeftRotation(this.leftRotationX, this.leftRotationY, this.leftRotationZ, this.leftRotationW)
         .setRightRotation(this.rightRotationX, this.rightRotationY, this.rightRotationZ, this.rightRotationW);
   }

   @Override
   public String toString() {
      return "DisplayTransformation{translation=("
         + this.translationX
         + ", "
         + this.translationY
         + ", "
         + this.translationZ
         + "), scale=("
         + this.scaleX
         + ", "
         + this.scaleY
         + ", "
         + this.scaleZ
         + ")}";
   }
}
