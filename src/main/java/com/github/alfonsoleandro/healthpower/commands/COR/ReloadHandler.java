package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.guis.utils.PlayersOnGUIsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Objects;

public class ReloadHandler extends AbstractHandler {

    private final HPManager hpManager;
    private final Settings settings;

    public ReloadHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.hpManager = plugin.getHpManager();
        this.settings = plugin.getSettings();
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

        if (this.plugin.getConfig().getBoolean("config.check HP on reload")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                this.hpManager.checkAndCorrectHP(p);
            }
            this.messageSender.send(sender, Message.PLAYERS_CHECKED);
        }

        // Closes the GUI for all players in case anyone was buying HP
        PlayersOnGUIsManager.removeAll("MPHealthPower:SHOP");

        FileConfiguration hpFile = this.plugin.getHpYaml().getAccess();

        if (hpFile.contains("HP.groups") && this.settings.isUseGroupsSystem()) {
            double minimumHP = this.settings.getMinimumHP();
            for (String groupName : Objects.requireNonNull(hpFile.getConfigurationSection("HP.groups"))
                    .getKeys(false)) {
                double groupHp = hpFile.getDouble("HP.groups." + groupName);
                if (groupHp < minimumHP) {
                    if(!sender.equals(Bukkit.getConsoleSender())){
                        this.messageSender.send(sender, Message.GROUP_HP_UNDER_MINIMUM,
                                "%group%", groupName,
                                "%HP%", String.valueOf(groupHp),
                                "%minimum%", String.valueOf(minimumHP));
                    }
                    this.messageSender.send(Bukkit.getConsoleSender(), Message.GROUP_HP_UNDER_MINIMUM,
                            "%group%", groupName,
                            "%HP%", String.valueOf(groupHp),
                            "%minimum%", String.valueOf(minimumHP));
                }

            }
        }

        this.messageSender.send(sender, "&aFiles reloaded");
    }
}
