package com.github.alfonsoleandro.healthpower;

import com.github.alfonsoleandro.healthpower.commands.MainCommand;
import com.github.alfonsoleandro.healthpower.commands.MainCommandTabCompleter;
import com.github.alfonsoleandro.healthpower.listeners.ConsumablesEvents;
import com.github.alfonsoleandro.healthpower.listeners.InventoryEvents;
import com.github.alfonsoleandro.healthpower.listeners.PlayerJoin;
import com.github.alfonsoleandro.healthpower.managers.AbstractHPManager;
import com.github.alfonsoleandro.healthpower.managers.HPManager;
import com.github.alfonsoleandro.healthpower.managers.HPManagerLegacy;
import com.github.alfonsoleandro.healthpower.utils.Message;
import com.github.alfonsoleandro.mputils.files.YamlFile;
import com.github.alfonsoleandro.mputils.managers.MessageSender;
import com.github.alfonsoleandro.mputils.metrics.Metrics;
import com.github.alfonsoleandro.mputils.reloadable.ReloaderPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.mariuszgromada.math.mxparser.Expression;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public final class HealthPower extends ReloaderPlugin {

    private final PluginDescriptionFile pdfFile = getDescription();
    private final String version = this.pdfFile.getVersion();
    private String latestVersion;
    private Economy econ = null;
    private Permission perms = null;
    private MessageSender<Message> messageSender;
    private AbstractHPManager hpManager;
    private YamlFile configYaml;
    private YamlFile hpYaml;
    private YamlFile consumablesYaml;

    @Override
    public void onEnable() {
        registerFiles();
        this.messageSender = new MessageSender<>(this, Message.values(), this.configYaml, "config.prefix");
        this.hpManager = (Integer.parseInt(getServer().getBukkitVersion().split("-")[0].replace(".", "-").split("-")[1]) < 9) ?
                new HPManagerLegacy(this) : new HPManager(this);
        this.messageSender.send("&aEnabled&f. Version: &e" + this.version);
        this.messageSender.send("&fThank you for using my plugin! &c" + this.pdfFile.getName() + "&f By " + this.pdfFile.getAuthors().get(0));
        this.messageSender.send("&fJoin my discord server at &chttps://discordapp.com/invite/ZznhQud");
        this.messageSender.send("Please consider subscribing to my yt channel: &c" + this.pdfFile.getWebsite());
        if(setupEconomy()){
            this.messageSender.send("&aPlugin Vault and economy found, economy hooked");
        }else {
            this.messageSender.send("&cPlugin Vault or an economy plugin not found, disabling economy");
        }
        if(setupPermissions()){
            this.messageSender.send("&aPlugin Vault and a permissions plugin found, permissions hooked");
        }else {
            this.messageSender.send("&cPlugin Vault or a permissions plugin not found, disabling groups/permissions system");
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




    private void startMetrics(){
        if(getConfig().getBoolean("config.use metrics")){
            new Metrics(this, 9480);
            this.messageSender.send("&aMetrics enabled! Thank you for keeping them enabled!");
        }else{
            this.messageSender.send("&cMetrics disabled :(. Please consider enabling metrics in config.");
        }
    }



    public boolean setupEconomy() {
        if(!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp==null) {
            return false;
        }
        this.econ = rsp.getProvider();
        return true;
    }

    public boolean setupPermissions() {
        if(!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return false;
        }
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if(rsp == null) return false;
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
    private void updateChecker(){
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=78260").openConnection();
            final int timed_out = 1250;
            con.setConnectTimeout(timed_out);
            con.setReadTimeout(timed_out);
            this.latestVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            if (this.latestVersion.length() <= 7) {
                if(!this.version.equals(this.latestVersion)){
                    String exclamation = "&e&l(&4&l!&e&l)";
                    this.messageSender.send(exclamation +" &cThere is a new version available &e(&7"+ this.latestVersion +"&e)");
                    this.messageSender.send(exclamation +" &cDownload it here: &fhttps://bit.ly/3fqzRpR");
                }
            }
        } catch (Exception ex) {
            this.messageSender.send("&cError while checking for updates");
        }
    }


    /**
     * Gets the plugin's current version.
     * @return The current version.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the plugin's latest available version on spigot.
     * @return The latest available version.
     */
    public String getLatestVersion() {
        return this.latestVersion;
    }


    /**
     * Registers plugin files.
     */
    @SuppressWarnings("deprecation") //legacy versions support
    public void registerFiles(){
        this.configYaml = new YamlFile(this, "config.yml");
        this.hpYaml = new YamlFile(this, "HP.yml");
        boolean consumablesFileExisted = new File(this.getDataFolder(), "consumables.yml").exists();
        this.consumablesYaml = new YamlFile(this, "consumables.yml");
        if(!consumablesFileExisted){
            ItemStack specialPotion = new ItemStack(Material.POTION);
            PotionMeta meta = (PotionMeta) specialPotion.getItemMeta();
            assert meta != null;

            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aMax health modifier!"));
            meta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&cDrink this potion, to gain 2 extra hearts").split(",")));
            if(Integer.parseInt(getServer().getBukkitVersion().split("-")[0].replace(".", "-").split("-")[1]) > 8) {
                meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
            }else{
                meta.setMainEffect(PotionEffectType.HEAL);
            }

            this.consumablesYaml.getAccess().set("consumables.example1.item", specialPotion);
            this.consumablesYaml.save(false);
        }
    }

    /**
     * Reloads plugin files.
     */
    public void reloadFiles(){
        this.configYaml.loadFileConfiguration();
        this.hpYaml.loadFileConfiguration();
        this.consumablesYaml.loadFileConfiguration();
    }

    @Override
    public void reload(boolean deep){
        reloadFiles();
        super.reload(deep);
    }

    /**
     * Checks the config for new fields across versions.
     */
    private void checkAndCorrectConfig() {
        FileConfiguration configEndFile = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        FileConfiguration config = this.configYaml.getAccess();


        if(!configEndFile.contains("config.use permissions system")){
            config.set("config.use permissions system", true);
            this.configYaml.save(false);
        }
        if(!configEndFile.contains("config.consumables enabled")){
            config.set("config.consumables enabled", true);
            this.configYaml.save(false);
        }
        if(!configEndFile.contains("config.use metrics")){
            config.set("config.use metrics", true);
            this.configYaml.save(false);
        }
        if(!configEndFile.contains("config.messages.default consumable message")){
            config.set("config.messages.default consumable message", "&fYou HP is now %HP%");
            this.configYaml.save(false);
        }
        if(!configEndFile.contains("config.messages.consumable given")){
            config.set("config.messages.consumable given", "&aA consumable has been given to yo");
            this.configYaml.save(false);
        }
        if(!configEndFile.contains("config.messages.consumable not in hand")){
            config.set("config.messages.consumable not in hand", "&cYou must be holding the consumable");
            this.configYaml.save(false);
        }
        if(!configEndFile.contains("config.messages.consumables disabled")){
            config.set("config.messages.consumables disabled", "&cConsumables are disabled in this server!");
            this.configYaml.save(false);
        }
        if(!configEndFile.contains("config.HP cap.enabled")){
            config.set("config.HP cap.enabled", true);
            this.configYaml.save(false);
        }
        if(!configEndFile.contains("config.HP cap.amount")){
            config.set("config.HP cap.amount", 40);
            this.configYaml.save(false);
        }
        if(!configEndFile.contains("config.update HP on join")){
            config.set("config.update HP on join", true);
            this.configYaml.save(false);
        }
        if(!configEndFile.contains("config.messages.hp cannot be 0")) {
            config.set("config.messages.hp cannot be 0", "&cHP cannot be 0 or lower than 0");
            this.configYaml.save(false);
        }
        if(!configEndFile.contains("config.messages.hp cleared")){
            config.set("config.messages.hp cleared", "&fHP of player %player% cleared!");
            this.configYaml.save(false);
        }
        if(!configEndFile.contains("config.messages.hp cleared all")){
            config.set("config.messages.hp cleared all", "&fEvery player's HP has been cleared!");
            this.configYaml.save(false);
        }
        if(!configEndFile.contains("config.messages.inventory full")){
            config.set("config.messages.inventory full", "&cYour inventory is full.");
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

        if(mainCommand == null){
            this.messageSender.send("&cThe main command has not been registered properly. Disabling HealthPower");
            this.setEnabled(false);
            return;
        }

        mainCommand.setExecutor(new MainCommand(this));
        mainCommand.setTabCompleter(new MainCommandTabCompleter(this));
    }




    public MessageSender<Message> getMessageSender(){
        return this.messageSender;
    }

    public AbstractHPManager getHpManager(){
        return this.hpManager;
    }

    public double calculatePrice(String price, double HP){
        if(price == null) return 999999999999999.0;

        if(price.contains("%formula_")){
            List<String> formulas = this.getConfigYaml().getAccess().getStringList("config.GUI.formulas");
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


    @NotNull
    @Override
    public FileConfiguration getConfig(){
        return this.getConfigYaml().getAccess();
    }

    public YamlFile getConfigYaml(){
        return this.configYaml;
    }

    public YamlFile getHpYaml(){
        return this.hpYaml;
    }

    public YamlFile getConsumablesYaml(){
        return this.consumablesYaml;
    }
}