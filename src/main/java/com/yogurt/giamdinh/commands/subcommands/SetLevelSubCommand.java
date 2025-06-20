package com.yogurt.giamdinh.commands.subcommands;

import com.yogurt.giamdinh.GiamDinh;
import com.yogurt.giamdinh.commands.SubCommand;
import com.yogurt.giamdinh.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SetLevelSubCommand implements SubCommand {
    private final GiamDinh plugin;

    public SetLevelSubCommand(GiamDinh plugin) { this.plugin = plugin; }

    @Override
    public String getName() { return "setlevel"; }
    @Override
    public String getDescription() { return "Đặt cấp độ cho người chơi."; }
    @Override
    public String getSyntax() { return "/gda setlevel <tên> <cấp>"; }
    @Override
    public String getPermission() { return "giamdinh.admin.setlevel"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            ChatUtil.sendMessage(sender, getSyntax());
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            ChatUtil.sendMessage(sender, plugin.getConfigManager().getMessage("player_not_found").replace("{player}", args[0]));
            return;
        }

        try {
            int level = Integer.parseInt(args[1]);
            int maxLevel = plugin.getConfigManager().getMainConfig().getInt("leveling.max_level");
            if (level < 1 || level > maxLevel) {
                ChatUtil.sendMessage(sender, plugin.getConfigManager().getMessage("setlevel_fail_max").replace("{max_level}", String.valueOf(maxLevel)));
                return;
            }
            plugin.getLevelManager().setPlayerLevel(target, level);
            ChatUtil.sendMessage(sender, plugin.getConfigManager().getMessage("setlevel_success")
                    .replace("{player}", target.getName())
                    .replace("{level}", String.valueOf(level)));
        } catch (NumberFormatException e) {
            ChatUtil.sendMessage(sender, plugin.getConfigManager().getMessage("invalid_number").replace("{number}", args[1]));
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(name -> StringUtil.startsWithIgnoreCase(name, args[0]))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return List.of("1", "10", "50", "100");
        }
        return Collections.emptyList();
    }
}
