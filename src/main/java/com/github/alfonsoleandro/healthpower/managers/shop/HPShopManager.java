package com.github.alfonsoleandro.healthpower.managers.shop;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.formula.Formula;
import com.github.alfonsoleandro.healthpower.managers.health.formula.FormulaManager;
import com.github.alfonsoleandro.healthpower.managers.health.formula.FormulaVariable;
import com.github.alfonsoleandro.healthpower.managers.health.formula.PlayerHpData;
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
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author alfonsoLeandro
 */
public class HPShopManager extends Reloadable {

    private final HealthPower plugin;
    private final MessageSender<Message> messageSender;
    private final FormulaManager formulaManager;
    private final Settings settings;
    private final Economy economy;
    private String shopGUITitle;
    private int shopGUISize;
    private double defaultFormulaValue;
    private HPShopItem[] items;

    public HPShopManager(HealthPower plugin) {
        super(plugin);
        this.plugin = plugin;
        this.messageSender = plugin.getMessageSender();
        this.formulaManager = plugin.getFormulaManager();
        this.settings = plugin.getSettings();
        this.economy = plugin.getEconomy();
        loadSettings();
    }

    private void loadSettings() {
        ConfigurationSection shopGuiSettings = this.plugin.getGuiYaml().getAccess().getConfigurationSection("GUI.shop");

        if (shopGuiSettings == null) {
            throw new RuntimeException("GUI.shop not found");
        }

        this.shopGUITitle = StringUtils.colorizeString(shopGuiSettings.getString("title"));
        this.shopGUISize = GUI.getValidSize(shopGuiSettings.getInt("size"), GUIType.SIMPLE);

        this.defaultFormulaValue = shopGuiSettings.getDouble("default formula value");

        this.items = new HPShopItem[this.shopGUISize];

        for (int i = 0; i < this.shopGUISize; i++) {
            int slot = i;
            ConfigurationSection itemSection = shopGuiSettings.getConfigurationSection("items." + slot);
            if (itemSection != null) {
                String configMaterial = itemSection.getString("material");
                String name = itemSection.getString("name");
                List<String> lore = itemSection.getStringList("lore");
                String configType = itemSection.getString("type");
                double amount = itemSection.getDouble("amount");
                Double price = itemSection.contains("price") ? itemSection.getDouble("price") : null;
                String node = itemSection.getString("node");
                String message = itemSection.getString("message");

                if (configMaterial == null || name == null || configType == null || amount < 0) {
                    this.messageSender.send(Bukkit.getConsoleSender(), Message.INVALID_GUI_ITEM,
                            "%slot%", String.valueOf(slot));
                    continue;
                }

                HPShopItem.HPShopItemType type;
                Material material;

                try {
                    type = HPShopItem.HPShopItemType.valueOf(configType.toUpperCase());
                    material = Material.valueOf(configMaterial.toUpperCase());
                } catch (IllegalArgumentException e) {
                    this.messageSender.send(Bukkit.getConsoleSender(), Message.INVALID_GUI_ITEM,
                            "%slot%", String.valueOf(slot));
                    continue;
                }

                List<String> formulasStrings = itemSection.getStringList("formulas");
                // Load formulas
                List<Formula> formulas = formulasStrings.stream()
                        .map(Formula::new)
                        .filter(f -> {
                            if (f.isValid()) {
                                if (this.settings.isDebug()) {
                                    this.messageSender.send(Bukkit.getConsoleSender(),
                                            Message.FORMULA_VALID_SHOP,
                                            "%formula%", f.getRawFormulaString(),
                                            "%slot%", String.valueOf(slot));
                                }
                                return true;
                            }
                            this.messageSender.send(Bukkit.getConsoleSender(),
                                    Message.FORMULA_INVALID_SHOP,
                                    "%formula%", f.getRawFormulaString(),
                                    "%slot%", String.valueOf(slot));
                            return false;
                        }).collect(Collectors.toCollection(ArrayList::new));

                if (formulas.isEmpty() && price == null) {
                    this.messageSender.send(Bukkit.getConsoleSender(), Message.INVALID_GUI_ITEM_PRICE,
                            "%slot%", String.valueOf(slot));
                    continue;
                }

                ConfigurationSection requirementsSection = shopGuiSettings.getConfigurationSection("requirements");
                Map<FormulaVariable, HPShopItem.HPShopItemRequirementValues> requirements = new HashMap<>();
                if (requirementsSection != null) {
                    for (FormulaVariable variable : FormulaVariable.values()) {
                        String raw = requirementsSection.getString(variable.name().toLowerCase());
                        if (raw == null) {
                            continue;
                        }
                        String[] values = raw.split(",");
                        try {
                            requirements.put(variable, new HPShopItem.HPShopItemRequirementValues(
                                    values[0] == null ? null : Double.parseDouble(values[0]),
                                    values[1] == null ? null : Double.parseDouble(values[1]))
                            );
                        } catch (NumberFormatException e) {
                            this.messageSender.send(Bukkit.getConsoleSender(), Message.SHOP_ITEM_INVALID_REQUIREMENT,
                                    "%slot%", String.valueOf(i),
                                    "%type%", variable.name().toLowerCase(),
                                    "%received%", raw);
                        }
                    }
                }

                this.items[slot] = new HPShopItem(
                        MPItemStacks.newItemStack(material, 1, name, lore),
                        type,
                        price,
                        amount,
                        node,
                        formulas,
                        message,
                        requirements
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
        PlayerHpData playerHpData = this.formulaManager.getPlayerHpData(player);

        for (int i = 0; i < this.shopGUISize; i++) {
            int slot = i;
            if (this.items[slot] != null) {
                HPShopItem item = this.items[slot];
                double price = getPriceForItem(player, slot);
                shopGUI.setItem(slot, MPItemStacks.replacePlaceholders(item.itemStack().clone(),
                        new HashMap<>() {{
                            put("%price%", String.valueOf(price));
                            put("%affordable%", StringUtils.colorizeString(HPShopManager.this.messageSender.getString(price > balance ? Message.NO : Message.YES)));
                            put("%name%", player.getName());
                            put("%balance%", String.valueOf(balance));
                            put("%base_hp%", String.valueOf(playerHpData.baseHp() == null ? 0 : playerHpData.baseHp()));
                            put("%shop_hp%", String.valueOf(playerHpData.shopHp() == null ? 0 : playerHpData.shopHp()));
                            put("%group_hp%", String.valueOf(playerHpData.groupHp() == null ? 0 : playerHpData.groupHp()));
                            put("%permission_hp%", String.valueOf(playerHpData.permissionHp() == null ? 0 : playerHpData.permissionHp()));
                            put("%has_permission%", StringUtils.colorizeString(HPShopManager.this.messageSender.getString(hasPermissionForItem(player, slot) ? Message.YES : Message.NO)));
                            put("%meets_requirements%", StringUtils.colorizeString(HPShopManager.this.messageSender.getString(meetsRequirementForItem(player, slot) ? Message.YES : Message.NO)));
                        }}
                ));
            }
        }

        shopGUI.openGUI(player);
    }

    public HPShopItem getItem(int slot) {
        if (slot >= this.items.length) return null;
        return this.items[slot];
    }

    public boolean hasPermissionForItem(Player player, int slot) {
        if (slot >= this.items.length) return true;
        HPShopItem item = this.items[slot];
        if (item == null || item.node() == null) {
            return true;
        }
        return player.hasPermission(item.node()) ||
                player.hasPermission("healthpower.shop.item.*");
    }

    public boolean meetsRequirementForItem(Player player, int slot) {
        if (slot >= this.items.length) return true;
        HPShopItem item = this.items[slot];
        if (item == null || item.requirements().isEmpty()) {
            return true;
        }
        PlayerHpData playerHpData = this.formulaManager.getPlayerHpData(player);
        for (FormulaVariable variable : FormulaVariable.values()) {
            if (!item.meetsRequirement(variable, playerHpData)) {
                return false;
            }
        }

        return true;
    }

    public double getPriceForItem(Player player, int slot) {
        if (this.items.length < slot) return Double.MAX_VALUE;
        HPShopItem item = this.items[slot];

        if (item == null) return Double.MAX_VALUE;

        if (item.price() != null) {
            return item.price();
        }

        List<Formula> formulas = item.formulas();
        if (formulas != null) {
            PlayerHpData playerHpData = this.formulaManager.getPlayerHpData(player);
            for (Formula formula : formulas) {
                if (formula.canApply(playerHpData)) {
                    return formula.calculate(playerHpData, this.defaultFormulaValue);
                }
            }
            // Use last formula in the list if none is fully applicable
            return formulas.getLast().calculate(playerHpData, this.defaultFormulaValue);
        }

        // Should not happen
        return Double.MAX_VALUE;
    }

    @Override
    public void reload(boolean deep) {
        loadSettings();
    }
}
