package id.seria.gacha.lib.slf4j.spi;

import id.seria.gacha.lib.slf4j.IMarkerFactory;

public interface MarkerFactoryBinder {
   IMarkerFactory getMarkerFactory();

   String getMarkerFactoryClassStr();
}
