package com.github.alfonsoleandro.healthpower.managers.shop;

import org.bukkit.inventory.ItemStack;

/**
 * @author alfonsoLeandro
 */
public record HPShopItem(ItemStack itemStack, HPShopItemType type, String priceOrFormula, double amount) {

    public enum HPShopItemType {
        INFO,
        ADD,
        SET,
        REMOVE
    }
}
