package id.seria.gacha.api.sdk.internal;

import id.seria.gacha.api.sdk.plugin.Plugin;
import id.seria.gacha.api.sdk.utilities.Validate;

public interface LifecycleObject {
   static void massInit(LifecycleObject... objects) throws Exception {
      for (LifecycleObject object : objects) {
         Validate.isFalse(object instanceof Plugin, "Plugin not allowed");
         object.init();
      }
   }

   static void massLoad(LifecycleObject... objects) throws Exception {
      for (LifecycleObject object : objects) {
         Validate.isFalse(object instanceof Plugin, "Plugin not allowed");
         object.load();
      }
   }

   static void massUnload(LifecycleObject... objects) throws Exception {
      for (LifecycleObject object : objects) {
         Validate.isFalse(object instanceof Plugin, "Plugin not allowed");
         object.unload();
      }
   }

   static void massReload(LifecycleObject... objects) throws Exception {
      for (LifecycleObject object : objects) {
         Validate.isFalse(object instanceof Plugin, "Plugin not allowed");
         object.reload();
      }
   }

   void init() throws Exception;

   void load() throws Exception;

   void unload() throws Exception;

   void reload() throws Exception;
}
