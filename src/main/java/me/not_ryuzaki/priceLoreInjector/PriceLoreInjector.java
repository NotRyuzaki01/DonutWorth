package me.not_ryuzaki.priceLoreInjector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class PriceLoreInjector extends JavaPlugin implements Listener {

    private ProtocolManager protocolManager;
    private static final Map<Material, Double> materialPrices = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadPrices();

        protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL,
                PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                if (packet.getType() == PacketType.Play.Server.SET_SLOT) {
                    ItemStack item = packet.getItemModifier().read(0);
                    if (item != null && item.getType() != Material.AIR) {
                        packet.getItemModifier().write(0, injectPriceLore(item.clone()));
                    }
                } else if (packet.getType() == PacketType.Play.Server.WINDOW_ITEMS) {
                    List<ItemStack> items = packet.getItemListModifier().read(0);
                    List<ItemStack> newItems = new ArrayList<>();

                    for (ItemStack item : items) {
                        if (item != null && item.getType() != Material.AIR) {
                            newItems.add(injectPriceLore(item.clone()));
                        } else {
                            newItems.add(item);
                        }
                    }
                    packet.getItemListModifier().write(0, newItems);
                }
            }
        });

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("worth").setExecutor(new WorthCommand());

        getLogger().info("✅ PriceLoreInjector enabled with " + materialPrices.size() + " prices loaded!");
    }

    private void loadPrices() {
        materialPrices.clear();
        ConfigurationSection section = getConfig().getConfigurationSection("prices");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase());
                    double price = section.getDouble(key);
                    materialPrices.put(material, price);
                } catch (IllegalArgumentException ex) {
                    getLogger().warning("⚠️ Unknown material in config.yml: " + key);
                }
            }
        }
    }

    private ItemStack injectPriceLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.removeIf(line -> line.startsWith("§7Worth: §a$"));

        double unitPrice = materialPrices.getOrDefault(item.getType(), 0.0);
        double totalPrice = unitPrice * item.getAmount();

        if (totalPrice > 0.0) {
            lore.add("§7Worth: §a$" + formatPrice(totalPrice));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static String formatPrice(double price) {
        if (price >= 1_000_000_000) {
            return String.format("%.2fB", price / 1_000_000_000.0);
        } else if (price >= 1_000_000) {
            return String.format("%.2fM", price / 1_000_000.0);
        } else if (price >= 1_000) {
            return String.format("%.2fK", price / 1_000.0);
        } else {
            return String.format("%.2f", price);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            Bukkit.getScheduler().runTaskLater(this, player::updateInventory, 2L);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            Bukkit.getScheduler().runTaskLater(this, player::updateInventory, 2L);
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(this, player::updateInventory, 2L);
    }

    public static Map<Material, Double> getMaterialPrices() {
        return materialPrices;
    }
}
