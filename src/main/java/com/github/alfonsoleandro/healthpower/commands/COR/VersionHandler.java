package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.utils.Message;
import org.bukkit.command.CommandSender;

/**
 * Handles the version check command - shows the plugin's current version.
 */
public class VersionHandler extends AbstractHandler{

    public VersionHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String[] args) {
        return args.length > 0 && args[0].equalsIgnoreCase("version");
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        if(!sender.hasPermission("HealthPower.version")) {
            this.messageSender.send(sender, Message.NO_PERMISSION);
            return;
        }

        if(this.plugin.getVersion().equals(this.plugin.getLatestVersion())){
            this.messageSender.send(sender, "&fVersion: &e"+this.plugin.getVersion()+" &f(&aLatest version!&f)");
        }else{
            this.messageSender.send(sender, "&fVersion: &e"+this.plugin.getVersion()+" &f(&cUpdate available!&f)");
            this.messageSender.send(sender, "&cDownload: &fhttps://bit.ly/3fqzRpR");
        }
    }
}
