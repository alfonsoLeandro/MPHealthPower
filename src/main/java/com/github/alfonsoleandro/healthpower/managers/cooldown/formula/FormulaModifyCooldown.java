package com.github.alfonsoleandro.healthpower.managers.cooldown.formula;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class FormulaModifyCooldown {

    private final HealthPower plugin;
    private final MessageSender<Message> messageSender;
    private final Map<Player, FormulaClickedData> cooldowns = new HashMap<>();

    public FormulaModifyCooldown(HealthPower plugin) {
        this.plugin = plugin;
        this.messageSender = plugin.getMessageSender();
    }

    public void startCooldown(Player player, String worldName, int formulaOrder, FormulaGUIAction action) {
        BukkitTask task = new BukkitRunnable() {

            @Override
            public void run() {
                if (FormulaModifyCooldown.this.cooldowns.containsKey(player)) {
                    FormulaModifyCooldown.this.cooldowns.remove(player);
                    FormulaModifyCooldown.this.messageSender.send(player, Message.FORMULA_ACTION_TIMER_RAN_OUT);
                }
            }
        }.runTaskLater(this.plugin, 200);
        this.cooldowns.put(player, new FormulaClickedData(worldName, formulaOrder, action, task));

    }

    public boolean isInCooldown(Player player) {
        return this.cooldowns.containsKey(player);
    }

    public FormulaClickedData getData(Player player) {
        return this.cooldowns.get(player);
    }

    public void removeCooldown(Player player) {
        FormulaClickedData data = this.cooldowns.remove(player);
        if (data != null && !data.cooldownTimer().isCancelled()) {
            data.cooldownTimer().cancel();
        }
    }
}
