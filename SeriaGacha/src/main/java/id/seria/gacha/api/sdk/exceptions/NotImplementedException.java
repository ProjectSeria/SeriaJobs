package id.seria.gacha.api.sdk.exceptions;

public class NotImplementedException extends RuntimeException {
   public NotImplementedException() {
   }

   public NotImplementedException(String reason) {
      super(reason);
   }
}
