package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.formula.FormulaManager;
import com.github.alfonsoleandro.healthpower.managers.health.formula.PlayerHpData;
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
    private final HPShopManager hpShopManager;
    private final FormulaManager formulaManager;
    private final Settings settings;
    private final Economy economy;

    public ShopGUIClickListener(HealthPower plugin) {
        this.messageSender = plugin.getMessageSender();
        this.hpManager = plugin.getHpManager();
        this.hpShopManager = plugin.getHpGUIManager();
        this.formulaManager = plugin.getFormulaManager();
        this.settings = plugin.getSettings();
        this.economy = plugin.getEconomy();
    }

    @EventHandler
    public void onGUIClick(GUIClickEvent event) {
        // check the cause of the event is a GUI from this plugin
        if (!event.getGui().getGuiTags().equals(Settings.SHOP_GUI_TAG)) {
            return;
        }
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot >= event.getGui().getSize() || slot < 0) {
            return;
        }

        // get gui item from raw slot, from hp manager
        HPShopItem item = this.hpShopManager.getItem(slot);

        if (item == null) {
            return;
        }

        // get type from gui item
        // if type is info, return
        if (item.type().equals(HPShopItem.HPShopItemType.INFO)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (!this.hpShopManager.hasPermissionForItem(player, slot)) {
            this.messageSender.send(player, Message.SHOP_ITEM_NO_PERMISSION);
            return;
        }

        if (!this.hpShopManager.meetsRequirementForItem(player, slot)) {
            this.messageSender.send(player, Message.SHOP_ITEM_REQUIREMENTS_NOT_MET);
            return;
        }

        // get price from gui item
        double balance = this.economy.getBalance(player);
        double price = this.hpShopManager.getPriceForItem(player, slot);

        // check if player has enough money
        if (price > 0 && price > balance) {
            this.messageSender.send(player, Message.NOT_ENOUGH_MONEY,
                    "%price%", String.valueOf(price),
                    "%balance%", String.valueOf(balance));
            player.closeInventory();
            return;
        }

        if (this.settings.isCheckLimitsForShop()) {
            // Simulate to check if transaction is possible
            String worldName = player.getWorld().getName();
            Double hpCap = this.hpManager.getHpCapForWorld(worldName);
            Double hpFloor = this.hpManager.getHpFloorForWorld(worldName);
            PlayerHpData playerHpData = this.formulaManager.getPlayerHpData(player);
            double currentVal = playerHpData.shopHp() == null ? 0 : playerHpData.shopHp();
            double amount = item.amount();
            boolean hasCap = hpCap != null && hpCap != -1;

            PlayerHpData simulated;
            switch (item.type()) {
                case ADD:
                    if (!hasCap) break;
                    simulated = new PlayerHpData(playerHpData.baseHp(), playerHpData.groupHp(), playerHpData.permissionHp(), currentVal + amount);
                    double newValue = this.formulaManager.simulate(player, worldName, simulated);
                    if (newValue > hpCap) {
                        this.messageSender.send(player, Message.YOUR_HP_ABOVE_CAP);
                        return;
                    }
                    break;
                case SET:
                    simulated = new PlayerHpData(playerHpData.baseHp(), playerHpData.groupHp(), playerHpData.permissionHp(), amount);
                    double newSetValue = this.formulaManager.simulate(player, worldName, simulated);
                    if ((hasCap && newSetValue > hpCap)) {
                        this.messageSender.send(player, Message.YOUR_HP_ABOVE_CAP);
                        return;
                    }
                    if (newSetValue < hpFloor) {
                        this.messageSender.send(player, Message.YOUR_HP_BELOW_FLOOR);
                        return;
                    }
                    break;
                case REMOVE:
                    simulated = new PlayerHpData(playerHpData.baseHp(), playerHpData.groupHp(), playerHpData.permissionHp(), currentVal - amount);
                    double newRemoveValue = this.formulaManager.simulate(player, worldName, simulated);
                    if (newRemoveValue < hpFloor) {
                        this.messageSender.send(player, Message.NOT_ENOUGH_HP,
                                "%hp%", String.valueOf(item.amount()));
                        player.closeInventory();
                        return;
                    }
            }
        }

        if (item.message() != null) {
            this.messageSender.send(player, item.message()
                    .replace("%amount%", String.valueOf(item.amount())));
        }

        // finally, add/set/remove hp
        switch (item.type()) {
            case ADD:
                chargeOrPay(price, player);
                this.hpManager.addShopHP(player, item.amount());
                if (item.message() == null) {
                    this.messageSender.send(player, Message.SHOP_ADD_HP,
                            "%amount%", String.valueOf(item.amount()));
                }
                break;
            case SET:
                chargeOrPay(price, player);
                this.hpManager.setShopHP(player, item.amount());
                if (item.message() == null) {
                    this.messageSender.send(player, Message.SHOP_SET_HP,
                            "%amount%", String.valueOf(item.amount()));
                }
                break;
            case REMOVE:
                chargeOrPay(price, player);
                this.hpManager.addShopHP(player, -item.amount());
                if (item.message() == null) {
                    this.messageSender.send(player, Message.SHOP_REMOVE_HP,
                            "%amount%", String.valueOf(item.amount()));
                }

        }

        // Check and correct player's HP
        this.hpManager.checkAndCorrectHP(player);
        // Re-open the GUI if everything went correctly, to update descriptions
        this.hpShopManager.openGUI(player);
    }

    private void chargeOrPay(double price, Player player) {
        if (price < 0) {
            this.economy.depositPlayer(player, price * (-1.0));
        } else {
            this.economy.withdrawPlayer(player, price);
        }
    }

}
