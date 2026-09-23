package id.seria.gacha.lib.common.utils.configuration.adapter;

import id.seria.gacha.lib.common.utils.configuration.adapter.defaults.BukkitColorAdapter;
import id.seria.gacha.lib.common.utils.configuration.adapter.defaults.BukkitItemStackAdapter;
import id.seria.gacha.lib.common.utils.configuration.adapter.defaults.BukkitLocationAdapter;
import id.seria.gacha.lib.common.utils.configuration.adapter.defaults.BukkitPotionEffectTypeAdapter;
import id.seria.gacha.lib.common.utils.configuration.adapter.defaults.BukkitVectorAdapter;
import id.seria.gacha.lib.common.utils.configuration.adapter.defaults.BukkitWorldAdapter;
import id.seria.gacha.lib.common.utils.configuration.adapter.defaults.ComplexMaterialAdapter;
import id.seria.gacha.lib.common.utils.configuration.adapter.defaults.JavaColorAdapter;
import id.seria.gacha.lib.common.utils.configuration.adapter.defaults.SlotCompoundAdapter;
import id.seria.gacha.lib.common.utils.configuration.adapter.defaults.XMaterialAdapter;
import id.seria.gacha.lib.common.utils.configuration.adapter.defaults.XSoundAdapter;
import id.seria.gacha.lib.common.utils.configuration.adapter.defaults.collections.EnumSetCollectionAdapter;
import id.seria.gacha.lib.common.utils.configuration.adapter.defaults.collections.LinkedHashSetCollectionAdapter;
import id.seria.gacha.lib.common.utils.configuration.adapter.defaults.collections.SetCollectionAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.typeresolver.TypeResolver;
import id.seria.gacha.lib.common.utils.typeresolver.TypeToken;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Generated;

public class AdaptersFactory {
   private Set<ConfigAdapter<?, ?>> adapters = new LinkedHashSet<>();

   public AdaptersFactory() {
      this.adapters.add(new SlotCompoundAdapter());
      this.adapters.add(new XMaterialAdapter());
      this.adapters.add(new XSoundAdapter());
      this.adapters.add(new ComplexMaterialAdapter());
      this.adapters.add(new JavaColorAdapter());
      this.adapters.add(new BukkitColorAdapter());
      this.adapters.add(new BukkitItemStackAdapter());
      this.adapters.add(new BukkitLocationAdapter());
      this.adapters.add(new BukkitWorldAdapter());
      this.adapters.add(new BukkitVectorAdapter());
      this.adapters.add(new BukkitPotionEffectTypeAdapter());
      this.adapters.add(new EnumSetCollectionAdapter());
      this.adapters.add(new LinkedHashSetCollectionAdapter());
      this.adapters.add(new SetCollectionAdapter());
   }

   public boolean haveAdapter(Type type) {
      return this.getAdapter(type) != null;
   }

   public ConfigAdapter getAdapter(Type type) {
      if (type instanceof Class) {
         Class<?> clazz = (Class<?>)type;
         Adaptable adaptableAnnotation = clazz.getAnnotation(Adaptable.class);
         if (adaptableAnnotation != null) {
            Class<? extends ConfigAdapter> adapterClass = adaptableAnnotation.adapter();

            try {
               return adapterClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }

      synchronized (this.adapters) {
         for (ConfigAdapter<?, ?> adapter : this.adapters) {
            ParameterizedType adapterType = (ParameterizedType)adapter.getClass().getGenericSuperclass();
            Type firstArg = adapterType.getActualTypeArguments()[0];
            if (TypeToken.isAssignableFrom(firstArg, type)) {
               return adapter;
            }
         }

         return null;
      }
   }

   public AdaptersFactory registerAdapter(ConfigAdapter<?, ?> adapter) throws ErrorException {
      ParameterizedType adapterType = (ParameterizedType)TypeResolver.reify(ConfigAdapter.class, adapter.getClass());
      Type firstArg = adapterType.getActualTypeArguments()[0];
      if (firstArg instanceof Class && ((Class)firstArg).isInterface()) {
         throw new ErrorException("Interfaces are not allowed, use abstract classes instead");
      }

      synchronized (this.adapters) {
         this.adapters.add(adapter);
         return this;
      }
   }

   @Generated
   public Set<ConfigAdapter<?, ?>> getAdapters() {
      return this.adapters;
   }
}
