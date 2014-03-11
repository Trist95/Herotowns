package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.groups.town.HerochatManager;
import com.herocraftonline.townships.groups.town.TownManager;
import org.bukkit.command.CommandSender;

/**
 * Author: gabizou
 */
public class TownChatHerochatCommand extends BasicTownCommand {

    private HerochatManager chatManager = null;

    public TownChatHerochatCommand(TownManager manager) {
        super(manager, "TownChat");
        setDescription("Sends messages to your party");
        setUsage("/townchat §9<msg> OR /tc §9<msg>");
        setArgumentRange(0, 1000);
        setIdentifiers("tc", "townchat");
        setPermission("townships.town.chat");

    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        /*
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
        if (plugin.getChatManager().hasGroupChannel(town))
            switch (args.length) {
                case 0 :
                    if (manager.getTownConfig().channelsenabled && chatManager != null) {
                        return chatManager.joinGroupChannel(citizen);
                    } else {
                        Messaging.send(sender,getMessage("town_channel_doesnt_exist"));
                        return true;
                    }
                default :
                    if (manager.getTownConfig().channelsenabled && chatManager != null) {
                        return chatManager.sendMessageToTownChannel(citizen, args);
                    } else {
                        Messaging.send(sender,getMessage("town_channel_doesnt_exist"));
                        return true;
                    }
            }
        else {
            Messaging.send(player, getMessage("town_channel_doesnt_exist"));
            return false;
        }
        */
        return true;
    }
}
