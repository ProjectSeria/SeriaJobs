package id.seria.gacha.lib.xseries.profiles.objects.cache;

import id.seria.gacha.lib.xseries.profiles.exceptions.MojangAPIRetryException;
import id.seria.gacha.lib.xseries.profiles.gameprofile.MojangGameProfile;
import id.seria.gacha.lib.xseries.profiles.objects.Profileable;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public abstract class CacheableProfileable implements Profileable {
   protected MojangGameProfile cache;
   protected Throwable lastError;

   public CacheableProfileable() {
   }

   @Override
   public final synchronized MojangGameProfile getProfile() {
      if (this.hasExpired(true)) {
         this.lastError = null;
         this.cache = null;
      }

      if (this.lastError != null && !(this.lastError instanceof MojangAPIRetryException)) {
         throw XReflection.throwCheckedException(this.lastError);
      }

      if (this.cache == null) {
         try {
            this.cache = this.cacheProfile();
            this.lastError = null;
         } catch (Throwable ex) {
            this.lastError = ex;
            throw ex;
         }
      }

      return this.cache;
   }

   public final boolean hasExpired() {
      return this.hasExpired(false);
   }

   @Override
   public final boolean isReady() {
      return !this.hasExpired(false);
   }

   protected boolean hasExpired(boolean renew) {
      return this.lastError instanceof MojangAPIRetryException;
   }

   @NotNull
   protected abstract MojangGameProfile cacheProfile();

   @Override
   public final String toString() {
      return this.getClass().getSimpleName() + "[cache=" + this.cache + ", lastError=" + this.lastError + ']';
   }
}
