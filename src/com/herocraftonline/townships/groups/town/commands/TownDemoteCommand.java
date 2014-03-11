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

/**
 * Author: gabizou
 */
public class TownDemoteCommand extends BasicTownCommand {

    public TownDemoteCommand(TownManager manager) {
        super(manager, "TownDemote");
        setDescription("Demotes a player to a lower rank in your town.");
        setUsage("/town demote §9<player>");
        setArgumentRange(1, 1);
        setIdentifiers("demote");
        setPermission("townships.town.demote");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        if (!(sender instanceof Player)) {
            Messaging.send(sender, getMessage("town_not_a_player"));
            return false;
        }

        Citizen citizen = getCitizen((Player) sender);
        if (!citizen.hasTown()) {
            Messaging.send(sender, getMessage("town_not_part_of_town"));
            return false;
        } else if (!citizen.getRank().canManage()) {
            Messaging.send(sender, getMessage("town_insufficient_perm"));
            return false;
        }

        Town town = citizen.getTown();
        OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
        if (!town.hasMember(op.getName())) {
            Messaging.send(sender, getMessage("town_demote_invalid_member"), op.getName());
            return false;
        }

        Rank rank = town.getCitizenRank(op.getName());
        if (rank == Rank.CITIZEN) {
            Messaging.send(sender, getMessage("town_demote_can_not_demote_further"));
            return false;
        } else if (citizen.getRank().ordinal() <= rank.ordinal() && citizen.getRank() != Rank.OWNER) {
            Messaging.send(sender, getMessage("town_demote_insufficient_rank"));
            return false;
        }

        town.setNewCitizenRank(op.getName(), Rank.values()[(rank.ordinal() - 1)]);
        //Notify town members
        town.sendAnnouncement(getMessage("town_demote_announcement"),
                op.getName(), town.getRankName(town.getCitizenRank(op.getName())));
        plugin.getStorageManager().getStorage().saveCitizenGroup(town,true);
        return true;
    }

}
