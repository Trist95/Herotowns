package com.herocraftonline.townships.groups.town;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.CitizenGroupManager;
import com.herocraftonline.townships.api.GroupType;
import com.herocraftonline.townships.api.config.MayoralConfig;
import com.herocraftonline.townships.api.config.RegionableConfig;
import com.herocraftonline.townships.api.config.TaxableConfig;
import com.herocraftonline.townships.util.BankItem;
import com.herocraftonline.townships.util.Messaging;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;

/**
 * Author: gabizou
 */
public final class TownConfig extends RegionableConfig implements TaxableConfig, MayoralConfig {

    // World Settings
    private Set<String> configuredWorlds = new HashSet<>();

    // Enabled CitizenGroups
    private List<String> enabledGroups = new ArrayList<>();

    // Town names for their sizes
    private Map<GroupType, TownSetting> townSettings;

    //Economy Settings
    public int townCreationCost = 0;

    public final GroupType defaultTownType;
    public final GroupType minregionedTownType;
    public final int maxTowns;
    public final int maxMissedTaxes;
    public final long taxInterval;
    public final long citizenInterval;
    public final long townInterval;
    public final Set<Flag<?>> defaultFlags;

    // Region Settings

    // Channel Settings
    public boolean channelsenabled = false;

    public static boolean debug = true;


    public TownConfig(Townships plugin, CitizenGroupManager citizenGroupManager) {
        super(plugin,citizenGroupManager);

        // Load global config options
        debug = groupSettings.getBoolean("debug");
        if (enabledGroups.isEmpty()) {  // At least implement the default implementation of Towns
            enabledGroups.add("town");
        }
        configuredWorlds.addAll(groupSettings.getStringList("towns.worlds"));
        townSettings = new EnumMap<>(GroupType.class);
        defaultTownType = GroupType.valueOf(groupSettings.getString("towns.default-type", "outpost").trim().toUpperCase());
        minregionedTownType = GroupType.valueOf(groupSettings.getString("towns.min-type-regions", "small").trim().toUpperCase());
        // Perform matching for flags to be enabled on all Regions.
        Set<Flag<?>> configFlags = new HashSet<>();
        for (String fuzzyFlag : groupSettings.getStringList("towns.default-flags")) {
            Flag<?> foundFlag = DefaultFlag.fuzzyMatchFlag(fuzzyFlag);
            if (foundFlag == null) {
                Townships.log(Level.WARNING, Messaging.getMessage("config_invalid_flag").replace("$1",fuzzyFlag));
                continue;
            }
            configFlags.add(foundFlag);
        }

        defaultFlags = configFlags;
        for (GroupType type : GroupType.values()) {
            String typeName = type.toString();
            int minResidents = groupSettings.getInt("towns."+ typeName+ ".min-residents", 10);
            int maxResidents = groupSettings.getInt("towns." + typeName + ".max-residents", 1000);
            int cost = groupSettings.getInt("towns." + typeName + ".cost", 0);
            int tax = groupSettings.getInt("towns." + typeName + ".tax", 0);
            boolean regions = groupSettings.getBoolean("towns." + typeName + ".enable-regions", true);
            int radius = groupSettings.getInt("towns." + typeName + ".radius", 0);
            int numPoints = groupSettings.getInt("towns." + typeName + ".num-of-points", 36);
            boolean subregions = groupSettings.getBoolean("towns." + typeName + ".enable-subregions", true);
            boolean channel = groupSettings.getBoolean("towns." + typeName + ".channel-enabled", true);
            int maxSubRegions = groupSettings.getInt("towns." + typeName + ".max-subregions", 10);
            String name = groupSettings.getString("towns." + typeName + ".name", type.toString());
            Set<BankItem> stacks = loadMaterials(groupSettings.getConfigurationSection("towns." + typeName));
            townSettings.put(type, new TownSetting(name, minResidents, maxResidents, cost, tax, regions, radius,
                    numPoints, subregions, maxSubRegions, stacks, channel));
        }
        channelsenabled = groupSettings.getBoolean("herochat.enabled", false);

        //Econ stuff
        townCreationCost = groupSettings.getInt("towns.creation-cost", 0);
        maxTowns = groupSettings.getInt("towns.max", 50);

        // Tax timing stuff
        maxMissedTaxes = groupSettings.getInt("tax.max-missed", 2);
        taxInterval = groupSettings.getLong("tax.interval", 7 * 24 * 60 * 60 * 1000);
        citizenInterval = groupSettings.getLong("towns.citizen-activity-check-interval", 7 * 24 * 60 * 60 * 1000);
        townInterval = groupSettings.getLong("towns.town-citizen-requirement-check-interval", 7 * 24 * 60 * 60 * 1000);
    }

    @Override
    public InputStream getDefaultSettingsStream() {
        return plugin.getResource("TownSettings.yml");
    }

    // Taxable Config
    @Override
    public int getGroupTypeTax(GroupType tType) {
        return townSettings.get(tType).getTax();
    }

    @Override
    public int getGroupTypeCitizenTax(GroupType type) {
        return 0;
    }

    @Override
    public int getGroupTypeCitizenJoinCost(GroupType type) {
        return 0;
    }

    // Regionable Config

    @Override
    public String getGroupTypeName(GroupType tType) {
        return townSettings.get(tType).getName();
    }

    @Override
    public int getGroupTypeRadius(GroupType tType) {
        return townSettings.get(tType).getRadius();
    }

    @Override
    public int getGroupTypeCost(GroupType tType) {
        return townSettings.get(tType).getCost();
    }

    @Override
    public Set<BankItem> getGroupTypeResourceCost(GroupType tType) {
        return townSettings.get(tType).getMaterialCost();
    }

    @Override
    public boolean isGroupTypeRegionsEnabled(GroupType tType) {
        return townSettings.get(tType).getRegionEnabled();
    }

    @Override
    public boolean isGroupTypeChildRegionsEnabled(GroupType tType) {
        return townSettings.get(tType).getSubRegionsEnabled();
    }

    @Override
    public int getGroupTypePoints(GroupType type) {
        return townSettings.get(type).getPoints();
    }

    @Override
    public int getGroupTypeMaxChildRegions(GroupType type) {
        return townSettings.get(type).getMaxChildRegions();
    }

    // Mayoral Configs

    @Override
    public int getGroupTypeMinResidents(GroupType type) {
        return townSettings.get(type).getMinCitizens();
    }

    @Override
    public int getGroupTypeMaxResidents(GroupType tType) {
        return townSettings.get(tType).getMaxCitizens();
    }

    // Herochat configs

    public boolean getGroupTypeChannelEnabled(GroupType type) {
        return townSettings.get(type).getChannelEnabled();
    }

}
