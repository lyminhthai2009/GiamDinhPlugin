package com.yogurt.giamdinh.managers;

import com.github.sup2is.coin.api.CoinEngineAPI;
import com.yogurt.giamdinh.GiamDinh;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

@Getter
public class EconomyManager {

    private final GiamDinh plugin;
    private Economy vaultEconomy = null;
    private PlayerPointsAPI playerPointsAPI = null;
    private CoinEngineAPI coinEngineAPI = null;
    private final CurrencyType currencyType;
    private final String currencyName;

    public enum CurrencyType { VAULT, PLAYER_POINTS, COIN_ENGINE, NONE }

    public EconomyManager(GiamDinh plugin) {
        this.plugin = plugin;
        String type = plugin.getConfigManager().getMainConfig().getString("currency.type", "VAULT").toUpperCase();
        this.currencyType = CurrencyType.valueOf(type);

        switch (this.currencyType) {
            case VAULT:
                if (setupVault()) {
                    this.currencyName = vaultEconomy.currencyNameSingular();
                    plugin.getLogger().info("Da lien ket voi Vault.");
                } else {
                    this.currencyName = "Error";
                    plugin.getLogger().warning("Khong tim thay Vault hoac plugin kinh te ho tro Vault.");
                }
                break;
            case PLAYER_POINTS:
                if (setupPlayerPoints()) {
                    this.currencyName = "Points";
                    plugin.getLogger().info("Da lien ket voi PlayerPoints.");
                } else {
                    this.currencyName = "Error";
                    plugin.getLogger().warning("Khong tim thay PlayerPoints.");
                }
                break;
            case COIN_ENGINE:
                 if (setupCoinEngine()) {
                    this.currencyName = "Coins"; // Or get from CoinEngine config
                    plugin.getLogger().info("Da lien ket voi CoinEngine.");
                } else {
                    this.currencyName = "Error";
                    plugin.getLogger().warning("Khong tim thay CoinEngine.");
                }
                break;
            default:
                this.currencyName = "None";
                plugin.getLogger().info("He thong kinh te da bi tat.");
                break;
        }
    }

    private boolean setupVault() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        vaultEconomy = rsp.getProvider();
        return vaultEconomy != null;
    }

    private boolean setupPlayerPoints() {
        if (plugin.getServer().getPluginManager().getPlugin("PlayerPoints") == null) {
            return false;
        }
        playerPointsAPI = PlayerPoints.getPlugin(PlayerPoints.class).getAPI();
        return playerPointsAPI != null;
    }

    private boolean setupCoinEngine() {
        if (plugin.getServer().getPluginManager().getPlugin("CoinEngine") == null) {
            return false;
        }
        coinEngineAPI = CoinEngineAPI.getInstance();
        return coinEngineAPI != null;
    }

    public double getBalance(Player player) {
        switch (currencyType) {
            case VAULT:
                return vaultEconomy != null ? vaultEconomy.getBalance(player) : 0;
            case PLAYER_POINTS:
                return playerPointsAPI != null ? playerPointsAPI.look(player.getUniqueId()) : 0;
            case COIN_ENGINE:
                return coinEngineAPI != null ? coinEngineAPI.getCoin(player) : 0;
            default:
                return 0;
        }
    }

    public boolean hasEnough(Player player, double amount) {
        return getBalance(player) >= amount;
    }

    public boolean withdraw(Player player, double amount) {
        if (amount <= 0) return true;
        if (!hasEnough(player, amount)) return false;

        switch (currencyType) {
            case VAULT:
                if (vaultEconomy != null) {
                    return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
                }
                break;
            case PLAYER_POINTS:
                if (playerPointsAPI != null) {
                    return playerPointsAPI.take(player.getUniqueId(), (int) Math.round(amount));
                }
                break;
            case COIN_ENGINE:
                if (coinEngineAPI != null) {
                   coinEngineAPI.takeCoin(player, (long) Math.round(amount));
                   return true; // CoinEngine does not return success boolean
                }
                break;
        }
        return false;
    }
}
