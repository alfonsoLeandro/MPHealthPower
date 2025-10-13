package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import com.github.alfonsoleandro.healthpower.managers.health.formula.cooldown.FormulaModifyManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.guis.utils.PlayersOnGUIsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadHandler extends AbstractHandler {

    private final HPManager hpManager;
    private final FormulaModifyManager formulaModifyManager;

    public ReloadHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.hpManager = plugin.getHpManager();
        this.formulaModifyManager = plugin.getFormulaModifyManager();
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String[] args) {
        return args.length > 0 && args[0].equalsIgnoreCase("reload");
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("HealthPower.reload")) {
            this.messageSender.send(sender, Message.NO_PERMISSION);
            return;
        }
        this.plugin.reload(false);

        if (this.plugin.getConfigYaml().getAccess().getBoolean("config.check HP on reload")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                this.hpManager.checkAndCorrectHP(p);
            }
            this.messageSender.send(sender, Message.PLAYERS_CHECKED);
        }

        // Closes the GUI for all players in case anyone was buying HP
        PlayersOnGUIsManager.removeAll(Settings.SHOP_GUI_TAG);
        PlayersOnGUIsManager.removeAll(Settings.FORMULAS_PER_WORLD_GUI_TAG);
        // Cannot close the GUI for players on formula add and formulas for world GUIs

        // Cancel all players editing or creating formulas
        for (Player player : this.formulaModifyManager.getPlayersOnCooldown()) {
            this.formulaModifyManager.removeCooldown(player);
            if (player.isOnline()) {
                this.messageSender.send(player, Message.FORMULA_ACTION_CANCELED);
                player.closeInventory();
            }
        }

        for (Player player : this.formulaModifyManager.getPlayersCreating()) {
            this.formulaModifyManager.clearCreationData(player);
            if (player.isOnline()) {
                this.messageSender.send(player, Message.FORMULA_ACTION_CANCELED);
                player.closeInventory();
            }
        }

        this.messageSender.send(sender, "&aFiles reloaded");
    }
}
