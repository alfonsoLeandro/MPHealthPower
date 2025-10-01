package com.github.alfonsoleandro.healthpower.managers.cooldown.formula;

import com.github.alfonsoleandro.healthpower.managers.health.formula.Formula;

public record FormulaCreationData(String worldName, int formulaOrder, Formula formula) {
}
