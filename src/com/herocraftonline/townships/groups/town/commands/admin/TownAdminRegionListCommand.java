package com.herocraftonline.townships.groups.town.commands.admin;

import com.herocraftonline.townships.api.CitizenGroup;
import com.herocraftonline.townships.api.Region;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.groups.town.commands.BasicTownCommand;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author: gabizou
 */
public class TownAdminRegionListCommand extends BasicTownCommand {

    public TownAdminRegionListCommand(TownManager manager) {
        super(manager, "TownAdminRegionListCommand");
        setDescription("Gives you a list of registered Town regions.");
        setUsage("/town admin rglist [page]");
        setArgumentRange(0, 3);
        setIdentifiers("admin rglist");
        setPermission("townships.town.admin");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        if (args.length == 0) {
            Messaging.send(sender, "Towns with Regions: ");
            Messaging.send(sender, Messaging.formatCitizenGroup(plugin.getTownManager().getGroups()));
            return true;
        }


        Map<CitizenGroup, Region> nameAndRank = manager.getRegionManager().getRegions();

        List<String> names = new ArrayList<>();
        for (Map.Entry<CitizenGroup, Region> entry : nameAndRank.entrySet()) {
            names.add(entry.getKey().getName());
        }

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
            Messaging.send(sender, "$1  -  $2", names.get(i));
        }
        return true;
    }
}
