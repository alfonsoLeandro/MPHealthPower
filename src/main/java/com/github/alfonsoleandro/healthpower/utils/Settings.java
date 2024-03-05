package com.github.alfonsoleandro.healthpower.utils;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.mputils.reloadable.Reloadable;
import org.bukkit.configuration.file.FileConfiguration;

public class Settings extends Reloadable {

    private final HealthPower plugin;
    // Fields
    private boolean checkHPOnJoin;
    private boolean consumablesEnabled;
    private boolean debug;
    private boolean shopGUIEnabled;
    private boolean updateHPOnJoin;

    public Settings(HealthPower plugin) {
        super(plugin);
        this.plugin = plugin;
        loadFields();
    }

    private void loadFields() {
        FileConfiguration config = this.plugin.getConfigYaml().getAccess();

        this.checkHPOnJoin = config.getBoolean("config.check HP on join");
        this.consumablesEnabled = config.getBoolean("config.consumables enabled");
        this.debug = config.getBoolean("config.debug");
        this.shopGUIEnabled = config.getBoolean("config.GUI.enabled");
        this.updateHPOnJoin = config.getBoolean("config.update HP on join");
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

    public boolean isShopGUIEnabled() {
        return this.shopGUIEnabled;
    }

    public boolean isUpdateHPOnJoin() {
        return this.updateHPOnJoin;
    }
}