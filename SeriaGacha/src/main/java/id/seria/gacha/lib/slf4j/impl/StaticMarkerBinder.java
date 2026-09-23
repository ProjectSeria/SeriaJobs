package id.seria.gacha.lib.slf4j.impl;

import id.seria.gacha.lib.slf4j.IMarkerFactory;
import id.seria.gacha.lib.slf4j.helpers.BasicMarkerFactory;
import id.seria.gacha.lib.slf4j.spi.MarkerFactoryBinder;

public class StaticMarkerBinder implements MarkerFactoryBinder {
   public static final StaticMarkerBinder SINGLETON = new StaticMarkerBinder();
   public static StaticMarkerBinder getSingleton() {
      return SINGLETON;
   }
   final IMarkerFactory markerFactory = new BasicMarkerFactory();

   private StaticMarkerBinder() {
   }

   public IMarkerFactory getMarkerFactory() {
      return this.markerFactory;
   }

   public String getMarkerFactoryClassStr() {
      return BasicMarkerFactory.class.getName();
   }
}
