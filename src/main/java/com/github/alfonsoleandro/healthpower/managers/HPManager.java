package com.github.alfonsoleandro.healthpower.managers;

import com.github.alfonsoleandro.healthpower.HealthPower;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Objects;

public class HPManager extends AbstractHPManager {


    public HPManager(HealthPower plugin){
        super(plugin);
    }

    @Override
    public double getHealth(Player player){
        return Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
    }

    @Override
    public void setHP(Player player, double value){
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(value);
    }




}
