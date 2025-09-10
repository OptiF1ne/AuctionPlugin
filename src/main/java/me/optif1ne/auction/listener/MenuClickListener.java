package me.optif1ne.auction.listener;

import me.optif1ne.auction.Auction;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;

public class MenuClickListener implements Listener {
    private final Auction plugin;

    public MenuClickListener(Auction plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String title = ChatColor.stripColor(e.getView().getTitle());
        if (!(e.getWhoClicked() instanceof Player player)) return;

        // --- наше главное меню аукциона
        if (title != null && title.startsWith("Аукцион | стр.")) {
            e.setCancelled(true);

            int slot = e.getRawSlot();
            if (slot < 0) return;

            if (slot == 45) {
                int current = parsePage(title);
                plugin.getGui().openMainMenu(player, Math.max(1, current - 1));
                return;
            }
            if (slot == 49) { player.closeInventory(); return; }
            if (slot == 53) {
                int current = parsePage(title);
                plugin.getGui().openMainMenu(player, current + 1);
                return;
            }

            if (slot >= 0 && slot <= 44 && e.getCurrentItem() != null) {
                int page = parsePage(title);
                int globalIndex = (page - 1) * 45 + slot;

                var lot = plugin.getService().getByIndex(globalIndex);
                if (lot != null) {
                    // запомним выбор
                    plugin.getGui().setPending(player, lot.getId(), page);
                    // откроем подтверждение
                    new me.optif1ne.auction.gui.ConfirmMenu(plugin, lot, page).open(player);
                }
            }
            return;
        }

        if (title != null && title.startsWith("Покупка лота")) {
            e.setCancelled(true);
            int slot = e.getRawSlot();
            if (slot < 0) return;

            if (slot == 15) {
                int page = parseConfirmPage(title);
                plugin.getGui().openMainMenu(player, page);
                return;
            }

            if (slot == 11) {
                var msg = plugin.getMessages();

                if (!player.hasPermission("auction.buy")) {
                    player.sendMessage(msg.withPrefix("no-perm"));
                    return;
                }

                if (plugin.getEconomy() == null) {
                    player.sendMessage(msg.withPrefix("buy-disabled"));
                    player.closeInventory();
                    return;
                }

                var pend = plugin.getGui().consumePending(player);
                if (pend == null) {
                    player.sendMessage(msg.withPrefix("session-missing"));
                    player.closeInventory();
                    return;
                }

                var lot = plugin.getService().getById(pend.lotId());
                if (lot == null) {
                    player.sendMessage(msg.withPrefix("lot-unavailable"));
                    player.closeInventory();
                    plugin.getGui().openMainMenu(player, pend.returnPage());
                    return;
                }

                if (lot.getOwner().equals(player.getUniqueId())) {
                    player.sendMessage(msg.withPrefix("buy-self"));
                    return;
                }
                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(msg.withPrefix("buy-no-space"));
                    return;
                }

                double price = lot.getCurrentPrice();
                var econ = plugin.getEconomy();

                var withdraw = econ.withdrawPlayer(player, price);
                if (!withdraw.transactionSuccess()) {
                    player.sendMessage(msg.withPrefix("buy-no-money", Map.of("price", String.valueOf(price))));
                    return;
                }

                var seller = plugin.getServer().getOfflinePlayer(lot.getOwner());
                var deposit = econ.depositPlayer(seller, price);
                if (!deposit.transactionSuccess()) {
                    econ.depositPlayer(player, price); // откат
                    player.sendMessage(msg.withPrefix("buy-deposit-error"));
                    return;
                }

                var leftovers = player.getInventory().addItem(lot.getItem());
                if (!leftovers.isEmpty()) {
                    leftovers.values().forEach(st ->
                            player.getWorld().dropItemNaturally(player.getLocation(), st)
                    );
                }

                boolean removed = plugin.getService().removeLot(lot.getId());

                if (removed) {
                    player.sendMessage(msg.withPrefix("buy-ok-buyer", Map.of("price", String.valueOf(price))));
                    if (seller.isOnline() && seller.getPlayer() != null) {
                        seller.getPlayer().sendMessage(
                                msg.withPrefix("buy-ok-seller", Map.of("buyer", player.getName(), "price", String.valueOf(price)))
                        );
                    }
                    try { plugin.getStorage().save(plugin.getService().getAll()); } catch (Exception ignored) {}
                } else {
                    player.sendMessage(msg.withPrefix("buy-removed"));
                    econ.depositPlayer(player, price); // вернуть деньги покупателю
                }

                player.closeInventory();
                plugin.getGui().openMainMenu(player, pend.returnPage());
                return;
            }
        }
    }

    private int parsePage(String title) {
        try {
            return Integer.parseInt(
                    title.replace("Аукцион | стр. ", "").trim()
            );
        } catch (Exception ex) {
            return 1;
        }
    }

    private int parseConfirmPage(String title) {
        try {
            return Integer.parseInt(title.replace("Покупка лота | стр. ", "").trim());
        } catch (Exception e) {
            return 1;
        }
    }
}