package me.optif1ne.auction.gui;

import me.optif1ne.auction.Auction;
import me.optif1ne.auction.util.Names;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AuctionMenu {
    public static final String RAW_TITLE = "Аукцион | стр. %d";
    private final Auction plugin;
    private final int page;
    private final Inventory inv;

    public AuctionMenu(Auction plugin, int page) {
        this.plugin = plugin;
        this.page = Math.max(1, page);
        this.inv = Bukkit.createInventory(null, 54, String.format(RAW_TITLE, this.page));
        build();
    }

    private void build() {
        int pageSize = 45;
        var lots = plugin.getService().getPage(page, pageSize);

        for (int i = 0; i < lots.size(); i++) {
            var lot = lots.get(i);

            ItemStack it = lot.getItem();
            ItemMeta meta = it.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("§7Старт: §e" + lot.getStartPrice());
            lore.add("§7Текущая: §e" + lot.getCurrentPrice());
            lore.add("§7Владелец: §f" + (Names.nameOf(lot.getOwner()) != null ? Names.nameOf(lot.getOwner()) : "Неизвестно"));
            lore.add("§7Кликни, чтобы открыть");
            meta.setLore(lore);
            it.setItemMeta(meta);

            inv.setItem(i, it);
        }

        inv.setItem(45, button(Material.ARROW, "Прошлая страница"));
        inv.setItem(49, button(Material.BARRIER, "Закрыть меню"));
        inv.setItem(53, button(Material.ARROW, "Следующая страница"));
    }

    private ItemStack button(Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        it.setItemMeta(meta);
        return it;
    }

    public void open(Player player) {
        player.openInventory(inv);
    }

    public int getPage() {
        return page;
    }
}