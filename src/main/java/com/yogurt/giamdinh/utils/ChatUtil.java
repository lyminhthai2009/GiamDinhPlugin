package com.yogurt.giamdinh.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ChatUtil {

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> color(List<String> list) {
        return list.stream().map(ChatUtil::color).collect(Collectors.toList());
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) return;
        if (sender instanceof Player) {
            sender.sendMessage(setPlaceholders((Player) sender, message));
        } else {
            sender.sendMessage(color(message));
        }
    }

    public static String setPlaceholders(Player player, String text) {
        if (player == null || text == null) return "";
        return PlaceholderAPI.setPlaceholders(player, color(text));
    }

    public static List<String> setPlaceholders(Player player, List<String> text) {
        if (player == null || text == null) return List.of();
        return PlaceholderAPI.setPlaceholders(player, color(text));
    }
}
