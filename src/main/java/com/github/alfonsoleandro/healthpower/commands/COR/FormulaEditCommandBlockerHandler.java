package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.formula.cooldown.FormulaModifyManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Prevents players from sending HP commands when editing formulas.
 */
public class FormulaEditCommandBlockerHandler extends AbstractHandler {

    private final FormulaModifyManager formulaModifyManager;

    public FormulaEditCommandBlockerHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.formulaModifyManager = plugin.getFormulaModifyManager();
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String[] args) {
        return sender instanceof Player && !this.formulaModifyManager.isNotInCooldown((Player) sender);
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        this.messageSender.send(sender, Message.FORMULA_EDITING_BLOCKED_COMMANDS);
    }
}
