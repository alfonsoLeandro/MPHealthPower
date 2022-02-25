package com.github.alfonsoleandro.healthpower.managers;

import com.github.alfonsoleandro.healthpower.HealthPower;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class HPManagerLegacy extends AbstractHPManager{

    public HPManagerLegacy(HealthPower plugin) {
        super(plugin);
    }

    @Override
    public void setHP(Player player, double value){
        player.setMaxHealth(value);
    }

    @Override
    public double getHealth(Player player){
        return player.getMaxHealth();
    }
}
