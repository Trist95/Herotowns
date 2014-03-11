package com.herocraftonline.townships.groups.town.commands.admin;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.CitizenGroup;
import com.herocraftonline.townships.api.Region;
import com.herocraftonline.townships.command.BasicInteractiveCommandState;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.groups.town.commands.BasicTownInteractiveCommand;
import com.herocraftonline.townships.util.Messaging;
import com.herocraftonline.townships.util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: gabizou
 */
public class TownAdminForceUpgradeCommand extends BasicTownInteractiveCommand {

    Map<String, Town> pendingTown = new HashMap<>();

    public TownAdminForceUpgradeCommand(TownManager manager) {
        super(manager, "TownAdminForceUpgrade");
        setDescription("Marks the location of your town's center, can only be used to setup a town's region once!");
        setUsage("/town admin upgrade <town name>");
        setPermission("townships.town.admin");
        setIdentifiers("admin upgrade");
        setStates(new StateA(), new StateB());
    }

    @Override
    public String getCancelIdentifier() {
        return "cancel";
    }

    @Override
    public void onCommandCancelled(CommandSender executor) { }

    class StateA extends BasicInteractiveCommandState {

        public StateA() {
            super("admin upgrade", "a u");
            setArgumentRange(1,1);
        }

        @Override
        public boolean execute(CommandSender sender, String identifier, String[] args) {
            if (!(sender instanceof Player)) {
                Messaging.send(sender, getMessage("town_not_a_player"));
                cancelInteraction(sender);
                return false;
            }

            Player player = (Player) sender;
            String townName = args[0];
            Town foundTown = manager.get(townName);
            if (foundTown == null) {
                Messaging.send(player, "There isn't a town by that name!");
                cancelInteraction(sender);
                return true;
            }
            boolean regionRequired = manager.getConfig().isGroupTypeRegionsEnabled(foundTown.getType());
            if (regionRequired) {
                if (!foundTown.hasRegions() || manager.getRegionManager().getRegion(foundTown) == null) {
                    Messaging.send(sender, "This town does not have regions yet, it is a requirement for their town to have" +
                            "an existing region!");
                    Messaging.send(sender, "If you know there is a WorldGuard region that exists for this town, please " +
                            "/town admin reclaim " + foundTown.getName());
                    cancelInteraction(sender);
                    return false;
                }
            }

            if (!foundTown.hasRegions()) { // Do NOT process if the Town has not upgraded with regions yet.
                Messaging.send(sender, getMessage("town_cant_reclaim"));
                cancelInteraction(sender);
                return true;
            }

            //Check nearby towns to make sure we're not overlapping
            for (CitizenGroup group : manager.getGroups()) {
                Town town = (Town) group;
                if (town.equals(foundTown))
                    continue;
                Region tr = manager.getRegionManager().getRegions().get(town);
                if (tr == null) {
                    continue; // If the town doesn't have a region skip it.
                }
                int minDist = Townships.config.minimumDistance;
                if (Util.getDistanceFromGroupCenter(player.getLocation(), town.getCenter()) < minDist) {
                    Messaging.send(sender, getMessage("town_claim_too_close"));
                    cancelInteraction(sender);
                    return false;
                }
            }
            pendingTown.put(player.getName(), foundTown);
            Messaging.send(sender, getMessage("town_claim_confirm1"));
            Messaging.send(sender, getMessage("town_claim_confirm2"));
            Messaging.send(player, getMessage("town_command_request_confirm"));
            return true;
        }
    }

    class StateB extends BasicInteractiveCommandState {

        public StateB() {
            super("confirm");
            setArgumentRange(0,0);
        }

        @Override
        public boolean execute(CommandSender sender, String identifier, String[] args) {
            if (!(sender instanceof Player)) {
                return false;
            }
            Player player = (Player) sender;
            Town town = pendingTown.remove(player.getName());
            if (town == null) {
                Messaging.send(sender, getMessage("town_no_longer_in_town"));
                return true;
            }
            // Make sure that the land claim actually goes through
            town.setType(town.getType().nextUpgrade());
            manager.getRegionManager().resizeGroupRegion(town, town.getType());
            plugin.getStorageManager().getStorage().saveCitizenGroup(town, true);
            town.sendAnnouncement(player.getDisplayName() + " has upgraded the town to a " +
                    manager.getTownConfig().getGroupTypeName(town.getType().nextUpgrade()) );
            Messaging.send(sender, "You have successfully upgraded the town: " + town.getName());
            return true;
        }
    }
}