package me.optif1ne.auction.gui;

import me.optif1ne.auction.Auction;
import me.optif1ne.auction.model.AuctionItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MyLotsMenu {
    public static final String RAW_TITLE = "Мои лоты | стр. %d";

    private final Auction plugin;
    private final int page;
    private final Inventory inv;
    private final List<AuctionItem> pageLots;

    public MyLotsMenu(Auction plugin, UUID owner, int page) {
        this.plugin = plugin;
        this.page = Math.max(1, page);

        List<AuctionItem> mine = plugin.getService().getAll().stream()
                .filter(l -> l.getOwner().equals(owner))
                .collect(Collectors.toList());

        int from = Math.max(0, (this.page - 1) * 45);
        int to = Math.min(mine.size(), from + 45);
        this.pageLots = (from < to) ? new ArrayList<>(mine.subList(from, to)) : new ArrayList<>();

        this.inv = Bukkit.createInventory(null, 54, String.format(RAW_TITLE, this.page));
        build();
    }

    public void build() {
        for (int i = 0; i < pageLots.size(); i++) {
            var lot = pageLots.get(i);
            ItemStack it = lot.getItem();
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("§7Старт: §e" + lot.getStartPrice());
                lore.add("§7Текущая: §e" + lot.getCurrentPrice());
                lore.add("§a(Ваш лот)");
                meta.setLore(lore);
                it.setItemMeta(meta);
            }
            inv.setItem(i, it);
        }

        inv.setItem(45, button(Material.ARROW, "Прошлая страница"));
        inv.setItem(49, button(Material.BARRIER, "Назад в меню"));
        inv.setItem(53, button(Material.ARROW, "Следующая страница"));
    }

    private ItemStack button(Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            it.setItemMeta(meta);
        }
        return it;
    }

    public void open(Player player) {
        player.openInventory(inv);
    }
}
