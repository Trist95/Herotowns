package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.GroupType;
import com.herocraftonline.townships.api.PendingGroup;
import com.herocraftonline.townships.command.BasicInteractiveCommandState;
import com.herocraftonline.townships.groups.town.PendingTown;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: gabizou
 */
public class TownCreateCommand extends BasicTownInteractiveCommand {

    private Map<Player, String> pendingTownCreations = new HashMap<>();

    public TownCreateCommand(TownManager manager) {
        super(manager, "TownCreate");
        this.setStates(new StateA(), new StateB());
        setDescription("Creates a new town with the given name");
        setUsage("/town create");
        setIdentifiers("create");
        setPermission("townships.town.create");
    }

    @Override
    public String getCancelIdentifier() {
        return "cancel";
    }

    @Override
    public void onCommandCancelled(CommandSender executor) {
        if (!(executor instanceof Player))
            return;
        pendingTownCreations.remove(executor);
    }

    class StateA extends BasicInteractiveCommandState {

        public StateA() {
            super("create");
            setArgumentRange(0, 0);
        }

        @Override
        public boolean execute(CommandSender sender, String identifier, String[] args) {
            if (!(sender instanceof Player)) {
                Messaging.send(sender, getMessage("town_not_a_player"));
                cancelInteraction(sender);
                return false;
            }

            if (plugin.getTownManager().getGroups().size() >= manager.getTownConfig().maxTowns) {
                Messaging.send(sender, getMessage("town_create_max_reached"));
                cancelInteraction(sender);
                return false;
            }

            Player player = (Player) sender;
            Citizen citizen = getCitizen((Player) sender);
            TownManager tm = plugin.getTownManager();

            PendingTown pendingTown = citizen.getPendingTown();
            if (pendingTown == null) {
                Messaging.send(player, getMessage("town_create_invalid_charter"));
                cancelInteraction(sender);
                return false;
            }

            if (citizen.getTown() != null) {
                citizen.setPendingTown(null);
                citizen.setPendingTownOwner(false);
                Messaging.send(player, getMessage("town_create_already_in_town"));
                cancelInteraction(sender);
                return false;
            }

            if (!tm.isPending(pendingTown.name)) {
                Messaging.send(player, getMessage("town_create_pending_invalid"));
                cancelInteraction(sender);
                return false;
            }

            PendingGroup pg = tm.getPending(pendingTown.name);
            int minimumSignatures = manager.getTownConfig().getGroupTypeMinResidents(manager.getTownConfig().defaultTownType);
            if (minimumSignatures > pg.getMembers().size()) {
                Messaging.send(player, getMessage("town_create_insufficient_signatures"),
                        minimumSignatures - pg.getMembers().size());
                cancelInteraction(sender);
                return false;
            }

            int townCost = manager.getTownConfig().townCreationCost;
            if (Townships.econ != null && !Townships.econ.has(player.getName(), townCost)) {
                Messaging.send(player, getMessage("town_create_insufficient_econ"),
                        Townships.econ.currencyNamePlural(), Townships.econ.format(townCost));
                cancelInteraction(sender);
                return false;
            }

            Messaging.send(player, getMessage("town_create_warning1"), pendingTown.name);
            Messaging.send(player, getMessage("town_command_request_confirm"));
            pendingTownCreations.put(player, pendingTown.name);
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
            String tName = pendingTownCreations.remove(player);
            int townCost = manager.getTownConfig().townCreationCost;
            GroupType tType = manager.getTownConfig().defaultTownType;
            PendingGroup pg = Townships.getInstance().getTownManager().removePending(tName, false);

            if (Townships.econ != null && !Townships.econ.has(player.getName(), townCost)) {
                Messaging.send(player, getMessage("town_create_insufficient_econ"),
                        Townships.econ.currencyNamePlural(), Townships.econ.format(townCost));
                return false;
            } else if (Townships.econ != null) {
                Townships.econ.withdrawPlayer(player.getName(), townCost);
                Messaging.send(player, getMessage("town_create_econ_confirm"),
                        Townships.econ.format(townCost), tName);
            }

            Messaging.sendAnnouncement(plugin, getMessage("town_create_announcement"),
                    player.getName(), manager.getTownConfig().getGroupTypeName(tType), tName);
            //Create the town and add all players that signed the charter
            plugin.getTownManager().createTown(pg, tType, player.getName());

            return true;
        }
    }
}
