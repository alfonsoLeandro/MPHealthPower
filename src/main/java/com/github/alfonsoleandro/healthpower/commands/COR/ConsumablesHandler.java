package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.ConsumableManager;
import com.github.alfonsoleandro.healthpower.utils.Consumable;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.mputils.itemstacks.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ConsumablesHandler extends AbstractHandler {

    private final ConsumableManager consumableManager;

    public ConsumablesHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.consumableManager = plugin.getConsumableManager();
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String label, String[] args) {
        return args.length > 0 && args[0].equalsIgnoreCase("consumable");
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        if (args.length < 4 || (!args[1].equalsIgnoreCase("give") && !args[1].equalsIgnoreCase("set"))) {
            this.messageSender.send(sender, Message.COMMAND_USE_CONSUMABLE);
            return;
        }

        if (!sender.hasPermission("HealthPower.consumable." + args[1])) {
            this.messageSender.send(sender, Message.NO_PERMISSION);
            return;
        }

        // /hp consumable give (player) (name)
        if (args[1].equalsIgnoreCase("give")) {
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


        // /hp consumable set (name) (add/set) (amount)
        } else if (args[1].equalsIgnoreCase("set")) {
            if (!(sender instanceof Player player)) {
                this.messageSender.send(sender, Message.CANNOT_SEND_CONSOLE);
                return;
            }

            String mode = args[3];

            if (args.length < 5 || (!mode.equalsIgnoreCase("add") && !mode.equalsIgnoreCase("set"))) {
                this.messageSender.send(sender, Message.COMMAND_USE_CONSUMABLE);
                return;
            }

            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (this.consumableManager.invalidConsumable(inHand)) {
                this.messageSender.send(sender, Message.IN_HAND_NOT_CONSUMABLE);
                return;
            }

            double value;
            try {
                value = Double.parseDouble(args[4]);
            } catch (NumberFormatException e) {
                this.messageSender.send(sender, Message.HP_MUST_BE_NUMBER);
                return;
            }

            String consumableName = args[2];

            this.consumableManager.addConsumable(consumableName,
                    mode.equalsIgnoreCase("add") ? Consumable.ConsumableMode.ADD : Consumable.ConsumableMode.SET,
                    value,
                    null,
                    inHand);

            saveConsumableToFile(consumableName, mode, value, inHand);
            this.messageSender.send(sender, Message.CONSUMABLE_CREATED, "%name%", consumableName, "%mode%", mode, "%value%", String.valueOf(value));
        }
    }

    private void saveConsumableToFile(String consumableName, String mode, double value, ItemStack inHand) {
        FileConfiguration consumables = this.plugin.getConsumablesYaml().getAccess();
        consumables.set("consumables." + consumableName + ".options." + mode, value);
        consumables.set("consumables." + consumableName + ".item", inHand);
        this.plugin.getConsumablesYaml().save(true);
    }
}
