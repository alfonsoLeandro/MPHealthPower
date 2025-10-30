package com.github.alfonsoleandro.healthpower.managers.shop;

import com.github.alfonsoleandro.healthpower.managers.health.formula.Formula;
import com.github.alfonsoleandro.healthpower.managers.health.formula.FormulaVariable;
import com.github.alfonsoleandro.healthpower.managers.health.formula.PlayerHpData;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * @author alfonsoLeandro
 */
public record HPShopItem(
        ItemStack itemStack,
        HPShopItemType type,
        Double price,
        double amount,
        String node,
        List<Formula> formulas,
        String message,
        Map<FormulaVariable, HPShopItemRequirementValues> requirements
) {

    public enum HPShopItemType {
        INFO,
        ADD,
        SET,
        REMOVE
    }

    public record HPShopItemRequirementValues(Double min, Double max) {
    }

    public boolean meetsRequirement(FormulaVariable variable, PlayerHpData playerHpData) {
        HPShopItemRequirementValues hpShopItemRequirementValues = this.requirements.get(variable);
        if (hpShopItemRequirementValues == null) {
            return true;
        }
        Double min = hpShopItemRequirementValues.min;
        Double max = hpShopItemRequirementValues.max;
        Double healthType;
        switch (variable) {
            case BASE -> healthType = playerHpData.baseHp();
            case GROUP -> healthType = playerHpData.groupHp();
            case PERMISSION -> healthType = playerHpData.permissionHp();
            default -> healthType = playerHpData.shopHp();
        }
        return ((min == null || (healthType != null && healthType >= min))
                        && (max == null ||  healthType == null || healthType <= max));
    }
}
