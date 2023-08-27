package com.github.alfonsoleandro.healthpower.managers;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.mputils.reloadable.Reloadable;

/**
 * @author alfonsoLeandro
 */
public class ConsumableManager extends Reloadable {

    public ConsumableManager(HealthPower plugin) {
        super(plugin);
        loadConsumables();
    }

    private void loadConsumables() {
        //TODO
    }

    @Override
    public void reload(boolean deep) {
        loadConsumables();
    }
}
