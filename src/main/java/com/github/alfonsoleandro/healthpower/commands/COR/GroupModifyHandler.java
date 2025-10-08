package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;

public class GroupModifyHandler extends AbstractHandler {

    private final Settings settings;

    public GroupModifyHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
        this.settings = plugin.getSettings();
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String[] args) {
        return args.length > 0 && args[0].equalsIgnoreCase("group");
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("HealthPower.group.set")) {
            this.messageSender.send(sender, Message.NO_PERMISSION);
            return;
        }
        if (!this.settings.isUseGroupsSystem() || this.plugin.getPermissions() == null) {
            this.messageSender.send(sender, Message.PERMISSIONS_SYSTEM_DISABLED);
            return;
        }

        if (args.length < 4
                || !args[0].equalsIgnoreCase("group")
                || !args[1].equalsIgnoreCase("set")) {
            this.messageSender.send(sender, Message.COMMAND_USE_GROUP_SET,
                    "%command%", label);
            return;
        }

        String groupName = args[2].strip();
        Permission permissions = this.plugin.getPermissions();
        if (Arrays.stream(permissions.getGroups()).noneMatch(groupName::equals)) {
            this.messageSender.send(sender, Message.GROUP_NOT_FOUND,
                    "%group%", groupName);
            return;
        }

        double hp;
        try {
            hp = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            this.messageSender.send(sender, Message.HP_MUST_BE_NUMBER);
            return;
        }

        FileConfiguration hpFile = this.plugin.getHpYaml().getAccess();
        hpFile.set("HP.groups." + groupName, hp);

        this.plugin.getHpYaml().save(true);

        this.messageSender.send(sender, Message.GROUP_HP_SET,
                "%group%", groupName,
                "%HP%", String.valueOf(hp));
    }
}
