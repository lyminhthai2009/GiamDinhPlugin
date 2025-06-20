package com.yogurt.giamdinh.gui;

import com.yogurt.giamdinh.GiamDinh;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class BaseGUI implements InventoryHolder {
    protected final GiamDinh plugin;
    protected final Player player;
    protected Inventory inventory;

    public BaseGUI(GiamDinh plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public abstract String getTitle();
    public abstract int getSize();
    public abstract void setup();
    public abstract void handleClick(InventoryClickEvent e);

    public void open() {
        inventory = Bukkit.createInventory(this, getSize(), getTitle());
        this.setup();
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
