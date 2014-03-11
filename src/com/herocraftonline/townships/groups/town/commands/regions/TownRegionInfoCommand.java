package com.herocraftonline.townships.groups.town.commands.regions;

import com.herocraftonline.townships.api.Region;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.groups.town.commands.BasicTownCommand;
import com.herocraftonline.townships.util.Messaging;
import com.herocraftonline.townships.util.RegionPrintoutBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Author: gabizou
 */
public class TownRegionInfoCommand extends BasicTownCommand {

    public TownRegionInfoCommand(TownManager manager) {
        super(manager, "TownRegionInfo");
        setIdentifiers("region info", "r i");
        setUsage("/town region info <regionName>");
        setArgumentRange(1,1);
        setPermission("townships.town.region.info");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {

        // Get and validate the region ID
        String id = validateRegionId(args[0], false);

        Region region = manager.getRegionManager().validateRegion(id);
        if (region == null) {
            Messaging.send(executor, "No region Town Region or Sub Region found by the name of: " + args[0]);
            return true;
        }
        if (!(executor instanceof Player) && !manager.getRegionManager().checkGuest((Player) executor, region)) { // This player passes
            return true;
        }
        // Use WorldGuard's custom printout creator
        RegionPrintoutBuilder printout = new RegionPrintoutBuilder(region);
        printout.appendRegionInfo();
        printout.send(executor);
        return true;
    }
}
