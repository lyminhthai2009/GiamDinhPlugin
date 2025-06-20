package com.yogurt.giamdinh.commands;

import com.yogurt.giamdinh.GiamDinh;
import com.yogurt.giamdinh.commands.subcommands.GiveSubCommand;
import com.yogurt.giamdinh.commands.subcommands.ReloadSubCommand;
import com.yogurt.giamdinh.commands.subcommands.SetLevelSubCommand;
import com.yogurt.giamdinh.utils.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final GiamDinh plugin;
    private final List<SubCommand> subCommands = new ArrayList<>();
    private final String mainCmd, adminCmd, statsCmd, shopCmd;

    public CommandManager(GiamDinh plugin) {
        this.plugin = plugin;
        this.mainCmd = plugin.getConfigManager().getMainConfig().getString("commands.main");
        this.adminCmd = plugin.getConfigManager().getMainConfig().getString("commands.admin");
        this.statsCmd = plugin.getConfigManager().getMainConfig().getString("commands.player_stats");
        this.shopCmd = plugin.getConfigManager().getMainConfig().getString("commands.shop");

        subCommands.add(new ReloadSubCommand(plugin));
        subCommands.add(new SetLevelSubCommand(plugin));
        subCommands.add(new GiveSubCommand(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String cmdName = command.getName().toLowerCase();
        List<String> mainAliases = plugin.getConfigManager().getMainConfig().getStringList("commands.aliases");

        // Player commands
        if (cmdName.equals(mainCmd) || mainAliases.contains(cmdName)) {
            handlePlayerCommand(sender, "giamdinh.use", () -> plugin.getGuiManager().openMainAppraisalGUI((Player) sender));
            return true;
        }
        if (cmdName.equals(statsCmd)) {
            handlePlayerCommand(sender, "giamdinh.stats", () -> plugin.getGuiManager().openPlayerStatsGUI((Player) sender));
            return true;
        }
        if (cmdName.equals(shopCmd)) {
            handlePlayerCommand(sender, "giamdinh.shop", () -> plugin.getGuiManager().openShopGUI((Player) sender));
            return true;
        }

        // Admin command
        if (cmdName.equals(adminCmd)) {
            handleAdminCommand(sender, args);
            return true;
        }

        return false;
    }

    private void handlePlayerCommand(CommandSender sender, String permission, Runnable action) {
        if (!(sender instanceof Player)) {
            ChatUtil.sendMessage(sender, plugin.getConfigManager().getMessage("player_only"));
            return;
        }
        if (!sender.hasPermission(permission)) {
            ChatUtil.sendMessage(sender, plugin.getConfigManager().getMessage("no_permission"));
            return;
        }
        action.run();
    }

    private void handleAdminCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            ChatUtil.sendMessage(sender, plugin.getConfigManager().getMessage("gda_help_header"));
            subCommands.forEach(sub -> ChatUtil.sendMessage(sender, sub.getSyntax() + " - " + sub.getDescription()));
            return;
        }

        for (SubCommand sub : subCommands) {
            if (sub.getName().equalsIgnoreCase(args[0])) {
                if (!sender.hasPermission(sub.getPermission())) {
                    ChatUtil.sendMessage(sender, plugin.getConfigManager().getMessage("no_permission"));
                    return;
                }
                sub.perform(sender, Arrays.copyOfRange(args, 1, args.length));
                return;
            }
        }
        ChatUtil.sendMessage(sender, getSyntax());
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase(adminCmd)) {
            if (!(sender instanceof Player)) return List.of();
            Player p = (Player) sender;
            if (args.length == 1) {
                return subCommands.stream()
                        .filter(sub -> p.hasPermission(sub.getPermission()))
                        .map(SubCommand::getName)
                        .filter(name -> name.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            for (SubCommand sub : subCommands) {
                if (sub.getName().equalsIgnoreCase(args[0]) && p.hasPermission(sub.getPermission())) {
                    return sub.getSubcommandArguments(p, Arrays.copyOfRange(args, 1, args.length));
                }
            }
        }
        return List.of();
    }
}
