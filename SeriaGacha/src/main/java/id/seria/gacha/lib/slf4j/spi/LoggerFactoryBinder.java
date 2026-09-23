package id.seria.gacha.lib.slf4j.spi;

import id.seria.gacha.lib.slf4j.ILoggerFactory;

public interface LoggerFactoryBinder {
   ILoggerFactory getLoggerFactory();

   String getLoggerFactoryClassStr();
}
