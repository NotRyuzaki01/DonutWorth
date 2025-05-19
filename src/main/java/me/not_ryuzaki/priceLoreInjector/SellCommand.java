package me.not_ryuzaki.priceLoreInjector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.util.*;

public class SellCommand implements CommandExecutor, Listener {
    private final PriceLoreInjector plugin;
    private final String SELL_GUI_TITLE = "Sell Items";

    public SellCommand(PriceLoreInjector plugin) {
        this.plugin = plugin;
    }

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
        if (!event.getView().getTitle().equals(SELL_GUI_TITLE)) return;

        Inventory inventory = event.getInventory();
        double total = 0.0;
        List<ItemStack> unsoldItems = new ArrayList<>();

        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                double basePrice = PriceLoreInjector.getMaterialPrices().getOrDefault(item.getType(), 0.0);
                double enchantPrice = 0.0;

                ItemMeta meta = item.getItemMeta();
                if (meta instanceof EnchantmentStorageMeta storageMeta) {
                    for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : storageMeta.getStoredEnchants().entrySet()) {
                        String key = entry.getKey().getKey().getKey().toUpperCase();
                        double perLevel = PriceLoreInjector.getEnchantmentPrices().getOrDefault(key, 0.0);
                        enchantPrice += perLevel * entry.getValue();
                    }
                } else if (meta != null && meta.hasEnchants()) {
                    for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                        String key = entry.getKey().getKey().getKey().toUpperCase();
                        double perLevel = PriceLoreInjector.getEnchantmentPrices().getOrDefault(key, 0.0);
                        enchantPrice += perLevel * entry.getValue();
                    }
                }

                if (basePrice > 0.0 || enchantPrice > 0.0) {
                    total += (basePrice + enchantPrice) * item.getAmount();
                } else {
                    unsoldItems.add(item.clone());
                }
            }
        }

        if (total > 0.0) {
            PriceLoreInjector.getEconomy().depositPlayer(player, total);
            player.sendMessage("§a+$" + PriceLoreInjector.formatPrice(total));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§a+$" + PriceLoreInjector.formatPrice(total)));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        } else {
            player.sendMessage("§cNo sellable items found.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);

            for (ItemStack unsold : inventory.getContents()) {
                if (unsold != null && unsold.getType() != Material.AIR) {
                    player.getInventory().addItem(unsold.clone());
                }
            }
        }
    }
}
