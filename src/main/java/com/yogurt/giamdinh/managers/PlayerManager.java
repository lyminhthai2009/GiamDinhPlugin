package com.yogurt.giamdinh.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yogurt.giamdinh.GiamDinh;
import com.yogurt.giamdinh.model.PlayerData;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@Getter
public class PlayerManager {
    private final GiamDinh plugin;
    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();
    private final File dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public PlayerManager(GiamDinh plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.json");
        loadAllPlayerData();
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataCache.get(player.getUniqueId());
    }

    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerDataCache.containsKey(uuid)) {
            // PlayerData will be loaded from the file on startup,
            // so we just create a new one if it doesn't exist.
            PlayerData data = new PlayerData(uuid);
            playerDataCache.put(uuid, data);
            plugin.getLevelManager().applyPermissions(player);
        } else {
            // Ensure the transient plugin field is set on login
            playerDataCache.get(uuid).setPlugin(plugin);
        }
    }

    public void unloadPlayerData(Player player) {
        // Data is saved periodically and on disable, so no need to save here
        // to prevent excessive I/O on frequent logins/logouts.
    }

    public void saveAllPlayerData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(playerDataCache, writer);
            plugin.getLogger().info("Saved " + playerDataCache.size() + " player data entries.");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save playerdata.json", e);
        }
    }

    private void loadAllPlayerData() {
        if (!dataFile.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<UUID, PlayerData>>(){}.getType();
            Map<UUID, PlayerData> loadedData = gson.fromJson(reader, type);
            if (loadedData != null) {
                playerDataCache.putAll(loadedData);
                plugin.getLogger().info("Loaded " + playerDataCache.size() + " player data entries.");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load playerdata.json", e);
        }
    }
}
