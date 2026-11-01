package id.seria.jobs.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Memastikan SeriaCore yang terpasang tidak lebih lama dari versi yang dipakai saat SeriaJobs di-build.
 * Versi minimum dibaca dari key {@code seriacore-min-version} di plugin.yml (diisi dari pom saat build),
 * sehingga salah pasang versi langsung terlihat saat enable, bukan sebagai NoSuchMethodError saat menu dibuka.
 */
public final class CoreVersionCheck {

    private static final String CORE_PLUGIN = "SeriaCore";
    private static final String MIN_VERSION_KEY = "seriacore-min-version";

    private CoreVersionCheck() {}

    /**
     * @return true bila SeriaCore memenuhi versi minimum (atau versi minimum tidak bisa dibaca), false bila terlalu lama.
     */
    public static boolean verify(@NotNull JavaPlugin plugin) {
        Logger log = plugin.getLogger();
        String required = readRequiredVersion(plugin);
        if (required == null) {
            log.warning("Versi minimum SeriaCore tidak ditemukan di plugin.yml, pengecekan versi dilewati.");
            return true;
        }

        Plugin core = Bukkit.getPluginManager().getPlugin(CORE_PLUGIN);
        String installed = core != null ? core.getPluginMeta().getVersion() : null;

        if (installed != null && isAtLeast(installed, required)) {
            return true;
        }

        log.severe("============================================================");
        log.severe(" SeriaCore terlalu lama untuk SeriaJobs v" + plugin.getPluginMeta().getVersion() + "!");
        log.severe("   Terpasang : SeriaCore " + (installed != null ? installed : "(tidak ada)"));
        log.severe("   Dibutuhkan: SeriaCore " + required + " atau lebih baru");
        log.severe(" Pasang SeriaCore yang sesuai lalu restart server.");
        log.severe(" SeriaJobs dinonaktifkan untuk mencegah error saat runtime.");
        log.severe("============================================================");
        return false;
    }

    @Nullable
    private static String readRequiredVersion(@NotNull JavaPlugin plugin) {
        try (InputStream in = plugin.getResource("plugin.yml")) {
            if (in == null) return null;
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
            String value = yml.getString(MIN_VERSION_KEY);
            // Placeholder yang tidak ter-filter (build tanpa Maven) dianggap tidak ada
            return value == null || value.isBlank() || value.contains("${") ? null : value.trim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Bandingkan MAJOR.MINOR.PATCH secara numerik; sufiks seperti -SNAPSHOT diabaikan.
     */
    public static boolean isAtLeast(@NotNull String installed, @NotNull String required) {
        int[] a = parse(installed);
        int[] b = parse(required);
        for (int i = 0; i < 3; i++) {
            if (a[i] != b[i]) return a[i] > b[i];
        }
        return true;
    }

    private static int[] parse(@NotNull String version) {
        int[] parts = new int[3];
        String core = version.trim().split("[-+ ]", 2)[0];
        String[] tokens = core.split("\\.");
        for (int i = 0; i < 3 && i < tokens.length; i++) {
            String digits = tokens[i].replaceAll("\\D.*$", "");
            parts[i] = digits.isEmpty() ? 0 : Integer.parseInt(digits);
        }
        return parts;
    }
}
