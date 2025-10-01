package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.formula.Formula;
import com.github.alfonsoleandro.healthpower.managers.health.formula.gui.FormulaGUIManager;
import com.github.alfonsoleandro.healthpower.managers.health.formula.FormulaManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class FormulasHandler extends AbstractHandler {

    private final FormulaManager formulaManager;
    private final FormulaGUIManager formulaGUIManager;

    public FormulasHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.formulaManager = plugin.getFormulaManager();
        this.formulaGUIManager = plugin.getFormulaGUIManager();
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String[] args) {
        return args.length > 0 && (args[0].equalsIgnoreCase("formulas"));
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("healthPower.formulas")) {
            this.messageSender.send(sender, Message.NO_PERMISSION);
            return;
        }

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                this.messageSender.send(sender, Message.COMMAND_USE_FORMULAS,
                        "%command%", label);
                return;
            }
            // Open GUI
            this.formulaGUIManager.openFormulasGUI((Player) sender);

        } else {
            List<Formula> formulas = this.formulaManager.getFormulas(args[1]);
            if (formulas.isEmpty()) {
                this.messageSender.send(sender, Message.WORLD_HAS_NO_FORMULAS,
                        "%world%", args[1]);
                return;
            }

            //Send the list of formulas for a given world
            this.messageSender.send(sender, Message.FORMULAS_FOR_WORLD,
                    "%world%", args[1]);
            for (int i = 0; i < formulas.size(); i++) {
                Formula formula = formulas.get(i);
                this.messageSender.send(sender, Message.FORMULA_LIST_ELEMENT,
                        "%order%", String.valueOf(i + 1),
                        "%formula%", formula.getRawFormulaString());
            }

        }

    }

}
