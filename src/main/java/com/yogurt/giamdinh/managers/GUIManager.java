package com.yogurt.giamdinh.managers;

import com.yogurt.giamdinh.GiamDinh;
import com.yogurt.giamdinh.gui.AppraisalGUI;
import com.yogurt.giamdinh.gui.PlayerStatsGUI;
import com.yogurt.giamdinh.gui.ShopGUI;
import org.bukkit.entity.Player;

public class GUIManager {

    private final GiamDinh plugin;

    public GUIManager(GiamDinh plugin) {
        this.plugin = plugin;
    }

    public void openMainAppraisalGUI(Player player) {
        new AppraisalGUI(plugin).open(player);
    }

    public void openPlayerStatsGUI(Player player) {
        new PlayerStatsGUI(plugin).open(player);
    }

    public void openShopGUI(Player player) {
        new ShopGUI(plugin).open(player);
    }
}
