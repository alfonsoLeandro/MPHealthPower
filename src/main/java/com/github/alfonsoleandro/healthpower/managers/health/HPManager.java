package com.github.alfonsoleandro.healthpower.managers.health;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.formula.Formula;
import com.github.alfonsoleandro.healthpower.managers.health.formula.FormulaVariable;
import com.github.alfonsoleandro.healthpower.managers.health.formula.PlayerHpData;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.files.YamlFile;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import com.github.alfonsoleandro.mputils.reloadable.Reloadable;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HPManager extends Reloadable {

    private final HealthPower plugin;
    private final MessageSender<Message> messageSender;
    private final YamlFile hpYaml;
    private final Settings settings;
    private boolean usePermissionsSystem;
    private double hpCap;
    private double defaultBaseHp;
    private Map<String, Double> defaultVariablesPerWorld;
    private Map<String, Double> hpPerGroup;
    private double defaultVariableGlobal;
    private Formula defaultFormula;
    private Map<String, List<Formula>> formulasPerWorld;

    public HPManager(HealthPower plugin) {
        super(plugin);
        this.plugin = plugin;
        this.messageSender = plugin.getMessageSender();
        this.hpYaml = plugin.getHpYaml();
        this.settings = plugin.getSettings();
        loadSettings();
    }

    protected void loadSettings() {
        FileConfiguration config = this.plugin.getConfigYaml().getAccess();
        this.usePermissionsSystem = config.getBoolean("config.use permissions system");
        this.hpCap = config.getBoolean("config.HP cap.enabled") ?
                config.getDouble("config.HP cap.amount")
                :
                -1;
        this.defaultBaseHp = config.getDouble("config.default base HP");
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

        // Load default formula for all non specified worlds
        this.defaultFormula = new Formula(Objects.requireNonNull(formulas.getString("default formula")));
        if (!this.defaultFormula.isValid()) {
            //TODO: throw error, default formula cannot be invalid
        }

        //Load formulas per world
        this.formulasPerWorld = new HashMap<>();
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
                                            "%formula%", f.getRawFormulaString(), "%world%", worldName);
                                }
                                return true;
                            }
                            this.messageSender.send(Bukkit.getConsoleSender(),
                                    Message.FORMULA_INVALID,
                                    "%formula%", f.getRawFormulaString(), "%world%", worldName);
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
                if (!this.usePermissionsSystem) {
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
                if (playersSection != null && playersSection.contains(player.getName()+".shop")) {
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

        // If no world-specific formulas, return the default formula.
        return this.defaultFormula;
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

    /**
     * Automatically sets a player's HP.
     * Called by {@link #checkAndCorrectHP(Player)}.
     *
     * @param player   The player to correct the HP for.
     * @param newValue The value to set the player's hp to.
     */
    public void automaticSetHP(Player player, double newValue) {
        // TODO: use new code
        if (this.settings.isDebug()) {
            if (cannotSetHP(player, newValue)) {
                this.messageSender.send("&cDEBUG: &fHP of " + player.getName() + " would have been set to " + newValue + ", but would exceed the cap.");
                this.messageSender.send("&cDEBUG: &fHP of " + player.getName() + " set to cap (" + this.hpCap + ").");
            } else {
                this.messageSender.send("&cDEBUG: &fHP of " + player.getName() + " set to " + newValue);
            }
        }

        if (cannotSetHP(player, newValue)) {
            newValue = this.hpCap;
        }

        setHP(player, newValue);

        this.messageSender.send(player, Message.HP_AUTOMATIC_SET,
                "%HP%", String.valueOf(newValue));
    }

    /**
     * Checks if a player's health is not set to the value it should be, if so, corrects this value.
     *
     * @param player The player to check and correct.
     */
    public void checkAndCorrectHP(Player player) {
        FileConfiguration hp = this.hpYaml.getAccess();
        double currentHealth = getHealth(player);

        if (this.settings.isDebug()) {
            if (this.plugin.setupPermissions() && this.plugin.getPermissions().hasGroupSupport()) {
                Permission perms = this.plugin.getPermissions();
                this.messageSender.send(
                        "&cDEBUG: &fGroup of " + player.getName() + ": " + perms.getPrimaryGroup(player));
            } else {
                this.messageSender.send("&cDEBUG: &fPermissions system not found for checking " + player.getName() + "'s permission group");
            }
        }


        if (hp.contains("HP.players." + player.getName())) {
            if (this.settings.isDebug()) {
                this.messageSender.send("&cDEBUG &fHP file contains " + player.getName());
            }
            double value = hp.getDouble("HP.players." + player.getName());
            if (value != currentHealth) {
                if (this.settings.isDebug()) {
                    this.messageSender.send("&cDEBUG: &fHP of " + player.getName() + " set by name (overrides groups and permissions based HP)");
                }
                if (value <= 0) {
                    this.messageSender.send("&cIncorrect HP for player " + player.getName() + " in the HP file.");
                    return;
                }
                automaticSetHP(player, value);
            } else if (cannotSetHP(player, currentHealth)) {
                if (this.settings.isDebug()) {
                    this.messageSender.send("&cDEBUG: &fHP of " + player.getName() + " was above cap, it has now been set to cap (" + this.hpCap + ")");
                }
                automaticSetHP(player, this.hpCap);
            }
            return;
        }

        if (this.usePermissionsSystem) {
            double value = 0;

            //Check for every permission if a permission is similar to an amount permission
            for (PermissionAttachmentInfo perm : player.getEffectivePermissions()
                    .stream()
                    .filter(p -> p.getPermission().startsWith("healthpower.amount."))
                    .collect(Collectors.toSet())) {

                if (this.settings.isDebug()) {
                    this.messageSender.send("&cDEBUG: &fFound permission \"&c" + perm.getPermission() + "&f\" for player " + player.getName());
                }

                String newValueString = perm.getPermission().replace("healthpower.amount.", "");
                try {
                    double newValue = Double.parseDouble(newValueString);
                    if (value == 0 || newValue > value) {
                        value = newValue;
                    }
                } catch (NumberFormatException e) {
                    this.messageSender.send("&cThere was an error while trying to set HP using permissions system.");
                    this.messageSender.send("\"&c" + newValueString + "&f\" is not a valid number");
                    this.messageSender.send("Correct syntax is: \"&chealthpower.amount.&a&lNUMBER&f\"");
                }

            }

            //Finally, set the value
            if (value > 0) {
                if (value != currentHealth) {
                    if (this.settings.isDebug()) {
                        this.messageSender.send("&cDEBUG: &fHP of " + player.getName() + " set by permission (overrides groups based HP)");
                    }
                    automaticSetHP(player, value);
                }
                return;
            }

        }

        if (this.settings.isUseGroupsSystem() && this.plugin.getPermissions() != null) {
            Permission perms = this.plugin.getPermissions();
            if (perms.hasGroupSupport()) {
                String group = perms.getPrimaryGroup(player);
                if (hp.contains("HP.groups." + group)) {
                    double value = hp.getDouble("HP.groups." + group);

                    if (value > 0) {
                        if (value != currentHealth) {
                            if (this.settings.isDebug()) {
                                this.messageSender.send("&cDEBUG: &fHP of " + player.getName() + " set by group (group: " + group + ")");
                            }
                            automaticSetHP(player, value);
                        }
                        return;
                    }

                }
            }
        }

        if (this.defaultBaseHp < 1) {
            if (this.settings.isDebug()) {
                this.messageSender.send("&cDEBUG: &fHP of " + player.getName() + " would have been set to the default value," +
                        " but the default value is currently disabled.");
            }
            return;
        }

        if (this.settings.isDebug()) {
            this.messageSender.send("&cDEBUG: &fHP of " + player.getName() + " set to default value (" + this.defaultBaseHp + ")");
        }
        if (currentHealth != this.defaultBaseHp) {
            automaticSetHP(player, this.defaultBaseHp);
        }


    }

    public void setHPCommand(CommandSender setter, Player player, double newValue) {
        if (setter.equals(player)) {
            this.messageSender.send(player, Message.YOU_SET_HP,
                    "%player%", this.messageSender.getString(Message.YOURSELF),
                    "%HP%", String.valueOf(newValue));
        } else {
            this.messageSender.send(setter, Message.YOU_SET_HP,
                    "%player%", player.getName(),
                    "%HP%", String.valueOf(newValue));
            this.messageSender.send(player, Message.SET_HP,
                    "%player%", setter.getName(),
                    "%HP%", String.valueOf(newValue));
        }

        //Check if HP would be above cap
        if (cannotSetHP(player, newValue)) {
            this.messageSender.send(setter, Message.PLAYER_HP_ABOVE_CAP);
            this.messageSender.send(player, Message.YOUR_HP_ABOVE_CAP);
            return;
        }

        setHP(player, newValue);
        saveToFile(player);
    }

    public void addHPCommand(CommandSender setter, Player player, double newValue) {
        if (setter.equals(player)) {
            this.messageSender.send(setter, Message.YOU_ADD_HP,
                    "%player%", this.messageSender.getString(Message.YOURSELF),
                    "%HP%", String.valueOf(newValue));
        } else {
            this.messageSender.send(setter, Message.YOU_ADD_HP,
                    "%player%", player.getName(),
                    "%HP%", String.valueOf(newValue));
            this.messageSender.send(player, Message.ADD_HP,
                    "%player%", setter.getName(),
                    "%HP%", String.valueOf(newValue));
        }

        if (cannotAddHP(player, newValue)) {
            this.messageSender.send(setter, Message.PLAYER_HP_ABOVE_CAP);
            this.messageSender.send(player, Message.YOUR_HP_ABOVE_CAP);
            return;
        }

        setHP(player, newValue + getHealth(player));
        saveToFile(player);
    }

    public void consumableOrGUIAddHP(Player player, double amount) {
        if (cannotAddHP(player, amount)) {
            this.messageSender.send(player, Message.YOUR_HP_ABOVE_CAP);
            return;
        }

        setHP(player, amount + getHealth(player));
        saveToFile(player);
    }

    public void consumableOrGUISetHP(Player player, double amount) {
        if (cannotSetHP(player, amount)) {
            this.messageSender.send(player, Message.YOUR_HP_ABOVE_CAP);
            return;
        }

        setHP(player, amount);
        saveToFile(player);
    }

    public void guiRemoveHP(Player player, double amount) {
        if (getHealth(player) - amount <= 0) {
            this.messageSender.send(player, Message.CANNOT_SET_HP_UNDER_0);
            return;
        }

        setHP(player, getHealth(player) - amount);
        saveToFile(player);
    }

    private void saveToFile(Player player) {
        this.hpYaml.getAccess().set("HP.players." + player.getName(), getHealth(player));
        this.hpYaml.save(true);
    }

    public boolean cannotSetHP(Player player, double amount) {
        return !player.hasPermission("HealthPower.cap.bypass") && amount > this.hpCap;
    }

    public boolean cannotAddHP(Player player, double amount) {
        return !player.hasPermission("HealthPower.cap.bypass") && (amount + getHealth(player)) > this.hpCap;
    }

    public double getHealth(Player player) {
        return Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
    }

    public void setHP(Player player, double value) {
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH))
                .setBaseValue(Math.max(this.settings.getMinimumHP(), value));
    }


    @Override
    public void reload(boolean deep) {
        loadSettings();
    }


}
