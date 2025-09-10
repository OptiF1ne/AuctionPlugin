package me.optif1ne.auction.service;

import me.optif1ne.auction.model.AuctionItem;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AuctionService {
    private final List<AuctionItem> lots = new ArrayList<>();

    public AuctionItem addLot(UUID owner, ItemStack item, double price) {
        AuctionItem lot = new AuctionItem(owner, item, price);
        lots.add(lot);
        return lot;
    }

    public AuctionItem getById(java.util.UUID id) {
        for (AuctionItem l : lots) {
            if (l.getId().equals(id)) return l;
        }
        return null;
    }

    public boolean removeLot(UUID id) {
        return lots.removeIf(l -> l.getId().equals(id));
    }

    public AuctionItem getByIndex(int index) {
        if (index < 0 || index >= lots.size()) return null;
        return lots.get(index);
    }

    public List<AuctionItem> getPage(int page, int pageSize) {
        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(lots.size(), from + pageSize);
        if (from >= to) return Collections.emptyList();
        return lots.subList(from, to);
    }

    public int totalLots() {
        return lots.size();
    }

    public void setAll(List<AuctionItem> newLots) {
        lots.clear();
        if (newLots != null) lots.addAll(newLots);
    }

    public List<AuctionItem> getAll() {
        return new ArrayList<>(lots);
    }
}