package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.shop.HPShopItem;
import com.github.alfonsoleandro.healthpower.managers.shop.HPShopManager;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.guis.events.GUIClickEvent;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ShopGUIClickListener implements Listener {

    private final MessageSender<Message> messageSender;
    private final HPManager hpManager;
    private final HPShopManager hpGUIManager;
    private final Economy economy;

    public ShopGUIClickListener(HealthPower plugin) {
        this.messageSender = plugin.getMessageSender();
        this.hpManager = plugin.getHpManager();
        this.hpGUIManager = plugin.getHpGUIManager();
        this.economy = plugin.getEconomy();
    }

    @EventHandler
    public void onGUIClick(GUIClickEvent event) {
        // check the cause of the event is a GUI from this plugin
        if (!event.getGui().getGuiTags().equals(Settings.SHOP_GUI_TAG)) {
            return;
        }
        int slot = event.getRawSlot();
        if (slot >= event.getGui().getSize()) {
            return;
        }
        event.setCancelled(true);

        // get gui item from raw slot, from hp manager
        HPShopItem item = this.hpGUIManager.getItem(slot);

        // get type from gui item
        // if type is info, return
        if (item.type().equals(HPShopItem.HPShopItemType.INFO)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // get price from gui item
        double balance = this.economy.getBalance(player);
        double currentHP = this.hpManager.getHealth(player);
        double price = this.hpGUIManager.getPrice(slot, currentHP);

        // check if player has enough money
        if (price > 0 && price > balance) {
            this.messageSender.send(player, Message.NOT_ENOUGH_MONEY,
                    "%price%", String.valueOf(price),
                    "%balance%", String.valueOf(balance));
            player.closeInventory();
            return;
        }

        // validate amount
        if (item.amount() <= 0) {
            this.messageSender.send(player, Message.CANNOT_SET_HP_UNDER_0);
            player.closeInventory();
            return;
        }

        // finally, add/set/remove hp
        switch (item.type()) {
            case ADD:
                if (this.hpManager.cannotAddHP(player, item.amount())) {
                    this.messageSender.send(player, Message.YOUR_HP_ABOVE_CAP);
                    return;
                }
                chargeOrPay(price, player);
                this.hpManager.consumableOrGUIAddHP(player, item.amount());
            case SET:
                if (this.hpManager.cannotSetHP(player, item.amount())) {
                    this.messageSender.send(player, Message.YOUR_HP_ABOVE_CAP);
                    return;
                }
                chargeOrPay(price, player);
                this.hpManager.consumableOrGUISetHP(player, item.amount());
            case REMOVE:
                if (this.hpManager.getHealth(player) < item.amount()) {
                    this.messageSender.send(player, Message.NOT_ENOUGH_HP,
                            "%hp%", String.valueOf(item.amount()));
                    player.closeInventory();
                    return;
                }
                chargeOrPay(price, player);
                this.hpManager.guiRemoveHP(player, item.amount());
        }

        // Re-open the GUI if everything went correctly, to update descriptions
        this.hpGUIManager.openGUI(player);

    }

    private void chargeOrPay(double price, Player player) {
        if (price < 0) {
            this.economy.depositPlayer(player, price * (-1.0));
        } else {
            this.economy.withdrawPlayer(player, price);
        }
    }

}
