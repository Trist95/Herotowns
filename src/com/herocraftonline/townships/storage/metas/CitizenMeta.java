package com.herocraftonline.townships.storage.metas;

import com.herocraftonline.herostorage.storageobjects.PlayerMeta;

/**
 * CitizenMeta is the general Player information regarding what town and guild a player is mainly
 * enlisted as. These groups are the primary groups associated with the player.
 *
 * @author gabizou
 */
public class CitizenMeta extends PlayerMeta {

    long townJoinDate,guildJoinDate,townLastJoinDate,guildLastJoinDate;
    String town,guild;

    public long getTownJoinDate() {
        return townJoinDate;
    }

    public void setTownJoinDate(long townJoinDate) {
        this.townJoinDate = townJoinDate;
    }

    public long getGuildJoinDate() {
        return guildJoinDate;
    }

    public void setGuildJoinDate(long guildJoinDate) {
        this.guildJoinDate = guildJoinDate;
    }

    public long getTownLastJoinDate() {
        return townLastJoinDate;
    }

    public void setTownLastJoinDate(long townLastJoinDate) {
        this.townLastJoinDate = townLastJoinDate;
    }

    public long getGuildLastJoinDate() {
        return guildLastJoinDate;
    }

    public void setGuildLastJoinDate(long guildLastJoinDate) {
        this.guildLastJoinDate = guildLastJoinDate;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getGuild() {
        return guild;
    }

    public void setGuild(String guild) {
        this.guild = guild;
    }
}
