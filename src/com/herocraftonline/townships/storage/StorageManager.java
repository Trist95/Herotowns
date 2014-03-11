package com.herocraftonline.townships.storage;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.ConfigManager;
import com.herocraftonline.townships.storage.managers.SQLStorage;
import com.herocraftonline.townships.storage.managers.YMLStorage;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Borrowing StorageManager from Heroes seeing as it has potential for loading more than one type
 * of storage solution.
 * Author: gabizou
 */
public class StorageManager extends URLClassLoader {

    private final Map<String, Storage> storageHandlers;
    private final Townships plugin;
    private final Map<String, File> storageFiles;
    private final String configuredStorage;

    public StorageManager(Townships plugin) {
        super(((URLClassLoader)plugin.getClass().getClassLoader()).getURLs(),plugin.getClass().getClassLoader());
        storageHandlers = new HashMap<>();
        storageFiles = new HashMap<>();
        this.plugin = plugin;
        File dir = new File(plugin.getDataFolder(),"storage");
        dir.mkdir();
        configuredStorage = Townships.config.getStorageType();
        // Check whether we're using internal yml or third party plugin
        switch (configuredStorage) {
            case ("yml") :
                storageHandlers.put("yml",new YMLStorage(plugin));
                break;
            case "sql" :
                if (Townships.heroStorage != null)
                    storageHandlers.put("sql",new SQLStorage(plugin));
                else
                    storageHandlers.put("yml", new YMLStorage(plugin));
                break;
            default:
                // Go ahead and load the third party storage jar
                for(final String storageFile : dir.list()) {
                    if(storageFile.contains(".jar")) {
                        final File file = new File(dir, storageFile);
                        final String name = storageFile.toLowerCase().replace(".jar","");
                        if(storageFiles.containsKey(name)) {
                            Townships.log(Level.SEVERE, "Duplicate Storage jar found! Please remove "+ storageFile +" or "+storageFiles.get(name).getName());
                            continue;
                        }
                        Townships.debugLog(Level.INFO, "Loading storage: " + storageFile);
                        storageFiles.put(name, file);
                        try {
                            this.addURL(file.toURI().toURL());
                        } catch (final MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                initializeStorage(Townships.config.getStorageType());
                break;
        }
    }

    private void initializeStorage(String storageType) {
        if(isListed(storageType.toLowerCase())) {
            loadStorage(storageType);
        } else {
            Townships.log(Level.SEVERE, "There is no storage jar called " + Townships.config.getStorageType() + "! Disabling Heroes!");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public void addStorage(Storage storage) {
        storageHandlers.put(storage.getName().toLowerCase(), storage);
    }

    public Storage getStorage() {
        return storageHandlers.get(configuredStorage.toLowerCase());
    }

    private boolean isListed(String name) {
        return storageFiles.containsKey(name.toLowerCase());
    }

    private boolean loadStorage(String name) {
        final Storage storage = loadStorage(storageFiles.get(name.toLowerCase()));
        if(storage == null) {
            return false;
        }

        addStorage(storage);
        return true;
    }

    private Storage loadStorage(File file) {
        try {
            final JarFile jarFile = new JarFile(file);
            final Enumeration<JarEntry> entries = jarFile.entries();

            String mainClass = null;
            while(entries.hasMoreElements()) {
                final JarEntry element = entries.nextElement();
                if(element.getName().equalsIgnoreCase("storage.info")) {
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(element)));
                    mainClass = reader.readLine().substring(12);
                    break;
                }
            }

            if (mainClass != null) {
                final Class<?> clazz = Class.forName(mainClass, true, this);
                final Class<? extends Storage> storageClass = clazz.asSubclass(Storage.class);
                final Constructor<? extends Storage> ctor = storageClass.getConstructor(plugin.getClass());
                return ctor.newInstance(plugin);
            } else {
                jarFile.close();
                throw new IllegalArgumentException();
            }

        } catch (final NoClassDefFoundError | ClassNotFoundException e) {
            Townships.log(Level.WARNING, "Unable to load " + file.getName() + "! Make sure Storage was written for Townships!");
            if(ConfigManager.debug)
                Townships.debugThrow(this.getClass().toString(), "loadSkill", e);
            return null;
        } catch (final IllegalArgumentException e) {
            Townships.log(Level.SEVERE, "Could not detect the proper Storage class to load for: " + file.getName());
            return null;
        } catch (final Exception e) {
            Townships.log(Level.INFO, "The storage " + file.getName() + " failed to load for an unknown reason.");
            if(ConfigManager.debug)
                Townships.debugLog.getLogger().throwing(this.getClass().getName(), "loadStorage", e);
            return null;
        }
    }

}
