package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: gabizou
 */
public class TownTaxCommand extends BasicTownCommand {

    public TownTaxCommand(TownManager manager) {
        super(manager, "TownTax");
        setDescription("This command displays various tax information about your current town.");
        setUsage("/town tax");
        setIdentifiers("tax");
        setArgumentRange(0,1);
        setPermission("townships.town.tax");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        if (!(executor instanceof Player)) {
            Messaging.send(executor, "Can not handle town tax commands through console at this time!");
            return true;
        }

        Player player = (Player) executor;
        Citizen citizen = getCitizen(player);
        if (citizen.getTown() == null) {
            Messaging.send(player, "You aren't in a town to check taxes!");
            return true;
        }

        Town town = citizen.getTown();
        if (!town.hasMember(citizen)) {
            Messaging.send(player, "You aren't part of a town right now!");
            citizen.setTown(null);
            return true;
        }

        int tax = manager.getTownConfig().getGroupTypeTax(town.getType());
        long lastTax = town.getLastTax();
        int missed = town.getMissedPayments();
        long taxwarning = town.getLastTaxWarning();
        long nextTax = lastTax + manager.getTownConfig().taxInterval;
        Date nextTaxDate = new Date(nextTax);
        Date lastTaxDate = new Date(lastTax);
        Date warningDate = new Date(taxwarning);
        String lastTaxFormat = new SimpleDateFormat("EEE, d MMM yyyy 'around' HH:mm").format(lastTaxDate);
        String nextTaxFormat = new SimpleDateFormat("EEE, d MMM yyyy 'around' HH:mm").format(nextTaxDate);
        String warningFormat = new SimpleDateFormat("EEE, d MMM yyyy 'around' HH:mm").format(warningDate);

        Messaging.send(player, "Your town's tax information:");
        Messaging.send(player, town.getName() + " has a weekly tax of: " + tax);
        Messaging.send(player, "and has last processed taxes: " + lastTaxFormat);
        Messaging.send(player, "The next attempt at taxes will be on " + nextTaxFormat + ".");
        Messaging.send(player, "Your town has " + missed + " missed taxes!");
        if (taxwarning != 0) {
            Messaging.send(player, "Your town is at risk for tax evasion! Your warning was posted on: " + warningFormat);
            Messaging.send(player, "Please make sure that your town bank has enough money to pay ALL taxes on the next tax date!");
        }
        return false;
    }
}
