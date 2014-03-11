package com.herocraftonline.townships.storage.managers;

import com.herocraftonline.herostorage.api.SQLSerializerConfig;
import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.CitizenGroup;
import com.herocraftonline.townships.api.CitizenGroupManager;
import com.herocraftonline.townships.api.GroupRegionManager;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.storage.SQLInstance;
import com.herocraftonline.townships.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Author: gabizou
 */
public class SQLStorage extends Storage {

    SQLInstance sql;
    private final BukkitTask id;
    private final int SAVE_INTERVAL = 6000;
    private final Map<String, Citizen> citizensToSave = new ConcurrentHashMap<>();
    private final Map<String, Town> townsToSave = new ConcurrentHashMap<>();

    public SQLStorage(Townships plugin) {
        super(plugin,"sql");
        this.sql = new SQLInstance();
        SQLSerializerConfig.configureSQL(this.plugin, sql, this.plugin.getConfig().getConfigurationSection("database"));
        id = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new TownshipsSaveThread(), SAVE_INTERVAL, SAVE_INTERVAL);
    }

    protected class TownshipsSaveThread implements Runnable {
        @Override
        public void run() {
            if (!citizensToSave.isEmpty()) {
                final Iterator<Map.Entry<String, Citizen>> iterator = citizensToSave.entrySet().iterator();
                while (iterator.hasNext()) {
                    final Citizen citizen = iterator.next().getValue();
                    try {
                        saveCitizen(citizen, true);
                    } catch (final Exception e) {
                        Townships.log(Level.SEVERE, "There was an issue saving the Citizen: "
                                + citizen.getName());
                        e.printStackTrace();
                        continue;
                    }
                    iterator.remove();
                }
            }
            if (!townsToSave.isEmpty()) {
                final Iterator<Map.Entry<String, Town>> iterator = townsToSave.entrySet().iterator();
                while (iterator.hasNext()) {
                    final Town town = iterator.next().getValue();
                    try {
                        saveCitizenGroup(town, true);
                    } catch (final Exception e) {
                        Townships.log(Level.SEVERE, "There was an issue saving the Citizen: " + town.getName());
                        e.printStackTrace();
                        continue;
                    }
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public boolean saveCitizenGroup(CitizenGroup group, boolean now) {
        return false;
    }

    @Override
    public CitizenGroup loadCitizenGroup(CitizenGroupManager manager, String name) {
        return null;
    }

    @Override
    public Map<String, CitizenGroup> loadCitizenGroups(CitizenGroupManager manager) {
        return null;
    }

    @Override
    public boolean saveCitizenGroups(CitizenGroupManager manager) {
        return false;
    }

    @Override
    public void deleteCitizenGroup(CitizenGroupManager manager, CitizenGroup group) {

    }

    @Override
    public void saveCitizens() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean saveCitizen(Citizen citizen, boolean now) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Citizen loadCitizen(Player player) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Citizen loadOfflineCitizen(String name) {
        return null;
    }


    @Override
    public void saveManagerData(CitizenGroupManager manager) {


    }

    @Override
    public void saveRegionManagerData(GroupRegionManager manager) {

    }

    @Override
    public void loadManagerData(CitizenGroupManager manager) {


    }

    @Override
    public void loadRegionManagerData(GroupRegionManager manager) {

    }
}
