package me.not_ryuzaki.priceLoreInjector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static me.not_ryuzaki.priceLoreInjector.PriceLoreInjector.formatPrice;

public class WorthCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return false;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) {
            player.sendMessage("§cYou are not holding any item!");
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§7You are not holding any item!"));

        } else {
            Material material = heldItem.getType();
            double unitPrice = PriceLoreInjector.getMaterialPrices().getOrDefault(material, 0.0);

            String name = heldItem.getType().name().toLowerCase().replace("_", " ");

            Boolean capitalizeNext = true;
            StringBuilder result = new StringBuilder();

            for (char c : name.toCharArray()) {
                if (Character.isWhitespace(c)) {
                    capitalizeNext = true;
                    result.append(c);
                }
                else if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                }
                else{
                    result.append(c);
                }
            }
            String final_name = result.toString();

            player.sendMessage("§7One " + final_name + " §7is Worth: §a$" + formatPrice(unitPrice));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(" §7Worth: §a$" + formatPrice(unitPrice)));
        }
        return true;
    }
}
