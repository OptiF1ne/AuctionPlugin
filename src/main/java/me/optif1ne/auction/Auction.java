package me.optif1ne.auction;

import me.optif1ne.auction.gui.GuiManager;
import me.optif1ne.auction.listener.MenuClickListener;
import me.optif1ne.auction.service.AuctionService;
import me.optif1ne.auction.storage.Storage;
import me.optif1ne.auction.storage.YamlStorage;
import me.optif1ne.auction.util.Messages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Auction extends JavaPlugin {

    private GuiManager gui;
    private AuctionService service;
    private Storage storage;
    private int autosaveTaskId = -1;
    private Economy economy;
    private Messages messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();               // создаст config.yml, если нет

        this.gui = new GuiManager(this);
        this.service = new AuctionService();
        this.storage = new YamlStorage(this, "data.yml");
        this.messages = new Messages(this);


        if (!setupEconomy()) {
            getLogger().warning("Vault не найден!");
        }

        try {
            var loaded = storage.load();
            service.setAll(loaded);
            getLogger().info("Loaded lots: " + loaded.size());
        } catch (Exception e) {
            getLogger().warning("Failed to load lots: " + e.getMessage());
        }

        if (getCommand("auction") != null) {
            getCommand("auction").setExecutor(new me.optif1ne.auction.command.AuctionCommand(this));
        }
        getServer().getPluginManager().registerEvents(new MenuClickListener(this), this);

        startAutoSave();                   // ← автосейв
        getLogger().info("Auction has enabled!");
    }

    @Override
    public void onDisable() {
        stopAutoSave();
        try {
            storage.save(service.getAll());
            getLogger().info("Saved lots: " + service.totalLots());
        } catch (Exception e) {
            getLogger().warning("Failed to save lots: " + e.getMessage());
        }
        getLogger().info("Auction has disabled!");
    }

    // ------------ autosave ------------
    private void startAutoSave() {
        int minutes = Math.max(1, getConfig().getInt("autosave-minutes", 3));
        long period = minutes * 60L * 20L;
        autosaveTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            var snapshot = service.getAll();
            getServer().getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    storage.save(snapshot);
                } catch (Exception e) {
                    getLogger().warning("Autosave failed: " + e.getMessage());
                }
            });
        }, period, period);
        getLogger().info("Autosave every " + minutes + " min");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() { return economy; }

    public Messages getMessages() { return messages; }

    private void stopAutoSave() {
        if (autosaveTaskId != -1) {
            getServer().getScheduler().cancelTask(autosaveTaskId);
            autosaveTaskId = -1;
        }
    }

    public GuiManager getGui() { return gui; }
    public AuctionService getService() { return service; }
    public Storage getStorage() { return storage; }
}