package com.github.alfonsoleandro.healthpower.managers;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.utils.Consumable;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import com.github.alfonsoleandro.mputils.reloadable.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author alfonsoLeandro
 */
public class ConsumableManager extends Reloadable {

    private final HealthPower plugin;

    private final MessageSender<Message> messageSender;

    private final NamespacedKey namespacedKey;

    private final Map<String, Consumable> consumablesByName = new HashMap<>();

    public ConsumableManager(HealthPower plugin) {
        super(plugin);
        this.plugin = plugin;
        this.namespacedKey = new NamespacedKey(this.plugin, "consumable");
        this.messageSender = plugin.getMessageSender();
        loadConsumables();
    }

    private void loadConsumables() {
        ConfigurationSection consumables = this.plugin.getConsumablesYaml().getAccess().getConfigurationSection("consumables");
        if (consumables == null) {
            return;
        }

        for (String consumableName : consumables.getKeys(false)) {
            Consumable.ConsumableMode mode;
            double amount;

            if (consumables.isSet(consumableName + ".options.add")) {
                mode = Consumable.ConsumableMode.ADD;
                amount = consumables.getDouble(consumableName + ".options.add");
            } else if (consumables.isSet(consumableName + ".options.set")) {
                mode = Consumable.ConsumableMode.SET;
                amount = consumables.getDouble(consumableName + ".options.set");
            } else {
                this.messageSender.send(Bukkit.getConsoleSender(), Message.INVALID_CONSUMABLE, "%name%", consumableName);
                continue;
            }

            ItemStack consumableItem = consumables.getItemStack(consumableName + ".item");
            boolean valid = addConsumable(consumableName, mode, amount, consumables.getString(consumableName + "options.message"), consumableItem);

            if (!valid) {
                this.messageSender.send(Bukkit.getConsoleSender(), Message.INVALID_CONSUMABLE_ITEM, "%name%", consumableName);
            }
        }

    }

    public boolean addConsumable(String name, Consumable.ConsumableMode mode, double amount, String message, ItemStack item) {
        if (!isValidConsumable(item)) {
            return false;
        }

        PersistentDataContainer persistentDataContainer = Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer();
        persistentDataContainer.set(this.namespacedKey, PersistentDataType.BYTE, (byte) 1);


        Consumable consumable = new Consumable(name, mode, amount, message, item);
        this.consumablesByName.put(name, consumable);
        return true;
    }

    public boolean isValidConsumable(ItemStack item) {
        return item != null && !item.getType().equals(Material.AIR) && (item.getType().isEdible() || item.getType().equals(Material.POTION));
    }

    public Consumable getConsumable(String name) {
        return this.consumablesByName.get(name);
    }

    public NamespacedKey getNamespacedKey() {
        return this.namespacedKey;
    }

    public Set<String> getConsumablesNames() {
        return this.consumablesByName.keySet();
    }

    @Override
    public void reload(boolean deep) {
        this.consumablesByName.clear();
        loadConsumables();
    }
}
