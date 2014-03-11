package com.herocraftonline.townships.groups.town.commands.regions;

import com.herocraftonline.townships.api.Region;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.groups.town.commands.BasicTownCommand;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Author: gabizou
 */
public class TownRegionRemoveCommand extends BasicTownCommand {

    public TownRegionRemoveCommand(TownManager manager) {
        super(manager, "TownRegionRemove");
        setDescription("Promotes a player to a higher rank in your town.");
        setUsage("/town region remove <guest|manager|owner> <region> <player1> <player2> etc.");
        setArgumentRange(3,10);
        setIdentifiers("region remove", "r r");
        setPermission("townships.town.region.remove");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        args[0] = args[0].toLowerCase();

        Region region = manager.getRegionManager().validateRegion(args[1]);
        if (region == null) {
            Messaging.send(executor, "Can not find a region by the name: " + args[1]);
            return true;
        }
        String[] newargs = Arrays.copyOfRange(args, 2, args.length);

        switch (args[0]) {
            case "guest":
                if (executor instanceof Player)
                    return manager.getRegionManager().checkManager((Player) executor, region) && removeMember(region, executor, newargs);
                return removeMember(region, executor, newargs);
            case "manager":
            case "owner":
                if (executor instanceof Player)
                    return manager.getRegionManager().checkOwner((Player) executor, region) && removeMember(region, executor, newargs);
                return removeMember(region, executor, newargs);
            default:
                displayHelp(executor);
                return true;
        }
    }

    private boolean removeMember(Region region, CommandSender sender, String... args) {
        String playerlist = "";
        for (String name : args) {
            region.removeMember(name);
            playerlist = playerlist + " " + name;
        }
        Messaging.send(sender, "You have successfully removed the following people from the region: " + playerlist);
        plugin.getStorageManager().getStorage().saveRegionManagerData(manager.getRegionManager());
        return true;
    }
}

