package com.yogurt.giamdinh.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public interface SubCommand {
    String getName();
    String getDescription();
    String getSyntax();
    String getPermission();
    void perform(CommandSender sender, String[] args);
    List<String> getSubcommandArguments(Player player, String[] args);
}
