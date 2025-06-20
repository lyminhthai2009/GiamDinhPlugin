package com.yogurt.giamdinh.commands.subcommands;

import com.yogurt.giamdinh.GiamDinh;
import com.yogurt.giamdinh.commands.SubCommand;
import com.yogurt.giamdinh.model.Booster;
import com.yogurt.giamdinh.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GiveSubCommand implements SubCommand {

    private final GiamDinh plugin;

    public GiveSubCommand(GiamDinh plugin) { this.plugin = plugin; }

    @Override
    public String getName() { return "give"; }
    @Override
    public String getDescription() { return "Tặng vật phẩm hỗ trợ/công cụ."; }
    @Override
    public String getSyntax() { return "/gda give <tên> <booster/tool> <id> [số lượng]"; }
    @Override
    public String getPermission() { return "giamdinh.admin.give"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 3) {
            ChatUtil.sendMessage(sender, getSyntax());
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            ChatUtil.sendMessage(sender, plugin.getConfigManager().getMessage("player_not_found").replace("{player}", args[0]));
            return;
        }

        String type = args[1].toLowerCase();
        String id = args[2];
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                ChatUtil.sendMessage(sender, plugin.getConfigManager().getMessage("invalid_number").replace("{number}", args[3]));
                return;
            }
        }

        ItemStack toGive = null;
        String itemName = "";

        if (type.equals("booster")) {
            Booster booster = plugin.getConfigManager().getBooster(id);
            if (booster != null) {
                toGive = booster.getItem().clone();
                itemName = ChatUtil.color(toGive.getItemMeta().getDisplayName());
            }
        } else if (type.equals("tool")) {
            // Logic for giving tools here
            // Tool tool = plugin.getConfigManager().getTool(id);
        } else {
            ChatUtil.sendMessage(sender, plugin.getConfigManager().getMessage("give_fail_type"));
            return;
        }

        if (toGive == null) {
            ChatUtil.sendMessage(sender, plugin.getConfigManager().getMessage("give_fail_not_found")
                    .replace("{type}", type).replace("{id}", id));
            return;
        }

        toGive.setAmount(amount);
        target.getInventory().addItem(toGive);
        ChatUtil.sendMessage(sender, plugin.getConfigManager().getMessage("give_success")
                .replace("{amount}", String.valueOf(amount))
                .replace("{item_name}", itemName)
                .replace("{player}", target.getName()));
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(name -> StringUtil.startsWithIgnoreCase(name, args[0]))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], List.of("booster", "tool"), new ArrayList<>());
        }
        if (args.length == 3) {
            if (args[1].equalsIgnoreCase("booster")) {
                return StringUtil.copyPartialMatches(args[2], plugin.getConfigManager().getBoosterIds(), new ArrayList<>());
            }
            // Add logic for tools here
        }
        return Collections.emptyList();
    }
}
