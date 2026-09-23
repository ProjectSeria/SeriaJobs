package id.seria.gacha.sdk.utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class HttpBuilder {
   private static final List<Consumer<HttpURLConnection>> ITEMS = new CopyOnWriteArrayList<>();

   private HttpBuilder() {
   }

   public static void register(Consumer<HttpURLConnection> step) {
      ITEMS.add(step);
   }

   public static String post(String url, Supplier<String> body, int connectTimeout, int readTimeout) throws Exception {
      HttpURLConnection c = open(url);
      base(c, connectTimeout, readTimeout);
      write(c, body.get());
      return read(c);
   }

   private static HttpURLConnection open(String u) throws Exception {
      return (HttpURLConnection)new URL(u).openConnection();
   }

   private static void base(HttpURLConnection c, int ct, int rt) throws Exception {
      c.setRequestMethod("POST");
      c.setConnectTimeout(ct);
      c.setReadTimeout(rt);
      c.setDoOutput(true);
      c.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
      walk(c);
   }

   private static void walk(HttpURLConnection c) {
      for (Consumer<HttpURLConnection> step : ITEMS) {
         step.accept(c);
      }
   }

   private static void write(HttpURLConnection c, String payload) throws Exception {
      OutputStream os = c.getOutputStream();

      try {
         os.write(payload.getBytes(StandardCharsets.UTF_8));
      } catch (Throwable var6) {
         if (os != null) {
            try {
               os.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (os != null) {
         os.close();
      }
   }

   private static String read(HttpURLConnection c) throws Exception {
      int code = c.getResponseCode();
      if (code != 200) {
         throw new RuntimeException("HttpResponseCode: " + code);
      }

      InputStreamReader is = new InputStreamReader(c.getInputStream(), Charset.forName("UTF-8"));

      String var6;
      try {
         BufferedReader br = new BufferedReader(is);

         try {
            StringBuilder sb = new StringBuilder();

            String l;
            while ((l = br.readLine()) != null) {
               sb.append(l);
            }

            var6 = sb.toString();
         } catch (Throwable var9) {
            try {
               br.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }

            throw var9;
         }

         br.close();
      } catch (Throwable var10) {
         try {
            is.close();
         } catch (Throwable var7) {
            var10.addSuppressed(var7);
         }

         throw var10;
      }

      is.close();
      return var6;
   }

   public static Supplier<String> form(Consumer<StringBuilder> c) {
      return () -> {
         StringBuilder sb = new StringBuilder();
         c.accept(sb);
         return sb.toString();
      };
   }

   public static void add(StringBuilder sb, String k, String v) {
      if (sb.length() > 0) {
         sb.append('&');
      }

      sb.append(enc(k)).append('=').append(enc(v));
   }

   private static String enc(String s) {
      try {
         return URLEncoder.encode(s, "UTF-8");
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
}
