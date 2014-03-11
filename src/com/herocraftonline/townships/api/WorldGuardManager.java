package com.herocraftonline.townships.api;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.groups.kingdom.KingdomRegion;
import com.herocraftonline.townships.groups.town.Town;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author gabizou
 */

public class WorldGuardManager {

    private transient WorldGuardPlugin wgp;

    private Map<RegionedCitizenGroupManager, GroupRegionManager> managers = new HashMap<>();
    private Map<String, Region> regions = new HashMap<>();
    private Map<CitizenGroup, Map<String, ChildRegion>> childRegions = new HashMap<>();

    public WorldGuardManager(Townships plugin, WorldGuardPlugin wgp) {
        this.wgp = wgp;
    }

    public WorldGuardPlugin getWorldGuard() {
        return wgp;
    }

    public boolean registerRegionManager(RegionedCitizenGroupManager manager, GroupRegionManager regionManager) {
        if (!managers.containsKey(manager)) {
            managers.put(manager, regionManager);
            return true;
        }
        return false;
    }

    public GroupRegionManager getGroupRegionManager(RegionedCitizenGroupManager manager) {
        return managers.get(manager);
    }

    public void addRegion(CitizenGroup group, Region region) {
        regions.put(group.getName(), region);
    }

    public void addChildRegion(CitizenGroup group, ChildRegion region) {
        Map<String, ChildRegion> regions;
        if (childRegions.containsKey(group))
            regions  = childRegions.remove(group);
        else
            regions = new HashMap<>();
        regions.put(region.getName(), region);
        childRegions.put(group,regions);
    }

    public void removeRegion(CitizenGroup group) {
        regions.remove(group.getName());
    }

    public void removeChildRegions(CitizenGroup group) {
        childRegions.remove(group);
    }

    public void removeChildRegion(CitizenGroup group, ChildRegion childRegion) {
        Map<String, ChildRegion> regions;
        if (childRegions.containsKey(group))
            regions = childRegions.remove(group);
        else
            return;
        // Must remove region from WorldGuard
        ChildRegion region = regions.remove(childRegion.getName());
        getRegionManager((Regionable) group).removeRegion(region.getName());
        childRegions.put(group, regions);
    }

    public Map<String, Region> getRegions() {
        return Collections.unmodifiableMap(regions);
    }

    public Region getRegion(ProtectedRegion wgRegion) {
        return wgRegion != null ? regions.get(wgRegion.getId().toLowerCase()) : null;
    }

    public ChildRegion getChildRegion(ProtectedRegion wgRegion) {
        if (wgRegion != null) {
            for (Map.Entry<CitizenGroup, Map<String, ChildRegion>> parentGroup : childRegions.entrySet()) {
                for (Map.Entry<String, ChildRegion> childRegionEntry : parentGroup.getValue().entrySet()) {
                    if (childRegionEntry.getValue().getName().equalsIgnoreCase(wgRegion.getId()))
                        return childRegionEntry.getValue();
                }
            }
        }
        return null;
    }

    public Map<CitizenGroup, Map<String,ChildRegion>> getChildRegions() {
        return Collections.unmodifiableMap(childRegions);
    }

    public RegionManager getRegionManager(World world) {
        return wgp.getRegionManager(world);
    }

    public RegionManager getRegionManager(Player player) {
        return getRegionManager(player.getWorld().getName());
    }

    public RegionManager getRegionManager(Regionable group) {
        return group != null ? getRegionManager(group.getClaimedWorld()) : null;
    }

    public RegionManager getRegionManager(String worldName) {
        if (worldName == null)
            return null;
        return getRegionManager(Bukkit.getWorld(worldName));
    }

    public boolean validateRegion(ProtectedRegion region, World world) {
        return getIntersectingRegions(region, world).size() == 0;
    }

    public List<ProtectedRegion> getIntersectingRegions(ProtectedRegion region, World world) {
        List<ProtectedRegion> regionsToCheck = new ArrayList<>();
        for (Map.Entry<String, ProtectedRegion> regionEntry : getRegionManager(world).getRegions().entrySet()) {
            regionsToCheck.add(regionEntry.getValue());
        }
        List<ProtectedRegion> intersectingRegions;
        try {
            intersectingRegions = region.getIntersectingRegions(regionsToCheck);

        } catch (UnsupportedIntersectionException e) {
            return null;
        }
        return intersectingRegions;
    }

    // Kingdom Stuff!

    /**
     * This is a helper method that will return a new KingdomRegion that is carefully calculated
     * by the number of towns within the supposed Kingdom Region. The King and mayors of included
     * towns will be the defacto owners of the new Kingdom Region and will be able to create
     * sub regions within the newly formed Kingdom Region.
     * of the new Kingdom Region
     * @param towns
     * @param king
     * @return
     */
    public KingdomRegion createKingdomRegion(Set<Town> towns, String king) {
        switch (towns.size()) {
            case 3 :
                return calculateThreeTownKingdomRegion(towns, king);
            case 4 :
                return calculateFourTownKingdomRegion(towns,king);
            case 5 :
                return calculateFiveTownKingdomRegion(towns,king);
            default :
                return null;
        }
    }

    /**
     * Specifically segmented due to the nature of shapes that a kingdom could possibly have.
     * Given the three town size, a Kingdom could have a Triangle where the middle area would
     * be circumscribed
     * @param towns
     * @param king
     * @return
     */
    private KingdomRegion calculateThreeTownKingdomRegion(Set<Town> towns, String king) {
        return null;
    }

    private KingdomRegion calculateFourTownKingdomRegion(Set<Town> towns, String king) {
        return null;
    }

    private KingdomRegion calculateFiveTownKingdomRegion(Set<Town> towns, String king) {
        return null;
    }

}
