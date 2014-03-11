package com.herocraftonline.townships.groups.guild;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.CitizenGroup;
import com.herocraftonline.townships.api.CitizenGroupManager;
import com.herocraftonline.townships.api.PendingGroup;
import com.herocraftonline.townships.api.config.CitizenGroupConfig;
import com.herocraftonline.townships.command.Command;

import java.io.File;
import java.util.Map;

/**
 * @author gabizou
 */

public class GuildManager extends CitizenGroupManager {

    public GuildManager(Townships plugin) {
        super(plugin,new File(plugin.getDataFolder(), "guildsConfig.yml"));
    }

    @Override
    public String getName() {
        return "guild";
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
        return PendingGuild.class;
    }

    @Override
    public Class<? extends CitizenGroup> getCitizenGroupClass() {
        return Guild.class;
    }

    @Override
    public void save() {
        storage.saveCitizenGroups(this);
        storage.saveManagerData(this);
    }

    @Override
    public void load() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void delete(CitizenGroup group) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void removeCitizenFromPendingGroup(String name, Citizen c, boolean message) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addCitizenToPendingGroup(String name, String citizen) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
