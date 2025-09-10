package me.optif1ne.auction.storage;

import me.optif1ne.auction.Auction;
import me.optif1ne.auction.model.AuctionItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class YamlStorage implements Storage {
    private final Auction plugin;
    private final File file;

    public YamlStorage(Auction plugin, String fileName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), fileName);
    }

    @Override
    public List<AuctionItem> load() throws IOException {
        if (!file.exists()) return Collections.emptyList();
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = cfg.getConfigurationSection("lots");
        if (sec == null) return Collections.emptyList();

        List<AuctionItem> result = new ArrayList<>();
        for (String key : sec.getKeys(false)) {
            try {
                UUID owner = UUID.fromString(sec.getString(key + ".owner"));
                ItemStack item = sec.getItemStack(key + ".item");
                double start = sec.getDouble(key + ".start");
                double current = sec.getDouble(key + ".current", start);
                if (owner == null || item == null) continue;

                AuctionItem lot = new AuctionItem(owner, item, start);
                lot.setCurrentPrice(current);
                result.add(lot);
            } catch (Exception ignore) { /* можно залогировать */ }
        }
        return result;
    }

    @Override
    public void save(List<AuctionItem> lots) throws IOException {
        YamlConfiguration cfg = new YamlConfiguration();
        int i = 0;
        for (AuctionItem lot : lots) {
            String base = "lots." + (i++);
            cfg.set(base + ".owner", lot.getOwner().toString());
            cfg.set(base + ".item", lot.getItem());    // Bukkit сам сериализует ItemStack
            cfg.set(base + ".start", lot.getStartPrice());
            cfg.set(base + ".current", lot.getCurrentPrice());
        }
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        cfg.save(file);
    }
}