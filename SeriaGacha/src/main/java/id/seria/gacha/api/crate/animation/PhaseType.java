package id.seria.gacha.api.crate.animation;

public enum PhaseType {
   PRE_OPENING("pre-open"),
   OPENING("open"),
   DISPLAYING("display");

   private final String id;

   PhaseType(String key) {
      this.id = key;
   }

   public String getId() {
      return this.id;
   }
}
