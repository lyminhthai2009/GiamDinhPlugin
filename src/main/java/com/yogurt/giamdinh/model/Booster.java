package com.yogurt.giamdinh.model;

import com.yogurt.giamdinh.utils.ItemUtil;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

@Getter
public class Booster {
    private final String id;
    private final ItemStack item;
    private final BoosterType type;
    private final EffectType effectType;
    private final double value;

    public enum BoosterType { SINGLE_USE, TIMED }
    public enum EffectType { XP_BOOST, RARE_FIND_CHANCE, FEE_REDUCTION }

    public Booster(String id, ConfigurationSection section) {
        this.id = id;
        this.item = ItemUtil.createItem(section.getConfigurationSection("item"), null);
        this.type = BoosterType.valueOf(section.getString("type", "SINGLE_USE").toUpperCase());
        this.effectType = EffectType.valueOf(section.getString("effect.modifier").toUpperCase());
        this.value = section.getDouble("effect.value");
    }
}
