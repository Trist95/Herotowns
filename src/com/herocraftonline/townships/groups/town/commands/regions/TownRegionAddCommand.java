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
public class TownRegionAddCommand extends BasicTownCommand {

    public TownRegionAddCommand(TownManager manager) {
        super(manager, "TownRegionAdd");
        setDescription("Promotes a player to a higher rank in your town.");
        setUsage("/town region add <guest|manager|owner> <region> <player1> <player2> etc.");
        setArgumentRange(3,10);
        setIdentifiers("region add", "r a");
        setPermission("townships.town.region.add");

    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        args[0] = args[0].toLowerCase();

        Region region = manager.getRegionManager().validateRegion(args[1]);
        if (region == null) {
            Messaging.send(executor, "Can not find a region by the name: " + args[1]);
            return true;
        }
        String[] newargs = Arrays.copyOfRange(args, 2,args.length);

        switch (args[0]) {
            case "guest":
                if (executor instanceof Player)
                    return manager.getRegionManager().checkManager((Player) executor, region) && addGuest(region, executor, newargs);
                return addGuest(region, executor, newargs);
            case "manager":
                if (executor instanceof Player)
                    return manager.getRegionManager().checkOwner((Player) executor, region) && addManager(region, executor, newargs);
                return addManager(region, executor, newargs);
            case "owner":
                if (executor instanceof Player)
                    return manager.getRegionManager().checkOwner((Player) executor, region) && addOwner(region, executor, newargs);
                return addOwner(region, executor, newargs);
            default:
                displayHelp(executor);
                return true;
        }
    }

    private boolean addGuest(Region region, CommandSender sender, String[] args) {
        String playerlist = "";
        for (String playerName : args) {
            region.addGuest(playerName);
            playerlist = playerlist + " " + playerName;
        }
        Messaging.send(sender, "You have successfully added the following guests to the region: " + playerlist);
        plugin.getStorageManager().getStorage().saveRegionManagerData(manager.getRegionManager());
        return true;
    }

    private boolean addManager(Region region, CommandSender sender, String[] args) {
        String playerlist = "";
        for (String playerName : args) {
            region.addManager(playerName);
            playerlist = playerlist + " " + playerName;
        }
        Messaging.send(sender, "You have successfully added the following managers to the region: " + playerlist);
        plugin.getStorageManager().getStorage().saveRegionManagerData(manager.getRegionManager());
        return true;
    }

    private boolean addOwner(Region region, CommandSender sender, String[] args) {
        String playerlist = "";
        for (String playerName : args) {
            region.addOwner(playerName);
            playerlist = playerlist + " " + playerName;
        }
        Messaging.send(sender, "You have successfully added the following owners to the region: " + playerlist);
        plugin.getStorageManager().getStorage().saveRegionManagerData(manager.getRegionManager());
        return true;
    }
}
