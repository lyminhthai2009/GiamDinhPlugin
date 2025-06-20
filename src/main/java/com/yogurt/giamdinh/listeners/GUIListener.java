package com.yogurt.giamdinh.listeners;

import com.yogurt.giamdinh.GiamDinh;
import com.yogurt.giamdinh.gui.BaseGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIListener implements Listener {
    private final GiamDinh plugin;

    public GUIListener(GiamDinh plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BaseGUI) {
            ((BaseGUI) holder).handleClick(event);
        }
    }
}
