package com.herocraftonline.townships.api.config;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.CitizenGroupManager;
import com.herocraftonline.townships.util.BankItem;
import com.herocraftonline.townships.util.Messaging;
import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;

/**
 * Author: gabizou
 *
 * CitizenGroupConfig is the vanilla configuration file for the basic CitizenGroup. A CitizenGroup should have an
 * ItemBank but will not contain any default configurable options. For configurable options, interfaces should be used.
 */
public abstract class CitizenGroupConfig {

    private transient final File groupConfigFile;

    protected transient final Townships plugin;
    protected transient final CitizenGroupManager manager;
    protected transient final FileConfiguration groupSettings;
    protected transient final InputStream defaultSettingsStream;

    public CitizenGroupConfig(Townships plugin, CitizenGroupManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        if (manager.getName().length() < 2) {
            Townships.log(Level.SEVERE, "A Manager is trying to set it's name too short! This will not work!");
            Townships.log(Level.SEVERE, "The offending class in use is: " + manager.getClass().toString());
        }
        String name = manager.getName().substring(0,1).toUpperCase() + manager.getName().substring(1);
        groupConfigFile = new File(plugin.getDataFolder() + File.separator + "configs" + File.separator + name + "Settings.yml");
        File configFolder = new File(plugin.getDataFolder() + File.separator + "configs");
        configFolder.mkdirs();
        defaultSettingsStream = getDefaultSettingsStream();
        groupSettings = new YamlConfiguration();
        // This will attempt to load and save the default configuration file for this implementation of CitizenGroup
        if (!groupConfigFile.exists()) {
            if (defaultSettingsStream == null) {
                Townships.log(Level.SEVERE, "Could not load up the Configuration for " + manager.getName() + " because there is no " +
                        "default settings file included! Contact the developer to fix this issue!");
            } else {
                Townships.log(Level.INFO, "Loading default config for: " + manager.getName() + ". Should how up as: " + name);
                try {
                    groupConfigFile.createNewFile();
                    // Load up default config
                    YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultSettingsStream);
                    defaultConfig.save(groupConfigFile);
                    groupSettings.load(groupConfigFile);
                } catch (IOException | InvalidConfigurationException e) {
                    // Do nothing
                    e.printStackTrace();
                }
            }
        } else {
            try {
                groupSettings.load(groupConfigFile);
            } catch (IOException | InvalidConfigurationException e) {
                //do nothing
                e.printStackTrace();
            }
        }
    }

    public abstract InputStream getDefaultSettingsStream();

    protected Set<BankItem> loadMaterials(ConfigurationSection section) {
        if (section == null) {
            return new HashSet<>();
        }
        Set<BankItem> mats = new LinkedHashSet<>(); // Keep the bank order persisting between saves

        if (section.getList("material-costs") != null) {
            mats = new HashSet<>();

            for(Object obj : section.getList("material-costs")) {
                if (obj instanceof LinkedHashMap) {
                    ConfigurationSection itemConfig = createItemConfig(obj);
                    ItemInfo iteminfo = Items.itemByString(itemConfig.getString("item").toLowerCase());
                    ItemStack item = iteminfo.toStack();
                    if (iteminfo.getSubTypeId() != 0)
                        item.setDurability(iteminfo.getSubTypeId());
                    if (item.getType() != Material.WRITTEN_BOOK) {
                        ItemMeta meta = item.getItemMeta();
                        if (itemConfig.getString("name") != null) {
                            meta.setDisplayName(Messaging.colorize(itemConfig.getString("name")));
                            item.setItemMeta(meta);
                        }
                        if (!itemConfig.getStringList("lore").isEmpty()) {
                            List<String> lore = new ArrayList<>();
                            for (String lorestring : itemConfig.getStringList("lore")) {
                                lore.add(Messaging.colorize(lorestring));
                            }
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                        }
                        item.setAmount(itemConfig.getInt("amount"));
                        BankItem bankItem = new BankItem(item,item.getAmount());
                        mats.add(bankItem);
                    }
                }
            }
        }
        return mats;
    }

    protected List<String> convertStrings(List<String> strings) {
        List<String> convertedMessages = new ArrayList<>();
        for (String string : strings) {
            convertedMessages.add(convertString(string));
        }
        return convertedMessages;
    }

    protected String convertString(String string) {
        return string != null ? string.replaceAll("&&", "\b").replaceAll("&", "§").replaceAll("\b", "&") : null;
    }

    @SuppressWarnings("unchecked")
    protected static ConfigurationSection createItemConfig(Object obj) {
        MemoryConfiguration itemConfig = new MemoryConfiguration();
        itemConfig.addDefaults((Map<String,Object>) obj);
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) obj).entrySet()) {
            itemConfig.createSection(entry.getKey());
            itemConfig.set(entry.getKey(),entry.getValue());
        }
        return itemConfig;
    }

}
