package id.seria.gacha.lib.common.utils.api.authentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class GracePeriodVerification {
   private final byte[] signature;
   private final long expirationMillis;

   public GracePeriodVerification(byte[] signature, long expirationMillis) {
      this.signature = signature;
      this.expirationMillis = expirationMillis;
   }

   public boolean validate() throws Exception {
      PublicKey publicKey = this.asPublicKey(this.loadResourceAsString("public_key_rsa.pem"));
      long currentTimeMillis = System.currentTimeMillis();
      Signature rsaVerify = Signature.getInstance("SHA256withRSA");
      rsaVerify.initVerify(publicKey);
      rsaVerify.update(String.valueOf(this.expirationMillis).getBytes(StandardCharsets.UTF_8));
      boolean isValidSignature = rsaVerify.verify(this.signature);
      if (isValidSignature && currentTimeMillis <= this.expirationMillis) {
         return true;
      } else if (!isValidSignature) {
         throw new IllegalStateException("Signature is invalid.");
      } else {
         return false;
      }
   }

   private String loadResourceAsString(String resourceName) throws IOException {
      InputStream inputStream = GracePeriodVerification.class.getResourceAsStream("/" + resourceName);

      String var6;
      try {
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

         try {
            StringBuilder content = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
               content.append(line).append(System.lineSeparator());
            }

            var6 = content.toString();
         } catch (Throwable var9) {
            try {
               reader.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }

            throw var9;
         }

         reader.close();
      } catch (Throwable var10) {
         if (inputStream != null) {
            try {
               inputStream.close();
            } catch (Throwable var7) {
               var10.addSuppressed(var7);
            }
         }

         throw var10;
      }

      if (inputStream != null) {
         inputStream.close();
      }

      return var6;
   }

   private PublicKey asPublicKey(String publicKey) throws Exception {
      publicKey = publicKey.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replace("\\n", "").replaceAll("\\s+", "");
      byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
      return keyFactory.generatePublic(keySpec);
   }
}
