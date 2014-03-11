package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.Rank;
import com.herocraftonline.townships.command.BasicInteractiveCommandState;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: gabizou
 */
public class TownKickCommand extends BasicTownInteractiveCommand {

    private Map<Player, OfflinePlayer> pendingTownKicks = new HashMap<>();

    public TownKickCommand(TownManager manager) {
        super(manager,"TownKick");
        setDescription("Kicks a player from the town.");
        setUsage("/town kick <player>");
        setArgumentRange(1, 1);
        setIdentifiers("kick");
        setPermission("townships.town.kick");
        setStates(new StateA(), new StateB());
    }

    @Override
    public String getCancelIdentifier() {
        return "cancel";
    }

    @Override
    public void onCommandCancelled(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return;
        }
        pendingTownKicks.remove(sender.getName());
    }

    class StateA extends BasicInteractiveCommandState {

        public StateA() {
            super("kick");
            setArgumentRange(1, 1);
        }

        @Override
        public boolean execute(CommandSender sender, String identifiers, String[] args) {
            if (!(sender instanceof Player)) {
                Messaging.send(sender, "Only players may kick citizens from a town.");
                cancelInteraction(sender);
                return false;
            }
            Player player = (Player) sender;
            Citizen citizen = plugin.getCitizenManager().getCitizen((Player) sender);
            if (!citizen.hasTown()) {
                Messaging.send(player, "You don't have a town!");
                cancelInteraction(sender);
                return false;
            }

            if (citizen.getRank().ordinal() < Rank.MANAGER.ordinal()) {
                Messaging.send(player, "You aren't a high enough rank to kick citizens.");
                cancelInteraction(sender);
                return false;
            }

            Town town = citizen.getTown();
            OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
            if (op == null) {
                Messaging.send(player, "$1 could not be found.", args[0]);
                cancelInteraction(sender);
                return false;
            } else if (!town.hasMember(op.getName())) {
                Messaging.send(player, "$1 is not a member of your town.", op.getName());
                cancelInteraction(sender);
                return false;
            }

            if (town.getCitizenRank(op.getName()).ordinal() >= citizen.getRank().ordinal()) {
                Messaging.send(player, "You may not kick citizens of equal rank!");
                cancelInteraction(sender);
                return false;
            }

            pendingTownKicks.put(player, op);
            Messaging.send(player, "You are about to kick $1.", op.getName());
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

            Town town = plugin.getCitizenManager().getCitizen(player).getTown();
            OfflinePlayer op = pendingTownKicks.remove(player);
            String name = op.getName();
            if (op.isOnline()) {
                Citizen c = plugin.getCitizenManager().getCitizen(op.getPlayer());
                c.setTown(null);
                Messaging.send((Player) op, "You have been kicked from $1", town.getName());
                plugin.getStorageManager().getStorage().saveCitizen(c, true);
            }
            town.removeMember(name);


            //Notify town members
            String message = "$1 has been kicked from the town.".replace("$1", name);
            town.sendAnnouncement(message);
            plugin.getStorageManager().getStorage().saveCitizenGroup(town, true);
            return true;
        }
    }
}
