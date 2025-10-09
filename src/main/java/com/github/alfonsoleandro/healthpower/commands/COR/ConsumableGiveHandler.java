package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.consumable.ConsumableManager;
import com.github.alfonsoleandro.healthpower.managers.consumable.Consumable;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.mputils.itemstacks.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConsumableGiveHandler extends AbstractHandler {

    private final ConsumableManager consumableManager;

    public ConsumableGiveHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.consumableManager = plugin.getConsumableManager();
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String[] args) {
        return args.length > 1
                && args[0].equalsIgnoreCase("consumable")
                && args[1].equalsIgnoreCase("give");
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("HealthPower.consumable.give")) {
            this.messageSender.send(sender, Message.NO_PERMISSION);
            return;
        }

        // /hp consumable give (player) (name)
        Player toGive = Bukkit.getPlayer(args[2]);

        if (toGive == null || !toGive.isOnline()) {
            this.messageSender.send(sender, Message.PLAYER_NOT_ONLINE);
            return;
        }

        String consumableName = args[3];
        Consumable consumable = this.consumableManager.getConsumable(consumableName);

        if (consumable == null) {
            this.messageSender.send(sender, Message.CONSUMABLE_NOT_EXIST);
            return;
        }

        if (!InventoryUtils.canAdd(consumable.item(), toGive.getInventory())) {
            this.messageSender.send(sender, Message.PLAYER_INV_FULL, "%player%", toGive.getName());
            return;
        }

        toGive.getInventory().addItem(consumable.item());
        this.messageSender.send(sender, Message.CONSUMABLE_GIVEN, "%player%", toGive.getName());
        this.messageSender.send(toGive, Message.CONSUMABLE_RECEIVED);
    }

}
