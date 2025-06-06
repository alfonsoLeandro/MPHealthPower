package com.github.alfonsoleandro.healthpower.utils;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.mputils.reloadable.Reloadable;
import com.github.alfonsoleandro.mputils.time.TimeUtils;
import org.bukkit.configuration.file.FileConfiguration;

public class Settings extends Reloadable {

    private final HealthPower plugin;
    // Fields
    private boolean checkHPOnJoin;
    private boolean consumablesEnabled;
    private boolean debug;
    private double minimumHP;
    private boolean periodicCheckerEnabled;
    private long periodicCheckerPeriod;
    private boolean shopGUIEnabled;
    private boolean updateHPOnJoin;
    private boolean useGroupsSystem;


    public Settings(HealthPower plugin) {
        super(plugin, Priority.HIGHEST);
        this.plugin = plugin;
        loadFields();
    }

    private void loadFields() {
        FileConfiguration config = this.plugin.getConfigYaml().getAccess();
        FileConfiguration hp = this.plugin.getHpYaml().getAccess();

        this.checkHPOnJoin = config.getBoolean("config.check HP on join");
        this.consumablesEnabled = config.getBoolean("config.consumables enabled");
        this.debug = config.getBoolean("config.debug");
        this.minimumHP = Math.max(1, hp.getDouble("HP.minimum"));
        this.periodicCheckerEnabled = config.getBoolean("config.periodic checker.enabled");
        String timeString = config.getString("config.periodic checker.period");
        this.periodicCheckerPeriod = TimeUtils.getTicks(timeString != null ? timeString : "5m");
        this.shopGUIEnabled = config.getBoolean("config.GUI.enabled");
        this.updateHPOnJoin = config.getBoolean("config.update HP on join");
        this.useGroupsSystem = config.getBoolean("config.use groups system");
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

    public double getMinimumHP() {
        return this.minimumHP;
    }

    public boolean isPeriodicCheckerEnabled() {
        return this.periodicCheckerEnabled;
    }

    public long getPeriodicCheckerPeriod() {
        return this.periodicCheckerPeriod;
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
}