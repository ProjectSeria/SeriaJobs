package id.seria.gacha.lib.xseries.profiles.objects.transformer;

import com.mojang.authlib.properties.Property;
import id.seria.gacha.lib.xseries.profiles.PlayerProfiles;
import id.seria.gacha.lib.xseries.profiles.gameprofile.MojangGameProfile;
import id.seria.gacha.lib.xseries.profiles.objects.Profileable;
import java.util.concurrent.atomic.AtomicLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface ProfileTransformer {
   @NotNull
   MojangGameProfile transform(@NotNull Profileable var1, @NotNull MojangGameProfile var2);

   @Internal
   boolean canBeCached();

   @NotNull
   static ProfileTransformer stackable() {
      return ProfileTransformer.RemoveMetadata.INSTANCE;
   }

   @NotNull
   static ProfileTransformer nonStackable() {
      return ProfileTransformer.MakeNotStackable.INSTANCE;
   }

   @NotNull
   static ProfileTransformer removeMetadata() {
      return ProfileTransformer.RemoveMetadata.INSTANCE;
   }

   @NotNull
   static ProfileTransformer includeOriginalValue() {
      return ProfileTransformer.IncludeOriginalValue.INSTANCE;
   }

   final class IncludeOriginalValue implements ProfileTransformer {
      private static final ProfileTransformer.IncludeOriginalValue INSTANCE = new ProfileTransformer.IncludeOriginalValue();
      public static final String PROPERTY_NAME = "OriginalValue";

      public IncludeOriginalValue() {
      }

      @Nullable
      public static String getOriginalValue(@NotNull MojangGameProfile profile) {
         Property prop = profile.getProperty("OriginalValue");
         return prop == null ? null : PlayerProfiles.getPropertyValue(prop);
      }

      @Override
      public MojangGameProfile transform(Profileable profileable, MojangGameProfile profile) {
         String originalValue = profileable.getProfileValue();
         return profile.copy(x -> x.setProperty("OriginalValue", originalValue));
      }

      @Override
      public boolean canBeCached() {
         return true;
      }
   }

   final class MakeNotStackable implements ProfileTransformer {
      private static final ProfileTransformer.MakeNotStackable INSTANCE = new ProfileTransformer.MakeNotStackable();
      private static final String PROPERTY_NAME = "XSeriesSeed";
      private static final AtomicLong NEXT_ID = new AtomicLong();

      public MakeNotStackable() {
      }

      @Override
      public MojangGameProfile transform(Profileable profileable, MojangGameProfile profile) {
         String value = System.currentTimeMillis() + "-" + NEXT_ID.getAndIncrement();
         return profile.copy(x -> x.setProperty("XSeriesSeed", value));
      }

      @Override
      public boolean canBeCached() {
         return false;
      }
   }

   final class RemoveMetadata implements ProfileTransformer {
      private static final ProfileTransformer.RemoveMetadata INSTANCE = new ProfileTransformer.RemoveMetadata();

      public RemoveMetadata() {
      }

      @Override
      public MojangGameProfile transform(Profileable profileable, MojangGameProfile profile) {
         PlayerProfiles.removeTimestamp(profile);
         return profile.copy(x -> {
            x.removeProperty("XSeries");
            x.removeProperty("OriginalValue");
         });
      }

      @Override
      public boolean canBeCached() {
         return true;
      }
   }
}
