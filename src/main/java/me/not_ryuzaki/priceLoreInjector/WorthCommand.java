package me.not_ryuzaki.priceLoreInjector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.not_ryuzaki.priceLoreInjector.PriceLoreInjector.formatPrice;

public class WorthCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return false;
        }

        if (args.length == 0) {
            player.sendMessage("§cPlease specify an item name!");
            return true;
        }

        String input = String.join("_", args).toUpperCase();
        Material material = Material.matchMaterial(input);

        if (material == null) {
            player.sendMessage("§cInvalid material name!");
            return true;
        }

        double unitPrice = PriceLoreInjector.getMaterialPrices().getOrDefault(material, 0.0);

        String name = material.name().toLowerCase().replace("_", " ");
        boolean capitalizeNext = true;
        StringBuilder result = new StringBuilder();

        for (char c : name.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        String finalName = result.toString();

        player.sendMessage("§7One " + finalName + " §7is Worth: §a$" + formatPrice(unitPrice));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(finalName + " §7is Worth: §a$" + formatPrice(unitPrice)));
        return true;
    }
}