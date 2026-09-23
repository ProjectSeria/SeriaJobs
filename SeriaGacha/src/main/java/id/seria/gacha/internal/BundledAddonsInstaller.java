package id.seria.gacha.internal;

import id.seria.gacha.PhoenixCrates;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.bukkit.configuration.file.YamlConfiguration;

public final class BundledAddonsInstaller {
   private BundledAddonsInstaller() {
   }

   public static void ensurePresent(PhoenixCrates plugin) {
      File addonsFolder = new File(plugin.getDataFolder(), "addons");
      if (!addonsFolder.exists() && !addonsFolder.mkdirs()) {
         plugin.getLogger().warn("Could not create addons folder at " + addonsFolder.getAbsolutePath());
      } else {
         Map<String, List<BundledAddonsInstaller.InstalledAddonJar>> installedByIdentity = scanInstalledAddonJars(addonsFolder);

         for (BundledAddonsInstaller.BundledAddonJar bundled : scanBundledAddonJars(plugin)) {
            if (bundled.identity != null && !bundled.identity.isEmpty()) {
               List<BundledAddonsInstaller.InstalledAddonJar> installed = installedByIdentity.get(bundled.identity);
               BundledAddonsInstaller.InstalledAddonJar installedBest = installed != null && !installed.isEmpty() ? selectHighestVersion(installed) : null;
               int comparison = installedBest == null ? 1 : compareVersions(bundled.version, installedBest.version);
               boolean shouldInstall;
               String installReason;
               if (installedBest == null) {
                  shouldInstall = true;
                  installReason = "missing";
               } else if (comparison > 0) {
                  shouldInstall = true;
                  installReason = "newer-version";
               } else if (comparison == 0) {
                  boolean hasSameContent = hasSameContentForEqualVersion(installed, bundled);
                  shouldInstall = !hasSameContent;
                  installReason = hasSameContent ? "same-version-same-content" : "same-version-different-content";
               } else {
                  shouldInstall = false;
                  installReason = "older-version";
               }

               if (shouldInstall) {
                  File target = resolveTargetFile(addonsFolder, bundled, installed);
                  if (writeBundledAddon(plugin, target, bundled)) {
                     if (installed != null) {
                        for (BundledAddonsInstaller.InstalledAddonJar oldAddon : installed) {
                           if (oldAddon.file != null && oldAddon.file.exists() && !isSameFile(oldAddon.file, target) && !oldAddon.file.delete()) {
                              plugin.getLogger().warn("Failed to delete outdated addon jar: " + oldAddon.file.getAbsolutePath());
                           }
                        }
                     }

                     if (installedBest == null) {
                        plugin.getLogger()
                           .info("Installed bundled addon \"" + bundled.displayName + "\" v" + safeVersion(bundled.version) + " into addons folder.");
                     } else {
                        plugin.getLogger()
                           .info(
                              "Updated bundled addon \""
                                 + bundled.displayName
                                 + "\" from v"
                                 + safeVersion(installedBest.version)
                                 + " to v"
                                 + safeVersion(bundled.version)
                                 + " (reason: "
                                 + installReason
                                 + ")."
                           );
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean writeBundledAddon(PhoenixCrates plugin, File target, BundledAddonsInstaller.BundledAddonJar bundled) {
      try {
         java.nio.file.Files.write(target.toPath(), bundled.bytes);
         return true;
      } catch (Exception exception) {
         plugin.getLogger().warn("Failed to install bundled addon from resources/addons/" + bundled.fileName + ": " + exception.getMessage());
         return false;
      }
   }

   private static File resolveTargetFile(
      File addonsFolder, BundledAddonsInstaller.BundledAddonJar bundled, List<BundledAddonsInstaller.InstalledAddonJar> installed
   ) {
      String baseName = sanitizeFileNamePart(bundled.displayName);
      File target = new File(addonsFolder, baseName + ".jar");

      for (int suffix = 1; target.exists() && !isInstalledAs(installed, target); suffix++) {
         target = new File(addonsFolder, baseName + "-" + suffix + ".jar");
      }

      return target;
   }

   private static boolean isInstalledAs(List<BundledAddonsInstaller.InstalledAddonJar> installed, File file) {
      if (installed == null) {
         return false;
      }

      for (BundledAddonsInstaller.InstalledAddonJar addon : installed) {
         if (addon.file != null && isSameFile(addon.file, file)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isSameFile(File left, File right) {
      try {
         return left.getCanonicalFile().equals(right.getCanonicalFile());
      } catch (IOException exception) {
         return left.getAbsoluteFile().equals(right.getAbsoluteFile());
      }
   }

   private static Map<String, List<BundledAddonsInstaller.InstalledAddonJar>> scanInstalledAddonJars(File addonsFolder) {
      Map<String, List<BundledAddonsInstaller.InstalledAddonJar>> installed = new HashMap<>();
      File[] jars = addonsFolder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".jar"));
      if (jars == null) {
         return installed;
      }

      for (File jar : jars) {
         try {
            BundledAddonsInstaller.AddonDescriptor descriptor = readAddonDescriptorFromJar(jar.toPath());
            if (descriptor != null && descriptor.identity != null) {
               installed.computeIfAbsent(descriptor.identity, key -> new ArrayList<>())
                  .add(new BundledAddonsInstaller.InstalledAddonJar(jar, descriptor.version));
            }
         } catch (Exception var8) {
         }
      }

      return installed;
   }

   private static BundledAddonsInstaller.InstalledAddonJar selectHighestVersion(List<BundledAddonsInstaller.InstalledAddonJar> installed) {
      return installed.stream().max(Comparator.comparing(addon -> addon.version, (left, right) -> compareVersions(left, right))).orElse(installed.get(0));
   }

   private static List<BundledAddonsInstaller.BundledAddonJar> scanBundledAddonJars(PhoenixCrates plugin) {
      List<BundledAddonsInstaller.BundledAddonJar> bundled = new ArrayList<>();

      try {
         URL location = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
         if (location == null) {
            return bundled;
         }

         Path path = Paths.get(location.toURI());
         if (java.nio.file.Files.isRegularFile(path)) {
            scanBundledFromPluginJar(path, bundled);
         } else if (java.nio.file.Files.isDirectory(path)) {
            scanBundledFromClassesDirectory(path.resolve("addons"), bundled);
         }
      } catch (IOException | URISyntaxException exception) {
         plugin.getLogger().warn("Could not scan bundled addons: " + exception.getMessage());
      }

      return bundled;
   }

   private static void scanBundledFromPluginJar(Path pluginJar, List<BundledAddonsInstaller.BundledAddonJar> out) throws IOException {
      try (ZipFile zipFile = new ZipFile(pluginJar.toFile())) {
         Enumeration<? extends ZipEntry> entries = zipFile.entries();

         while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!entry.isDirectory() && name.startsWith("addons/") && name.toLowerCase(Locale.ROOT).endsWith(".jar")) {
               try (InputStream stream = zipFile.getInputStream(entry)) {
                  byte[] bytes = readAllBytes(stream);
                  BundledAddonsInstaller.AddonDescriptor descriptor = readAddonDescriptorFromJarBytes(bytes);
                  if (descriptor != null && descriptor.identity != null) {
                     String fileName = name.substring(name.lastIndexOf(47) + 1);
                     out.add(new BundledAddonsInstaller.BundledAddonJar(fileName, descriptor.displayName, descriptor.identity, descriptor.version, bytes));
                  }
               }
            }
         }
      }
   }

   private static void scanBundledFromClassesDirectory(Path addonsDir, List<BundledAddonsInstaller.BundledAddonJar> out) throws IOException {
      if (java.nio.file.Files.isDirectory(addonsDir)) {
         try (Stream<Path> stream = java.nio.file.Files.list(addonsDir)) {
            stream.filter(p -> java.nio.file.Files.isRegularFile(p) && p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
               .forEach(
                  path -> {
                     try {
                        BundledAddonsInstaller.AddonJarMetadata metadata = readAddonJarMetadata(path);
                        if (metadata == null || metadata.descriptor == null || metadata.descriptor.identity == null) {
                           return;
                        }

                        out.add(
                           new BundledAddonsInstaller.BundledAddonJar(
                              path.getFileName().toString(),
                              metadata.descriptor.displayName,
                              metadata.descriptor.identity,
                              metadata.descriptor.version,
                              metadata.bytes
                           )
                        );
                     } catch (Exception var3) {
                     }
                  }
               );
         }
      }
   }

   private static BundledAddonsInstaller.AddonJarMetadata readAddonJarMetadata(Path path) throws IOException {
      byte[] bytes = java.nio.file.Files.readAllBytes(path);
      BundledAddonsInstaller.AddonDescriptor descriptor = readAddonDescriptorFromJarBytes(bytes);
      return descriptor == null ? null : new BundledAddonsInstaller.AddonJarMetadata(descriptor, sha256(bytes), bytes);
   }

   private static BundledAddonsInstaller.AddonDescriptor readAddonDescriptorFromJar(Path path) throws IOException {
      try (ZipFile zipFile = new ZipFile(path.toFile())) {
         Enumeration<? extends ZipEntry> entries = zipFile.entries();

         while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (!entry.isDirectory() && (entryName.equalsIgnoreCase("addon.yml") || entryName.toLowerCase(Locale.ROOT).endsWith("/addon.yml"))) {
               try (InputStream stream = zipFile.getInputStream(entry)) {
                  byte[] ymlBytes = readAllBytes(stream);
                  return readAddonDescriptorFromYaml(ymlBytes);
               }
            }
         }
      }

      return null;
   }

   private static boolean hasSameContentForEqualVersion(
      List<BundledAddonsInstaller.InstalledAddonJar> installed, BundledAddonsInstaller.BundledAddonJar bundled
   ) {
      String bundledHash = sha256(bundled.bytes);

      for (BundledAddonsInstaller.InstalledAddonJar old : installed) {
         if (old != null && old.file != null && old.file.exists() && compareVersions(old.version, bundled.version) == 0) {
            try {
               String installedHash = sha256(old.file.toPath());
               if (bundledHash.equalsIgnoreCase(installedHash)) {
                  return true;
               }
            } catch (IOException var6) {
            }
         }
      }

      return false;
   }

   private static BundledAddonsInstaller.AddonDescriptor readAddonDescriptorFromJarBytes(byte[] jarBytes) throws IOException {
      ZipEntry entry;
      try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(jarBytes))) {
         while ((entry = zipInputStream.getNextEntry()) != null) {
            String entryName = entry.getName();
            if (!entry.isDirectory() && (entryName.equalsIgnoreCase("addon.yml") || entryName.toLowerCase(Locale.ROOT).endsWith("/addon.yml"))) {
               byte[] ymlBytes = readAllBytes(zipInputStream);
               return readAddonDescriptorFromYaml(ymlBytes);
            }
         }
      }

      return null;
   }

   private static BundledAddonsInstaller.AddonDescriptor readAddonDescriptorFromYaml(byte[] ymlBytes) {
      YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(new ByteArrayInputStream(ymlBytes), StandardCharsets.UTF_8));
      String main = normalize(yaml.getString("main"));
      String name = normalize(yaml.getString("name"));
      String version = normalize(yaml.getString("version"));
      String identity = main != null ? main.toLowerCase(Locale.ROOT) : (name == null ? null : name.toLowerCase(Locale.ROOT));
      if (identity == null) {
         return null;
      }

      String displayName = name != null ? name : identity;
      return new BundledAddonsInstaller.AddonDescriptor(identity, displayName, version);
   }

   private static int compareVersions(String leftVersion, String rightVersion) {
      String left = normalize(leftVersion);
      String right = normalize(rightVersion);
      if (left == null && right == null) {
         return 0;
      }

      if (left == null) {
         return -1;
      }

      if (right == null) {
         return 1;
      }

      if (left.equalsIgnoreCase(right)) {
         return 0;
      }

      String[] leftParts = left.split("[^0-9A-Za-z]+");
      String[] rightParts = right.split("[^0-9A-Za-z]+");
      int maxLength = Math.max(leftParts.length, rightParts.length);

      for (int i = 0; i < maxLength; i++) {
         String leftPart = i < leftParts.length ? leftParts[i] : "0";
         String rightPart = i < rightParts.length ? rightParts[i] : "0";
         if (!leftPart.equalsIgnoreCase(rightPart)) {
            boolean leftNumeric = leftPart.matches("\\d+");
            boolean rightNumeric = rightPart.matches("\\d+");
            if (leftNumeric && rightNumeric) {
               int numberCompare = compareNumericStrings(leftPart, rightPart);
               if (numberCompare != 0) {
                  return numberCompare;
               }
            } else {
               if (leftNumeric != rightNumeric) {
                  return leftNumeric ? 1 : -1;
               }

               int textCompare = leftPart.compareToIgnoreCase(rightPart);
               if (textCompare != 0) {
                  return textCompare;
               }
            }
         }
      }

      return left.compareToIgnoreCase(right);
   }

   private static int compareNumericStrings(String left, String right) {
      String normalizedLeft = stripLeadingZeros(left);
      String normalizedRight = stripLeadingZeros(right);
      return normalizedLeft.length() != normalizedRight.length()
         ? Integer.compare(normalizedLeft.length(), normalizedRight.length())
         : normalizedLeft.compareTo(normalizedRight);
   }

   private static String stripLeadingZeros(String value) {
      String trimmed = value.replaceFirst("^0+", "");
      return trimmed.isEmpty() ? "0" : trimmed;
   }

   private static String sanitizeFileNamePart(String value) {
      String normalized = normalize(value);
      if (normalized == null) {
         return "addon";
      }

      String sanitized = normalized.replaceAll("[^A-Za-z0-9._-]+", "-").replaceAll("-+", "-").replaceAll("^-|-$", "");
      return sanitized.isEmpty() ? "addon" : sanitized;
   }

   private static String safeVersion(String value) {
      return normalize(value) == null ? "unknown" : value;
   }

   private static String sha256(byte[] data) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         byte[] hash = digest.digest(data);
         return hex(hash);
      } catch (NoSuchAlgorithmException exception) {
         throw new IllegalStateException("SHA-256 not available", exception);
      }
   }

   private static String sha256(Path path) throws IOException {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");

         try (
            InputStream in = new BufferedInputStream(java.nio.file.Files.newInputStream(path));
            DigestInputStream dis = new DigestInputStream(in, digest);
         ) {
            byte[] buffer = new byte[8192];

            while (dis.read(buffer) != -1) {
            }
         }

         return hex(digest.digest());
      } catch (NoSuchAlgorithmException exception) {
         throw new IllegalStateException("SHA-256 not available", exception);
      }
   }

   private static String hex(byte[] bytes) {
      StringBuilder builder = new StringBuilder(bytes.length * 2);

      for (byte b : bytes) {
         builder.append(String.format("%02x", b));
      }

      return builder.toString();
   }

   private static byte[] readAllBytes(InputStream inputStream) throws IOException {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];

      int read;
      while ((read = inputStream.read(buffer)) != -1) {
         outputStream.write(buffer, 0, read);
      }

      return outputStream.toByteArray();
   }

   private static String normalize(String value) {
      if (value == null) {
         return null;
      }

      String trimmed = value.trim();
      return trimmed.isEmpty() ? null : trimmed;
   }

   private static final class AddonDescriptor {
      private final String identity;
      private final String displayName;
      private final String version;

      private AddonDescriptor(String identity, String displayName, String version) {
         this.identity = identity;
         this.displayName = displayName;
         this.version = version;
      }
   }

   private static final class AddonJarMetadata {
      private final BundledAddonsInstaller.AddonDescriptor descriptor;
      private final String sha256;
      private final byte[] bytes;

      private AddonJarMetadata(BundledAddonsInstaller.AddonDescriptor descriptor, String sha256, byte[] bytes) {
         this.descriptor = descriptor;
         this.sha256 = sha256;
         this.bytes = bytes;
      }
   }

   private static final class BundledAddonJar {
      private final String fileName;
      private final String displayName;
      private final String identity;
      private final String version;
      private final byte[] bytes;

      private BundledAddonJar(String fileName, String displayName, String identity, String version, byte[] bytes) {
         this.fileName = fileName;
         this.displayName = displayName;
         this.identity = identity;
         this.version = version;
         this.bytes = bytes;
      }
   }

   private static final class InstalledAddonJar {
      private final File file;
      private final String version;

      private InstalledAddonJar(File file, String version) {
         this.file = file;
         this.version = version;
      }
   }
}
