package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.Rank;
import com.herocraftonline.townships.api.Relation;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Author: gabizou
 */
public class TownRelationCommand extends BasicTownCommand {

    public TownRelationCommand(TownManager manager) {
        super(manager, "townrelation");
        setIdentifiers("ally","war","neutral");
        setArgumentRange(1,19);
        setUsage("/town <ally|war|neutral> <town1> <town2> etc...");
        setDescription("Assigns a Town Relation with another town. " +
                "This marks all citizens of the town to the provided relation.");
        setPermission("townships.town.relation");

    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        if (executor instanceof Player) {
            Citizen citizen = getCitizen((Player) executor);

            if (!citizen.hasTown()) {
                // TODO Make this a resource message.
                Messaging.send(executor,"You are not in a town! You must be in a town to use Town Relations.");
                return false;
            }
            Town town = citizen.getTown();
            if (town.getCitizenRank(citizen).ordinal() <= Rank.MANAGER.ordinal()) {
                Messaging.send(executor, "You are not high enough rank to set your town's relations!");
                return false;
            }
            for (String townName : args) {
                Town relatedTown = plugin.getTownManager().get(townName);
                if (relatedTown != null) {
                    // We guarantee that the identifier is the relation we want since it's the commands identifier
                    town.addGroupRelation(relatedTown, Relation.valueOf(identifier.toUpperCase()));
                    Messaging.send(executor, "Added " + relatedTown.getName() + " to " + town.getName() +
                            "'s relations of: " + Relation.valueOf(identifier.toUpperCase()));
                } else {
                    Messaging.send(executor, "The town: " + townName + " does not exist or is mispelled!");
                }
            }
            plugin.getStorageManager().getStorage().saveCitizenGroup(town,true);
            return true;

        }
        if (args.length < 2) {
            Messaging.send(executor, "You must provide the town whose relation ");
            return false;
        }
        Town town = plugin.getTownManager().get(args[0]);
        if (town == null) {
            return false;
        }
        for (int i = 1; i < args.length; i++) {
            Town relatedTown = plugin.getTownManager().get(args[i]);
            town.addGroupRelation(relatedTown, Relation.valueOf(identifier.toUpperCase()));
            Messaging.send(executor, "Added " + relatedTown.getName() + " to " + town.getName() + "'s relations of: "
                    + Relation.valueOf(identifier.toUpperCase()));
        }
        plugin.getStorageManager().getStorage().saveCitizenGroup(town,true);

        return false;
    }
}
