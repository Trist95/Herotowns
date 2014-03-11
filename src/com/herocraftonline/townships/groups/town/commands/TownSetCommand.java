package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;

/**
 * Author: gabizou
 */
public class TownSetCommand extends BasicTownCommand {

    public TownSetCommand(TownManager manager)
    {
        super(manager, "TownSet");
        setDescription("Sets a property of a town");
        setUsage("/town §9set <town> <name|prefix> <value>");
        setArgumentRange(3, 3);
        setIdentifiers("set");
        setPermission("townships.town.set");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        Town town = plugin.getTownManager().get(args[0]);

        if (args[1].equalsIgnoreCase("name")) {
            town.setDisplayName(args[2]);
            Messaging.send(sender, "You have set the display name of " + args[0] + " to " + args[2]);
            plugin.getStorageManager().getStorage().saveCitizenGroup(town,true);
        }
        else if (args[1].equalsIgnoreCase("prefix")) {
            town.setPrefix(args[2]);
            Messaging.send(sender, "You have set the prefix of " + args[0] + " to " + args[2]);
            plugin.getStorageManager().getStorage().saveCitizenGroup(town,true);
        }
        else {
            Messaging.send(sender, "Invalid property!");
            return false;
        }

        return true;
    }

}
