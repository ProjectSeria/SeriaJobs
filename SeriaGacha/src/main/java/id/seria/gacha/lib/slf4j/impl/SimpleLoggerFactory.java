package id.seria.gacha.lib.slf4j.impl;

import id.seria.gacha.lib.slf4j.ILoggerFactory;
import id.seria.gacha.lib.slf4j.Logger;
import java.util.HashMap;
import java.util.Map;

public class SimpleLoggerFactory implements ILoggerFactory {
   static final SimpleLoggerFactory INSTANCE = new SimpleLoggerFactory();
   Map loggerMap = new HashMap();

   public SimpleLoggerFactory() {
   }

   public Logger getLogger(String name) {
      Logger slogger = null;
      synchronized (this) {
         slogger = (Logger)this.loggerMap.get(name);
         if (slogger == null) {
            slogger = new SimpleLogger(name);
            this.loggerMap.put(name, slogger);
         }

         return slogger;
      }
   }
}
