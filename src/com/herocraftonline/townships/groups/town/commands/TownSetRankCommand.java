package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.Rank;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Author: gabizou
 */
public class TownSetRankCommand extends BasicTownCommand {

    public TownSetRankCommand(TownManager manager) {
        super(manager, "TownSetRank");
        setDescription("Sets a citizens rank ");
        setUsage("/town setrank §9<player> <rank>");
        setArgumentRange(2, 2);
        setIdentifiers("setrank");
        setPermission("townships.town.setrank");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        if (!(sender instanceof Player)) {
            Messaging.send(sender, "Only players can manage their towns.");
            return false;
        }
        Citizen citizen = getCitizen((Player) sender);
        if (!citizen.hasTown() || citizen.getRank().ordinal() < Rank.MANAGER.ordinal()) {
            Messaging.send(sender, "You must be a town Administrator to do that.");
            return false;
        }

        Town town = citizen.getTown();
        Rank rank = null;
        try {
            rank = Rank.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            for (Map.Entry<Rank, String> entry : town.getRanks().entrySet()) {
                if (entry.getValue().equalsIgnoreCase(args[1])) {
                    rank = entry.getKey();
                }
            }
        }
        if (rank == null) {
            Messaging.send(sender, "$1 is not a valid rank.", args[1]);
            return false;
        }
        if (citizen.getRank().ordinal() <= rank.ordinal()) {
            Messaging.send(sender, "You may not set others to your rank.");
            return false;
        } else if (citizen.getRank() != Rank.OWNER && rank == Rank.OWNER) {
            Messaging.send(sender, "You may not de-rank the town $1", town.getRankName(rank));
            return false;
        }

        OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
        if (op == null) {
            Messaging.send(sender, "$1 has not been on the server.", args[0]);
            return false;
        }
        if (!town.hasCitizen(op.getName())) {
            if (rank.isOutsider()) {
                if (!town.setNewCitizenRank(op.getName(), rank)) {
                    Messaging.send(sender,getMessage("town_setrank_cancelled"), op.getName());
                    return true;
                }
            } else {
                Messaging.send(sender, "$1 must join the town before you can set their rank higher.", op.getName());
                return false;
            }
        } else {
            if (rank.isOutsider()) {
                Messaging.send(sender, "You must kick $1 from the town before setting their rank that low.", op.getName());
                return false;
            } else {
                if (!town.setNewCitizenRank(op.getName(), rank)) {
                    Messaging.send(sender,getMessage("town_setrank_cancelled"), op.getName());
                    return true;
                }
            }
        }

        String message = getMessage("town_setrank_announce");
        town.sendAnnouncement(message, op.getName(), rank);
        if (Bukkit.getPlayer(op.getName()) != null) {
            Messaging.send(Bukkit.getPlayer(op.getName()), getMessage("town_setrank_player"), rank);
        }
        return true;
    }

}
