package id.seria.gacha.configurations.adapters;

import id.seria.gacha.api.crate.animation.AnimationPhase;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.lib.common.utils.configuration.ConfigurationTokenizer;
import id.seria.gacha.lib.common.utils.configuration.ConfigurationUtilities;
import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import id.seria.gacha.managers.crates.animations.opening.OpeningPhase;
import id.seria.gacha.managers.crates.animations.opening.data.ColoredOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.ColoredSkullOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.EmptyOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.KeybasedOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.OpeningPhaseData;
import id.seria.gacha.managers.crates.type.list.OpeningAnimation;
import id.seria.gacha.managers.crates.type.list.OpeningAnimationWrapper;
import id.seria.gacha.registries.AnimationsRegistry;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.sdk.utilities.exceptions.NotImplementedException;
import java.awt.Color;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import org.bukkit.configuration.ConfigurationSection;

public class OpeningAnimationConfigAdapter extends ConfigAdapter<OpeningAnimationWrapper, Object> {
   public OpeningAnimationConfigAdapter() {
   }

   public OpeningAnimationWrapper deserialize(ConfigurationReader reader, Type objectType, Object object) throws ErrorException, WarningException {
      if (object instanceof List) {
         return this.importLegacy(reader, object);
      } else if (!(object instanceof ConfigurationSection section)) {
         throw new WarningException("Expected \"Yaml Section\", founded \"" + object.getClass().getSimpleName() + "\"");
      } else {
         if (!Arrays.stream(PhaseType.values()).anyMatch(t -> section.contains(t.getId()))) {
            return this.importLegacy(reader, section);
         }

         OpeningAnimation animation = OpeningAnimation.defaultAnimation();

         for (PhaseType phaseType : PhaseType.values()) {
            String id = phaseType.getId();
            Object typeObj = section.get(id);
            String phaseId = null;
            AnimationProperties properties = AnimationProperties.empty();
            if (typeObj instanceof String) {
               phaseId = (String)typeObj;
            } else if (typeObj instanceof ConfigurationSection) {
               phaseId = ((ConfigurationSection)typeObj).getString("id");
               if (((ConfigurationSection)typeObj).get("properties") instanceof ConfigurationSection propertiesSection) {
                  for (String key : propertiesSection.getKeys(false)) {
                     properties.addProperty(key, propertiesSection.get(key));
                  }
               }
            }

            AnimationPhase animationPhase = null;

            try {
               animationPhase = AnimationsRegistry.getInstance().getPhaseById(phaseType, phaseId).get();
            } catch (NoSuchElementException e) {
               reader.getLogger()
                  .warningAndFlush(
                     reader.getCachedLoggedPath(),
                     "Unknown opening phase \"" + phaseType.name() + "\", \"" + id + "\". Ignoring yaml value and using predefined value."
                  );
               continue;
            }

            OpeningPhase openingPhase = new OpeningPhase(animationPhase, properties);
            switch (phaseType) {
               case PRE_OPENING:
                  animation.setPreOpenPhase(openingPhase);
                  break;
               case OPENING:
                  animation.setOpenPhase(openingPhase);
                  break;
               case DISPLAYING:
                  animation.setDisplayPhase(openingPhase);
                  break;
               default:
                  throw new NotImplementedException();
            }
         }

         return new OpeningAnimationWrapper(animation);
      }
   }

   public Object serialize(ConfigurationWriter writer, OpeningAnimationWrapper animations) throws ErrorException {
      if (animations.list().size() > 1) {
         return this.exportLegacy(writer, animations);
      }

      OpeningAnimation animation = animations.list().get(0);
      Map<String, Object> animationMap = new LinkedHashMap<>();

      for (Entry<PhaseType, OpeningPhase> entry : animation.map().entrySet()) {
         OpeningPhase phase = entry.getValue();
         Map<String, Object> properties = phase.getProperties().map();
         properties.remove("legacyType");
         Object serialized = null;
         if (properties.isEmpty()) {
            serialized = phase.getPhase().getId();
         } else {
            Map<String, Object> serializedMap = new LinkedHashMap<>();
            serializedMap.put("id", phase.getPhase().getId());
            serializedMap.put("properties", properties);
            serialized = serializedMap;
         }

         animationMap.put(entry.getKey().getId(), serialized);
      }

      return animationMap;
   }

   @Deprecated
   private OpeningAnimationWrapper importLegacy(ConfigurationReader reader, Object object) throws ErrorException, WarningException {
      if (object instanceof List) {
         if (((List)object).size() != 3) {
            throw new WarningException("Expected \"3\" phases, founded \"" + ((List)object).size() + "\" arguments");
         }

         OpeningAnimation openingAnimation = OpeningAnimation.defaultAnimation();
         int i = 0;

         for (Object rawValue : (List)object) {
            if (i == 3) {
               break;
            }

            String value = rawValue.toString();
            PhaseType phaseType = PhaseType.values()[i];
            int separatorIndex = value.indexOf(";");
            if (separatorIndex == -1) {
               throw new WarningException("Expected at least \"1\" argument, founded \"0\" arguments");
            }

            String id = value.substring(0, separatorIndex).trim().toUpperCase();
            AnimationPhase openingPhaseType = null;

            try {
               openingPhaseType = AnimationsRegistry.getInstance().getPhaseById(phaseType, id).get();
            } catch (NoSuchElementException e) {
               throw new WarningException("Unknown opening phase \"" + phaseType.name() + "\", \"" + id + "\"");
            }

            String rawData = value.substring(separatorIndex + 1, value.length());
            OpeningPhaseData data = null;
            if (openingPhaseType.getLegacyDataClass().equals(EmptyOpeningPhaseData.class)) {
               data = new EmptyOpeningPhaseData();
            } else {
               ConfigurationTokenizer tokenizer = new ConfigurationTokenizer(new ConfigurationTokenizer(rawData, ";").nextTokenAsObject(), ";");

               try {
                  data = (OpeningPhaseData)reader.unserializeObject(
                     ConfigurationUtilities.GENERIC_SERIALIZATION, openingPhaseType.getLegacyDataClass(), tokenizer
                  );
               } catch (WarningException ex) {
                  data = ((AnimationPhaseImpl)openingPhaseType).createDefaultLegacyData();
                  reader.getLogger()
                     .warningAndFlush(
                        reader.getCachedLoggedPath(),
                        " When using \"OpeningAnimationWrapper\" adapter. " + ex.getMessage() + " Ignoring yaml value and using predefined value."
                     );
               }
            }

            OpeningPhase phase = new OpeningPhase(openingPhaseType, data);
            if (i == 0) {
               openingAnimation.setPreOpenPhase(phase);
            } else if (i == 1) {
               openingAnimation.setOpenPhase(phase);
            } else if (i == 2) {
               openingAnimation.setDisplayPhase(phase);
            }

            i++;
         }

         return new OpeningAnimationWrapper(openingAnimation);
      } else if (!(object instanceof ConfigurationSection section)) {
         throw new WarningException("Expected \"Yaml Section\", founded \"" + object.getClass().getSimpleName() + "\"");
      } else {
         List<OpeningAnimation> animations = new ArrayList<>();

         for (String key : section.getKeys(false)) {
            Object child = section.get(key);
            if (!(child instanceof List)) {
               throw new WarningException("Expected \"List\", founded \"" + child.getClass().getSimpleName() + "\"");
            }

            if (((List)child).size() != 3) {
               throw new WarningException("Expected \"3\" phases, founded \"" + ((List)child).size() + "\" arguments");
            }

            OpeningAnimation openingAnimation = OpeningAnimation.defaultAnimation();
            int i = 0;

            for (Object rawValue : (List)child) {
               if (i == 3) {
                  break;
               }

               String value = rawValue.toString();
               PhaseType phaseType = PhaseType.values()[i];
               int separatorIndex = value.indexOf(";");
               if (separatorIndex == -1) {
                  throw new WarningException("Expected at least \"1\" argument, founded \"0\" arguments");
               }

               String id = value.substring(0, separatorIndex).trim().toUpperCase();
               AnimationPhase openingPhaseType = null;

               try {
                  openingPhaseType = AnimationsRegistry.getInstance().getPhaseById(phaseType, id).get();
               } catch (NoSuchElementException e) {
                  throw new WarningException("Unknown opening phase \"" + phaseType.name() + "\", \"" + id + "\"");
               }

               String rawData = value.substring(separatorIndex + 1);
               OpeningPhaseData data = new EmptyOpeningPhaseData();
               Class<?> legacyDataSupport = openingPhaseType.getLegacyDataClass();
               if (legacyDataSupport != null && !legacyDataSupport.equals(EmptyOpeningPhaseData.class)) {
                  ConfigurationTokenizer tokenizer = new ConfigurationTokenizer(new ConfigurationTokenizer(rawData, ";").nextTokenAsObject(), ";");

                  try {
                     data = (OpeningPhaseData)reader.unserializeObject(
                        ConfigurationUtilities.GENERIC_SERIALIZATION, openingPhaseType.getLegacyDataClass(), tokenizer
                     );
                  } catch (WarningException ex) {
                     data = ((AnimationPhaseImpl)openingPhaseType).createDefaultLegacyData();
                     reader.getLogger()
                        .warningAndFlush(
                           reader.getCachedLoggedPath(),
                           " When using \"OpeningAnimationWrapper\" adapter. " + ex.getMessage() + " Ignoring yaml value and using predefined value."
                        );
                  }
               }

               OpeningPhase phase = new OpeningPhase(openingPhaseType, data);
               if (i == 0) {
                  openingAnimation.setPreOpenPhase(phase);
               } else if (i == 1) {
                  openingAnimation.setOpenPhase(phase);
               } else if (i == 2) {
                  openingAnimation.setDisplayPhase(phase);
               }

               i++;
            }

            animations.add(openingAnimation);
         }

         if (animations.isEmpty()) {
            animations.add(OpeningAnimation.defaultAnimation());
         }

         return new OpeningAnimationWrapper(animations);
      }
   }

   public Object exportLegacy(ConfigurationWriter writer, OpeningAnimationWrapper animations) throws ErrorException {
      Map<String, Object> map = new LinkedHashMap<>();
      int i = 0;

      for (OpeningAnimation animation : animations.list()) {
         List<String> serialized = new ArrayList<>();

         for (OpeningPhase phase : animation.map().values()) {
            try {
               OpeningPhaseData data = this.toLegacyData(phase.getProperties());
               serialized.add(
                  String.format(
                     "%s;{%s}",
                     phase.getPhase().getId(),
                     writer.serializeObject(ConfigurationUtilities.GENERIC_SERIALIZATION, data.getClass(), data, false, false)
                  )
               );
            } catch (Exception e) {
               throw new ErrorException(e.getMessage());
            }
         }

         map.put(String.valueOf(i++), serialized);
      }

      return map;
   }

   @Deprecated
   private OpeningPhaseData toLegacyData(AnimationProperties properties) {
      String legacyType = properties.getProperty("legacyType", String.class);
      if (legacyType != null) {
         switch (legacyType.toLowerCase()) {
            case "color":
               return new ColoredOpeningPhaseData(properties.getColorOrDefault("color", Color.WHITE));
            case "skull":
               return new ColoredSkullOpeningPhaseData(
                  properties.getColorOrDefault("color", Color.WHITE),
                  properties.getPropertyOrDefault("textureSkull", "f98bc63f05f6378bf29ef10e3d82acb3ceb73a720bf80f30bc576d0ad8c40cfb")
               );
            case "key":
               return new KeybasedOpeningPhaseData(
                  properties.getPropertyOrDefault("useKeyAsModel", false),
                  properties.getEnumOrDefault("alignment", KeybasedOpeningPhaseData.KeyAlignment.VERTICAL),
                  properties.getPropertyOrDefault("offsetX", 0).intValue(),
                  properties.getPropertyOrDefault("offsetY", 0).intValue(),
                  properties.getPropertyOrDefault("offsetZ", 0).intValue()
               );
         }
      }

      return new EmptyOpeningPhaseData();
   }
}
