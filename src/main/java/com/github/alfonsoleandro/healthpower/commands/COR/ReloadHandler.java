package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.AbstractHPManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

//TODO
public class ReloadHandler extends AbstractHandler{

    private final AbstractHPManager hpManager;

    public ReloadHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.hpManager = plugin.getHpManager();
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String label, String[] args) {
        return args.length > 0 && args[0].equalsIgnoreCase("reload");
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        if(!sender.hasPermission("HealthPower.reload")) {
            this.messageSender.send(sender, Message.NO_PERMISSION);
            return;
        }
        this.plugin.reload(false);

        if(this.plugin.getConfig().getBoolean("config.check HP on reload")){
            for (Player p : Bukkit.getOnlinePlayers()){
                this.hpManager.checkAndCorrectHP(p);
            }
            this.messageSender.send(sender, Message.PLAYERS_CHECKED);
        }

//        this.playersOnGUIsManager.removeAll(); GUI close? //TODO
        this.messageSender.send(sender, "&aFiles reloaded");
    }
}
