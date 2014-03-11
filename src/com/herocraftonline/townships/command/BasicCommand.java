package com.herocraftonline.townships.command;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.CitizenGroupManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class BasicCommand implements Command {

    private final String name;
    private String description = "";
    private String usage = "";
    private String permission = "";
    private String[] notes = new String[0];
    private String[] identifiers = new String[0];
    private int minArguments = 0;
    private int maxArguments = 0;
    protected Townships plugin;
    protected CitizenGroupManager manager;

    protected BasicCommand(CitizenGroupManager manager, String name) {
        this.name = name;
        this.manager = manager;
        plugin = Townships.getInstance();
    }

    protected Citizen getCitizen(Player player) {
        return plugin.getCitizenManager().getCitizen(player);
    }

    protected Citizen getCitizen(String name) {
        return plugin.getCitizenManager().getCitizen(name);
    }

    @Override
    public void cancelInteraction(CommandSender executor) {}

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String[] getIdentifiers() {
        return identifiers;
    }

    @Override
    public int getMaxArguments() {
        return maxArguments;
    }

    @Override
    public int getMinArguments() {
        return minArguments;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getNotes() {
        return notes;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public String getUsage() {
        return usage;
    }

    @Override
    public boolean isIdentifier(CommandSender executor, String input) {
        for (final String identifier : identifiers) {
            if (input.equalsIgnoreCase(identifier)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isInProgress(CommandSender executor) {
        return false;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

    @Override
    public boolean isShownOnHelpMenu() {
        return true;
    }

    protected String getMessage(String key) {
        return Messaging.getMessage(key);
    }

    public void setArgumentRange(int min, int max) {
        this.minArguments = min;
        this.maxArguments = max;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIdentifiers(String... identifiers) {
        this.identifiers = identifiers;
    }

    public void setNotes(String... notes) {
        this.notes = notes;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public void displayHelp(CommandSender executor) {
        executor.sendMessage("§cCommand:§e " + getName());
        executor.sendMessage("§cDescription:§e " + getDescription());
        executor.sendMessage("§cUsage:§e " + getUsage());
        if (getNotes() != null) {
            for (String note : getNotes()) {
                executor.sendMessage("§e" + note);
            }
        }
    }

}
