package com.github.alfonsoleandro.healthpower.utils;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.AbstractHPManager;
import com.github.alfonsoleandro.mputils.guis.SimpleGUI;
import com.github.alfonsoleandro.mputils.itemstacks.MPItemStacks;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import com.github.alfonsoleandro.mputils.reloadable.Reloadable;
import org.bukkit.configuration.file.FileConfiguration;

public class Settings extends Reloadable {

    private final HealthPower plugin;
    // Fields
    private boolean shopGUIEnabled;
    private boolean consumablesEnabled;

    public Settings(HealthPower plugin) {
        super(plugin);
        this.plugin = plugin;
        loadFields();
    }

    private void loadFields() {
        FileConfiguration config = this.plugin.getConfigYaml().getAccess();

        this.shopGUIEnabled = config.getBoolean("config.GUI.enabled");
        this.consumablesEnabled = config.getBoolean("config.consumables enabled");
    }


    @Override
    public void reload(boolean deep) {
        this.loadFields();
    }


    public boolean isShopGUIEnabled() {
        return this.shopGUIEnabled;
    }

    public boolean isConsumablesEnabled() {
        return this.consumablesEnabled;
    }
}