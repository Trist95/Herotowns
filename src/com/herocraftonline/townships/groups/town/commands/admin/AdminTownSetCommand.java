package com.herocraftonline.townships.groups.town.commands.admin;

import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.groups.town.commands.BasicTownCommand;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;

/**
 * Author: gabizou
 */
public class AdminTownSetCommand extends BasicTownCommand {

    public AdminTownSetCommand(TownManager manager) {
        super(manager, "AdminTownSet");
        setDescription("Sets data for a town or guild.");
        setUsage("/town admin §9<townName> <owner|kick|promote|demote|invite|destroy> <option>");
        setArgumentRange(2, 4);
        setIdentifiers("town admin");
        setPermission("townships.admin");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        if (args[1].equalsIgnoreCase("destroy")) {
            return true;
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("tax")) {
            } else
                Messaging.send(executor, "You must specify an additional option!");
            return true;
        } else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("owner")) {
            } else if (args[1].equalsIgnoreCase("demote")) {
            } else if (args[1].equalsIgnoreCase("promote")) {
            } else if (args[1].equalsIgnoreCase("invite")) {
            } else if (args[1].equalsIgnoreCase("kick")) {
            } else if (args[1].equalsIgnoreCase("list")) {
            }
        } else if (args.length == 4) {
            if (args[1].equalsIgnoreCase("list")) {
            }
        }
        return true;
    }

}
