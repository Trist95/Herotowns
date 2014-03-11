package com.herocraftonline.townships.groups.town.commands.regions;

import com.herocraftonline.townships.api.ChildRegion;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.groups.town.commands.BasicTownCommand;
import com.herocraftonline.townships.util.Messaging;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Author: gabizou
 */
public class TownRegionRedefineCommand extends BasicTownCommand {

    public TownRegionRedefineCommand(TownManager manager) {
        super(manager, "TownRegionRedefine");
        setIdentifiers("region redefine", "r rd");
        setArgumentRange(1,1);
        setPermission("townships.town.region.redefine");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        if (!(executor instanceof Player)) {
            Messaging.send(executor, "Can not create sub regions through console!");
            return false;
        }
        String redefineRegion = args[0];
        Player player = (Player) executor;
        Citizen citizen = getCitizen(player);
        // Get and validate the region ID
        String id = validateRegionId(redefineRegion, false);

        // Can't replace regions with this command
        RegionManager regionManager = wgp.getGlobalRegionManager().get(player.getWorld());
        if (!regionManager.hasRegion(id)) {
            Messaging.send(executor,
                    "That region doesn't exist, use  " +
                            "/town region create <childRegion> <parentRegion>" + id);
            return true;
        }
        ChildRegion foundRegion = null;
        if (manager.get(redefineRegion) != null && manager.get(redefineRegion).getName().equalsIgnoreCase(redefineRegion)) {
            Messaging.send(executor, "You can not redefine your Town region!");
            return true;
        } else {
            ChildRegion temp = plugin.getWorldGuardManager().getChildRegion(regionManager.getRegion(redefineRegion));
            if (temp != null && temp.getParent() instanceof Town)
                foundRegion = temp;
        }

        Town town = citizen.getTown();
        if (foundRegion != null && foundRegion.getParent() instanceof Town) {
            // Means we found a child region belonging to a Town
            if (!foundRegion.getParent().equals(town)) {
                town = (Town) foundRegion.getParent();
            }
        } else {
            Messaging.send(executor, "The region you want to redefine isn't part of a Town!");
            return true;
        }

        if (!manager.getRegionManager().checkOwner(player, foundRegion)) {
            Messaging.send(executor, "You aren't a Region Owner! only Region Owners or Town Mayors can redefine regions!");
            return true;
        }
        ChildRegion newRegion = manager.getRegionManager().redefineChildRegion(getSelection(player), foundRegion, town);

        // Delete old region
        manager.getRegionManager().addChildRegion(town, newRegion);
        plugin.getStorageManager().getStorage().saveCitizenGroup(town, false);
        plugin.getStorageManager().getStorage().saveManagerData(manager);
        Messaging.send(executor, "You have successfully redefined the region: " + id);
        return true;
    }
}
