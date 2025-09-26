package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.cooldown.formula.FormulaClickedData;
import com.github.alfonsoleandro.healthpower.managers.cooldown.formula.FormulaGUIAction;
import com.github.alfonsoleandro.healthpower.managers.cooldown.formula.FormulaModifyCooldown;
import com.github.alfonsoleandro.healthpower.managers.health.formula.Formula;
import com.github.alfonsoleandro.healthpower.managers.health.formula.FormulaManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.mputils.guis.DynamicGUI;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class FormulasChatListener implements Listener {

    private final HealthPower plugin;
    private final MessageSender<Message> messageSender;
    private final FormulaManager formulaManager;
    private final FormulaModifyCooldown formulaModifyCooldown;

    public FormulasChatListener(HealthPower plugin) {
        this.plugin = plugin;
        this.messageSender = this.plugin.getMessageSender();
        this.formulaManager = plugin.getFormulaManager();
        this.formulaModifyCooldown = plugin.getFormulaModifyCooldown();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!this.formulaModifyCooldown.isInCooldown(player)) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage();

        if (message.equalsIgnoreCase("cancel")) {
            this.formulaModifyCooldown.removeCooldown(player);
            this.messageSender.send(player, Message.FORMULA_ACTION_CANCELED);
            return;
        }

        if (!player.hasPermission("HealthPower.formulas.edit")) {
            this.messageSender.send(player, Message.NO_PERMISSION);
            return;
        }

        FormulaClickedData formulaClickedData = this.formulaModifyCooldown.getData(player);
        List<Formula> formulas = this.formulaManager.getFormulas(formulaClickedData.worldName());

        if (formulaClickedData.action().equals(FormulaGUIAction.DELETE)) {
            if (!message.equalsIgnoreCase("yes")) {
                this.messageSender.send(player, Message.FORMULA_DELETE_UNKNOWN_MESSAGE);
                return;
            }

            //CONFIRM DELETE, DELETE FORMULA

            if (formulas == null || formulas.size() <= formulaClickedData.formulaOrder()) {
                this.messageSender.send(player, Message.FORMULA_DELETE_ERROR);
                return;
            }
            Formula formula = this.formulaManager.deleteFormula(formulaClickedData.worldName(), formulaClickedData.formulaOrder());
            this.messageSender.send(player, Message.FORMULA_DELETED,
                    "%world%", formulaClickedData.worldName(),
                    "%formula%", formula.getRawFormulaString());
            this.formulaModifyCooldown.removeCooldown(player);

        } else if (formulaClickedData.action().equals(FormulaGUIAction.EDIT)) {
            // Validate order is number and between range
            int newOrder;
            try {
                newOrder = Integer.parseInt(message);
            } catch (NumberFormatException e) {
                this.messageSender.send(player, Message.FORMULA_INVALID_ORDER,
                        "%min%", String.valueOf(1),
                        "%max%", String.valueOf(formulas.size()));
                return;
            }

            if (newOrder < 1 || newOrder > formulas.size()) {
                this.messageSender.send(player, Message.FORMULA_INVALID_ORDER,
                        "%min%", String.valueOf(1),
                        "%max%", String.valueOf(formulas.size()));
                return;
            }

            //finally, change order
            if (formulaClickedData.formulaOrder() != newOrder) {
                this.formulaManager.changeFormulaOrder(formulaClickedData.worldName(), formulaClickedData.formulaOrder(), newOrder);
            }
            this.messageSender.send(player, Message.FORMULA_ORDER_CHANGED,
                    "%order%", String.valueOf(newOrder));
            this.formulaModifyCooldown.removeCooldown(player);

            //Re-open GUI
            DynamicGUI gui = this.formulaManager.createFormulasGUIForWorld(formulaClickedData.worldName());
            // Open GUI synchronously
            new BukkitRunnable() {
                @Override
                public void run() {
                    gui.openGUI(player);
                }
            }.runTask(this.plugin);

        }

        //TODO: if going to create formulas with chat, add all available worlds to GUI, not only those that already have formulas
    }


}
