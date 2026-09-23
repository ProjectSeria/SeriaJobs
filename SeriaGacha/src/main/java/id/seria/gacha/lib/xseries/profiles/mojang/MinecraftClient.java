package id.seria.gacha.lib.xseries.profiles.mojang;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import id.seria.gacha.lib.xseries.profiles.ProfileLogger;
import id.seria.gacha.lib.xseries.profiles.ProfilesCore;
import id.seria.gacha.lib.xseries.profiles.exceptions.MojangAPIException;
import id.seria.gacha.lib.xseries.profiles.exceptions.MojangAPIRetryException;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class MinecraftClient {
   private static final AtomicInteger SESSION_ID = new AtomicInteger();
   private static final Proxy PROXY = ProfilesCore.PROXY == null ? Proxy.NO_PROXY : ProfilesCore.PROXY;
   private static final Gson GSON = new Gson();
   private static final RateLimiter TOTAL_REQUESTS = new RateLimiter(Integer.MAX_VALUE, Duration.ofMinutes(10L));
   private static final String USER_AGENT = "XSeries/13.6.0 ("
      + System.getProperty("os.name")
      + "; "
      + System.getProperty("os.version")
      + "; "
      + System.getProperty("java.vendor")
      + "; "
      + System.getProperty("java.version")
      + ") "
      + Bukkit.getName()
      + '/'
      + Bukkit.getBukkitVersion()
      + ' '
      + Bukkit.getVersion();
   private final String method;
   private final URI baseURL;
   private final RateLimiter rateLimiter;

   public MinecraftClient.Session session(@Nullable ProfileRequestConfiguration config) {
      MinecraftClient.Session session = new MinecraftClient.Session();
      if (config != null) {
         config.configure(session);
      }

      return session;
   }

   public MinecraftClient(String method, String baseURL, RateLimiter rateLimiter) {
      this.method = method;

      try {
         this.baseURL = new URI(baseURL);
      } catch (URISyntaxException e) {
         throw new IllegalArgumentException("Invalid Minecraft API URL: " + baseURL, e);
      }

      this.rateLimiter = rateLimiter;
   }

   private static String totalReq() {
      return " (total: " + TOTAL_REQUESTS.getEffectiveRequestsCount() + ')';
   }

   public final class Session {
      private final int sessionId = MinecraftClient.SESSION_ID.getAndIncrement();
      private Duration connectTimeout = Duration.ofSeconds(10L);
      private Duration readTimeout = Duration.ofSeconds(10L);
      private Duration retryDelay = Duration.ofSeconds(5L);
      private int retries;
      private boolean waitInQueue = true;
      private Object body;
      private String append;
      private HttpURLConnection connection;
      private BiFunction<MinecraftClient.Session, Throwable, Boolean> errorHandler;

      public Session() {
      }

      private void debug(String message, Object... vars) {
         Object[] variables = XReflection.concatenate(new Object[]{this.sessionId, this.append}, vars);
         ProfileLogger.debug("[MinecraftClient-{}][{}] " + message, variables);
      }

      public MinecraftClient.Session exceptionally(BiFunction<MinecraftClient.Session, Throwable, Boolean> errorHandler) {
         this.errorHandler = errorHandler;
         return this;
      }

      public MinecraftClient.Session waitInQueue(boolean wait) {
         this.waitInQueue = wait;
         return this;
      }

      public MinecraftClient.Session retry(int retries, Duration delay) {
         this.retries = retries;
         this.retryDelay = delay;
         return this;
      }

      public MinecraftClient.Session body(Object body) {
         this.validateMethod("POST");
         this.body = Objects.requireNonNull(body);
         return this;
      }

      public MinecraftClient.Session append(@NotNull String append) {
         this.append = Objects.requireNonNull(append);
         return this;
      }

      public MinecraftClient.Session timeout(Duration connectTimeout, Duration readTimeout) {
         this.connectTimeout = connectTimeout;
         this.readTimeout = readTimeout;
         return this;
      }

      private void validateMethod(String method) {
         if (!MinecraftClient.this.method.equals(method)) {
            throw new UnsupportedOperationException("Cannot " + method + " with a client using method " + MinecraftClient.this);
         }
      }

      @Nullable
      public JsonElement request() throws IOException, MojangAPIException {
         try {
            JsonElement response = this.request0();
            this.debug("Received response: {}", response);
            return response == null ? null : (response.isJsonNull() ? null : response);
         } catch (Exception ex) {
            if (this.retries > 0) {
               this.retries--;
               if (!(ex instanceof MojangAPIRetryException) || ((MojangAPIRetryException)ex).getReason() != MojangAPIRetryException.Reason.RATELIMITED) {
                  try {
                     Thread.sleep(this.retryDelay.toMillis());
                  } catch (InterruptedException e) {
                     throw new IllegalStateException("Mojang API retry thread was interrupted unexpectedly", e);
                  }
               }

               return this.request();
            } else if (this.errorHandler == null) {
               throw ex;
            } else {
               Boolean shouldRetry = this.errorHandler.apply(this, ex);
               if (shouldRetry != null && !shouldRetry) {
                  throw ex;
               } else {
                  return this.request();
               }
            }
         }
      }

      @Nullable
      private JsonElement request0() throws IOException, MojangAPIException {
         if (this.waitInQueue) {
            MinecraftClient.this.rateLimiter.acquireOrWait();
         } else if (!MinecraftClient.this.rateLimiter.acquire()) {
            throw new MojangAPIRetryException(
               MojangAPIRetryException.Reason.RATELIMITED, "Rate limit has been hit! " + MinecraftClient.this.rateLimiter + MinecraftClient.totalReq()
            );
         }

         this.connection = (HttpURLConnection)(this.append == null ? MinecraftClient.this.baseURL : MinecraftClient.this.baseURL.resolve(this.append))
            .toURL()
            .openConnection(MinecraftClient.PROXY);
         this.connection.setRequestMethod(MinecraftClient.this.method);
         this.connection.setConnectTimeout((int)this.connectTimeout.toMillis());
         this.connection.setReadTimeout((int)this.readTimeout.toMillis());
         this.connection.setDoInput(true);
         this.connection.setUseCaches(false);
         this.connection.setAllowUserInteraction(false);
         this.connection.setRequestProperty("User-Agent", MinecraftClient.USER_AGENT);
         if (this.body != null) {
            this.connection.setDoOutput(true);
            String stringBody = MinecraftClient.GSON.toJson(this.body);
            this.debug("Writing body {} to {}", stringBody, this.connection.getURL());
            byte[] bodyBytes = stringBody.getBytes(StandardCharsets.UTF_8);
            this.connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            this.connection.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));
            OutputStream outputStream = this.connection.getOutputStream();

            try {
               outputStream.write(bodyBytes);
            } catch (Throwable var7) {
               if (outputStream != null) {
                  try {
                     outputStream.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (outputStream != null) {
               outputStream.close();
            }
         } else {
            this.connection.setDoOutput(false);
         }

         return this.request00();
      }

      @Nullable
      private JsonElement request00() {
         this.debug("Sending request to {}", this.connection.getURL());

         try {
            return this.connectionStreamToJson(false);
         } catch (Throwable var6) {
            Throwable ex = var6;

            MojangAPIException exception;
            try {
               switch (this.connection.getResponseCode()) {
                  case 404:
                     return null;
                  case 429:
                     String rateLimitBefore = MinecraftClient.this.rateLimiter.toString();
                     MinecraftClient.this.rateLimiter.instantRateLimit();
                     throw new MojangAPIRetryException(
                        MojangAPIRetryException.Reason.RATELIMITED,
                        "Rate limit has been hit (server confirmed): " + rateLimitBefore + " -> " + rateLimitBefore + MinecraftClient.totalReq()
                     );
                  default:
                     if (ex instanceof SocketException && ex.getMessage().toLowerCase(Locale.ENGLISH).contains("connection reset")) {
                        throw new MojangAPIRetryException(MojangAPIRetryException.Reason.CONNECTION_RESET, "Connection was closed", ex);
                     }

                     JsonElement errorJson = this.connectionStreamToJson(true);
                     exception = new MojangAPIException(errorJson == null ? "[NO ERROR RESPONSE]" : errorJson.toString(), ex);
               }
            } catch (MojangAPIRetryException rethrowEx) {
               throw rethrowEx;
            } catch (Throwable errorEx) {
               exception = new MojangAPIException("Failed to read both normal response and error response from '" + this.connection.getURL() + '\'');
               exception.addSuppressed(var6);
               exception.addSuppressed(errorEx);
            }

            throw exception;
         }
      }

      private JsonElement connectionStreamToJson(boolean error) throws IOException, IllegalStateException {
         InputStream inputStream = error ? this.connection.getErrorStream() : this.connection.getInputStream();

         Object var12;
         label89: {
            JsonElement rawResponse;
            try {
               if (error && inputStream == null) {
                  var12 = null;
                  break label89;
               }

               JsonReader reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

               try {
                  Exception ex = null;

                  JsonElement json;
                  try {
                     json = Streams.parse(reader);
                  } catch (Exception e) {
                     ex = e;
                     json = null;
                  }

                  if (json == null) {
                     String rawResponsex = CharStreams.toString(
                        new InputStreamReader(error ? this.connection.getErrorStream() : this.connection.getInputStream(), Charsets.UTF_8)
                     );
                     throw new IllegalStateException(
                        (error ? "error response" : "normal response")
                           + " is not a JSON object '"
                           + this.connection.getResponseCode()
                           + " - "
                           + this.connection.getResponseMessage()
                           + "': "
                           + rawResponsex,
                        ex
                     );
                  }

                  rawResponse = json;
               } catch (Throwable var10) {
                  try {
                     reader.close();
                  } catch (Throwable var8) {
                     var10.addSuppressed(var8);
                  }

                  throw var10;
               }

               reader.close();
            } catch (Throwable var11) {
               if (inputStream != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var7) {
                     var11.addSuppressed(var7);
                  }
               }

               throw var11;
            }

            if (inputStream != null) {
               inputStream.close();
            }

            return rawResponse;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         return (JsonElement)var12;
      }
   }
}
