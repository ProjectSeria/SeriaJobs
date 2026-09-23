package id.seria.gacha.lib.xseries.reflection.aggregate;

import id.seria.gacha.lib.xseries.reflection.XReflection;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class VersionHandle<T> {
   private int version;
   private int patch;
   private T handle;

   @Internal
   public VersionHandle(int version, T handle) {
      this(version, 0, handle);
   }

   @Internal
   public VersionHandle(int version, int patch, T handle) {
      if (XReflection.supports(version, patch)) {
         this.version = version;
         this.patch = patch;
         this.handle = handle;
      }
   }

   @Internal
   public VersionHandle(int version, int patch, Callable<T> handle) {
      if (XReflection.supports(version, patch)) {
         this.version = version;
         this.patch = patch;

         try {
            this.handle = handle.call();
         } catch (Exception var5) {
         }
      }
   }

   @Internal
   public VersionHandle(int version, Callable<T> handle) {
      this(version, 0, handle);
   }

   public VersionHandle<T> v(int version, T handle) {
      return this.v(version, 0, handle);
   }

   private boolean checkVersion(int version, int patch) {
      if (version == this.version && patch == this.patch) {
         throw new IllegalArgumentException("Cannot have duplicate version handles for version: " + version + '.' + patch);
      } else {
         return version > this.version && patch >= this.patch && XReflection.supports(version, patch);
      }
   }

   public VersionHandle<T> v(int version, int patch, Callable<T> handle) {
      if (!this.checkVersion(version, patch)) {
         return this;
      }

      try {
         this.handle = handle.call();
      } catch (Exception var5) {
      }

      this.version = version;
      this.patch = patch;
      return this;
   }

   public VersionHandle<T> v(int version, int patch, T handle) {
      if (this.checkVersion(version, patch)) {
         this.version = version;
         this.patch = patch;
         this.handle = handle;
      }

      return this;
   }

   public T orElse(T handle) {
      return this.version == 0 ? handle : this.handle;
   }

   public T orElse(Callable<T> handle) {
      if (this.version == 0) {
         try {
            return handle.call();
         } catch (Exception e) {
            throw new IllegalArgumentException("The last handle also failed", e);
         }
      } else {
         return this.handle;
      }
   }
}
