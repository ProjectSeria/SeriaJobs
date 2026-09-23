package id.seria.gacha.lib.book;

import id.seria.gacha.lib.book.internals.NmsUtil;

public class UnsupportedVersionException extends RuntimeException {
   private final String version;

   public UnsupportedVersionException(Exception e) {
      super("Error while executing reflections, submit to developers the following log (version: " + NmsUtil.version + ")", e);
      this.version = NmsUtil.version;
   }

   public String getVersion() {
      return this.version;
   }
}
