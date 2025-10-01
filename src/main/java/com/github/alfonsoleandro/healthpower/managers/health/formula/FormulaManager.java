package com.github.alfonsoleandro.healthpower.managers.health.formula;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.cooldown.formula.FormulaCreationData;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.files.YamlFile;
import com.github.alfonsoleandro.mputils.guis.DynamicGUI;
import com.github.alfonsoleandro.mputils.guis.SimpleGUI;
import com.github.alfonsoleandro.mputils.itemstacks.MPItemStacks;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import com.github.alfonsoleandro.mputils.reloadable.Reloadable;
import com.github.alfonsoleandro.mputils.string.StringUtils;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FormulaManager extends Reloadable {

    private final HealthPower plugin;
    private final MessageSender<Message> messageSender;
    private final YamlFile hpYaml;
    private final Settings settings;
    private Map<String, Double> defaultVariablesPerWorld;
    private Map<String, Double> hpPerGroup;
    private double defaultVariableGlobal;
    private Map<String, List<Formula>> formulasPerWorld;
    private double defaultBaseHp;

    public FormulaManager(HealthPower plugin) {
        super(plugin, Priority.HIGHEST);
        this.plugin = plugin;
        this.messageSender = plugin.getMessageSender();
        this.hpYaml = plugin.getHpYaml();
        this.settings = plugin.getSettings();
        loadSettings();
    }

    protected void loadSettings() {
        FileConfiguration config = this.plugin.getConfigYaml().getAccess();
        // Load per group HP
        this.hpPerGroup = new HashMap<>();
        if (this.settings.isUseGroupsSystem()) {
            ConfigurationSection groupsHp = this.hpYaml.getAccess().getConfigurationSection("HP.groups");
            if (groupsHp != null) {
                groupsHp.getKeys(false).forEach(key ->
                        this.hpPerGroup.put(key, groupsHp.getDouble(key))
                );
            }
        }
        this.defaultBaseHp = config.getDouble("config.default base HP");

        loadFormulasAndCases();
    }

    protected void loadFormulasAndCases() {
        FileConfiguration formulas = this.plugin.getFormulasYaml().getAccess();

        // load defaults per world
        this.defaultVariablesPerWorld = new HashMap<>();
        ConfigurationSection defaultsSection = formulas.getConfigurationSection("default if not present");
        if (defaultsSection != null) {
            for (String worldName : defaultsSection.getKeys(false)) {
                this.defaultVariablesPerWorld.put(worldName, defaultsSection.getDouble(worldName));
            }
        }

        // load default for all non specified worlds
        this.defaultVariableGlobal = formulas.getDouble("default for all worlds");

        // Load global formulas for all non specified worlds
        List<String> globalFormulasStrings = formulas.getStringList("global formulas");
        List<Formula> globalFormulas = globalFormulasStrings
                .stream()
                .map(Formula::new)
                .filter(f -> {
                    if (f.isValid()) {
                        if (this.settings.isDebug()) {
                            this.messageSender.send(Bukkit.getConsoleSender(),
                                    Message.FORMULA_VALID,
                                    "%formula%", f.getRawFormulaString(),
                                    "%world%", this.messageSender.getString(Message.FORMULA_LIST_GLOBAL_WORLD_NAME));
                        }
                        return true;
                    }
                    this.messageSender.send(Bukkit.getConsoleSender(),
                            Message.FORMULA_INVALID,
                            "%formula%", f.getRawFormulaString(),
                            "%world%", this.messageSender.getString(Message.FORMULA_LIST_GLOBAL_WORLD_NAME));
                    return false;
                }).collect(Collectors.toCollection(ArrayList::new));
        if (globalFormulas.isEmpty()) {
            throw new RuntimeException("Global formulas list has no valid formulas, this is essential for the plugin to work, please fix your formulas file.");
        }

        //Load formulas per world
        this.formulasPerWorld = new HashMap<>();
        this.formulasPerWorld.put(Settings.GLOBAL_WORLD_SYMBOL, globalFormulas);
        ConfigurationSection formulasPerWorld = formulas.getConfigurationSection("formulas per world");
        if (formulasPerWorld != null) {
            for (String worldName : formulasPerWorld.getKeys(false)) {
                List<Formula> formulasList = formulasPerWorld.getStringList(worldName)
                        .stream()
                        .map(Formula::new)
                        .filter(f -> {
                            if (f.isValid()) {
                                if (this.settings.isDebug()) {
                                    this.messageSender.send(Bukkit.getConsoleSender(),
                                            Message.FORMULA_VALID,
                                            "%formula%", f.getRawFormulaString(),
                                            "%world%", worldName);
                                }
                                return true;
                            }
                            this.messageSender.send(Bukkit.getConsoleSender(),
                                    Message.FORMULA_INVALID,
                                    "%formula%", f.getRawFormulaString(),
                                    "%world%", worldName);
                            return false;
                        })
                        .collect(Collectors.toCollection(ArrayList::new));
                if (!formulasList.isEmpty()) {
                    this.formulasPerWorld.put(worldName, formulasList);
                }
            }
        }
    }

    public Double getPlayerHpVariable(Player player, String worldName, FormulaVariable variable) {
        switch (variable) {
            case BASE -> {
                // Get from HP.yaml
                FileConfiguration hpYaml = this.hpYaml.getAccess();
                ConfigurationSection playersSection = hpYaml.getConfigurationSection("HP.players");
                if (playersSection != null && playersSection.contains(player.getName())) {
                    return hpYaml.getDouble("HP.players." + player.getName() + ".base");
                }
                // return and save default
                hpYaml.set("HP.players." + player.getName() + ".base", this.defaultBaseHp);
                this.hpYaml.save(false);
                return this.defaultBaseHp;
            }
            case GROUP -> {
                if (!this.settings.isUseGroupsSystem() && this.plugin.getPermissions() != null && this.plugin.getPermissions().hasGroupSupport()) {
                    return this.defaultVariablesPerWorld.getOrDefault(worldName, this.defaultVariableGlobal);
                }
                // get from HP.yaml
                Permission perms = this.plugin.getPermissions();
                String groupName = perms.getPrimaryGroup(player);
                if (this.hpPerGroup.containsKey(groupName)) {
                    return this.hpPerGroup.get(groupName);
                }
                return null;
            }
            case PERMISSION -> {
                if (!this.settings.isUsePermissionsSystem()) {
                    return this.defaultVariablesPerWorld.getOrDefault(worldName, this.defaultVariableGlobal);
                }
                Double value = null;
                // Any healthpower.amount.X where X is a whole number
                Pattern pattern = Pattern.compile("healthpower\\.amount\\.[0-9]+$");

                //Check for every permission if a permission is similar to an amount permission
                for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
                    Matcher matcher = pattern.matcher(perm.getPermission());
                    if (matcher.matches()) {
                        if (this.settings.isDebug()) {
                            this.messageSender.send("&cDEBUG: &fFound permission \"&c" + perm.getPermission() + "&f\" for player " + player.getName());
                        }
                        double newValue = Double.parseDouble(matcher.group());
                        if (value != null) {
                            this.messageSender.send("&cDEBUG: &fPlayer &c" + player.getName() + " &fhas more than one HP amount permission set. The highest value will be used.");
                        }
                        if (value == null || value < newValue) {
                            value = newValue;
                        }
                    }
                }
                return value;
            }
            // case SHOP
            default -> {
                // Get from HP.yaml
                FileConfiguration hpYaml = this.hpYaml.getAccess();
                ConfigurationSection playersSection = hpYaml.getConfigurationSection("HP.players");
                if (playersSection != null && playersSection.contains(player.getName() + ".shop")) {
                    return playersSection.getDouble(player.getName() + ".shop");
                }
                return null;
            }

        }
    }

    public PlayerHpData getPlayerHpData(Player player, String worldName) {
        Double baseHp = getPlayerHpVariable(player, worldName, FormulaVariable.BASE);
        Double groupHp = getPlayerHpVariable(player, worldName, FormulaVariable.GROUP);
        Double permissionHp = getPlayerHpVariable(player, worldName, FormulaVariable.PERMISSION);
        Double shopHp = getPlayerHpVariable(player, worldName, FormulaVariable.SHOP);

        return new PlayerHpData(baseHp, groupHp, permissionHp, shopHp);
    }

    public Formula getApplicableFormula(Player player, String worldName) {
        PlayerHpData playerHpData = getPlayerHpData(player, worldName);

        if (this.formulasPerWorld.containsKey(worldName)) {
            List<Formula> worldFormulas = this.formulasPerWorld.get(worldName);
            for (Formula formula : worldFormulas) {
                if (formula.canApply(playerHpData)) {
                    return formula;
                }
            }
            // If no formula is applicable, return last and replace missing variables with default values
            return worldFormulas.getLast();
        }

        // If no world-specific formulas, find a global formula
        List<Formula> globalFormulas = this.formulasPerWorld.get(Settings.GLOBAL_WORLD_SYMBOL);
        for (Formula globalFormula : globalFormulas) {
            if (globalFormula.canApply(playerHpData)) {
                return globalFormula;
            }
        }
        // If no global formula is 100% aplicable, use the last one
        return globalFormulas.getLast();
    }

    public double calculate(Player player, String worldName) {
        // Check which formula should apply
        Formula formula = getApplicableFormula(player, worldName);

        // Abstract player to playerHpData
        PlayerHpData playerHpData = getPlayerHpData(player, worldName);

        // calculate value from applicable formula
        Double defaultValue = this.defaultVariablesPerWorld.getOrDefault(worldName, this.defaultVariableGlobal);
        return formula.calculate(playerHpData, defaultValue);
    }

    public List<Formula> getFormulas(String worldName) {
        return List.copyOf(this.formulasPerWorld.getOrDefault(worldName, new ArrayList<>()));
    }

    public Set<String> getFormulaWorldsNames() {
        return this.formulasPerWorld.keySet().stream().filter(s -> !s.equals(Settings.GLOBAL_WORLD_SYMBOL))
                .collect(Collectors.toCollection(HashSet::new));
    }

    public Formula deleteFormula(String worldName, int formulaOrder) {
        List<Formula> formulasForWorld = this.formulasPerWorld.get(worldName);
        // Special case global formulas
        if (worldName.equals(Settings.GLOBAL_WORLD_SYMBOL) && formulasForWorld.size() == 1) {
            throw new RuntimeException("The global formulas list must contain at least 1 valid formula.");
        }
        Formula removed = formulasForWorld.remove(formulaOrder);
        String rawFormulaString = removed.getRawFormulaString();

        YamlFile formulasYaml = this.plugin.getFormulasYaml();
        List<String> formulasForWorldInFile = formulasYaml.getAccess().getStringList("formulas per world." + worldName);

        formulasForWorldInFile.remove(rawFormulaString);

        if (formulasForWorldInFile.isEmpty() && formulasForWorld.isEmpty()) {
            this.formulasPerWorld.remove(worldName);
            formulasYaml.getAccess().set("formulas per world." + worldName, null);
        }

        formulasYaml.save(true);

        return removed;
    }

    /**
     * Changes the relative order of a formula inside a list of formulas for a given world.
     * All orders are 1 based.
     *
     * @param worldName     The world in which to modify the formulas.
     * @param previousOrder The order of the formula that is to be modified.
     * @param newOrder      The new place the formula will take.
     */
    public void changeFormulaOrder(String worldName, int previousOrder, int newOrder) {
        if (previousOrder == newOrder) {
            return;
        }
        List<Formula> formulas = this.formulasPerWorld.get(worldName);
        if (newOrder < 1 || newOrder > formulas.size() + 1) {
            return;
        }
        Formula toEdit = formulas.remove(previousOrder - 1);
        formulas.add(newOrder - 1, toEdit);

        YamlFile formulasYaml = this.plugin.getFormulasYaml();
        List<String> formulasForWorldInFile = formulasYaml.getAccess().getStringList("formulas per world." + worldName);

        formulasForWorldInFile.remove(toEdit.getRawFormulaString());
        formulasForWorldInFile.add(newOrder - 1, toEdit.getRawFormulaString());

        formulasYaml.getAccess().set("formulas per world." + worldName, formulasForWorldInFile);
        formulasYaml.save(true);
    }

    public void saveNewFormula(String worldName, Formula formula, int formulaOrder) {
        if (!formula.isValid()) {
            this.messageSender.send(this.messageSender.getString(Message.FORMULA_CANNOT_SAVE_INVALID));
            throw new RuntimeException(this.messageSender.getString(Message.FORMULA_CANNOT_SAVE_INVALID));
        }
        List<Formula> existingFormulas = this.formulasPerWorld.getOrDefault(worldName, new ArrayList<>());
        if (formulaOrder < 1 || existingFormulas.size() + 1 < formulaOrder) {
            this.messageSender.send(this.messageSender.getString(Message.FORMULA_CANNOT_SAVE_INVALID_ORDER));
            throw new RuntimeException(this.messageSender.getString(Message.FORMULA_CANNOT_SAVE_INVALID_ORDER));
        }
        existingFormulas.add(formulaOrder - 1, formula);
    }

    public DynamicGUI createFormulasGUI() {
        DynamicGUI gui = new DynamicGUI(StringUtils.colorizeString(this.settings.getFormulasWorldsTitle()),
                Settings.FORMULAS_PER_WORLD_GUI_TAG,
                false,
                this.settings.getNavigationBar());
        NamespacedKey worldNameNamespacedKey = this.settings.getWorldNameNamespacedKey();
        Set<String> worldNames = getFormulaWorldsNames();
        // Add the worlds that do not have formulas
        worldNames.addAll(Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toSet()));

        // Add "GLOBAL" option for global formulas
        ItemStack formulasGlobalItem = this.settings.getFormulasGlobalItem();
        int globalFormulasCount = this.formulasPerWorld.get(Settings.GLOBAL_WORLD_SYMBOL).size();
        MPItemStacks.replacePlaceholders(formulasGlobalItem, new HashMap<>() {{
            put("%formulas%", String.valueOf(globalFormulasCount));
        }});
        gui.addItem(formulasGlobalItem);

        for (String worldName : worldNames) {
            ItemStack formulaWorldItem = this.settings.getFormulasWorldsItem();
            int formulasCount = getFormulas(worldName).size();
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

        return gui;
    }

    public DynamicGUI createFormulasGUIForWorld(String worldName) {
        boolean isGlobal = worldName.equals(Settings.GLOBAL_WORLD_SYMBOL);

        DynamicGUI gui = new DynamicGUI(StringUtils.colorizeString(
                this.settings.getFormulasForWorldTitle().replace("%world%",
                        isGlobal ? this.messageSender.getString(Message.FORMULA_LIST_GLOBAL) : worldName)),
                Settings.FORMULAS_FOR_WORLD_GUI_TAG_PREFIX + worldName,
                false,
                this.settings.getNavigationBar());
        NamespacedKey formulaOrderNamespacedKey = this.settings.getFormulaOrderNamespacedKey();
        List<Formula> formulas = getFormulas(worldName);

        for (int i = 0; i < formulas.size(); i++) {
            Formula formula = formulas.get(i);
            ItemStack formulaWorldItem = this.settings.getFormulasForWorldItem();
            int order = i + 1;
            MPItemStacks.replacePlaceholders(formulaWorldItem, new HashMap<>() {{
                put("%world%", isGlobal ?
                        FormulaManager.this.messageSender.getString(Message.FORMULA_LIST_GLOBAL_WORLD_NAME)
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
                    FormulaManager.this.messageSender.getString(Message.FORMULA_LIST_GLOBAL_WORLD_NAME)
                    : worldName);
        }});

        gui.addItem(formulaAddItem);

        return gui;
    }

    public SimpleGUI createFormulaAddGUI(FormulaCreationData formulaCreationData) {
        return createFormulaAddGUI(formulaCreationData.worldName(),
                formulaCreationData.formula() == null ? null : formulaCreationData.formula().getRawFormulaString(),
                formulaCreationData.formulaOrder());
    }

    public SimpleGUI createFormulaAddGUI(String worldName, String formulaRawString, int formulaOrder) {
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

        return gui;
    }

    @Override
    public void reload(boolean deep) {
        loadSettings();
    }

}
