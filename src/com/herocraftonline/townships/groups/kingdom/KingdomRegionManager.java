package com.herocraftonline.townships.groups.kingdom;

import com.herocraftonline.townships.api.GroupRegionManager;
import com.herocraftonline.townships.api.GroupType;
import com.herocraftonline.townships.api.Region;
import com.herocraftonline.townships.api.Regionable;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;

/**
 * Author: gabizou
 */
public class KingdomRegionManager extends GroupRegionManager {

    public KingdomRegionManager(KingdomManager manager) {
        super(manager);
    }

    @Override
    public void applyDefaultGroupRegionFlags(ProtectedRegion region, GroupType type, String name) {

    }

    @Override
    public Region getClosestGroupRegion(Regionable group) {
        return null;
    }

    @Override
    public Region getClosestGroupRegionByLocation(Location loc) {
        return null;
    }
}
