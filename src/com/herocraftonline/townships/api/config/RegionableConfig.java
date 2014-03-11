package com.herocraftonline.townships.api.config;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.CitizenGroupManager;
import com.herocraftonline.townships.api.GroupType;

/**
 * Author: gabizou
 *
 * RegionableConfig is a default implementation of CitizenGroupConfig with {@link com.herocraftonline.townships.api.config.UpgradableConfig}
 *
 * RegionableConfig should have defaults for constructing new Regions for Regioned CitizenGroups. Examples are:
 * Number of points that create a "circular" region, Whether child regioning is enabled, how many child regions should
 * be allowed etc.
 */
public abstract class RegionableConfig extends CitizenGroupConfig implements UpgradableConfig {

    public RegionableConfig(Townships plugin, CitizenGroupManager manager) {
        super(plugin, manager);
    }

    public abstract int getGroupTypeRadius(GroupType type);

    public abstract boolean isGroupTypeRegionsEnabled(GroupType type);

    public abstract boolean isGroupTypeChildRegionsEnabled(GroupType type);

    public abstract int getGroupTypePoints(GroupType type);

    public abstract int getGroupTypeMaxChildRegions(GroupType type);

}
