package com.github.alfonsoleandro.healthpower.commands;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.commands.COR.*;
import com.github.alfonsoleandro.healthpower.managers.AbstractHPManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.PlayersOnGUIsManager;
import com.github.alfonsoleandro.mputils.managers.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainCommand implements CommandExecutor {

    private final PlayersOnGUIsManager playersOnGUIsManager = PlayersOnGUIsManager.getInstance();
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

    private void addItem(Player player, ItemStack itemStack){
        Inventory inv = player.getInventory();
        if(inv.firstEmpty() != -1) {
            inv.addItem(itemStack);
            return;
        }else{
            for(int j = 0; j <= 35 ; j++) {
                ItemStack it = inv.getItem(j);
                assert it != null;
                if(it.isSimilar(itemStack) && it.getAmount() < 64) {
                    inv.addItem(itemStack);
                    return;
                }
            }
        }
        this.messageSender.send(player, Message.INV_FULL);
        player.getWorld().dropItem(player.getLocation(), itemStack);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        FileConfiguration config = this.plugin.getConfig();
        this.COR.handle(sender, label, args);
//        return true;

       if(args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add")) {
            if(!sender.hasPermission("healthPower." + args[0])) {
                this.messageSender.send(sender, Message.NO_PERMISSION);
                return true;
            }
            if(args.length > 2) {
                try {
                    double hp = Double.parseDouble(args[2]);
                    Player toAdd = Bukkit.getPlayer(args[1]);
                    if(toAdd != null) {
                        if(args[0].equalsIgnoreCase("set")){
                            this.hpManager.setHPCommand(sender, toAdd, hp);
                        }else{
                            this.hpManager.addHP(sender, toAdd, hp);
                        }
                    } else {
                        this.messageSender.send(sender, Message.PLAYER_NOT_ONLINE);
                    }

                } catch (Exception e) {
                    this.messageSender.send(sender, Message.HP_MUST_BE_NUMBER);
                }


            } else {
                this.messageSender.send(sender, Message.COMMAND_USE,
                        "%what%", args[0],
                        "%command%", label);
            }


        }else if(args[0].equalsIgnoreCase("consumable")) {
            if(sender instanceof Player) {
                if(!sender.hasPermission("healthPower.consumables")) {
                    this.messageSender.send(sender, Message.NO_PERMISSION);
                    return true;
                }


                if(args.length < 3 || (!args[1].equalsIgnoreCase("get") && !args[1].equalsIgnoreCase("set"))) {
                    this.messageSender.send(sender, "&cUse: &f/" + label + " consumable (get/set) (name)");
                } else {
                    FileConfiguration consumables = this.plugin.getConsumablesYaml().getAccess();
                    final String consumableName = args[2];
                    if(args[1].equalsIgnoreCase("get")) {
                        //get code
                        if(consumables.contains("consumables." + consumableName + ".item")) {
                            ItemStack consumableItem = consumables.getItemStack("consumables." + consumableName + ".item");
                            addItem((Player) sender, consumableItem);
                            this.messageSender.send(sender, Message.CONSUMABLE_GIVEN);

                        } else {
                            this.messageSender.send(sender, "&cA consumable with that name does not exist.");
                        }

                    } else {
                        //Set code
                        if(args.length < 5 || (!args[3].equalsIgnoreCase("add") && !args[3].equalsIgnoreCase("set"))) {
                            this.messageSender.send(sender, "&cUse: &f/" + label + " consumables set " + consumableName + " (add/set) (HP amount)");
                            return true;
                        }
                        Player player = (Player) sender;
                        ItemStack inHand = player.getInventory().getItemInHand();
                        if(inHand.getType().equals(Material.AIR) && !(inHand.getType().isEdible() || inHand.getType().equals(Material.POTION))) {
                            this.messageSender.send(sender, Message.IN_HAND_NOT_CONSUMABLE);
                            return true;
                        }
                        double value;

                        try {
                            value = Double.parseDouble(args[4]);
                        } catch (NumberFormatException e) {
                            this.messageSender.send(sender, "&c" + args[4] + " is not a valid number.");
                            return true;
                        }

                        consumables.set("consumables." + consumableName + ".item", inHand);
                        consumables.set("consumables." + consumableName + ".options." + args[3], value);
                        this.plugin.getConsumablesYaml().save(true);
                        this.messageSender.send(sender, "&aConsumable saved!");

                    }

                }

            } else {
                this.messageSender.send(sender, "&cThat command can only be sent by a player");
            }


        }else if(args[0].equalsIgnoreCase("clear")){
            if(!sender.hasPermission("HealthPower.clear")){
                this.messageSender.send(sender, Message.NO_PERMISSION);
                return true;
            }
            if(args.length < 2){
                this.messageSender.send(sender, "&cUse: &f/"+label+" clear (player)");
                return true;
            }
            FileConfiguration hp = this.plugin.getHpYaml().getAccess();
            hp.set("HP.players."+args[1], null);
            this.plugin.getHpYaml().save(true);

            this.messageSender.send(sender, Message.PLAYER_CLEARED,
                    "%player%", args[1]);

        }else if(args[0].equalsIgnoreCase("clearAll")){
            if(!sender.hasPermission("HealthPower.clear")){
                this.messageSender.send(sender, Message.NO_PERMISSION);
                return true;
            }
            FileConfiguration hp = this.plugin.getHpYaml().getAccess();
            hp.set("HP.players", new ArrayList<>());
            this.plugin.getHpYaml().save(true);

            this.messageSender.send(sender, Message.PLAYERS_CLEARED);


        }else if(args[0].equalsIgnoreCase("check")){
            if(!sender.hasPermission("HealthPower.check")){
                this.messageSender.send(sender, Message.NO_PERMISSION);
                return true;
            }
            if(args.length < 2){
                this.messageSender.send(sender, "&cUse: &f/"+label+" check (player)");
                return true;
            }
            Player toCheck = Bukkit.getPlayer(args[1]);
            if(toCheck == null){
                this.messageSender.send(sender, Message.PLAYER_NOT_ONLINE);
                return true;
            }
            this.hpManager.checkAndCorrectHP(toCheck);

            this.messageSender.send(sender, Message.PLAYER_CHECKED,
                    "%player%", args[1]);


        }else if(args[0].equalsIgnoreCase("checkAll")){
            if(!sender.hasPermission("HealthPower.clear")){
                this.messageSender.send(sender, Message.NO_PERMISSION);
                return true;
            }
            for(Player player : Bukkit.getOnlinePlayers()){
                this.hpManager.checkAndCorrectHP(player);
            }

            this.messageSender.send(sender, Message.PLAYERS_CHECKED);





            //unknown command
        }else {
            this.messageSender.send(sender, Message.UNKNOWN_COMMAND,
                    "%command%", label);
        }



        return true;
    }

}
