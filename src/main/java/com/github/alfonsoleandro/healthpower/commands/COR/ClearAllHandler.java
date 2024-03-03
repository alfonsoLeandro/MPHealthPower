package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.AbstractHPManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ClearAllHandler extends AbstractHandler {

    private final AbstractHPManager hpManager;

    public ClearAllHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.hpManager = plugin.getHpManager();
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String label, String[] args) {
        return args.length > 0 && args[0].equalsIgnoreCase("clearAll");
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("HealthPower.clear")) {
            this.messageSender.send(sender, Message.NO_PERMISSION);
            return;
        }

        FileConfiguration hp = this.plugin.getHpYaml().getAccess();
        hp.set("HP.players", null);
        this.plugin.getHpYaml().save(true);

        this.messageSender.send(sender, Message.PLAYERS_CLEARED);
    }
}
