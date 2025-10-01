package com.github.alfonsoleandro.healthpower.managers.health.formula.gui;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.formula.Formula;
import com.github.alfonsoleandro.healthpower.managers.health.formula.FormulaManager;
import com.github.alfonsoleandro.healthpower.managers.health.formula.cooldown.FormulaCreationData;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.guis.DynamicGUI;
import com.github.alfonsoleandro.mputils.guis.SimpleGUI;
import com.github.alfonsoleandro.mputils.itemstacks.MPItemStacks;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import com.github.alfonsoleandro.mputils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class FormulaGUIManager {

    private final MessageSender<Message> messageSender;
    private final Settings settings;
    private final FormulaManager formulaManager;
    private final Map<Player, FormulaGUIData> formulaGUIInfo = new HashMap<>();

    public FormulaGUIManager(HealthPower plugin) {
        this.messageSender = plugin.getMessageSender();
        this.settings = plugin.getSettings();
        this.formulaManager = plugin.getFormulaManager();
    }

    public void openFormulasGUI(Player player) {
        DynamicGUI gui = new DynamicGUI(StringUtils.colorizeString(this.settings.getFormulasWorldsTitle()),
                Settings.FORMULAS_PER_WORLD_GUI_TAG,
                false,
                this.settings.getNavigationBar());
        NamespacedKey worldNameNamespacedKey = this.settings.getWorldNameNamespacedKey();
        Set<String> worldNames = this.formulaManager.getFormulaWorldsNames();
        // Add the worlds that do not have formulas
        worldNames.addAll(Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toSet()));

        // Add "GLOBAL" option for global formulas
        ItemStack formulasGlobalItem = this.settings.getFormulasGlobalItem();
        int globalFormulasCount = this.formulaManager.getFormulas(Settings.GLOBAL_WORLD_SYMBOL).size();
        MPItemStacks.replacePlaceholders(formulasGlobalItem, new HashMap<>() {{
            put("%formulas%", String.valueOf(globalFormulasCount));
        }});
        gui.addItem(formulasGlobalItem);

        for (String worldName : worldNames) {
            ItemStack formulaWorldItem = this.settings.getFormulasWorldsItem();
            int formulasCount = this.formulaManager.getFormulas(worldName).size();
            MPItemStacks.replacePlaceholders(formulaWorldItem, new HashMap<>() {{
                put("%world%", worldName);
                put("%formulas%", String.valueOf(formulasCount));
            }});
            ItemMeta itemMeta = formulaWorldItem.getItemMeta();
            PersistentDataContainer persistentDataContainer = Objects.requireNonNull(itemMeta).getPersistentDataContainer();
            persistentDataContainer.set(worldNameNamespacedKey, PersistentDataType.STRING, worldName);
            formulaWorldItem.setItemMeta(itemMeta);

            gui.addItem(formulaWorldItem);
        }

        gui.openGUI(player);
        this.formulaGUIInfo.put(player, new FormulaGUIData(FormulaGUIMenu.FORMULAS, null, null, null));
    }

    public void openFormulasGUIForWorld(Player player, String worldName) {
        boolean isGlobal = worldName.equals(Settings.GLOBAL_WORLD_SYMBOL);

        DynamicGUI gui = new DynamicGUI(StringUtils.colorizeString(
                this.settings.getFormulasForWorldTitle().replace("%world%",
                        isGlobal ? this.messageSender.getString(Message.FORMULA_LIST_GLOBAL) : worldName)),
                Settings.FORMULAS_FOR_WORLD_GUI_TAG_PREFIX + worldName,
                false,
                this.settings.getNavigationBar());
        NamespacedKey formulaOrderNamespacedKey = this.settings.getFormulaOrderNamespacedKey();
        List<Formula> formulas = this.formulaManager.getFormulas(worldName);

        for (int i = 0; i < formulas.size(); i++) {
            Formula formula = formulas.get(i);
            ItemStack formulaWorldItem = this.settings.getFormulasForWorldItem();
            int order = i + 1;
            MPItemStacks.replacePlaceholders(formulaWorldItem, new HashMap<>() {{
                put("%world%", isGlobal ?
                        FormulaGUIManager.this.messageSender.getString(Message.FORMULA_LIST_GLOBAL_WORLD_NAME)
                        : worldName);
                put("%order%", String.valueOf(order));
                put("%formula%", formula.getRawFormulaString());
            }});
            ItemMeta itemMeta = formulaWorldItem.getItemMeta();
            PersistentDataContainer persistentDataContainer = Objects.requireNonNull(itemMeta).getPersistentDataContainer();
            persistentDataContainer.set(formulaOrderNamespacedKey, PersistentDataType.INTEGER, i + 1);
            formulaWorldItem.setItemMeta(itemMeta);

            gui.addItem(formulaWorldItem);
        }

        //Add "add formula" item
        ItemStack formulaAddItem = this.settings.getFormulaAddItem();
        MPItemStacks.replacePlaceholders(formulaAddItem, new HashMap<>() {{
            put("%world%", isGlobal ?
                    FormulaGUIManager.this.messageSender.getString(Message.FORMULA_LIST_GLOBAL_WORLD_NAME)
                    : worldName);
        }});

        gui.addItem(formulaAddItem);

        gui.openGUI(player);
        this.formulaGUIInfo.put(player, new FormulaGUIData(FormulaGUIMenu.FORMULAS_FOR_WORLD, worldName, null, null));
    }

    public void openFormulaAddGUI(Player player, FormulaCreationData formulaCreationData) {
        openFormulaAddGUI(player,
                formulaCreationData.worldName(),
                formulaCreationData.formula() == null ? null : formulaCreationData.formula().getRawFormulaString(),
                formulaCreationData.formulaOrder());
    }

    public void openFormulaAddGUI(Player player, String worldName, String formulaRawString, int formulaOrder) {
        boolean isGlobal = worldName.equals(Settings.GLOBAL_WORLD_SYMBOL);

        SimpleGUI gui = new SimpleGUI(StringUtils.colorizeString(
                this.settings.getAddFormulaTitle().replace("%world%",
                        (isGlobal ? this.messageSender.getString(Message.FORMULA_LIST_GLOBAL) : worldName))),
                9,
                Settings.FORMULAS_ADD_GUI_TAG_PREFIX + worldName);

        ItemStack formulaStringItem;
        if (formulaRawString == null) {
            formulaStringItem = this.settings.getFormulaAddStringWithoutValueItem();
        } else {
            formulaStringItem = this.settings.getFormulaAddStringWithValueItem();
            MPItemStacks.replacePlaceholders(formulaStringItem, new HashMap<>() {{
                put("%formula%", formulaRawString);
            }});
        }
        ItemStack formulaOrderItem = this.settings.getFormulaAddOrderItem();
        MPItemStacks.replacePlaceholders(formulaOrderItem, new HashMap<>() {{
            put("%order%", String.valueOf(formulaOrder));
        }});

        ItemStack formulaSaveItem;
        if (formulaRawString == null) {
            formulaSaveItem = this.settings.getFormulaAddSaveIncorrectItem();
        } else {
            formulaSaveItem = this.settings.getFormulaAddSaveCorrectItem();
        }

        gui.setItem(0, formulaStringItem);
        gui.setItem(1, formulaOrderItem);
        gui.setItem(8, formulaSaveItem);

        gui.openGUI(player);
        this.formulaGUIInfo.put(player, new FormulaGUIData(FormulaGUIMenu.CREATE, worldName, formulaRawString, formulaOrder));

    }

    public void openLastGUI(Player player) {
        if (this.formulaGUIInfo.containsKey(player)) {
            FormulaGUIData formulaGUIData = this.formulaGUIInfo.get(player);
            switch (formulaGUIData.guiMenu()) {
                case FORMULAS -> openFormulasGUI(player);
                case FORMULAS_FOR_WORLD -> openFormulasGUIForWorld(player, formulaGUIData.worldName());
                case CREATE -> openFormulaAddGUI(player, formulaGUIData.worldName(), formulaGUIData.formulaRawString(), formulaGUIData.order());
            }
        }
    }

    public void clearGUIInfo(Player player) {
        this.formulaGUIInfo.remove(player);
    }

}
