package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportListener implements Listener {

    private final HPManager hpManager;

    public PlayerTeleportListener(HealthPower plugin) {
        this.hpManager = plugin.getHpManager();
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        this.hpManager.checkAndCorrectHP(event.getPlayer());
    }
}
