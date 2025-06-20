package com.yogurt.giamdinh.utils;

import com.yogurt.giamdinh.GiamDinh;
import dev.lone.itemsadder.api.CustomStack;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;

public class ItemUtil {

    public static ItemStack createItem(ConfigurationSection section, Player player) {
        if (section == null) return new ItemStack(Material.AIR);

        String typeStr = section.getString("type", "VANILLA").toUpperCase();
        String id = section.getString("id");

        ItemStack item;
        switch (typeStr) {
            case "MMOITEMS":
                String[] mmoId = id.split(";");
                item = MMOItems.plugin.getItem(net.Indyuce.mmoitems.api.Type.get(mmoId[0]), mmoId[1]);
                break;
            case "ITEMSADDER":
                CustomStack customStack = CustomStack.getInstance(id);
                item = (customStack != null) ? customStack.getItemStack() : new ItemStack(Material.STONE);
                break;
            case "PLAYER_HEAD":
                item = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                if (player != null) {
                    skullMeta.setOwningPlayer(player);
                }
                item.setItemMeta(skullMeta);
                break;
            default: // VANILLA
                item = new ItemStack(Material.getMaterial(id, Material.STONE));
                break;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (section.contains("name")) {
                meta.setDisplayName(ChatUtil.setPlaceholders(player, section.getString("name")));
            }
            if (section.contains("lore")) {
                meta.setLore(ChatUtil.setPlaceholders(player, section.getStringList("lore")));
            }
            if (section.contains("custom_model_data")) {
                meta.setCustomModelData(section.getInt("custom_model_data"));
            }
            if (section.getBoolean("glow", false)) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String getItemIdentifier(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;

        // Check ItemsAdder
        CustomStack customStack = CustomStack.byItemStack(item);
        if (customStack != null) {
            return customStack.getNamespacedID();
        }

        // Check MMOItems
        String mmoIdentifier = MMOItems.getTypeName(item) + ";" + MMOItems.getID(item);
        if (MMOItems.getType(item) != null && MMOItems.getID(item) != null) {
            return mmoIdentifier;
        }

        // Default to Vanilla Material name
        return item.getType().name();
    }
}
