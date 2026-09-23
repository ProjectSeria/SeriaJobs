package id.seria.gacha.api.sdk.logger;

public interface Logger {
   void warn(String var1);

   void info(String var1);

   void error(String var1);

   void warn(String var1, Throwable var2);

   void info(String var1, Throwable var2);

   void error(String var1, Throwable var2);

   void debug(String var1);

   void debug(String var1, Throwable var2);
}
