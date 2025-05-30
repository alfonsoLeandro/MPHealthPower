package com.github.alfonsoleandro.healthpower.managers.checking;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.reloadable.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class PeriodicHPChecker extends Reloadable {

    private final HealthPower plugin;
    private final HPManager hpManager;
    private final Settings settings;
    private BukkitTask checkerTask;

    public PeriodicHPChecker(HealthPower plugin) {
        super(plugin);
        this.plugin = plugin;
        this.hpManager = plugin.getHpManager();
        this.settings = plugin.getSettings();
        if (this.settings.isPeriodicCheckerEnabled()) {
            schedulePeriodicChecker();
        }
    }

    private void schedulePeriodicChecker() {
        long period = this.settings.getPeriodicCheckerPeriod();
        this.checkerTask = new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PeriodicHPChecker.this.hpManager.checkAndCorrectHP(player);
                }
            }
        }.runTaskTimer(this.plugin, period, period);
    }


    @Override
    public void reload(boolean deep) {
        if (this.checkerTask != null && !this.checkerTask.isCancelled()) {
            this.checkerTask.cancel();
        }
        if (this.settings.isPeriodicCheckerEnabled()) {
            schedulePeriodicChecker();
        }
    }
}
