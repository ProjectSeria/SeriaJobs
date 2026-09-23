package id.seria.gacha.lib.common.utils.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import id.seria.gacha.lib.common.utils.api.authentication.BuildInfo;
import id.seria.gacha.lib.common.utils.api.authentication.ServerMetrics;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Generated;
import lombok.NonNull;

public final class PhoenixPluginsAPI {
   private static int connectTimeout = 10000;
   private static int readTimeout = 10000;
   private static final String API_URL = "https://api.phoenixplugins.com/v4";
   private static final Gson GSON = new GsonBuilder().create();

   public PhoenixPluginsAPI() {
   }

   public static List<Product> fetchProductAddons(@NonNull String productId) throws Exception {
      if (productId == null) {
         throw new NullPointerException("productId is marked non-null but is null");
      }

      HttpURLConnection connection = null;

      try {
         URL url = new URL("https://api.phoenixplugins.com/v4/products/" + productId + "/addons");
         connection = (HttpURLConnection)url.openConnection();
         connection.setRequestProperty(
            "User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
         );
         connection.setRequestMethod("GET");
         connection.setConnectTimeout(connectTimeout);
         connection.setReadTimeout(readTimeout);
         connection.setDoOutput(true);
         int responsecode = connection.getResponseCode();
         if (responsecode != 200) {
            throw new PhoenixPluginsAPI.ApiConnectionException("HttpResponseCode: " + responsecode);
         }

         InputStreamReader inputReader = new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"));

         try {
            BufferedReader r = new BufferedReader(inputReader);

            try {
               StringBuilder sb = new StringBuilder();

               String line;
               while ((line = r.readLine()) != null) {
                  sb.append(line);
               }

               JsonObject response = (JsonObject)GSON.fromJson(sb.toString(), JsonObject.class);
               String statusCode = response.get("status").getAsString();
               JsonElement bodyElement = response.get("body");
               switch (statusCode) {
                  case "ok":
                     JsonArray body = bodyElement.getAsJsonArray();
                     List<Product> products = new ArrayList<>();

                     for (JsonElement element : body) {
                        JsonObject object = element.getAsJsonObject();
                        products.add(
                           new Product(
                              object.get("url").getAsString(),
                              object.get("name").getAsString(),
                              object.get("description").getAsString(),
                              object.get("popularity_index").getAsInt(),
                              object.get("original_price").getAsDouble(),
                              object.get("current_price").getAsDouble()
                           )
                        );
                     }

                     return products;
                  case "error":
                     int errorCode = response.get("error_code").getAsInt();
                     throw new PhoenixPluginsAPI.ApiResponseException(errorCode, bodyElement.getAsString());
                  default:
                     throw new PhoenixPluginsAPI.ApiResponseException(-1, "Unknown status code \"" + statusCode + "\"");
               }
            } finally {
               if (Collections.singletonList(r).get(0) != null) {
                  r.close();
               }
            }
         } finally {
            if (Collections.singletonList(inputReader).get(0) != null) {
               inputReader.close();
            }
         }
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   @Deprecated
   public static PhoenixPluginsAPI.AuthenticationResponse loginWithLicenseAndToken(
      @NonNull String productId, @NonNull String planId, @NonNull String license, String signature
   ) throws Exception {
      if (productId == null) {
         throw new NullPointerException("productId is marked non-null but is null");
      } else if (planId == null) {
         throw new NullPointerException("planId is marked non-null but is null");
      } else if (license == null) {
         throw new NullPointerException("license is marked non-null but is null");
      } else {
         return loginWithLicenseAndToken(productId, planId, license, signature, null);
      }
   }

   public static PhoenixPluginsAPI.AuthenticationResponse loginWithLicenseAndToken(
      @NonNull String productId, @NonNull String planId, @NonNull String license, String signature, BuildInfo info
   ) throws Exception {
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
         connection.setRequestProperty(
            "User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
         );
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setRequestMethod("POST");
         connection.setConnectTimeout(connectTimeout);
         connection.setReadTimeout(readTimeout);
         connection.setDoOutput(true);
         JsonObject requestBody = new JsonObject();
         requestBody.addProperty("product", productId);
         requestBody.addProperty("plan", planId);
         requestBody.addProperty("license", license);
         if (signature != null) {
            requestBody.addProperty("signature", signature);
         }

         if (info != null) {
            requestBody.add("info", GSON.toJsonTree(info));
         }

         byte[] postDataBytes = requestBody.toString().getBytes(StandardCharsets.UTF_8);
         OutputStream outputStream = connection.getOutputStream();

         try {
            outputStream.write(postDataBytes);
            outputStream.flush();
         } catch (Throwable var41) {
            if (outputStream != null) {
               try {
                  outputStream.close();
               } catch (Throwable var40) {
                  var41.addSuppressed(var40);
               }
            }

            throw var41;
         }

         if (outputStream != null) {
            outputStream.close();
         }

         int responsecode = connection.getResponseCode();
         if (responsecode != 200) {
            throw new PhoenixPluginsAPI.ApiConnectionException("HttpResponseCode: " + responsecode);
         }

         InputStreamReader inputReader = new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"));

         try {
            BufferedReader r = new BufferedReader(inputReader);

            try {
               StringBuilder sb = new StringBuilder();

               String line;
               while ((line = r.readLine()) != null) {
                  sb.append(line);
               }

               JsonObject response = (JsonObject)GSON.fromJson(sb.toString(), JsonObject.class);
               String statusCode = response.get("status").getAsString();
               JsonElement bodyElement = response.get("body");
               switch (statusCode) {
                  case "ok":
                     JsonObject body = bodyElement.getAsJsonObject();
                     ProductVersion currentVersion = null;
                     if (body.has("current_version") && !body.get("current_version").isJsonNull()) {
                        currentVersion = new ProductVersion(body.get("current_version").getAsString());
                     }

                     return new PhoenixPluginsAPI.AuthenticationResponse(
                        currentVersion,
                        body.get("auth_token").getAsString(),
                        body.get("auth_token_expiry").getAsLong(),
                        body.get("grace_period_signature").getAsString(),
                        body.get("grace_period_expiry").getAsLong(),
                        null
                     );
                  case "error":
                     int errorCode = response.get("error_code").getAsInt();
                     throw new PhoenixPluginsAPI.ApiResponseException(errorCode, bodyElement.getAsString());
                  default:
                     throw new PhoenixPluginsAPI.ApiResponseException(-1, "Unknown status code \"" + statusCode + "\"");
               }
            } finally {
               if (Collections.singletonList(r).get(0) != null) {
                  r.close();
               }
            }
         } finally {
            if (Collections.singletonList(inputReader).get(0) != null) {
               inputReader.close();
            }
         }
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   @Deprecated
   public static PhoenixPluginsAPI.RenewResponse renewWithToken(@NonNull String productId, @NonNull String planId, @NonNull String signature) throws Exception {
      if (productId == null) {
         throw new NullPointerException("productId is marked non-null but is null");
      } else if (planId == null) {
         throw new NullPointerException("planId is marked non-null but is null");
      } else if (signature == null) {
         throw new NullPointerException("signature is marked non-null but is null");
      } else {
         return renewWithToken(signature);
      }
   }

   public static PhoenixPluginsAPI.RenewResponse renewWithToken(@NonNull String signature) throws Exception {
      if (signature == null) {
         throw new NullPointerException("signature is marked non-null but is null");
      }

      HttpURLConnection connection = null;

      try {
         URL url = new URL("https://api.phoenixplugins.com/v4/licenses/renew");
         connection = (HttpURLConnection)url.openConnection();
         connection.setRequestProperty(
            "User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
         );
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setRequestMethod("POST");
         connection.setConnectTimeout(connectTimeout);
         connection.setReadTimeout(readTimeout);
         connection.setDoOutput(true);
         JsonObject requestBody = new JsonObject();
         requestBody.addProperty("signature", signature);
         byte[] postDataBytes = requestBody.toString().getBytes(StandardCharsets.UTF_8);
         OutputStream outputStream = connection.getOutputStream();

         try {
            outputStream.write(postDataBytes);
            outputStream.flush();
         } catch (Throwable var36) {
            if (outputStream != null) {
               try {
                  outputStream.close();
               } catch (Throwable var35) {
                  var36.addSuppressed(var35);
               }
            }

            throw var36;
         }

         if (outputStream != null) {
            outputStream.close();
         }

         int responsecode = connection.getResponseCode();
         if (responsecode != 200) {
            throw new PhoenixPluginsAPI.ApiConnectionException("HttpResponseCode: " + responsecode);
         }

         InputStreamReader inputReader = new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"));

         try {
            BufferedReader r = new BufferedReader(inputReader);

            try {
               StringBuilder sb = new StringBuilder();

               String line;
               while ((line = r.readLine()) != null) {
                  sb.append(line);
               }

               JsonObject response = (JsonObject)GSON.fromJson(sb.toString(), JsonObject.class);
               String statusCode = response.get("status").getAsString();
               JsonElement bodyElement = response.get("body");
               switch (statusCode) {
                  case "ok":
                     JsonObject body = bodyElement.getAsJsonObject();
                     return new PhoenixPluginsAPI.RenewResponse(
                        body.get("auth_token").getAsString(),
                        body.get("auth_token_expiry").getAsLong(),
                        body.get("grace_period_signature").getAsString(),
                        body.get("grace_period_expiry").getAsLong()
                     );
                  case "error":
                     int errorCode = response.get("error_code").getAsInt();
                     throw new PhoenixPluginsAPI.ApiResponseException(errorCode, bodyElement.getAsString());
                  default:
                     throw new PhoenixPluginsAPI.ApiResponseException(-1, "Unknown status code \"" + statusCode + "\"");
               }
            } finally {
               if (Collections.singletonList(r).get(0) != null) {
                  r.close();
               }
            }
         } finally {
            if (Collections.singletonList(inputReader).get(0) != null) {
               inputReader.close();
            }
         }
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   @Deprecated
   public static void logoutWithToken(@NonNull String productId, @NonNull String planId, @NonNull String signature) throws Exception {
      if (productId == null) {
         throw new NullPointerException("productId is marked non-null but is null");
      }

      if (planId == null) {
         throw new NullPointerException("planId is marked non-null but is null");
      }

      if (signature == null) {
         throw new NullPointerException("signature is marked non-null but is null");
      }

      logoutWithToken(signature);
   }

   public static void logoutWithToken(@NonNull String signature) throws Exception {
      if (signature == null) {
         throw new NullPointerException("signature is marked non-null but is null");
      }

      HttpURLConnection connection = null;

      try {
         URL url = new URL("https://api.phoenixplugins.com/v4/licenses/logout");
         connection = (HttpURLConnection)url.openConnection();
         connection.setRequestProperty(
            "User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
         );
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setRequestMethod("POST");
         connection.setConnectTimeout(connectTimeout);
         connection.setReadTimeout(readTimeout);
         connection.setDoOutput(true);
         JsonObject requestBody = new JsonObject();
         requestBody.addProperty("signature", signature);
         byte[] postDataBytes = requestBody.toString().getBytes(StandardCharsets.UTF_8);
         OutputStream outputStream = connection.getOutputStream();

         try {
            outputStream.write(postDataBytes);
            outputStream.flush();
         } catch (Throwable var35) {
            if (outputStream != null) {
               try {
                  outputStream.close();
               } catch (Throwable var34) {
                  var35.addSuppressed(var34);
               }
            }

            throw var35;
         }

         if (outputStream != null) {
            outputStream.close();
         }

         int responsecode = connection.getResponseCode();
         if (responsecode != 200) {
            throw new PhoenixPluginsAPI.ApiConnectionException("HttpResponseCode: " + responsecode);
         }

         InputStreamReader inputReader = new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"));

         try {
            BufferedReader r = new BufferedReader(inputReader);

            try {
               StringBuilder sb = new StringBuilder();

               String line;
               while ((line = r.readLine()) != null) {
                  sb.append(line);
               }

               JsonObject response = (JsonObject)GSON.fromJson(sb.toString(), JsonObject.class);
               String statusCode = response.get("status").getAsString();
               JsonElement bodyElement = response.get("body");
               switch (statusCode) {
                  case "ok":
                     return;
                  case "error":
                     int errorCode = response.get("error_code").getAsInt();
                     throw new PhoenixPluginsAPI.ApiResponseException(errorCode, bodyElement.getAsString());
                  default:
                     throw new PhoenixPluginsAPI.ApiResponseException(-1, "Unknown status code \"" + statusCode + "\"");
               }
            } finally {
               if (Collections.singletonList(r).get(0) != null) {
                  r.close();
               }
            }
         } finally {
            if (Collections.singletonList(inputReader).get(0) != null) {
               inputReader.close();
            }
         }
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   public static void metrics(@NonNull String signature, long timestamp, @NonNull ServerMetrics data) throws Exception {
      if (signature == null) {
         throw new NullPointerException("signature is marked non-null but is null");
      }

      if (data == null) {
         throw new NullPointerException("data is marked non-null but is null");
      }

      HttpURLConnection connection = null;

      try {
         URL url = new URL("https://api.phoenixplugins.com/v4/metrics/data");
         connection = (HttpURLConnection)url.openConnection();
         connection.setRequestProperty(
            "User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
         );
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setRequestMethod("POST");
         connection.setConnectTimeout(connectTimeout);
         connection.setReadTimeout(readTimeout);
         connection.setDoOutput(true);
         JsonObject requestBody = new JsonObject();
         requestBody.addProperty("signature", signature);
         requestBody.addProperty("timestamp", timestamp);
         requestBody.add("data", GSON.toJsonTree(data));
         byte[] postDataBytes = requestBody.toString().getBytes(StandardCharsets.UTF_8);
         OutputStream outputStream = connection.getOutputStream();

         try {
            outputStream.write(postDataBytes);
            outputStream.flush();
         } catch (Throwable var38) {
            if (outputStream != null) {
               try {
                  outputStream.close();
               } catch (Throwable var37) {
                  var38.addSuppressed(var37);
               }
            }

            throw var38;
         }

         if (outputStream != null) {
            outputStream.close();
         }

         int responsecode = connection.getResponseCode();
         if (responsecode != 200) {
            throw new PhoenixPluginsAPI.ApiConnectionException("HttpResponseCode: " + responsecode);
         }

         InputStreamReader inputReader = new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"));

         try {
            BufferedReader r = new BufferedReader(inputReader);

            try {
               StringBuilder sb = new StringBuilder();

               String line;
               while ((line = r.readLine()) != null) {
                  sb.append(line);
               }

               JsonObject response = (JsonObject)GSON.fromJson(sb.toString(), JsonObject.class);
               String statusCode = response.get("status").getAsString();
               JsonElement bodyElement = response.get("body");
               switch (statusCode) {
                  case "ok":
                     return;
                  case "error":
                     int errorCode = response.get("error_code").getAsInt();
                     throw new PhoenixPluginsAPI.ApiResponseException(errorCode, bodyElement.getAsString());
                  default:
                     throw new PhoenixPluginsAPI.ApiResponseException(-1, "Unknown status code \"" + statusCode + "\"");
               }
            } finally {
               if (Collections.singletonList(r).get(0) != null) {
                  r.close();
               }
            }
         } finally {
            if (Collections.singletonList(inputReader).get(0) != null) {
               inputReader.close();
            }
         }
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   public static class ApiConnectionException extends PhoenixPluginsAPI.ApiException {
      public ApiConnectionException(String message) {
         super(message);
      }
   }

   public static class ApiException extends Exception {
      public ApiException(String message) {
         super(message);
      }
   }

   public static class ApiResponseException extends PhoenixPluginsAPI.ApiException {
      private final int errorCode;

      public ApiResponseException(int errorCode, String message) {
         super(message);
         this.errorCode = errorCode;
      }

      @Generated
      public int getErrorCode() {
         return this.errorCode;
      }
   }

   public static class AuthenticationResponse {
      private final ProductVersion currentVersion;
      private final String authToken;
      private final long authTokenExpiry;
      private final String gracePeriodSignature;
      private final long gracePeriodExpiry;
      private final Exception exception;

      @Generated
      public ProductVersion getCurrentVersion() {
         return this.currentVersion;
      }

      @Generated
      public String getAuthToken() {
         return this.authToken;
      }

      @Generated
      public long getAuthTokenExpiry() {
         return this.authTokenExpiry;
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
      public Exception getException() {
         return this.exception;
      }

      @Generated
      public AuthenticationResponse(
         ProductVersion currentVersion, String authToken, long authTokenExpiry, String gracePeriodSignature, long gracePeriodExpiry, Exception exception
      ) {
         this.currentVersion = currentVersion;
         this.authToken = authToken;
         this.authTokenExpiry = authTokenExpiry;
         this.gracePeriodSignature = gracePeriodSignature;
         this.gracePeriodExpiry = gracePeriodExpiry;
         this.exception = exception;
      }
   }

   public static class RenewResponse {
      private final String authToken;
      private final long authTokenExpiry;
      private final String gracePeriodSignature;
      private final long gracePeriodExpiry;

      @Generated
      public String getAuthToken() {
         return this.authToken;
      }

      @Generated
      public long getAuthTokenExpiry() {
         return this.authTokenExpiry;
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
      public RenewResponse(String authToken, long authTokenExpiry, String gracePeriodSignature, long gracePeriodExpiry) {
         this.authToken = authToken;
         this.authTokenExpiry = authTokenExpiry;
         this.gracePeriodSignature = gracePeriodSignature;
         this.gracePeriodExpiry = gracePeriodExpiry;
      }
   }
}
