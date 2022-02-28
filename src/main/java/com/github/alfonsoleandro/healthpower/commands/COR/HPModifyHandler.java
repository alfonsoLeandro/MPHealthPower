package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import org.bukkit.command.CommandSender;
//TODO
public class HPModifyHandler extends AbstractHandler{

    public HPModifyHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String label, String[] args) {
        return args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add");
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {

    }
}
