package com.github.alfonsoleandro.healthpower.managers.health;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.formula.FormulaManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.files.YamlFile;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import com.github.alfonsoleandro.mputils.reloadable.Reloadable;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class HPManager extends Reloadable {

    private final HealthPower plugin;
    private final MessageSender<Message> messageSender;
    private final Settings settings;
    private final FormulaManager formulaManager;
    private Map<String, Double> hpFloorPerWorld;
    private Map<String, Double> hpCapPerWorld;

    public HPManager(HealthPower plugin) {
        super(plugin);
        this.plugin = plugin;
        this.messageSender = plugin.getMessageSender();
        this.settings = plugin.getSettings();
        this.formulaManager = plugin.getFormulaManager();
        loadSettings();
    }

    private void loadSettings() {
        FileConfiguration config = this.plugin.getConfigYaml().getAccess();
        this.hpFloorPerWorld = new HashMap<>();
        ConfigurationSection hpFloorSection = config.getConfigurationSection("config.HP floor.worlds");
        if (hpFloorSection != null) {
            for (String worldName : hpFloorSection.getKeys(false)) {
                this.hpFloorPerWorld.put(worldName, hpFloorSection.getDouble(worldName));
            }
        }
        this.hpCapPerWorld.put(Settings.GLOBAL_WORLD_SYMBOL,
                Math.max(1, config.getDouble("config.HP cap.global")));
        this.hpCapPerWorld = new HashMap<>();
        ConfigurationSection hpCapSection = config.getConfigurationSection("config.HP cap.worlds");
        if (hpCapSection != null) {
            for (String worldName : hpCapSection.getKeys(false)) {
                this.hpCapPerWorld.put(worldName, hpCapSection.getDouble(worldName));
            }
        }
        this.hpCapPerWorld.put(Settings.GLOBAL_WORLD_SYMBOL,
                config.contains("config.HP cap.global") ?
                        config.getDouble("config.HP cap.global") : -1);
    }

    /**
     * Checks if a player's health is not set to the value it should be, if so, corrects this value.
     *
     * @param player The player to check and correct.
     */
    public void checkAndCorrectHP(Player player) {
        String worldName = player.getWorld().getName();

        debugPlayerGroupInfo(player);

        // Get calculated value (formulas)
        double calculated = this.formulaManager.calculate(player, worldName);

        debugPlayerHPDifference(player, calculated);

        // Get HP floor and cap
        double hpFloorForWorld = this.hpFloorPerWorld.getOrDefault(worldName, this.hpFloorPerWorld.get(Settings.GLOBAL_WORLD_SYMBOL));
        double hpCapForWorld = this.hpCapPerWorld.getOrDefault(worldName, this.hpCapPerWorld.get(Settings.GLOBAL_WORLD_SYMBOL));

        debugHpLimitsForWorld(worldName, hpFloorForWorld, hpCapForWorld);
        debugPlayerHPOverOrBelowLimits(player, calculated, hpFloorForWorld, worldName, hpCapForWorld);

        // Add Floor and Cap into the equation
        double effectiveHP = Math.max(calculated, hpFloorForWorld);
        if (hpCapForWorld > -1 && !player.hasPermission("HealthPower.cap.bypass")) {
            effectiveHP = Math.min(effectiveHP, hpCapForWorld);
        }

        debugEffectivePlayerMaxHp(player, worldName, effectiveHP);

        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH))
                .setBaseValue(effectiveHP);

        if (getHealth(player) != effectiveHP && this.settings.isNotifyHPCheck()) {
            this.messageSender.send(player, Message.HP_AUTOMATIC_SET,
                    "%HP%", String.valueOf(effectiveHP));
        }
    }

    public double getHealth(Player player) {
        return Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
    }

    public void addBaseHP(Player player, double amount) {
        YamlFile hpYaml = this.plugin.getHpYaml();
        FileConfiguration hpFile = hpYaml.getAccess();
        double prev = hpFile.contains("HP.players." + player.getName() + ".base") ?
                hpFile.getDouble("HP.players." + player.getName() + ".base") :
                this.settings.getDefaultBaseHp();
        hpFile.set("HP.players." + player.getName() + ".base", prev + amount);

        hpYaml.save(false);
    }

    public void setBaseHP(Player player, double amount) {
        YamlFile hpYaml = this.plugin.getHpYaml();
        hpYaml.getAccess().set("HP.players." + player.getName() + ".base", amount);

        hpYaml.save(false);
    }

    public void addShopHP(Player player, double amount) {
        YamlFile hpYaml = this.plugin.getHpYaml();
        FileConfiguration hpFile = hpYaml.getAccess();
        double prev = hpFile.contains("HP.players." + player.getName() + ".shop") ?
                hpFile.getDouble("HP.players." + player.getName() + ".shop") :
                this.settings.getDefaultBaseHp();
        hpFile.set("HP.players." + player.getName() + ".shop", prev + amount);

        hpYaml.save(false);
    }

    public void setShopHP(Player player, double amount) {
        YamlFile hpYaml = this.plugin.getHpYaml();
        hpYaml.getAccess().set("HP.players." + player.getName() + ".shop", amount);

        hpYaml.save(false);
    }

    private void debugPlayerGroupInfo(Player player) {
        if (this.settings.isDebug()) {
            if (this.plugin.setupPermissions() && this.plugin.getPermissions().hasGroupSupport()) {
                Permission perms = this.plugin.getPermissions();
                this.messageSender.send(
                        "&cDEBUG: &fGroup of " + player.getName() + ": " + perms.getPrimaryGroup(player));
            } else {
                this.messageSender.send("&cDEBUG: &fPermissions system not found for checking " + player.getName() + "'s permission group");
            }
        }
    }

    private void debugHpLimitsForWorld(String worldName, double hpFloorForWorld, double hpCapForWorld) {
        if (this.settings.isDebug()) {
            this.messageSender.send("&cDEBUG &fHP limits for world " + worldName + ": Floor: " + hpFloorForWorld + ", Cap: " + hpCapForWorld);
        }
    }

    private void debugEffectivePlayerMaxHp(Player player, String worldName, double effectiveHP) {
        if (this.settings.isDebug()) {
            this.messageSender.send("&cDEBUG &fSetting max HP of player " + player.getName() + " for world " + worldName + " to " + effectiveHP);
        }
    }

    private void debugPlayerHPOverOrBelowLimits(Player player, double calculated, double hpFloorForWorld, String worldName, double hpCapForWorld) {
        if (this.settings.isDebug()) {
            if (calculated < hpFloorForWorld) {
                this.messageSender.send("&cDEBUG &fHP of player " + player.getName() + " for world " + worldName + " (" + calculated + ") would be BELOW hp floor for the world: " + hpFloorForWorld);
            }
            if (calculated > hpCapForWorld) {
                this.messageSender.send("&cDEBUG &fHP of player " + player.getName() + " for world " + worldName + " (" + calculated + ") would be OVER hp cap for the world: " + hpCapForWorld);
            }
        }

    }

    private void debugPlayerHPDifference(Player player, double calculated) {
        double currentHealth = getHealth(player);
        if (calculated != currentHealth) {
            if (this.settings.isDebug()) {
                this.messageSender.send("&cDEBUG &fHP of " + player.getName() + " was incorrect (" + currentHealth + "), setting the correct value: " + calculated);
            }
        }
    }

    @Override
    public void reload(boolean deep) {
        loadSettings();
    }
}
