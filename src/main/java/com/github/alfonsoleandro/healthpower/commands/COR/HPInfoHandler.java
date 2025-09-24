package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import com.github.alfonsoleandro.healthpower.managers.health.formula.Formula;
import com.github.alfonsoleandro.healthpower.managers.health.formula.PlayerHpData;
import com.github.alfonsoleandro.healthpower.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HPInfoHandler extends AbstractHandler {

    private final HPManager hpManager;

    public HPInfoHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.hpManager = plugin.getHpManager();
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String label, String[] args) {
        return args.length > 0 && (args[0].equalsIgnoreCase("info"));
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("healthPower.info")) {
            this.messageSender.send(sender, Message.NO_PERMISSION);
            return;
        }

        if (args.length <= 1) {
            this.messageSender.send(sender, Message.COMMAND_USE_INFO,
                    "%command%", label);
            return;
        }

        Player toCheck = Bukkit.getPlayer(args[1]);
        if (toCheck == null || !toCheck.isOnline()) {
            this.messageSender.send(sender, Message.PLAYER_NOT_ONLINE);
            return;
        }

        World world;
        if (args.length == 3) {
            world = Bukkit.getWorld(args[2]);
            if (world == null) {
                this.messageSender.send(sender, Message.INVALID_WORLD);
                return;
            }
        } else {
            world = toCheck.getWorld();
        }

        PlayerHpData playerHpData = this.hpManager.getPlayerHpData(toCheck, world.getName());
        Formula applicableFormula = this.hpManager.getApplicableFormula(toCheck, world.getName());

        this.messageSender.send(sender, Message.PLAYER_HP_INFO,
                "%player%", toCheck.getName(),
                "%world%", world.getName(),
                "%formula%", applicableFormula.getRawFormulaString(),
                "%base%", playerHpData.baseHp().toString(),
                "%group%", (playerHpData.hasGroupHp() ? playerHpData.groupHp().toString() : this.messageSender.getString(Message.UNDEFINED)),
                "%permission%", (playerHpData.hasPermissionHp() ? playerHpData.permissionHp().toString() : this.messageSender.getString(Message.UNDEFINED)),
                "%shop%", (playerHpData.hasShopHp() ? playerHpData.shopHp().toString() : this.messageSender.getString(Message.UNDEFINED)));

    }
}
