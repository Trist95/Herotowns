package com.herocraftonline.townships.api;

import com.herocraftonline.townships.Townships;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author gabizou
 */

public class ConfigManager {

    // Enabled CitizenGroups
    private List<String> enabledGroups = new ArrayList<>();

    //Storage Settings
    public String storage;


    public final int minimumDistance;
    public final int maxTowns;

    // Channel Settings
    public boolean channelsenabled = false;

    public static boolean debug = true;


    public ConfigManager(Townships plugin) {
        FileConfiguration config = plugin.getConfig();
        config.options().copyDefaults(true);
        plugin.saveConfig();
        // Load global config options

        debug = config.getBoolean("debug");
        enabledGroups = config.getStringList("groups.enabled");
        if (enabledGroups.isEmpty()) {  // At least implement the default implementation of Towns
            enabledGroups.add("town");
        }
        channelsenabled = config.getBoolean("herochat.enabled", false);

        storage = config.getString("storage.type", "yml");

        //Econ stuff
        minimumDistance = config.getInt("towns.distance-between", 350);
        maxTowns = config.getInt("towns.max", 50);
    }

    public String getStorageType() {
        return storage;
    }

    public List<String> getEnabledGroups() {
        return Collections.unmodifiableList(enabledGroups);
    }

}
