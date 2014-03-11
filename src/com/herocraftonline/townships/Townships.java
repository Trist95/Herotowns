package com.herocraftonline.townships;

import com.dthielke.herochat.Herochat;
import com.herocraftonline.herostorage.HeroStorage;
import com.herocraftonline.townships.api.CitizenGroupManager;
import com.herocraftonline.townships.api.CitizenManager;
import com.herocraftonline.townships.api.ConfigManager;
import com.herocraftonline.townships.api.WorldGuardManager;
import com.herocraftonline.townships.command.CommandHandler;
import com.herocraftonline.townships.groups.GroupsManager;
import com.herocraftonline.townships.groups.guild.GuildManager;
import com.herocraftonline.townships.groups.kingdom.KingdomManager;
import com.herocraftonline.townships.groups.town.TownListener;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.listener.PlayerListener;
import com.herocraftonline.townships.listener.TagAPIListener;
import com.herocraftonline.townships.storage.StorageManager;
import com.herocraftonline.townships.util.DebugLog;
import com.herocraftonline.townships.util.Messaging;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author gabizou
 */

public class Townships extends JavaPlugin {

    public static boolean debug = false;

    private static Logger log;
    public static DebugLog debugLog;
    public static HeroStorage heroStorage;
    public static Economy econ;
    public static Permission perms;
    public static Herochat chat;
    public static ConfigManager config;

    private boolean titlesEnabled = false;

    public static WorldGuardPlugin wgp;

    private WorldGuardManager wgManager;
    private CitizenManager cManager;
    private GroupsManager groupManager;
    private StorageManager sManager;
    private CommandHandler commandHandler;


    private static Townships instance;
    private static boolean isDoneLoading;


    @Override
    public void onEnable() {
        this.log = this.getLogger();
        setupDependencies();
        config = new ConfigManager(this);
        if (ConfigManager.debug)
            debugLog = new DebugLog(getName(), getDataFolder() + File.separator + "debug.log");
        sManager = new StorageManager(this);
        wgManager = new WorldGuardManager(this, wgp);
        groupManager = new GroupsManager(this);
        cManager = new CitizenManager(this);
        commandHandler = new CommandHandler(this);
        groupManager.loadManagers();
        registerListeners();
        try {
            Messaging.setLocale(Locale.ENGLISH);
        } catch (ClassNotFoundException e) {
            log(Level.SEVERE, "There was an issue loading the language messages!");
            e.printStackTrace();
        }
        instance = this;
        registerCommands();
        isDoneLoading = true;
        log(Level.INFO,"Enabled Townships version: " + getDescription().getVersion() + " on " +
                Bukkit.getServer().getName() + " version " + Bukkit.getServer().getVersion());
    }

    @Override
    public void onDisable() {
        if(ConfigManager.debug)
            debugLog.close();
        groupManager.shutdown();
        groupManager = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandHandler.dispatch(sender, command, label, args);
    }

    /**
     * Make sure that all the dependencies are loaded, can't use Townships without any of these three.
     */
    private void setupDependencies() {
        Townships.econ = this.getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
        Townships.perms = this.getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.permission.Permission.class).getProvider();
        Townships.heroStorage = (HeroStorage) this.getServer().getPluginManager().getPlugin("HeroStorage");
        Townships.wgp = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
        Townships.chat = (Herochat) this.getServer().getPluginManager().getPlugin("Herochat");

        if(econ == null || perms == null || wgp == null) {
            this.onDisable();
        }
    }

    private void registerCommands() {
        for (CitizenGroupManager manager : groupManager.getCitizenGroupManagers().values()) {
            commandHandler.addCommandGroup(manager);
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TagAPIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TownListener(this), this);
    }

    // Supporting Manager Accessors
    public StorageManager getStorageManager() {
        return sManager;
    }

    public WorldGuardManager getWorldGuardManager() {
        return wgManager;
    }

    public TownManager getTownManager() {
        return (TownManager) groupManager.getCitizenGroupManager("town");
    }

    public GuildManager getGuildManager() {
        return (GuildManager) groupManager.getCitizenGroupManager("town");
    }

    public CitizenManager getCitizenManager() {
        return cManager;
    }

    public KingdomManager getKingdomManager() {
        return (KingdomManager) groupManager.getCitizenGroupManager("kingdom");
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }


    public static void debugLog(Level level, String msg) {
        if(ConfigManager.debug)
            debugLog.log(level, "[Debug] " + msg);
    }

    public static void debugThrow(String sourceClass, String sourceMethod, Throwable thrown) {
        if(ConfigManager.debug)
            debugLog.throwing(sourceClass, sourceMethod, thrown);
    }

    public static void log(Level level, String msg) {
        log.log(level, msg);
        if(ConfigManager.debug)
            debugLog.log(level, "[Towns] " + msg);
    }

    // ------------
    // Third Party Accessor Methods
    // -------------
    public boolean addGroupManager(CitizenGroupManager manager) {
        return groupManager.addCitizenGroupManager(manager);
    }

    public CitizenGroupManager getGroupManager(String name) {
        return groupManager.getCitizenGroupManager(name);
    }

    public static Townships getInstance() {
        return instance;
    }

    /**
     * This is a toggle for internal methods. This returns true after all interal plugin
     * managers have finished loading their respective Citizens and CitizenGroups.
     *
     * This prevents API Events not being called by methods provided by the API.
     * @return
     */
    public static boolean isDoneLoading() {
        return isDoneLoading;
    }
}
