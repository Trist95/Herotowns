package com.herocraftonline.townships.groups.town.commands.admin;

import com.herocraftonline.townships.api.CitizenGroupManager;
import com.herocraftonline.townships.command.BasicCommand;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;

/**
 * Author: gabizou
 */
public class TownAdminAddCitizenCommand extends BasicCommand {

    public TownAdminAddCitizenCommand(CitizenGroupManager manager) {
        super(manager, "TownAdminAddCitizen");
        setDescription("Send an invite to a player to join a Town.");
        setUsage("/town admin add <town> <citizen|manager|successor|owner> <playername>");
        setArgumentRange(3,3);
        setIdentifiers("admin add", "a a");
        setPermission("townships.town.admin");

    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        Messaging.send(sender, "Doesn't do anything yet!");
        return true;
    }
}