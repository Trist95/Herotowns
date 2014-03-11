package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.Rank;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Author: gabizou
 */
public class TownInvitesCommand extends BasicTownCommand {

    public TownInvitesCommand(TownManager manager) {
        super(manager, "TownInvites");
        setDescription("Lists all invites that you currently have.");
        setUsage("/town invites");
        setArgumentRange(0, 0);
        setIdentifiers("invites");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        if (!(sender instanceof Player)) {
            Messaging.send(sender, "Only players can recieve invites!");
            return false;
        }

        Citizen citizen = getCitizen((Player) sender);
        if (citizen.hasTown() && citizen.getRank().ordinal() < Rank.MANAGER.ordinal()) {
            Messaging.send(sender, "You are already part of a town!");
            return false;
        } else if (citizen.hasTown()) {
            Messaging.send(sender, "$1 has invited the following players: $2", citizen.getTown().getName(), citizen.getTown().getInvites().toString().replace("[", "").replace("]", ""));
            return true;
        }
        if (citizen.hasInvites()) {
            Messaging.send(sender, "You have invites from the following towns/guilds: $1", citizen.getInviteNames().toString().replace("[", "").replace("]", ""));
        } else {
            Messaging.send(sender, "You currently have no pending town invitations");
        }
        return true;
    }
}
