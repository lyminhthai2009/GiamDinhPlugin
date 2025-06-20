package com.yogurt.giamdinh.hooks;

import com.yogurt.giamdinh.GiamDinh;
import com.yogurt.giamdinh.model.LevelPerk;
import com.yogurt.giamdinh.model.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class GiamDinhPlaceholders extends PlaceholderExpansion {

    private final GiamDinh plugin;
    private final DecimalFormat df = new DecimalFormat("#,###.##");

    public GiamDinhPlaceholders(GiamDinh plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "giamdinh";
    }

    @Override
    public @NotNull String getAuthor() {
        return "yogurt";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) {
            return "";
        }
        Player player = offlinePlayer.getPlayer();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) {
            return "";
        }

        switch (params.toLowerCase()) {
            case "level":
                return String.valueOf(data.getLevel());
            case "max_level":
                return String.valueOf(plugin.getConfigManager().getMainConfig().getInt("leveling.max_level", 100));
            case "xp":
                return String.valueOf(Math.round(data.getXp()));
            case "xp_formatted":
                return df.format(data.getXp());
            case "xp_required":
                return String.valueOf(Math.round(plugin.getLevelManager().getRequiredXpForLevel(data.getLevel())));
            case "xp_required_formatted":
                double required = plugin.getLevelManager().getRequiredXpForLevel(data.getLevel());
                return required == Double.POSITIVE_INFINITY ? "MAX" : df.format(required);
            case "xp_bar":
                return createXpBar(data.getXp(), plugin.getLevelManager().getRequiredXpForLevel(data.getLevel()));
            default:
                // Perks placeholders
                LevelPerk perk = plugin.getLevelManager().getPerksForLevel(data.getLevel());
                switch (params.toLowerCase()) {
                    case "perk_batch_size":
                        return String.valueOf(perk.getMaxBatchSize());
                    case "perk_rare_find_bonus":
                        return String.valueOf(df.format((perk.getRareFindChanceModifier() - 1) * 100));
                    case "perk_fee_reduction_bonus":
                        return String.valueOf(df.format((1 - perk.getFeeReductionModifier()) * 100));
                    case "perk_xp_boost_bonus":
                        return String.valueOf(df.format((perk.getXpBoostModifier() - 1) * 100));
                }
        }
        return null;
    }

    private String createXpBar(double current, double required) {
        if (required == Double.POSITIVE_INFINITY) {
            return "&a&l██████████ &r&a(MAX)";
        }
        double ratio = current / required;
        if(ratio > 1) ratio = 1;
        if(ratio < 0) ratio = 0;

        int greenChars = (int) Math.round(ratio * 10);
        int grayChars = 10 - greenChars;

        return "&a&l" + "█".repeat(greenChars) + "&7&l" + "█".repeat(grayChars);
    }
}
