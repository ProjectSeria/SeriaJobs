package id.seria.gacha.sdk.core.internal;

public class NumberModifierParser {
   private String num;
   private boolean suffixes;
   private double mod;

   public NumberModifierParser(String num, boolean suffixes) {
      this.num = num;
      this.suffixes = suffixes;
   }

   public String getNum() {
      return this.num;
   }

   public double getMod() {
      return this.mod;
   }

   public NumberModifierParser invoke() {
      this.mod = 1.0;
      if (this.suffixes) {
         switch (this.num.charAt(this.num.length() - 1)) {
            case 'K':
            case 'k':
               this.mod = 1000.0;
               this.num = this.num.substring(0, this.num.length() - 1);
               break;
            case 'M':
            case 'm':
               this.mod = 1000000.0;
               this.num = this.num.substring(0, this.num.length() - 1);
         }
      }

      return this;
   }
}
