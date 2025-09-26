package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.cooldown.formula.FormulaGUIAction;
import com.github.alfonsoleandro.healthpower.managers.cooldown.formula.FormulaModifyCooldown;
import com.github.alfonsoleandro.healthpower.managers.health.formula.Formula;
import com.github.alfonsoleandro.healthpower.managers.health.formula.FormulaManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.guis.DynamicGUI;
import com.github.alfonsoleandro.mputils.guis.events.GUIClickEvent;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;

public class FormulasGUIListener implements Listener {

    private final Settings settings;
    private final MessageSender<Message> messageSender;
    private final FormulaManager formulaManager;
    private final FormulaModifyCooldown formulaModifyCooldown;

    public FormulasGUIListener(HealthPower plugin) {
        this.settings = plugin.getSettings();
        this.messageSender = plugin.getMessageSender();
        this.formulaManager = plugin.getFormulaManager();
        this.formulaModifyCooldown = plugin.getFormulaModifyCooldown();
    }

    @EventHandler
    public void onGUIClick(GUIClickEvent event) {
        // check the cause of the event is a GUI from this plugin, specifically a formulas GUI click
        String guiTags = event.getGui().getGuiTags();
        if (!guiTags.startsWith("MPHealthPower:formulas")
                || event.isButtonClick()
                || event.getRawSlot() >= event.getGui().getSize()) {
            return;
        }
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        if (guiTags.equals("MPHealthPower:formulas:worlds")) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) {
                return;
            }
            //check if it's world OR NEW item
            PersistentDataContainer persistentDataContainer =
                    Objects.requireNonNull(clickedItem.getItemMeta()).getPersistentDataContainer();
            String worldName = persistentDataContainer.get(this.settings.getWorldNameNamespacedKey(), PersistentDataType.STRING);

            DynamicGUI gui = this.formulaManager.createFormulasGUIForWorld(worldName);
            gui.openGUI(player);

        } else if (guiTags.startsWith("MPHealthPower:formulas:items:")) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !event.getClick().isMouseClick()) {
                return;
            }
            if (!player.hasPermission("HealthPower.formulas.edit")) {
                this.messageSender.send(player, Message.NO_PERMISSION);
                return;
            }
            PersistentDataContainer persistentDataContainer = Objects.requireNonNull(clickedItem.getItemMeta()).getPersistentDataContainer();
            String worldName = guiTags.replace("MPHealthPower:formulas:items:", "");
            Integer order = persistentDataContainer.get(this.settings.getFormulaOrderNamespacedKey(), PersistentDataType.INTEGER);

            if (order == null) {
                return;
            }
            List<Formula> formulas = this.formulaManager.getFormulas(worldName);
            if (formulas == null || formulas.size() < order) {
                return;
            }
            Formula formula = formulas.get(order - 1);

            if (event.getClick().isRightClick()) {
                //CONFIRM DELETE
                this.messageSender.send(event.getWhoClicked(), Message.FORMULA_CONFIRM_DELETE,
                        "%world%", worldName,
                        "%formula%", formula.getRawFormulaString());

                player.closeInventory();

                this.formulaModifyCooldown.startCooldown(player, worldName, order, FormulaGUIAction.DELETE);

            } else if (event.getClick().isLeftClick()) {
                this.messageSender.send(event.getWhoClicked(), Message.FORMULA_ENTER_NEW_ORDER,
                        "%world%", worldName,
                        "%formula%", formula.getRawFormulaString());

                player.closeInventory();

                this.formulaModifyCooldown.startCooldown(player, worldName, order, FormulaGUIAction.EDIT);

            }

        }

    }

}
