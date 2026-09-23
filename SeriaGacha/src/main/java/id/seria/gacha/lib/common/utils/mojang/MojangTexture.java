package id.seria.gacha.lib.common.utils.mojang;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.Generated;

public class MojangTexture {
   private static final String MINESKIN_API = "https://api.mineskin.org/generate/url";
   private static final Gson gson = new GsonBuilder().create();
   private final UUID uniqueId;
   private final String value;
   private final String signature;

   public static CompletableFuture<Optional<MojangTexture>> fetchTextureFromUsername(String username) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Optional<UUID> uuid = fetchUUIDFromUsername(username).get();
            if (!uuid.isPresent()) {
               return Optional.ofNullable(null);
            }

            Optional<MojangTexture> texture = fetchTextureFromUUID(uuid.get()).get();
            return !texture.isPresent() ? Optional.ofNullable(null) : Optional.of(texture.get());
         } catch (Exception ex) {
            throw new CompletionException(ex);
         }
      });
   }

   public static CompletableFuture<Optional<MojangTexture>> fetchTextureFromUUID(UUID uuid) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            String id = uuid.toString().replace("-", "");
            JsonObject jsonObject = readJsonFromUrl("https://sessionserver.mojang.com/session/minecraft/profile/" + id + "?unsigned=false");
            if (jsonObject == null) {
               return Optional.ofNullable(null);
            }

            JsonObject properties = jsonObject.getAsJsonArray("properties").get(0).getAsJsonObject();
            String value = properties.get("value").getAsString();
            String signature = properties.get("signature").getAsString();
            return Optional.of(new MojangTexture(uuid, value, signature));
         } catch (Exception ex) {
            throw new CompletionException(ex);
         }
      });
   }

   public static CompletableFuture<Optional<UUID>> fetchUUIDFromUsername(String username) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            JsonObject jsonObject = readJsonFromUrl("https://api.mojang.com/users/profiles/minecraft/" + username);
            if (jsonObject == null) {
               return Optional.ofNullable(null);
            }

            String id = jsonObject.get("id").getAsString();
            return Optional.of(StringUtil.toUUID(id));
         } catch (Exception ex) {
            throw new CompletionException(ex);
         }
      });
   }

   public static CompletableFuture<MojangTexture> fetchTextureFromMineskinUrl(String url) {
      return CompletableFuture.supplyAsync(() -> {
         HttpURLConnection connection = null;

         MojangTexture var10;
         try {
            URL target = new URL("https://api.mineskin.org/generate/url");
            connection = (HttpURLConnection)target.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(5000);
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            try {
               out.writeBytes("url=" + URLEncoder.encode(url, "UTF-8"));
            } catch (Throwable t$) {
               try {
                  out.close();
               } catch (Throwable x2) {
                  t$.addSuppressed(x2);
               }

               throw t$;
            }

            out.close();
            if (connection.getResponseCode() != 200) {
               throw new CompletionException(new MojangTexture.HttpException(connection.getResponseCode()));
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));

            try {
               JsonObject jsonObject = (JsonObject)gson.fromJson(reader, JsonObject.class);
               JsonObject data = jsonObject.getAsJsonObject("data");
               JsonObject texture = data.getAsJsonObject("texture");
               String id = data.get("uuid").getAsString();
               String value = texture.get("value").getAsString();
               String signature = texture.get("signature").getAsString();
               var10 = new MojangTexture(StringUtil.toUUID(id), value, signature);
            } catch (Throwable t$) {
               try {
                  reader.close();
               } catch (Throwable x2) {
                  t$.addSuppressed(x2);
               }

               throw t$;
            }

            reader.close();
         } catch (Exception e) {
            throw new CompletionException(e);
         } finally {
            if (connection != null) {
               connection.disconnect();
            }
         }

         return var10;
      });
   }

   private static String readAll(Reader rd) throws IOException {
      StringBuilder sb = new StringBuilder();

      int cp;
      while ((cp = rd.read()) != -1) {
         sb.append((char)cp);
      }

      return sb.toString();
   }

   private static JsonObject readJsonFromUrl(String url) throws IOException {
      InputStream is = new URL(url).openStream();

      JsonObject var7;
      label43: {
         try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            if (jsonText.length() == 0) {
               var7 = null;
               break label43;
            }

            var7 = (JsonObject)gson.fromJson(jsonText, JsonObject.class);
         } catch (Throwable var6) {
            if (is != null) {
               try {
                  is.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (is != null) {
            is.close();
         }

         return var7;
      }

      if (is != null) {
         is.close();
      }

      return var7;
   }

   public String extractTextureUrl() {
      String skinJson = new String(Base64.getDecoder().decode(this.value));
      JsonObject skinObject = (JsonObject)gson.fromJson(skinJson, JsonObject.class);
      return skinObject.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
   }

   @Generated
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   @Generated
   public String getValue() {
      return this.value;
   }

   @Generated
   public String getSignature() {
      return this.signature;
   }

   @Generated
   private MojangTexture(UUID uniqueId, String value, String signature) {
      this.uniqueId = uniqueId;
      this.value = value;
      this.signature = signature;
   }

   public static class HttpException extends Exception {
      private final int responseCode;

      public HttpException(int responseCode) {
         super("Server returned HTTP response code: " + responseCode);
         this.responseCode = responseCode;
      }

      @Generated
      public int getResponseCode() {
         return this.responseCode;
      }
   }
}
