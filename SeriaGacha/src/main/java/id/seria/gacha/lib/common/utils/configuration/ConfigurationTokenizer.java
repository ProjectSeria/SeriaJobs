package id.seria.gacha.lib.common.utils.configuration;

import lombok.Generated;
import org.apache.commons.lang.Validate;

public class ConfigurationTokenizer {
   private String str;
   private String separator;

   public ConfigurationTokenizer(String str, String separator) {
      this.str = str;
      this.separator = separator;
   }

   public String nextToken() {
      synchronized (this.str) {
         if (!this.hasToken()) {
            try {
               throw new Exception("No more tokens");
            } catch (Exception e) {
               e.printStackTrace();
            }
         }

         Validate.isTrue(this.hasToken(), "No more tokens");
         int index = this.str.indexOf(this.separator);
         if (index == -1) {
            String token = this.str;
            this.str = "";
            return token;
         } else {
            String token = this.str.substring(0, index);
            this.str = this.str.substring(index + this.separator.length(), this.str.length());
            return token;
         }
      }
   }

   public ConfigurationTokenizer tokenObject() {
      return new ConfigurationTokenizer(this.nextTokenAsObject(), this.separator);
   }

   public ConfigurationTokenizer tokenArray() {
      return new ConfigurationTokenizer(this.nextTokenAsArray(), this.separator);
   }

   public String nextTokenAsObject() {
      synchronized (this.str) {
         if (!this.hasToken()) {
            try {
               throw new Exception();
            } catch (Exception e) {
               e.printStackTrace();
            }
         }

         Validate.isTrue(this.hasToken(), "No more tokens");
         Validate.isTrue(this.str.startsWith("{"), "No object token found");
         int depth = 0;
         int index = -1;
         boolean endsWithSemiColon = false;

         for (int i = 1; i < this.str.length(); i++) {
            if (this.str.charAt(i) == '{') {
               depth++;
            } else if (this.str.charAt(i) == '}') {
               if (depth == 0) {
                  index = i;
                  if (i + 1 < this.str.length() && this.str.charAt(i + 1) == ';') {
                     endsWithSemiColon = true;
                  }
                  break;
               }

               depth--;
            }
         }

         Validate.isTrue(index != -1, "No end of object found");
         String token = this.str.substring(1, index);
         if (endsWithSemiColon) {
            this.str = this.str.substring(index + 2);
         } else {
            this.str = this.str.substring(index + 1);
         }

         return token;
      }
   }

   public String nextTokenAsArray() {
      synchronized (this.str) {
         if (!this.hasToken()) {
            try {
               throw new Exception();
            } catch (Exception e) {
               e.printStackTrace();
            }
         }

         Validate.isTrue(this.hasToken(), "No more tokens");
         Validate.isTrue(this.str.startsWith("["), "No array token founded");
         String temp = this.str.substring(1, this.str.length());
         int index = temp.indexOf("];");
         if (index == -1) {
            index = temp.indexOf("]");
            Validate.isTrue(index != -1, "No end of array founded");
            String token = this.str.substring(1, index + 1);
            this.str = "";
            return token;
         } else {
            String token = this.str.substring(1, index + 1);
            this.str = this.str.substring(index + this.separator.length() + 2, this.str.length());
            return token;
         }
      }
   }

   public boolean hasToken() {
      return !this.str.isEmpty();
   }

   public static void main(String[] args) {
      String str = "{Hello;World};{Good;[Array Start;Example;Array End];Bye}";
      ConfigurationTokenizer tokenizer = new ConfigurationTokenizer(str, ";");
      System.out.println("" + tokenizer.nextTokenAsObject());
      System.out.println("" + tokenizer.nextTokenAsObject());
   }

   @Generated
   public String getStr() {
      return this.str;
   }

   @Generated
   public String getSeparator() {
      return this.separator;
   }
}
