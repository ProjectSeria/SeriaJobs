package id.seria.gacha.lib.common.utils.api.authentication;

import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.api.PhoenixPluginsAPI;
import id.seria.gacha.lib.common.utils.utility.BukkitUtil;
import id.seria.gacha.sdk.core.ServerIdentity;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AuthenticationHandler {
   private static final long METRICS_SEND_INTERVAL_MINUTES = 30L;
   private static final long PLAYER_SAMPLE_INTERVAL_MINUTES = 5L;
   private static final int MAX_PLAYER_SAMPLES = 72;
   private static final Pattern IPV4_PATTERN = Pattern.compile("^\\d{1,3}(?:\\.\\d{1,3}){3}$");
   private static final Pattern DOMAIN_PATTERN = Pattern.compile("^(?=.{1,253}$)(?!-)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}$", 2);
   private final ServerPlugin plugin;
   private final String productId;
   private final String planId;
   private final String license;
   private final Consumer<Exception> onError;
   private final Consumer<PhoenixPluginsAPI.AuthenticationResponse> onSuccess;
   private final AuthCache authCache;
   private ScheduledFuture<?> currentTask;
   private ScheduledFuture<?> metricsTask;
   private ScheduledFuture<?> playerSamplesTask;
   private boolean authenticated;
   private final BuildInfo buildInfo = new BuildInfo("1789460705000296", "stable", "6.3.0", "41");
   private final String serverId = this.resolveServerId();
   private final Set<String> serverDomains = ConcurrentHashMap.newKeySet();
   private final Deque<ServerMetrics.OnlinePlayersSample> onlinePlayersTimeline = new ConcurrentLinkedDeque<>();
   private volatile boolean firstJoinMetricSent = false;
   private volatile Integer lastSentOnlinePlayers = null;
   private final Listener loginListener = new Listener() {
      @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
      public void onPlayerJoin(PlayerJoinEvent event) {
         String hostname = null;

         try {
            Method method = event.getPlayer().getClass().getMethod("getVirtualHost");
            Object result = method.invoke(event.getPlayer());
            if (result instanceof InetSocketAddress) {
               hostname = ((InetSocketAddress)result).getHostString();
            }
         } catch (Exception var5) {
         }

         if (hostname == null && event.getPlayer().getAddress() != null) {
            hostname = event.getPlayer().getAddress().getHostString();
         }

         AuthenticationHandler.this.captureServerDomain(hostname);
         AuthenticationHandler.this.onFirstJoinMetrics();
      }
   };

   private String resolveServerId() {
      try {
         return ServerIdentity.getServerUUID().toString();
      } catch (Exception ignored) {
         return UUID.randomUUID().toString();
      }
   }

   @Deprecated
   public AuthenticationHandler(ServerPlugin plugin, String productId, String license, Consumer<Exception> onError) {
      this(plugin, productId, "standard", license, onError);
   }

   @Deprecated
   public AuthenticationHandler(
      ServerPlugin plugin, String productId, String license, Consumer<Exception> onError, Consumer<PhoenixPluginsAPI.AuthenticationResponse> onSuccess
   ) {
      this(plugin, productId, "standard", license, onError, onSuccess);
   }

   public AuthenticationHandler(ServerPlugin plugin, String productId, String planId, String license, Consumer<Exception> onError) {
      this(plugin, productId, planId, license, onError, auth -> {});
   }

   public AuthenticationHandler(
      ServerPlugin plugin,
      String productId,
      String planId,
      String license,
      Consumer<Exception> onError,
      Consumer<PhoenixPluginsAPI.AuthenticationResponse> onSuccess
   ) {
      this.plugin = plugin;
      this.productId = productId;
      this.planId = planId;
      this.license = license;
      this.onError = onError;
      this.onSuccess = onSuccess;
      this.authCache = new AuthCache(plugin);
      this.plugin.getEventBus().registerListener(this.loginListener);
   }

   public PhoenixPluginsAPI.AuthenticationResponse login() {
      try {
         AuthCache.CacheData data = this.authCache.read();
         PhoenixPluginsAPI.AuthenticationResponse response = PhoenixPluginsAPI.loginWithLicenseAndToken(
            this.productId, this.planId, this.license, data == null ? null : data.getSignature(), this.buildInfo
         );
         this.authCache.save(response.getAuthToken(), response.getGracePeriodSignature(), response.getGracePeriodExpiry());
         long millis = response.getAuthTokenExpiry() - System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5L);
         if (millis <= 0L) {
            this.onError.accept(new Exception("Authentication expiration time already expired. Is your clock with right time?"));
            return null;
         } else {
            this.currentTask = Utilities.getSchedulerService().schedule(() -> this.renewAuthToken(1), millis, TimeUnit.MILLISECONDS);
            this.callSuccess(response);
            return response;
         }
      } catch (Exception var6) {
         Exception e = var6;

         try {
            this.tryGracePeriodVerification(e);
            AuthCache.CacheData data = this.authCache.read();
            PhoenixPluginsAPI.AuthenticationResponse forged = new PhoenixPluginsAPI.AuthenticationResponse(
               null, data.getSignature(), -1L, data.getGracePeriodSignature(), data.getGracePeriodExpiry(), e
            );
            this.callSuccess(forged);
            return forged;
         } catch (Exception ex) {
            this.callError(ex);
            this.currentTask = Utilities.getSchedulerService().schedule(() -> this.login(), 5L, TimeUnit.MINUTES);
            return null;
         }
      }
   }

   private void tryGracePeriodVerification(Exception exception) throws Exception {
      AuthCache.CacheData data = this.authCache.read();
      if (data == null) {
         throw exception;
      }

      GracePeriodVerification gracePeriodVerification = new GracePeriodVerification(
         Base64.getDecoder().decode(data.getGracePeriodSignature()), data.getGracePeriodExpiry()
      );
      if (!gracePeriodVerification.validate()) {
         throw new Exception(exception.getMessage() + ". Grace period has passed.");
      }
   }

   public void logout() {
      try {
         if (!this.authenticated) {
            return;
         }

         AuthCache.CacheData data = this.authCache.read();
         if (this.currentTask != null) {
            this.currentTask.cancel(true);
            this.currentTask = null;
         }

         if (this.metricsTask != null) {
            this.metricsTask.cancel(true);
            this.metricsTask = null;
         }

         if (this.playerSamplesTask != null) {
            this.playerSamplesTask.cancel(true);
            this.playerSamplesTask = null;
         }

         if (data != null) {
            this.sendMetricsSnapshot("shutdown");
            PhoenixPluginsAPI.logoutWithToken(data.getSignature());
            return;
         }
      } catch (Exception var5) {
         return;
      } finally {
         this.plugin.getEventBus().unregisterListener(this.loginListener);
      }
   }

   private void renewAuthToken(int tryCount) {
      try {
         AuthCache.CacheData data = this.authCache.read();
         if (data == null) {
            this.login();
            return;
         }

         PhoenixPluginsAPI.RenewResponse response = PhoenixPluginsAPI.renewWithToken(data.getSignature());
         this.authCache.save(response.getAuthToken(), response.getGracePeriodSignature(), response.getGracePeriodExpiry());
         long millis = response.getAuthTokenExpiry() - System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5L);
         if (millis <= 0L) {
            this.onError.accept(new Exception("Authentication expiration time already expired. Is your clock with right time"));
            return;
         }

         this.currentTask = Utilities.getSchedulerService().schedule(() -> this.renewAuthToken(1), millis, TimeUnit.MILLISECONDS);
      } catch (PhoenixPluginsAPI.ApiResponseException e) {
         this.callError(e);
      } catch (Exception var8) {
         Exception e = var8;

         try {
            this.tryGracePeriodVerification(e);
            AuthCache.CacheData data = this.authCache.read();
            PhoenixPluginsAPI.AuthenticationResponse forged = new PhoenixPluginsAPI.AuthenticationResponse(
               null, data.getSignature(), -1L, data.getGracePeriodSignature(), data.getGracePeriodExpiry(), e
            );
            this.callSuccess(forged);
         } catch (Exception ex) {
            this.plugin.getLogger().warn("An unexpected error occurred while renewing plugin connection: " + ex.getMessage());
            if (tryCount == 3) {
               this.callError(ex);
               this.currentTask = Utilities.getSchedulerService().schedule(() -> this.login(), 5L, TimeUnit.MINUTES);
            } else {
               this.currentTask = Utilities.getSchedulerService().schedule(() -> this.renewAuthToken(tryCount + 1), 5L, TimeUnit.MINUTES);
            }
         }
      }
   }

   public void callError(Exception error) {
      this.authenticated = false;
      if (this.metricsTask != null) {
         this.metricsTask.cancel(true);
         this.metricsTask = null;
      }

      if (this.playerSamplesTask != null) {
         this.playerSamplesTask.cancel(true);
         this.playerSamplesTask = null;
      }

      BukkitUtil.runSync(this.plugin, () -> this.onError.accept(error));
   }

   public void callSuccess(PhoenixPluginsAPI.AuthenticationResponse success) {
      this.authenticated = true;
      if (!Bukkit.getOnlinePlayers().isEmpty()) {
         Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getAddress() != null) {
               this.captureServerDomain(player.getAddress().getHostString());
            }
         });
         this.onFirstJoinMetrics();
      }

      BukkitUtil.runSync(this.plugin, () -> this.onSuccess.accept(success));
   }

   private void startMetricsTask() {
      if (this.metricsTask == null || this.metricsTask.isCancelled()) {
         this.metricsTask = Utilities.getSchedulerService().scheduleAtFixedRate(() -> this.sendMetricsSnapshot("periodic"), 30L, 30L, TimeUnit.MINUTES);
      }
   }

   private void startPlayerSamplesTask() {
      if (this.playerSamplesTask == null || this.playerSamplesTask.isCancelled()) {
         this.recordOnlinePlayersSample();
         this.playerSamplesTask = Utilities.getSchedulerService().scheduleAtFixedRate(() -> this.recordOnlinePlayersSample(), 5L, 5L, TimeUnit.MINUTES);
      }
   }

   private void onFirstJoinMetrics() {
      if (this.authenticated && !this.firstJoinMetricSent) {
         this.firstJoinMetricSent = true;
         this.startPlayerSamplesTask();
         this.startMetricsTask();
      }
   }

   private void sendMetricsSnapshot(String reason) {
      try {
         if (!this.authenticated) {
            return;
         }

         AuthCache.CacheData data = this.authCache.read();
         if (data == null || data.getSignature() == null || data.getSignature().trim().isEmpty()) {
            return;
         }

         ServerMetrics metrics = this.buildMetrics(reason);
         if (metrics.getOnlinePlayers() == 0 && metrics.getOnlinePlayersTimeline().isEmpty()) {
            return;
         }

         PhoenixPluginsAPI.metrics(data.getSignature(), System.currentTimeMillis(), metrics);
         this.lastSentOnlinePlayers = metrics.getOnlinePlayers();
         this.onlinePlayersTimeline.clear();
      } catch (Exception var4) {
      }
   }

   private void recordOnlinePlayersSample() {
      int onlinePlayers = Bukkit.getOnlinePlayers().size();
      if (onlinePlayers > 0) {
         this.onlinePlayersTimeline.addLast(new ServerMetrics.OnlinePlayersSample(System.currentTimeMillis(), onlinePlayers));

         while (this.onlinePlayersTimeline.size() > 72) {
            this.onlinePlayersTimeline.pollFirst();
         }
      }
   }

   private ServerMetrics buildMetrics(String reason) {
      List<String> domains = new ArrayList<>(this.serverDomains);
      List<ServerMetrics.OnlinePlayersSample> timeline = new ArrayList<>(this.onlinePlayersTimeline);
      int latestPlayers = timeline.isEmpty() ? Bukkit.getOnlinePlayers().size() : timeline.get(timeline.size() - 1).getOnlinePlayers();
      String primaryDomain = domains.isEmpty() ? "unknown" : domains.get(0);
      return new ServerMetrics(this.serverId, Bukkit.getBukkitVersion(), Bukkit.getName(), primaryDomain, domains, latestPlayers, timeline, reason);
   }

   private void captureServerDomain(String rawDomain) {
      if (rawDomain != null && !rawDomain.trim().isEmpty()) {
         String parsed = rawDomain.trim().toLowerCase(Locale.ROOT);
         int colonIndex = parsed.indexOf(58);
         if (colonIndex > 0) {
            parsed = parsed.substring(0, colonIndex);
         }

         if (parsed.endsWith(".")) {
            parsed = parsed.substring(0, parsed.length() - 1);
         }

         if (this.isValidDomain(parsed)) {
            this.serverDomains.add(parsed);
         }
      }
   }

   private boolean isValidDomain(String domain) {
      if (domain == null || domain.isEmpty()) {
         return false;
      } else if ("unknown".equals(domain) || "localhost".equals(domain)) {
         return false;
      } else {
         return IPV4_PATTERN.matcher(domain).matches() ? false : DOMAIN_PATTERN.matcher(domain).matches();
      }
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public String getProductId() {
      return this.productId;
   }

   @Generated
   public String getPlanId() {
      return this.planId;
   }

   @Generated
   public String getLicense() {
      return this.license;
   }

   @Generated
   public Consumer<Exception> getOnError() {
      return this.onError;
   }

   @Generated
   public Consumer<PhoenixPluginsAPI.AuthenticationResponse> getOnSuccess() {
      return this.onSuccess;
   }

   @Generated
   public AuthCache getAuthCache() {
      return this.authCache;
   }

   @Generated
   public ScheduledFuture<?> getCurrentTask() {
      return this.currentTask;
   }

   @Generated
   public ScheduledFuture<?> getMetricsTask() {
      return this.metricsTask;
   }

   @Generated
   public ScheduledFuture<?> getPlayerSamplesTask() {
      return this.playerSamplesTask;
   }

   @Generated
   public boolean isAuthenticated() {
      return this.authenticated;
   }

   @Generated
   public BuildInfo getBuildInfo() {
      return this.buildInfo;
   }

   @Generated
   public String getServerId() {
      return this.serverId;
   }

   @Generated
   public Set<String> getServerDomains() {
      return this.serverDomains;
   }

   @Generated
   public Deque<ServerMetrics.OnlinePlayersSample> getOnlinePlayersTimeline() {
      return this.onlinePlayersTimeline;
   }

   @Generated
   public boolean isFirstJoinMetricSent() {
      return this.firstJoinMetricSent;
   }

   @Generated
   public Integer getLastSentOnlinePlayers() {
      return this.lastSentOnlinePlayers;
   }

   @Generated
   public Listener getLoginListener() {
      return this.loginListener;
   }
}
