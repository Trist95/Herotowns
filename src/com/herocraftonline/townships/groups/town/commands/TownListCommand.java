package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.Rank;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Author: gabizou
 */
public class TownListCommand extends BasicTownCommand {

    public TownListCommand(TownManager manager) {
        super(manager, "TownList");
        setDescription("Gives you information about a town.");
        setUsage("/town list <town> <members|nonmembers|online> [page]");
        setArgumentRange(0, 3);
        setIdentifiers("list");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        if (args.length == 0) {
            Messaging.send(sender, "Towns: ");
            Messaging.send(sender, Messaging.formatCitizenGroup(plugin.getTownManager().getGroups()));
            return true;
        }


        Citizen citizen = getCitizen((Player) sender);
        if (!citizen.hasTown()) {
            // TODO Convert this message to getMessage()
            Messaging.send(sender, "You must be a part of a town to list the members or non-members");
            return false;
        }

        Map<String, Rank> nameAndRank;
        switch (args[0].toLowerCase()) {
            case "members" :
                nameAndRank = citizen.getTown().getCitizens();
                break;
            case "nonmembers" :
                nameAndRank = citizen.getTown().getNoncitizens();
                break;
            case "guests" :
                nameAndRank = new HashMap<>();
                for (Map.Entry<String,Rank> guest : citizen.getTown().getNoncitizens().entrySet()) {
                    if (guest.getValue() == Rank.GUEST)
                        nameAndRank.put(guest.getKey(),guest.getValue());
                }
                break;
            default :
                Messaging.send(sender, getUsage());
                return false;
        }

        List<String> names = new ArrayList<>(nameAndRank.keySet());
        Collections.sort(names);
        int start = 0;
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
                start = (page - 1) * 8;
            } catch (NumberFormatException e) {
                // TODO Convert this message to getMessage()
                Messaging.send(sender, "You must specify a number as the second argument!");
                return false;
            }
        }
        int totalPages = (int) Math.ceil(names.size() / 8.0);
        if (totalPages < page) {
            // TODO Convert this message to getMessage()
            Messaging.send(sender, "There are only $1 pages of $2", totalPages, args[0]);
            return false;
        }

        // TODO Convert this message to getMessage()
        Messaging.send(sender, "Showing page $1 of $2", page, totalPages);
        int finish = (page * 8);
        if (finish > names.size())
            finish = names.size();
        for (int i = start; i < finish; i++ ) {
            Messaging.send(sender, "$1  -  $2", names.get(i), citizen.getTown().getRankName(nameAndRank.get(names.get(i))));
        }
        return true;
    }
}
