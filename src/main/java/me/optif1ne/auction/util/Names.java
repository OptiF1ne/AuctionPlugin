package me.optif1ne.auction.util;

import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Names {
    private static final Map<UUID, String> CACHE = new ConcurrentHashMap<>();

    private Names() {}

    public static String nameOf(UUID uuid) {
        return CACHE.computeIfAbsent(uuid, id -> {
            var p = Bukkit.getOfflinePlayer(id);
            return p != null ? p.getName() : null;
        });
    }
}