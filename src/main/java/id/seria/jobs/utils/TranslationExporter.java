package id.seria.jobs.utils;

import id.seria.jobs.SeriaJobs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class TranslationExporter {

    public static void setupTranslationFolders(SeriaJobs plugin) {
        File dataFolder = plugin.getDataFolder();

        // 1. Create cache folder
        File cacheDir = new File(dataFolder, "cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        exportResourceIfNotExists(plugin, "cache/translations.cache.json", new File(cacheDir, "translations.cache.json"));

        // 2. Create translator folder
        File translatorDir = new File(dataFolder, "translator");
        if (!translatorDir.exists()) {
            translatorDir.mkdirs();
        }
        exportResourceIfNotExists(plugin, "translator/seriajobs.json", new File(translatorDir, "seriajobs.json"));

        // 3. Create translations folder (compatible with both SeriaTranslator & SeriaCore compile scanner)
        File translationsDir = new File(dataFolder, "translations");
        if (!translationsDir.exists()) {
            translationsDir.mkdirs();
        }
        exportResourceIfNotExists(plugin, "translator/seriajobs.json", new File(translationsDir, "seriajobs.json"));

        plugin.getLogger().info("Translation directories initialized: cache/, translator/, and translations/ are ready.");
    }

    private static void exportResourceIfNotExists(SeriaJobs plugin, String resourcePath, File targetFile) {
        if (targetFile.exists()) {
            return;
        }

        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) {
                // If resource not packaged under that exact name, write fallback
                targetFile.createNewFile();
                return;
            }

            try (OutputStream out = new FileOutputStream(targetFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            plugin.getLogger().info("Exported default resource to " + targetFile.getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to export " + resourcePath + ": " + e.getMessage());
        }
    }
}
