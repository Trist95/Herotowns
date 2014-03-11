package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.Rank;
import com.herocraftonline.townships.api.events.RankChangeEvent;
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
public class TownPromoteCommand extends BasicTownCommand {

    public TownPromoteCommand(TownManager manager) {
        super(manager, "TownPromote");
        setDescription("Promotes a player to a higher rank in your town.");
        setUsage("/town promote §9<player>");
        setArgumentRange(1, 1);
        setIdentifiers("promote");
        setPermission("townships.town.promote");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        // TODO Message.properties this command.
        if (!(sender instanceof Player)) {
            Messaging.send(sender, "Only players can issue town commands.");
            return false;
        }

        Citizen citizen = getCitizen((Player) sender);
        if (!citizen.hasTown()) {
            Messaging.send(sender, "You are not part of a town!");
            return false;
        }

        if (!citizen.getRank().canManage()) {
            Messaging.send(sender, "You are not a high enough rank to do that!");
            return false;
        }

        Town town = citizen.getTown();
        OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
        if (sender.equals(op.getPlayer())) {
            Messaging.send(sender, "You can not promote yourself!");
            return false;
        }


        if (!town.hasMember(op.getName())) {
            Messaging.send(sender, "$1 is not a member of this town, they must be invited to join the town!", op.getName());
            return false;
        }

        Rank rank = town.getCitizenRank(op.getName());
        if (citizen.getRank().ordinal() <= rank.ordinal() + 1) {
            Messaging.send(sender, "You can't promote someone to your own rank!");
            return false;
        }

        town.setNewCitizenRank(op.getName(), Rank.values()[(rank.ordinal() + 1)]);
        plugin.getStorageManager().getStorage().saveCitizenGroup(town, false);
        Bukkit.getPluginManager().callEvent(new RankChangeEvent(op.getName(), town, rank, Rank.values()[rank.ordinal() + 1]));
        //Notify town members
        String message = "$1 has been promoted to $2.".replace("$1", op.getName()).replace("$2", town.getRankName(town.getCitizenRank(op.getName())));
        town.sendAnnouncement(message);
        Messaging.send(sender, message);
        return true;
    }
}
