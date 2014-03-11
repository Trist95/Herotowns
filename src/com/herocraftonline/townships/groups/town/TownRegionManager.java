package com.herocraftonline.townships.groups.town;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.*;
import com.herocraftonline.townships.groups.town.event.TownResizeEvent;
import com.herocraftonline.townships.util.Util;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Author: gabizou
 */
public final class TownRegionManager extends GroupRegionManager {

    public TownRegionManager(TownManager manager) {
        super(manager);
    }

    @Override
    public Region getClosestGroupRegionByLocation(Location loc) {
        TreeMap<Integer,Regionable> distances = new TreeMap<>();
        for (Map.Entry<CitizenGroup, Region> entry : getRegions().entrySet()) {
            distances.put(Util.getDistanceFromGroupCenter(loc, ((Regionable) entry.getKey()).getCenter()), (Regionable) entry.getKey());
        }

        return distances.get(distances.firstKey()).getRegion();
    }

    @Override
    public void applyDefaultGroupRegionFlags(ProtectedRegion region, GroupType type, String name) {

        // Process Notification flags
        String greeting = "Welcome to the ";
        String farewell = "Now leaving the ";
        String messageType = "Township, ";
        switch (type) {
            case HUGE:
                messageType = "Capitol city, ";
                break;
            case LARGE:
                messageType = "quaint City, ";
                break;
            case MEDIUM:
                messageType = "small Town, ";
                break;
            case SMALL:
                messageType = "little Hamlet, ";
                break;
            case OUTPOST:
                messageType = "minor Outpost, ";
                break;
        }
        greeting = greeting + messageType + name;
        farewell = farewell + messageType + name;

        Flag<?> greetingFlag = DefaultFlag.fuzzyMatchFlag("greeting");
        Flag<?> farewellFlag = DefaultFlag.fuzzyMatchFlag("farewell");

        // Process Mob spawning flag
        String spawns = "BLAZE,CAVE_SPIDER,CREEPER,ENDERMAN,GHAST,GIANT,MAGMA_CUBE,PIG_ZOMBIE,SILVERFISH,SKELETON,SLIME,SPIDER,WITCH,WITHER,ZOMBIE";
        Flag<?> spawnsFlag = DefaultFlag.fuzzyMatchFlag("deny-spawn");

        Flag<?> creeperBlockDamage = DefaultFlag.fuzzyMatchFlag("creeper-explosion");
        Flag<?> otherExplosionDamage = DefaultFlag.fuzzyMatchFlag("other-explosion");

        // Now, apply the flags
        try {
            setFlag(region, greetingFlag, greeting);
            setFlag(region, farewellFlag, farewell);
            setFlag(region, spawnsFlag, Bukkit.getConsoleSender(), spawns);
            setFlag(region, creeperBlockDamage, StateFlag.State.DENY);
            setFlag(region, otherExplosionDamage, StateFlag.State.DENY);
        } catch (InvalidFlagFormat e) {
            // Do Nothing.
        }
    }

    @Override
    public Region resizeGroupRegion(Regionable group, GroupType newType) {
        Town town = (Town) group;
        Region region = super.resizeGroupRegion(town, newType);
        if (Townships.isDoneLoading()) {
            TownResizeEvent event = new TownResizeEvent(town, region);
            Bukkit.getPluginManager().callEvent(event);
        }
        return region;
    }

    @Override
    public Region getClosestGroupRegion(Regionable group) {
        return null;
    }

    public void checkGroupRegionSize(Regionable group) {
        GroupType type = group.getType();
        if (canRegion(type) && group.getRegion() !=null)
            removeGroupRegionInfo(group);
        ProtectedRegion townRegion = getRegionManager(group).getRegion(((Town) group).getName());
        if (townRegion instanceof ProtectedPolygonalRegion) {
            List<BlockVector2D> regionPoints = townRegion.getPoints();
            if (regionPoints.size() != TownManager.getInstance().townConfig.getGroupTypePoints(type))
                redefineGroupRegion(group);

        }
    }

}
