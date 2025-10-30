package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HPCheckHandler extends AbstractHandler{

    private final HPManager hpManager;

    public HPCheckHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.hpManager = plugin.getHpManager();
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String[] args) {
        return args.length > 0 && args[0].equalsIgnoreCase("check");
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("HealthPower.check")) {
            this.messageSender.send(sender, Message.NO_PERMISSION);
            return;
        }
        if (args.length < 2) {
            this.messageSender.send(sender, Message.COMMAND_USE_CHECK,
                    "%command%", label);
            return;
        }
        Player toCheck = Bukkit.getPlayer(args[1]);
        if (toCheck == null) {
            this.messageSender.send(sender, Message.PLAYER_NOT_ONLINE);
            return;
        }
        this.hpManager.checkAndCorrectHP(toCheck);

        this.messageSender.send(sender, Message.PLAYER_CHECKED,
                "%player%", args[1]);


    }
}
