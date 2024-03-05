package com.github.alfonsoleandro.healthpower.managers.consumable;

import org.bukkit.inventory.ItemStack;

/**
 * @author alfonsoLeandro
 */
public record Consumable(String name, ConsumableMode mode, double amount, String message, ItemStack item) {

    public enum ConsumableMode {
        ADD,
        SET
    }
}
