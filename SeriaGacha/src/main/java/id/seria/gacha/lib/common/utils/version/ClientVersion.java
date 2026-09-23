package id.seria.gacha.lib.common.utils.version;

import java.util.HashMap;
import java.util.Map;

public enum ClientVersion {
   v1_8(47),
   v1_9(110),
   v1_10(210),
   v1_11(315),
   v1_12(340),
   v1_13(393, 401, 404),
   v1_14(477, 480, 485, 490, 498),
   v1_15(573, 575, 578),
   v1_16(735, 736, 751, 753, 754),
   v1_17(755, 756),
   v1_18(757, 758),
   v1_19(759, 760, 761, 762),
   v1_20(763, 764, 765, 766),
   v1_21(767),
   v26_1(768);

   private static final Map<Integer, ClientVersion> protocolToVersionMap = new HashMap<>();
   private int[] protocolVersions;

   ClientVersion(int... protocolVersions) {
      this.protocolVersions = protocolVersions;
   }

   public static ClientVersion findByProtocolVersion(int protocolVersion) {
      return protocolToVersionMap.get(protocolVersion);
   }

   static {
      for (ClientVersion version : values()) {
         for (int protocol : version.protocolVersions) {
            protocolToVersionMap.put(protocol, version);
         }
      }
   }
}
