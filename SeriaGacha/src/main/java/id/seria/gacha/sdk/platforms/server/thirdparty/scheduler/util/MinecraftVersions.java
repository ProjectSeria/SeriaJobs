package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MinecraftVersions {
   @NotNull
   private static final Map<String, MinecraftVersions.Update> allUpdates = new LinkedHashMap<>();
   @NotNull
   private static final Map<String, MinecraftVersions.Version> allVersions = new LinkedHashMap<>();
   @Nullable
   private static MinecraftVersions.Version CURRENT = null;
   @NotNull
   public static final MinecraftVersions.Update WORLD_OF_COLOR = registerUpdate(new MinecraftVersions.Update(1, 12, update -> {
      update.version(0, 1);
      update.version(1, 1);
      update.version(2, 1);
   }));
   @NotNull
   public static final MinecraftVersions.Update UPDATE_AQUATIC = registerUpdate(new MinecraftVersions.Update(1, 13, update -> {
      update.version(0, 1);
      update.version(1, 2);
      update.version(2, 2);
   }));
   @NotNull
   public static final MinecraftVersions.Update VILLAGE_AND_PILLAGE = registerUpdate(new MinecraftVersions.Update(1, 14, update -> {
      update.version(0, 1);
      update.version(1, 1);
      update.version(2, 1);
      update.version(3, 1);
      update.version(4, 1);
   }));
   @NotNull
   public static final MinecraftVersions.Update BUZZY_BEES = registerUpdate(new MinecraftVersions.Update(1, 15, update -> {
      update.version(0, 1);
      update.version(1, 1);
      update.version(2, 1);
   }));
   @NotNull
   public static final MinecraftVersions.Update NETHER_UPDATE = registerUpdate(new MinecraftVersions.Update(1, 16, update -> {
      update.version(0, 1);
      update.version(1, 1);
      update.version(2, 2);
      update.version(3, 2);
      update.version(4, 3);
      update.version(5, 3);
   }));
   @NotNull
   public static final MinecraftVersions.Update CAVES_AND_CLIFFS_1 = registerUpdate(new MinecraftVersions.Update(1, 17, update -> {
      update.version(0, 1);
      update.version(1, 1);
   }));
   @NotNull
   public static final MinecraftVersions.Update CAVES_AND_CLIFFS_2 = registerUpdate(new MinecraftVersions.Update(1, 18, update -> {
      update.version(0, 1);
      update.version(1, 1);
      update.version(2, 2);
   }));
   @NotNull
   public static final MinecraftVersions.Update WILD_UPDATE = registerUpdate(new MinecraftVersions.Update(1, 19, update -> {
      update.version(0, 1);
      update.version(1, 1);
      update.version(2, 2);
      update.version(3, 3);
      update.version(4, 3);
   }));
   @NotNull
   public static final MinecraftVersions.Update TRAILS_AND_TAILS = registerUpdate(new MinecraftVersions.Update(1, 20, update -> {
      update.version(0, 1);
      update.version(1, 1);
      update.version(2, 2);
      update.version(3, 3);
      update.version(4, 3);
      update.version(5, 4);
      update.version(6, 4);
   }));
   @NotNull
   public static final MinecraftVersions.Update TRICKY_TRIALS = registerUpdate(new MinecraftVersions.Update(1, 21, update -> {
      update.version(0, 1);
      update.version(1, 1);
      update.version(2, 2);
      update.version(3, 2);
      update.version(4, 3);
      update.version(5, 4);
      update.version(6, 5);
   }));

   private MinecraftVersions() {
   }

   @NotNull
   private static MinecraftVersions.Update registerUpdate(@NotNull MinecraftVersions.Update update) {
      allUpdates.put(update.toString(), update);

      for (MinecraftVersions.Version version : update.versions) {
         allVersions.put(version.toString(), version);
      }

      return update;
   }

   @NotNull
   public static Map<String, MinecraftVersions.Update> updates() {
      return Collections.unmodifiableMap(allUpdates);
   }

   @NotNull
   public static Map<String, MinecraftVersions.Version> versions() {
      return Collections.unmodifiableMap(allVersions);
   }

   @NotNull
   public static MinecraftVersions.Version getCurrent() {
      if (CURRENT == null) {
         CURRENT = parseCurrentVersion(Bukkit.getVersion());
      }

      return CURRENT;
   }

   @NotNull
   static MinecraftVersions.Version parseCurrentVersion(@NotNull String versionString) {
      Pattern pattern = Pattern.compile("\\d+\\.\\d+(\\.\\d+)?");
      Matcher matcher = pattern.matcher(versionString);
      if (!matcher.find()) {
         throw new IllegalArgumentException("Could not find any version in: " + versionString);
      }

      String currentVersion = matcher.group();
      String[] parts = currentVersion.split("\\.");
      int major = Integer.parseInt(parts[0]);
      int minor = Integer.parseInt(parts[1]);
      int patch = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
      MinecraftVersions.Version version = allVersions.get(major + "." + minor + "." + patch);
      return version != null ? version : new MinecraftVersions.Version(major, minor, patch, -1);
   }

   public static class Update implements Comparable<MinecraftVersions.Update> {
      private final int major;
      private final int minor;
      private final List<MinecraftVersions.Version> versions = new ArrayList<>();
      private boolean lock = false;

      public Update(int major, int minor, @NotNull List<MinecraftVersions.Version> versions) {
         this.major = major;
         this.minor = minor;
         this.versions.addAll(versions);
         this.lock = true;
      }

      public Update(int major, int minor, @NotNull Consumer<MinecraftVersions.Update> init) {
         this.major = major;
         this.minor = minor;
         init.accept(this);
         this.lock = true;
      }

      public int getMajor() {
         return this.major;
      }

      public int getMinor() {
         return this.minor;
      }

      void version(int patch, int protocol) {
         if (this.lock) {
            throw new IllegalStateException("Cannot add versions after initialization");
         }

         MinecraftVersions.Version version = new MinecraftVersions.Version(this, patch, protocol);
         this.versions.add(version);
      }

      public boolean isCurrent() {
         return MinecraftVersions.getCurrent().getUpdate().equals(this);
      }

      public boolean isAbove() {
         return MinecraftVersions.getCurrent().getUpdate().compareTo(this) > 0;
      }

      public boolean isAtLeast() {
         return MinecraftVersions.getCurrent().getUpdate().compareTo(this) >= 0;
      }

      public boolean isBelow() {
         return MinecraftVersions.getCurrent().getUpdate().compareTo(this) < 0;
      }

      public boolean isAtMost() {
         return MinecraftVersions.getCurrent().getUpdate().compareTo(this) <= 0;
      }

      @NotNull
      public MinecraftVersions.Version get(int patch) {
         try {
            return this.versions.get(patch);
         } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Unknown version: " + this.major + "." + this.minor + "." + patch);
         }
      }

      public int compareTo(@NotNull MinecraftVersions.Update other) {
         int majorCompare = Integer.compare(this.major, other.major);
         return majorCompare != 0 ? majorCompare : Integer.compare(this.minor, other.minor);
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            MinecraftVersions.Update update = (MinecraftVersions.Update)o;
            return this.major == update.major && this.minor == update.minor && Objects.equals(this.versions, update.versions);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.major, this.minor, this.versions);
      }

      @NotNull
      @Override
      public String toString() {
         return this.major + "." + this.minor;
      }
   }

   public static class Version implements Comparable<MinecraftVersions.Version> {
      @NotNull
      private final MinecraftVersions.Update update;
      private final int major;
      private final int minor;
      private final int patch;
      private final int protocol;

      public Version(int major, int minor, int patch, int protocol) {
         this.major = major;
         this.minor = minor;
         this.patch = patch;
         this.protocol = protocol;
         this.update = new MinecraftVersions.Update(major, minor, Collections.singletonList(this));
      }

      public Version(@NotNull MinecraftVersions.Update update, int patch, int protocol) {
         this.update = update;
         this.major = update.major;
         this.minor = update.minor;
         this.patch = patch;
         this.protocol = protocol;
      }

      @NotNull
      public MinecraftVersions.Update getUpdate() {
         return this.update;
      }

      public int getMajor() {
         return this.major;
      }

      public int getMinor() {
         return this.minor;
      }

      public int getPatch() {
         return this.patch;
      }

      public int getProtocol() {
         return this.protocol;
      }

      public boolean isCurrent() {
         return MinecraftVersions.getCurrent().equals(this);
      }

      public boolean isAbove() {
         return MinecraftVersions.getCurrent().compareTo(this) > 0;
      }

      public boolean isAtLeast() {
         return MinecraftVersions.getCurrent().compareTo(this) >= 0;
      }

      public boolean isBelow() {
         return MinecraftVersions.getCurrent().compareTo(this) < 0;
      }

      public boolean isAtMost() {
         return MinecraftVersions.getCurrent().compareTo(this) <= 0;
      }

      @NotNull
      public String toProtocolString() {
         return "v" + this.major + "_" + this.minor + "_R" + this.protocol;
      }

      public int compareTo(@NotNull MinecraftVersions.Version other) {
         int majorCompare = Integer.compare(this.major, other.major);
         if (majorCompare != 0) {
            return majorCompare;
         }

         int minorCompare = Integer.compare(this.minor, other.minor);
         return minorCompare != 0 ? minorCompare : Integer.compare(this.patch, other.patch);
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            MinecraftVersions.Version version = (MinecraftVersions.Version)o;
            return this.major == version.major && this.minor == version.minor && this.patch == version.patch;
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.major, this.minor, this.patch);
      }

      @NotNull
      @Override
      public String toString() {
         return this.major + "." + this.minor + "." + this.patch;
      }
   }
}
