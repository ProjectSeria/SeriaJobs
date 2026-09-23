package id.seria.gacha.sdk.core.license;

import com.google.gson.annotations.SerializedName;
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
   @SerializedName("online_players")
   private final int onlinePlayers;

   public ServerMetrics(String serverId, String serverVersion, String serverSoftware, String serverDomain, int onlinePlayers) {
      this.serverId = serverId;
      this.serverVersion = serverVersion;
      this.serverSoftware = serverSoftware;
      this.onlinePlayers = onlinePlayers;
      this.serverDomain = serverDomain;
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
   public int getOnlinePlayers() {
      return this.onlinePlayers;
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
         + ", onlinePlayers="
         + this.getOnlinePlayers()
         + ")";
   }
}
