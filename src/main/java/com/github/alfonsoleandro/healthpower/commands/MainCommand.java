package com.github.alfonsoleandro.healthpower.commands;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.commands.COR.*;
import com.github.alfonsoleandro.healthpower.managers.health.AbstractHPManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainCommand implements CommandExecutor {

    private final HealthPower plugin;
    private final MessageSender<Message> messageSender;
    private final AbstractHPManager hpManager;
    private final AbstractHandler COR;

    public MainCommand(HealthPower plugin) {
        this.plugin = plugin;
        this.messageSender = plugin.getMessageSender();
        this.hpManager = plugin.getHpManager();
        this.COR = new ShopHandler(plugin, new HelpHandler(plugin,
                new VersionHandler(plugin, new ReloadHandler(plugin,
                        new HPModifyHandler(plugin, new ConsumablesHandler(plugin,
                                new ClearHandler(plugin, new HPCheckHandler(plugin,
                                        new GroupModifyHandler(plugin, null)
                                ))
                        ))
                ))
        ));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        this.COR.handle(sender, label, args);
//        return true;

        if (args[0].equalsIgnoreCase("clear")) {
            if (!sender.hasPermission("HealthPower.clear")) {
                this.messageSender.send(sender, Message.NO_PERMISSION);
                return true;
            }
            if (args.length < 2) {
                this.messageSender.send(sender, "&cUse: &f/" + label + " clear (player)");
                return true;
            }
            FileConfiguration hp = this.plugin.getHpYaml().getAccess();
            hp.set("HP.players." + args[1], null);
            this.plugin.getHpYaml().save(true);

            this.messageSender.send(sender, Message.PLAYER_CLEARED,
                    "%player%", args[1]);

        } else if (args[0].equalsIgnoreCase("clearAll")) {
            if (!sender.hasPermission("HealthPower.clear")) {
                this.messageSender.send(sender, Message.NO_PERMISSION);
                return true;
            }
            FileConfiguration hp = this.plugin.getHpYaml().getAccess();
            hp.set("HP.players", new ArrayList<>());
            this.plugin.getHpYaml().save(true);

            this.messageSender.send(sender, Message.PLAYERS_CLEARED);


        } else if (args[0].equalsIgnoreCase("check")) {
            if (!sender.hasPermission("HealthPower.check")) {
                this.messageSender.send(sender, Message.NO_PERMISSION);
                return true;
            }
            if (args.length < 2) {
                this.messageSender.send(sender, "&cUse: &f/" + label + " check (player)");
                return true;
            }
            Player toCheck = Bukkit.getPlayer(args[1]);
            if (toCheck == null) {
                this.messageSender.send(sender, Message.PLAYER_NOT_ONLINE);
                return true;
            }
            this.hpManager.checkAndCorrectHP(toCheck);

            this.messageSender.send(sender, Message.PLAYER_CHECKED,
                    "%player%", args[1]);


        } else if (args[0].equalsIgnoreCase("checkAll")) {
            if (!sender.hasPermission("HealthPower.clear")) {
                this.messageSender.send(sender, Message.NO_PERMISSION);
                return true;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.hpManager.checkAndCorrectHP(player);
            }

            this.messageSender.send(sender, Message.PLAYERS_CHECKED);


            //unknown command
        } else {
            this.messageSender.send(sender, Message.UNKNOWN_COMMAND,
                    "%command%", label);
        }


        return true;
    }

}
