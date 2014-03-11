package com.herocraftonline.townships.storage;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.*;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class Storage {

    protected String name = null;
    protected Townships plugin;
    final List<String> citizensNotToSave = new ArrayList<>();

    protected Storage(Townships plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    /**
     * Returns a name for the storage
     */
    public String getName() {
        return name;
    }

    /**
     * Saves a generic CitizenGroup. This should type check for Upgradable and Taxable interfaces including
     * Regionable type check to avoid data loss.
     * @param group - Generic CitizenGroup to save
     * @param now - Whether or not to save this CitizenGroup now or later
     * @return - Completion
     */
    public abstract boolean saveCitizenGroup(CitizenGroup group, boolean now);


    /**
     * Loads a generalized CitizenGroup. Checks to make sure this CitizenGroup type uses any of the various
     * Interfaces provided by the API
     */
    public abstract CitizenGroup loadCitizenGroup(CitizenGroupManager manager, String name);

    public abstract Map<String, CitizenGroup> loadCitizenGroups(CitizenGroupManager manager);

    public abstract boolean saveCitizenGroups(CitizenGroupManager manager);

    public abstract void deleteCitizenGroup(CitizenGroupManager manager, CitizenGroup group);

    /**
     * Saves all citizens currently loaded
     *
     * @return
     */
    public abstract void saveCitizens();

    /**
     * Saves a single citizen to file
     *
     * @param citizen
     * @return
     */
    public abstract boolean saveCitizen(Citizen citizen, boolean now);

    /**
     * loads a single citizen from file
     *
     * @param player
     * @return
     */
    public abstract Citizen loadCitizen(Player player);


    /**
     * Loads a Citizen that is offline for data manipulation. This should only be called by the
     * CitizenManager in the event that a Player is not online. This will also attempt to resolve to
     * any name citizen regardless of case. Citizen names SHOULD be case insensitive when searching is involved.
     */
    public abstract Citizen loadOfflineCitizen(String name);

    /**
     * Loads a generic CitizenGroupManager's data. Varies between CitizenGroupManagers.
     * @return
     */
    public abstract void loadManagerData(CitizenGroupManager manager);

    public abstract void loadRegionManagerData(GroupRegionManager manager);

    /**
     * Saves a generic CitizenGroupManager's data. Varies between CitizenGroupManager.
     * @return
     */
    public abstract void saveManagerData(CitizenGroupManager manager);

    public abstract void saveRegionManagerData(GroupRegionManager manager);

    public List<String> getCitizensNotToSave() {
        return citizensNotToSave;
    }

    /**
     * Fetches the Constructor for the appropriate PendingGroup belonging to the CitizenGroupManager
     *
     * @param manager
     * @return
     */
    protected final Constructor<? extends PendingGroup> getPendingConstructor(CitizenGroupManager manager) {
        String className = manager.getPendingGroupClass().toString();
        // Have to resort to some reflection to find the proper PendingGroup type to instantiate
        try {
            final Class<? extends PendingGroup> pendingClass = manager.getPendingGroupClass();
            final Constructor<? extends PendingGroup> ctor = pendingClass.getConstructor(String.class, Collection.class);
            return ctor;
        } catch (final NoClassDefFoundError e) {
            Townships.log(Level.WARNING, "Unable to load " + className + " PendingGroup was written for a previous Townships version, please check the debug.log for more information!");
            if(ConfigManager.debug)
                Townships.debugThrow(this.getClass().toString(), "loadSkill", e);
        } catch (final IllegalArgumentException e) {
            Townships.log(Level.SEVERE, "Could not detect the proper PendingGroup class to load for: " + className);
        } catch (final Exception e) {
            Townships.log(Level.INFO, "The PendingGroup " + className + " failed to load for an unknown reason.");
            if(ConfigManager.debug)
                Townships.debugLog.getLogger().throwing(this.getClass().getName(), "loadPendingGroup", e);
        }
        return null;
    }

    /**
     * Returns a new instance of the PendingGroup with a populated list of Citizens belonging to said PendingGroup.
     * This is appropriate to use for ALL storages provided there is a Collection included. Should be used STRICTLY for
     * loading from Storage.
     *
     * @param manager
     * @param name
     * @param collection
     * @return
     */
    protected final PendingGroup getNewPendingGroup(CitizenGroupManager manager, String name, Collection<? extends String> collection) {
        try {
            return getPendingConstructor(manager).newInstance(name, collection);
        } catch (Exception e) {
            Townships.log(Level.SEVERE, "Could not detect the proper PendingGroup class to load for: " + manager.getPendingGroupClass());
        }
        return null;
    }

    protected final Constructor<? extends CitizenGroup> getCitizenGroupConstructor(CitizenGroupManager manager) {
        String className = manager.getCitizenGroupClass().toString();
        // Have to resort to some reflection to find the proper PendingGroup type to instantiate
        try {
            final Class<? extends CitizenGroup> groupClass = manager.getCitizenGroupClass();
            final Constructor<? extends CitizenGroup> ctor = groupClass.getConstructor(String.class, Integer.class);
            return ctor;
        } catch (final NoClassDefFoundError e) {
            Townships.log(Level.WARNING, "Unable to load " + className + " CitizenGroup was written for a previous Townships version, please check the debug.log for more information!");
            if(ConfigManager.debug)
                Townships.debugThrow(this.getClass().toString(), "loadCitizenGroup", e);
        } catch (final IllegalArgumentException e) {
            Townships.log(Level.SEVERE, "Could not detect the proper CitizenGroup class to load for: " + className);
        } catch (final Exception e) {
            Townships.log(Level.INFO, "The class " + className + " failed to load for an unknown reason.");
            if(ConfigManager.debug)
                Townships.debugLog.getLogger().throwing(this.getClass().getName(), "loadCitizenGroup", e);
        }
        return null;
    }

    protected final CitizenGroup getNewCitizenGroup(CitizenGroupManager manager, String name) {
        try {
            return getCitizenGroupConstructor(manager).newInstance(name, 0);
        } catch (Exception e) {
            Townships.log(Level.SEVERE, "Could not detect the proper CitizenGroup class to load for: " + manager.getCitizenGroupClass());
        }
        return null;
    }

}
