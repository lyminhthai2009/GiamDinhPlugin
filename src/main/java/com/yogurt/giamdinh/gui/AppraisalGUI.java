package com.yogurt.giamdinh.gui;

import com.yogurt.giamdinh.GiamDinh;
import com.yogurt.giamdinh.model.Booster;
import com.yogurt.giamdinh.model.LevelPerk;
import com.yogurt.giamdinh.utils.ChatUtil;
import com.yogurt.giamdinh.utils.ItemUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AppraisalGUI extends BaseGUI {

    private final int ITEM_SLOT = 13;
    private final int BOOSTER_SLOT = 30;
    private final int CONFIRM_SLOT = 32;

    public AppraisalGUI(GiamDinh plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public String getTitle() {
        return ChatUtil.color(plugin.getConfigManager().getMainConfig().getString("gui.main_appraisal_title"));
    }

    @Override
    public int getSize() {
        return plugin.getConfigManager().getMainConfig().getInt("gui.appraisal_gui_size", 54);
    }

    @Override
    public void setup() {
        // Create border
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        for (int i = 0; i < getSize(); i++) {
            inventory.setItem(i, filler);
        }

        // Create functional slots
        inventory.setItem(ITEM_SLOT, createPlaceholderItem(Material.CHEST, "&aĐặt vật phẩm giám định vào đây", List.of("&7Tối đa: " + getMaxBatchSize() + " vật phẩm")));
        inventory.setItem(BOOSTER_SLOT, createPlaceholderItem(Material.EXPERIENCE_BOTTLE, "&bĐặt bùa hỗ trợ vào đây", List.of("&7(Tùy chọn)")));
        inventory.setItem(CONFIRM_SLOT, createPlaceholderItem(Material.ANVIL, "&a&lXÁC NHẬN GIÁM ĐỊNH", List.of("&7Click để bắt đầu giám định.")));

        // Allow putting items in
        inventory.setItem(22, null); // An empty slot for visual separation
    }

    private int getMaxBatchSize() {
        LevelPerk perk = plugin.getLevelManager().getPerksForLevel(plugin.getPlayerManager().getPlayerData(player).getLevel());
        return perk.getMaxBatchSize();
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true); // Cancel all clicks by default

        int slot = e.getRawSlot();
        ItemStack clickedItem = e.getCurrentItem();
        ItemStack cursorItem = e.getCursor();

        // Allow placing items in the main slot and booster slot
        if (slot == ITEM_SLOT || slot == BOOSTER_SLOT) {
            if (cursorItem != null && !cursorItem.getType().isAir()) {
                // Placing an item
                e.setCancelled(false);
            } else if (clickedItem != null && !clickedItem.getType().isAir()) {
                // Taking an item out
                e.setCancelled(false);
            }
            return;
        }

        if (slot == CONFIRM_SLOT) {
            handleConfirmClick();
        }
    }

    private void handleConfirmClick() {
        ItemStack toAppraise = inventory.getItem(ITEM_SLOT);
        ItemStack boosterItem = inventory.getItem(BOOSTER_SLOT);
        Booster appliedBooster = null;

        if (toAppraise == null || toAppraise.getType().isAir() || toAppraise.getItemMeta().getDisplayName().contains("Đặt vật phẩm")) {
            ChatUtil.sendMessage(player, plugin.getConfigManager().getMessage("item_required_in_gui"));
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1.5f);
            return;
        }

        if (toAppraise.getAmount() > getMaxBatchSize()) {
            ChatUtil.sendMessage(player, plugin.getConfigManager().getMessage("batch_size_exceeded").replace("{max}", String.valueOf(getMaxBatchSize())));
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1.5f);
            return;
        }

        if (boosterItem != null && !boosterItem.getType().isAir()) {
            String boosterId = getBoosterId(boosterItem);
            if (boosterId != null) {
                appliedBooster = plugin.getConfigManager().getBooster(boosterId);
                if (appliedBooster != null) {
                    ChatUtil.sendMessage(player, plugin.getConfigManager().getMessage("booster_applied")
                        .replace("{booster_name}", ChatUtil.color(appliedBooster.getItem().getItemMeta().getDisplayName())));
                }
            }
        }
        
        player.closeInventory();
        
        // Process each item in the stack
        int amount = toAppraise.getAmount();
        ItemStack singleItem = toAppraise.clone();
        singleItem.setAmount(1);

        for(int i = 0; i < amount; i++){
            plugin.getAppraisalManager().performAppraisal(player, singleItem, appliedBooster);
        }

        // Consume booster if it's single use
        if (appliedBooster != null && boosterItem != null && appliedBooster.getType() == Booster.BoosterType.SINGLE_USE) {
            boosterItem.setAmount(boosterItem.getAmount() - 1);
        }
    }
    
    private String getBoosterId(ItemStack item) {
        if(item == null || !item.hasItemMeta()) return null;
        for (Booster booster : plugin.getConfigManager().getBoosters().values()) {
            if (item.isSimilar(booster.getItem())) {
                return booster.getId();
            }
        }
        return null;
    }

    private ItemStack createPlaceholderItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatUtil.color(name));
        meta.setLore(ChatUtil.color(lore));
        item.setItemMeta(meta);
        return item;
    }
}
