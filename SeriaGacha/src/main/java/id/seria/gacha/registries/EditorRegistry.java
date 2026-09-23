package id.seria.gacha.registries;

import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Key;
import id.seria.gacha.managers.crates.CrateType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

public final class EditorRegistry {
   private static final Map<Key, Function<EditorFacade.EditorLayout, EditorFacade.EditorLayout>> REGISTRY = new HashMap<>();
   private static final Map<Key, BiFunction<CrateType, EditorFacade.EditorLayout, EditorFacade.EditorLayout>> CRATE_SCOPED_REGISTRY = new HashMap<>();
   private static final Map<Key, EditorRegistry.RarityGroupFactory> RARITY_GROUP_REGISTRY = new HashMap<>();

   public EditorRegistry() {
   }

   public static void register(Key key, Function<EditorFacade.EditorLayout, EditorFacade.EditorLayout> factory) {
      REGISTRY.put(key, factory);
   }

   @Nullable
   public static EditorFacade.EditorLayout get(Key key, EditorFacade.EditorLayout parent) {
      Function<EditorFacade.EditorLayout, EditorFacade.EditorLayout> factory = REGISTRY.get(key);
      return factory != null ? factory.apply(parent) : null;
   }

   public static void register(Key key, BiFunction<CrateType, EditorFacade.EditorLayout, EditorFacade.EditorLayout> factory) {
      CRATE_SCOPED_REGISTRY.put(key, factory);
   }

   @Nullable
   public static EditorFacade.EditorLayout get(Key key, CrateType crateType, EditorFacade.EditorLayout parent) {
      BiFunction<CrateType, EditorFacade.EditorLayout, EditorFacade.EditorLayout> factory = CRATE_SCOPED_REGISTRY.get(key);
      return factory != null ? factory.apply(crateType, parent) : null;
   }

   public static void registerRarityGroup(Key key, EditorRegistry.RarityGroupFactory factory) {
      RARITY_GROUP_REGISTRY.put(key, factory);
   }

   @Nullable
   public static EditorFacade.EditorLayout getRarityGroup(
      Key key, CrateType crateType, String rarityIdentifier, String rarityDisplayName, EditorFacade.EditorLayout parent
   ) {
      EditorRegistry.RarityGroupFactory factory = RARITY_GROUP_REGISTRY.get(key);
      return factory != null ? factory.create(crateType, rarityIdentifier, rarityDisplayName, parent) : null;
   }

   public static void unregister(Key key) {
      REGISTRY.remove(key);
      CRATE_SCOPED_REGISTRY.remove(key);
      RARITY_GROUP_REGISTRY.remove(key);
   }

   public static boolean isRegistered(Key key) {
      return REGISTRY.containsKey(key) || CRATE_SCOPED_REGISTRY.containsKey(key) || RARITY_GROUP_REGISTRY.containsKey(key);
   }

   @FunctionalInterface
   public interface RarityGroupFactory {
      EditorFacade.EditorLayout create(CrateType var1, String var2, String var3, EditorFacade.EditorLayout var4);
   }
}
