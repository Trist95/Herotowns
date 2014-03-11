package com.herocraftonline.townships.api;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.groups.guild.Guild;
import com.herocraftonline.townships.groups.guild.PendingGuild;
import com.herocraftonline.townships.groups.town.PendingTown;
import com.herocraftonline.townships.groups.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class Citizen {

    // Transitive Properties - Not to be saved to DB
    private transient final OfflinePlayer player;
    private transient Map<String, CitizenGroup> invites = new HashMap<>();

    // Persisting Properties - To be saved to DB
    private Date lastLogin = null;
    private Map<String, CitizenGroup> groups = new HashMap<>();
    private Map<String, PendingGroup> pendingGroups = new HashMap<>();
    private List<String> ownerships = new LinkedList<>();

    public Citizen (Player player) {
        this.player = player;
    }

    public Citizen(OfflinePlayer player) {
        this.player = player;
    }

    /**
     * This will ALWAYS create a new citizen regardless of player name. This is UNSAFE and should NOT be used
     * lightly.
     * @param name
     */
    public Citizen(String name) {
        this.player = Bukkit.getOfflinePlayer(name);
    }

    public boolean isOnline() {
        return player.isOnline();
    }

    public boolean hasPlayedBefore() {
        return player.hasPlayedBefore();
    }

    /**
     * Return the player object associated with this citizen
     *
     * @return Player object associated with this Citizen
     */
    public Player getPlayer() {
        return player.getPlayer();
    }

    public String getName() {
        return player.getName();
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date date) {
        lastLogin = date;
    }

    public void setLastLogin(long time) {
        lastLogin = new Date(time);
    }

    /**
     * Get the town this Citizen is a part of
     *
     * @return Town that this citizen believes it is a part of, not always true.
     */
    public Town getTown() {
        return (Town) groups.get("town");
    }

    /**
     * Sets this Citizens town
     *
     * @param town Town that this citizen is now a part of
     */
    public void setTown(Town town) {
        if (town == null)
            groups.remove("town");
        else
            groups.put("town", town);
    }

    /**
     * Gets this Citizens guild
     * @return the Guild
     */
    public Guild getGuild() {
        return (Guild) groups.get("guild");
    }

    /**
     * Sets this citizen's guild
     * @param guild - The guild that this citizen is being set to.
     */
    public void setGuild(Guild guild) {
        if (guild == null)
            groups.remove("guild");
        else
            groups.put("guild", guild);
    }

    /**
     * Returns if the Citizen is a part of a town
     * @return
     */
    public boolean hasTown() {
        return groups.containsKey("town");
    }

    public boolean hasGuild() {
        return groups.containsKey("guild");
    }

    /**
     * Return the citizens rank for the town they are a part of
     * Return's NONE if the citizen has no town
     *
     * @return Rank of the citizen in the citizen's current town.
     */
    public Rank getRank() {
        return groups.get("town") != null ? groups.get("town").getCitizenRank(this) : Rank.NONE;
    }

    /**
     * @return the pendingGuild
     */
    public PendingGuild getPendingGuild() {
        return (PendingGuild) pendingGroups.get("guild");
    }

    /**
     * @param pendingGuild the pendingGuild to the value given
     */
    public void setPendingGuild(PendingGuild pendingGuild) {
        if (pendingGuild == null)
            pendingGroups.remove("guild");
        else
            pendingGroups.put("guild", pendingGuild);
    }

    /**
     * @return the pendingTown
     */
    public PendingTown getPendingTown() {
        return (PendingTown) pendingGroups.get("town");
    }

    /**
     * @return true if this citizen is the pendingGuildOwner
     */
    public boolean isPendingGuildOwner() {
        return ownerships.contains("guild");
    }

    /**
     * Sets this citizen as the pendingGuildOwner or removes them as a pendingGuildOwner
     * @param pendingGuildOwner
     */
    public void setPendingGuildOwner(boolean pendingGuildOwner) {
        if (pendingGuildOwner)
            ownerships.add("guild");
        else
            ownerships.remove("guild");
    }

    /**
     * @param pendingTown the pendingTown to set
     */
    public void setPendingTown(PendingTown pendingTown) {
        if (pendingTown == null)
            pendingGroups.remove("town");
        else
            pendingGroups.put("town",pendingTown);
    }

    /**
     * @return the pendingTownOwner
     */
    public boolean isPendingTownOwner() {
        return ownerships.contains(Townships.getInstance().getTownManager().getName());
    }

    /**
     * @param pendingTownOwner the pendingTownOwner to set
     */
    public void setPendingTownOwner(boolean pendingTownOwner) {
        if (pendingTownOwner)
            ownerships.add(Townships.getInstance().getTownManager().getName());
        else
            ownerships.remove(Townships.getInstance().getTownManager().getName());

    }

    // -----------------
    // Generic Citizen methods
    // -----------------

    public void setCitizenGroup(CitizenGroup name) {
        groups.put(name.getGroupType(), name);
    }

    public CitizenGroup getSpecificCitizenGroup(String name) {
        if (groups.containsKey(name))
            return groups.get(name);
        else
            return null;
    }

    public void setPendingGroup(PendingGroup group) {
        pendingGroups.put(group.getGroupType(), group);
    }

    public PendingGroup getSpecificPendingGroup(String name) {
        if (pendingGroups.containsKey(name)) {
            return pendingGroups.get(name);
        }
        return null;
    }

    public void setPendingGroupOwner(CitizenGroupManager group, boolean owner) {
        if (ownerships.contains(group.getName()))
            ownerships.add(group.getName());
        else
            ownerships.remove(group.getName());
    }

    public boolean isPendingGroupOwner(CitizenGroupManager group) {
        return ownerships.contains(group.getName());
    }

    public Map<String, CitizenGroup> getGroups() {
        return Collections.unmodifiableMap(groups);
    }

    public Map<String, PendingGroup> getPendingGroups() {
        return Collections.unmodifiableMap(pendingGroups);
    }

    public List<String> getPendingOwnerships() {
        return Collections.unmodifiableList(ownerships);
    }

    // -----------------
    // Invite methods
    // -----------------

    /**
     * Removes all pending invites from the Citizen
     */
    public void removeInvites() {
        invites.clear();
    }

    /**
     * Remove a single invite from the citizengroup
     * @param group
     */
    public void removeInvite(CitizenGroup group) {
        invites.remove(group.getName().toLowerCase());
    }

    /**
     * Adds an invite to the specified group
     * @param group
     */
    public void addInvite(CitizenGroup group) {
        invites.put(group.getName().toLowerCase(),group);
    }

    /**
     * Checks if the citizen is invited to join a specific citizen group
     * @param group - CitizenGroup in query
     * @return
     */
    public boolean hasInvite(CitizenGroup group) {
        return invites.containsKey(group.getName().toLowerCase());
    }

    /**
     * Checks if the citizen is invited to join a CitizenGroup by name
     */
    public boolean hasInvite(String name) {
        return name != null && invites.containsKey(name.toLowerCase());
    }

    /**
     * Checks if this player has any pending invites
     * @return
     */
    public boolean hasInvites() {
        return !invites.isEmpty();
    }


    /**
     * returns a list of all invites this citizen has
     * @return
     */
    public Map<String, CitizenGroup> getInvites() {
        return Collections.unmodifiableMap(invites);
    }

    /**
     * Returns a list of CitizenGroups by name, does not regard the type of CitizenGroup
     * @return
     */
    public List<String> getInviteNames() {
        List<String> vals = new ArrayList<>();
        for (Map.Entry<String,CitizenGroup> entry : invites.entrySet()) {
            vals.add(entry.getValue().getName());
        }
        return vals;
    }


    @Override
    public int hashCode() {
        return player.getName().hashCode();
    }

}
