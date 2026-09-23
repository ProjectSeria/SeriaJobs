package id.seria.gacha.lib.common.uicomponents.newest.actions.builder;

import id.seria.gacha.lib.common.utils.actions.ActionType;
import id.seria.gacha.lib.common.utils.actions.ActionsFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;

public class ActionBuilderRegistry {
   private final ActionsFactory factory;
   private final Map<String, ActionBuilderFactory> builders;

   public ActionBuilderRegistry(ActionsFactory factory) {
      this.factory = factory;
      this.builders = new HashMap<>();
   }

   public ActionBuilderRegistry register(String typeName, ActionBuilderFactory builder) {
      this.builders.put(typeName.toUpperCase(), builder);
      return this;
   }

   public ActionBuilderRegistry registerInstant(String typeName) {
      this.builders.put(typeName.toUpperCase(), null);
      return this;
   }

   public void unregister(String typeName) {
      this.builders.remove(typeName.toUpperCase());
   }

   public boolean isRegistered(String typeName) {
      return this.builders.containsKey(typeName.toUpperCase());
   }

   public boolean isInstant(String typeName) {
      return this.isRegistered(typeName) && this.builders.get(typeName.toUpperCase()) == null;
   }

   public ActionBuilderFactory getBuilderOrNull(String typeName) {
      return this.builders.get(typeName.toUpperCase());
   }

   public List<ActionType> getBuildableTypes() {
      List<ActionType> types = new ArrayList<>();

      for (ActionType type : this.factory.getRegisteredTypes()) {
         if (!type.isLegacy() && this.isRegistered(type.getName())) {
            types.add(type);
         }
      }

      types.sort(Comparator.comparing(ActionType::getName));
      return types;
   }

   @Generated
   public ActionsFactory getFactory() {
      return this.factory;
   }

   @Generated
   public Map<String, ActionBuilderFactory> getBuilders() {
      return this.builders;
   }
}
