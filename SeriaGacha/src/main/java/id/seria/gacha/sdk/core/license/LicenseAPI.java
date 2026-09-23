package id.seria.gacha.sdk.core.license;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import id.seria.gacha.sdk.utilities.api.ProductVersion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class LicenseAPI {
   private static final Gson GSON = new GsonBuilder().registerTypeAdapter(ProductVersion.class, new JsonDeserializer<ProductVersion>() {
      public ProductVersion deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         return new ProductVersion(json.getAsString());
      }
   }).create();
   private static final String API_URL = "https://api.phoenixplugins.com/v4";

   public LicenseAPI() {
   }

   public static void main(String[] args) throws IOException, LicenseAPI.ApiException {
      BuildInfo info = new BuildInfo("123456hash", "1.0.0", "dev", "10");
      ServerMetrics metrics = new ServerMetrics(UUID.randomUUID().toString(), "1.21.4-SNAPSHOT", "Paper", "play.phoenixplugins.com", 0);
      AuthenticationResponse r = login("phoenix-crates", "standard", "OU50X-Q5IWB-OC2V7-F3332", null, info);
      System.out.println(r);
      RenewResponse response = renew(r.getAuthToken());
      System.out.println(response);

      try {
         metrics(response.getAuthToken(), System.currentTimeMillis() - 10000L, metrics);
      } catch (Exception ex) {
         ex.printStackTrace();
      }

      try {
         logout(response.getAuthToken());
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   public static AuthenticationResponse login(
      @NonNull String productId, @NonNull String planId, @NonNull String license, @Nullable String signature, @Nullable BuildInfo info
   ) throws LicenseAPI.ApiException, IOException {
      if (productId == null) {
         throw new NullPointerException("productId is marked non-null but is null");
      }

      if (planId == null) {
         throw new NullPointerException("planId is marked non-null but is null");
      }

      if (license == null) {
         throw new NullPointerException("license is marked non-null but is null");
      }

      HttpURLConnection connection = null;

      try {
         URL url = new URL("https://api.phoenixplugins.com/v4/licenses/login");
         connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("POST");
         connection.setConnectTimeout(5000);
         connection.setReadTimeout(5000);
         connection.setDoOutput(true);
         connection.setRequestProperty("Content-Type", "application/json");
         JsonObject requestBody = new JsonObject();
         requestBody.addProperty("product", productId);
         requestBody.addProperty("plan", planId);
         requestBody.addProperty("license", license);
         if (signature != null) {
            requestBody.addProperty("signature", signature);
         }

         if (info != null) {
            JsonElement infoJson = GSON.toJsonTree(info);
            requestBody.add("info", infoJson);
         }

         OutputStream os = connection.getOutputStream();

         try {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input);
         } catch (Throwable var17) {
            if (os != null) {
               try {
                  os.close();
               } catch (Throwable var16) {
                  var17.addSuppressed(var16);
               }
            }

            throw var17;
         }

         if (os != null) {
            os.close();
         }

         int status = connection.getResponseCode();
         String responseJson = readResponseBody(connection);
         JsonObject json = JsonParser.parseString(responseJson).getAsJsonObject();
         if (status == 200 && json.has("body")) {
            return (AuthenticationResponse)GSON.fromJson(json.get("body"), AuthenticationResponse.class);
         }

         String message = json.has("message") ? json.get("message").getAsString() : "No message returned from API";
         switch (status) {
            case 400:
               throw new LicenseAPI.BadRequestException(message);
            case 403:
               throw new LicenseAPI.ForbiddenException(message);
            case 404:
               throw new LicenseAPI.NotFoundException(message);
            case 500:
               throw new LicenseAPI.InternalServerErrorException(message);
            default:
               throw new LicenseAPI.UnexpectedApiException(status, message);
         }
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   public static RenewResponse renew(@Nullable String signature) throws LicenseAPI.ApiException, IOException {
      HttpURLConnection connection = null;

      try {
         URL url = new URL("https://api.phoenixplugins.com/v4/licenses/renew");
         connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("POST");
         connection.setConnectTimeout(5000);
         connection.setReadTimeout(5000);
         connection.setDoOutput(true);
         connection.setRequestProperty("Content-Type", "application/json");
         JsonObject requestBody = new JsonObject();
         requestBody.addProperty("signature", signature);
         OutputStream os = connection.getOutputStream();

         try {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input);
         } catch (Throwable var13) {
            if (os != null) {
               try {
                  os.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }
            }

            throw var13;
         }

         if (os != null) {
            os.close();
         }

         int status = connection.getResponseCode();
         String responseJson = readResponseBody(connection);
         JsonObject json = JsonParser.parseString(responseJson).getAsJsonObject();
         if (status == 200 && json.has("body")) {
            return (RenewResponse)GSON.fromJson(json.get("body"), RenewResponse.class);
         }

         String message = json.has("message") ? json.get("message").getAsString() : "No message returned from API";
         switch (status) {
            case 400:
               throw new LicenseAPI.BadRequestException(message);
            case 403:
               throw new LicenseAPI.ForbiddenException(message);
            case 404:
               throw new LicenseAPI.NotFoundException(message);
            case 500:
               throw new LicenseAPI.InternalServerErrorException(message);
            default:
               throw new LicenseAPI.UnexpectedApiException(status, message);
         }
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   public static void logout(@NonNull String signature) throws LicenseAPI.ApiException, IOException {
      if (signature == null) {
         throw new NullPointerException("signature is marked non-null but is null");
      }

      HttpURLConnection connection = null;

      try {
         URL url = new URL("https://api.phoenixplugins.com/v4/licenses/logout");
         connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("POST");
         connection.setConnectTimeout(5000);
         connection.setReadTimeout(5000);
         connection.setDoOutput(true);
         connection.setRequestProperty("Content-Type", "application/json");
         JsonObject requestBody = new JsonObject();
         requestBody.addProperty("signature", signature);
         OutputStream os = connection.getOutputStream();

         try {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input);
         } catch (Throwable var13) {
            if (os != null) {
               try {
                  os.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }
            }

            throw var13;
         }

         if (os != null) {
            os.close();
         }

         int status = connection.getResponseCode();
         String responseJson = readResponseBody(connection);
         JsonObject json = JsonParser.parseString(responseJson).getAsJsonObject();
         if (status != 200 || !json.has("body")) {
            String message = json.has("message") ? json.get("message").getAsString() : "No message returned from API";
            switch (status) {
               case 400:
                  throw new LicenseAPI.BadRequestException(message);
               case 403:
                  throw new LicenseAPI.ForbiddenException(message);
               case 404:
                  throw new LicenseAPI.NotFoundException(message);
               case 500:
                  throw new LicenseAPI.InternalServerErrorException(message);
               default:
                  throw new LicenseAPI.UnexpectedApiException(status, message);
            }
         }
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   public static void metrics(@NonNull String signature, long timestamp, ServerMetrics data) throws LicenseAPI.ApiException, IOException {
      if (signature == null) {
         throw new NullPointerException("signature is marked non-null but is null");
      }

      HttpURLConnection connection = null;

      try {
         URL url = new URL("https://api.phoenixplugins.com/v4/metrics/data");
         connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("POST");
         connection.setConnectTimeout(5000);
         connection.setReadTimeout(5000);
         connection.setDoOutput(true);
         connection.setRequestProperty("Content-Type", "application/json");
         JsonObject requestBody = new JsonObject();
         requestBody.addProperty("signature", signature);
         requestBody.addProperty("timestamp", timestamp);
         JsonElement metricsJson = GSON.toJsonTree(data);
         requestBody.add("data", metricsJson);
         OutputStream os = connection.getOutputStream();

         try {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input);
         } catch (Throwable var17) {
            if (os != null) {
               try {
                  os.close();
               } catch (Throwable var16) {
                  var17.addSuppressed(var16);
               }
            }

            throw var17;
         }

         if (os != null) {
            os.close();
         }

         int status = connection.getResponseCode();
         String responseJson = readResponseBody(connection);
         JsonObject json = JsonParser.parseString(responseJson).getAsJsonObject();
         if (status != 200 || !json.has("body")) {
            String message = json.has("message") ? json.get("message").getAsString() : "No message returned from API";
            switch (status) {
               case 400:
                  throw new LicenseAPI.BadRequestException(message);
               case 403:
                  throw new LicenseAPI.ForbiddenException(message);
               case 404:
                  throw new LicenseAPI.NotFoundException(message);
               case 500:
                  throw new LicenseAPI.InternalServerErrorException(message);
               default:
                  throw new LicenseAPI.UnexpectedApiException(status, message);
            }
         }
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   private static String readResponseBody(HttpURLConnection connection) throws IOException {
      BufferedReader reader = new BufferedReader(
         new InputStreamReader(connection.getResponseCode() < 400 ? connection.getInputStream() : connection.getErrorStream(), StandardCharsets.UTF_8)
      );

      String var4;
      try {
         StringBuilder content = new StringBuilder();

         String line;
         while ((line = reader.readLine()) != null) {
            content.append(line);
         }

         var4 = content.toString();
      } catch (Throwable var6) {
         try {
            reader.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      reader.close();
      return var4;
   }

   public abstract static class ApiException extends Exception {
      public ApiException(String message) {
         super(message);
      }
   }

   public static class BadRequestException extends LicenseAPI.ApiException {
      public BadRequestException(String message) {
         super(message);
      }
   }

   public static class ForbiddenException extends LicenseAPI.ApiException {
      public ForbiddenException(String message) {
         super(message);
      }
   }

   public static class InternalServerErrorException extends LicenseAPI.ApiException {
      public InternalServerErrorException(String message) {
         super(message);
      }
   }

   public static class NotFoundException extends LicenseAPI.ApiException {
      public NotFoundException(String message) {
         super(message);
      }
   }

   public static class UnexpectedApiException extends LicenseAPI.ApiException {
      private final int statusCode;

      public UnexpectedApiException(int statusCode, String message) {
         super("Unexpected error (" + statusCode + "): " + message);
         this.statusCode = statusCode;
      }

      public int getStatusCode() {
         return this.statusCode;
      }
   }
}
