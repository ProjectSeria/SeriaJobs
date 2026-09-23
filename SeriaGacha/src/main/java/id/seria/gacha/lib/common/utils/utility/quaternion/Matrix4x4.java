package id.seria.gacha.lib.common.utils.utility.quaternion;

import org.bukkit.util.Vector;

public class Matrix4x4 implements Cloneable {
   public double m00;
   public double m01;
   public double m02;
   public double m03;
   public double m10;
   public double m11;
   public double m12;
   public double m13;
   public double m20;
   public double m21;
   public double m22;
   public double m23;
   public double m30;
   public double m31;
   public double m32;
   public double m33;

   public Matrix4x4() {
      this.setIdentity();
   }

   public Matrix4x4(
      double m00,
      double m01,
      double m02,
      double m03,
      double m10,
      double m11,
      double m12,
      double m13,
      double m20,
      double m21,
      double m22,
      double m23,
      double m30,
      double m31,
      double m32,
      double m33
   ) {
      this.set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
   }

   public Matrix4x4(Matrix4x4 matrix) {
      this.set(matrix);
   }

   public final void set(
      double m00,
      double m01,
      double m02,
      double m03,
      double m10,
      double m11,
      double m12,
      double m13,
      double m20,
      double m21,
      double m22,
      double m23,
      double m30,
      double m31,
      double m32,
      double m33
   ) {
      this.m00 = m00;
      this.m01 = m01;
      this.m02 = m02;
      this.m03 = m03;
      this.m10 = m10;
      this.m11 = m11;
      this.m12 = m12;
      this.m13 = m13;
      this.m20 = m20;
      this.m21 = m21;
      this.m22 = m22;
      this.m23 = m23;
      this.m30 = m30;
      this.m31 = m31;
      this.m32 = m32;
      this.m33 = m33;
   }

   public final void set(Matrix4x4 m) {
      this.set(m.m00, m.m01, m.m02, m.m03, m.m10, m.m11, m.m12, m.m13, m.m20, m.m21, m.m22, m.m23, m.m30, m.m31, m.m32, m.m33);
   }

   public final void setIdentity() {
      this.set(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);
   }

   public final void set(float[] values) {
      this.set(
         values[0],
         values[1],
         values[2],
         values[3],
         values[4],
         values[5],
         values[6],
         values[7],
         values[8],
         values[9],
         values[10],
         values[11],
         values[12],
         values[13],
         values[14],
         values[15]
      );
   }

   public final void set(double[] values) {
      this.set(
         values[0],
         values[1],
         values[2],
         values[3],
         values[4],
         values[5],
         values[6],
         values[7],
         values[8],
         values[9],
         values[10],
         values[11],
         values[12],
         values[13],
         values[14],
         values[15]
      );
   }

   public final void toArray(float[] values) {
      values[0] = (float)this.m00;
      values[1] = (float)this.m01;
      values[2] = (float)this.m02;
      values[3] = (float)this.m03;
      values[4] = (float)this.m10;
      values[5] = (float)this.m11;
      values[6] = (float)this.m12;
      values[7] = (float)this.m13;
      values[8] = (float)this.m20;
      values[9] = (float)this.m21;
      values[10] = (float)this.m22;
      values[11] = (float)this.m23;
      values[12] = (float)this.m30;
      values[13] = (float)this.m31;
      values[14] = (float)this.m32;
      values[15] = (float)this.m33;
   }

   public final void toArray(double[] values) {
      values[0] = this.m00;
      values[1] = this.m01;
      values[2] = this.m02;
      values[3] = this.m03;
      values[4] = this.m10;
      values[5] = this.m11;
      values[6] = this.m12;
      values[7] = this.m13;
      values[8] = this.m20;
      values[9] = this.m21;
      values[10] = this.m22;
      values[11] = this.m23;
      values[12] = this.m30;
      values[13] = this.m31;
      values[14] = this.m32;
      values[15] = this.m33;
   }

   public final boolean invert() {
      double[] mInput = new double[16];
      int[] row_perm = new int[4];
      this.toArray(mInput);
      if (!MatrixMath.luDecomposition(mInput, row_perm)) {
         return false;
      }

      double[] mOutput = new double[16];

      for (int i = 0; i < 16; i++) {
         mOutput[i] = 0.0;
      }

      mOutput[0] = 1.0;
      mOutput[5] = 1.0;
      mOutput[10] = 1.0;
      mOutput[15] = 1.0;
      MatrixMath.luBacksubstitution(mInput, row_perm, mOutput);
      this.set(mOutput);
      return true;
   }

   public final void rotate(Quaternion quat) {
      double x = quat.getX();
      double y = quat.getY();
      double z = quat.getZ();
      double w = quat.getW();
      double q00 = 2.0 * (-y * y + -z * z);
      double q01 = 2.0 * (x * y + -z * w);
      double q02 = 2.0 * (x * z + y * w);
      double q10 = 2.0 * (x * y + z * w);
      double q11 = 2.0 * (-x * x + -z * z);
      double q12 = 2.0 * (y * z + -x * w);
      double q20 = 2.0 * (x * z + -y * w);
      double q21 = 2.0 * (y * z + x * w);
      double q22 = 2.0 * (-x * x + -y * y);
      double a00 = this.m00 * q00 + this.m01 * q10 + this.m02 * q20;
      double a01 = this.m00 * q01 + this.m01 * q11 + this.m02 * q21;
      double a02 = this.m00 * q02 + this.m01 * q12 + this.m02 * q22;
      double a10 = this.m10 * q00 + this.m11 * q10 + this.m12 * q20;
      double a11 = this.m10 * q01 + this.m11 * q11 + this.m12 * q21;
      double a12 = this.m10 * q02 + this.m11 * q12 + this.m12 * q22;
      double a20 = this.m20 * q00 + this.m21 * q10 + this.m22 * q20;
      double a21 = this.m20 * q01 + this.m21 * q11 + this.m22 * q21;
      double a22 = this.m20 * q02 + this.m21 * q12 + this.m22 * q22;
      double a30 = this.m30 * q00 + this.m31 * q10 + this.m32 * q20;
      double a31 = this.m30 * q01 + this.m31 * q11 + this.m32 * q21;
      double a32 = this.m30 * q02 + this.m31 * q12 + this.m32 * q22;
      this.m00 += a00;
      this.m01 += a01;
      this.m02 += a02;
      this.m10 += a10;
      this.m11 += a11;
      this.m12 += a12;
      this.m20 += a20;
      this.m21 += a21;
      this.m22 += a22;
      this.m30 += a30;
      this.m31 += a31;
      this.m32 += a32;
   }

   public final void rotateX(double angle) {
      if (angle != 0.0) {
         double angleRad = Math.toRadians(angle);
         this.rotateX_unsafe(Math.cos(angleRad), Math.sin(angleRad));
      }
   }

   public final void rotateX(double y, double z) {
      double f = MathUtil.getNormalizationFactor(y, z);
      this.rotateX_unsafe(y * f, z * f);
   }

   private final void rotateX_unsafe(double cos, double sin) {
      double m01 = this.m01 * cos + this.m02 * sin;
      double m02 = this.m01 * -sin + this.m02 * cos;
      double m11 = this.m11 * cos + this.m12 * sin;
      double m12 = this.m11 * -sin + this.m12 * cos;
      double m21 = this.m21 * cos + this.m22 * sin;
      double m22 = this.m21 * -sin + this.m22 * cos;
      double m31 = this.m31 * cos + this.m32 * sin;
      double m32 = this.m31 * -sin + this.m32 * cos;
      this.m01 = m01;
      this.m02 = m02;
      this.m11 = m11;
      this.m12 = m12;
      this.m21 = m21;
      this.m22 = m22;
      this.m31 = m31;
      this.m32 = m32;
   }

   public final void rotateY(double angle) {
      if (angle != 0.0) {
         double angleRad = Math.toRadians(angle);
         this.rotateY_unsafe(Math.cos(angleRad), Math.sin(angleRad));
      }
   }

   public final void rotateY(double x, double z) {
      double f = MathUtil.getNormalizationFactor(x, z);
      this.rotateY_unsafe(x * f, z * f);
   }

   private final void rotateY_unsafe(double cos, double sin) {
      double m00 = this.m00 * cos + this.m02 * -sin;
      double m02 = this.m00 * sin + this.m02 * cos;
      double m10 = this.m10 * cos + this.m12 * -sin;
      double m12 = this.m10 * sin + this.m12 * cos;
      double m20 = this.m20 * cos + this.m22 * -sin;
      double m22 = this.m20 * sin + this.m22 * cos;
      double m30 = this.m30 * cos + this.m32 * -sin;
      double m32 = this.m30 * sin + this.m32 * cos;
      this.m00 = m00;
      this.m02 = m02;
      this.m10 = m10;
      this.m12 = m12;
      this.m20 = m20;
      this.m22 = m22;
      this.m30 = m30;
      this.m32 = m32;
   }

   public final void rotateZ(double angle) {
      if (angle != 0.0) {
         double angleRad = Math.toRadians(angle);
         this.rotateZ_unsafe(Math.cos(angleRad), Math.sin(angleRad));
      }
   }

   public final void rotateZ(double x, double y) {
      double f = MathUtil.getNormalizationFactor(x, y);
      this.rotateZ_unsafe(x * f, y * f);
   }

   private final void rotateZ_unsafe(double cos, double sin) {
      double m00 = this.m00 * cos + this.m01 * sin;
      double m01 = this.m00 * -sin + this.m01 * cos;
      double m10 = this.m10 * cos + this.m11 * sin;
      double m11 = this.m10 * -sin + this.m11 * cos;
      double m20 = this.m20 * cos + this.m21 * sin;
      double m21 = this.m20 * -sin + this.m21 * cos;
      double m30 = this.m30 * cos + this.m31 * sin;
      double m31 = this.m30 * -sin + this.m31 * cos;
      this.m00 = m00;
      this.m01 = m01;
      this.m10 = m10;
      this.m11 = m11;
      this.m20 = m20;
      this.m21 = m21;
      this.m30 = m30;
      this.m31 = m31;
   }

   public final void rotateYawPitchRoll(Vector rotation) {
      this.rotateYawPitchRoll(rotation.getX(), rotation.getY(), rotation.getZ());
   }

   public final void rotateYawPitchRoll(double pitch, double yaw, double roll) {
      this.rotateY(-yaw);
      this.rotateX(pitch);
      this.rotateZ(roll);
   }

   public final void rotateYawPitchRoll(float pitch, float yaw, float roll) {
      this.rotateY(-yaw);
      this.rotateX(pitch);
      this.rotateZ(roll);
   }

   public final Quaternion getRotation() {
      double tr = this.m00 + this.m11 + this.m22;
      if (tr > 0.0) {
         return new Quaternion(this.m21 - this.m12, this.m02 - this.m20, this.m10 - this.m01, 1.0 + tr);
      } else if (this.m00 > this.m11 & this.m00 > this.m22) {
         return new Quaternion(1.0 + this.m00 - this.m11 - this.m22, this.m01 + this.m10, this.m02 + this.m20, this.m21 - this.m12);
      } else {
         return this.m11 > this.m22
            ? new Quaternion(this.m01 + this.m10, 1.0 + this.m11 - this.m00 - this.m22, this.m12 + this.m21, this.m02 - this.m20)
            : new Quaternion(this.m02 + this.m20, this.m12 + this.m21, 1.0 + this.m22 - this.m00 - this.m11, this.m10 - this.m01);
      }
   }

   public final double getRotationPitch() {
      double tr = this.m00 + this.m11 + this.m22;
      double x;
      double y;
      double z;
      double w;
      if (tr > 0.0) {
         x = this.m21 - this.m12;
         y = this.m02 - this.m20;
         z = this.m10 - this.m01;
         w = 1.0 + tr;
      } else if (this.m00 > this.m11 && this.m00 > this.m22) {
         x = 1.0 + this.m00 - this.m11 - this.m22;
         y = this.m01 + this.m10;
         z = this.m02 + this.m20;
         w = this.m21 - this.m12;
      } else if (this.m11 > this.m22) {
         x = this.m01 + this.m10;
         y = 1.0 + this.m11 - this.m00 - this.m22;
         z = this.m12 + this.m21;
         w = this.m02 - this.m20;
      } else {
         x = this.m02 + this.m20;
         y = this.m12 + this.m21;
         z = 1.0 + this.m22 - this.m00 - this.m11;
         w = this.m10 - this.m01;
      }

      double f = MathUtil.getNormalizationFactor(x, y, z, w);
      x *= f;
      y *= f;
      z *= f;
      w *= f;
      return Quaternion.getPitch(x, y, z, w);
   }

   public final double getRotationYaw() {
      double tr = this.m00 + this.m11 + this.m22;
      double x;
      double y;
      double z;
      double w;
      if (tr > 0.0) {
         x = this.m21 - this.m12;
         y = this.m02 - this.m20;
         z = this.m10 - this.m01;
         w = 1.0 + tr;
      } else if (this.m00 > this.m11 && this.m00 > this.m22) {
         x = 1.0 + this.m00 - this.m11 - this.m22;
         y = this.m01 + this.m10;
         z = this.m02 + this.m20;
         w = this.m21 - this.m12;
      } else if (this.m11 > this.m22) {
         x = this.m01 + this.m10;
         y = 1.0 + this.m11 - this.m00 - this.m22;
         z = this.m12 + this.m21;
         w = this.m02 - this.m20;
      } else {
         x = this.m02 + this.m20;
         y = this.m12 + this.m21;
         z = 1.0 + this.m22 - this.m00 - this.m11;
         w = this.m10 - this.m01;
      }

      double f = MathUtil.getNormalizationFactor(x, y, z, w);
      x *= f;
      y *= f;
      z *= f;
      w *= f;
      return Quaternion.getYaw(x, y, z, w);
   }

   public final double getRotationRoll() {
      double tr = this.m00 + this.m11 + this.m22;
      double x;
      double y;
      double z;
      double w;
      if (tr > 0.0) {
         x = this.m21 - this.m12;
         y = this.m02 - this.m20;
         z = this.m10 - this.m01;
         w = 1.0 + tr;
      } else if (this.m00 > this.m11 && this.m00 > this.m22) {
         x = 1.0 + this.m00 - this.m11 - this.m22;
         y = this.m01 + this.m10;
         z = this.m02 + this.m20;
         w = this.m21 - this.m12;
      } else if (this.m11 > this.m22) {
         x = this.m01 + this.m10;
         y = 1.0 + this.m11 - this.m00 - this.m22;
         z = this.m12 + this.m21;
         w = this.m02 - this.m20;
      } else {
         x = this.m02 + this.m20;
         y = this.m12 + this.m21;
         z = 1.0 + this.m22 - this.m00 - this.m11;
         w = this.m10 - this.m01;
      }

      double f = MathUtil.getNormalizationFactor(x, y, z, w);
      x *= f;
      y *= f;
      z *= f;
      w *= f;
      return Quaternion.getRoll(x, y, z, w);
   }

   public final Vector getYawPitchRoll() {
      double tr = this.m00 + this.m11 + this.m22;
      double x;
      double y;
      double z;
      double w;
      if (tr > 0.0) {
         x = this.m21 - this.m12;
         y = this.m02 - this.m20;
         z = this.m10 - this.m01;
         w = 1.0 + tr;
      } else if (this.m00 > this.m11 && this.m00 > this.m22) {
         x = 1.0 + this.m00 - this.m11 - this.m22;
         y = this.m01 + this.m10;
         z = this.m02 + this.m20;
         w = this.m21 - this.m12;
      } else if (this.m11 > this.m22) {
         x = this.m01 + this.m10;
         y = 1.0 + this.m11 - this.m00 - this.m22;
         z = this.m12 + this.m21;
         w = this.m02 - this.m20;
      } else {
         x = this.m02 + this.m20;
         y = this.m12 + this.m21;
         z = 1.0 + this.m22 - this.m00 - this.m11;
         w = this.m10 - this.m01;
      }

      double f = MathUtil.getNormalizationFactor(x, y, z, w);
      x *= f;
      y *= f;
      z *= f;
      w *= f;
      return Quaternion.getYawPitchRoll(x, y, z, w);
   }

   public final void translate(Vector translation) {
      this.translate(translation.getX(), translation.getY(), translation.getZ());
   }

   public final void translate(double dx, double dy, double dz) {
      this.m03 = this.m03 + (this.m00 * dx + this.m01 * dy + this.m02 * dz);
      this.m13 = this.m13 + (this.m10 * dx + this.m11 * dy + this.m12 * dz);
      this.m23 = this.m23 + (this.m20 * dx + this.m21 * dy + this.m22 * dz);
      this.m33 = this.m33 + (this.m30 * dx + this.m31 * dy + this.m32 * dz);
   }

   public final void worldTranslate(Vector translation) {
      this.worldTranslate(translation.getX(), translation.getY(), translation.getZ());
   }

   public final void worldTranslate(double dx, double dy, double dz) {
      this.m00 = this.m00 + dx * this.m30;
      this.m01 = this.m01 + dx * this.m31;
      this.m02 = this.m02 + dx * this.m32;
      this.m03 = this.m03 + dx * this.m33;
      this.m10 = this.m10 + dy * this.m30;
      this.m11 = this.m11 + dy * this.m31;
      this.m12 = this.m12 + dy * this.m32;
      this.m13 = this.m13 + dy * this.m33;
      this.m20 = this.m20 + dz * this.m30;
      this.m21 = this.m21 + dz * this.m31;
      this.m22 = this.m22 + dz * this.m32;
      this.m23 = this.m23 + dz * this.m33;
   }

   public final void scale(Vector scale) {
      this.scale(scale.getX(), scale.getY(), scale.getZ());
   }

   public final void scale(double sx, double sy, double sz) {
      this.m00 *= sx;
      this.m10 *= sx;
      this.m20 *= sx;
      this.m30 *= sx;
      this.m01 *= sy;
      this.m11 *= sy;
      this.m21 *= sy;
      this.m31 *= sy;
      this.m02 *= sz;
      this.m12 *= sz;
      this.m22 *= sz;
      this.m32 *= sz;
   }

   public final void scale(double scale) {
      this.scale(scale, scale, scale);
   }

   public final void translateRotate(double x, double y, double z, float pitch, float yaw) {
      this.translate(x, y, z);
      this.rotateYawPitchRoll(pitch, yaw, 0.0F);
   }

   public final void translateRotate(double x, double y, double z, float pitch, float yaw, float roll) {
      this.translate(x, y, z);
      this.rotateYawPitchRoll(pitch, yaw, roll);
   }

   public final void multiply(Matrix4x4 mRight) {
      multiply(this, mRight, this);
   }

   public final void storeMultiply(Matrix4x4 mLeft, Matrix4x4 mRight) {
      double m00 = mLeft.m00 * mRight.m00 + mLeft.m01 * mRight.m10 + mLeft.m02 * mRight.m20 + mLeft.m03 * mRight.m30;
      double m01 = mLeft.m00 * mRight.m01 + mLeft.m01 * mRight.m11 + mLeft.m02 * mRight.m21 + mLeft.m03 * mRight.m31;
      double m02 = mLeft.m00 * mRight.m02 + mLeft.m01 * mRight.m12 + mLeft.m02 * mRight.m22 + mLeft.m03 * mRight.m32;
      double m03 = mLeft.m00 * mRight.m03 + mLeft.m01 * mRight.m13 + mLeft.m02 * mRight.m23 + mLeft.m03 * mRight.m33;
      double m10 = mLeft.m10 * mRight.m00 + mLeft.m11 * mRight.m10 + mLeft.m12 * mRight.m20 + mLeft.m13 * mRight.m30;
      double m11 = mLeft.m10 * mRight.m01 + mLeft.m11 * mRight.m11 + mLeft.m12 * mRight.m21 + mLeft.m13 * mRight.m31;
      double m12 = mLeft.m10 * mRight.m02 + mLeft.m11 * mRight.m12 + mLeft.m12 * mRight.m22 + mLeft.m13 * mRight.m32;
      double m13 = mLeft.m10 * mRight.m03 + mLeft.m11 * mRight.m13 + mLeft.m12 * mRight.m23 + mLeft.m13 * mRight.m33;
      double m20 = mLeft.m20 * mRight.m00 + mLeft.m21 * mRight.m10 + mLeft.m22 * mRight.m20 + mLeft.m23 * mRight.m30;
      double m21 = mLeft.m20 * mRight.m01 + mLeft.m21 * mRight.m11 + mLeft.m22 * mRight.m21 + mLeft.m23 * mRight.m31;
      double m22 = mLeft.m20 * mRight.m02 + mLeft.m21 * mRight.m12 + mLeft.m22 * mRight.m22 + mLeft.m23 * mRight.m32;
      double m23 = mLeft.m20 * mRight.m03 + mLeft.m21 * mRight.m13 + mLeft.m22 * mRight.m23 + mLeft.m23 * mRight.m33;
      double m30 = mLeft.m30 * mRight.m00 + mLeft.m31 * mRight.m10 + mLeft.m32 * mRight.m20 + mLeft.m33 * mRight.m30;
      double m31 = mLeft.m30 * mRight.m01 + mLeft.m31 * mRight.m11 + mLeft.m32 * mRight.m21 + mLeft.m33 * mRight.m31;
      double m32 = mLeft.m30 * mRight.m02 + mLeft.m31 * mRight.m12 + mLeft.m32 * mRight.m22 + mLeft.m33 * mRight.m32;
      double m33 = mLeft.m30 * mRight.m03 + mLeft.m31 * mRight.m13 + mLeft.m32 * mRight.m23 + mLeft.m33 * mRight.m33;
      this.m00 = m00;
      this.m01 = m01;
      this.m02 = m02;
      this.m03 = m03;
      this.m10 = m10;
      this.m11 = m11;
      this.m12 = m12;
      this.m13 = m13;
      this.m20 = m20;
      this.m21 = m21;
      this.m22 = m22;
      this.m23 = m23;
      this.m30 = m30;
      this.m31 = m31;
      this.m32 = m32;
      this.m33 = m33;
   }

   public final void transformPoint(Vector point) {
      double x = this.m00 * point.getX() + this.m01 * point.getY() + this.m02 * point.getZ() + this.m03;
      double y = this.m10 * point.getX() + this.m11 * point.getY() + this.m12 * point.getZ() + this.m13;
      double z = this.m20 * point.getX() + this.m21 * point.getY() + this.m22 * point.getZ() + this.m23;
      point.setX(x);
      point.setY(y);
      point.setZ(z);
   }

   public Vector toVector() {
      return new Vector(this.m03, this.m13, this.m23);
   }

   public Matrix4x4 clone() {
      return new Matrix4x4(this);
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      }

      if (!(o instanceof Matrix4x4)) {
         return false;
      }

      Matrix4x4 m = (Matrix4x4)o;
      return this.m00 == m.m00
         && this.m01 == m.m01
         && this.m02 == m.m02
         && this.m03 == m.m03
         && this.m10 == m.m10
         && this.m11 == m.m11
         && this.m12 == m.m12
         && this.m13 == m.m13
         && this.m20 == m.m20
         && this.m21 == m.m21
         && this.m22 == m.m22
         && this.m23 == m.m23
         && this.m30 == m.m30
         && this.m31 == m.m31
         && this.m32 == m.m32
         && this.m33 == m.m33;
   }

   @Override
   public String toString() {
      return "{"
         + this.m00
         + ", "
         + this.m01
         + ", "
         + this.m02
         + ", "
         + this.m03
         + "\n "
         + this.m10
         + ", "
         + this.m11
         + ", "
         + this.m12
         + ", "
         + this.m13
         + "\n "
         + this.m20
         + ", "
         + this.m21
         + ", "
         + this.m22
         + ", "
         + this.m23
         + "\n "
         + this.m30
         + ", "
         + this.m31
         + ", "
         + this.m32
         + ", "
         + this.m33
         + "}";
   }

   public static Matrix4x4 fromColumns3x3(Vector v0, Vector v1, Vector v2) {
      return new Matrix4x4(v0.getX(), v1.getX(), v2.getX(), 0.0, v0.getY(), v1.getY(), v2.getY(), 0.0, v0.getZ(), v1.getZ(), v2.getZ(), 0.0, 0.0, 0.0, 0.0, 1.0);
   }

   public static Matrix4x4 identity() {
      return new Matrix4x4();
   }

   public static Matrix4x4 translation(Vector position) {
      return translation(position.getX(), position.getY(), position.getZ());
   }

   public static Matrix4x4 translation(double x, double y, double z) {
      return new Matrix4x4(1.0, 0.0, 0.0, x, 0.0, 1.0, 0.0, y, 0.0, 0.0, 1.0, z, 0.0, 0.0, 0.0, 1.0);
   }

   public static Matrix4x4 diff(Matrix4x4 m1, Matrix4x4 m2) {
      Matrix4x4 diff = m1.clone();
      diff.invert();
      diff.multiply(m2);
      return diff;
   }

   public static Quaternion diffRotation(Matrix4x4 m1, Matrix4x4 m2) {
      return diff(m1, m2).getRotation();
   }

   public static Matrix4x4 multiply(Matrix4x4 mLeft, Matrix4x4 mRight) {
      Matrix4x4 result = new Matrix4x4();
      result.storeMultiply(mLeft, mRight);
      return result;
   }

   public static void multiply(Matrix4x4 mLeft, Matrix4x4 mRight, Matrix4x4 mResult) {
      mResult.storeMultiply(mLeft, mRight);
   }
}
