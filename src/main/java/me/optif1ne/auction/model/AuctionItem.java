package me.optif1ne.auction.model;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class AuctionItem {
    private final UUID id = UUID.randomUUID();
    private final UUID owner;        // кто выставил
    private final ItemStack item;    // предмет
    private final double startPrice; // стартовая цена
    private double currentPrice;     // текущая цена (пока = стартовая)

    public AuctionItem(UUID owner, ItemStack item, double startPrice) {
        this.owner = owner;
        this.item = item.clone(); // чтобы не менять исходный предмет в руке
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
    }

    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }
    public ItemStack getItem() { return item.clone(); }
    public double getStartPrice() { return startPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
}