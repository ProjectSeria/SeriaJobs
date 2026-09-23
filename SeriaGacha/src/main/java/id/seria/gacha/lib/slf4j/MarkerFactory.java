package id.seria.gacha.lib.slf4j;

import id.seria.gacha.lib.slf4j.helpers.BasicMarkerFactory;
import id.seria.gacha.lib.slf4j.helpers.Util;
import id.seria.gacha.lib.slf4j.impl.StaticMarkerBinder;

public class MarkerFactory {
   static IMarkerFactory MARKER_FACTORY;

   private MarkerFactory() {
   }

   private static IMarkerFactory bwCompatibleGetMarkerFactoryFromBinder() throws NoClassDefFoundError {
      try {
         return StaticMarkerBinder.getSingleton().getMarkerFactory();
      } catch (NoSuchMethodError nsme) {
         return StaticMarkerBinder.SINGLETON.getMarkerFactory();
      }
   }

   public static Marker getMarker(String name) {
      return MARKER_FACTORY.getMarker(name);
   }

   public static Marker getDetachedMarker(String name) {
      return MARKER_FACTORY.getDetachedMarker(name);
   }

   public static IMarkerFactory getIMarkerFactory() {
      return MARKER_FACTORY;
   }

   static {
      try {
         MARKER_FACTORY = bwCompatibleGetMarkerFactoryFromBinder();
      } catch (NoClassDefFoundError e) {
         MARKER_FACTORY = new BasicMarkerFactory();
      } catch (Exception e) {
         Util.report("Unexpected failure while binding MarkerFactory", e);
      }
   }
}
