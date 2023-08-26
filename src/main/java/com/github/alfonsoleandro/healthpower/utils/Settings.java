package com.github.alfonsoleandro.healthpower.utils;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.AbstractHPManager;
import com.github.alfonsoleandro.mputils.guis.SimpleGUI;
import com.github.alfonsoleandro.mputils.itemstacks.MPItemStacks;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import com.github.alfonsoleandro.mputils.reloadable.Reloadable;
import com.github.alfonsoleandro.mputils.string.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mariuszgromada.math.mxparser.Expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings extends Reloadable {

    private final HealthPower plugin;
    private final MessageSender<Message> messageSender;
    // Fields
    private boolean shopGUIEnabled;

    private int shopGuiSize;

    private String shopGUITitle;

    private Map<Integer, ItemStack> shopGUIItems;
    private Map<Integer, String> shopGUIItemsPrices;

    public Settings(HealthPower plugin) {
        super(plugin);
        this.plugin = plugin;
        this.messageSender = plugin.getMessageSender();
    }

    private void loadFields() {
        FileConfiguration config = this.plugin.getConfigYaml().getAccess();

        this.shopGUIEnabled = config.getBoolean("config.GUI.enabled");

        this.shopGuiSize = Math.min(54, Math.max(9, config.getInt("config.GUI.size")));

        this.shopGUITitle = StringUtils.colorizeString(config.getString("config.GUI.title"));

        this.shopGUIItems = new HashMap<>();

        this.shopGUIItemsPrices = new HashMap<>();

        for (int i = 0; i < this.shopGuiSize; i++) {
            if(config.contains("config.GUI.items." + i)) {
                ItemStack item = new ItemStack(Material.valueOf(config.getString("config.GUI.items."+i+".material")));
                ItemMeta meta = item.getItemMeta();
                if(meta == null) continue;

                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                        config.getString("config.GUI.items."+i+".name", "")));
                meta.setLore(config.getStringList("config.GUI.items."+i+".lore"));
                item.setItemMeta(meta);
                this.shopGUIItems.put(i, item);
                this.shopGUIItemsPrices.put(i, config.getString("config.GUI.items."+i+".price"));
            }
        }

    }


    @Override
    public void reload(boolean deep) {
        this.loadFields();
    }

    public void openShopGUI(Player player){
        SimpleGUI shopGUI = new SimpleGUI(this.shopGUITitle,
                this.shopGuiSize,
                "MPHealthPower:SHOP");

        AbstractHPManager hpManager = this.plugin.getHpManager();

        double balance = this.plugin.getEconomy().getBalance(player);
        double health = hpManager.getHealth(player);

        for (int i = 0; i < this.shopGuiSize; i++) {
            double price = this.calculatePrice(this.shopGUIItemsPrices.get(i), health);
            if (this.shopGUIItems.containsKey(i)) shopGUI.setItem(i,
                    MPItemStacks.replacePlaceholders(this.shopGUIItems.get(i).clone(),
                            new HashMap<String,String>(){{
                                put("%price%", String.valueOf(price));
                                put("%affordable%", Settings.this.messageSender.getString(price > balance ?
                                        Message.NO : Message.YES));
                                put("%name%", player.getName());
                                put("%balance%", String.valueOf(balance));
                                put("%HP%", String.valueOf(health));
                            }}));
        }

        shopGUI.openGUI(player);
    }

    public double calculatePrice(String price, double HP){
        if(price == null) return 999999999999999.0;

        if(price.contains("%formula_")){
            List<String> formulas = this.plugin.getConfigYaml().getAccess().getStringList("config.GUI.formulas");
            int index = Integer.parseInt(price.replace("%formula_", "").replace("%", ""));
            String formula = formulas.get(index).replace("%HP%", String.valueOf(HP));
            Expression e = new Expression(formula);
            return e.calculate();

        }else {
            try {
                return Double.parseDouble(price);
            } catch (NumberFormatException ex) {
                this.messageSender.send("&cThere was an error while calculating a price");
                return 999999999999999.0;
            }
        }
    }

    public boolean isShopGUIEnabled() {
        return this.shopGUIEnabled;
    }
}