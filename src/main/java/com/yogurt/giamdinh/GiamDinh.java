package com.yogurt.giamdinh;

import com.yogurt.giamdinh.commands.CommandManager;
import com.yogurt.giamdinh.hooks.GiamDinhPlaceholders;
import com.yogurt.giamdinh.listeners.GUIListener;
import com.yogurt.giamdinh.listeners.PlayerListener;
import com.yogurt.giamdinh.managers.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@Getter
public final class GiamDinh extends JavaPlugin {

    private static GiamDinh instance;
    private ConfigManager configManager;
    private PlayerManager playerManager;
    private EconomyManager economyManager;
    private AppraisalManager appraisalManager;
    private LevelManager levelManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("---------------------------------");
        getLogger().info("Dang khoi tao GiamDinh...");

        if(!setupDependencies()) {
            getLogger().severe("Thieu dependencies quan trong! Plugin se bi vo hieu hoa.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 1. Tải cấu hình
        configManager = new ConfigManager(this);
        configManager.loadAllConfigs();

        // 2. Khởi tạo các manager
        economyManager = new EconomyManager(this);
        playerManager = new PlayerManager(this);
        levelManager = new LevelManager(this);
        appraisalManager = new AppraisalManager(this);
        guiManager = new GUIManager(this);

        // 3. Đăng ký listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        // 4. Đăng ký lệnh động
        registerDynamicCommands();

        // 5. Hook vào PlaceholderAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new GiamDinhPlaceholders(this).register();
            getLogger().info("Da hook thanh cong vao PlaceholderAPI.");
        }

        getLogger().info("GiamDinh da duoc bat boi yogurt!");
        getLogger().info("---------------------------------");
    }
    
    private boolean setupDependencies() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Khong tim thay Vault! Vault la mot dependency bat buoc.");
            return false;
        }
        return true;
    }

    @Override
    public void onDisable() {
        if (playerManager != null) {
            playerManager.saveAllPlayerData();
        }
        getLogger().info("GiamDinh da duoc tat.");
    }

    private void registerDynamicCommands() {
        try {
            CommandManager commandManager = new CommandManager(this);
            String mainCmd = configManager.getMainConfig().getString("commands.main");
            List<String> aliases = configManager.getMainConfig().getStringList("commands.aliases");
            String adminCmd = configManager.getMainConfig().getString("commands.admin");
            String statsCmd = configManager.getMainConfig().getString("commands.player_stats");
            String shopCmd = configManager.getMainConfig().getString("commands.shop");

            final Field bukkitCommandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMapField.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) bukkitCommandMapField.get(Bukkit.getServer());
            
            final Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            // Unregister old commands for reload safety
            List<String> toUnregister = new ArrayList<>(List.of(mainCmd, adminCmd, statsCmd, shopCmd));
            toUnregister.addAll(aliases);
            toUnregister.forEach(cmd -> {
                if (commandMap.getCommand(cmd) != null) {
                    knownCommands.remove(cmd);
                }
            });


            // Register new commands
            registerCommand(commandMap, mainCmd, aliases, commandManager);
            registerCommand(commandMap, adminCmd, List.of(), commandManager);
            registerCommand(commandMap, statsCmd, List.of(), commandManager);
            registerCommand(commandMap, shopCmd, List.of(), commandManager);

            getLogger().info("Da dang ky lenh dong thanh cong.");

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Khong the dang ky lenh dong!", e);
        }
    }

    private void registerCommand(SimpleCommandMap commandMap, String name, List<String> aliases, CommandManager executor) {
        PluginCommand command = createPluginCommand(name);
        if (command != null) {
            command.setAliases(aliases);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
            commandMap.register(getName().toLowerCase(), command);
        }
    }

    private PluginCommand createPluginCommand(String name) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
            constructor.setAccessible(true);
            return constructor.newInstance(name, this);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error creating plugin command: " + name, e);
            return null;
        }
    }

    public static GiamDinh getInstance() { return instance; }
}
