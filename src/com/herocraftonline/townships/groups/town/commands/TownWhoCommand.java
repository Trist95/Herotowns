package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/**
 * Author: gabizou
 */
public class TownWhoCommand extends BasicTownCommand {

    public TownWhoCommand(TownManager manager) {
        super(manager, "TownWho");
        setDescription("Gives you information about an online citizen.");
        setUsage("/town who §9<player>");
        setArgumentRange(1, 1);
        setIdentifiers("who");
        setPermission("townships.town.who");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (player.hasPlayedBefore()) {
            Citizen citizen = getCitizen(player.getName());
            if (!citizen.hasTown()) {
                if (player.isOnline())
                    Messaging.send(executor, "$1 is not a member of a town!", player.getPlayer().getDisplayName());
                else
                    Messaging.send(executor, "$1 is not a member of a town!", player.getName());
                return true;
            }
            Town town = citizen.getTown();
            if (player.isOnline())
                Messaging.send(executor, "$1 is a $2 of $3", player.getPlayer().getDisplayName(), town.getRankName(citizen.getRank()), town.getName());
            else
                Messaging.send(executor, "$1 is a $2 of $3", player.getName(), town.getRankName(citizen.getRank()), town.getName());
            return true;
        } else {
            Messaging.send(executor, "$1 has not logged into this server yet!", args[0]);
            return true;
        }
    }
}
