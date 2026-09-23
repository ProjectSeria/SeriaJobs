package id.seria.gacha.sdk.platforms.server.internal;

public class MiniMessageCheck {
   public MiniMessageCheck() {
   }

   public static boolean isSyntaxCompatible(String input) {
      if (input != null && !input.isEmpty()) {
         int len = input.length();

         for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            if (c == '<' && i + 1 < len) {
               char next = input.charAt(i + 1);
               if (isAsciiLetter(next)
                  || next == '#'
                  || next == '/' && i + 2 < len && isAsciiLetter(input.charAt(i + 2))
                  || next == '!' && i + 2 < len && isAsciiLetter(input.charAt(i + 2))) {
                  for (int j = i + 2; j < len; j++) {
                     char end = input.charAt(j);
                     if (end == '>') {
                        return true;
                     }

                     if (end == '<') {
                        break;
                     }
                  }
               }

               if (next != '<' && next == '>') {
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean isAsciiLetter(char c) {
      return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
   }
}
