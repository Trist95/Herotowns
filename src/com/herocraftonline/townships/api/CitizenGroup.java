package com.herocraftonline.townships.api;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.events.RankChangeEvent;
import com.herocraftonline.townships.util.BankItem;
import com.herocraftonline.townships.util.Messaging;
import net.milkbowl.vault.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;

public abstract class CitizenGroup {

    // Data storages
    private final String name;
    protected String displayName;
    protected String prefix;
    protected ChatColor color = ChatColor.WHITE;
    protected Map<String,BankItem> itemBank = new LinkedHashMap<>();
    protected int bank = 0;
    protected long creationDate = 0;

    protected Map<String, Rank> citizens = new HashMap<>();
    protected Map<String, Rank> noncitizens = new HashMap<>();
    protected Map<String, Long> lastLogins = new HashMap<>();
    protected Map<Rank, String> rankNames = Rank.getDefaultNames();

    protected Map<String, Relation> relations = new HashMap<>();
    protected Map<String, Relation> groupRelations = new HashMap<>();

    protected int joinCost = 0;
    private boolean pvp = false;

    // Transitive properties, never should be saved
    protected transient Set<String> invites = new HashSet<>();
    protected transient Set<Citizen> onlineCitizens = new HashSet<>();

    public CitizenGroup(String name, Integer bank) {
        if (name == null) {
            throw new IllegalArgumentException("Name can not be null.");
        }
        this.name = name;
        this.bank = bank;
        int index = name.length() > 2 ? 3 : name.length() > 1 ? 2 : 1;
        this.prefix = name.substring(0, index).toUpperCase();
    }

    // --------------------------
    // Generic Methods
    // --------------------------
    /**
     * @return the name of the group
     */
    public String getName() {
        return name;
    }

    /**
     * @return the display name of the group
     */
    public String getDisplayName() {
        return displayName;
    }

    public abstract String getGroupType();

    /**
     * Sets the group's display name
     * 
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Sets the group's prefix
     * 
     * @param prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the groups prefix
     * 
     * @return string - prefix
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Sets the group's chat color
     */
    public void setColor(ChatColor color) {
        this.color = color;
    }

    /**
     * Gets the group's chat color
     *
     * @return ChatColor - Color to be used in town chat
     */
    public ChatColor getColor() {
        return color;
    }

    // --------------------------
    // Member related Methods
    // --------------------------

    /**
     * Adds a Citizen to this Group with the specified Rank. Checks if the given Citizen doesn't have a Player object
     * assigned to it. Returns false if failed.
     * @param citizen
     * @param rank
     */
    public boolean addMember(Citizen citizen, Rank rank) {
        return citizen != null && addMember(citizen.getName(), rank);
    }

    /**
     * Add a String player. This doesn't perform any checks whether the player exists or not (useful for offline adding)
     * @param string
     * @param rank
     * @return
     */
    public boolean addMember(String string, Rank rank) {
        if (hasMember(string)) {
            citizens.remove(string);
            noncitizens.remove(string);
        }
        return setNewCitizenRank(string, rank);
    }

    /**
     * Removes a Citizen from the selected Group. This will check if the citizen is indeed online, else, please attempt
     * to use removeMember(String) for offline players.
     *
     * @param citizen
     * @return true if the remove was successful
     */
    public boolean removeMember(Citizen citizen) {
        return removeMember(citizen.getName());
    }

    /**
     * Attempts to remove a given player string from the Group. If the player by the name of the String isn't in the group,
     * returns false.
     *
     * @param string Name of the player being removed.
     * @return true if the remove was successful
     */
    public boolean removeMember(String string) {
        if (!hasMember(string))
            return false;
        if (hasMember(string))
            setNewCitizenRank(string,Rank.NONE);
        citizens.remove(string);
        noncitizens.remove(string);
        onlineCitizens.remove(string);
        return true;
    }

    /**
     * Returns an unmodifiable Map of members of the Group that are of Rank Citizen or above. Does not include
     * guests and enemies.
     * @return
     */
    public Map<String, Rank> getCitizens() {
        return Collections.unmodifiableMap(citizens);
    }

    /**
     * Returns an unmodifiable Map of non members of the Group that are of Rank Guest or Neutral.
     * @return
     */
    public Map<String, Rank> getNoncitizens() {
        return Collections.unmodifiableMap(noncitizens);
    }

    /**
     * Attempts to set the Citizen's new rank in the town. This does not perform any checks for whether the
     * citizen is of this town or not. Not to be used to add a new Citizen to the Town.
     * @param citizen Citizen's rank that is being set
     * @param rank The rank that the citizen will have
     * @return true if the change was successful
     */
    public boolean setNewCitizenRank(Citizen citizen, Rank rank) {
        return setNewCitizenRank(citizen.getName(), rank);
    }

    /**
     * Attempts to set the Citizen's new rank in the town. This does not perform any checks for whether the
     * citizen is of this town or not. Not to be used to add a new Citizen to the Town.
     * @param string Name of the citizen whose rank is being set
     * @param rank The rank that the citizen will have
     * @return true if the change was successful
     */
    public boolean setNewCitizenRank(String string, Rank rank) {
        if (rank == null) {
            rank = Rank.NONE;
        }
        if (string == null) // Just an added check, since this is an open API, anything can happen
            return false;
        if (Townships.isDoneLoading()) {
            Rank oldRank = getCitizenRank(string);
            if (oldRank != rank && Townships.isDoneLoading()) {
                RankChangeEvent event = new RankChangeEvent(string, this, oldRank,rank);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return false;
                }
            }
            // Let's debug this just to make sure we're not missing something somewhere
            Townships.debugLog(Level.INFO,"The citizenGroup: " + name + " is setting the rank of " +
                    "player: " + string + " from the old rank of: " + oldRank.toString() + ", to the" +
                    " new rank of: " + rank.toString());
        }
        setCitizenRank(string, rank);
        return true;
    }

    private final void setCitizenRank(String string, Rank rank) {
        switch (rank) {
            case NONE :
                citizens.remove(string);
                noncitizens.remove(string);
                break;
            case GUEST :
            case ENEMY :
                noncitizens.put(string, rank);
                citizens.remove(string);
                break;
            case OWNER :
                if (this instanceof Mayoral && rank == Rank.OWNER) {
                    Mayoral group = (Mayoral) this;
                    group.setMayor(string);
                }
            case SUCCESSOR :
                if (this instanceof Mayoral && rank == Rank.SUCCESSOR) {
                    Mayoral group = (Mayoral) this;
                    group.setSuccessor(string);
                }
            case CITIZEN :
            case MANAGER :
                noncitizens.remove(string);
                citizens.put(string,rank);
                break;
        }

    }

    /**
     * Check if this town has the Citizen added as a counting member. Does not check Guest or Enemy status.
     * @param citizen
     * @return
     */
    public boolean hasCitizen(Citizen citizen) {
        return citizen != null && hasCitizen(citizen.getName());
    }

    /**
     * Check if this town has the Named Citizen as a counting member. Does not check for Guest or Enemy status.
     * @param name
     * @return
     */
    public boolean hasCitizen(String name) {
        return citizens.containsKey(name);
    }

    /**
     * Check if the Citizen is labeled as a guest/citizen of the town. Does not check enemy status.
     * @param citizen
     * @return
     */
    public boolean hasMember(Citizen citizen) {
        return hasMember(citizen.getName());
    }

    /**
     * Check if the named Citizen is labeled as a guest/citizen of the town. Does not check enemy status.
     * @param name
     * @return
     */
    public boolean hasMember(String name) {
        return citizens.containsKey(name) || noncitizens.containsKey(name);
    }

    /**
     * Returns the rank of this Citizen.
     * @param citizen
     * @return
     */
    public Rank getCitizenRank(Citizen citizen) {
        if (citizen == null)
            return null;
        String name = citizen.getName();
        return getCitizenRank(name);
    }

    /**
     * Returns the rank of the named Citizen.
     * @param name
     * @return
     */
    public Rank getCitizenRank(String name) {
        Rank rank = null;
        if (citizens.containsKey(name))
            rank = citizens.get(name);
        else if (noncitizens.containsKey(name))
            rank = noncitizens.get(name);
        else
            rank = Rank.NONE;
        return rank;
    }

    /**
     * Attempts to set the citizen as being online. This is used for listing online members.
     * It will cross check that the citizen is indeed a member of the town.
     * @param citizen Citizen in question
     * @return true if citizen is set as logged in for this town
     */
    public boolean citizenLogin(Citizen citizen) {
        if (citizen.getPlayer() == null)
            return false;
        if (getCitizenRank(citizen).ordinal() < Rank.CITIZEN.ordinal())
            return false;
        onlineCitizens.add(citizen);
        lastLogins.put(citizen.getPlayer().getName(),citizen.getLastLogin().getTime());
        return true;
    }

    /**
     * Attempts to set the citizen as being offline. Removes from the Online Citizen Set.
     * Does not perform any checks.
     * @param citizen to set as logged off
     */
    public void citizenLogoff(Citizen citizen) {
        lastLogins.put(citizen.getPlayer().getName(),citizen.getLastLogin().getTime());
        onlineCitizens.remove(citizen);
    }

    public Set<Citizen> getOnlineCitizens() {
        return Collections.unmodifiableSet(onlineCitizens);
    }

    public Map<String, Long> getLastLogins() {
        return Collections.unmodifiableMap(lastLogins);
    }

    public void setLastLogins(Map<String, Long> logins) {
        this.lastLogins = logins;
    }

    /**
     * Updates the last login time for a given citizen to a given time. This should only be used when a player is
     * offline.
     *
     * @param name
     * @param time
     */
    public void updateLastLogin(String name, long time) {
        if (lastLogins.containsKey(name)) {
            long lastlogin = lastLogins.remove(name);
            if (Townships.isDoneLoading()) {
                if (time < lastlogin) {
                    Townships.log(Level.WARNING, "Attention! A Citizen's LastLogin is being set to an earlier date!");
                }
            }
            lastLogins.put(name, time);
        } else {
            lastLogins.put(name, time);
        }

    }

    /**
     * Fetches the last login time for the given name of a citizen. The login times are used for pruning citizens
     * from CitizenGroups if necessary.
     * @param name
     * @return
     */
    public long getCitizenLastLogin(String name) {
        return lastLogins.containsKey(name) ? lastLogins.get(name) : 0;
    }

    // --------------------------
    // Relation Methods
    // --------------------------

    /**
     * Group Related Relations. Used to define an entire group as an enemy or an ally or neutral
     * @return
     */
    public Map<String, Relation> getGroupRelations() {
        return Collections.unmodifiableMap(groupRelations);
    }

    public void addGroupRelation(CitizenGroup group, Relation relation) {
        if (group != null)
            groupRelations.put(group.getName().toLowerCase(), relation);
    }

    public void addGroupRelation(String group, Relation relation) {
        groupRelations.put(group.toLowerCase(), relation);
    }

    public void removeGroupRelation(CitizenGroup group) {
        if (group != null)
            groupRelations.remove(group.getName().toLowerCase());
    }

    public void removeGroupRelation(String group) {
        groupRelations.remove(group.toLowerCase());
    }

    public int getJoinCost() {
        return joinCost;
    }

    public void setJoinCost(int joinCost) {
        this.joinCost = joinCost;
    }

    public boolean isPvp() {
        return pvp;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    // ---------------------
    // Rank related Methods
    // ---------------------

    /**
     * Return the Town-Specific name for the rank.
     * 
     * @param rank
     * @return
     */
    public String getRankName(Rank rank) {
        return rankNames.containsKey(rank) ? rankNames.get(rank) : rank.name().toLowerCase();
    }

    /**
     * Sets a specific rank to the given name for display purposes
     * 
     * @param rank
     * @param rName
     */
    public void setRankName(Rank rank, String rName) {
        rankNames.put(rank, rName.toLowerCase());
    }

    /**
     * @return the rankNames mapping
     */
    public Map<Rank, String> getRanks() {
        return Collections.unmodifiableMap(rankNames);
    }

    // ----------------------
    //
    // Invite Methods
    // -----------------------

    /**
     * Adds an invite to the set of invited players
     */
    public boolean addInvite(String name) {
        return invites.add(name);
    }

    /**
     * removes an invite from the set of invited players
     */
    public boolean removeInvite(String name) {
        return invites.remove(name);
    }

    /**
     * 
     * @param citizen
     * @return if the name is on the invite list
     */
    public boolean hasInvite(Citizen citizen) {
        return invites.contains(citizen.getName());
    }

    /**
     * @return Set of all invited players
     */
    public Set<String> getInvites() {
        return Collections.unmodifiableSet(invites);
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long date) {
        this.creationDate = date;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof CitizenGroup))
            return false;

        return ((CitizenGroup) obj).name.equalsIgnoreCase(name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    // --------------------
    // Utility Methods
    // --------------------

    public Map<String,BankItem> getBankContents() {
        return itemBank;
    }

    public void depositItem(ItemStack itemStack) {
        String itemName;
        if (itemStack.getItemMeta().hasDisplayName()) {
            itemName = itemStack.getItemMeta().getDisplayName();
        } else {
            itemName = Items.itemByStack(itemStack).getName();
        }
        if (itemBank.containsKey(itemName)) {
            int amount = itemBank.remove(itemName).getAmount();
            amount += itemStack.getAmount();
            BankItem newItem = new BankItem(itemStack,amount);
            itemBank.put(itemName,newItem);
        } else {
            itemBank.put(itemName,new BankItem(itemStack,itemStack.getAmount()));
        }
    }

    public void depositItem(ItemStack itemStack, int adding) {
        String itemName;
        if (itemStack.getItemMeta().hasDisplayName()) {
            itemName = itemStack.getItemMeta().getDisplayName();
        } else {
            itemName = Items.itemByStack(itemStack).getName();
        }

        if (itemBank.containsKey(itemName)) {
            int amount = itemBank.get(itemName).getAmount();
            amount += adding;
            BankItem newItem = new BankItem(itemStack,amount);
            itemBank.put(itemName,newItem);
        } else {
            itemBank.put(itemName,new BankItem(itemStack,adding));
        }
    }

    public void deposit(int amount) {
        bank += amount;
    }

    public boolean withdraw(int amount) {
        if (bank < amount) {
            return false;
        } else {
            bank -= amount;
            return true;
        }
    }

    public int getBankMoney() {
        return bank;
    }

    public void setBankMoney(int amount) {
        bank = amount;
    }

    public void sendAnnouncement(String msg, Object... args) {
        for (Citizen citizen : onlineCitizens) {
            Messaging.send(citizen.getPlayer(),msg,args);
        }
    }

    public void sendChatMessage(Citizen sender, String msg, String... args) {
        for (Citizen citizen : onlineCitizens) {
            Messaging.send(citizen.getPlayer(),msg,args);
        }
    }

}
