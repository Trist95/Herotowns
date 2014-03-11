package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.command.BasicInteractiveCommandState;
import com.herocraftonline.townships.groups.town.PendingTown;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: gabizou
 */
public class TownSignCommand extends BasicTownInteractiveCommand {

    private final Map<Player, String> pendingSigns = new HashMap<Player, String>();

    public TownSignCommand(TownManager manager) {
        super(manager, "TownSign");
        this.setStates(new StateA(), new StateB());
        setDescription("Signs a charter for a new town!");
        setUsage("/town §9sign <town>");
        setArgumentRange(1, 1);
        setPermission("townships.town.sign");
    }

    @Override
    public String getCancelIdentifier() {
        return "cancel";
    }

    @Override
    public void onCommandCancelled(CommandSender executor) {
        if (!(executor instanceof Player))
            return;
        pendingSigns.remove(executor);
    }
    class StateA extends BasicInteractiveCommandState {

        public StateA() {
            super("sign");
            setArgumentRange(1, 1);
        }

        @Override
        public boolean execute(CommandSender sender, String identifier, String[] args) {
            if (!(sender instanceof Player)) {
                Messaging.send(sender, "Only players may sign charters towns");
                cancelInteraction(sender);
                return false;
            }
            Player player = (Player) sender;
            Citizen citizen = getCitizen((Player) sender);

            if (citizen.getTown() != null) {
                Messaging.send(player, "You can't sign a charter while a citizen of a town!");
                cancelInteraction(sender);
                return false;
            }
            if (citizen.getPendingTown() != null) {
                Messaging.send(player, "You've already signed the charter for: " + citizen.getPendingTown() + "!");
                cancelInteraction(sender);
                return false;
            }
            if (!plugin.getTownManager().isPending(args[0])) {
                Messaging.send(player, "$1 is not a town accepting signatures", args[0]);
                cancelInteraction(sender);
                return false;
            }

            Messaging.send(player,"You are about to sign the town charter for: $1", args[0]);
            Messaging.send(player, "Please §a/town confirm §7or §c/town cancel §7this selection.");
            pendingSigns.put(player, args[0]);
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
            Citizen citi = getCitizen(player);
            String townName = pendingSigns.get(player);
            pendingSigns.remove(player);
            if (!plugin.getTownManager().isPending(townName)) {
                Messaging.send(player, "$1 is no longer pending creation!", townName);
                return true;
            }
            PendingTown pg = (PendingTown) plugin.getTownManager().getPending(townName);
            pg.addMember(player.getName());
            citi.setPendingTown(pg);
            plugin.getTownManager().save();
            Messaging.send(pg.getMembers(), "$1 has signed the charter for $2", plugin, player.getDisplayName(), townName);
            return true;
        }
    }
}
