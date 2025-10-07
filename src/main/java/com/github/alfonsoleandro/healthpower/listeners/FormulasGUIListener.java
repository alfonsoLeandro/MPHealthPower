package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.formula.cooldown.FormulaCreationData;
import com.github.alfonsoleandro.healthpower.managers.health.formula.cooldown.FormulaGUIAction;
import com.github.alfonsoleandro.healthpower.managers.health.formula.cooldown.FormulaModifyManager;
import com.github.alfonsoleandro.healthpower.managers.health.formula.Formula;
import com.github.alfonsoleandro.healthpower.managers.health.formula.gui.FormulaGUIManager;
import com.github.alfonsoleandro.healthpower.managers.health.formula.FormulaManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.guis.events.GUIClickEvent;
import com.github.alfonsoleandro.mputils.guis.events.GUICloseEvent;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;

public class FormulasGUIListener implements Listener {

    private final HealthPower plugin;
    private final Settings settings;
    private final MessageSender<Message> messageSender;
    private final FormulaManager formulaManager;
    private final FormulaGUIManager formulaGUIManager;
    private final FormulaModifyManager formulaModifyManager;

    public FormulasGUIListener(HealthPower plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();
        this.messageSender = plugin.getMessageSender();
        this.formulaManager = plugin.getFormulaManager();
        this.formulaGUIManager = plugin.getFormulaGUIManager();
        this.formulaModifyManager = plugin.getFormulaModifyManager();
    }

    @EventHandler
    public void onGUIClick(GUIClickEvent event) {
        // check the cause of the event is a GUI from this plugin, specifically a formulas GUI click
        String guiTags = event.getGui().getGuiTags();
        int rawSlot = event.getRawSlot();
        if (!guiTags.startsWith(Settings.FORMULAS_GUI_TAG_PREFIX)
                || event.isButtonClick()
                || rawSlot >= event.getGui().getSize()) {
            return;
        }
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        if (guiTags.equals(Settings.FORMULAS_PER_WORLD_GUI_TAG)) {
            handleFormulasPerWorldGUI(event, player);

        } else if (guiTags.startsWith(Settings.FORMULAS_FOR_WORLD_GUI_TAG_PREFIX)) {
            handleFormulaInWorldGUI(event, player, guiTags);

        } else if (guiTags.startsWith(Settings.FORMULAS_ADD_GUI_TAG_PREFIX)) {
            handleFormulaAddGUI(player, rawSlot);
        }
    }

    @EventHandler
    public void onGUIClose(GUICloseEvent event) {
        // check the cause of the event is a GUI from this plugin, specifically a formulas GUI click
        String guiTags = event.getGui().getGuiTags();
        if (!guiTags.startsWith(Settings.FORMULAS_GUI_TAG_PREFIX)) {
            return;
        }
        // Clear creation data in case the player closed the GUI intentionally
        Player player = (Player) event.getPlayer();
        if (this.formulaModifyManager.isNotInCooldown(player)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.getOpenInventory().getType().equals(InventoryType.CRAFTING)) {
                        FormulasGUIListener.this.formulaModifyManager.clearCreationData(player);
                        FormulasGUIListener.this.formulaGUIManager.clearGUIInfo(player);
                    }
                }
            }.runTask(this.plugin);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        this.formulaModifyManager.clearCreationData(event.getPlayer());
        this.formulaGUIManager.clearGUIInfo(event.getPlayer());
    }


    private void handleFormulasPerWorldGUI(GUIClickEvent event, Player player) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }
        PersistentDataContainer persistentDataContainer =
                Objects.requireNonNull(clickedItem.getItemMeta()).getPersistentDataContainer();
        String itemType = persistentDataContainer.get(this.settings.getFormulaItemTypeNamespacedKey(), PersistentDataType.STRING);

        String worldName;
        if (Objects.equals(itemType, "GLOBAL")) {
            // Use as null to mean GLOBAL
            worldName = Settings.GLOBAL_WORLD_SYMBOL;
        } else {
            worldName = persistentDataContainer.get(this.settings.getWorldNameNamespacedKey(), PersistentDataType.STRING);
        }

        this.formulaGUIManager.openFormulasGUIForWorld(player, Objects.requireNonNull(worldName));
    }

    private void handleFormulaInWorldGUI(GUIClickEvent event, Player player, String guiTags) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !event.getClick().isMouseClick()) {
            return;
        }
        if (!player.hasPermission("HealthPower.formulas.edit")) {
            this.messageSender.send(player, Message.NO_PERMISSION);
            return;
        }
        PersistentDataContainer persistentDataContainer = Objects.requireNonNull(clickedItem.getItemMeta()).getPersistentDataContainer();
        String itemType = persistentDataContainer.get(this.settings.getFormulaItemTypeNamespacedKey(), PersistentDataType.STRING);
        String worldName = guiTags.replace(Settings.FORMULAS_FOR_WORLD_GUI_TAG_PREFIX, "");
        String worldNameForMessages = worldName.equals(Settings.GLOBAL_WORLD_SYMBOL) ?
                this.messageSender.getString(Message.FORMULA_LIST_GLOBAL_WORLD_NAME) : worldName;

        if (Objects.equals(itemType, "ADD")) {
            int defaultOrder = this.formulaManager.getFormulas(worldName).size() + 1;
            this.formulaGUIManager.openFormulaAddGUI(player, worldName, null, defaultOrder);

            this.formulaModifyManager.addCreationData(player, worldName, defaultOrder, null);
            return;
        }

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
            this.messageSender.send(player, Message.FORMULA_CONFIRM_DELETE,
                    "%world%", worldNameForMessages,
                    "%formula%", formula.getRawFormulaString(),
                    "%seconds%", String.valueOf(this.settings.getFormulaEditCooldownTime()));

            player.closeInventory();

            this.formulaModifyManager.startCooldown(player, worldName, order, FormulaGUIAction.DELETE, this.settings.getFormulaDeleteCooldownTime());

        } else if (event.getClick().isLeftClick()) {
            this.messageSender.send(player, Message.FORMULA_ENTER_NEW_ORDER,
                    "%world%", worldNameForMessages,
                    "%formula%", formula.getRawFormulaString(),
                    "%seconds%", String.valueOf(this.settings.getFormulaEditCooldownTime()));

            player.closeInventory();

            this.formulaModifyManager.startCooldown(player, worldName, order, FormulaGUIAction.EDIT, this.settings.getFormulaEditCooldownTime());
        }
    }

    private void handleFormulaAddGUI(Player player, int rawSlot) {
        // get creation data
        FormulaCreationData creationData = this.formulaModifyManager.getCreationData(player);
        String worldName = creationData.worldName();
        String worldNameForMessages = worldName.equals(Settings.GLOBAL_WORLD_SYMBOL) ?
                this.messageSender.getString(Message.FORMULA_LIST_GLOBAL_WORLD_NAME) : worldName;
        int formulaOrder = creationData.formulaOrder();

        if (rawSlot == 0) {
            this.messageSender.send(player, Message.FORMULA_ADD_ENTER_NEW_STRING,
                    "%seconds%", String.valueOf(this.settings.getFormulaCreateCooldownTime()),
                    "%world%", worldNameForMessages);
            this.formulaModifyManager.startCooldown(player, worldName, formulaOrder, FormulaGUIAction.CREATE_SET_STRING, this.settings.getFormulaCreateCooldownTime());
            player.closeInventory();

        } else if (rawSlot == 1) {
            this.messageSender.send(player, Message.FORMULA_ADD_ENTER_NEW_ORDER,
                    "%seconds%", String.valueOf(this.settings.getFormulaCreateCooldownTime()),
                    "%world%", worldNameForMessages);
            this.formulaModifyManager.startCooldown(player, worldName, formulaOrder, FormulaGUIAction.CREATE_SET_ORDER, this.settings.getFormulaCreateCooldownTime());
            player.closeInventory();

        } else if (rawSlot == 8) {
            Formula formula = creationData.formula();
            if (formula != null && formula.isValid()) {
                this.formulaManager.saveNewFormula(worldName, formula, formulaOrder);
                this.formulaModifyManager.clearCreationData(player);

                this.messageSender.send(player, Message.FORMULA_SAVED,
                        "%formula%", formula.getRawFormulaString(),
                        "%world%", worldNameForMessages,
                        "%order%",  String.valueOf(formulaOrder));
                player.closeInventory();
            } else {
                this.messageSender.send(player, Message.FORMULA_CANNOT_SAVE);
            }

        }
    }

}
