package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.gui.HPGUIManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopHandler extends AbstractHandler {

    private final HPGUIManager hpGUIManager;
    private final Settings settings;

    public ShopHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.hpGUIManager = plugin.getHpGUIManager();
        this.settings = plugin.getSettings();
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String[] args) {
        return (args.length == 0 &&
                this.settings.isShopGUIEnabled() &&
                sender.hasPermission("HealthPower.gui"))
                ||
                (args.length > 0 && args[0].equalsIgnoreCase("gui"));
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.messageSender.send(sender, Message.CANNOT_SEND_CONSOLE);
            return;
        }
        if (!sender.hasPermission("HealthPower.gui")) {
            this.messageSender.send(sender, Message.NO_PERMISSION);
            return;
        }
        if (!this.settings.isShopGUIEnabled()) {
            this.messageSender.send(sender, Message.GUI_DISABLED);
            return;
        }
        if (this.plugin.getEconomy() == null) {
            this.messageSender.send(sender, Message.ECONOMY_DISABLED);
            return;
        }

        this.hpGUIManager.openGUI(player);

    }

}
