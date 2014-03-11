package com.herocraftonline.townships.groups.town.commands.regions;

import com.herocraftonline.townships.api.*;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.groups.town.commands.BasicTownCommand;
import com.herocraftonline.townships.util.Messaging;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Author: gabizou
 */
public class TownRegionCreateCommand extends BasicTownCommand {

    public TownRegionCreateCommand(TownManager manager) {
        super(manager, "TownRegionCreate");
        setIdentifiers("region create", "r c");
        setUsage("/town region create <regionName> <parentRegion>");
        setArgumentRange(2,2);
        setPermission("townships.town.region.create");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        if (!(executor instanceof Player)) {
            Messaging.send(executor, "Can not create sub regions through console!");
            return false;
        }
        Player player = (Player) executor;
        Citizen citizen = getCitizen(player);


        // Get and validate the region ID
        String id = validateRegionId(args[0], false);
        String parent = validateRegionId(args[1], false);

        if (id == null || parent == null) {
            Messaging.send(executor, "An invalid region name was provided, please make sure to use only alphanumeric characters for region names");
            return true;
        }

        // Can't replace regions with this command
        RegionManager regionManager = wgp.getGlobalRegionManager().get(player.getWorld());
        if (regionManager.hasRegion(id)) {
            Messaging.send(executor,
                    "That region is already defined. To change the shape, use " +
                            "/town region redefine " + id);
            return true;
        }
        if (!regionManager.hasRegion(parent)) {
            Messaging.send(executor, "The parent region: " + parent + " does not exist! Make sure the parent region " +
                    "already exists!");
            return true;
        }

        Region parentRegion = null;
        if (manager.get(parent) != null && manager.get(parent).getName().equalsIgnoreCase(parent)) {
            parentRegion = manager.getRegionManager().getRegion(manager.get(parent));
        } else {
            ChildRegion temp = plugin.getWorldGuardManager().getChildRegion(regionManager.getRegion(parent));
            if (temp != null && temp.getParent() instanceof Town)
                parentRegion = temp;
        }

        Town town = citizen.getTown();
        if (town == null) {
            // Means we found a child region belonging to a CitizenGroup
            if (parentRegion instanceof ChildRegion && ((ChildRegion) parentRegion).getParent() != null) {
                CitizenGroup group = ((ChildRegion) parentRegion).getParent();
                if (group instanceof Town)
                    town = (Town) group;
                else {
                    Messaging.send(executor, "The parent region you designated isn't part of a Town.");
                    return true;
                }
            } else if (parentRegion != null && parentRegion.getName() != null) {
                CitizenGroup group = manager.get(parentRegion.getName().toLowerCase());
                if (group != null) {
                    town = (Town) group;
                } else {
                    Messaging.send(executor, "The parent region you designated isn't part of a Town.");
                    return true;
                }
            }
        }

        if (manager.getTownConfig().getGroupTypeMaxChildRegions(town.getType()) <= town.getChildRegions().size()) {
            Messaging.send(executor, "The Town has already reached the sub region limit for it's size!");
            Messaging.send(executor, "Please either remove some sub regions to make a new sub region");
            return true;
        }

        if (!manager.getRegionManager().checkOwner(player, parentRegion)) {
            Messaging.send(executor, "You aren't allowed to create regions within this parent region!");
            return true;
        }
        // Don't handle anything else if the parent region isn't registered as a Region in Townships
        if (parentRegion == null || parentRegion.getWorldGuardRegion() == null) {
            Messaging.send(executor, "There is no registered parent region by the name: " + parent);
            Messaging.send(executor, "Please contact an Administrator if you think this is an error.");
            return true;
        }

        // Make a region from the user's selection
        ProtectedRegion region = createRegionFromSelection(player, id);
        if (region == null) {
            Messaging.send(executor, "You haven't made a selection yet! Use the wand to make a selection first.");
            return true;
        }
        if (!manager.getRegionManager().checkChildRegion(region, parentRegion.getWorldGuardRegion())) {
            Messaging.send(executor, "The selected region is not contained in the parent region! Make a new selection!");
            return true;
        }
        if (plugin.getWorldGuardManager().validateRegion(region, player.getWorld())) {
            List<ProtectedRegion> intersecting = plugin.getWorldGuardManager().getIntersectingRegions(region, player.getWorld());
            if (intersecting != null && intersecting.size() != 0) {
                for (ProtectedRegion intersected : intersecting) {
                    if (intersected.getId().equalsIgnoreCase(town.getName()) || intersected.getId().equalsIgnoreCase(parent))
                        continue;
                    Region childRegion = manager.getRegionManager().getChildRegion(town, intersected.getId());
                    if (childRegion == null) {
                        // We haven't registered this region to this town just yet, might need to warn about this
                        continue;
                    }
                    // We only allow intersecting Region Owners or town Mayors to create sub regions that intersect
                    if (!childRegion.getOwners().contains(player.getName()) || town.getCitizenRank(citizen) != Rank.OWNER) {
                        Messaging.send(executor, "You can't create sub regions in sub regions that you are not owner of!");
                        return true;
                    } else {
                        // Notify the person about the intersection
                        Messaging.send(executor, "You will be intersecting the region: " + intersected.getId());
                    }
                }
            }
        }

        try { // Make sure to set the parent of the region BEFORE handling anything else
            region.setParent(parentRegion.getWorldGuardRegion());
        } catch (ProtectedRegion.CircularInheritanceException e) {
            Messaging.send(executor, "Could not create sub region of the town: " + town.getName()
                + " Please contact an Administrator.");
            return true;
        }
        ChildRegion childRegion = new ChildRegion(town, region, player.getName());
        childRegion.addOwner(player.getName()); // Automatically cast the creator as the owner.
        regionManager.addRegion(region);

        // Issue a warning about height
        int height = region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY();
        if (height <= 2) {
            executor.sendMessage(ChatColor.GOLD +
                    "(Warning: The height of the region was " + (height + 1) + " block(s).)");
        }

        manager.getRegionManager().addChildRegion(town, childRegion);
        manager.getRegionManager().commitChanges(player.getWorld());
        plugin.getStorageManager().getStorage().saveManagerData(manager);
        // Tell the user
        executor.sendMessage(ChatColor.YELLOW + "A new sub has been made named '" + id + "'.");
        return true;
    }
}
