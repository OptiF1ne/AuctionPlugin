package me.optif1ne.auction.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

public class Messages {
    private final JavaPlugin plugin;
    private FileConfiguration cfg;

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        if (!new File(plugin.getDataFolder(), "messages.yml").exists()) {
            plugin.saveResource("messages.yml", false);
        }
        cfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
    }

    private String raw(String path) {
        return cfg.getString(path, path);
    }

    public String get(String path) {
        return ChatColor.translateAlternateColorCodes('&', raw(path));
    }

    public String f(String path, Map<String, String> vars) {
        String s = get(path);
        for (var e : vars.entrySet()) {
            s = s.replace("{" + e.getKey() + "}", e.getValue());
        }
        return s;
    }

    public String withPrefix(String path) {
        return get("prefix") + " " + get(path);
    }

    public String withPrefix(String path, Map<String,String> vars) {
        return get("prefix") + " " + f(path, vars);
    }
}
