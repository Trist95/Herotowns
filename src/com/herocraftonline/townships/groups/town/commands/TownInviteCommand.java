package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: gabizou
 */
public class TownInviteCommand extends BasicTownCommand {

    public TownInviteCommand(TownManager manager) {
        super(manager, "TownInvite");
        setDescription("Send an invite to a player to join a Town.");
        setUsage("§c/town §binvite §a<playername>");
        setArgumentRange(1,5);
        setIdentifiers("invite");
        setPermission("townships.town.invite");

    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        if (!(sender instanceof Player)) {
            Messaging.send(sender, getMessage("town_not_a_player"));
            return false;
        }

        Player player = (Player) sender;
        Citizen citizen = getCitizen(player);
        if (!citizen.getRank().canManage()) {
            Messaging.send(player, getMessage("town_invite_insufficient_perm"));
            return false;
        }

        List<String> removed = new ArrayList<>();
        List<String> added = new ArrayList<>();
        Town town = citizen.getTown();

        if (town == null) {
            Messaging.send(player, getMessage("town_not_part_of_town"));
            return true;
        }


        for (String name : args) {
            if (name.startsWith("-")) {
                name = name.replace("-", "");
                if (town.removeInvite(name)) {
                    citizen.removeInvite(town);
                    removed.add(name);
                }
            } else if (name.startsWith("+")) {
                if (town.getCitizens().size() > manager.getTownConfig().getGroupTypeMaxResidents(town.getType())) {
                    Messaging.send(player,"Can't invite this player because your town already has reached the max citizen count!");
                    continue;
                }
                name = name.replace("+", "");
                Player target = plugin.getServer().getPlayer(name);
                if (target == null) {
                    Messaging.send(sender, getMessage("town_invite_not_online"), name);
                }
                else {
                    Citizen citi = getCitizen(target);
                    if (citi == null) {
                        Messaging.send(player, getMessage("town_invite_not_online"), name);
                        continue;
                    }
                    if (citi.getTown() != null) {
                        Messaging.send(player, getMessage("town_invite_already_in_town"), name);
                        continue;
                    }
                    town.addInvite(target.getName());
                    citi.addInvite(town);
                    added.add(target.getName());
                    Messaging.send(target, getMessage("town_invite_invite_sent1"),
                            player.getDisplayName(), town.getName());
                    Messaging.send(target, getMessage("town_invite_invite_sent2"), town.getName());
                }
            } else {
                Messaging.send(player, getMessage("town_invite_invalid_entry"));
            }
        }
        //Let our player know who was invited or removed.
        if (!added.isEmpty()) {

            Messaging.send(player, getMessage("town_invite_added_confirm"));
            for (String name : added) {
                Messaging.send(player, getMessage("town_invite_added_player"),name);
            }
        }

        if (!removed.isEmpty()) {
            Messaging.send(player, getMessage("town_invite_removed_confirm"));
            for (String name : removed) {
                Messaging.send(player, getMessage("town_invite_removed_player"), name);
            }
        }
        return true;
    }
}
