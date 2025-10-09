package com.github.alfonsoleandro.healthpower.utils;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.mputils.guis.navigation.GUIButton;
import com.github.alfonsoleandro.mputils.guis.navigation.NavigationBar;
import com.github.alfonsoleandro.mputils.itemstacks.MPItemStacks;
import com.github.alfonsoleandro.mputils.reloadable.Reloadable;
import com.github.alfonsoleandro.mputils.time.TimeUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class Settings extends Reloadable {

    // Constants
    public static final String TAG_PREFIX = "MPHealthPower";
    // GUI TAGS
    public static final String SHOP_GUI_TAG = TAG_PREFIX + ":SHOP";
    public static final String FORMULAS_GUI_TAG_PREFIX = TAG_PREFIX + ":formulas";
    public static final String FORMULAS_PER_WORLD_GUI_TAG = FORMULAS_GUI_TAG_PREFIX + ":worlds";
    public static final String FORMULAS_FOR_WORLD_GUI_TAG_PREFIX = FORMULAS_GUI_TAG_PREFIX + ":items:";
    public static final String FORMULAS_ADD_GUI_TAG_PREFIX = FORMULAS_GUI_TAG_PREFIX + ":add:";
    // BUTTON TAGS
    public static final String PREVIOUS_PAGE_BUTTON_TAG = TAG_PREFIX + ":previous_page";
    public static final String NEXT_PAGE_BUTTON_TAG = TAG_PREFIX + ":next_page";
    public static final String MIDDLE_BUTTON_TAG = TAG_PREFIX + ":middle";
    public static final String EMPTY_SLOTS_BUTTON_TAG = TAG_PREFIX + ":empty";
    // GLOBAL WORLD SYMBOL
    public static final String GLOBAL_WORLD_SYMBOL = "*";


    private final HealthPower plugin;
    // Fields
    private boolean checkHPOnJoin;
    private boolean consumablesEnabled;
    private boolean debug;
    private boolean periodicCheckerEnabled;
    private long periodicCheckerPeriod;
    private boolean notifyHPCheck;
    private boolean shopGUIEnabled;
    private boolean updateHPOnJoin;
    private boolean useGroupsSystem;
    private boolean usePermissionsSystem;
    private long formulaCreateCooldownTime;
    private long formulaDeleteCooldownTime;
    private long formulaEditCooldownTime;
    private String addFormulaTitle;
    private String formulasForWorldTitle;
    private String formulasWorldsTitle;
    private ItemStack formulaAddItem;
    private ItemStack formulaAddOrderItem;
    private ItemStack formulaAddSaveCorrectItem;
    private ItemStack formulaAddSaveIncorrectItem;
    private ItemStack formulaAddStringWithValueItem;
    private ItemStack formulaAddStringWithoutValueItem;
    private ItemStack formulasForWorldItem;
    private ItemStack formulasGlobalItem;
    private ItemStack formulasWorldsItem;
    private NavigationBar navigationBar;
    private final NamespacedKey worldNameNamespacedKey;
    private final NamespacedKey formulaItemTypeNamespacedKey;
    private final NamespacedKey formulaOrderNamespacedKey;


    public Settings(HealthPower plugin) {
        super(plugin, Priority.HIGHEST);
        this.worldNameNamespacedKey = new NamespacedKey(plugin, "worldName");
        this.formulaItemTypeNamespacedKey = new NamespacedKey(plugin, "formulaItemType");
        this.formulaOrderNamespacedKey = new NamespacedKey(plugin, "formulaOrder");
        this.plugin = plugin;
        loadFields();
    }

    private void loadFields() {
        FileConfiguration config = this.plugin.getConfigYaml().getAccess();
        FileConfiguration gui = this.plugin.getGuiYaml().getAccess();

        this.checkHPOnJoin = config.getBoolean("config.check HP on join");
        this.consumablesEnabled = config.getBoolean("config.consumables enabled");
        this.debug = config.getBoolean("config.debug");
        this.periodicCheckerEnabled = config.getBoolean("config.periodic checker.enabled");
        String timeString = config.getString("config.periodic checker.period");
        this.periodicCheckerPeriod = TimeUtils.getTicks(timeString != null ? timeString : "5m");
        this.notifyHPCheck = config.getBoolean("config.notify HP check");
        this.shopGUIEnabled = gui.getBoolean("GUI.shop.enabled");
        this.updateHPOnJoin = config.getBoolean("config.update HP on join");
        this.useGroupsSystem = config.getBoolean("config.use groups system");
        this.usePermissionsSystem = config.getBoolean("config.use permissions system");

        this.formulaCreateCooldownTime = config.getLong("config.formulas admin cooldown.create");
        this.formulaDeleteCooldownTime = config.getLong("config.formulas admin cooldown.delete");
        this.formulaEditCooldownTime = config.getLong("config.formulas admin cooldown.edit");

        this.addFormulaTitle = gui.getString("GUI.add formula.title");
        this.formulasForWorldTitle = gui.getString("GUI.formulas for world.title");
        this.formulasWorldsTitle = gui.getString("GUI.formulas worlds.title");

        this.formulaAddItem = getGUIItem(gui, "formulas for world.add formula item");
        this.formulasForWorldItem = getGUIItem(gui, "formulas for world.item");
        this.formulasGlobalItem = getGUIItem(gui,"formulas worlds.global");
        this.formulasWorldsItem = getGUIItem(gui, "formulas worlds.item");
        setItemPersistentData(this.formulaAddItem, this.formulaItemTypeNamespacedKey, "ADD");
        setItemPersistentData(this.formulasForWorldItem, this.formulaItemTypeNamespacedKey, "ITEM");
        setItemPersistentData(this.formulasGlobalItem, this.formulaItemTypeNamespacedKey, "GLOBAL");
        setItemPersistentData(this.formulasWorldsItem, this.formulaItemTypeNamespacedKey, "ITEM");
        this.formulaAddOrderItem = getGUIItem(gui, "add formula.formula order");
        this.formulaAddSaveCorrectItem = getGUIItem(gui, "add formula.save.correct");
        this.formulaAddSaveIncorrectItem = getGUIItem(gui, "add formula.save.incorrect");
        this.formulaAddStringWithValueItem = getGUIItem(gui, "add formula.formula string.with value");
        this.formulaAddStringWithoutValueItem = getGUIItem(gui, "add formula.formula string.without value");

        this.navigationBar = new NavigationBar();

        ItemStack previousPageItem = getGUIItem(gui,"navigation bar.previous page");
        ItemStack nextPageItem = getGUIItem(gui, "navigation bar.next page");
        ItemStack middleItem = getGUIItem(gui, "navigation bar.middle");
        ItemStack emptySlotsItem = getGUIItem(gui, "navigation bar.empty slots");

        GUIButton previousPageButton = new GUIButton(PREVIOUS_PAGE_BUTTON_TAG, previousPageItem, GUIButton.GUIButtonCondition.HAS_PREVIOUS_PAGE, emptySlotsItem);
        GUIButton nextPageButton = new GUIButton(NEXT_PAGE_BUTTON_TAG, nextPageItem, GUIButton.GUIButtonCondition.HAS_NEXT_PAGE, emptySlotsItem);
        GUIButton middleButton = new GUIButton(MIDDLE_BUTTON_TAG, middleItem, GUIButton.GUIButtonCondition.ALWAYS, null);
        GUIButton emptySlotsButton = new GUIButton(EMPTY_SLOTS_BUTTON_TAG, emptySlotsItem, GUIButton.GUIButtonCondition.ALWAYS, null);

        this.navigationBar.setButtonAt(0, previousPageButton);
        this.navigationBar.setButtonAt(4, middleButton);
        this.navigationBar.setButtonAt(8, nextPageButton);

        for (int i : new int[]{1,2,3,5,6,7}) {
            this.navigationBar.setButtonAt(i, emptySlotsButton);
        }
    }

    private ItemStack getGUIItem(FileConfiguration fileConfiguration, String path) {
        return MPItemStacks.newItemStack(
                Material.valueOf(fileConfiguration.getString("GUI."+path+".material")),
                1,
                fileConfiguration.getString("GUI."+path+".name"),
                fileConfiguration.getStringList("GUI."+path+".lore")
        );
    }

    private void setItemPersistentData(ItemStack itemStack, NamespacedKey namespacedKey, String data) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer persistentDataContainer = Objects.requireNonNull(itemMeta).getPersistentDataContainer();
        persistentDataContainer.set(namespacedKey, PersistentDataType.STRING, data);
        itemStack.setItemMeta(itemMeta);
    }


    @Override
    public void reload(boolean deep) {
        this.loadFields();
    }


    public boolean isCheckHPOnJoin() {
        return this.checkHPOnJoin;
    }

    public boolean isConsumablesEnabled() {
        return this.consumablesEnabled;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public boolean isPeriodicCheckerEnabled() {
        return this.periodicCheckerEnabled;
    }

    public long getPeriodicCheckerPeriod() {
        return this.periodicCheckerPeriod;
    }

    public boolean isNotifyHPCheck() {
        return this.notifyHPCheck;
    }

    public boolean isShopGUIEnabled() {
        return this.shopGUIEnabled;
    }

    public boolean isUpdateHPOnJoin() {
        return this.updateHPOnJoin;
    }

    public boolean isUseGroupsSystem() {
        return this.useGroupsSystem;
    }

    public boolean isUsePermissionsSystem() {
        return this.usePermissionsSystem;
    }

    public long getFormulaCreateCooldownTime() {
        return this.formulaCreateCooldownTime;
    }

    public long getFormulaDeleteCooldownTime() {
        return this.formulaDeleteCooldownTime;
    }

    public long getFormulaEditCooldownTime() {
        return this.formulaEditCooldownTime;
    }

    public String getAddFormulaTitle() {
        return this.addFormulaTitle;
    }

    public String getFormulasForWorldTitle() {
        return this.formulasForWorldTitle;
    }

    public String getFormulasWorldsTitle() {
        return this.formulasWorldsTitle;
    }

    public ItemStack getFormulaAddOrderItem() {
        return this.formulaAddOrderItem.clone();
    }

    public ItemStack getFormulaAddSaveCorrectItem() {
        return this.formulaAddSaveCorrectItem.clone();
    }

    public ItemStack getFormulaAddSaveIncorrectItem() {
        return this.formulaAddSaveIncorrectItem.clone();
    }

    public ItemStack getFormulaAddStringWithValueItem() {
        return this.formulaAddStringWithValueItem.clone();
    }

    public ItemStack getFormulaAddStringWithoutValueItem() {
        return this.formulaAddStringWithoutValueItem.clone();
    }

    public ItemStack getFormulaAddItem() {
        return this.formulaAddItem.clone();
    }

    public ItemStack getFormulasForWorldItem() {
        return this.formulasForWorldItem.clone();
    }

    public ItemStack getFormulasGlobalItem() {
        return this.formulasGlobalItem.clone();
    }

    public ItemStack getFormulasWorldsItem() {
        return this.formulasWorldsItem.clone();
    }

    public NavigationBar getNavigationBar() {
        return this.navigationBar;
    }

    public NamespacedKey getWorldNameNamespacedKey() {
        return this.worldNameNamespacedKey;
    }

    public NamespacedKey getFormulaItemTypeNamespacedKey() {
        return this.formulaItemTypeNamespacedKey;
    }

    public NamespacedKey getFormulaOrderNamespacedKey() {
        return this.formulaOrderNamespacedKey;
    }
}