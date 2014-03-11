package com.herocraftonline.townships.storage.managers;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.*;
import com.herocraftonline.townships.storage.Storage;
import com.herocraftonline.townships.util.BankItem;
import com.herocraftonline.townships.util.Messaging;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Author: gabizou
 */
public class YMLStorage extends Storage {

    private File citizensDir;
    private File groupsDir;
    private File regionsDir;

    public YMLStorage(Townships plugin) {
        super(plugin,"yml");
        citizensDir = new File(plugin.getDataFolder() + File.separator + "citizens");
        citizensDir.mkdirs();
        groupsDir = new File(plugin.getDataFolder() + File.separator + "groups");
        groupsDir.mkdirs();
        regionsDir = new File(plugin.getDataFolder() + File.separator + "regions");
        this.plugin = plugin;
    }

    private void loadCitizens(CitizenGroup group, ConfigurationSection section) {
        if (section == null)
            return;
        for (String cName : section.getKeys(false)) {
            Rank rank;
            long login = 0;
            try {
                rank = Rank.valueOf(section.getString(cName + ".rank", "CITIZEN"));
                login = section.getLong(cName + ".last-login");
            } catch (IllegalArgumentException e) {
                rank = Rank.NONE;
            }
            group.addMember(cName, rank);
            group.updateLastLogin(cName, login);
        }

    }

    private void loadNonCitizens(CitizenGroup group, ConfigurationSection section) {
        if (section == null)
            return;
        for (String cName : section.getKeys(false)) {
            Rank rank;
            try {
                rank = Rank.valueOf(section.getString(cName + ".rank", "GUEST"));
            } catch (IllegalArgumentException e) {
                rank = Rank.GUEST;
            }
            group.setNewCitizenRank(cName, rank);
        }
    }

    private void loadRankNames(CitizenGroup group, ConfigurationSection section) {
        if (section == null)
            return;
        for (String rName : section.getKeys(false)) {
            Rank rank;
            try {
                rank = Rank.valueOf(rName);
            } catch (IllegalArgumentException e) {
                continue;
            }
            group.setRankName(rank, section.getString(rName, rank.name()));
        }

    }

    private void loadRelations(CitizenGroup group, ConfigurationSection section) {
        if (section == null)
            return;
        for (String otherGroup : section.getKeys(false)) {
            Relation rel;
            try {
                rel = Relation.valueOf(section.getString(otherGroup));
            } catch (IllegalArgumentException e) {
                rel = Relation.NEUTRAL;
            }
            group.addGroupRelation(otherGroup, rel);
        }
    }

    private void loadBank(CitizenGroup group, List<?> section) {
        if (section == null) {
            return;
        }

        for(Object obj : section) {
            if (obj instanceof LinkedHashMap) {
                ConfigurationSection itemConfig = createItemConfig(obj);
                ItemInfo itemInfo;
                // Allow for loading between different types of
                String itemString = itemConfig.getString("item");
                if (itemString.startsWith("\\'\\d"))
                    itemInfo = Items.itemByString(itemString.replace("'",""));
                else
                    itemInfo = Items.itemByString(itemString);
                if (itemInfo == null) {
                    Townships.log(Level.WARNING, "Could not load the town, " + group.getName() + ", due to the item: " + itemConfig.getString("item"));
                    continue;
                }
                ItemStack item = itemInfo.toStack();
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
                    String name;
                    if (meta != null && meta.hasDisplayName()) {
                        name = meta.getDisplayName();
                    } else {
                        name = Items.itemByStack(bankItem.getItem()).getName();
                    }
                    group.getBankContents().put(name, bankItem);
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private static ConfigurationSection createItemConfig(Object obj) {
        MemoryConfiguration itemConfig = new MemoryConfiguration();
        itemConfig.addDefaults((Map<String,Object>) obj);
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) obj).entrySet()) {
            itemConfig.createSection(entry.getKey());
            itemConfig.set(entry.getKey(),entry.getValue());
        }
        return itemConfig;
    }

    @Override
    public Map<String, CitizenGroup> loadCitizenGroups(CitizenGroupManager manager) {
        Map<String, CitizenGroup> groups = new HashMap<>();
        File groupDir = new File(groupsDir + File.separator + manager.getName());
        if (!groupDir.exists()) {
            groupDir.mkdirs();
            return groups;
        }
        for (String groupFile : groupDir.list()) {
            if (!groupFile.contains(".yml"))
                continue;
            String groupName = groupFile.replace(".yml", "");
            CitizenGroup group = loadCitizenGroup(manager, groupName);
            if (group != null)
                groups.put(group.getName().toLowerCase(), group);
        }
        return groups;
    }

    @Override
    public boolean saveCitizenGroup(CitizenGroup group, boolean now) {
        String groupType = group.getGroupType();
        File groupDir = new File(groupsDir + File.separator + groupType);
        File groupFile = new File(groupDir + File.separator + group.getName() + ".yml");
        try {
            groupFile.createNewFile();
        } catch (IOException e) {
            Townships.log(Level.SEVERE, "Could not save " + group.getName() + " - file could not be created!");
            e.printStackTrace();
            return false;

        }

        FileConfiguration groupConfig = new YamlConfiguration();

        groupConfig.set(groupType + ".display-name", group.getDisplayName());
        groupConfig.set(groupType + ".prefix", group.getPrefix());
        groupConfig.set(groupType + ".money", group.getBankMoney());
        groupConfig.set(groupType + ".pvp-toggle", group.isPvp());

        groupConfig.set(groupType + ".creation-date", group.getCreationDate());
        if (group instanceof Taxable) {
            Taxable taxableGroup = (Taxable) group;
            groupConfig.set(groupType + ".tax-cost", taxableGroup.getTax());
            groupConfig.set(groupType + ".missed-payments", taxableGroup.getMissedPayments());
            groupConfig.set(groupType + ".last-tax-date", taxableGroup.getLastTax());
            groupConfig.set(groupType + ".last-tax-warning", taxableGroup.getLastTaxWarning());
        }

        if (group instanceof Mayoral) {
            Mayoral mayoralGroup = (Mayoral) group;
            if (mayoralGroup.getMayor() == null)
                validateMayor(group);
            groupConfig.set(groupType + ".mayor", mayoralGroup.getMayor());
            groupConfig.set(groupType + ".successor", mayoralGroup.getSuccessor());
            groupConfig.set(groupType + ".last-citizen-warning", mayoralGroup.getLastCitizenWarning());
        }

        if (group instanceof Regionable) {
            Regionable regionedGroup = (Regionable) group;
            if (regionedGroup.getRegion() != null) {
                groupConfig.set("region.town-region", regionedGroup.getRegion().getName());
                if (regionedGroup.getCenter() != null ) {
                    groupConfig.set("region.center.x", regionedGroup.getCenter().getX());
                    groupConfig.set("region.center.y", regionedGroup.getCenter().getY());
                    groupConfig.set("region.center.z", regionedGroup.getCenter().getZ());
                }
                groupConfig.set("region.town-type", regionedGroup.getType().name());
                groupConfig.set("region.claim-world", regionedGroup.getClaimedWorld());
            }
        }

        // Save custom Rank names
        for (Map.Entry<Rank, String> e : group.getRanks().entrySet()) {
            groupConfig.set("ranks." + e.getKey().name(), e.getValue());
        }

        // Save Group relations
        for (Map.Entry<String, Relation> r : group.getGroupRelations().entrySet()) {
            groupConfig.set("relations." + r.getKey(), r.getValue().name());
        }

        // Save Group Citizens
        for (Map.Entry<String, Rank> c : group.getCitizens().entrySet()) {
            groupConfig.set("citizens." + c.getKey() + ".rank", c.getValue().name());
            groupConfig.set("citizens." + c.getKey() + ".last-login", group.getCitizenLastLogin(c.getKey()));
        }

        // Save Group Non-citizens
        for (Map.Entry<String, Rank> c : group.getNoncitizens().entrySet()) {
            groupConfig.set("non-citizens." + c.getKey() + ".rank", c.getValue().name());
        }

        saveBankContents(group,groupConfig);

        try {
            groupConfig.save(groupFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }

    @Override
    public CitizenGroup loadCitizenGroup(CitizenGroupManager manager, String name) {
        File groupDir = new File(groupsDir + File.separator + manager.getName());
        File groupFile = new File(groupDir + File.separator + name + ".yml");
        if (!groupFile.exists()) {
            return null;
        }
        FileConfiguration tConfig = YamlConfiguration.loadConfiguration(groupFile);

        CitizenGroup group = super.getNewCitizenGroup(manager, name);

        group.setDisplayName(tConfig.getString(manager.getName() + ".display-name"));
        group.setPrefix(tConfig.getString(manager.getName() + ".prefix"));
        group.setBankMoney(tConfig.getInt(manager.getName() + ".money"));
        group.setPvp(tConfig.getBoolean(manager.getName() + ".pvp-toggle"));

        if (group instanceof Regionable) {
            Regionable regionedGroup = (Regionable) group;
            GroupType type;
            try {
                type = GroupType.valueOf(tConfig.getString("region.town-type", "SMALL").toUpperCase());
            } catch (IllegalArgumentException e) {
                type = GroupType.SMALL;
            }
            regionedGroup.setType(type);
            regionedGroup.setClaimedWorld(tConfig.getString("region.claim-world"));
            if (tConfig.getConfigurationSection("region.center") != null) {
                int x = tConfig.getInt("region.center.x");
                int y = tConfig.getInt("region.center.y");
                int z = tConfig.getInt("region.center.z");
                regionedGroup.setCenter(new CitizenGroupCenter(x,y,z));
            }
        }
        group.setCreationDate(tConfig.getLong(group.getName() + ".creation-date"));
        if (group instanceof Taxable) {
            Taxable taxableGroup = (Taxable) group;
            taxableGroup.setLastTax(tConfig.getLong(manager.getName() + ".last-tax-date"));
            taxableGroup.setLastTaxWarning(tConfig.getLong(manager.getName() + ".last-tax-warning"));
            taxableGroup.setMissedPayments(tConfig.getInt(manager.getName() + ".missed-payments"));
            taxableGroup.setLastTax(tConfig.getInt(manager.getName() + ".tax-cost"));
        }

        if (group instanceof Mayoral) {
            Mayoral mayoralGroup = (Mayoral) group;
            group.setNewCitizenRank(tConfig.getString(manager.getName() + ".mayor"), Rank.OWNER);
            group.setNewCitizenRank(tConfig.getString(manager.getName() + ".successor"), Rank.SUCCESSOR);
            mayoralGroup.setLastCitizenWarning(tConfig.getLong(manager.getName() + ".last-citizen-warning"));
        }

        loadCitizens(group, tConfig.getConfigurationSection("citizens"));
        loadRankNames(group, tConfig.getConfigurationSection("ranks"));
        loadRelations(group, tConfig.getConfigurationSection("relations"));
        loadNonCitizens(group, tConfig.getConfigurationSection("non-citizens"));
        List<?> bankSection = tConfig.getList("bank");
        loadBank(group, bankSection);
        return group;
    }


    // Necessary in the event the town's save file lost the mayor info, we don't want Nulls!
    private void validateMayor(CitizenGroup group) {
        if (!(group instanceof Mayoral))
            return;
        Mayoral mayoralGroup = (Mayoral) group;

        if (mayoralGroup.getMayor() == null) {
            Map<String,Rank> ranks = group.getCitizens();
            for (Map.Entry<String,Rank> entry : ranks.entrySet()) {
                if (entry.getValue() == Rank.OWNER) {
                    group.setNewCitizenRank(entry.getKey(), Rank.OWNER);
                }
            }
        }
    }

    private void saveBankContents(CitizenGroup group, FileConfiguration config) {
        Map<String, BankItem> bank = group.getBankContents();
        if (bank == null)
            return;
        ConfigurationSection section = config.getConfigurationSection("");
        ArrayList<LinkedHashMap<String,Object>> bankSection = new ArrayList<>();
        for (Map.Entry<String, BankItem> entry : bank.entrySet()) {
            BankItem bankItem = entry.getValue();
            int amount = entry.getValue().getAmount();
            ItemStack itemStack = bankItem.getItem().clone();
            ItemMeta meta = itemStack.getItemMeta();
            LinkedHashMap<String,Object> itemMap = new LinkedHashMap<>();
            String itemName = Items.itemByStack(itemStack).getName();
            itemMap.put("item",itemName);
            if (meta.hasDisplayName()) {
                itemMap.put("name", meta.getDisplayName().replace("\u00a7", "&"));
            }
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                List<String> converted = new ArrayList<>();
                for (String loreString : lore) {
                    converted.add(loreString.replace("\u00a7", "&"));
                }
                itemMap.put("lore",converted);
            }
            itemMap.put("amount",amount);
            bankSection.add(itemMap);
        }
        section.set("bank", bankSection);
    }

    @Override
    public boolean saveCitizenGroups(CitizenGroupManager manager) {
        if (manager.getGroups().isEmpty())
            return true;
        for (CitizenGroup group : manager.getGroups()) {
            saveCitizenGroup(group, true);
        }
        return true;
    }

    @Override
    public void deleteCitizenGroup(CitizenGroupManager manager, CitizenGroup group) {
        File groupDir = new File(groupsDir + File.separator + manager.getName());
        File groupFile = new File(groupDir + File.separator + group.getName() + ".yml");
        if (!groupFile.exists()) {
            return;
        }
        groupFile.delete();

    }


    @Override
    public void saveCitizens() {
        for (Citizen citizen : plugin.getCitizenManager().getCitizens()) {
            saveCitizen(citizen,true);
        }
    }

    @Override
    public boolean saveCitizen(Citizen citizen, boolean now) {
        String playerName = citizen.getName();
        File playersFolder = new File(citizensDir + File.separator + playerName.toLowerCase().substring(0, 1));
        playersFolder.mkdir();
        File cFile = new File(playersFolder, playerName + ".yml");
        try {
            cFile.createNewFile();
        } catch (IOException e) {
            Townships.log(Level.SEVERE, "Could not save " + citizen.getName() + " - file could not be created");
            e.printStackTrace();
            return false;
        }
        FileConfiguration cConfig = new YamlConfiguration();
        cConfig.set("last-login", citizen.getLastLogin().getTime());

        // Save established CitizenGroups
        for (Map.Entry<String, CitizenGroup> entry : citizen.getGroups().entrySet()) {
            String type = entry.getValue().getGroupType();
            cConfig.set("groups." + type + ".name", entry.getValue().getName());

        }
        // Save PendingGroups
        for (Map.Entry<String, PendingGroup> entry : citizen.getPendingGroups().entrySet()) {
            if (entry.getValue() != null)  {
            String type = entry.getValue().getGroupType();
            cConfig.set("pending." + type.toLowerCase() + ".name", entry.getValue().name);
            }
        }
        // Save PendingGroup ownerships
        for (String groupType : citizen.getPendingOwnerships()) {
            cConfig.set("pending." + groupType.toLowerCase() + ".owner", true);
        }
        try {
            cConfig.save(cFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public Citizen loadCitizen(Player player) {
        String playerName = player.getName();
        File playersFolder = new File(citizensDir + File.separator + playerName.toLowerCase().substring(0, 1));
        playersFolder.mkdir();
        File cFile = new File(playersFolder, playerName + ".yml");
        if (!cFile.exists())
            return null;
        FileConfiguration cConfig = YamlConfiguration.loadConfiguration(cFile);
        Citizen citizen = new Citizen(player);
        long time = cConfig.getLong("last-login");
        citizen.setLastLogin(time);
        if (cConfig.getConfigurationSection("groups") != null) {
            for (String group : cConfig.getConfigurationSection("groups").getKeys(false)) {
                CitizenGroupManager manager = Townships.getInstance().getGroupManager(group);
                if (manager != null) {
                    // Only fetch the group name if the manager exists
                    String groupName = cConfig.getString("groups." + group + ".name");
                    CitizenGroup citGroup = manager.get(groupName);
                    if (citGroup != null) {
                        citizen.setCitizenGroup(citGroup);
                    }
                } else {
                    Townships.debugLog(Level.WARNING, "The citizen: " + playerName + " has a group that's manager doesn't exist!");
                }
            }
        }
        if (cConfig.getConfigurationSection("pending") != null) {
            for (String group : cConfig.getConfigurationSection("pending").getKeys(false)) {
                CitizenGroupManager manager = Townships.getInstance().getGroupManager(group);
                if (manager != null) {
                    PendingGroup citGroup = manager.getPending(cConfig.getString("pending." + group + ".name"));
                    if (citGroup != null) {
                        citizen.setPendingGroup(citGroup);
                        if (citGroup.getOwner().equalsIgnoreCase(citizen.getName())) {
                            citizen.setPendingGroupOwner(manager, cConfig.getBoolean("pending." + group + ".owner", false));
                        }
                    }
                }
            }
        }
        return citizen;
    }

    public Citizen loadOfflineCitizen(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (!player.hasPlayedBefore()) // Bukkit KNOWS the player has NOT logged in this server before, so we don't have a file on hand.
            return null;
        String playerName = player.getName();
        File playersFolder = new File(citizensDir + File.separator + playerName.toLowerCase().substring(0, 1));
        playersFolder.mkdir();
        File cFile = new File(playersFolder, playerName + ".yml");
        if (!cFile.exists())
            return null;
        FileConfiguration cConfig = YamlConfiguration.loadConfiguration(cFile);
        Citizen citizen = new Citizen(name);
        long time = cConfig.getLong("last-login");
        citizen.setLastLogin(time);
        if (cConfig.getConfigurationSection("groups") != null) {
            for (String group : cConfig.getConfigurationSection("groups").getKeys(false)) {
                CitizenGroupManager manager = Townships.getInstance().getGroupManager(group);
                if (manager != null) {
                    // Only fetch the group name if the manager exists
                    String groupName = cConfig.getString("groups." + group + ".name");
                    CitizenGroup citGroup = manager.get(groupName);
                    if (citGroup != null) {
                        citizen.setCitizenGroup(citGroup);
                    }
                }
            }
        }
        if (cConfig.getConfigurationSection("pending") != null) {
            for (String group : cConfig.getConfigurationSection("pending").getKeys(false)) {
                CitizenGroupManager manager = Townships.getInstance().getGroupManager(group);
                if (manager != null) {
                    PendingGroup citGroup = manager.getPending(cConfig.getString("pending." + group + ".name"));
                    if (citGroup != null) {
                        citizen.setPendingGroup(citGroup);
                        if (citGroup.getOwner().equalsIgnoreCase(citizen.getName())) {
                            citizen.setPendingGroupOwner(manager, cConfig.getBoolean("pending." + group + ".owner", false));
                        }
                    }
                }
            }
        }
        return citizen;
    }

    @Override
    public void saveManagerData(CitizenGroupManager manager) {
        if (!manager.getManagerFile().exists()) {
            try {
                manager.getManagerFile().createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        FileConfiguration mConfig = new YamlConfiguration();

        mConfig.set("last-interval", manager.lastInterval);

        //Load in towns that are still
        ConfigurationSection pendingSection = mConfig.getConfigurationSection("pending");
        if (pendingSection == null) {
            pendingSection = mConfig.createSection("pending");
        }

        // Ensure that we're keeping capitalization for pending groups in storage
        for (Map.Entry<String,PendingGroup> i : manager.getPending().entrySet()) {
            pendingSection.set(i.getValue().name + ".members", new ArrayList<>(i.getValue().getMembers()));
            pendingSection.set(i.getValue().name + ".creation-date", i.getValue().getCreationDate());
            pendingSection.set(i.getValue().name + ".owner", i.getValue().getOwner());
        }
        if (manager instanceof RegionedCitizenGroupManager) {
            saveRegionManagerData(((RegionedCitizenGroupManager) manager).getRegionManager());
        }
        try {
            mConfig.save(manager.getManagerFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void saveRegionManagerData(GroupRegionManager manager) {
        File managersFolder = new File(regionsDir + File.separator + manager.getManager().getName());
        managersFolder.mkdirs();

        File regionFile = new File(managersFolder + File.separator + manager.getManager().getName() + ".yml");
        if (!regionFile.exists()) {
            try {
                regionFile.createNewFile();
            } catch (IOException e) {
                return;
            }
        }
        FileConfiguration regionConfig = new YamlConfiguration();
        ConfigurationSection regionSection = regionConfig.getConfigurationSection("group-regions");
        if (regionSection == null)
            regionSection = regionConfig.createSection("group-regions");

        for (Map.Entry<CitizenGroup, Region> entry : manager.getRegions().entrySet()) {
            regionSection.set(entry.getKey().getName() + ".region", entry.getValue().getName());
            regionSection.set(entry.getKey().getName() + ".owners", new ArrayList<>(entry.getValue().getOwners()));
            regionSection.set(entry.getKey().getName() + ".managers", new ArrayList<>(entry.getValue().getManagers()));
            regionSection.set(entry.getKey().getName() + ".guests", new ArrayList<>(entry.getValue().getGuests()));
        }
        if (!manager.getChildRegions().isEmpty()) {
            ConfigurationSection childSection = regionConfig.getConfigurationSection("child-regions");
            if (childSection == null)
                childSection = regionConfig.createSection("child-regions");
            // For every CitizenGroup, there is a Map of Child regions
            for (Map.Entry<CitizenGroup, Map<String, ChildRegion>> childRegions : manager.getChildRegions().entrySet()) {
                Map<String, ChildRegion> childRegionEntry = childRegions.getValue();

                // Grab the Parent section
                ConfigurationSection parentRegion = childSection.createSection(childRegions.getKey().getName());

                // Process each child region - Each region knows which region is it's parent, no need to save that
                for (Map.Entry<String,ChildRegion> childRegion : childRegionEntry.entrySet()) {
                    parentRegion.set(childRegion.getKey() + ".region", childRegion.getValue().getName());
                    parentRegion.set(childRegion.getKey() + ".owners", new ArrayList<>(childRegion.getValue().getOwners()));
                    parentRegion.set(childRegion.getKey() + ".managers", new ArrayList<>(childRegion.getValue().getManagers()));
                    parentRegion.set(childRegion.getKey() + ".guests", new ArrayList<>(childRegion.getValue().getGuests()));                }
            }
        }

        try {
            regionConfig.save(regionFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void loadManagerData(CitizenGroupManager manager) {
        if (!manager.getManagerFile().exists()) {
            try {
                manager.getManagerFile().createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        FileConfiguration mConfig = new YamlConfiguration();
        try {
            mConfig.load(manager.getManagerFile());
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
            return;
        }
        if (manager instanceof RegionedCitizenGroupManager) {
            loadRegionManagerData(((RegionedCitizenGroupManager) manager).getRegionManager());
        }
        //Load in pending groups that are still pending
        ConfigurationSection pendingSection = mConfig.getConfigurationSection("pending");
        if (pendingSection != null) {
            for (String key : pendingSection.getKeys(false)) {
                // Have to resort to some reflection to find the proper PendingGroup type to instantiate
                PendingGroup group = super.getNewPendingGroup(manager, key, pendingSection.getStringList(key + ".members"));
                group.setCreationDate(pendingSection.getLong(key+".creation-date"));
                group.setOwner(pendingSection.getString(key + ".owner"));
                manager.addPending(group);
            }
        }

    }

    @Override
    public void loadRegionManagerData(GroupRegionManager manager) {
        File managersFolder = new File(regionsDir + File.separator + manager.getManager().getName());
        managersFolder.mkdirs();

        File regionFile = new File(managersFolder + File.separator + manager.getManager().getName() + ".yml");
        if (!regionFile.exists()) {
            try {
                regionFile.createNewFile();
                return;
            } catch (IOException e) {
                return;
            }
        }
        FileConfiguration regionConfig = new YamlConfiguration();
        try {
            regionConfig.load(regionFile);
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
            return;
        }

        ConfigurationSection regionSection = regionConfig.getConfigurationSection("group-regions");
        if (regionSection == null)
            return;

        // Load each CitizenGroup master Region
        for (String key : regionSection.getKeys(false)) {
            String regionName = regionSection.getString(key + ".region");
            ProtectedRegion wgregion = plugin.getWorldGuardManager().getRegionManager(((Regionable) manager.getManager().get(key))).getRegion(regionName);
            Region groupRegion = new Region(wgregion);
            groupRegion.addGuests(regionSection.getStringList(key + ".guests"));
            groupRegion.addManagers(regionSection.getStringList(key + ".managers"));
            groupRegion.addOwners(regionSection.getStringList(key + ".owners"));
            // Finally, add the region to the manager's registry, this also registers the group's region
            manager.addRegion(manager.getManager().get(key),groupRegion);
        }

        // Load Child Regions for each CitizenGroupRegion
        ConfigurationSection childSections = regionConfig.getConfigurationSection("child-regions");
        if (childSections == null)
            return;

        for (String groupName : childSections.getKeys(false)) { // Grab all Parent owned regions
            ConfigurationSection parentSection = childSections.getConfigurationSection(groupName);
            CitizenGroup group = manager.getManager().get(groupName);
            if (parentSection == null)
                continue;
            for (String childSection : parentSection.getKeys(false)) { // Grab all Child regions of parent region
                String childName = parentSection.getString(childSection + ".region");
                ProtectedRegion wgregion = plugin.getWorldGuardManager().getRegionManager((Regionable) group).getRegion(childName);
                if (wgregion == null)
                    continue; // Means this
                ChildRegion childRegion = new ChildRegion(group, wgregion);
                childRegion.addOwners(parentSection.getStringList(childSection + ".owners"));
                childRegion.addManagers(parentSection.getStringList(childSection + ".managers"));
                childRegion.addGuests(parentSection.getStringList(childSection + ".guests"));
                manager.addChildRegion(group, childRegion);

            }
        }
    }
}
