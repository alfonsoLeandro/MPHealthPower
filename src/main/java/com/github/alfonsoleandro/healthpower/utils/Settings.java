package com.github.alfonsoleandro.healthpower.utils;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.mputils.reloadable.Reloadable;
import org.bukkit.configuration.file.FileConfiguration;

public class Settings extends Reloadable {

    private final HealthPower plugin;
    // Fields
    private boolean shopGUIEnabled;

    public Settings(HealthPower plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    private void loadFields() {
        FileConfiguration config = this.plugin.getConfigYaml().getAccess();
        //TODO
        this.shopGUIEnabled = config.getBoolean("config.GUI.enabled");

    }


    @Override
    public void reload(boolean deep) {
        this.loadFields();
    }

    public boolean isShopGUIEnabled() {
        return this.shopGUIEnabled;
    }
}