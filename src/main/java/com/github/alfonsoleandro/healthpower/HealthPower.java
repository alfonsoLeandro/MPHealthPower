package com.github.alfonsoleandro.healthpower;

import com.github.alfonsoleandro.healthpower.commands.MainCommand;
import com.github.alfonsoleandro.healthpower.listeners.*;
import com.github.alfonsoleandro.healthpower.managers.checking.PeriodicHPChecker;
import com.github.alfonsoleandro.healthpower.managers.consumable.ConsumableManager;
import com.github.alfonsoleandro.healthpower.managers.health.formula.cooldown.FormulaModifyManager;
import com.github.alfonsoleandro.healthpower.managers.shop.HPShopManager;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import com.github.alfonsoleandro.healthpower.managers.health.formula.gui.FormulaGUIManager;
import com.github.alfonsoleandro.healthpower.managers.health.formula.FormulaManager;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.commands.MPTabCompleter;
import com.github.alfonsoleandro.mputils.files.YamlFile;
import com.github.alfonsoleandro.mputils.itemstacks.MPItemStacks;
import com.github.alfonsoleandro.mputils.message.MessageSender;
import com.github.alfonsoleandro.mputils.metrics.bukkit.Metrics;
import com.github.alfonsoleandro.mputils.reloadable.ReloaderPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.mariuszgromada.math.mxparser.License;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class HealthPower extends ReloaderPlugin {

    private final PluginDescriptionFile pdfFile = getDescription();
    private final String version = this.pdfFile.getVersion();
    private Integer serverMajorVersion;
    private Integer serverMinorVersion;
    private String latestVersion;
    private HPManager hpManager;
    private FormulaManager formulaManager;
    private FormulaGUIManager formulaGUIManager;
    private FormulaModifyManager formulaModifyManager;
    private ConsumableManager consumableManager;
    private HPShopManager hpGUIManager;
    private MessageSender<Message> messageSender;
    private Settings settings;
    private YamlFile configYaml;
    private YamlFile consumablesYaml;
    private YamlFile formulasYaml;
    private YamlFile guiYaml;
    private YamlFile hpYaml;
    private YamlFile messagesYaml;
    private Economy econ = null;
    private Permission perms = null;

    @Override
    public void onEnable() {
        License.iConfirmNonCommercialUse("Leandro Alfonso");
        findVersion();
        registerFiles();
        this.messageSender = new MessageSender<>(this, Message.values(), this.messagesYaml, "prefix");
        this.settings = new Settings(this);
        this.formulaManager = new FormulaManager(this);
        this.hpManager = new HPManager(this);
        this.formulaGUIManager = new FormulaGUIManager(this);
        this.formulaModifyManager = new FormulaModifyManager(this);
        new PeriodicHPChecker(this);
        this.consumableManager = new ConsumableManager(this);
        this.messageSender.send("&aEnabled&f. Version: &e" + this.version);
        this.messageSender.send("&fThank you for using my plugin! &c" + this.pdfFile.getName() + "&f By " + this.pdfFile.getAuthors().getFirst());
        this.messageSender.send("&fJoin my discord server at &chttps://bit.ly/MPDiscordSv");
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
        this.hpGUIManager = new HPShopManager(this);
        checkAndCorrectConfig();
        checkAndCorrectMessages();
        registerCommands();
        registerEvents();
        registerLuckPermsEvents();
        updateChecker();
        startMetrics();
        checkAndCorrectAllPlayersHp();
    }

    @Override
    public void onDisable() {
        this.messageSender.send("&cDisabled&f. Version: &e" + this.version);
        this.messageSender.send("&fThank you for using my plugin! &c" + this.pdfFile.getName() + "&f By " + this.pdfFile.getAuthors().getFirst());
        this.messageSender.send("&fJoin my discord server at &chttps://bit.ly/MPDiscordSv");
        this.messageSender.send("Please consider subscribing to my yt channel: &c" + this.pdfFile.getWebsite());
    }


    private void findVersion() {
        String[] version = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        try {
            this.serverMajorVersion = Integer.parseInt(version[1]);
            if (version.length > 2) {
                this.serverMinorVersion = Integer.parseInt(version[2]);
            } else {
                this.serverMinorVersion = 0;
            }
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage("There's been an error while trying to check the server's version.");
        }
    }

    private void startMetrics() {
        if (this.configYaml.getAccess().getBoolean("config.use metrics")) {
            new Metrics(this, 9480);
            this.messageSender.send("&aMetrics enabled! Thank you for keeping them enabled!");
        } else {
            this.messageSender.send("&cMetrics disabled :(. Please consider enabling metrics in config.");
        }
    }

    private void checkAndCorrectAllPlayersHp() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.hpManager.checkAndCorrectHP(player);
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
            HttpURLConnection con = (HttpURLConnection) new URI(
                    "https://api.spigotmc.org/legacy/update.php?resource=78260")
                    .toURL().openConnection();
            final int timed_out = 1250;
            con.setConnectTimeout(timed_out);
            con.setReadTimeout(timed_out);
            this.latestVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            if (this.latestVersion.length() <= 7) {
                if (!this.version.equals(this.latestVersion)) {
                    String exclamation = "&e&l(&4&l!&e&l)";
                    this.messageSender.send(exclamation + " &cThere is a new version available &e(&7" + this.latestVersion + "&e)");
                    this.messageSender.send(exclamation + " &cDownload it here: &fhttps://bit.ly/hpUpdate");
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
        this.formulasYaml = new YamlFile(this, "formulas.yml");
        this.guiYaml = new YamlFile(this, "gui.yml");
        this.hpYaml = new YamlFile(this, "HP.yml");
        this.messagesYaml = new YamlFile(this, "messages.yml");
        boolean consumablesFileExisted = new File(this.getDataFolder(), "consumables.yml").exists();
        this.consumablesYaml = new YamlFile(this, "consumables.yml");

        if (!consumablesFileExisted) {
            ItemStack specialPotion = MPItemStacks.newItemStack(Material.POTION, 1, "&aMax health modifier!", Arrays.asList("&cDrink this potion", "&cto gain 2 extra hearts"));
            PotionMeta meta = (PotionMeta) specialPotion.getItemMeta();
            assert meta != null;

            if (this.serverMajorVersion >= 20 && this.serverMinorVersion >= 2) {
                meta.setBasePotionType(PotionType.INSTANT_HEAL);
            } else {
                //noinspection deprecation (deprecated in 1.20.2, still used since 1.9)
                meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
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
        this.formulasYaml.loadFileConfiguration();
        this.guiYaml.loadFileConfiguration();
        this.messagesYaml.loadFileConfiguration();
        this.consumablesYaml.loadFileConfiguration();
    }

    @Override
    public void reload(boolean deep) {
        reloadFiles();
        super.reload(deep);
        setTabCompleter();
    }

    private void setTabCompleter() {
        Set<String> consumablesNames = this.consumableManager.getConsumablesNames();
        List<String> possibilities = new ArrayList<>(Arrays.asList(
                "help",
                "version",
                "reload",
                "modify add {PLAYERS} base 20",
                "modify add {PLAYERS} shop 20",
                "modify set {PLAYERS} base 20",
                "modify set {PLAYERS} shop 20",
                "gui",
                "clear {PLAYERS}",
                "clearAll",
                "check {PLAYERS}",
                "checkAll",
                "info {PLAYERS} {WORLDS}",
                "formulas {WORLDS}"
        ));
        if (consumablesNames.isEmpty()) {
            possibilities.add("consumable give {PLAYERS} {consumable_name}");
            possibilities.add("consumable set {consumable_name} add 20");
            possibilities.add("consumable set {consumable_name} set 20");
        } else {
            for (String consumableName : consumablesNames) {
                possibilities.add("consumable give {PLAYERS} " + consumableName);
                possibilities.add("consumable set " + consumableName + " add 20");
                possibilities.add("consumable set " + consumableName + " set 20");
            }
        }
        if (this.perms == null) {
            possibilities.add("group set {group_name} 1");
            possibilities.add("group set {group_name} 5");
            possibilities.add("group set {group_name} 10");
            possibilities.add("group set {group_name} 20");
        } else {
            for (String group : this.perms.getGroups()) {
                possibilities.add("group set " + group + " 1");
                possibilities.add("group set " + group + " 5");
                possibilities.add("group set " + group + " 10");
                possibilities.add("group set " + group + " 20");
            }
        }
        if (this.settings.isShopGUIEnabled()) {
            possibilities.add("shop");
        }

        Objects.requireNonNull(getCommand("HealthPower"))
                .setTabCompleter(new MPTabCompleter(possibilities, "HealthPower.tabcomplete"));
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
     * Checks the messages file for missing messages.
     */
    private void checkAndCorrectMessages() {
        FileConfiguration messagesEndFile = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        FileConfiguration messages = this.messagesYaml.getAccess();

        boolean shouldSave = false;
        for (Message message : Message.values()) {
            if (!messagesEndFile.contains(message.getPath())) {
                Bukkit.broadcastMessage("Message no contenido: " + message.name() + " path: " +  message.getPath());
                shouldSave = true;
                messages.set(message.getPath(), message.getDefault());
            }
        }

        if (shouldSave) {
            this.messagesYaml.save(false);
        }
    }


    /**
     * Registers the event listeners for this plugin.
     */
    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ConsumablesListener(this), this);
        pm.registerEvents(new FormulasChatListener(this), this);
        pm.registerEvents(new FormulasGUIListener(this), this);
        pm.registerEvents(new NavigableGUIClickListener(), this);
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerChangeWorldListener(this), this);
        pm.registerEvents(new ShopGUIClickListener(this), this);
    }

    /**
     * Registers the LuckPerms event listeners for this plugin.
     */
    private void registerLuckPermsEvents() {
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            LuckPerms api = LuckPermsProvider.get();
            new LuckPermsListener(this, api);
            this.messageSender.send("&aPlugin LuckPerms found, events hooked");
        }
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
        mainCommand.setExecutor(new MainCommand(this));
        setTabCompleter();
    }

    public HPManager getHpManager() {
        return this.hpManager;
    }

    public FormulaManager getFormulaManager() {
        return this.formulaManager;
    }

    public FormulaGUIManager getFormulaGUIManager() {
        return this.formulaGUIManager;
    }

    public FormulaModifyManager getFormulaModifyManager() {
        return this.formulaModifyManager;
    }

    public ConsumableManager getConsumableManager() {
        return this.consumableManager;
    }

    public HPShopManager getHpGUIManager() {
        return this.hpGUIManager;
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

    public YamlFile getFormulasYaml() {
        return this.formulasYaml;
    }

    public YamlFile getGuiYaml() {
        return this.guiYaml;
    }

    public YamlFile getHpYaml() {
        return this.hpYaml;
    }

    public YamlFile getConsumablesYaml() {
        return this.consumablesYaml;
    }
}