package id.seria.gacha.lib.common.utils.api.authentication;

import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.util.Scanner;
import lombok.Generated;

public class BuildInfo {
   @SerializedName("download_hash")
   private final String downloadHash;
   @SerializedName("build_type")
   private final String buildType;
   @SerializedName("build_version")
   private final String buildVersion;
   @SerializedName("build_number")
   private final String buildNumber;
   @SerializedName("java_version")
   private final String javaVersion = System.getProperty("java.version");
   @SerializedName("os_name")
   private final String osName = System.getProperty("os.name");
   @SerializedName("os_arch")
   private final String osArch = System.getProperty("os.arch");
   @SerializedName("os_version")
   private final String osVersion = System.getProperty("os.version");
   @SerializedName("docker_detected")
   private final boolean dockerDetected = this.detectDocker();

   public BuildInfo(String downloadHash, String buildType, String buildVersion, String buildNumber) {
      this.downloadHash = downloadHash;
      this.buildType = buildType;
      this.buildVersion = buildVersion;
      this.buildNumber = buildNumber;
   }

   private boolean detectDocker() {
      try {
         if (new File("/.dockerenv").exists()) {
            return true;
         }

         File cgroup = new File("/proc/1/cgroup");
         if (cgroup.exists()) {
            Scanner scanner = new Scanner(cgroup);

            boolean var3;
            label57: {
               try {
                  while (scanner.hasNextLine()) {
                     if (scanner.nextLine().toLowerCase().contains("docker")) {
                        var3 = true;
                        break label57;
                     }
                  }
               } catch (Throwable var6) {
                  try {
                     scanner.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }

                  throw var6;
               }

               scanner.close();
               return false;
            }

            scanner.close();
            return var3;
         }
      } catch (Exception var7) {
      }

      return false;
   }

   @Generated
   public String getDownloadHash() {
      return this.downloadHash;
   }

   @Generated
   public String getBuildType() {
      return this.buildType;
   }

   @Generated
   public String getBuildVersion() {
      return this.buildVersion;
   }

   @Generated
   public String getBuildNumber() {
      return this.buildNumber;
   }

   @Generated
   public String getJavaVersion() {
      return this.javaVersion;
   }

   @Generated
   public String getOsName() {
      return this.osName;
   }

   @Generated
   public String getOsArch() {
      return this.osArch;
   }

   @Generated
   public String getOsVersion() {
      return this.osVersion;
   }

   @Generated
   public boolean isDockerDetected() {
      return this.dockerDetected;
   }

   @Generated
   @Override
   public String toString() {
      return "BuildInfo(downloadHash="
         + this.getDownloadHash()
         + ", buildType="
         + this.getBuildType()
         + ", buildVersion="
         + this.getBuildVersion()
         + ", buildNumber="
         + this.getBuildNumber()
         + ", javaVersion="
         + this.getJavaVersion()
         + ", osName="
         + this.getOsName()
         + ", osArch="
         + this.getOsArch()
         + ", osVersion="
         + this.getOsVersion()
         + ", dockerDetected="
         + this.isDockerDetected()
         + ")";
   }
}
