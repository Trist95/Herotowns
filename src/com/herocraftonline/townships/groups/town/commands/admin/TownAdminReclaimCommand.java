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
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: gabizou
 */
public class TownAdminReclaimCommand extends BasicTownInteractiveCommand {


    Map<String, Location> pending = new HashMap<>();
    Map<String, Town> pendingTown = new HashMap<>();

    public TownAdminReclaimCommand(TownManager manager) {
        super(manager, "TownAdminReclaim");
        setDescription("Marks the location of your town's center, can only be used to setup a town's region once!");
        setUsage("/town admin reclaim <town name>");
        setPermission("townships.town.admin");
        setIdentifiers("admin reclaim");
        setStates(new StateA(), new StateB());
    }

    @Override
    public String getCancelIdentifier() {
        return "cancel";
    }

    @Override
    public void onCommandCancelled(CommandSender executor) {
        if (executor instanceof Player) {
            pending.remove(executor.getName());
        }
    }

    class StateA extends BasicInteractiveCommandState {

        public StateA() {
            super("admin reclaim", "a rc");
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
            Town foundTown = (Town) manager.get(townName);
            if (foundTown == null) {
                Messaging.send(player, "There isn't a town by that name!");
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
            pending.put(player.getName(), player.getLocation());
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
            Location location = pending.remove(player.getName());
            Town town = pendingTown.remove(player.getName());
            if (town == null) {
                Messaging.send(sender, getMessage("town_no_longer_in_town"));
                return true;
            }
            // Make sure that the land claim actually goes through
            if (!manager.reclaimArea(town, location)) {
                Messaging.send(sender, getMessage("town_claim_too_close_region"));
                return true;
            }
            return true;
        }
    }
}