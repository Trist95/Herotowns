package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.PendingGroup;
import com.herocraftonline.townships.groups.town.PendingTown;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Author: gabizou
 */
public class TownCharterCommand extends BasicTownCommand {

    public TownCharterCommand(TownManager manager) {
        super(manager, "TownCharter");
        setDescription("Creates or removes a charter for a new town!");
        setUsage("/town §9charter <create|remove|list> <town>");
        setArgumentRange(1, 2);
        setIdentifiers("charter");
        setPermission("townships.town.charter");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        if (executor instanceof ConsoleCommandSender) {
            Messaging.send(executor, getMessage("town_charter_invalid_sender"));
        } else if (executor instanceof Player) {
            Player player = plugin.getServer().getPlayer(executor.getName());
            switch (args[0].toLowerCase()) {
                case "list" :
                    return list(player,args);
                case "create" :
                    if (plugin.getTownManager().getGroups().size() >= manager.getTownConfig().maxTowns) {
                        Messaging.send(executor, getMessage("town_charter_max_reached"));
                        return false;
                    }
                    Citizen cit = getCitizen((Player) executor);
                    if (cit.hasTown() || cit.getPendingTown() != null) {
                        Messaging.send(executor, getMessage("town_charter_already_in_town"));
                        return false;
                    }
                    switch (args.length) {
                        case 1 :
                            Messaging.send(executor, getMessage("town_charter_invalid_name"));
                            return false;
                        case 2 :
                            if (townAlreadyExists(args[1])) {
                                Messaging.send(executor, getMessage("town_charter_town_exists"));
                                return false;
                            } else
                                return create((Player) executor, args[1]);
                        default :
                            Messaging.send(executor, getMessage("town_charter_invalid_command"));
                            return false;
                    }
                case "remove" :
                    switch (args.length) {
                        case 1 :
                            Messaging.send(executor, getMessage("town_charter_remove_invalid"));
                            return false;
                        case 2 :
                            return remove((Player) executor, args[1]);
                        default :
                            return false;
                    }
                default :
                    Messaging.send(executor, "That is an invalid command! Use :$1", getUsage());
                    return false;
            }
        }
        return true;
    }


    private boolean create(Player player, String name) {
        if (townAlreadyExists(name))
            return false;
        else {
            Citizen cit = getCitizen(player);
            HashSet<String> citizens = new HashSet<>();
            citizens.add(player.getName());
            PendingTown pendingTown = new PendingTown(name, citizens, System.currentTimeMillis());
            pendingTown.setOwner(player.getName());
            cit.setPendingTown(pendingTown);
            cit.setPendingTownOwner(true);
            plugin.getTownManager().addPending(pendingTown);
            Messaging.send(player, getMessage("town_charter_create_message"), name);
            Messaging.sendAnnouncement(plugin, getMessage("town_charter_create_announcement"),
                    player.getDisplayName(), name);
            plugin.getStorageManager().getStorage().saveCitizen(cit,true);
            return true;
        }
    }

    private boolean list(Player player, String[] args) {
        Map<String,PendingGroup> pending = plugin.getTownManager().getPending();
        List<String> names = new ArrayList<>(pending.keySet().size());
        for (Map.Entry<String,PendingGroup> entry : pending.entrySet()) {
             names.add(entry.getValue().name);
        }
        if (pending.isEmpty())
            Messaging.send(player, getMessage("town_charter_list_none"));
        else {
            String[] lines = Messaging.formatCollection(names);
            Messaging.send(player, getMessage("town_charter_list"));
            for (String name : lines) {
                Messaging.send(player, name);
            }
        }
        return true;
    }

    private boolean remove(Player player, String args) {
        Citizen c = getCitizen(player);
        if (!c.isPendingTownOwner()) {
            Messaging.send(player, getMessage("town_charter_remove_no_permission"));
            return false;
        }
        String name = c.getPendingTown().name;
        plugin.getTownManager().removePending(name, true);
        Messaging.sendAnnouncement(plugin, getMessage("town_charter_invalid_remove"), name);
        return false;
    }

    private boolean townAlreadyExists(String name) {
        return plugin.getTownManager().getGroups().contains(name) ||
               plugin.getTownManager().getPending().containsKey(name);
    }
}
