package id.seria.gacha.lib.common.utils.utility.quaternion;

public class MatrixMath {
   public MatrixMath() {
   }

   public static boolean luDecomposition(double[] matrix0, int[] row_perm) {
      double[] row_scale = new double[4];
      int ptr = 0;
      int rs = 0;
      int i = 4;

      while (i-- != 0) {
         double big = 0.0;
         int j = 4;

         while (j-- != 0) {
            double temp = matrix0[ptr++];
            temp = Math.abs(temp);
            if (temp > big) {
               big = temp;
            }
         }

         if (big == 0.0) {
            return false;
         }

         row_scale[rs++] = 1.0 / big;
      }

      int mtx = 0;

      for (int j = 0; j < 4; j++) {
         for (int ix = 0; ix < j; ix++) {
            int target = mtx + 4 * ix + j;
            double sum = matrix0[target];
            int k = ix;
            int p1 = mtx + 4 * ix;

            for (int p2 = mtx + j; k-- != 0; p2 += 4) {
               sum -= matrix0[p1] * matrix0[p2];
               p1++;
            }

            matrix0[target] = sum;
         }

         double big = 0.0;
         rs = -1;

         for (int var24 = j; var24 < 4; var24++) {
            int target = mtx + 4 * var24 + j;
            double sum = matrix0[target];
            int k = j;
            int p1 = mtx + 4 * var24;

            for (int p2 = mtx + j; k-- != 0; p2 += 4) {
               sum -= matrix0[p1] * matrix0[p2];
               p1++;
            }

            matrix0[target] = sum;
            double temp;
            if ((temp = row_scale[var24] * Math.abs(sum)) >= big) {
               big = temp;
               rs = var24;
            }
         }

         if (rs < 0) {
            return false;
         }

         if (j != rs) {
            int k = 4;
            int p1 = mtx + 4 * rs;
            int p2 = mtx + 4 * j;

            while (k-- != 0) {
               double temp = matrix0[p1];
               matrix0[p1++] = matrix0[p2];
               matrix0[p2++] = temp;
            }

            row_scale[rs] = row_scale[j];
         }

         row_perm[j] = rs;
         double SINGULAR_EP = 1.0E-5;
         double v = matrix0[mtx + 4 * j + j];
         if (v >= -1.0E-5 && v <= 1.0E-5) {
            return false;
         }

         if (j != 3) {
            double temp = 1.0 / matrix0[mtx + 4 * j + j];
            int target = mtx + 4 * (j + 1) + j;

            for (int var25 = 3 - j; var25-- != 0; target += 4) {
               matrix0[target] *= temp;
            }
         }
      }

      return true;
   }

   public static void luBacksubstitution(double[] matrix1, int[] row_perm, double[] matrix2) {
      int rp = 0;

      for (int k = 0; k < 4; k++) {
         int cv = k;
         int ii = -1;

         for (int i = 0; i < 4; i++) {
            int ip = row_perm[rp + i];
            double sum = matrix2[cv + 4 * ip];
            matrix2[cv + 4 * ip] = matrix2[cv + 4 * i];
            if (ii >= 0) {
               int rv = i * 4;

               for (int j = ii; j <= i - 1; j++) {
                  sum -= matrix1[rv + j] * matrix2[cv + 4 * j];
               }
            } else if (sum != 0.0) {
               ii = i;
            }

            matrix2[cv + 4 * i] = sum;
         }

         int rv = 12;
         matrix2[cv + 12] = matrix2[cv + 12] / matrix1[rv + 3];
         rv -= 4;
         matrix2[cv + 8] = (matrix2[cv + 8] - matrix1[rv + 3] * matrix2[cv + 12]) / matrix1[rv + 2];
         rv -= 4;
         matrix2[cv + 4] = (matrix2[cv + 4] - matrix1[rv + 2] * matrix2[cv + 8] - matrix1[rv + 3] * matrix2[cv + 12]) / matrix1[rv + 1];
         rv -= 4;
         matrix2[cv + 0] = (matrix2[cv + 0] - matrix1[rv + 1] * matrix2[cv + 4] - matrix1[rv + 2] * matrix2[cv + 8] - matrix1[rv + 3] * matrix2[cv + 12])
            / matrix1[rv + 0];
      }
   }
}
