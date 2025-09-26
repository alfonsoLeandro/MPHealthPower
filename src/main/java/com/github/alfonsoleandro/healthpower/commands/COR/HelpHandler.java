package com.github.alfonsoleandro.healthpower.commands.COR;

import com.github.alfonsoleandro.healthpower.HealthPower;
import org.bukkit.command.CommandSender;

public class HelpHandler extends AbstractHandler{

    public HelpHandler(HealthPower plugin, AbstractHandler successor) {
        super(plugin, successor);
    }

    @Override
    protected boolean meetsCondition(CommandSender sender, String label, String[] args) {
        return args.length == 0 || args[0].equalsIgnoreCase("help");
    }

    @Override
    protected void internalHandle(CommandSender sender, String label, String[] args) {
        this.messageSender.send(sender, "&6List of commands");
        this.messageSender.send(sender, "&f/"+label+" help");
        this.messageSender.send(sender, "&f/"+label+" add (player) (HP)");
        this.messageSender.send(sender, "&f/"+label+" checkAll");
        this.messageSender.send(sender, "&f/"+label+" check (name)");
        this.messageSender.send(sender, "&f/"+label+" clearAll");
        this.messageSender.send(sender, "&f/"+label+" clear (name)");
        this.messageSender.send(sender, "&f/"+label+" consumable give (player) (name)");
        this.messageSender.send(sender, "&f/"+label+" consumable set (name) (add/set) (amount)");
        this.messageSender.send(sender, "&f/"+label+" formulas <world>");
        this.messageSender.send(sender, "&f/"+label+" group set (group) (HP)");
        this.messageSender.send(sender, "&f/"+label+" gui");
        this.messageSender.send(sender, "&f/"+label+" info (player) <world>");
        this.messageSender.send(sender, "&f/"+label+" reload");
        this.messageSender.send(sender, "&f/"+label+" set (player) (HP)");
        this.messageSender.send(sender, "&f/"+label+" version");

    }
}
