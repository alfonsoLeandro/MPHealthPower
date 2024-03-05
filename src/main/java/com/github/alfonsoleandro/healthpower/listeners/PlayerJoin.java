package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.AbstractHPManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoin implements Listener {

    private final HealthPower plugin;
    private final MessageSender<Message> messageSender;
    private final AbstractHPManager hpManager;
    private final Settings settings;

    public PlayerJoin(HealthPower plugin) {
        this.plugin = plugin;
        this.messageSender = plugin.getMessageSender();
        this.hpManager = plugin.getHpManager();
        this.settings = plugin.getSettings();
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        checkForUpdates(player);

        if (this.settings.isCheckHPOnJoin()) {
            this.hpManager.checkAndCorrectHP(player);
        }

        updateHPBar(player);
    }


    /**
     * Checks if there is a new version of the plugin and if that is the case, sends a message to the player if
     * that player has OP permissions.
     *
     * @param player The player that joined and is checking for updates.
     */
    private void checkForUpdates(Player player) {
        if (player.isOp()) {
            if (!this.plugin.getVersion().equals(this.plugin.getLatestVersion())) {
                this.messageSender.send(player, "&4New version available &7(&e" + this.plugin.getLatestVersion() + "&7)");
                this.messageSender.send(player, "&fhttps://bit.ly/hpUpdate");
            }
        }
    }


    /**
     * Forces a render update for the players' HP bar.
     *
     * @param player The player to update the bar for.
     */
    private void updateHPBar(Player player) {
        if (this.settings.isUpdateHPOnJoin()) {
            if (this.settings.isDebug()) {
                this.messageSender.send("Updating HP bar of player " + player.getName());
            }
            double actualHealth = this.hpManager.getHealth(player);
            this.hpManager.setHP(player, 1);
            new BukkitRunnable() {
                public void run() {
                    PlayerJoin.this.hpManager.setHP(player, actualHealth);
                }
            }.runTaskLater(this.plugin, 2);

        }
    }


}
