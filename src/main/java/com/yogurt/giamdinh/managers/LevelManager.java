package com.yogurt.giamdinh.managers;

import com.yogurt.giamdinh.GiamDinh;
import com.yogurt.giamdinh.model.LevelPerk;
import com.yogurt.giamdinh.model.PlayerData;
import com.yogurt.giamdinh.utils.ChatUtil;
import me.clip.placeholderapi.libs.ezql.ezql.EZQL;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class LevelManager {
    private final GiamDinh plugin;
    private final NavigableMap<Integer, LevelPerk> levelPerks = new TreeMap<>();
    private final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
    private final String xpFormula;
    private final int maxLevel;

    public LevelManager(GiamDinh plugin) {
        this.plugin = plugin;
        this.xpFormula = plugin.getConfigManager().getMainConfig().getString("leveling.xp_formula", "100 * {level}");
        this.maxLevel = plugin.getConfigManager().getMainConfig().getInt("leveling.max_level", 100);
        loadLevelPerks();
    }

    private void loadLevelPerks() {
        levelPerks.clear();
        ConfigurationSection levelsSection = plugin.getConfigManager().getLevelsConfig();
        for (String key : levelsSection.getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                ConfigurationSection perksSection = levelsSection.getConfigurationSection(key + ".perks");
                if (perksSection != null) {
                    int maxBatchSize = perksSection.getInt("max_batch_size", 1);
                    List<String> permissions = perksSection.getStringList("permissions");
                    double rareFind = perksSection.getDouble("modifiers.rare_find_chance", 1.0);
                    double feeReduction = perksSection.getDouble("modifiers.fee_reduction", 1.0);
                    double xpBoost = perksSection.getDouble("modifiers.xp_boost", 1.0);
                    levelPerks.put(level, new LevelPerk(maxBatchSize, permissions, rareFind, feeReduction, xpBoost));
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid level key in levels.yml: " + key);
            }
        }
        plugin.getLogger().info("Da tai " + levelPerks.size() + " moc phan thuong cap do.");
    }

    public LevelPerk getPerksForLevel(int level) {
        var entry = levelPerks.floorEntry(level);
        return entry != null ? entry.getValue() : LevelPerk.getDefault();
    }

    public double getRequiredXpForLevel(int level) {
        if (level >= maxLevel) return Double.POSITIVE_INFINITY;
        String formula = xpFormula.replace("{level}", String.valueOf(level));
        try {
            Object result = scriptEngine.eval(formula);
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }
        } catch (ScriptException e) {
            plugin.getLogger().severe("Error evaluating XP formula: " + formula);
            e.printStackTrace();
        }
        return 100 * level; // Fallback
    }

    public void checkLevelUp(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data.getLevel() >= maxLevel) return;

        double requiredXp = getRequiredXpForLevel(data.getLevel());
        boolean leveledUp = false;

        while (data.getXp() >= requiredXp) {
            data.setXp(data.getXp() - requiredXp);
            data.setLevel(data.getLevel() + 1);
            leveledUp = true;

            if (data.getLevel() >= maxLevel) {
                data.setXp(0); // Cap reached, reset XP
                break;
            }
            requiredXp = getRequiredXpForLevel(data.getLevel());
        }

        if (leveledUp) {
            applyPermissions(player);
            String levelUpMessage = plugin.getConfigManager().getMessage("level_up")
                    .replace("{level}", String.valueOf(data.getLevel()));
            ChatUtil.sendMessage(player, levelUpMessage);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    public void applyPermissions(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        LevelPerk perks = getPerksForLevel(data.getLevel());
        for (String perm : perks.getPermissions()) {
            if (!player.hasPermission(perm)) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set " + perm + " true");
            }
        }
    }

    public void setPlayerLevel(Player player, int level) {
        if (level > maxLevel || level < 1) {
            return;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        data.setLevel(level);
        data.setXp(0);
        checkLevelUp(player); // Recalculate and apply perks
    }
}
