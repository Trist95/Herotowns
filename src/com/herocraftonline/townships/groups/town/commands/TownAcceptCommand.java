package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.CitizenGroup;
import com.herocraftonline.townships.api.Rank;
import com.herocraftonline.townships.command.BasicInteractiveCommandState;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
/**
 * Author: gabizou
 */
public class TownAcceptCommand extends BasicTownInteractiveCommand {

    private Map<Player, Town> pendingTownInvites = new HashMap<>();

    public TownAcceptCommand(TownManager manager) {
        super(manager, "TownAccept");
        setDescription("Accepts an invitation to join a town");
        setUsage("/town accept §9<name>");
        setStates(new StateA(), new StateB());
        setPermission("townships.town.accept");
    }

    @Override
    public String getCancelIdentifier() {
        return "cancel";
    }

    @Override
    public void onCommandCancelled(CommandSender sender) {
        if (!(sender instanceof Player))
            return;
        pendingTownInvites.remove(((Player) sender).getPlayer());
    }

    class StateA extends BasicInteractiveCommandState {

        public StateA() {
            super("accept");
            setArgumentRange(1, 1);
            setIdentifiers("accpet","join");
        }

        @Override
        public boolean execute(CommandSender sender, String identifier, String[] args) {
            if (!(sender instanceof Player)) {
                Messaging.send(sender, getMessage("town_not_a_player"));
                return false;
            }
            Player player = (Player) sender;
            Citizen citizen = getCitizen((Player) sender);
            if (citizen.hasInvite(args[0])) {
                Map<String,CitizenGroup> invites = citizen.getInvites();
                CitizenGroup group = invites.get(args[0].toLowerCase());

                // Check if Group is still a town
                if (!(group instanceof Town)) {
                    Messaging.send(sender, getMessage("town_doesnt_exist"), args[0]);
                    cancelInteraction(sender);
                    return false;
                }
                Town town = (Town) group;

                // Check Town invites
                if (!town.hasInvite(citizen)) {
                    Messaging.send(sender,getMessage("town_invalid_invite"), town.getName());
                    cancelInteraction(sender);
                    citizen.removeInvite(town);
                    return false;
                }

                // Check Econ
                if (!Townships.econ.has(player.getName(), town.getJoinCost())) {
                    Messaging.send(sender, getMessage("town_invite_insufficient_econ"),
                            Townships.econ.format(town.getJoinCost()));
                    cancelInteraction(sender);
                    return false;
                }
                pendingTownInvites.put(player,town);
                Messaging.send(player, getMessage("town_invite_accept_warning1"), town.getName(),
                        Townships.econ.format(town.getJoinCost()));
                Messaging.send(player, getMessage("town_command_request_confirm"));
                return true;
            }
            Messaging.send(sender, getMessage("town_invite_doesnt_exist"), args[0]);
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
                return true;
            }

            Player player = (Player) sender;
            Town town = pendingTownInvites.get(player);
            if (!Townships.econ.has(player.getName(), town.getJoinCost())) {
                Messaging.send(sender, getMessage("town_invite_insufficient_econ"),
                        Townships.econ.format(town.getJoinCost()));
                pendingTownInvites.remove(player);
                return true;
            }

            Citizen citizen = getCitizen(player);
            town.removeInvite(player.getName());
            pendingTownInvites.remove(player);
            citizen.removeInvite(town);

            //Add the citizen to the town
            town.addMember(citizen, Rank.CITIZEN);
            town.citizenLogin(citizen);
            Messaging.send(sender, getMessage("town_welcome"), town.getName());
            //Notify town members
            town.sendAnnouncement(getMessage("town_invite_accepted_announcement"),
                    player.getDisplayName(), town.getName());

            plugin.getStorageManager().getStorage().saveCitizen(citizen,true);
            plugin.getStorageManager().getStorage().saveCitizenGroup(town,true);
            return true;
        }
    }
}
