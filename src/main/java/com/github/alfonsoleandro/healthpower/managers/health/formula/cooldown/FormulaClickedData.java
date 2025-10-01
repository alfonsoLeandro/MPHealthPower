package com.github.alfonsoleandro.healthpower.managers.health.formula.cooldown;

import org.bukkit.scheduler.BukkitTask;

public record FormulaClickedData(
        String worldName,
        int formulaOrder,
        FormulaGUIAction action,
        BukkitTask cooldownTimer
) {
}
