package com.yogurt.giamdinh.gui;

import com.yogurt.giamdinh.GiamDinh;
import com.yogurt.giamdinh.model.Booster;
import com.yogurt.giamdinh.model.PlayerData;
import com.yogurt.giamdinh.utils.ChatUtil;
import com.yogurt.giamdinh.utils.ItemUtil;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI extends BaseGUI {

    public ShopGUI(GiamDinh plugin) {
        super(plugin, null);
    }

    public void open(Player p) {
        this.player = p;
        super.open();
    }

    @Override
    public String getTitle() {
        return ChatUtil.color(plugin.getConfigManager().getMainConfig().getString("gui.shop_title"));
    }

    @Override
    public int getSize() {
        return plugin.getConfigManager().getMainConfig().getInt("gui.shop_gui_size", 54);
    }

    @Override
    public void setup() {
        ConfigurationSection shopItems = plugin.getConfigManager().getShopConfig();
        for (String key : shopItems.getKeys(false)) {
            ConfigurationSection itemSection = shopItems.getConfigurationSection(key);
            if (itemSection == null) continue;

            ConfigurationSection displaySection = itemSection.getConfigurationSection("display_item");
            if(displaySection == null) continue;

            double price = itemSection.getDouble("price", 0);
            ItemStack displayItem = ItemUtil.createItem(displaySection, player);
            ItemMeta meta = displayItem.getItemMeta();
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.replaceAll(s -> s.replace("{price}", String.format("%,.0f", price))
                    .replace("{currency_name}", plugin.getEconomyManager().getCurrencyName()));
            meta.setLore(ChatUtil.setPlaceholders(player, lore));
            displayItem.setItemMeta(meta);

            if (itemSection.isList("slots")) {
                for (int slot : itemSection.getIntegerList("slots")) {
                    inventory.setItem(slot, displayItem);
                }
            } else {
                inventory.setItem(itemSection.getInt("slot"), displayItem);
            }
        }
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        ConfigurationSection shopItems = plugin.getConfigManager().getShopConfig();
        for (String key : shopItems.getKeys(false)) {
            ConfigurationSection itemSection = shopItems.getConfigurationSection(key);
            if (itemSection != null && e.getSlot() == itemSection.getInt("slot")) {
                handlePurchase(itemSection);
                return;
            }
        }
    }

    private void handlePurchase(ConfigurationSection itemSection) {
        PlayerData pData = plugin.getPlayerManager().getPlayerData(player);
        int requiredLevel = itemSection.getInt("requirements.level", 0);
        String requiredPerm = itemSection.getString("requirements.permission", "");

        if (pData.getLevel() < requiredLevel) {
            ChatUtil.sendMessage(player, plugin.getConfigManager().getMessage("shop_level_required").replace("{level}", String.valueOf(requiredLevel)));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (!requiredPerm.isEmpty() && !player.hasPermission(requiredPerm)) {
            ChatUtil.sendMessage(player, plugin.getConfigManager().getMessage("shop_permission_required"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        double price = itemSection.getDouble("price");
        if (!plugin.getEconomyManager().withdraw(player, price)) {
            String notEnoughMoney = plugin.getConfigManager().getMessage("not_enough_money")
                    .replace("{currency_name}", plugin.getEconomyManager().getCurrencyName())
                    .replace("{amount}", String.format("%,.2f", price))
                    .replace("{balance}", String.format("%,.2f", plugin.getEconomyManager().getBalance(player)));
            ChatUtil.sendMessage(player, notEnoughMoney);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        String itemType = itemSection.getString("give_item_type");
        String itemId = itemSection.getString("give_item_id");
        ItemStack toGive = null;
        String itemName = "";

        if ("BOOSTER".equalsIgnoreCase(itemType)) {
            Booster booster = plugin.getConfigManager().getBooster(itemId);
            if(booster != null){
                toGive = booster.getItem().clone();
                itemName = toGive.getItemMeta().getDisplayName();
            }
        } else if ("TOOL".equalsIgnoreCase(itemType)) {
            // Logic for giving tools (to be implemented if tools.yml is used)
        }

        if (toGive != null) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), toGive);
                ChatUtil.sendMessage(player, plugin.getConfigManager().getMessage("inventory_full"));
            } else {
                player.getInventory().addItem(toGive);
            }
            ChatUtil.sendMessage(player, plugin.getConfigManager().getMessage("shop_purchase_success")
                    .replace("{item_name}", itemName)
                    .replace("{price}", String.format("%,.0f", price))
                    .replace("{currency_name}", plugin.getEconomyManager().getCurrencyName()));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
        }
    }
}
