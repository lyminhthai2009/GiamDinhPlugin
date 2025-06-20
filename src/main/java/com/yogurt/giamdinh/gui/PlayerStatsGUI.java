package com.yogurt.giamdinh.gui;

import com.yogurt.giamdinh.GiamDinh;
import com.yogurt.giamdinh.utils.ChatUtil;
import com.yogurt.giamdinh.utils.ItemUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlayerStatsGUI extends BaseGUI {

    public PlayerStatsGUI(GiamDinh plugin) {
        super(plugin, null); // Player is set in open()
    }

    public void open(Player p) {
        this.player = p;
        super.open();
    }

    @Override
    public String getTitle() {
        return ChatUtil.setPlaceholders(player, plugin.getConfigManager().getStatsGuiConfig().getString("title"));
    }

    @Override
    public int getSize() {
        return plugin.getConfigManager().getStatsGuiConfig().getInt("size");
    }

    @Override
    public void setup() {
        ConfigurationSection itemsSection = plugin.getConfigManager().getStatsGuiConfig().getConfigurationSection("items");
        if (itemsSection == null) return;

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemConfig = itemsSection.getConfigurationSection(key);
            ItemStack item = ItemUtil.createItem(itemConfig.getConfigurationSection("item"), player);

            if (itemConfig.isList("slots")) {
                List<Integer> slots = itemConfig.getIntegerList("slots");
                for (int slot : slots) {
                    inventory.setItem(slot, item);
                }
            } else {
                inventory.setItem(itemConfig.getInt("slot"), item);
            }
        }
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
    }
}
