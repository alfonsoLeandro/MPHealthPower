package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import org.bukkit.command.CommandSender;
//TODO
public class ConsumablesHandler extends AbstractHandler{

    public ConsumablesHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String label, String[] args) {
        return args.length > 0 && args[0].equalsIgnoreCase("consumable");
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {

    }
}
