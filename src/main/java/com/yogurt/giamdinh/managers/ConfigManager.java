package com.yogurt.giamdinh.managers;

import com.yogurt.giamdinh.GiamDinh;
import com.yogurt.giamdinh.model.Booster;
import com.yogurt.giamdinh.utils.ChatUtil;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

@Getter
public class ConfigManager {

    private final GiamDinh plugin;
    private FileConfiguration mainConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration oresConfig;
    private FileConfiguration levelsConfig;
    private FileConfiguration boostersConfig;
    private FileConfiguration toolsConfig;
    private FileConfiguration shopConfig;
    private FileConfiguration statsGuiConfig;

    private final Map<String, Booster> boosters = new HashMap<>();

    public ConfigManager(GiamDinh plugin) {
        this.plugin = plugin;
    }

    public void loadAllConfigs() {
        mainConfig = loadConfig("config.yml");
        messagesConfig = loadConfig("messages_vi.yml");
        oresConfig = loadConfig("ores.yml");
        levelsConfig = loadConfig("levels.yml");
        boostersConfig = loadConfig("boosters.yml");
        toolsConfig = loadConfig("tools.yml");
        shopConfig = loadConfig("shop.yml");
        statsGuiConfig = loadConfig("player_stats_gui.yml");
        loadBoosters();
    }

    private FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void loadBoosters() {
        boosters.clear();
        if (boostersConfig == null) return;
        for (String key : boostersConfig.getKeys(false)) {
            boosters.put(key, new Booster(key, boostersConfig.getConfigurationSection(key)));
        }
        plugin.getLogger().info("Da tai " + boosters.size() + " vat pham ho tro.");
    }

    public Booster getBooster(String id) {
        return boosters.get(id);
    }

    public Set<String> getBoosterIds() {
        return boosters.keySet();
    }

    public String getMessage(String path) {
        String message = messagesConfig.getString(path, "&cMessage not found: " + path);
        String prefix = messagesConfig.getString("prefix", "");
        return ChatUtil.color(prefix + message);
    }

    public String getRawMessage(String path) {
        return messagesConfig.getString(path, "&cMessage not found: " + path);
    }
}
