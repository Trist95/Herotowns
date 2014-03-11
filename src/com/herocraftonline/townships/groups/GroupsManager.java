package com.herocraftonline.townships.groups;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.CitizenGroupManager;
import com.herocraftonline.townships.api.ConfigManager;
import com.herocraftonline.townships.groups.guild.GuildManager;
import com.herocraftonline.townships.groups.kingdom.KingdomManager;
import com.herocraftonline.townships.groups.town.TownManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Author: gabizou
 */
public class GroupsManager extends URLClassLoader {

    private final Townships plugin;

    private final Map<String, File> groupJarFiles;

    private final Map<String, CitizenGroupManager> citizenGroupManagers;
    private final List<String> enabledGroupTypes;

    public GroupsManager(Townships plugin) {
        super(((URLClassLoader)plugin.getClass().getClassLoader()).getURLs(),plugin.getClass().getClassLoader());

        citizenGroupManagers = new HashMap<>();
        groupJarFiles = new HashMap<>();
        this.plugin = plugin;
        File dir = new File(plugin.getDataFolder(),"grouptypes");
        enabledGroupTypes = Townships.config.getEnabledGroups();

        // Check whether we're using internal yml or third party plugin
        for (String enabledGroup : enabledGroupTypes) {
            switch (enabledGroup.toLowerCase()) {
                case ("town") :
                    addCitizenGroupManager(new TownManager(plugin));
                    break;
                case "guild" :
                    addCitizenGroupManager(new GuildManager(plugin));
                    break;
                case "kingdom" :
                    addCitizenGroupManager(new KingdomManager(plugin));
                    break;
                default:
                    if (dir.exists()) {
                        Townships.log(Level.WARNING, "The groupTypes folder isn't generated. Therefor, we can't" +
                                "load any custom Groups!");
                        Townships.log(Level.WARNING, "Skipping: " + enabledGroup);
                        break;
                    }
                    // Go ahead and load the third party storage jar
                    for(final String groupFile : dir.list()) {
                        if(groupFile.contains(".jar")) {
                            final File file = new File(dir, groupFile);
                            final String name = groupFile.toLowerCase().replace(".jar","");
                            if (groupJarFiles.containsKey(name)) {
                                Townships.log(Level.SEVERE, "Duplicate GroupType jar found! Please remove " + groupFile +
                                        " or " + groupJarFiles.get(name).getName());
                                continue;
                            }
                            Townships.debugLog(Level.INFO, "Loading group type: " + groupFile);
                            groupJarFiles.put(name, file);
                            try {
                                this.addURL(file.toURI().toURL());
                            } catch (final MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    CitizenGroupManager manager = initializeCitizenGroup(enabledGroup);
                    addCitizenGroupManager(manager);
                    break;
            }
        }
    }

    public void loadManagers() {
        Iterator<Map.Entry<String, CitizenGroupManager>> it = citizenGroupManagers.entrySet().iterator();
        while (it.hasNext()) {
            CitizenGroupManager manager = it.next().getValue();
            manager.load();
        }
    }

    private CitizenGroupManager initializeCitizenGroup(String groupType) {
        if(isListed(groupType.toLowerCase())) {
            loadCitizenGroup(groupType);
        } else {
            Townships.log(Level.SEVERE, "There is no CitizenGroup jar called " + Townships.config.getStorageType() + "!");
        }
        return null;
    }

    public void shutdown() {
        Iterator<Map.Entry<String, CitizenGroupManager>> it = citizenGroupManagers.entrySet().iterator();
        while (it.hasNext()) {
            CitizenGroupManager manager = it.next().getValue();
            it.remove();
            Townships.debugLog(Level.INFO, "Shutting down: " + manager.getName() + " manager.");
            manager.save();
            manager.shutdown();
        }
        try {
            this.close();
        } catch (IOException e) {
            Townships.log(Level.SEVERE, "Could not close the GroupManager Class Loader!!");
            e.printStackTrace();
        }
        citizenGroupManagers.clear();
    }

    public boolean addCitizenGroupManager(CitizenGroupManager manager) {
        if (manager == null) {
            Townships.log(Level.WARNING, "An attempt to add a CitizenGroupManager failed because the manager wasn't initialized!");
            return false;
        }
        if (citizenGroupManagers.containsKey(manager.getName().toLowerCase())) {
            Townships.log(Level.WARNING, "A plugin is attempting to replace an already enabled CitizenGroup Manager!");
            Townships.log(Level.WARNING, "The duplicate manager's supposed name is: " + manager.getName());
            Townships.log(Level.WARNING, "The the class declaration is: " + manager.getClass().toString());
            Townships.log(Level.WARNING, "To avoid potentitally catastrophic failure of Townships, we are ignoring this addition.");
            return false;
        }
        citizenGroupManagers.put(manager.getName().toLowerCase(), manager);
        return true;
    }

    public CitizenGroupManager getCitizenGroupManager(String name) {
        return citizenGroupManagers.get(name.toLowerCase());
    }

    public void removeCitizenGroupManager(CitizenGroupManager manager) {
        if (manager == null) {
            Townships.log(Level.WARNING, "An attempt to remove a CitizenGroupManager failed because the manager wasn't initialized!");
            return;
        }
        if (citizenGroupManagers.containsKey(manager.getName().toLowerCase())) {
            manager.save();
            citizenGroupManagers.remove(manager.getName());
        } else {
            Townships.log(Level.INFO, "The requested CitizenGroupManager: " + manager.getName() + " was not loaded or enabled!");
        }
    }

    public Map<String, CitizenGroupManager> getCitizenGroupManagers() {
        return Collections.unmodifiableMap(citizenGroupManagers);
    }

    private boolean isListed(String name) {
        return groupJarFiles.containsKey(name.toLowerCase());
    }

    private boolean loadCitizenGroup(String name) {
        final CitizenGroupManager groupManager = loadCitizenGroupManager(groupJarFiles.get(name.toLowerCase()));
        if(groupManager == null) {
            return false;
        }

        addCitizenGroupManager(groupManager);
        return true;
    }

    private CitizenGroupManager loadCitizenGroupManager(File file) {
        try {
            final JarFile jarFile = new JarFile(file);
            final Enumeration<JarEntry> entries = jarFile.entries();

            String mainClass = null;
            while(entries.hasMoreElements()) {
                final JarEntry element = entries.nextElement();
                if(element.getName().equalsIgnoreCase("manager.info")) {
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(element)));
                    mainClass = reader.readLine().substring(12);
                    break;
                }
            }

            if (mainClass != null) {
                final Class<?> clazz = Class.forName(mainClass, true, this);
                final Class<? extends CitizenGroupManager> storageClass = clazz.asSubclass(CitizenGroupManager.class);
                final Constructor<? extends CitizenGroupManager> ctor = storageClass.getConstructor(plugin.getClass());
                return ctor.newInstance(plugin);
            } else {
                jarFile.close();
                throw new IllegalArgumentException();
            }

        } catch (final NoClassDefFoundError | ClassNotFoundException e) {
            Townships.log(Level.WARNING, "Unable to load " + file.getName() + "! Make sure the CitizenGroupManager was written for Townships!");
            if(ConfigManager.debug)
                Townships.debugThrow(this.getClass().toString(), "loadCitizenGroupManager", e);
            return null;
        } catch (final IllegalArgumentException e) {
            Townships.log(Level.SEVERE, "Could not detect the proper CitizenGroupManager class to load for: " + file.getName());
            return null;
        } catch (final Exception e) {
            Townships.log(Level.INFO, "The CitizenGroupManager " + file.getName() + " failed to load for an unknown reason.");
            if(ConfigManager.debug)
                Townships.debugLog.getLogger().throwing(this.getClass().getName(), "loadCitizenGroupManager", e);
            return null;
        }
    }
}