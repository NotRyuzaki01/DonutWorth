package me.not_ryuzaki.priceLoreInjector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class PriceLoreInjector extends JavaPlugin implements Listener {

    private static final Map<Material, Double> materialPrices = new HashMap<>();
    private static final Map<String, Double> enchantmentPrices = new HashMap<>();
    private static Economy economy = null;
    private static final Set<UUID> playersWithItemsInCursor = new HashSet<>();

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("❌ Disabled because Vault or an economy plugin (like EssentialsX) was not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        loadPrices();

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL,
                PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();

                if (playersWithItemsInCursor.contains(player.getUniqueId())) return;

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
        getCommand("sell").setExecutor(new SellCommand(this));
        getServer().getPluginManager().registerEvents(new SellCommand(this), this);
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

        enchantmentPrices.clear();
        ConfigurationSection enchants = getConfig().getConfigurationSection("enchantments");
        if (enchants != null) {
            for (String enchantKey : enchants.getKeys(false)) {
                double price = enchants.getDouble(enchantKey);
                enchantmentPrices.put(enchantKey.toUpperCase(), price);
            }
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        var rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return true;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static Map<Material, Double> getMaterialPrices() {
        return materialPrices;
    }

    public static Map<String, Double> getEnchantmentPrices() {
        return enchantmentPrices;
    }

    private ItemStack injectPriceLore(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return item;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (meta.hasLore() && !meta.getLore().isEmpty()) return item;

        if (meta.hasDisplayName()) {
            String displayName = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();
            String defaultName = item.getType().name().toLowerCase().replace('_', ' ');
            if (!displayName.contains(defaultName)) return item;
        }

        double basePrice = materialPrices.getOrDefault(item.getType(), 0.0);
        double enchantPrice = 0.0;

        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            for (var entry : storageMeta.getStoredEnchants().entrySet()) {
                String key = entry.getKey().getKey().getKey().toUpperCase();
                enchantPrice += enchantmentPrices.getOrDefault(key, 0.0) * entry.getValue();
            }
        } else if (meta.hasEnchants()) {
            for (var entry : meta.getEnchants().entrySet()) {
                String key = entry.getKey().getKey().getKey().toUpperCase();
                enchantPrice += enchantmentPrices.getOrDefault(key, 0.0) * entry.getValue();
            }
        }

        double total = (basePrice + enchantPrice) * item.getAmount();
        if (total <= 0.0) return item;

        List<String> lore = new ArrayList<>();
        lore.add("§7Worth: §a$" + formatPrice(total));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static String formatPrice(double price) {
        if (price >= 1_000_000_000) return trimTrailingZeros(price / 1_000_000_000.0) + "B";
        if (price >= 1_000_000) return trimTrailingZeros(price / 1_000_000.0) + "M";
        if (price >= 1_000) return trimTrailingZeros(price / 1_000.0) + "K";
        return trimTrailingZeros(price);
    }

    private static String trimTrailingZeros(double value) {
        if (value == (long) value)
            return String.format("%d", (long) value);
        else
            return String.format("%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        switch (event.getAction()) {
            case PICKUP_ALL:
            case PICKUP_HALF:
            case PICKUP_SOME:
            case PICKUP_ONE:
                playersWithItemsInCursor.add(player.getUniqueId());
                break;

            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE:
            case SWAP_WITH_CURSOR:
            case MOVE_TO_OTHER_INVENTORY:
            case HOTBAR_SWAP:
                playersWithItemsInCursor.add(player.getUniqueId());
                break;

            case DROP_ALL_CURSOR:
            case DROP_ONE_CURSOR:
                if (player.getItemOnCursor().getType() == Material.AIR) {
                    playersWithItemsInCursor.remove(player.getUniqueId());
                }
                break;
        }

        Bukkit.getScheduler().runTask(this, () -> {
            ItemStack newCursor = player.getItemOnCursor();
            if (newCursor.getType() == Material.AIR) {
                playersWithItemsInCursor.remove(player.getUniqueId());
            } else {
                playersWithItemsInCursor.add(player.getUniqueId());
            }
            player.updateInventory();
        });
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        playersWithItemsInCursor.add(player.getUniqueId());

        Bukkit.getScheduler().runTask(this, () -> {
            ItemStack newCursor = player.getItemOnCursor();
            if (newCursor.getType() == Material.AIR) {
                playersWithItemsInCursor.remove(player.getUniqueId());
            }
            player.updateInventory();
        });
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        playersWithItemsInCursor.remove(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(this, player::updateInventory, 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playersWithItemsInCursor.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(this, () -> event.getPlayer().updateInventory(), 10L);
    }
}
