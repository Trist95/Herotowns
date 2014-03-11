package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.CitizenGroup;
import com.herocraftonline.townships.api.GroupType;
import com.herocraftonline.townships.api.Region;
import com.herocraftonline.townships.command.BasicInteractiveCommandState;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.groups.town.TownUtil;
import com.herocraftonline.townships.util.BankItem;
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
public class TownClaimCommand extends BasicTownInteractiveCommand {
    Map<String, Location> pending = new HashMap<>();
    private TownManager tm;

    public TownClaimCommand(TownManager manager) {
        super(manager, "TownClaim");
        setDescription("Marks the location of your town's center, can only be used to setup a town's region once!");
        setUsage("/town claim");
        setPermission("townships.town.claim");
        setStates(new StateA(), new StateB());
        tm = Townships.getInstance().getTownManager();
    }

    @Override
    public String getCancelIdentifier() {
        return "cancel";
    }

    @Override
    public void onCommandCancelled(CommandSender sender) {
        if (sender instanceof Player) {
            pending.remove(sender.getName());
        }

    }

    class StateA extends BasicInteractiveCommandState {

        public StateA() {
            super("claim");
            setArgumentRange(0, 0);
        }

        @Override
        public boolean execute(CommandSender sender, String identifier, String[] args) {
            if (!(sender instanceof Player)) {
                Messaging.send(sender, getMessage("town_not_a_player"));
                cancelInteraction(sender);
                return false;
            }
            Player player = (Player) sender;
            Citizen citizen = getCitizen(player);
            //Check if the player has a town
            if (!citizen.hasTown()) {
                Messaging.send(sender, getMessage("town_not_part_of_town"));
                cancelInteraction(sender);
                return false;
            }
            //Check if the player is a manager
            if (!citizen.getRank().canManage()) {
                Messaging.send(sender, getMessage("town_claim_invalid_perms"));
                cancelInteraction(sender);
                return false;
            }

            //Check nearby towns to make sure we're not overlapping
            for (CitizenGroup group : tm.getGroups()) {
                Town town = (Town) group;
                Region tr = tm.getRegionManager().getRegions().get(town);
                if (tr == null) {
                    continue; // If the town doesn't have a region skip it.
                }
                if (town.equals(citizen.getTown()))
                    continue;
                int minDist = Townships.config.minimumDistance;
                if (Util.getDistanceFromGroupCenter(player.getLocation(), town.getCenter()) < minDist) {
                    Messaging.send(sender, getMessage("town_claim_too_close"));
                    cancelInteraction(sender);
                    return false;
                }
            }

            //Do the material checks for the bank to verify that the town bank has enough/the right materials to claim a spot
            Town claimingTown = citizen.getTown();
            GroupType claimingType = claimingTown.getType();
            boolean hasAll = true;
            for (BankItem is : manager.getTownConfig().getGroupTypeResourceCost(claimingType)) {
                boolean hasEnough = false;
                for (Map.Entry<String, BankItem> entry : citizen.getTown().getBankContents().entrySet()) {
                    if (entry.getValue().getItem().isSimilar(is.getItem())) {
                        if (entry.getValue().getAmount() >= is.getAmount()) {
                            hasEnough = true;
                        } else {
                            hasAll = false;
                            break;
                        }
                    }
                }
                if (!hasEnough) {
                    hasAll = false;
                    break;
                }
            }
            if (!hasAll || claimingTown.getBankMoney() < manager.getTownConfig().getGroupTypeCost(claimingType)) {
                Messaging.send(sender, getMessage("town_claim_warning1"));
                Messaging.send(sender, getMessage("town_claim_warning2"));
                Messaging.send(sender, getMessage("town_claim_warning3"));
                Messaging.send(sender, getMessage("town_claim_warning4"));
                cancelInteraction(sender);
                return false;
            }

            pending.put(player.getName(), player.getLocation());
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
            Citizen citizen = getCitizen(player);
            Town town = citizen.getTown();
            if (town == null) {
                Messaging.send(sender, getMessage("town_no_longer_in_town"));
                return true;
            }
            if (town.getRegion() != null) {
                Messaging.send(sender, getMessage("town_claim_region_already_made"));
                return true;
            }
            // Re-run the Check for towns to make sure we're not overlapping
            for (CitizenGroup group : tm.getGroups()) {
                Town otherTown = (Town) group;
                Region tr = otherTown.getRegion();
                if (tr == null) {
                    continue; // If the town doesn't have a region skip it.
                }
                int minDist = Townships.config.minimumDistance;
                if (Util.getDistanceFromGroupCenter(player.getLocation(), town.getCenter()) < minDist) {
                    Messaging.send(sender, getMessage("town_claim_too_close"));
                    return true;
                }
            }
            // Make sure that the land claim actually goes through
            if (!tm.claimArea(town, location)) {
                Messaging.send(sender, getMessage("town_claim_too_close_region"));
                return true;
            }
            TownUtil.removeTownCosts(town, town.getType());
            town.withdraw(manager.getTownConfig().getGroupTypeCost(town.getType()));
            return true;
        }
    }
}
