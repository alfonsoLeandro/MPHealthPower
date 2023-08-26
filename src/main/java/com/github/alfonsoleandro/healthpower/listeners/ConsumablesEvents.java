package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.AbstractHPManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class ConsumablesEvents implements Listener {

    private final HealthPower plugin;
    private final MessageSender<Message> messageSender;
    private final AbstractHPManager hpManager;

    public ConsumablesEvents(HealthPower plugin){
        this.plugin = plugin;
        this.messageSender = plugin.getMessageSender();
        this.hpManager = plugin.getHpManager();
    }


    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event){
        ConfigurationSection consumables = this.plugin.getConsumablesYaml().getAccess().getConfigurationSection("consumables");

        if(consumables == null) return;
        ItemStack consumed = event.getItem();

        for(String consumable : consumables.getKeys(false)){
            if(consumed.isSimilar(consumables.getItemStack(consumable+".item"))){
                Player player = event.getPlayer();

                if(!this.plugin.getConfig().getBoolean("config.consumables enabled")){
                    this.messageSender.send(player, Message.CONSUMABLES_DISABLED);
                    event.setCancelled(true);
                    return;
                }

                if(consumables.contains(consumable+".options.add")){
                    if(!this.hpManager.consumableAddHP(player, consumables.getDouble(consumable+".options.add"))){
                        return;
                    }

                }else if(consumables.contains(consumable+".options.set")){
                    if(!this.hpManager.consumableSetHP(player, consumables.getDouble(consumable+".options.set"))){
                        return;
                    }

                }else{
                    return;
                }


                String message;

                if(consumables.contains(consumable+".options.message")){
                    message = consumables.getString(consumable+".options.message")+"";
                }else{
                    message = this.messageSender.getString(Message.DEFAULT_CONSUMABLE_MSG);
                }

                this.messageSender.send(player, message.replace("%HP%", String.valueOf(this.hpManager.getHealth(player))));




                return;
            }
        }

    }




}
