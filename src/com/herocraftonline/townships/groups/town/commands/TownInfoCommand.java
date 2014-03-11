package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;

/**
 * Author: gabizou
 */
public class TownInfoCommand extends BasicTownCommand {

    public TownInfoCommand(TownManager manager) {
        super(manager, "TownInfo");
        setDescription("Gives you information about a town.");
        setUsage("/town info §9<town>");
        setArgumentRange(1, 1);
        setIdentifiers("info");
        setPermission("townships.town.info");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        Town town = plugin.getTownManager().get(args[0]);
        if (town == null) {
            Messaging.send(sender,getMessage("town_info_invalid"));
            return false;
        }
        Messaging.send(sender, getMessage("town_info_message"),
                town.getName(), manager.getTownConfig().getGroupTypeName(town.getType()), town.getCitizens().size());
        return true;
    }
}
