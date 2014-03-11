package com.herocraftonline.townships.api;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.config.CitizenGroupConfig;
import com.herocraftonline.townships.command.Command;
import com.herocraftonline.townships.storage.Storage;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

/**
 * Author: gabizou
 */
public abstract class CitizenGroupManager {
    /**
     * Map of town names to their respective towns
     */
    protected Map<String, CitizenGroup> groups;
    protected Map<String, PendingGroup> pendingGroups;
    public double lastInterval = 0;

    protected transient final Townships plugin;
    protected transient final Storage storage;
    protected transient final File managerFile;
    protected transient final CitizenGroupConfig groupConfig;

    private static transient CitizenGroupManager instance;

    protected transient Map<String, Command> commands = new LinkedHashMap<>();


    public CitizenGroupManager(Townships plugin, File managerFile) {
        this.plugin = plugin;
        groups = new HashMap<>();
        pendingGroups = new HashMap<>();
        storage = plugin.getStorageManager().getStorage();
        this.managerFile = managerFile;
        groupConfig = loadGroupConfig();
        instance = this;
    }

    public static CitizenGroupManager getInstance() {
        return instance;
    }

    protected abstract CitizenGroupConfig loadGroupConfig();

    public CitizenGroupConfig getConfig() {
        return groupConfig;
    }

    public abstract Map<String, Command> getCommands();

    protected void addCommand(Command command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    public abstract Class<? extends PendingGroup> getPendingGroupClass();

    public abstract Class<? extends CitizenGroup> getCitizenGroupClass();

    public abstract void save();

    public abstract void load();

    public abstract void shutdown();

    public abstract void delete(CitizenGroup group);

    protected abstract void removeCitizenFromPendingGroup(String name, Citizen c, boolean message);

    public abstract void addCitizenToPendingGroup(String name, String citizen);

    public abstract String getName();

    /**
     * Returns the group with the given name, or null if it could not be found
     * @param name
     * @return
     */
    public CitizenGroup get(String name) {
        if (name == null)
            return null;
        return groups.get(name.toLowerCase());
    }

    /**
     * Adds the group to the mapping
     *
     * @param group
     * @return
     */
    public void add(CitizenGroup group) {
        String key = group.getName().toLowerCase();
        if (groups.containsKey(key))
            return;

        groups.put(group.getName().toLowerCase(), group);
        save();
    }

    /**
     * Checks if the group exists in this manager - either pending, or already made
     * @param name
     */
    public boolean exists(String name) {
        name = name.toLowerCase();
        return pendingGroups.containsKey(name) || groups.containsKey(name);
    }

    /**
     * Return a collection view of all groups loaded
     * @return
     */
    public Collection<CitizenGroup> getGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    /**
     * Returns all Pending groups and their associated citizens
     * @return
     */
    public Map<String, PendingGroup> getPending() {
        return pendingGroups;
    }

    public PendingGroup getPendingGroupCitizens(String groupName) {
        return pendingGroups.get(groupName.toLowerCase());
    }

    public PendingGroup removePending(String name, boolean message) {
        PendingGroup names = pendingGroups.remove(name.toLowerCase());
        if (names == null) {
            return null;
        }
        //Reset values for online players
        for (String n : names.getMembers()) {
            Player p = plugin.getServer().getPlayer(n);
            if (p == null)
                continue;
            Citizen c = plugin.getCitizenManager().getCitizen(p);
            removeCitizenFromPendingGroup(name, c, message);
        }
        save();
        return names;
    }

    public void addPending(PendingGroup pending) {
        if (pendingGroups.containsKey(pending.name.toLowerCase()))
            return;
        pendingGroups.put(pending.name.toLowerCase(), pending);
        save();
    }

    public boolean isPending(String group) {
        return pendingGroups.containsKey(group.toLowerCase());
    }

    /**
     * Searches for a pending group that this player is a part of
     * @param playerName
     * @return
     */
    public String getPendingFromPlayer(String playerName) {
        for (String townName : pendingGroups.keySet()) {
            if (pendingGroups.get(townName).getMembers().contains(playerName))
                return pendingGroups.get(townName).name;
        }
        return null;
    }

    public PendingGroup getPending(String name) {
        return pendingGroups.get(name.toLowerCase());
    }

    public File getManagerFile() {
        return managerFile;
    }
}
