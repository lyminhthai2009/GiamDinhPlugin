package com.yogurt.giamdinh.listeners;

import com.yogurt.giamdinh.GiamDinh;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final GiamDinh plugin;
    private final int npcId;

    public PlayerListener(GiamDinh plugin) {
        this.plugin = plugin;
        this.npcId = plugin.getConfigManager().getMainConfig().getInt("citizens_npc_id", -1);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getPlayerManager().loadPlayerData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerManager().unloadPlayerData(event.getPlayer());
    }

    @EventHandler
    public void onNpcRightClick(NPCRightClickEvent event) {
        if (npcId != -1 && event.getNPC().getId() == npcId) {
            // Mở GUI giám định chính khi click chuột phải vào NPC
            plugin.getGuiManager().openMainAppraisalGUI(event.getClicker());
        }
    }

    @EventHandler
    public void onNpcLeftClick(NPCLeftClickEvent event) {
        if (npcId != -1 && event.getNPC().getId() == npcId) {
             // Mở GUI shop khi click chuột trái vào NPC
             plugin.getGuiManager().openShopGUI(event.getClicker());
        }
    }
}
