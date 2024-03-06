package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.consumable.ConsumableManager;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import com.github.alfonsoleandro.healthpower.managers.consumable.Consumable;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class ConsumablesListener implements Listener {

    private final HPManager hpManager;
    private final ConsumableManager consumableManager;
    private final MessageSender<Message> messageSender;
    private final Settings settings;

    public ConsumablesListener(HealthPower plugin) {
        this.consumableManager = plugin.getConsumableManager();
        this.hpManager = plugin.getHpManager();
        this.messageSender = plugin.getMessageSender();
        this.settings = plugin.getSettings();
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack consumed = event.getItem();
        if (consumed.hasItemMeta()) {
            PersistentDataContainer persistentDataContainer = Objects.requireNonNull(consumed.getItemMeta()).getPersistentDataContainer();
            if (!persistentDataContainer.has(this.consumableManager.getNamespacedKey())) {
                return;
            }

            if (!this.settings.isConsumablesEnabled()) {
                this.messageSender.send(event.getPlayer(), Message.CONSUMABLES_DISABLED);
                event.setCancelled(true);
                return;
            }

            String consumableName = persistentDataContainer.get(this.consumableManager.getNamespacedKey(), PersistentDataType.STRING);
            Consumable consumable = this.consumableManager.getConsumable(consumableName);

            if (consumable == null) {
                return;
            }

            Player player = event.getPlayer();

            if (consumable.mode() == Consumable.ConsumableMode.ADD) {
                this.hpManager.consumableOrGUIAddHP(player, consumable.amount());
            } else if (consumable.mode() == Consumable.ConsumableMode.SET) {
                this.hpManager.consumableOrGUISetHP(player, consumable.amount());
            }

            String message = consumable.message() == null ? this.messageSender.getString(Message.DEFAULT_CONSUMABLE_MSG) : consumable.message();

            this.messageSender.send(player, message.replace("%HP%", String.valueOf(this.hpManager.getHealth(player))));
        }

    }


}
