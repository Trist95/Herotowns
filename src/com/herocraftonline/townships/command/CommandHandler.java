/**
 * Copyright (C) 2011 DThielke <dave.thielke@gmail.com>
 *
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/

package com.herocraftonline.townships.command;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.CitizenGroupManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

public class CommandHandler {

    protected LinkedHashMap<String, Command> commands;
    private Map<String, Command> identifiers = new HashMap<>();
    private transient Map<String, Map<String, Command>> commandGroups;
    private Townships plugin;

    public CommandHandler(Townships townships) {
        this.plugin = townships;
        commandGroups = new LinkedHashMap<>();
        commands = new LinkedHashMap<>();
    }

    public void addCommandGroup(CitizenGroupManager groupManager) {
        String name = groupManager.getName();
        Map<String, Command> groupCommands = groupManager.getCommands();
        if (groupCommands == null)
            return;
        commandGroups.put(name.toLowerCase(),groupCommands);
        commands.putAll(groupCommands);
    }

    protected void addCommand(Command command) {
        commands.put(command.getName().toLowerCase(), command);
        for (String ident : command.getIdentifiers()) {
            identifiers.put(ident.toLowerCase(), command);
        }
    }

    protected void removeCommand(Command command) {
        commands.remove(command);
        for (String ident : command.getIdentifiers()) {
            identifiers.remove(ident.toLowerCase());
        }
    }

    public Command getCommand(CitizenGroupManager manager, String name) {
        return commandGroups.get(manager.getName().toLowerCase()).get(name);
    }

    public List<Command> getCommands() {
        return new ArrayList<Command>(commands.values());
    }

    public Map<String, Command> getCommandGroup(CitizenGroupManager groupManager) {
        return commandGroups.get(groupManager.getName().toLowerCase());
    }

    public Map<String, Command> getCommandGroup(String groupName) {
        return commandGroups.containsKey(groupName.toLowerCase()) ? commandGroups.get(groupName.toLowerCase()) : null;
    }

    public boolean dispatch(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        String[] arguments;
        if (args.length < 1) {
            arguments = new String[]{ command.getName() };
        } else {
            arguments = args;
        }
        String name = command.getName().toLowerCase();
        if (name.equalsIgnoreCase("tn")) {
            name = "town";
        }

        // Temporary method to re-assign the lengthier commands to shorter commands
        if (name.equalsIgnoreCase("ta")) {
            name = "town";
            String[] newargs = new String[arguments.length + 1];
            newargs[0] = "admin";
            for (int i = 0; i < args.length; i++) {
                newargs[i + 1] = arguments[i];
            }
            arguments = newargs;
        }
        if (name.equalsIgnoreCase("tb")) {
            name = "town";
            String[] newargs = new String[arguments.length + 1];
            newargs[0] = "bank";
            for (int i = 0; i < arguments.length; i++) {
                newargs[i + 1] = arguments[i];
            }
            arguments = newargs;
        }

        if (name.equalsIgnoreCase("tr")) {
            name = "town";
            String[] newargs = new String[arguments.length + 1];
            newargs[0] = "region";
            for (int i = 0; i < arguments.length; i++) {
                newargs[i + 1] = arguments[i];
            }
            arguments = newargs;
        }

        Map<String, Command> groupCommands = getCommandGroup(name);

        if (groupCommands == null) {
            // TODO Handle when no command group is found.
            Townships.log(Level.INFO,"The command's name was: " + command.getName());
            return true;
        }

        // TODO Double check why commands are going to help
        for (int argsIncluded = arguments.length; argsIncluded >= 0; argsIncluded--) {
            String identifier = "";
            for (int i = 0; i < argsIncluded; i++) {
                identifier += " " + arguments[i];
            }

            identifier = identifier.trim();

            for (Command cmd : groupCommands.values()) {

                if (cmd.isIdentifier(sender, identifier)) {
                    String[] remove = new String[]{};
                    for (String identifiers : cmd.getIdentifiers()) {
                        if (identifier.equalsIgnoreCase(identifiers))
                             remove = identifier.split(" ");
                    }
                    if (remove.length == 0) {
                        remove = new String[] {""};
                    }
                    String[] realArgs = Arrays.copyOfRange(arguments, remove.length, arguments.length);

                    if (!cmd.isInProgress(sender)) {
                        if (realArgs.length < cmd.getMinArguments() || realArgs.length > cmd.getMaxArguments()) {
                            displayCommandHelp(cmd, sender);
                            return true;
                        } else if (realArgs.length > 0 && realArgs[0].equals("?")) {
                            displayCommandHelp(cmd, sender);
                            return true;
                        }
                    }

                    if (!hasPermission(sender, cmd.getPermission())) {
                        Messaging.send(sender, "Insufficient permission.");
                        return true;
                    }

                    Townships.debugLog(Level.INFO, "Command "+ cmd.getName() +" Identifier: " +
                            Arrays.toString(cmd.getIdentifiers()) + " using Identifier: "+ identifier +
                            " with Args: " + Arrays.toString(realArgs));
                    cmd.execute(sender, identifier, realArgs);
                    return true;
                }
            }
        }
        Townships.debugLog(Level.INFO, "No Command Identifier with Args: " + Arrays.toString(arguments));
//        showCommands(sender, groupCommands);
        return true;
    }

    private void displayCommandHelp(Command cmd, CommandSender sender) {
        sender.sendMessage("§cCommand:§e " + cmd.getName());
        sender.sendMessage("§cDescription:§e " + cmd.getDescription());
        sender.sendMessage("§cUsage:§e " + cmd.getUsage());
        if (cmd.getNotes() != null) {
            for (String note : cmd.getNotes()) {
                sender.sendMessage("§e" + note);
            }
        }
    }

    public static boolean hasPermission(CommandSender sender, String permission) {
        if (!(sender instanceof Player) || permission == null || permission.isEmpty()) {
            return true;
        }
        return Townships.perms.has((Player) sender, permission);
    }

    private void showCommands(CommandSender sender, Map<String, Command> groupCommands) {
        final int CMDS_PER_PAGE = 8;
        int page = 0;
        Collection<Command> sortCommands = new LinkedHashSet<>();
        for (Map.Entry<String, Command> commandEntry : groupCommands.entrySet()) {
            sortCommands.add(commandEntry.getValue());
        }
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
        Messaging.send(sender, Messaging.getMessage("town_help_list"),page+1,numPages);
        int start = page * CMDS_PER_PAGE;
        int end = start + CMDS_PER_PAGE;
        if (end > commands.size()) {
            end = commands.size();
        }
        for (int c = start; c < end; c++) {
            Command cmd = commands.get(c);
            sender.sendMessage("  §a" + cmd.getUsage());
        }

        sender.sendMessage(Messaging.getMessage("town_help_more"));
    }
}
