package me.optif1ne.auction.listener;

import me.optif1ne.auction.Auction;
import me.optif1ne.auction.gui.ConfirmMenu;
import me.optif1ne.auction.gui.OwnerConfirmMenu;
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
            int slot = e.getRawSlot();
            int topSize = e.getView().getTopInventory().getSize();
            if (slot < 0 || slot >= topSize) return;
            e.setCancelled(true);

            if (slot == 45) {
                int current = parsePage(title);
                int target = Math.max(1, current - 1);
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    plugin.getGui().openMainMenu(player, target);
                });
                return;
            }
            if (slot == 49) { player.closeInventory(); return; }
            if (slot == 53) {
                int current = parsePage(title);
                int target = current + 1;
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    plugin.getGui().openMainMenu(player, target);
                });
                return;
            }

            if (slot >= 0 && slot <= 44 && e.getCurrentItem() != null && !e.getCurrentItem().getType().isAir()) {
                int page = parsePage(title);
                int globalIndex = (page - 1) * 45 + slot;

                var lot = plugin.getService().getByIndex(globalIndex);
                if (lot != null) {
                    plugin.getGui().setPending(player, lot.getId(), page);
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        player.closeInventory();
                        if (lot.getOwner().equals(player.getUniqueId())) {
                            new OwnerConfirmMenu(plugin, lot, page).open(player);
                        } else {
                            new ConfirmMenu(plugin, lot, page).open(player);
                        }
                    });
                }
            }
            return;
        }

        if (title != null && title.startsWith("Покупка лота")) {
            int slot = e.getRawSlot();
            int topSize = e.getView().getTopInventory().getSize();
            if (slot < 0 || slot >= topSize) return;
            e.setCancelled(true);

            if (slot == 15) {
                int page = parseConfirmPage(title);
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    plugin.getGui().openMainMenu(player, page);
                });
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
                    econ.depositPlayer(player, price);
                }

                int back = pend.returnPage();
                player.closeInventory();
                plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getGui().openMainMenu(player, back));
                return;
            }
        }

        if (title != null && title.startsWith("Ваш лот")) {
            int slot = e.getRawSlot();
            int topSize = e.getView().getTopInventory().getSize();
            if (slot < 0 || slot >= topSize) return;
            e.setCancelled(true);

            if (slot == 15) { // Назад
                int page = parseOwnerPage(title);
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    plugin.getGui().openMainMenu(player, page);
                });
                return;
            }

            if (slot == 11) {
                var msg = plugin.getMessages();

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

                if (!lot.getOwner().equals(player.getUniqueId())) {
                    player.sendMessage(msg.withPrefix("no-perm"));
                    return;
                }

                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(msg.withPrefix("own-take-no-space"));
                    return;
                }

                var leftovers = player.getInventory().addItem(lot.getItem());
                if (!leftovers.isEmpty()) {
                    leftovers.values().forEach(st ->
                            player.getWorld().dropItemNaturally(player.getLocation(), st)
                    );
                    player.sendMessage(msg.withPrefix("own-take-dropped"));
                }

                boolean removed = plugin.getService().removeLot(lot.getId());
                if (removed) {
                    player.sendMessage(msg.withPrefix("own-take-ok"));
                    try { plugin.getStorage().save(plugin.getService().getAll()); } catch (Exception ignored) {}
                } else {
                    player.sendMessage(msg.withPrefix("buy-removed"));
                }

                int back = pend.returnPage();
                player.closeInventory();
                plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getGui().openMainMenu(player, back));
                return;
            }
        }
    }

    private int parseOwnerPage(String title) {
        try { return Integer.parseInt(title.replace("Ваш лот | стр. ", "").trim()); }
        catch (Exception e) { return 1; }
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