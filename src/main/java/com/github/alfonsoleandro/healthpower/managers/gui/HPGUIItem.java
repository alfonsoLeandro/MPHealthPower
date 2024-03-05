package com.github.alfonsoleandro.healthpower.managers.gui;

import org.bukkit.inventory.ItemStack;

/**
 * @author alfonsoLeandro
 */
public record HPGUIItem(ItemStack itemStack, HPGUIItemType type, String priceOrFormula, double amount) {

    public enum HPGUIItemType {
        INFO,
        ADD,
        SET,
        REMOVE
    }
}
