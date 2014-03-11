package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.GroupType;
import com.herocraftonline.townships.command.BasicInteractiveCommandState;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.groups.town.TownUtil;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: gabizou
 */
public class TownUpgradeCommand extends BasicTownInteractiveCommand {

    Set<String> pending = new HashSet<>();

    public TownUpgradeCommand(TownManager manager) {
        super(manager, "TownUpgrade");
        setDescription("Upgrades your town to the next size, can only be used if" +
                " your town has the requried materials in it's bank!");
        setUsage("/town upgrade");
        setStates(new StateA(), new StateB());
        setPermission("townships.town.upgrade");
    }

    @Override
    public String getCancelIdentifier() {
        return "cancel";
    }

    @Override
    public void onCommandCancelled(CommandSender sender) {
        if (sender instanceof Player) {
            pending.remove(sender.getName());
        }

    }

    class StateA extends BasicInteractiveCommandState {

        public StateA() {
            super("upgrade");
            setArgumentRange(0, 0);
        }

        @Override
        public boolean execute(CommandSender sender, String identifier, String[] args) {
            if (!(sender instanceof Player)) {
                Messaging.send(sender, "Only players are allowed to upgrade towns. Please use the town admin commands!");
                cancelInteraction(sender);
                return false;
            }
            Player player = (Player) sender;
            Citizen citizen = getCitizen(player);
            //Check if the player has a town
            if (!citizen.hasTown()) {
                Messaging.send(sender, "You are not part of a town!");
                cancelInteraction(sender);
                return false;
            }
            //Check if the player is a manager
            if (!citizen.getRank().canManage()) {
                Messaging.send(sender, "Only town managers and owners are allowed to upgrade!");
                cancelInteraction(sender);
                return false;
            }

            Town town = citizen.getTown();
            boolean regionRequired = manager.getConfig().isGroupTypeRegionsEnabled(town.getType());
            if (regionRequired) {
                if (!town.hasRegions() || manager.getRegionManager().getRegion(town) == null) {
                    Messaging.send(sender, "Your town does not have a registered region! It is a requirement for upgrading" +
                            "to have a region!");
                    Messaging.send(sender, "If your town DOES have a WorldGuard region, you can /town reclaim (at the" +
                            " center) and it will be registered, then you can retry /town upgrade.");
                    Messaging.send(sender, "If your town DOES NOT have a region, you must /town claim.");
                    cancelInteraction(sender);
                    return false;
                }
            }

            //Do the material checks for the bank to verify that the town bank has enough/the right materials to claim a spot
            GroupType current = citizen.getTown().getType();
            if (current.nextUpgrade() == null) {
                Messaging.send(sender, "You can't upgrade this town!");
                cancelInteraction(sender);
                return false;
            }
            if (!TownUtil.townHasUpgradeRequirements(citizen.getTown()) ||
                    citizen.getTown().getBankMoney() <
                            manager.getTownConfig().getGroupTypeCost(current.nextUpgrade())) {
                Messaging.send(sender, "Your town doesn't have enough resources to upgrade yet!");
                Messaging.send(sender, "To check what resources your town has use §8/townbank view");
                Messaging.send(sender, "To add more resources to your town use §8/townbank deposit");
                Messaging.send(sender, "Please check §8/town cost §7for costs associated with" +
                        " upgrading the area of your town!");
                cancelInteraction(sender);
                return false;
            }

            pending.add(player.getName());
            Messaging.send(sender, "You are about to upgrade the region for your town!");
            Messaging.send(sender, "Materials from your town bank will be used as the cost for this!");
            Messaging.send(player, "Please §a/town confirm §7or §c/town cancel §7this selection.");
            return true;
        }
    }

    class StateB extends BasicInteractiveCommandState {

        public StateB() {
            super("confirm");
            setArgumentRange(0,0);
        }

        @Override
        public boolean execute(CommandSender sender, String identifier, String[] args) {
            if (!(sender instanceof Player)) {
                return false;
            }
            Player player = (Player) sender;
            pending.remove(player.getName());
            Citizen citizen = getCitizen(player);
            Town town = citizen.getTown();
            if (town == null) {
                Messaging.send(sender, "You're no longer a part of a town!");
                return true;
            }

            TownUtil.removeTownCosts(town,town.getType().nextUpgrade());
            town.withdraw(manager.getTownConfig().getGroupTypeCost(town.getType().nextUpgrade()));
            town.setType(town.getType().nextUpgrade());
            manager.getRegionManager().resizeGroupRegion(town, town.getType());
            plugin.getStorageManager().getStorage().saveCitizenGroup(town, true);
            town.sendAnnouncement(player.getDisplayName() + " has upgraded the town to a " +
                    manager.getTownConfig().getGroupTypeName(town.getType().nextUpgrade()) );
            return true;
        }
    }
}
