package id.seria.gacha.lib.common.utils.api.authentication;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Generated;

public class ServerMetrics {
   @SerializedName("server_id")
   private final String serverId;
   @SerializedName("server_version")
   private final String serverVersion;
   @SerializedName("server_software")
   private final String serverSoftware;
   @SerializedName("server_domain")
   private final String serverDomain;
   @SerializedName("server_domains")
   private final List<String> serverDomains;
   @SerializedName("online_players")
   private final int onlinePlayers;
   @SerializedName("online_players_timeline")
   private final List<ServerMetrics.OnlinePlayersSample> onlinePlayersTimeline;
   @SerializedName("reason")
   private final String reason;

   public ServerMetrics(
      String serverId,
      String serverVersion,
      String serverSoftware,
      String serverDomain,
      List<String> serverDomains,
      int onlinePlayers,
      List<ServerMetrics.OnlinePlayersSample> onlinePlayersTimeline,
      String reason
   ) {
      this.serverId = serverId;
      this.serverVersion = serverVersion;
      this.serverSoftware = serverSoftware;
      this.serverDomain = serverDomain;
      this.serverDomains = serverDomains;
      this.onlinePlayers = onlinePlayers;
      this.onlinePlayersTimeline = onlinePlayersTimeline;
      this.reason = reason;
   }

   @Generated
   public String getServerId() {
      return this.serverId;
   }

   @Generated
   public String getServerVersion() {
      return this.serverVersion;
   }

   @Generated
   public String getServerSoftware() {
      return this.serverSoftware;
   }

   @Generated
   public String getServerDomain() {
      return this.serverDomain;
   }

   @Generated
   public List<String> getServerDomains() {
      return this.serverDomains;
   }

   @Generated
   public int getOnlinePlayers() {
      return this.onlinePlayers;
   }

   @Generated
   public List<ServerMetrics.OnlinePlayersSample> getOnlinePlayersTimeline() {
      return this.onlinePlayersTimeline;
   }

   @Generated
   public String getReason() {
      return this.reason;
   }

   @Generated
   @Override
   public String toString() {
      return "ServerMetrics(serverId="
         + this.getServerId()
         + ", serverVersion="
         + this.getServerVersion()
         + ", serverSoftware="
         + this.getServerSoftware()
         + ", serverDomain="
         + this.getServerDomain()
         + ", serverDomains="
         + this.getServerDomains()
         + ", onlinePlayers="
         + this.getOnlinePlayers()
         + ", onlinePlayersTimeline="
         + this.getOnlinePlayersTimeline()
         + ", reason="
         + this.getReason()
         + ")";
   }

   public static class OnlinePlayersSample {
      @SerializedName("timestamp")
      private final long timestamp;
      @SerializedName("online_players")
      private final int onlinePlayers;

      public OnlinePlayersSample(long timestamp, int onlinePlayers) {
         this.timestamp = timestamp;
         this.onlinePlayers = onlinePlayers;
      }

      @Generated
      public long getTimestamp() {
         return this.timestamp;
      }

      @Generated
      public int getOnlinePlayers() {
         return this.onlinePlayers;
      }

      @Generated
      @Override
      public String toString() {
         return "ServerMetrics.OnlinePlayersSample(timestamp=" + this.getTimestamp() + ", onlinePlayers=" + this.getOnlinePlayers() + ")";
      }
   }
}
