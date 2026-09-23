package id.seria.gacha.lib.slf4j.helpers;

import id.seria.gacha.lib.slf4j.Logger;
import id.seria.gacha.lib.slf4j.LoggerFactory;
import java.io.ObjectStreamException;
import java.io.Serializable;

abstract class NamedLoggerBase implements Logger, Serializable {
   private static final long serialVersionUID = 7535258609338176893L;
   protected String name;

   NamedLoggerBase() {
   }

   @Override
   public String getName() {
      return this.name;
   }

   protected Object readResolve() throws ObjectStreamException {
      return LoggerFactory.getLogger(this.getName());
   }
}
