package me.not_ryuzaki.priceLoreInjector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SellCommand implements CommandExecutor, Listener {
    private final PriceLoreInjector plugin;

    public SellCommand(PriceLoreInjector plugin) {
        this.plugin = plugin;
    }

    private final String SELL_GUI_TITLE = "Sell Items";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Inventory sellInventory = Bukkit.createInventory(player, 54, SELL_GUI_TITLE);
        player.openInventory(sellInventory);
        return true;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (event.getView().getTitle().equals(SELL_GUI_TITLE)) {
            Inventory inventory = event.getInventory();
            double total = 0.0;

            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    Map<Material, Double> prices = PriceLoreInjector.getMaterialPrices();
                    double price = prices.getOrDefault(item.getType(), 0.0);
                    total += price * item.getAmount();
                }
            }

            if (total > 0.0) {
                PriceLoreInjector.getEconomy().depositPlayer(player, total);
                player.sendMessage("§aYou sold your items for §6$" + PriceLoreInjector.formatPrice(total) + "§a!");
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§a+$" + PriceLoreInjector.formatPrice(total)));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            } else {
                player.sendMessage("§cNo sellable items found.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }
}