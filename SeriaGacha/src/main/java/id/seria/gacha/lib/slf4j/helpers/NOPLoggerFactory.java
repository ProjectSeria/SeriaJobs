package id.seria.gacha.lib.slf4j.helpers;

import id.seria.gacha.lib.slf4j.ILoggerFactory;
import id.seria.gacha.lib.slf4j.Logger;

public class NOPLoggerFactory implements ILoggerFactory {
   public NOPLoggerFactory() {
   }

   @Override
   public Logger getLogger(String name) {
      return NOPLogger.NOP_LOGGER;
   }
}
