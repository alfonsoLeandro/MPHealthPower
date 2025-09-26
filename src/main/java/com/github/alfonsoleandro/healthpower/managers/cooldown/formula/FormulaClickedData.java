package com.github.alfonsoleandro.healthpower.managers.cooldown.formula;

import org.bukkit.scheduler.BukkitTask;

public record FormulaClickedData(String worldName, int formulaOrder, FormulaGUIAction action, BukkitTask cooldownTimer) {
}
