package me.optif1ne.auction.command;

import me.optif1ne.auction.Auction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class AuctionCommand implements CommandExecutor {
    private final Auction plugin;

    public AuctionCommand(Auction plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessages().withPrefix("only-players"));
            return true;
        }

        if (!player.hasPermission("auction.use")) {
            player.sendMessage(plugin.getMessages().withPrefix("no-perm"));
            return true;
        }

        if (args.length == 0) {
            plugin.getGui().openMainMenu(player, 1);
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {
            if (!player.hasPermission("auction.add")) {
                player.sendMessage(plugin.getMessages().withPrefix("no-perm"));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(plugin.getMessages().withPrefix("usage-add", Map.of("label", label)));
                return false;
            }
            double price;
            try {
                price = Double.parseDouble(args[1]);
                if (price <= 0) throw new NumberFormatException();
            } catch (Exception exception) {
                player.sendMessage(plugin.getMessages().withPrefix("usage-add", Map.of("label", label)));
                return true;
            }

            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (inHand == null || inHand.getType().isAir()) {
                player.sendMessage(plugin.getMessages().withPrefix("hand-empty"));
                return true;
            }

            plugin.getService().addLot(player.getUniqueId(), inHand, price);
            player.getInventory().setItemInMainHand(null);
            player.sendMessage(plugin.getMessages().withPrefix("add-ok", Map.of("price", String.valueOf(price))));

            try { plugin.getStorage().save(plugin.getService().getAll()); } catch (Exception ignored) {}

            return true;
        }

        player.sendMessage(plugin.getMessages().withPrefix("usage-add", Map.of("label", label)));
        return true;
    }
}