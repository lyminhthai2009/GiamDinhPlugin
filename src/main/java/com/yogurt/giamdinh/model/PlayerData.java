package com.yogurt.giamdinh.model;

import com.yogurt.giamdinh.GiamDinh;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter @Setter
public class PlayerData {
    private final UUID uuid;
    private int level;
    private double xp;
    private transient GiamDinh plugin;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.xp = 0;
        this.plugin = GiamDinh.getInstance();
    }

    public PlayerData(UUID uuid, int level, double xp) {
        this.uuid = uuid;
        this.level = level;
        this.xp = xp;
        this.plugin = GiamDinh.getInstance();
    }

    public void addXp(double amount) {
        if (amount <= 0) return;
        int maxLevel = plugin.getConfigManager().getMainConfig().getInt("leveling.max_level", 100);
        if (this.level >= maxLevel) {
            this.xp = 0; // Reset xp if at max level
            return;
        }

        this.xp += amount;
        Player player = Bukkit.getPlayer(uuid);
        if(player != null && player.isOnline()){
            plugin.getLevelManager().checkLevelUp(player);
        }
    }

    public void setPlugin(GiamDinh plugin) {
        this.plugin = plugin;
    }
}
