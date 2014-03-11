package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Author: gabizou
 */
public class TownChatCommand extends BasicTownCommand {

    public TownChatCommand(TownManager manager) {
        super(manager, "TownChat");
        setDescription("Sends messages to your party");
        setUsage("/townchat §9<msg> OR /tc §9<msg>");
        setArgumentRange(0, 1000);
        setIdentifiers("tc", "townchat");
        setPermission("townships.town.chat");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        if (!(sender instanceof Player)) {
            Messaging.send(sender, getMessage("town_not_a_player"));
            return false;
        }
        Player player = (Player) sender;
        Citizen citizen = getCitizen(player);
        Town town = citizen.getTown();
        if(town == null) {
            Messaging.send(player, getMessage("town_not_part_of_town"));
            return false;
        }
        switch (args.length) {
            case 0 :
                Messaging.send(sender, getUsage());
                return true;
            default :
                String msg = "";
                for (String word : args) {
                    msg += word + " ";
                }
                town.sendChatMessage(citizen, msg.trim());
                return true;
        }
    }
}
