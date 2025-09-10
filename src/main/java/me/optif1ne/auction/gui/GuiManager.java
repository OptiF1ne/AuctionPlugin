package me.optif1ne.auction.gui;

import me.optif1ne.auction.Auction;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuiManager {
    private final Auction plugin;
    private final Map<UUID, PendingConfirm> pending = new ConcurrentHashMap<>();

    public GuiManager(Auction plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player, int page) {
        new AuctionMenu(plugin, page).open(player);
    }

    public static final class PendingConfirm {
        private final UUID lotId;
        private final int returnPage;
        public PendingConfirm(UUID lotId, int returnPage) {
            this.lotId = lotId; this.returnPage = returnPage;
        }
        public UUID lotId() { return lotId; }
        public int returnPage() { return returnPage; }
    }

    public void setPending(Player p, UUID lotId, int page) {
        pending.put(p.getUniqueId(), new PendingConfirm(lotId, page));
    }

    public PendingConfirm consumePending(Player p) {
        return pending.remove(p.getUniqueId());
    }
}