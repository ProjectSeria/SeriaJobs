package id.seria.gacha.lib.xseries.profiles.exceptions;

public class MojangAPIException extends ProfileException {
   public MojangAPIException(String message) {
      super(message);
   }

   public MojangAPIException(String message, Throwable cause) {
      super(message, cause);
   }
}
