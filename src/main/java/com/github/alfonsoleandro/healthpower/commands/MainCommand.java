package com.github.alfonsoleandro.healthpower.commands;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.commands.COR.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MainCommand implements CommandExecutor {

    private final AbstractHandler COR;
    //TODO: add command to check formulas for world
    //TODO: modify set command to allow to set each type of HP

    public MainCommand(HealthPower plugin) {
        this.COR = new ShopHandler(plugin, new HelpHandler(plugin,
                new VersionHandler(plugin, new ReloadHandler(plugin,
                        new HPModifyHandler(plugin, new ConsumablesHandler(plugin,
                                new ClearHandler(plugin, new ClearAllHandler(plugin,
                                        new HPCheckHandler(plugin, new HPCheckAllHandler(plugin,
                                                new GroupModifyHandler(plugin, new HPInfoHandler(plugin,
                                                        new FormulasHandler(plugin,
                                                        null)
                                                        ))
                                        ))
                                ))
                        ))
                ))
        ));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        this.COR.handle(sender, label, args);
        return true;
    }

}
