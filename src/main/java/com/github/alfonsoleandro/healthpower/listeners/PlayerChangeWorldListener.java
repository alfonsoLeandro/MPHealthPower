package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangeWorldListener implements Listener {

    private final HPManager hpManager;

    public PlayerChangeWorldListener(HealthPower plugin) {
        this.hpManager = plugin.getHpManager();
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        this.hpManager.checkAndCorrectHP(event.getPlayer());
    }
}
