package id.seria.gacha.lib.xseries.profiles.objects.transformer;

import id.seria.gacha.lib.xseries.profiles.gameprofile.MojangGameProfile;
import id.seria.gacha.lib.xseries.profiles.objects.Profileable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class TransformableProfile implements Profileable {
   private final Profileable profileable;
   private final TransformableProfile.TransformationSequence transformers;

   @Internal
   public TransformableProfile(Profileable profileable, List<ProfileTransformer> transformers) {
      this.profileable = profileable;
      this.transformers = new TransformableProfile.TransformationSequence(profileable, transformers);
   }

   @Override
   public boolean isReady() {
      return true;
   }

   @Override
   public Profileable transform(ProfileTransformer... transformers) {
      List<ProfileTransformer> transformersList = new ArrayList<>(this.transformers.transformers.length + transformers.length);
      transformersList.addAll(Arrays.stream(this.transformers.transformers).map(x -> x.transformer).collect(Collectors.toList()));
      transformersList.addAll(Arrays.asList(transformers));
      return new TransformableProfile(this.profileable, transformersList);
   }

   @Override
   public MojangGameProfile getProfile() {
      this.transformers.profile = this.profileable.getProfile();
      if (this.transformers.profile == null) {
         return null;
      }

      for (TransformableProfile.TransformationSequence.TransformedProfileCache transformer : this.transformers.transformers) {
         transformer.transform();
      }

      return this.transformers.profile;
   }

   private static final class TransformationSequence {
      private final Profileable profileable;
      @Nullable
      private MojangGameProfile profile;
      private boolean expired;
      private final TransformableProfile.TransformationSequence.TransformedProfileCache[] transformers;

      private TransformationSequence(Profileable profileable, List<ProfileTransformer> transformers) {
         this.profileable = profileable;
         this.transformers = transformers.stream()
            .map(x$0 -> new TransformableProfile.TransformationSequence.TransformedProfileCache(x$0))
            .toArray(TransformableProfile.TransformationSequence.TransformedProfileCache[]::new);
      }

      private final class TransformedProfileCache {
         @Nullable
         private final ProfileTransformer transformer;
         @Nullable
         private MojangGameProfile cacheProfile;

         private TransformedProfileCache(@Nullable ProfileTransformer transformer) {
            this.transformer = transformer;
         }

         private void transform() {
            if (this.cacheProfile != null && this.transformer.canBeCached()) {
               if (!TransformationSequence.this.expired) {
                  TransformationSequence.this.profile = this.cacheProfile;
                  return;
               }
            } else {
               TransformationSequence.this.expired = true;
            }

            TransformationSequence.this.profile = this.cacheProfile = this.transformer
               .transform(TransformationSequence.this.profileable, TransformationSequence.this.profile);
         }
      }
   }
}
