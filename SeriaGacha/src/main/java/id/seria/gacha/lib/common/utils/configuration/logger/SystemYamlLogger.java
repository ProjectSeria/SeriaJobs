package id.seria.gacha.lib.common.utils.configuration.logger;

public class SystemYamlLogger extends ConfigYamlLogger {
   public SystemYamlLogger() {
   }

   @Override
   public void flushRaw(String message) {
      System.out.println(message);
   }
}
