package com.yogurt.giamdinh.managers;

import com.yogurt.giamdinh.GiamDinh;
import com.yogurt.giamdinh.model.Booster;
import com.yogurt.giamdinh.model.LevelPerk;
import com.yogurt.giamdinh.model.PlayerData;
import com.yogurt.giamdinh.utils.ChatUtil;
import com.yogurt.giamdinh.utils.ItemUtil;
import dev.lone.itemsadder.api.CustomStack;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class AppraisalManager {
    private final GiamDinh plugin;

    public AppraisalManager(GiamDinh plugin) {
        this.plugin = plugin;
    }

    public ConfigurationSection getAppraisalConfig(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        String identifier = ItemUtil.getItemIdentifier(item);
        if (identifier != null && plugin.getConfigManager().getOresConfig().isConfigurationSection(identifier)) {
            return plugin.getConfigManager().getOresConfig().getConfigurationSection(identifier);
        }
        return null;
    }

    public boolean isAppraisable(ItemStack item) {
        return getAppraisalConfig(item) != null;
    }

    public void performAppraisal(Player player, ItemStack itemToAppraise, Booster appliedBooster) {
        if (!isAppraisable(itemToAppraise)) {
            ChatUtil.sendMessage(player, plugin.getConfigManager().getMessage("not_appraisable"));
            return;
        }

        ConfigurationSection config = getAppraisalConfig(itemToAppraise);
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        LevelPerk levelPerk = plugin.getLevelManager().getPerksForLevel(playerData.getLevel());

        // 1. Calculate Fee
        double baseFee = plugin.getConfigManager().getMainConfig().getDouble("appraisal.base_fee");
        double feeModifier = config.getDouble("fee_modifier", 1.0);
        double feeReduction = levelPerk.getFeeReductionModifier();
        double finalFee = baseFee * feeModifier * feeReduction;

        // 2. Charge Fee
        if (finalFee > 0 && !plugin.getEconomyManager().withdraw(player, finalFee)) {
            String notEnoughMoney = plugin.getConfigManager().getMessage("not_enough_money")
                    .replace("{currency_name}", plugin.getEconomyManager().getCurrencyName())
                    .replace("{amount}", String.format("%,.2f", finalFee))
                    .replace("{balance}", String.format("%,.2f", plugin.getEconomyManager().getBalance(player)));
            ChatUtil.sendMessage(player, notEnoughMoney);
            return;
        }
        if (finalFee > 0) {
            String feeCharged = plugin.getConfigManager().getMessage("appraisal_fee_charged")
                    .replace("{currency_name}", plugin.getEconomyManager().getCurrencyName())
                    .replace("{amount}", String.format("%,.2f", finalFee));
            ChatUtil.sendMessage(player, feeCharged);
        }

        // 3. Determine Rewards
        List<ItemStack> rewards = new ArrayList<>();
        ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");
        if (rewardsSection != null) {
            double totalChance = 0;
            for (String key : rewardsSection.getKeys(false)) {
                totalChance += rewardsSection.getDouble(key + ".chance");
            }

            double random = ThreadLocalRandom.current().nextDouble(totalChance);
            double cumulativeChance = 0;
            
            // Apply booster and perks to the random roll
            double rareFindBonus = levelPerk.getRareFindChanceModifier();
            if (appliedBooster != null && appliedBooster.getEffectType() == Booster.EffectType.RARE_FIND_CHANCE) {
                rareFindBonus *= appliedBooster.getValue();
            }

            for (String key : rewardsSection.getKeys(false)) {
                double chance = rewardsSection.getDouble(key + ".chance") * rareFindBonus;
                cumulativeChance += chance;
                if (random < cumulativeChance) {
                    List<ConfigurationSection> itemsToGive = (List<ConfigurationSection>) rewardsSection.getMapList(key + ".items");
                    for (ConfigurationSection itemSection : itemsToGive) {
                        String type = itemSection.getString("type", "VANILLA").toUpperCase();
                        if ("COMMAND".equals(type)) {
                            List<String> commands = itemSection.getStringList("commands");
                            for (String cmd : commands) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()));
                            }
                        } else {
                            ItemStack rewardItem = ItemUtil.createItem(itemSection, player);
                            if (itemSection.contains("amount")) {
                                List<Integer> amounts = itemSection.getIntegerList("amount");
                                rewardItem.setAmount(ThreadLocalRandom.current().nextInt(amounts.get(0), amounts.get(1) + 1));
                            } else {
                                rewardItem.setAmount(itemSection.getInt("amount", 1));
                            }
                            rewards.add(rewardItem);
                        }
                    }
                    break;
                }
            }
        }
        
        // 4. Handle Results
        if (rewards.isEmpty()) { // Appraisal Failed
            if (plugin.getConfigManager().getMainConfig().getBoolean("appraisal.return_item_on_fail")) {
                ChatUtil.sendMessage(player, plugin.getConfigManager().getMessage("appraisal_fail_returned"));
                // The item is already taken from the GUI, so we need to give it back.
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItem(player.getLocation(), itemToAppraise);
                } else {
                    player.getInventory().addItem(itemToAppraise);
                }
            } else {
                ChatUtil.sendMessage(player, plugin.getConfigManager().getMessage("appraisal_fail"));
            }
        } else { // Appraisal Succeeded
            // Give rewards
            for (ItemStack reward : rewards) {
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItem(player.getLocation(), reward);
                    ChatUtil.sendMessage(player, plugin.getConfigManager().getMessage("inventory_full"));
                } else {
                    player.getInventory().addItem(reward);
                }
            }
            String successMsg = plugin.getConfigManager().getMessage("appraisal_success")
                    .replace("{item_count}", String.valueOf(rewards.size()));
            ChatUtil.sendMessage(player, successMsg);
            
            // Grant XP
            double baseXp = plugin.getConfigManager().getMainConfig().getDouble("leveling.base_xp_gain");
            double xpModifier = config.getDouble("xp_modifier", 1.0);
            double xpBoost = levelPerk.getXpBoostModifier();
            if (appliedBooster != null && appliedBooster.getEffectType() == Booster.EffectType.XP_BOOST) {
                xpBoost *= appliedBooster.getValue();
            }
            double finalXp = baseXp * xpModifier * xpBoost;
            
            playerData.addXp(finalXp);
            String xpGainMsg = plugin.getConfigManager().getMessage("xp_gain")
                .replace("{xp}", String.format("%,.1f", finalXp))
                .replace("{current_xp}", String.format("%,.1f", playerData.getXp()))
                .replace("{required_xp}", String.format("%,.1f", plugin.getLevelManager().getRequiredXpForLevel(playerData.getLevel())));
            ChatUtil.sendMessage(player, xpGainMsg);
        }
    }
}
