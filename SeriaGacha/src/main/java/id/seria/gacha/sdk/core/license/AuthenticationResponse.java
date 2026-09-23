package id.seria.gacha.sdk.core.license;

import com.google.gson.annotations.SerializedName;
import id.seria.gacha.sdk.utilities.api.ProductVersion;
import lombok.Generated;

public class AuthenticationResponse {
   @SerializedName("current_version")
   private final ProductVersion currentVersion;
   @SerializedName("auth_token")
   private String authToken;
   @SerializedName("auth_token_expiry")
   private long authTokenExpiry;
   @SerializedName("grace_period_signature")
   private String gracePeriodSignature;
   @SerializedName("grace_period_expiry")
   private long gracePeriodExpiry;

   @Generated
   @Override
   public String toString() {
      return "AuthenticationResponse(currentVersion="
         + this.getCurrentVersion()
         + ", authToken="
         + this.getAuthToken()
         + ", authTokenExpiry="
         + this.getAuthTokenExpiry()
         + ", gracePeriodSignature="
         + this.getGracePeriodSignature()
         + ", gracePeriodExpiry="
         + this.getGracePeriodExpiry()
         + ")";
   }

   @Generated
   public ProductVersion getCurrentVersion() {
      return this.currentVersion;
   }

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
   public AuthenticationResponse(ProductVersion currentVersion, String authToken, long authTokenExpiry, String gracePeriodSignature, long gracePeriodExpiry) {
      this.currentVersion = currentVersion;
      this.authToken = authToken;
      this.authTokenExpiry = authTokenExpiry;
      this.gracePeriodSignature = gracePeriodSignature;
      this.gracePeriodExpiry = gracePeriodExpiry;
   }
}
