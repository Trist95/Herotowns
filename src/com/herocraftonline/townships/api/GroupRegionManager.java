package com.herocraftonline.townships.api;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.util.Messaging;
import com.herocraftonline.townships.util.Util;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Author: gabizou
 */
public abstract class GroupRegionManager {

    protected Map<CitizenGroup, Region> regions = new HashMap<>();
    protected Map<CitizenGroup, Map<String, ChildRegion>>  childRegions = new HashMap<>();

    protected transient Townships plugin;
    protected transient WorldGuardManager wgm;
    protected transient RegionedCitizenGroupManager manager;

    public GroupRegionManager(RegionedCitizenGroupManager manager) {
        this.plugin = manager.plugin;
        this.wgm = manager.plugin.getWorldGuardManager();
        this.manager = manager;
    }

    public RegionedCitizenGroupManager getManager() {
        return manager;
    }

    public Map<CitizenGroup, Region> getRegions() {
        return Collections.unmodifiableMap(regions);
    }

    public Map<CitizenGroup, Map<String, ChildRegion>> getChildRegions() {
        return Collections.unmodifiableMap(childRegions);
    }

    public RegionManager getRegionManager(World world) {
        return wgm.getWorldGuard().getRegionManager(world);
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

    public Region getRegion(CitizenGroup group) {
        return regions.get(group);
    }

    public void addRegion(CitizenGroup group, Region region) {
        regions.put(group, region);
        ((Regionable) group).setRegion(region); // Make sure the Group knows of this region change
        applyDefaultGroupRegionFlags(region.getWorldGuardRegion(), ((Regionable) group).getType(), group.getName());
        wgm.addRegion(group, region);
    }

    public void addChildRegion(CitizenGroup group, ChildRegion childRegion) {
        Map<String, ChildRegion> regions;
        if (childRegions.containsKey(group))
            regions  = childRegions.remove(group);
        else
            regions = new HashMap<>();
        regions.put(childRegion.getName(),new ChildRegion(group, childRegion));
        childRegions.put(group,regions);
        ((Regionable) group).addChildRegion(childRegion);
        wgm.addChildRegion(group, childRegion);
    }

    public void addChildRegions(CitizenGroup group, List<ChildRegion> regions) {
        for (ChildRegion region : regions) {
            addChildRegion(group, region);
        }
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
        wgm.removeChildRegion(group, childRegion);
        ((Regionable) group).removeChildRegion(region);
    }

    public ChildRegion getChildRegion(CitizenGroup group, String id) {
        return childRegions.get(group) == null ? null : childRegions.get(group).get(id);
    }

    /**
     * Requests the town's claim world and requests from WorldGuard's RegionManager whether the town indeed has a
     * region. This is defined that a town's region's id is the same as the town's name to lowercase.
     * @param group
     * @return
     */
    public boolean regionExists(Regionable group) {
        boolean exists = false;
        if (group.getClaimedWorld() == null && getRegionManager(group) == null)
            return false;
        RegionManager manager = getRegionManager(group);
        if (manager.hasRegion(((CitizenGroup) group).getName().toLowerCase()))
            exists = true;
        return exists;
    }


    public Region createAndSaveGroupRegion(Location loc, GroupType type, String name, String mayor) {
        int radius = manager.getConfig().getGroupTypeRadius(type);
        int num = manager.getConfig().getGroupTypePoints(type);
        List<BlockVector2D> points = Util.createCirclePoints(loc, radius, num);
        ProtectedRegion region = new ProtectedPolygonalRegion(name,points,0,255);
        if (!wgm.validateRegion(region, loc.getWorld()))
            return null;
        applyDefaultGroupRegionFlags(region, type, name);
        getRegionManager(loc.getWorld()).addRegion(region);
        commitChanges(loc.getWorld());
        return new Region(region,mayor);
    }

    public Region redefineRegion(Location loc, Region existing, GroupType type, String name) {
        Region newRegion;
        int radius = manager.getConfig().getGroupTypeRadius(type);
        int num = manager.getConfig().getGroupTypePoints(type);
        List<BlockVector2D> points = Util.createCirclePoints(loc, radius, num);
        ProtectedRegion newWGRegion = new ProtectedPolygonalRegion(name,points,0,255);
        newWGRegion.setMembers(existing.getWorldGuardRegion().getMembers());
        newWGRegion.setOwners(existing.getWorldGuardRegion().getOwners());
        newWGRegion.setFlags(existing.getWorldGuardRegion().getFlags());
        newWGRegion.setPriority(existing.getWorldGuardRegion().getPriority());
        try {
            newWGRegion.setParent(existing.getWorldGuardRegion().getParent());
        } catch (ProtectedRegion.CircularInheritanceException e) {
            // This shouldn't be called
        }

        newRegion = new Region(newWGRegion,existing.getOwners());
        newRegion.addManagers(existing.getManagers());
        newRegion.addGuests(existing.getGuests());
        newRegion.addOwners(existing.getOwners());
        applyDefaultGroupRegionFlags(newRegion.getWorldGuardRegion(), type, name);
        getRegionManager(loc.getWorld()).addRegion(newRegion.getWorldGuardRegion());
        return newRegion;
    }

    public ChildRegion redefineChildRegion(Selection selection, ChildRegion existing, CitizenGroup owner) {
        ChildRegion newRegion;
        ProtectedRegion newWGRegion = createRegionFromSelection(selection, existing.getName());
        newWGRegion.setMembers(existing.getWorldGuardRegion().getMembers());
        newWGRegion.setOwners(existing.getWorldGuardRegion().getOwners());
        newWGRegion.setFlags(existing.getWorldGuardRegion().getFlags());
        newWGRegion.setPriority(existing.getWorldGuardRegion().getPriority());
        try {
            newWGRegion.setParent(existing.getWorldGuardRegion().getParent());
        } catch (ProtectedRegion.CircularInheritanceException e) {
            // This shouldn't be called
        }

        newRegion = new ChildRegion(owner, newWGRegion, existing.getOwners());
        newRegion.addManagers(existing.getManagers());
        newRegion.addGuests(existing.getGuests());
        newRegion.addOwners(existing.getOwners());
        getRegionManager(selection.getWorld()).addRegion(newRegion.getWorldGuardRegion());
        return newRegion;
    }

    public abstract void applyDefaultGroupRegionFlags(ProtectedRegion region, GroupType type, String name);

    protected static <V> void setFlag(ProtectedRegion region, Flag<V> flag, Object value) throws InvalidFlagFormat {
        V val = flag.unmarshal(value);
        if (val != null)
            region.setFlag(flag, val);
    }

    protected static <V> void setFlag(ProtectedRegion region,
            Flag<V> flag, CommandSender sender, String value)
            throws InvalidFlagFormat {
        region.setFlag(flag, flag.parseInput(WorldGuardPlugin.inst(), sender, value));
    }

    /**
     * Attempts to recalculate the specified Regionable's region with the given GroupType for size. This will
     * attempt to copy over all access permissions and flags and other region assets to the new ProtectedRegion before
     * setting the Regionable's Region to the new one. This automatically removes any child region that is
     * pertruding from the new Region's area.
     * @param group - Regionable to downsize the region of
     * @param newType - New GroupType that this Regionable will take and have the region reflect.
     * @return - The New Region containing all permissions and attributes of the Old Region
     */
    public Region resizeGroupRegion(Regionable group, GroupType newType) {
        int radius = manager.getConfig().getGroupTypeRadius(newType);
        int num = manager.getConfig().getGroupTypePoints(newType);
        List<BlockVector2D> points = Util.createCirclePoints(group.getCenter(),radius,num);
        // Handle creating a new ProtectedRegion with the same membership and flags and such
        ProtectedRegion newRegion = new ProtectedPolygonalRegion(((CitizenGroup) group).getName(),points,0,255);
        newRegion.setMembers(group.getRegion().getWorldGuardRegion().getMembers());
        newRegion.setOwners(group.getRegion().getWorldGuardRegion().getOwners());
        newRegion.setFlags(group.getRegion().getWorldGuardRegion().getFlags());
        newRegion.setPriority(group.getRegion().getWorldGuardRegion().getPriority());
        try {
            newRegion.setParent(group.getRegion().getWorldGuardRegion().getParent());
        } catch (ProtectedRegion.CircularInheritanceException ignore) {}

        // Recalculate each WorldGuard child region within the Regionable
        for (Region region : group.getChildRegions()) {
            if (!checkChildRegion(region.getWorldGuardRegion(),newRegion)) {
                // Remove region if it is no longer belonging in the Regionable
                removeChildRegion((CitizenGroup) group, (ChildRegion) region);
                continue;
            }
            // Check that this child region's parent retains the previous parent if it was the old town region
            if (region.getWorldGuardRegion().getParent().getId().equals(group.getRegion().getWorldGuardRegion().getId()))
                try {
                    region.getWorldGuardRegion().setParent(newRegion);
                } catch (ProtectedRegion.CircularInheritanceException ignore) {}
        }
        Region groupRegion = new Region(newRegion);
        groupRegion.addGuests(group.getRegion().getGuests());
        groupRegion.addManagers(group.getRegion().getManagers());
        groupRegion.addOwners(group.getRegion().getOwners());
        applyDefaultGroupRegionFlags(groupRegion.getWorldGuardRegion(), newType, ((CitizenGroup) group).getName());
        // Swap regions and set the Group's new region in place.
        getRegionManager(group).addRegion(newRegion); // WorldGuard automatically replaces the old region with the new one in this line
        group.setRegion(groupRegion);
        return groupRegion;
    }

    public Region downsizeGroupRegion(Regionable group, GroupType type) {
        return resizeGroupRegion(group, type.downgrade());
    }

    public boolean checkChildRegion(ProtectedRegion region, ProtectedRegion parent) {
        List<BlockVector2D> points = region.getPoints();
        for (BlockVector2D point : points) {
            if (!parent.contains(point))
                return false;
        }
        return true;
    }

    public List<ChildRegion> getChildRegionChildren(ChildRegion region) {
        List<ChildRegion> children = new LinkedList<>();
        CitizenGroup parent = region.getParent();
        RegionManager regionManager = manager.getRegionManager().getRegionManager((Regionable) parent);
        if (regionManager == null)
            return children;
        for (Map.Entry<String, ProtectedRegion> regions : regionManager.getRegions().entrySet()) {
            ProtectedRegion regionToCheck = regions.getValue();
            if (region.getWorldGuardRegion().equals(regionToCheck.getParent())) {
                children.add(wgm.getChildRegion(regionToCheck));
            }
        }
        return children;
    }

    /**
     * Obtains the Region closest to the provided Regionable. This assumes that the provided
     * Regionable already has a Region.
     * @param group
     * @return
     */
    public abstract Region getClosestGroupRegion(Regionable group);

    /**
     * Gets the closest Region in the world of provided location. This will guarantee that the Region is
     * the closest.
     * @param loc
     * @return
     */
    public abstract Region getClosestGroupRegionByLocation(Location loc);

    public void removeGroupRegions(Regionable group) {
        if (group.getChildRegions() != null) {
            Iterator<Map.Entry<String, ChildRegion>> it = childRegions.get((CitizenGroup) group).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, ChildRegion> entry = it.next();
                it.remove();
                getRegionManager(group).removeRegion(entry.getValue().getWorldGuardRegion().getId());
                group.removeChildRegion(entry.getValue());
                wgm.removeChildRegion((CitizenGroup) group, entry.getValue());
            }
        }
        if (group.getRegion() != null) {
            getRegionManager(group).removeRegion(group.getRegion().getWorldGuardRegion().getId());
            group.setRegion(null);
        }
        group.setRegion(null);
        group.setClaimedWorld(null);
    }

    protected void removeGroupRegionInfo(Regionable group) {
        regions.remove((CitizenGroup) group);
        childRegions.remove((CitizenGroup) group);
        wgm.removeRegion((CitizenGroup) group);
        wgm.removeChildRegions((CitizenGroup) group);
        group.setRegion(null);
        group.setCenter(null);
        group.setClaimedWorld(null);
        group.clearChildRegions();
        plugin.getStorageManager().getStorage().saveRegionManagerData(this);
        plugin.getStorageManager().getStorage().saveCitizenGroup(((CitizenGroup) group), true);
    }

    protected boolean canRegion(GroupType type) {
        return manager.getConfig().isGroupTypeRegionsEnabled(type);
    }

    protected void redefineGroupRegion(Regionable group) {
        resizeGroupRegion(group, group.getType());
    }

    public Region validateRegion(String regionName) {
        // Return if this is a CitizenGroup region in which there are no child regions to check
        CitizenGroup group = manager.get(regionName.toLowerCase());
        if (group != null) {
            Region groupRegion = manager.getRegionManager().getRegion(group);
            if (groupRegion != null) {
                return groupRegion;
            }
        }

        // Check Child regions if Town region isn't found
        Region foundChild = null;
        // Have to find out which region the player is currently standing in since Guests can be region owners.
        for (Map.Entry<CitizenGroup, Map<String, ChildRegion>> entry : manager.getRegionManager().getChildRegions().entrySet()) {
            for (Map.Entry<String, ChildRegion> childEntry : entry.getValue().entrySet()) {
                if (childEntry.getKey().equalsIgnoreCase(regionName)) {
                    foundChild = childEntry.getValue();
                }
            }
            if (foundChild != null) {
                break;
            }
        }
        return foundChild;
    }

    public boolean checkOwner(Player player, Region region) {
        // Check if the region is a child region, in which, we have to check if the player is the owning Town's mayor or successor
        if (!region.isOwner(player.getName())) {
            // Check for Town permissions
            if (region instanceof ChildRegion && ((ChildRegion) region).getParent().getCitizenRank(player.getName()).ordinal() < Rank.SUCCESSOR.ordinal()) {
                Messaging.send(player, "You aren't an Owner of this region! " + "Please contact one of the region owners if you think this is a mistake.");
                return false;
            } else if (manager.get(region.getName()).getCitizenRank(player.getName()).ordinal() < Rank.SUCCESSOR.ordinal()) {
                Messaging.send(player, "You aren't a Town's Successor or Mayor, you don't have permission to add/remove guests/managers/owners to the town region!");
                return false;
            }
        }
        return true;
    }

    public boolean checkManager(Player player, Region region) {
        // Check if the region is a child region, in which, we have to check if the player is the owning Town's mayor or successor
        if (!region.isOwner(player.getName()) && !region.isManager(player.getName())) {
            if (region instanceof ChildRegion && ((ChildRegion) region).getParent().getCitizenRank(player.getName()).ordinal() < Rank.MANAGER.ordinal()) {
                Messaging.send(player, "You aren't an Owner or Manager of this region! " +
                        "Please contact one of the region owners if you think this is a mistake.");
                return false;
            } else if (manager.get(region.getName()).getCitizenRank(player.getName()).ordinal() < Rank.MANAGER.ordinal()) {
                Messaging.send(player, "You aren't a Town's Successor or Mayor, you don't have permission to add/remove guests to the town region!");
                return false;
            }
        }
        return true;
    }

    public boolean checkGuest(Player player, Region region) {
        if (!region.isGuest(player.getName()) && !region.isManager(player.getName()) && !region.isOwner(player.getName())) {
            if (region instanceof ChildRegion && ((ChildRegion) region).getParent().getCitizenRank(player.getName()).ordinal() < Rank.GUEST.ordinal()) {
                Messaging.send(player, "You aren't a Guest on either this region or the Town it belongs to!");
                Messaging.send(player, "If you think this is in error, please contact the Town management.");
                return false;
            }
            if (manager.get(region.getName()).getCitizenRank(player.getName()).ordinal() < Rank.GUEST.ordinal()) {
                Messaging.send(player, "You are not on the Guest list of this Town. If you think this is an error, please contact the Town management.");
                return false;
            }
        }
        return true;
    }

    /**
     * Create a {@link com.sk89q.worldguard.protection.regions.ProtectedRegion} from the player's selection.
     *
     * @param id the ID of the new region
     * @return a new region
     */
    protected static ProtectedRegion createRegionFromSelection(Selection selection, String id) {

        // Detect the type of region from WorldEdit
        if (selection instanceof Polygonal2DSelection) {
            Polygonal2DSelection polySel = (Polygonal2DSelection) selection;
            int minY = polySel.getNativeMinimumPoint().getBlockY();
            int maxY = polySel.getNativeMaximumPoint().getBlockY();
            return new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
        } else if (selection instanceof CuboidSelection) {
            BlockVector min = selection.getNativeMinimumPoint().toBlockVector();
            BlockVector max = selection.getNativeMaximumPoint().toBlockVector();
            return new ProtectedCuboidRegion(id, min, max);
        } else {
            return null;
        }
    }

    public void commitChanges(World world) {
        try {
            getRegionManager(world).save();
        } catch (ProtectionDatabaseException e) {
            // Do nothing
        }
    }
}
