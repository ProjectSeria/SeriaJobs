package id.seria.bossevent.service;

import id.seria.bossevent.SeriaBossEventPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerInfoService {

    private final SeriaBossEventPlugin plugin;
    private LuckPerms luckPerms;
    private final LegacyComponentSerializer legacySerializer;

    private final Map<UUID, String> cachedNames = new ConcurrentHashMap<>();
    private final Map<UUID, Component> cachedPrefixes = new ConcurrentHashMap<>();
    private final Map<UUID, String> cachedGroups = new ConcurrentHashMap<>();

    public PlayerInfoService(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
        this.legacySerializer = LegacyComponentSerializer.builder()
                .character('&')
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();
        initLuckPerms();
    }

    private void initLuckPerms() {
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            try {
                this.luckPerms = LuckPermsProvider.get();
            } catch (Throwable t) {
                plugin.getLogger().warning("[SeriaBossEvent] Could not get LuckPerms provider: " + t.getMessage());
            }
        }
    }

    public LuckPerms getLuckPerms() {
        if (luckPerms == null) {
            initLuckPerms();
        }
        return luckPerms;
    }

    public Component parsePrefix(String rawPrefix) {
        if (rawPrefix == null || rawPrefix.isBlank()) {
            return Component.empty();
        }
        try {
            String normalized = rawPrefix.replace('§', '&');
            return legacySerializer.deserialize(normalized);
        } catch (Exception e) {
            return Component.text(rawPrefix);
        }
    }

    public String getPrimaryGroup(Player player) {
        if (player == null) return "default";
        return getPrimaryGroup(player.getUniqueId());
    }

    public String getPrimaryGroup(UUID uuid) {
        if (luckPerms != null) {
            try {
                User user = luckPerms.getUserManager().getUser(uuid);
                if (user != null) {
                    String group = user.getPrimaryGroup();
                    if (group != null) {
                        cachedGroups.put(uuid, group);
                        return group;
                    }
                }
            } catch (Throwable ignored) {}
        }
        return cachedGroups.getOrDefault(uuid, "default");
    }

    public CompletableFuture<String> loadPrimaryGroupAsync(UUID uuid) {
        if (luckPerms != null) {
            User cachedUser = luckPerms.getUserManager().getUser(uuid);
            if (cachedUser != null) {
                String group = cachedUser.getPrimaryGroup();
                cachedGroups.put(uuid, group);
                return CompletableFuture.completedFuture(group);
            }

            return luckPerms.getUserManager().loadUser(uuid).thenApplyAsync(user -> {
                if (user != null) {
                    String group = user.getPrimaryGroup();
                    cachedGroups.put(uuid, group);
                    return group;
                }
                return cachedGroups.getOrDefault(uuid, "default");
            });
        }
        return CompletableFuture.completedFuture(cachedGroups.getOrDefault(uuid, "default"));
    }

    public Component getPrefix(Player player) {
        if (player == null) return Component.empty();
        return getPrefix(player.getUniqueId());
    }

    public Component getPrefix(UUID uuid) {
        if (luckPerms != null) {
            try {
                User user = luckPerms.getUserManager().getUser(uuid);
                if (user != null) {
                    String rawPrefix = user.getCachedData().getMetaData().getPrefix();
                    Component comp = parsePrefix(rawPrefix);
                    cachedPrefixes.put(uuid, comp);
                    return comp;
                }
            } catch (Throwable ignored) {}
        }
        return cachedPrefixes.getOrDefault(uuid, Component.empty());
    }

    public String getPlayerName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            cachedNames.put(uuid, online.getName());
            return online.getName();
        }
        String cached = cachedNames.get(uuid);
        if (cached != null) return cached;

        OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
        if (offline.getName() != null) {
            cachedNames.put(uuid, offline.getName());
            return offline.getName();
        }
        return "Unknown";
    }

    public void cachePlayer(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        cachedNames.put(uuid, player.getName());
        Component prefix = getPrefix(player);
        if (prefix != null && !prefix.equals(Component.empty())) {
            cachedPrefixes.put(uuid, prefix);
        }
        String group = getPrimaryGroup(player);
        if (group != null) {
            cachedGroups.put(uuid, group);
        }
    }

    public void snapshot(UUID uuid, String name, Component prefix, String group) {
        if (name != null) cachedNames.put(uuid, name);
        if (prefix != null) cachedPrefixes.put(uuid, prefix);
        if (group != null) cachedGroups.put(uuid, group);
    }
}
