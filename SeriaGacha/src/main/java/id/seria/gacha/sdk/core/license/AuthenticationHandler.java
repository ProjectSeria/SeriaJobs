package id.seria.gacha.sdk.core.license;

import id.seria.gacha.api.sdk.plugin.Plugin;
import id.seria.gacha.sdk.core.Core;
import java.util.Base64;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Generated;

public class AuthenticationHandler {
   private final Plugin plugin;
   private final String productId;
   private final String planId;
   private final String license;
   private final Consumer<Exception> onError;
   private final Consumer<AuthenticationResponse> onSuccess;
   private final AuthCache authCache;
   private ScheduledFuture<?> currentTask;
   private boolean authenticated;
   private final BuildInfo buildInfo = new BuildInfo("1789460705000296", "stable", "6.3.0", "41");

   @Deprecated
   public AuthenticationHandler(Plugin plugin, String productId, String license, Consumer<Exception> onError) {
      this(plugin, productId, "standard", license, onError);
   }

   @Deprecated
   public AuthenticationHandler(Plugin plugin, String productId, String license, Consumer<Exception> onError, Consumer<AuthenticationResponse> onSuccess) {
      this(plugin, productId, "standard", license, onError, onSuccess);
   }

   public AuthenticationHandler(Plugin plugin, String productId, String planId, String license, Consumer<Exception> onError) {
      this(plugin, productId, planId, license, onError, auth -> {});
   }

   public AuthenticationHandler(
      Plugin plugin, String productId, String planId, String license, Consumer<Exception> onError, Consumer<AuthenticationResponse> onSuccess
   ) {
      this.plugin = plugin;
      this.productId = productId;
      this.planId = planId;
      this.license = license;
      this.onError = onError;
      this.onSuccess = onSuccess;
      this.authCache = new AuthCache(plugin);
   }

   public AuthenticationResponse login() {
      try {
         AuthCache.CacheData data = this.authCache.read();
         AuthenticationResponse response = LicenseAPI.login(
            this.productId, this.planId, this.license, data == null ? null : data.getSignature(), this.buildInfo
         );
         this.authCache.save(response.getAuthToken(), response.getGracePeriodSignature(), response.getGracePeriodExpiry());
         long millis = response.getAuthTokenExpiry() - System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5L);
         if (millis <= 0L) {
            this.onError.accept(new Exception("Authentication expiration time already expired. Is your clock with right time?"));
            return null;
         } else {
            this.currentTask = Core.getSchedulerService().schedule(() -> this.renewAuthToken(1), millis, TimeUnit.SECONDS);
            this.callSuccess(response);
            return response;
         }
      } catch (Exception var6) {
         Exception e = var6;

         try {
            this.tryGracePeriodVerification(e);
            AuthCache.CacheData data = this.authCache.read();
            AuthenticationResponse forged = new AuthenticationResponse(
               null, data.getSignature(), -1L, data.getGracePeriodSignature(), data.getGracePeriodExpiry()
            );
            this.callSuccess(forged);
            return forged;
         } catch (Exception ex) {
            this.callError(ex);
            this.currentTask = Core.getSchedulerService().schedule(() -> this.login(), 5L, TimeUnit.MINUTES);
            return null;
         }
      }
   }

   private void tryGracePeriodVerification(Exception exception) throws Exception {
      AuthCache.CacheData data = this.authCache.read();
      if (data == null) {
         throw exception;
      }

      GracePeriodVerification gracePeriodVerification = new GracePeriodVerification(
         Base64.getDecoder().decode(data.getGracePeriodSignature()), data.getGracePeriodExpiry()
      );
      if (!gracePeriodVerification.validate()) {
         throw new Exception(exception.getMessage() + ". Grace period has passed.");
      }
   }

   public void logout() {
      try {
         if (!this.authenticated) {
            return;
         }

         AuthCache.CacheData data = this.authCache.read();
         if (this.currentTask != null) {
            this.currentTask.cancel(true);
            this.currentTask = null;
         }

         if (data == null) {
            return;
         }

         LicenseAPI.logout(data.getSignature());
      } catch (Exception var2) {
      }
   }

   private void renewAuthToken(int tryCount) {
      try {
         AuthCache.CacheData data = this.authCache.read();
         if (data == null) {
            this.login();
            return;
         }

         RenewResponse response = LicenseAPI.renew(data.getSignature());
         this.authCache.save(response.getAuthToken(), response.getGracePeriodSignature(), response.getGracePeriodExpiry());
         long millis = response.getAuthTokenExpiry() - System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5L);
         if (millis <= 0L) {
            this.onError.accept(new Exception("Authentication expiration time already expired. Is your clock with right time"));
            return;
         }

         this.currentTask = Core.getSchedulerService().schedule(() -> this.renewAuthToken(1), millis, TimeUnit.MILLISECONDS);
      } catch (LicenseAPI.ApiException e) {
         this.callError(e);
      } catch (Exception var8) {
         Exception e = var8;

         try {
            this.tryGracePeriodVerification(e);
            AuthCache.CacheData data = this.authCache.read();
            AuthenticationResponse forged = new AuthenticationResponse(
               null, data.getSignature(), -1L, data.getGracePeriodSignature(), data.getGracePeriodExpiry()
            );
            this.callSuccess(forged);
         } catch (Exception ex) {
            this.plugin.getLogger().warn("An unexpected error occurred while renewing plugin connection: " + ex.getMessage());
            if (tryCount == 3) {
               this.callError(ex);
               this.currentTask = Core.getSchedulerService().schedule(() -> this.login(), 5L, TimeUnit.MINUTES);
            } else {
               this.currentTask = Core.getSchedulerService().schedule(() -> this.renewAuthToken(tryCount + 1), 5L, TimeUnit.MINUTES);
            }
         }
      }
   }

   public void callError(Exception error) {
      this.authenticated = false;
      this.plugin.getScheduler().runSync(() -> this.onError.accept(error));
   }

   public void callSuccess(AuthenticationResponse success) {
      this.authenticated = true;
      this.plugin.getScheduler().runSync(() -> this.onSuccess.accept(success));
   }

   @Generated
   public Plugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public String getProductId() {
      return this.productId;
   }

   @Generated
   public String getPlanId() {
      return this.planId;
   }

   @Generated
   public String getLicense() {
      return this.license;
   }

   @Generated
   public Consumer<Exception> getOnError() {
      return this.onError;
   }

   @Generated
   public Consumer<AuthenticationResponse> getOnSuccess() {
      return this.onSuccess;
   }

   @Generated
   public AuthCache getAuthCache() {
      return this.authCache;
   }

   @Generated
   public ScheduledFuture<?> getCurrentTask() {
      return this.currentTask;
   }

   @Generated
   public boolean isAuthenticated() {
      return this.authenticated;
   }

   @Generated
   public BuildInfo getBuildInfo() {
      return this.buildInfo;
   }
}
