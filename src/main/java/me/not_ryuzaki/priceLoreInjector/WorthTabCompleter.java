package me.not_ryuzaki.priceLoreInjector;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WorthTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toUpperCase();
            for (Material material : Material.values()) {
                if (material.name().startsWith(input)) {
                    suggestions.add(material.name().toLowerCase());
                }
            }
        }
        return suggestions;
    }
}
