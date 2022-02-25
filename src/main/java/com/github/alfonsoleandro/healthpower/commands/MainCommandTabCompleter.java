package com.github.alfonsoleandro.healthpower.commands;

import com.github.alfonsoleandro.healthpower.HealthPower;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainCommandTabCompleter implements TabCompleter {

    final private HealthPower plugin;

    public MainCommandTabCompleter(HealthPower plugin){
        this.plugin = plugin;
    }

    public boolean equalsToStringUnCompleted(String input, String string){
        return input.equalsIgnoreCase(string.substring(0, Math.min(string.length(), input.length())));
    }


    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        final List<String> list = new ArrayList<>();

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("")){
                list.add("help");
                list.add("version");
                list.add("reload");
                list.add("set");
                list.add("add");
                list.add("gui");
                list.add("consumable");
                list.add("clear");
                list.add("clearAll");
                list.add("check");
                list.add("checkAll");

            } else if(equalsToStringUnCompleted(args[0], "help")) {
                list.add("help");

            } else if(equalsToStringUnCompleted(args[0], "version")) {
                list.add("version");

            } else if(equalsToStringUnCompleted(args[0], "reload")) {
                list.add("reload");

            } else if(equalsToStringUnCompleted(args[0], "set")) {
                list.add("set");

            } else if(equalsToStringUnCompleted(args[0], "add")) {
                list.add("add");

            } else if(equalsToStringUnCompleted(args[0], "gui")) {
                list.add("gui");

            }else if(equalsToStringUnCompleted(args[0], "c")) {
                list.add("consumable");
                list.add("clear");
                list.add("clearAll");
                list.add("check");
                list.add("checkAll");

            }else if(equalsToStringUnCompleted(args[0], "clear")){
                list.add("clear");
                list.add("clearAll");

            }else if(equalsToStringUnCompleted(args[0], "check")) {
                list.add("check");
                list.add("checkAll");

            }else if(equalsToStringUnCompleted(args[0], "clearAll")){
                list.add("clearAll");

            }else if(equalsToStringUnCompleted(args[0], "checkAll")){
                list.add("checkAll");


            } else if(equalsToStringUnCompleted(args[0], "consumable")) {
                list.add("consumable");
            }


        }else if(args.length == 2){
            if(args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add")){
                return null;

            }else if(args[0].equalsIgnoreCase("consumable")){
                list.add("get");
                list.add("set");

            }else if(args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("clear")){
                Bukkit.getOnlinePlayers().forEach(p -> list.add(p.getName()));
            }

        }else if(args.length == 3){
            if(args[0].equalsIgnoreCase("consumable")){
                if(args[1].equalsIgnoreCase("get")) {
                    ConfigurationSection consumablesSection = this.plugin.getConsumablesYaml().getAccess().getConfigurationSection("consumables");
                    if(consumablesSection != null)
                        list.addAll(consumablesSection.getKeys(false));
                }

            }

        }else if(args.length == 4){
            if(args[0].equalsIgnoreCase("consumable") && args[1].equalsIgnoreCase("set")){
                list.add("add");
                list.add("set");
            }
        }


        return list;
    }


}
