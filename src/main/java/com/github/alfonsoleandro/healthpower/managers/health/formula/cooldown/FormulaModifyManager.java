package com.github.alfonsoleandro.healthpower.managers.health.formula.cooldown;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.formula.Formula;
import com.github.alfonsoleandro.healthpower.managers.health.formula.gui.FormulaGUIManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class FormulaModifyManager {

    private final HealthPower plugin;
    private final MessageSender<Message> messageSender;
    private final FormulaGUIManager formulaGUIManager;
    private final Map<Player, FormulaClickedData> cooldowns = new HashMap<>();
    private final Map<Player, FormulaCreationData> formulaCreationData = new HashMap<>();

    public FormulaModifyManager(HealthPower plugin) {
        this.plugin = plugin;
        this.messageSender = plugin.getMessageSender();
        this.formulaGUIManager = plugin.getFormulaGUIManager();
    }

    public void startCooldown(Player player, String worldName, int formulaOrder, FormulaGUIAction action, long seconds) {
        BukkitTask task = new BukkitRunnable() {

            @Override
            public void run() {
                if (FormulaModifyManager.this.cooldowns.containsKey(player)) {
                    FormulaModifyManager.this.cooldowns.remove(player);
                    FormulaModifyManager.this.messageSender.send(player, Message.FORMULA_ACTION_TIMER_RAN_OUT);
                    // Re-open last GUI
                    FormulaModifyManager.this.formulaGUIManager.openLastGUI(player);
                }
            }
        }.runTaskLater(this.plugin, seconds * 20L);
        this.cooldowns.put(player, new FormulaClickedData(worldName, formulaOrder, action, task));

    }

    public boolean isNotInCooldown(Player player) {
        return !this.cooldowns.containsKey(player);
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

    public void addCreationData(Player player, String worldName, int formulaOrder, Formula formula) {
        this.formulaCreationData.put(player, new FormulaCreationData(worldName, formulaOrder, formula));
    }

    public void clearCreationData(Player player) {
        this.formulaCreationData.remove(player);
    }

    public boolean isCreating(Player player) {
        return this.formulaCreationData.containsKey(player);
    }

    public FormulaCreationData getCreationData(Player player) {
        return this.formulaCreationData.get(player);
    }
}
