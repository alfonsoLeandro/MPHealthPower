package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.AbstractHPManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.PlayersOnGUIsManager;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryEvents implements Listener {

    private final PlayersOnGUIsManager playersOnGUIsManager = PlayersOnGUIsManager.getInstance();
    private final HealthPower plugin;
    private final MessageSender<Message> messageSender;
    private final AbstractHPManager hpManager;

    public InventoryEvents(HealthPower plugin){
        this.plugin = plugin;
        this.messageSender = plugin.getMessageSender();
        this.hpManager = plugin.getHpManager();
    }


    @EventHandler
    public void onClick(InventoryClickEvent event){
        FileConfiguration config = this.plugin.getConfig();
        Player player = (Player) event.getWhoClicked();

        if(this.playersOnGUIsManager.isInGUI(player.getName())){
            event.setCancelled(true);
            String path = "config.GUI.items."+event.getRawSlot();
            if(!config.contains(path)) return;
            String type = config.getString(path+".type");
            if(type == null || type.equalsIgnoreCase("info")) return;

            Economy econ = this.plugin.getEconomy();
            double health = this.hpManager.getHealth(player);
            double amount = config.getDouble(path+".amount");
            double balance = econ.getBalance(player);
//            double price = this.plugin.calculatePrice(config.getString(path+".price"), health);

            if(price > 0 && price > balance){
                this.messageSender.send(player, Message.NOT_ENOUGH_MONEY,
                        "%price%", String.valueOf(price)
                        ,"%balance%", String.valueOf(balance));
                player.closeInventory();
                return;
            }

            if(!type.equalsIgnoreCase("set")
                    && !type.equalsIgnoreCase("add")
                    && !type.equalsIgnoreCase("remove")){
                type = "add";
            }

            if(amount <= 0){
                this.messageSender.send(player, Message.CANNOT_SET_HP_UNDER_0);
                player.closeInventory();
                return;
            }

            if(type.equalsIgnoreCase("remove") && amount > health){
                this.messageSender.send(player, Message.NOT_ENOUGH_HP,
                        "%hp%", String.valueOf(amount));
                player.closeInventory();
                return;
            }

            if(price < 0){
                econ.depositPlayer(player, price*(-1.0));
            }else{
                econ.withdrawPlayer(player, price);
            }

            if(type.equalsIgnoreCase("set")){
                if(!this.hpManager.guiSetHP(player, amount)) return;

            }else if(type.equalsIgnoreCase("add")){
                if(!this.hpManager.guiAddHP(player, amount)) return;

            }else{
                if(!this.hpManager.guiRemoveHP(player, amount)) return;
            }

            player.closeInventory();

        }
    }



    @EventHandler
    public void onClose(InventoryCloseEvent event){
        this.playersOnGUIsManager.remove(event.getPlayer().getName());
    }
}
