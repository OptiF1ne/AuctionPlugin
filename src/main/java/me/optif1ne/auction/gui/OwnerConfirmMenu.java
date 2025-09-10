package me.optif1ne.auction.gui;

import me.optif1ne.auction.Auction;
import me.optif1ne.auction.model.AuctionItem;
import me.optif1ne.auction.util.Names;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class OwnerConfirmMenu {
    public static final String TITLE_RAW = "Ваш лот | стр. %d";

    private final Auction plugin;
    private final AuctionItem lot;
    private final int returnPage;

    public OwnerConfirmMenu(Auction plugin, AuctionItem lot, int returnPage) {
        this.plugin = plugin;
        this.lot = lot;
        this.returnPage = returnPage;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, String.format(TITLE_RAW, returnPage));

        ItemStack it = lot.getItem();
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setLore(List.of(
                    "§7Цена: §e" + lot.getCurrentPrice(),
                    "§7Владелец: §f" + (Names.nameOf(lot.getOwner()) != null ? Names.nameOf(lot.getOwner()) : "Неизвестно")
            ));
            it.setItemMeta(meta);
        }
        inv.setItem(13, it);

        inv.setItem(11, button(Material.LIME_WOOL, "§aЗабрать"));
        inv.setItem(15, button(Material.RED_WOOL, "§cНазад"));

        player.openInventory(inv);
    }

    private static ItemStack button(Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        it.setItemMeta(meta);
        return it;
    }
}
