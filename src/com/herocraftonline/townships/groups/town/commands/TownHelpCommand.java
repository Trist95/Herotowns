package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.command.Command;
import com.herocraftonline.townships.command.CommandHandler;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Author: gabizou
 */
public class TownHelpCommand extends BasicTownCommand {

    private static final int CMDS_PER_PAGE = 8;

    public TownHelpCommand(TownManager manager) {
        super(manager, "TownHelp");
        setDescription("Displays the help menu");
        setUsage("/town help §8[page#]");
        setArgumentRange(0, 1);
        setIdentifiers("help", "town", "");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        int page = 0;
        if (args.length != 0) {
            try {
                page = Integer.parseInt(args[0]) - 1;
            } catch (NumberFormatException e) {
                Messaging.send(sender,getUsage());
            }
        }

        Collection<Command> sortCommands = plugin.getCommandHandler().getCommandGroup("town").values();
        List<Command> commands = new ArrayList<>();

        // Filter out Commands the user doesn't have permission for from the command list.
        Iterator<Command> iterator = sortCommands.iterator();

        while (iterator.hasNext()) {
            Command cmd = iterator.next();
            if (CommandHandler.hasPermission(sender, cmd.getPermission())) {
                commands.add(cmd);
            }
        }

        int numPages = commands.size() / CMDS_PER_PAGE;
        if (commands.size() % CMDS_PER_PAGE != 0) {
            numPages++;
        }

        if (page >= numPages || page < 0) {
            page = 0;
        }
        Messaging.send(sender, getMessage("town_help_list"),page+1,numPages);
        int start = page * CMDS_PER_PAGE;
        int end = start + CMDS_PER_PAGE;
        if (end > commands.size()) {
            end = commands.size();
        }
        for (int c = start; c < end; c++) {
            Command cmd = commands.get(c);
            sender.sendMessage("  §a" + cmd.getUsage());
        }

        sender.sendMessage(getMessage("town_help_more"));
        return true;
    }

}
