package id.seria.gacha.sdk.core.license;

import lombok.Generated;

public class RenewResponse {
   private final String authToken;
   private final long authTokenExpiry;
   private final String gracePeriodSignature;
   private final long gracePeriodExpiry;

   @Generated
   public String getAuthToken() {
      return this.authToken;
   }

   @Generated
   public long getAuthTokenExpiry() {
      return this.authTokenExpiry;
   }

   @Generated
   public String getGracePeriodSignature() {
      return this.gracePeriodSignature;
   }

   @Generated
   public long getGracePeriodExpiry() {
      return this.gracePeriodExpiry;
   }

   @Generated
   public RenewResponse(String authToken, long authTokenExpiry, String gracePeriodSignature, long gracePeriodExpiry) {
      this.authToken = authToken;
      this.authTokenExpiry = authTokenExpiry;
      this.gracePeriodSignature = gracePeriodSignature;
      this.gracePeriodExpiry = gracePeriodExpiry;
   }
}
