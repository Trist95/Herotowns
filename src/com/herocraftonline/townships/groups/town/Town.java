package com.herocraftonline.townships.groups.town;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.*;
import com.herocraftonline.townships.groups.town.event.PlayerJoinTownEvent;
import com.herocraftonline.townships.groups.town.event.PlayerLeaveTownEvent;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.Bukkit;

import java.util.*;

/**
 * Town - A {@link com.herocraftonline.townships.api.Regionable} that is standardized as a Town, contains a Bank,
 * activeCitizenList, taxes, a WorldGuardRegion and more.
 */
public final class Town extends CitizenGroup implements Taxable, Mayoral, Regionable {

    // Upgradable fields
    private GroupType type;

    // Mayoral fields
    private String mayor;
    private String successor;

    // Taxable fields
    private int tax = 0;
    private int missedPayments = 0;
    private long lastTax;
    private long lastTaxWarning;
    private long lastCitizenWarning;

    // Regionable fields
    private CitizenGroupCenter center;
    private boolean hasRegion;
    private Region region;
    // Transient Region fields (Dictated by GroupRegionManager)
    private transient Set<ChildRegion> childRegions = new HashSet<>();
    private transient String claimedWorld;


    public Town(String name, Integer bank) {
        super(name, bank);
    }

    @Override
    public String getGroupType() {
        return "town";
    }

    /**
     * Adds a Citizen to this town. Will use internal addMember(String,Rank)
     */
    @Override
    public boolean addMember(Citizen citizen, Rank rank) {
        if (addMember(citizen.getName(), rank)){
            citizen.setTown(this);
            return true;
        }
        return false;
    }

    /**
     * Adds a Citizen from this town. If Townships is enabled and done loading all towns,
     * this will also call a PlayerJoinTownEvent for the Citizen joining.
     * @param citizen - Citizen being added to the Town
     * @param rank - Rank that this citizen will be added to
     * @return - Whether this addition succeded.
     */
    @Override
    public boolean addMember(String citizen, Rank rank) {
        if (super.addMember(citizen,rank)) {
            if (Townships.isDoneLoading()) {
                Bukkit.getPluginManager().callEvent(new PlayerJoinTownEvent(citizen, this));
                return true;
            } else
                return true;
        }
        return false;
    }

    /**
     * Removes a Citizen from this Town. If Townships is enabled and done loading all towns,
     * this will attempt to call a PlayerLeaveTownEvent for the Citizen being removed.
     * @param citizen - Name of the citizen being removed from the Town
     * @return - True if the removal was a success
     */
    @Override
    public boolean removeMember(String citizen) {
        if (super.removeMember(citizen)) {
            if (mayor != null && mayor.equalsIgnoreCase(citizen)) {
                if (getSuccessor() == null) {
                    if (getManagers().isEmpty()) {
                        promoteLastCitizen();
                    }
                    successor = promoteManager();
                    sendAnnouncement(Messaging.getMessage("town_auto_promote_successor"), successor);
                }
                mayor = getSuccessor();
                successor = null;
                setNewCitizenRank(mayor, Rank.OWNER);
                sendAnnouncement(Messaging.getMessage("town_auto_promote_mayor"),mayor,getName());
                sendAnnouncement(Messaging.getMessage("town_auto_request_successor"));
            }
            if (Townships.isDoneLoading()) {
                Bukkit.getPluginManager().callEvent(new PlayerLeaveTownEvent(citizen,this));
                return true;
            } else
                return true;
        }
        return false;
    }

    @Override
    public boolean setNewCitizenRank(String string, Rank rank) {
        // Ensure that we're saving this Town when setting ranks for any reason
        if (super.setNewCitizenRank(string, rank)) {
            if (Townships.isDoneLoading())
                Townships.getInstance().getStorageManager().getStorage().saveCitizenGroup(this, true);
            return true;
        }
        return false;

    }

    @Override
    public boolean setNewCitizenRank(Citizen citizen, Rank rank) {
        // Ensure we're saving this Town and the Citizen whenever setting the new rank
        boolean state = false;
        if (super.setNewCitizenRank(citizen, rank)) {
            if (getCitizenRank(citizen).ordinal() >= Rank.CITIZEN.ordinal()) {
                citizen.setTown(this);
                Townships.getInstance().getStorageManager().getStorage().saveCitizen(citizen, true);
            }
            state = true;
            Townships.getInstance().getStorageManager().getStorage().saveCitizen(citizen, true);
        }
        return state;
    }

    // -------
    // Upgradable Methods
    // -------

    @Override
    public void setType(GroupType type) {
        this.type = type;
    }


    @Override
    public GroupType getType() {
        return type;
    }

    // -------
    // Tax Methods
    // -------

    @Override
    public boolean collectTax() {
        int taxes = TownManager.getInstance().getTownConfig().getGroupTypeTax(this.getType());
        if (bank < taxes)
            return false;
        else {
            bank -= taxes;
            return true;
        }
    }

    @Override
    public int getTax() {
        return tax;
    }

    @Override
    public void setTax(int tax) {
        this.tax = tax;
    }

    @Override
    public long getLastTax() {
        return lastTax;
    }

    @Override
    public void setLastTax(long lastTax) {
        this.lastTax = lastTax;
    }

    @Override
    public long getLastTaxWarning() {
        return lastTaxWarning;
    }

    @Override
    public void setLastTaxWarning(long time) {
        lastTaxWarning = time;
    }

    @Override
    public int getMissedPayments() {
        return missedPayments;
    }

    @Override
    public void setMissedPayments(int missedPayments) {
        this.missedPayments = missedPayments;
    }


    // --------
    // Mayoral Handling
    // --------
    @Override
    public String getMayor() {
        return mayor;
    }

    @Override
    public void setMayor(String name) {
        mayor = name;
    }

    @Override
    public String getSuccessor() {
        return successor;
    }

    @Override
    public void setSuccessor(String name) {
        successor = name;
    }

    @Override
    public String promoteManager() {
        for (String name : citizens.keySet()) {
            if (getCitizenRank(name) == Rank.MANAGER) {
                setSuccessor(name);
                setNewCitizenRank(name, Rank.SUCCESSOR);
                return name;
            }
        }
        return null;
    }

    @Override
    public Set<String> getManagers() {
        LinkedHashSet<String> managers = new LinkedHashSet<>();
        for (Map.Entry<String, Rank> citizen : getCitizens().entrySet()) {
            if (citizen.getValue() == Rank.MANAGER) {
                managers.add(citizen.getKey());
            }
        }
        return managers;
    }

    @Override
    public void promoteLastCitizen() {
        if (getCitizens().isEmpty())
            return;
        String lastCitizen = "";
        for (Map.Entry<String,Rank> citizen : getCitizens().entrySet()) {
            if (lastCitizen.isEmpty())
                lastCitizen = citizen.getKey();
            else if (getCitizenLastLogin(lastCitizen) < getCitizenLastLogin(citizen.getKey()))
                lastCitizen = citizen.getKey();
        }
        if (lastCitizen.isEmpty())
            return;
        else {
            setNewCitizenRank(lastCitizen,Rank.MANAGER);
        }
    }

    @Override
    public long getLastCitizenWarning() {
        return lastCitizenWarning;
    }

    @Override
    public void setLastCitizenWarning(long time) {
        lastCitizenWarning = time;
    }

    // --------
    // Region Methods
    // --------
    public String getClaimedWorld() {
        return claimedWorld;
    }

    public void setClaimedWorld(String worldName) {
        this.claimedWorld = worldName;
    }

    public Region getRegion() {
        return region;
    }


    public void setRegion(Region region) {
        this.region = region;
        setHasRegion(true);
    }

    public Set<ChildRegion> getChildRegions() {
        return Collections.unmodifiableSet(childRegions);
    }

    public void addChildRegion(ChildRegion region) {
        childRegions.add(region);
    }

    public void addChildRegions(List<ChildRegion> regions) {
        childRegions.addAll(regions);
    }

    public void removeChildRegion(ChildRegion region) {
        childRegions.remove(region);
    }

    public void clearChildRegions() {
        childRegions.clear();
    }

    public int amountOfChildRegions() {
        return childRegions.size();
    }

    public CitizenGroupCenter getCenter() {
        return center;
    }

    public void setCenter(CitizenGroupCenter center) {
        this.center = center;
    }

    public boolean hasRegions() {
        return hasRegion;
    }

    public void setHasRegion(boolean hasRegion) {
        this.hasRegion = hasRegion;
    }

    // --------
    // Utility Methods
    // --------

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Town) && ( obj == this || ((Town) obj).getName().equals(this.getName()));
    }
}
