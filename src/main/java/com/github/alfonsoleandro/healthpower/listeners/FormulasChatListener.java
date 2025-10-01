package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.formula.cooldown.FormulaClickedData;
import com.github.alfonsoleandro.healthpower.managers.health.formula.cooldown.FormulaCreationData;
import com.github.alfonsoleandro.healthpower.managers.health.formula.cooldown.FormulaGUIAction;
import com.github.alfonsoleandro.healthpower.managers.health.formula.cooldown.FormulaModifyManager;
import com.github.alfonsoleandro.healthpower.managers.health.formula.Formula;
import com.github.alfonsoleandro.healthpower.managers.health.formula.gui.FormulaGUIManager;
import com.github.alfonsoleandro.healthpower.managers.health.formula.FormulaManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
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
    private final FormulaGUIManager formulaGUIManager;
    private final FormulaModifyManager formulaModifyManager;

    public FormulasChatListener(HealthPower plugin) {
        this.plugin = plugin;
        this.messageSender = this.plugin.getMessageSender();
        this.formulaManager = plugin.getFormulaManager();
        this.formulaGUIManager = plugin.getFormulaGUIManager();
        this.formulaModifyManager = plugin.getFormulaModifyCooldown();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (this.formulaModifyManager.isNotInCooldown(player)) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage();
        FormulaCreationData creationData = this.formulaModifyManager.getCreationData(player);

        if (message.equalsIgnoreCase("cancel")) {
            this.formulaModifyManager.removeCooldown(player);
            this.messageSender.send(player, Message.FORMULA_ACTION_CANCELED);
            // Re-open last GUI
            new BukkitRunnable() {
                @Override
                public void run() {
                    FormulasChatListener.this.formulaGUIManager.openLastGUI(player);
                }
            }.runTask(this.plugin);
            return;
        }

        if (!player.hasPermission("HealthPower.formulas.edit")) {
            this.messageSender.send(player, Message.NO_PERMISSION);
            return;
        }

        FormulaClickedData formulaClickedData = this.formulaModifyManager.getData(player);
        List<Formula> formulas = this.formulaManager.getFormulas(formulaClickedData.worldName());

        FormulaGUIAction action = formulaClickedData.action();
        if (action.equals(FormulaGUIAction.DELETE)) {
            handleDelete(message, player, formulas, formulaClickedData);
        } else if (action.equals(FormulaGUIAction.EDIT)) {
            handleEdit(message, player, formulas, formulaClickedData);
        } else if (action.equals(FormulaGUIAction.CREATE_SET_ORDER)) {
            handleCreateSetOrder(message, player, formulas, creationData);
        } else if (action.equals(FormulaGUIAction.CREATE_SET_STRING)) {
            handleCreateSetString(message, player, creationData);

        }
    }

    private void handleEdit(String message, Player player, List<Formula> formulas, FormulaClickedData formulaClickedData) {
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
        this.formulaModifyManager.removeCooldown(player);

        //Re-open GUI
        new BukkitRunnable() {
            @Override
            public void run() {
                FormulasChatListener.this.formulaGUIManager.openFormulasGUIForWorld(player,
                        formulaClickedData.worldName());
            }
        }.runTask(this.plugin);
    }

    private void handleDelete(String message, Player player, List<Formula> formulas, FormulaClickedData formulaClickedData) {
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
        this.formulaModifyManager.removeCooldown(player);
    }

    private void handleCreateSetString(String message, Player player, FormulaCreationData creationData) {
        Formula formula = new Formula(message);

        if (!formula.isValid()) {
            this.messageSender.send(player, Message.FORMULA_INPUT_INVALID);
            return;
        }

        this.formulaModifyManager.addCreationData(player,
                creationData.worldName(),
                creationData.formulaOrder(),
                formula
        );

        this.messageSender.send(player, Message.FORMULA_VALUE_SET,
                "%formula%", formula.getRawFormulaString());
        this.formulaModifyManager.removeCooldown(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                FormulasChatListener.this.formulaGUIManager.openFormulaAddGUI(player,
                        FormulasChatListener.this.formulaModifyManager.getCreationData(player));
            }
        }.runTask(this.plugin);
    }

    private void handleCreateSetOrder(String message, Player player, List<Formula> formulas, FormulaCreationData creationData) {
        int newOrder;
        try {
            newOrder = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            this.messageSender.send(player, Message.FORMULA_INVALID_ORDER,
                    "%min%", String.valueOf(1),
                    "%max%", String.valueOf(formulas.size() + 1));
            return;
        }

        if (newOrder < 1 || newOrder > formulas.size() + 1) {
            this.messageSender.send(player, Message.FORMULA_INVALID_ORDER,
                    "%min%", String.valueOf(1),
                    "%max%", String.valueOf(formulas.size() + 1));
            return;
        }

        this.formulaModifyManager.addCreationData(player,
                creationData.worldName(),
                newOrder,
                creationData.formula() == null ? null : creationData.formula()
        );

        this.messageSender.send(player, Message.FORMULA_ORDER_SET,
                "%order%", String.valueOf(newOrder));
        this.formulaModifyManager.removeCooldown(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                FormulasChatListener.this.formulaGUIManager.openFormulaAddGUI(player,
                        FormulasChatListener.this.formulaModifyManager.getCreationData(player));
            }
        }.runTask(this.plugin);
    }

}
