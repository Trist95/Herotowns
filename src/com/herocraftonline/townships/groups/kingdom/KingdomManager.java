package com.herocraftonline.townships.groups.kingdom;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.CitizenGroup;
import com.herocraftonline.townships.api.PendingGroup;
import com.herocraftonline.townships.api.RegionedCitizenGroupManager;
import com.herocraftonline.townships.api.config.CitizenGroupConfig;
import com.herocraftonline.townships.command.Command;
import org.bukkit.Location;

import java.io.File;
import java.util.Map;

/**
 * @author gabizou
 */

public class KingdomManager extends RegionedCitizenGroupManager {

    public KingdomManager(Townships plugin) {
        super(plugin,new File(plugin.getDataFolder(), "kingdomsConfig.yml"));
    }

    @Override
    public String getName() {
        return "kingdom";
    }

    @Override
    public CitizenGroupConfig loadGroupConfig() {
        return null;
    }

    @Override
    public Map<String, Command> getCommands() {
        return null;
    }

    @Override
    public Class<? extends PendingGroup> getPendingGroupClass() {
        return PendingKingdom.class;
    }

    @Override
    public Class<? extends CitizenGroup> getCitizenGroupClass() {
        return Kingdom.class;
    }

    @Override
    public void save() {
        storage.saveCitizenGroups(this);
        storage.saveManagerData(this);

    }

    @Override
    public void load() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean claimArea(CitizenGroup group, Location location) {
        return false;
    }

    @Override
    public boolean reclaimArea(CitizenGroup group, Location location) {
        return false;
    }

    @Override
    public void delete(CitizenGroup group) {

    }

    @Override
    protected void removeCitizenFromPendingGroup(String name, Citizen c, boolean message) {

    }

    @Override
    public void addCitizenToPendingGroup(String name, String citizen) {

    }


    @Override
    public void registerRegionManager() {
        if (wgm == null) {
            wgm = new KingdomRegionManager(this);
        }
        plugin.getWorldGuardManager().registerRegionManager(this,wgm);
    }
}
