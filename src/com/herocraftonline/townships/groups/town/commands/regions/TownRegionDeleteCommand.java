package com.herocraftonline.townships.groups.town.commands.regions;

import com.herocraftonline.townships.api.ChildRegion;
import com.herocraftonline.townships.api.Region;
import com.herocraftonline.townships.command.BasicInteractiveCommandState;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.groups.town.commands.BasicTownInteractiveCommand;
import com.herocraftonline.townships.util.JSONMessage;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: gabizou
 */
public class TownRegionDeleteCommand extends BasicTownInteractiveCommand {

    private Map<String, ChildRegion> pending = new HashMap<>();
    private Map<String, List<ChildRegion>> pendingChildRegions = new HashMap<>();


    public TownRegionDeleteCommand(TownManager manager) {
        super(manager, "TownRegionDelete");
        setUsage("/town region delete <regionName>");
        setStates(new StateA(), new StateB());
        setIdentifiers("region delete", "r d");
        setPermission("townships.town.region.delete");
    }


    class StateA extends BasicInteractiveCommandState {

        public StateA() {
            super("region delete");
            setArgumentRange(1,1);
        }

        @Override
        public boolean execute(CommandSender executor, String identifier, String[] args) {
            Region region = manager.getRegionManager().validateRegion(args[0]);
            if (region == null) {
                Messaging.send(executor, "No region Town Region or Sub Region found by the name of: " + args[0]);
                cancelInteraction(executor);
                return true;
            }
            if (!(region instanceof ChildRegion)) {
                Messaging.send(executor, "Can not remove Town Regions, town must be deleted for a region to be removed!");
                cancelInteraction(executor);
                return true;
            }
            ChildRegion childRegion = (ChildRegion) region;
            if (executor instanceof Player && !manager.getRegionManager().checkOwner((Player) executor, region)) {
                Messaging.send(executor, "You must be a Town Mayor or the Owner of this region to remove it!");
                cancelInteraction(executor);
                return true;
            }
            List<ChildRegion> regions = manager.getRegionManager().getChildRegionChildren(childRegion);
            if (regions.size() > 0) {
                Messaging.send(executor, "There are regions that are children of this region!");
                Messaging.send(executor, "Sub regions of these children will also be REMOVED!");
                pendingChildRegions.put(executor.getName(), regions);
            }
            pending.put(executor.getName(), childRegion);
            Messaging.send(executor, "You are about to remove $1, all permission groups will be wiped.", childRegion.getName());
            if (executor instanceof Player) { // Let's use this suggestion
                JSONMessage message = new JSONMessage("If you are sure you want to remove this region, please type ")
                        .then(ChatColor.GREEN + "/town confirm").suggest("/town confirm")
                        .then(ChatColor.GRAY + " otherwise, ")
                        .then(ChatColor.RED + "/town cancel").suggest("/town cancel");
                message.send((Player) executor);
            } else
                Messaging.send(executor, "If you are sure you want to remove this region, please type /town confirm otherwise, /town cancel");
            return true;
        }
    }

    class StateB extends BasicInteractiveCommandState {

        public StateB() {
            super("confirm");
            setArgumentRange(0,0);
        }

        @Override
        public boolean execute(CommandSender executor, String identifier, String[] args) {
            List<ChildRegion> childRegions = pendingChildRegions.remove(executor.getName());
            ChildRegion region = pending.remove(executor.getName());

            if (childRegions != null)
            for (ChildRegion childRegion : childRegions) {
                manager.getRegionManager().removeChildRegion(childRegion.getParent(), childRegion);
            }

            manager.getRegionManager().removeChildRegion(region.getParent(), region);
            Messaging.send(executor, "You have successfully removed the subregion: " + region.getName());
            plugin.getStorageManager().getStorage().saveRegionManagerData(manager.getRegionManager());
            return true;
        }
    }

    @Override
    public String getCancelIdentifier() {
        return "cancel";
    }

    @Override
    public void onCommandCancelled(CommandSender executor) {
        if (!(executor instanceof Player)) {
            return;
        }
        pending.remove(((Player) executor).getPlayer().getName());
    }
}
