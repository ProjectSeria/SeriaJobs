package id.seria.gacha.lib.common.utils.api.authentication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import lombok.Generated;

public class AuthCache {
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final File file;

   public AuthCache(ServerPlugin plugin) {
      this.file = new File(
         plugin.getDataFolder().getParentFile(),
         String.format("PhoenixPlugins/%s_auth.json", StringUtil.camelCaseToUnder(plugin.getName().replace(" ", "_").toLowerCase()))
      );
      File legacyFile = new File(plugin.getDataFolder(), "auth_data.json");
      if (legacyFile.exists()) {
         if (!this.file.getParentFile().exists()) {
            this.file.getParentFile().mkdirs();
         }

         legacyFile.renameTo(this.file);
      }
   }

   public void save(String token, String gracePeriodSignature, long gracePeriodExpiry) {
      AuthCache.CacheData cacheData = new AuthCache.CacheData(token, gracePeriodSignature, gracePeriodExpiry);

      try {
         if (!this.file.exists()) {
            File parentDirectory = this.file.getParentFile();
            if (parentDirectory != null) {
               parentDirectory.mkdirs();
            }

            this.file.createNewFile();
         }

         BufferedWriter writer = new BufferedWriter(new FileWriter(this.file));

         try {
            writer.write(this.gson.toJson(cacheData));
         } finally {
            if (Collections.singletonList(writer).get(0) != null) {
               writer.close();
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public AuthCache.CacheData read() {
      try {
         BufferedReader reader = new BufferedReader(new FileReader(this.file));

         AuthCache.CacheData var2;
         try {
            var2 = (AuthCache.CacheData)this.gson.fromJson(reader, AuthCache.CacheData.class);
         } catch (Throwable var5) {
            try {
               reader.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         reader.close();
         return var2;
      } catch (FileNotFoundException var6) {
      } catch (IOException e) {
         e.printStackTrace();
      }

      return null;
   }

   @Generated
   public Gson getGson() {
      return this.gson;
   }

   @Generated
   public File getFile() {
      return this.file;
   }

   public static class CacheData {
      private final String signature;
      private final String gracePeriodSignature;
      private final long gracePeriodExpiry;

      @Generated
      public String getSignature() {
         return this.signature;
      }

      @Generated
      public String getGracePeriodSignature() {
         return this.gracePeriodSignature;
      }

      @Generated
      public long getGracePeriodExpiry() {
         return this.gracePeriodExpiry;
      }

      @Generated
      public CacheData(String signature, String gracePeriodSignature, long gracePeriodExpiry) {
         this.signature = signature;
         this.gracePeriodSignature = gracePeriodSignature;
         this.gracePeriodExpiry = gracePeriodExpiry;
      }
   }
}
