package com.yogurt.giamdinh.commands.subcommands;

import com.yogurt.giamdinh.GiamDinh;
import com.yogurt.giamdinh.commands.SubCommand;
import com.yogurt.giamdinh.utils.ChatUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ReloadSubCommand implements SubCommand {

    private final GiamDinh plugin;

    public ReloadSubCommand(GiamDinh plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() { return "reload"; }
    @Override
    public String getDescription() { return "Tải lại cấu hình của plugin."; }
    @Override
    public String getSyntax() { return "/gda reload"; }
    @Override
    public String getPermission() { return "giamdinh.admin.reload"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        plugin.getConfigManager().loadAllConfigs();
        ChatUtil.sendMessage(sender, plugin.getConfigManager().getMessage("reload_success"));
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return new ArrayList<>();
    }
}
