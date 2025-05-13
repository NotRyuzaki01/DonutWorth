package me.not_ryuzaki.priceLoreInjector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import static me.not_ryuzaki.priceLoreInjector.PriceLoreInjector.formatPrice;

public class WorthCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return false;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /worth <itemname>");
            return true;
        }

        String input = args[0].toUpperCase().replace(" ", "_");
        Material material;
        try {
            material = Material.valueOf(input);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cUnknown item: " + args[0]);
            return true;
        }

        ItemStack item = new ItemStack(material);
        double basePrice = PriceLoreInjector.getMaterialPrices().getOrDefault(material, 0.0);
        double enchantPrice = 0.0;

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            for (var entry : storageMeta.getStoredEnchants().entrySet()) {
                String key = entry.getKey().getKey().getKey().toUpperCase();
                double perLevel = PriceLoreInjector.getEnchantmentPrices().getOrDefault(key, 0.0);
                enchantPrice += perLevel * entry.getValue();
            }
        } else if (meta != null && meta.hasEnchants()) {
            for (var entry : meta.getEnchants().entrySet()) {
                String key = entry.getKey().getKey().getKey().toUpperCase();
                double perLevel = PriceLoreInjector.getEnchantmentPrices().getOrDefault(key, 0.0);
                enchantPrice += perLevel * entry.getValue();
            }
        }

        double total = basePrice + enchantPrice;

        // Format material name to look better
        String name = material.name().toLowerCase().replace("_", " ");
        StringBuilder displayName = new StringBuilder();
        boolean capNext = true;
        for (char c : name.toCharArray()) {
            if (c == ' ') {
                capNext = true;
                displayName.append(c);
            } else if (capNext) {
                displayName.append(Character.toUpperCase(c));
                capNext = false;
            } else {
                displayName.append(c);
            }
        }

        String finalName = displayName.toString();

        if (total > 0.0) {
            player.sendMessage("§7One " + finalName + " §7is Worth: §a$" + formatPrice(total));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(finalName + " §7is Worth: §a$" + formatPrice(total)));
        } else {
            player.sendMessage("§cThat item has no value.");
        }

        return true;
    }
}
