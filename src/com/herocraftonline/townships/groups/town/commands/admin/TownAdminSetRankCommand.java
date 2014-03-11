package com.herocraftonline.townships.groups.town.commands.admin;

import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.Rank;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.groups.town.commands.BasicTownCommand;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Map;

/**
 * Author: gabizou
 */
public class TownAdminSetRankCommand extends BasicTownCommand {

    public TownAdminSetRankCommand(TownManager manager) {
        super(manager, "TownAdminSetRank");
        setDescription("Sets a citizens rank ");
        setUsage("/town admin setrank <town> <player> <rank>");
        setArgumentRange(3, 3);
        setIdentifiers("admin setrank", "a sr");
        setPermission("townships.town.admin");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {


        Town town = manager.get(args[0]);
        Citizen citizen = getCitizen(args[1]);
        if (town == null || citizen == null) {
            Messaging.send(sender, "The town or citizen is null! Please make sure you're typing the exact name correctly!");
            return true;
        }
        Rank rank = null;
        try {
            rank = Rank.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            for (Map.Entry<Rank, String> entry : town.getRanks().entrySet()) {
                if (entry.getValue().equalsIgnoreCase(args[1])) {
                    rank = entry.getKey();
                }
            }
        }
        if (rank == null) {
            Messaging.send(sender, "$1 is not a valid rank.", args[2]);
            return false;
        }

        if (!town.setNewCitizenRank(citizen.getName(), rank)) {
            Messaging.send(sender,getMessage("town_setrank_cancelled"), citizen.getName());
            return true;
        }
        if (rank.ordinal() >= Rank.CITIZEN.ordinal()) {
            citizen.setTown(town);
            plugin.getStorageManager().getStorage().saveCitizen(citizen, true);
        }
        plugin.getStorageManager().getStorage().saveCitizenGroup(town, true);

        String message = getMessage("town_setrank_announce");
        town.sendAnnouncement(message, citizen.getName(), rank);
        if (Bukkit.getPlayer(citizen.getName()) != null) {
            Messaging.send(Bukkit.getPlayer(citizen.getName()), getMessage("town_setrank_announce"), rank);
        }
        return true;
    }

}