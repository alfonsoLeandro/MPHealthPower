package com.github.alfonsoleandro.healthpower.managers.shop;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.guis.GUI;
import com.github.alfonsoleandro.mputils.guis.SimpleGUI;
import com.github.alfonsoleandro.mputils.guis.utils.GUIType;
import com.github.alfonsoleandro.mputils.itemstacks.MPItemStacks;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import com.github.alfonsoleandro.mputils.reloadable.Reloadable;
import com.github.alfonsoleandro.mputils.string.StringUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.mariuszgromada.math.mxparser.Expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author alfonsoLeandro
 */
public class HPShopManager extends Reloadable {

    private final Map<String, String> formulas = new HashMap<>();
    private final HealthPower plugin;
    private final MessageSender<Message> messageSender;
    private final HPManager hpManager;
    private final Settings settings;
    private final Economy economy;
    private String shopGUITitle;
    private int shopGUISize;
    private HPShopItem[] items;

    public HPShopManager(HealthPower plugin) {
        super(plugin);
        this.plugin = plugin;
        this.messageSender = plugin.getMessageSender();
        this.hpManager = plugin.getHpManager();
        this.settings = plugin.getSettings();
        this.economy = plugin.getEconomy();
        loadGUI();
    }

    private void loadGUI() {
        FileConfiguration config = this.plugin.getConfigYaml().getAccess();

        List<String> formulas = config.getStringList("config.GUI.formulas");
        for (int i = 0; i < formulas.size(); i++) {
            String formula = formulas.get(i);
            this.formulas.put("%formula_" + i + "%", formula);
        }

        this.shopGUITitle = StringUtils.colorizeString(config.getString("config.GUI.title"));
        this.shopGUISize = GUI.getValidSize(config.getInt("config.GUI.size"), GUIType.SIMPLE);


        this.items = new HPShopItem[this.shopGUISize];

        for (int i = 0; i < this.shopGUISize; i++) {
            if (config.contains("config.GUI.items." + i)) {
                ConfigurationSection section = config.getConfigurationSection("config.GUI.items." + i);
                assert section != null;
                String configMaterial = section.getString("material");
                String name = section.getString("name");
                List<String> lore = section.getStringList("lore");
                String configType = section.getString("type");
                String priceOrFormula = section.getString("price");
                double amount = section.getDouble("amount");

                if (configMaterial == null || name == null || configType == null || priceOrFormula == null || amount < 0) {
                    this.messageSender.send(Bukkit.getConsoleSender(), Message.INVALID_GUI_ITEM,
                            "%slot%", String.valueOf(i));
                    continue;
                }

                HPShopItem.HPShopItemType type;
                Material material;

                try {
                    type = HPShopItem.HPShopItemType.valueOf(configType.toUpperCase());
                    material = Material.valueOf(configMaterial.toUpperCase());
                } catch (IllegalArgumentException e) {
                    this.messageSender.send(Bukkit.getConsoleSender(), Message.INVALID_GUI_ITEM,
                            "%slot%", String.valueOf(i));
                    continue;
                }

                this.items[i] = new HPShopItem(
                        MPItemStacks.newItemStack(material, 1, name, lore),
                        type,
                        priceOrFormula,
                        amount
                );

            }
        }
    }

    public void openGUI(Player player) {
        if (!this.settings.isShopGUIEnabled()) {
            return;
        }
        SimpleGUI shopGUI = new SimpleGUI(this.shopGUITitle, this.shopGUISize, Settings.SHOP_GUI_TAG);
        double balance = this.economy.getBalance(player);
        double health = this.hpManager.getHealth(player);

        for (int i = 0; i < this.shopGUISize; i++) {
            if (this.items[i] != null) {
                double price = getPrice(i, health);
                shopGUI.setItem(i, MPItemStacks.replacePlaceholders(this.items[i].itemStack().clone(),
                        new HashMap<>() {{
                            put("%price%", String.valueOf(price));
                            put("%affordable%", StringUtils.colorizeString(HPShopManager.this.messageSender.getString(price > balance ? Message.NO : Message.YES)));
                            put("%name%", player.getName());
                            put("%balance%", String.valueOf(balance));
                            put("%HP%", String.valueOf(health));
                        }}
                ));
            }
        }

        shopGUI.openGUI(player);
    }

    public HPShopItem getItem(int slot) {
        if (this.items.length < slot) return null;
        return this.items[slot];
    }

    public double getPrice(int slot, double currentHP) {
        if (this.items.length < slot) return Double.MAX_VALUE;
        HPShopItem item = this.items[slot];

        if (item == null) return Double.MAX_VALUE;

        String formula = this.formulas.get(item.priceOrFormula());
        if (formula != null) {
            Expression e = new Expression(formula.replace("%HP%", String.valueOf(currentHP)));
            return e.calculate();
        }
        try {
            return Double.parseDouble(item.priceOrFormula());
        } catch (NumberFormatException e) {
            return Double.MAX_VALUE;
        }
    }

    @Override
    public void reload(boolean deep) {
        this.formulas.clear();
        loadGUI();
    }
}
