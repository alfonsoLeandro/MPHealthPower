package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.AbstractHPManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HPModifyHandler extends AbstractHandler {

    private final AbstractHPManager hpManager;

    public HPModifyHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.hpManager = plugin.getHpManager();
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String label, String[] args) {
        return args.length > 0 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add"));
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("healthPower." + args[0])) {
            this.messageSender.send(sender, Message.NO_PERMISSION);
            return;
        }

        if (args.length <= 2) {
            this.messageSender.send(sender, Message.COMMAND_USE_HP_MODIFY,
                    "%what%", args[0],
                    "%command%", label);
            return;
        }

        Player toAdd = Bukkit.getPlayer(args[1]);
        if (toAdd == null || !toAdd.isOnline()) {
            this.messageSender.send(sender, Message.PLAYER_NOT_ONLINE);
            return;
        }

        double hp;
        try {
            hp = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            this.messageSender.send(sender, Message.HP_MUST_BE_NUMBER);
            return;
        }

        if (args[0].equalsIgnoreCase("set")) {
            this.hpManager.setHPCommand(sender, toAdd, hp);
        } else {
            this.hpManager.addHP(sender, toAdd, hp);
        }


    }
}
