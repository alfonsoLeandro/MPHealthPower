package com.github.alfonsoleandro.healthpower;

import com.github.alfonsoleandro.healthpower.commands.MainCommand;
import com.github.alfonsoleandro.healthpower.listeners.ConsumablesEvents;
import com.github.alfonsoleandro.healthpower.listeners.InventoryEvents;
import com.github.alfonsoleandro.healthpower.listeners.PlayerJoin;
import com.github.alfonsoleandro.healthpower.managers.ConsumableManager;
import com.github.alfonsoleandro.healthpower.managers.health.AbstractHPManager;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import com.github.alfonsoleandro.healthpower.managers.health.HPManagerLegacy;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.commands.MPTabCompleter;
import com.github.alfonsoleandro.mputils.files.YamlFile;
import com.github.alfonsoleandro.mputils.itemstacks.MPItemStacks;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import com.github.alfonsoleandro.mputils.metrics.bukkit.Metrics;
import com.github.alfonsoleandro.mputils.reloadable.ReloaderPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class HealthPower extends ReloaderPlugin {

    private final PluginDescriptionFile pdfFile = getDescription();
    private final String version = this.pdfFile.getVersion();
    private String latestVersion;
    private AbstractHPManager hpManager;
    private ConsumableManager consumableManager;
    private MessageSender<Message> messageSender;
    private Settings settings;
    private YamlFile configYaml;
    private YamlFile consumablesYaml;
    private YamlFile hpYaml;
    private YamlFile messagesYaml;
    private Economy econ = null;
    private Permission perms = null;

    @Override
    public void onEnable() {
        registerFiles();
        this.messageSender = new MessageSender<>(this, Message.values(), this.messagesYaml, "prefix");
        this.hpManager = (Integer.parseInt(getServer().getBukkitVersion().split("-")[0].replace(".", "-").split("-")[1]) < 9) ?
                new HPManagerLegacy(this) : new HPManager(this);
        this.consumableManager = new ConsumableManager(this);
        this.settings = new Settings(this);
        this.messageSender.send("&aEnabled&f. Version: &e" + this.version);
        this.messageSender.send("&fThank you for using my plugin! &c" + this.pdfFile.getName() + "&f By " + this.pdfFile.getAuthors().get(0));
        this.messageSender.send("&fJoin my discord server at &chttps://discordapp.com/invite/ZznhQud");
        this.messageSender.send("Please consider subscribing to my yt channel: &c" + this.pdfFile.getWebsite());
        if (setupEconomy()) {
            this.messageSender.send("&aPlugin Vault and economy found, economy hooked");
        } else {
            this.messageSender.send("&cPlugin Vault or an economy plugin not found, disabling economy");
        }
        if (setupPermissions()) {
            this.messageSender.send("&aPlugin Vault and a permissions plugin found, permissions hooked");
        } else {
            this.messageSender.send("&cPlugin Vault or a permissions plugin not found, disabling groups/permissions system");
        }
        if (this.configYaml.getAccess().contains("config.messages")) {
            this.messageSender.send("&c&lMessages have been moved from config to messages.yml. Make sure to re-personalize them!!!");
            this.messageSender.send("&c&lKeep in mind there are new messages too!");
        }
        checkAndCorrectConfig();
        registerCommands();
        registerEvents();
        updateChecker();
        startMetrics();
    }

    @Override
    public void onDisable() {
        this.messageSender.send("&cDisabled&f. Version: &e" + this.version);
        this.messageSender.send("&fThank you for using my plugin! &c" + this.pdfFile.getName() + "&f By " + this.pdfFile.getAuthors().get(0));
        this.messageSender.send("&fJoin my discord server at &chttps://discordapp.com/invite/ZznhQud");
        this.messageSender.send("Please consider subscribing to my yt channel: &c" + this.pdfFile.getWebsite());
    }


    private void startMetrics() {
        if (this.configYaml.getAccess().getBoolean("config.use metrics")) {
            new Metrics(this, 9480);
            this.messageSender.send("&aMetrics enabled! Thank you for keeping them enabled!");
        } else {
            this.messageSender.send("&cMetrics disabled :(. Please consider enabling metrics in config.");
        }
    }


    public boolean setupEconomy() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        this.econ = rsp.getProvider();
        return true;
    }

    public boolean setupPermissions() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return false;
        }
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return false;
        this.perms = rsp.getProvider();
        return true;
    }


    public Economy getEconomy() {
        return this.econ;
    }

    public Permission getPermissions() {
        return this.perms;
    }


    /**
     * Checks for updates in spigot.
     */
    private void updateChecker() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=78260").openConnection();
            final int timed_out = 1250;
            con.setConnectTimeout(timed_out);
            con.setReadTimeout(timed_out);
            this.latestVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            if (this.latestVersion.length() <= 7) {
                if (!this.version.equals(this.latestVersion)) {
                    String exclamation = "&e&l(&4&l!&e&l)";
                    this.messageSender.send(exclamation + " &cThere is a new version available &e(&7" + this.latestVersion + "&e)");
                    this.messageSender.send(exclamation + " &cDownload it here: &fhttps://bit.ly/3fqzRpR");
                }
            }
        } catch (Exception ex) {
            this.messageSender.send("&cError while checking for updates");
        }
    }


    /**
     * Gets the plugin's current version.
     *
     * @return The current version.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the plugin's latest available version on spigot.
     *
     * @return The latest available version.
     */
    public String getLatestVersion() {
        return this.latestVersion;
    }


    /**
     * Registers plugin files.
     */
    public void registerFiles() {
        this.configYaml = new YamlFile(this, "config.yml");
        this.hpYaml = new YamlFile(this, "HP.yml");
        this.messagesYaml = new YamlFile(this, "messages.yml");
        boolean consumablesFileExisted = new File(this.getDataFolder(), "consumables.yml").exists();
        this.consumablesYaml = new YamlFile(this, "consumables.yml");

        if (!consumablesFileExisted) {
            ItemStack specialPotion = MPItemStacks.newItemStack(Material.POTION, 1, "&aMax health modifier!", Arrays.asList("&cDrink this potion","&cto gain 2 extra hearts"));
            PotionMeta meta = (PotionMeta) specialPotion.getItemMeta();
            assert meta != null;

            if (Integer.parseInt(getServer().getBukkitVersion().split("-")[0].replace(".", "-").split("-")[1]) > 8) {
                meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
            } else {
                //noinspection deprecation (This is the only way to do it in 1.8)
                meta.setMainEffect(PotionEffectType.HEAL);
            }

            this.consumablesYaml.getAccess().set("consumables.example1.options.add", 2.0);
            this.consumablesYaml.getAccess().set("consumables.example1.item", specialPotion);
            this.consumablesYaml.save(false);
        }
    }

    /**
     * Reloads plugin files.
     */
    public void reloadFiles() {
        this.configYaml.loadFileConfiguration();
        this.hpYaml.loadFileConfiguration();
        this.messagesYaml.loadFileConfiguration();
        this.consumablesYaml.loadFileConfiguration();
    }

    @Override
    public void reload(boolean deep) {
        reloadFiles();
        super.reload(deep);
    }

    /**
     * Checks the config for new fields across versions.
     */
    private void checkAndCorrectConfig() {
        FileConfiguration configEndFile = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        FileConfiguration config = this.configYaml.getAccess();

        Map<String, Object> fields = new HashMap<>();

        fields.put("config.use permissions system", true);
        fields.put("config.consumables enabled", true);
        fields.put("config.use metrics", true);
        fields.put("config.HP cap.enabled", true);
        fields.put("config.HP cap.amount", 40);
        fields.put("config.update HP on join", true);

        boolean shouldSave = false;
        for (String field : fields.keySet()) {
            if (!configEndFile.contains(field)) {
                shouldSave = true;
                config.set(field, fields.get(field));
            }
        }

        if (shouldSave) {
            this.configYaml.save(false);
        }

    }


    /**
     * Registers the event listeners for this plugin.
     */
    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoin(this), this);
        pm.registerEvents(new InventoryEvents(this), this);
        pm.registerEvents(new ConsumablesEvents(this), this);
    }

    /**
     * Register this plugin's commands.
     */
    private void registerCommands() {
        PluginCommand mainCommand = getCommand("HealthPower");

        if (mainCommand == null) {
            this.messageSender.send("&cThe main command has not been registered properly. Disabling HealthPower");
            this.setEnabled(false);
            return;
        }

        //TODO: add consumables names list to tab completer, realod them on reload
        mainCommand.setExecutor(new MainCommand(this));
        mainCommand.setTabCompleter(new MPTabCompleter(Arrays.asList(
                "help",
                "version",
                "reload",
                "set {PLAYER} 20",
                "add {PLAYER} 20",
                "gui",
                "consumable give {PLAYER} {consumable_name}",
                "consumable set {consumable_name} add 20",
                "consumable set {consumable_name} set 20",
                "clear {PLAYER}",
                "clearAll",
                "check {PLAYER}",
                "checkAll"
        )));
    }


    public AbstractHPManager getHpManager() {
        return this.hpManager;
    }

    public ConsumableManager getConsumableManager() {
        return this.consumableManager;
    }

    public MessageSender<Message> getMessageSender() {
        return this.messageSender;
    }

    public Settings getSettings() {
        return this.settings;
    }


    @NotNull
    @Override
    public FileConfiguration getConfig() {
        return this.getConfigYaml().getAccess();
    }

    public YamlFile getConfigYaml() {
        return this.configYaml;
    }

    public YamlFile getHpYaml() {
        return this.hpYaml;
    }

    public YamlFile getConsumablesYaml() {
        return this.consumablesYaml;
    }
}