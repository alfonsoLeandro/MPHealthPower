package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import com.github.alfonsoleandro.healthpower.managers.health.formula.Formula;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.guis.DynamicGUI;
import com.github.alfonsoleandro.mputils.guis.events.GUIClickEvent;
import com.github.alfonsoleandro.mputils.itemstacks.MPItemStacks;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import com.github.alfonsoleandro.mputils.string.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FormulasGUIListener implements Listener {

    private enum GUIAction {
        CREATE,
        DELETE,
        EDIT
    }

    private record FormulaClickedData(String worldName, int formulaOrder, GUIAction action) {
    }

    private final HealthPower plugin;
    private final Settings settings;
    private final MessageSender<Message> messageSender;
    private final HPManager hpManager;
    private final Map<Player, FormulaClickedData> cooldowns = new HashMap<>();

    public FormulasGUIListener(HealthPower plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();
        this.messageSender = this.plugin.getMessageSender();
        this.hpManager = plugin.getHpManager();
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

            DynamicGUI gui = createFormulasGUIForWorld(worldName);
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
            List<Formula> formulas = this.hpManager.getFormulas(worldName);
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

                startCooldown(player, worldName, order, GUIAction.DELETE);

            } else if (event.getClick().isLeftClick()) {
                this.messageSender.send(event.getWhoClicked(), Message.FORMULA_ENTER_NEW_ORDER,
                        "%world%", worldName,
                        "%formula%", formula.getRawFormulaString());

                player.closeInventory();

                startCooldown(player, worldName, order, GUIAction.EDIT);

            }

        }

    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!this.cooldowns.containsKey(player)) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage();

        if (message.equalsIgnoreCase("cancel")) {
            this.cooldowns.remove(player);
            this.messageSender.send(player, Message.FORMULA_ACTION_CANCELED);
            return;
        }

        if (!player.hasPermission("HealthPower.formulas.edit")) {
            this.messageSender.send(player, Message.NO_PERMISSION);
            return;
        }

        FormulaClickedData formulaClickedData = this.cooldowns.get(player);
        List<Formula> formulas = this.hpManager.getFormulas(formulaClickedData.worldName);

        if (formulaClickedData.action.equals(GUIAction.DELETE)) {
            if (!message.equalsIgnoreCase("yes")) {
                this.messageSender.send(player, Message.FORMULA_DELETE_UNKNOWN_MESSAGE);
                return;
            }

            //CONFIRM DELETE, DELETE FORMULA

            if (formulas == null || formulas.size() <= formulaClickedData.formulaOrder) {
                this.messageSender.send(player, Message.FORMULA_DELETE_ERROR);
                return;
            }
            Formula formula = this.hpManager.deleteFormula(formulaClickedData.worldName, formulaClickedData.formulaOrder);
            this.messageSender.send(player, Message.FORMULA_DELETED,
                    "%world%", formulaClickedData.worldName,
                    "%formula%", formula.getRawFormulaString());
            this.cooldowns.remove(player);

        } else if (formulaClickedData.action.equals(GUIAction.EDIT)) {
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
            if (formulaClickedData.formulaOrder != newOrder) {
                this.hpManager.changeFormulaOrder(formulaClickedData.worldName, formulaClickedData.formulaOrder, newOrder);
            }
            this.messageSender.send(player, Message.FORMULA_ORDER_CHANGED,
                    "%order%", String.valueOf(newOrder));
            this.cooldowns.remove(player);

            //Re-open GUI
            DynamicGUI gui = createFormulasGUIForWorld(formulaClickedData.worldName);
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

    private DynamicGUI createFormulasGUIForWorld(String worldName) {
        DynamicGUI gui = new DynamicGUI(StringUtils.colorizeString(this.settings.getFormulasForWorldTitle().replace("%world%", worldName)),
                "MPHealthPower:formulas:items:" + worldName,
                false,
                this.settings.getNavigationBar());
        NamespacedKey formulaOrderNamespacedKey = this.settings.getFormulaOrderNamespacedKey();
        List<Formula> formulas = this.hpManager.getFormulas(worldName);

        for (int i = 0; i < formulas.size(); i++) {
            Formula formula = formulas.get(i);
            ItemStack formulaWorldItem = this.settings.getFormulasForWorldItem();
            int order = i + 1;
            MPItemStacks.replacePlaceholders(formulaWorldItem, new HashMap<>() {{
                put("%world%", worldName);
                put("%order%", String.valueOf(order));
                put("%formula%", formula.getRawFormulaString());
            }});
            ItemMeta itemMeta = formulaWorldItem.getItemMeta();
            PersistentDataContainer persistentDataContainer = Objects.requireNonNull(itemMeta).getPersistentDataContainer();
            persistentDataContainer.set(formulaOrderNamespacedKey, PersistentDataType.INTEGER, i + 1);
            formulaWorldItem.setItemMeta(itemMeta);

            gui.addItem(formulaWorldItem);
        }

        return gui;
    }

    private void startCooldown(Player player, String worldName, int formulaOrder, GUIAction action) {
        this.cooldowns.put(player, new FormulaClickedData(worldName, formulaOrder, action));
        new BukkitRunnable() {

            @Override
            public void run() {
                if (FormulasGUIListener.this.cooldowns.containsKey(player)) {
                    FormulasGUIListener.this.cooldowns.remove(player);
                    FormulasGUIListener.this.messageSender.send(player, Message.FORMULA_ACTION_TIMER_RAN_OUT);
                }
            }
        }.runTaskLater(this.plugin, 200);

    }


}
