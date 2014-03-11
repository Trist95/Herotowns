package com.herocraftonline.townships.api;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.config.RegionableConfig;
import org.bukkit.Location;

import java.io.File;

/**
 * Author: gabizou
 */
public abstract class RegionedCitizenGroupManager extends CitizenGroupManager {

    protected transient GroupRegionManager wgm;

    protected transient RegionableConfig groupConfig;

    private static transient RegionedCitizenGroupManager instance;

    public RegionedCitizenGroupManager(Townships plugin, File managerFile) {
        super(plugin, managerFile);
        this.groupConfig = (RegionableConfig) super.groupConfig; // Cast the config as a RegionableConfig so we maintain instance references
        registerRegionManager();
        instance = this;
    }

    public static RegionedCitizenGroupManager getInstance() {
        return instance;
    }

    @Override
    public RegionableConfig getConfig() {
        return groupConfig;
    }

    public abstract void registerRegionManager();

    public final GroupRegionManager getRegionManager() {
        return wgm;
    }

    @Override
    public void save() {
        storage.saveCitizenGroups(this);
        storage.saveManagerData(this);
        storage.saveRegionManagerData(wgm);
    }

    @Override
    public void load() {
        groups = storage.loadCitizenGroups(this);
        storage.loadManagerData(this);
        storage.loadRegionManagerData(wgm);
    }

    /**
     * Creates a {@link Region} and a ProtectedRegion at the given
     * location. This ignores any possible checks for whether the town already has a region or whether
     * a town has child regions. Calls a RegionedGroupClaimEvent when completed.
     *
     * @param group
     * @param location
     */
    public abstract boolean claimArea(CitizenGroup group, Location location);

    public abstract boolean reclaimArea(CitizenGroup group, Location location);
}
