package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.Rank;
import com.herocraftonline.townships.command.BasicInteractiveCommandState;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: gabizou
 */
public class TownLeaveCommand extends BasicTownInteractiveCommand {

    private Set<Player> pendingTownLeaves = new HashSet<>();

    public TownLeaveCommand(TownManager manager) {
        super(manager, "TownLeave");
        setDescription("Leaves a town");
        setUsage("/town leave");
        setPermission("townships.town.leave");
        setStates(new StateA(), new StateB());
    }

    @Override
    public String getCancelIdentifier() {
        return "cancel";
    }

    @Override
    public void onCommandCancelled(CommandSender sender) {
        if (!(sender instanceof Player))
            return;
        pendingTownLeaves.remove(sender);
    }

    class StateA extends BasicInteractiveCommandState {

        public StateA() {
            super("leave");
            setArgumentRange(0, 0);
        }

        @Override
        public boolean execute(CommandSender sender, String identifier, String[] args) {
            if (!(sender instanceof Player)) {
                Messaging.send(sender, "Only players may leave a town.");
                return false;
            }
            Player player = (Player) sender;
            Citizen citizen = getCitizen((Player) sender);
            if (!citizen.hasTown() && citizen.getPendingTown() == null) {
                Messaging.send(player, "You don't have a town!");
                cancelInteraction(sender);
                return false;
            } else if (citizen.getPendingTown() != null) {
                if (citizen.isPendingTownOwner())
                    removeFromCharter(player);
                else {
                    plugin.getTownManager().removeCitizenFromPendingGroup(sender.getName(), citizen, false);
                    Messaging.send(player, "You have removed your signature!");
                    cancelInteraction(sender);
                }
                return false;
            }

            Town town = citizen.getTown();
            if (citizen.getRank() == Rank.OWNER && town.getCitizens().size() > 1 && town.getSuccessor() == null ) {
                Messaging.send(player, "You must elect a new $1 before leaving the town!", citizen.getTown().getRankName(citizen.getRank()));
                cancelInteraction(sender);
                return false;
            }
            pendingTownLeaves.add(player);
            Messaging.send(player, "You are about to leave the town: $1.", citizen.getTown().getName());
            Messaging.send(player, "Please §a/town confirm §7or §c/town cancel §7this selection.");
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
            Citizen citizen = getCitizen(player);
            Town town = citizen.getTown();
            if (town == null || !town.hasMember(player.getName())) {
                Messaging.send(player, "You have already left the town, or were never in it.");
                citizen.setTown(null);
                return true;
            }
            //remove the citizen
            citizen.setTown(null);
            plugin.getStorageManager().getStorage().saveCitizen(citizen, true);
            town.removeMember(player.getName());
            //Notify town members
            String message = "$1 has left the town.".replace("$1", player.getDisplayName());
            town.sendAnnouncement(message);
            Messaging.send(player, "You are no longer a member of $1", town.getName());
            plugin.getStorageManager().getStorage().saveCitizenGroup(town, true);
            return true;
        }
    }

    private void removeFromCharter(Player player) {
        Citizen c = Townships.getInstance().getCitizenManager().getCitizen(player);
        if (!c.isPendingTownOwner()) {
            Messaging.send(player, getMessage("town_charter_remove_no_permission"));
            return;
        }
        String name = c.getPendingTown().name;
        plugin.getTownManager().removePending(name, true);
        Messaging.sendAnnouncement(plugin, getMessage("town_charter_invalid_remove"), name);
    }
}
