package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ClearHandler extends AbstractHandler {

    private final HPManager hpManager;

    public ClearHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.hpManager = plugin.getHpManager();
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String label, String[] args) {
        return args.length > 0 && args[0].equalsIgnoreCase("clear");
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("HealthPower.clear")) {
            this.messageSender.send(sender, Message.NO_PERMISSION);
            return;
        }
        if (args.length < 2) {
            this.messageSender.send(sender, Message.CLEAR_USE,
                    "%command%", label);
            return;
        }
        Player player = Bukkit.getPlayer(args[1]);
        if (player == null || !player.isOnline()) {
            this.messageSender.send(sender, Message.PLAYER_NOT_ONLINE);
            return;
        }

        FileConfiguration hp = this.plugin.getHpYaml().getAccess();
        hp.set("HP.players." + args[1], null);
        this.plugin.getHpYaml().save(false);
        this.hpManager.checkAndCorrectHP(player);

        this.messageSender.send(player, Message.YOUR_HP_CLEARED);
        this.messageSender.send(sender, Message.PLAYER_CLEARED,
                "%player%", args[1]);
    }
}
