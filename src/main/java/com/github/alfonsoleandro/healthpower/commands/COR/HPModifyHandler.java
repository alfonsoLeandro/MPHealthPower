package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HPModifyHandler extends AbstractHandler {

    private final HPManager hpManager;
    private final Settings settings;

    public HPModifyHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.hpManager = plugin.getHpManager();
        this.settings = plugin.getSettings();
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String[] args) {
        return args.length > 0 && (args[0].equalsIgnoreCase("modify"));
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("healthPower.modify")) {
            this.messageSender.send(sender, Message.NO_PERMISSION);
            return;
        }

        if (args.length < 5) {
            this.messageSender.send(sender, Message.COMMAND_USE_HP_MODIFY,
                    "%command%", label);
            return;
        }

        String mode = args[1];
        if (!mode.equalsIgnoreCase("add") && !mode.equalsIgnoreCase("set")) {
            this.messageSender.send(sender, Message.COMMAND_USE_HP_MODIFY,
                    "%command%", label);
            return;
        }

        Player toModify = Bukkit.getPlayer(args[2]);
        if (toModify == null || !toModify.isOnline()) {
            this.messageSender.send(sender, Message.PLAYER_NOT_ONLINE);
            return;
        }

        String type = args[3];
        if (!type.equalsIgnoreCase("base") && !type.equalsIgnoreCase("shop")) {
            this.messageSender.send(sender, Message.COMMAND_USE_HP_MODIFY,
                    "%command%", label);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[4]);
        } catch (NumberFormatException e) {
            this.messageSender.send(sender, Message.HP_MUST_BE_NUMBER);
            return;
        }

        if (mode.equalsIgnoreCase("add")) {
            if (type.equalsIgnoreCase("base")) {
                this.hpManager.addBaseHP(toModify, amount);
            } else if (type.equalsIgnoreCase("shop")) {
                this.hpManager.addShopHP(toModify, amount);
            }
            if (this.settings.isNotifyHPModify() && !sender.equals(toModify)) {
                this.messageSender.send(toModify, Message.YOUR_HP_ADDED,
                        "%type%", type.toLowerCase(),
                        "%amount%", String.valueOf(amount));
            }
            this.messageSender.send(sender, Message.YOU_ADD_HP,
                    "%player%", toModify.getName(),
                    "%type%", type,
                    "%amount%", String.valueOf(amount));

        } else if (mode.equalsIgnoreCase("set")) {
            if (type.equalsIgnoreCase("base")) {
                this.hpManager.setBaseHP(toModify, amount);
            } else if (type.equalsIgnoreCase("shop")) {
                this.hpManager.setShopHP(toModify, amount);
            }
            if (this.settings.isNotifyHPModify() && !sender.equals(toModify)) {
                this.messageSender.send(toModify, Message.YOUR_HP_SET,
                        "%type%", type.toLowerCase(),
                        "%amount%", String.valueOf(amount));
            }
            this.messageSender.send(sender, Message.YOU_SET_HP,
                    "%player%", toModify.getName(),
                    "%type%", type,
                    "%amount%", String.valueOf(amount));
        }

        // Finally correct HP
        this.hpManager.checkAndCorrectHP(toModify);
    }
}
