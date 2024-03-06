package com.github.alfonsoleandro.healthpower.managers.health;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.mputils.files.YamlFile;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import com.github.alfonsoleandro.mputils.reloadable.Reloadable;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Objects;
import java.util.stream.Collectors;

public class HPManager extends Reloadable {

    private final HealthPower plugin;
    private final MessageSender<Message> messageSender;
    private final YamlFile hpYaml;
    private boolean isDebug;
    private boolean usePermissionsSystem;
    private boolean useGroupsSystem;
    private double hpCap;

    public HPManager(HealthPower plugin) {
        super(plugin);
        this.plugin = plugin;
        this.messageSender = plugin.getMessageSender();
        this.hpYaml = plugin.getHpYaml();
        loadSettings();
    }


    /**
     * Automatically sets a player's HP.
     * Called by {@link #checkAndCorrectHP(Player)}.
     *
     * @param player   The player to correct the HP for.
     * @param newValue The value to set the player's hp to.
     */
    public void automaticSetHP(Player player, double newValue) {
        if (this.isDebug)
            this.messageSender.send("&fHP of " + player.getName() + " set to " + newValue);

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
        FileConfiguration hp = this.plugin.getHpYaml().getAccess();

        if (this.isDebug) {
            if (this.plugin.setupPermissions() && this.plugin.getPermissions().hasGroupSupport()) {
                Permission perms = this.plugin.getPermissions();
                this.messageSender.send(
                        "&fGroup of " + player.getName() + ": " + perms.getPrimaryGroup(player));
            } else {
                this.messageSender.send("&fPermissions system not found for checking " + player.getName() + "'s permission group");
            }
        }


        if (hp.contains("HP.players." + player.getName())) {
            if (this.isDebug)
                this.messageSender.send("&fHP file contains " + player.getName());
            double value = hp.getDouble("HP.players." + player.getName());
            if (value != getHealth(player)) {
                if (this.isDebug)
                    this.messageSender.send("&fHP of " + player.getName() + " set by name (overrides groups and permissions based HP)");
                automaticSetHP(player, value);
            }
            return;
        }
        if (this.usePermissionsSystem) {
            double value = 0;

            //Check for every permission if a permission is similar to an amount permission
            for (PermissionAttachmentInfo perm : player.getEffectivePermissions()
                    .stream()
                    .filter(p -> p.getPermission().contains("healthpower.amount."))
                    .collect(Collectors.toSet())) {

                if (this.isDebug)
                    this.messageSender.send("&fFound permission \"&c" + perm.getPermission() + "&f\" for player " + player.getName());

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
                if (value != getHealth(player)) {
                    if (this.isDebug)
                        this.messageSender.send("&fHP of " + player.getName() + " set by permission (overrides groups based HP)");
                    automaticSetHP(player, value);
                }
                return;
            }

        }
        if (this.useGroupsSystem && this.plugin.getPermissions() != null) {
            Permission perms = this.plugin.getPermissions();
            if (perms.hasGroupSupport()) {
                String group = perms.getPrimaryGroup(player);
                if (hp.contains("HP.groups." + group)) {
                    double value = hp.getDouble("HP.groups." + group);

                    if (value > 0) {
                        if (value != getHealth(player)) {
                            if (this.isDebug)
                                this.messageSender.send("&fHP of " + player.getName() + " set by group (group: " + group + ")");
                            automaticSetHP(player, value);
                        }
                        return;
                    }

                }
            }
        }

        double defaultValue = hp.getDouble("HP.default");
        if (defaultValue < 1) {
            if (this.isDebug)
                this.messageSender.send("&fHP of " + player.getName() + " would have been set to the default value," +
                        " but the default value is currently disabled.");
            return;
        }

        if (this.isDebug)
            this.messageSender.send("&fHP of " + player.getName() + " set to default value (" + defaultValue + ")");
        if (getHealth(player) != defaultValue) automaticSetHP(player, defaultValue);


    }

    public void setHPCommand(CommandSender setter, Player player, double newValue) {
        if (setter.equals(player)) {
            this.messageSender.send(player, Message.YOU_SET_HP,
                    "%player%", this.messageSender.getString(Message.YOURSELF),
                    "%HP%", String.valueOf(newValue));
        } else {
            this.messageSender.send(player, Message.YOU_SET_HP,
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

        YamlFile hpYaml = this.plugin.getHpYaml();
        hpYaml.getAccess().set("HP.players." + player.getName(), getHealth(player));
        hpYaml.save(true);
    }

    public void addHP(CommandSender setter, Player player, double newValue) {
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

        YamlFile hpYaml = this.plugin.getHpYaml();
        hpYaml.getAccess().set("HP.players." + player.getName(), getHealth(player));
        hpYaml.save(true);
    }

    public void consumableAddHP(Player player, double amount) {
        if (cannotAddHP(player, amount)) {
            this.messageSender.send(player, Message.YOUR_HP_ABOVE_CAP);
            return;
        }

        setHP(player, amount + getHealth(player));

        YamlFile hpYaml = this.plugin.getHpYaml();
        hpYaml.getAccess().set("HP.players." + player.getName(), getHealth(player));
        hpYaml.save(true);

        return true;

    }

    public void consumableSetHP(Player player, double amount) {
        if (cannotSetHP(player, amount)) {
            this.messageSender.send(player, Message.YOUR_HP_ABOVE_CAP);
            return;
        }

        setHP(player, amount);
        YamlFile hpYaml = this.plugin.getHpYaml();
        hpYaml.getAccess().set("HP.players." + player.getName(), getHealth(player));
        hpYaml.save(true);
        return true;
    }

    public boolean guiAddHP(Player player, double amount) {
        //Check if HP would be above cap
        if (!player.hasPermission("HealthPower.cap.bypass")
                && (this.hpCap > 0 && amount > this.hpCap)) {
            this.messageSender.send(player, Message.YOUR_HP_ABOVE_CAP);
            return false;
        }

        setHP(player, amount + getHealth(player));
        YamlFile hpYaml = this.plugin.getHpYaml();
        hpYaml.getAccess().set("HP.players." + player.getName(), getHealth(player));
        hpYaml.save(true);
        return true;
    }

    public void guiSetHP(Player player, double amount) {
        if (cannotSetHP(player, amount)) {
            this.messageSender.send(player, Message.YOUR_HP_ABOVE_CAP);
            return;
        }

        setHP(player, amount);
        YamlFile hpYaml = this.plugin.getHpYaml();
        hpYaml.getAccess().set("HP.players." + player.getName(), getHealth(player));
        hpYaml.save(true);
        return true;
    }

    public void guiRemoveHP(Player player, double amount) {
        if (getHealth(player) - amount <= 0) {
            this.messageSender.send(player, Message.CANNOT_SET_HP_UNDER_0);
            return;
        }

        setHP(player, getHealth(player) - amount);
        YamlFile hpYaml = this.plugin.getHpYaml();
        hpYaml.getAccess().set("HP.players." + player.getName(), getHealth(player));
        hpYaml.save(true);
        return true;
    }

    private void saveToFile(Player player) {
        YamlFile hpYaml = this.plugin.getHpYaml();
        hpYaml.getAccess().set("HP.players." + player.getName(), getHealth(player));
        hpYaml.save(true);
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
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(value);
    }


    protected void loadSettings() {
        FileConfiguration config = this.plugin.getConfigYaml().getAccess();
        this.isDebug = config.getBoolean("config.debug");
        this.usePermissionsSystem = config.getBoolean("config.use permissions system");
        this.useGroupsSystem = config.getBoolean("config.use groups system");
        this.hpCap = config.getBoolean("config.HP cap.enabled") ?
                config.getDouble("config.HP cap.amount")
                :
                -1;
    }


    @Override
    public void reload(boolean deep) {
        loadSettings();
    }


}
