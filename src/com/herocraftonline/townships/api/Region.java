package com.herocraftonline.townships.api;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The Abstract Region wrapper for Townships, handling the basics of having an Owner, Manager and Member
 * while handling the intermediary method calls to WorldGuard's ProtectedRegion.
 *
 * Author: gabizou
 */
public class Region {

    protected ProtectedRegion region;
    protected Set<String> owners = new HashSet<>();
    protected Set<String> managers = new HashSet<>();
    protected Set<String> guests = new HashSet<>();

    public Region(ProtectedRegion region) {
        this(region,region.getOwners().getPlayers());
    }

    public Region(ProtectedRegion region, String owner) {
        this.region = region;
        addOwner(owner);
    }

    public Region(ProtectedRegion region, Set<String> owners) {
        this.region = region;
        addOwners(owners);
    }

    public ProtectedRegion getWorldGuardRegion() {
        return region;
    }

    public void addOwner(String player) {
        region.getOwners().addPlayer(player.toLowerCase());
        owners.add(player.toLowerCase());
    }

    public void addOwners(Collection<String> players) {
        for (String player : players)
            addOwner(player.toLowerCase());
    }

    public void addManager(String player) {
        if (guests.contains(player.toLowerCase()))
            guests.remove(player.toLowerCase());
        managers.add(player.toLowerCase());
        checkMembership(player);
    }

    public void addManagers(Collection<String> players) {
        for (String name : players)
            addManager(name);
    }

    public void addGuest(String player) {
        checkMembership(player);
        guests.add(player.toLowerCase());
    }

    public void addGuests(Collection<String> players) {
        for (String name : players)
            addGuest(name);
    }

    public boolean isOwner(String player) {
        return owners.contains(player.toLowerCase());
    }

    public boolean isManager(String player) {
        return managers.contains(player.toLowerCase());
    }

    public boolean isGuest(String player) {
        return guests.contains(player.toLowerCase());
    }

    public void removeMember(String player) {
        guests.remove(player);
        guests.remove(player.toLowerCase());
        managers.remove(player);
        managers.remove(player.toLowerCase());
        owners.remove(player);
        owners.remove(player.toLowerCase());
        region.getMembers().removePlayer(player);
        region.getOwners().removePlayer(player);

    }

    public String getName() {
        return region.getId();
    }

    public Set<String> getGuests() {
        return Collections.unmodifiableSet(guests);
    }

    public Set<String> getManagers() {
        return Collections.unmodifiableSet(managers);
    }

    public Set<String> getOwners() {
        return Collections.unmodifiableSet(owners);
    }

    private void checkMembership(String player) {
        if (!region.getMembers().contains(player.toLowerCase()))
            region.getMembers().addPlayer(player.toLowerCase());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Region))
            return false;

        return ((Region) obj).region.getId().equals(region.getId());
    }

    @Override
    public int hashCode() {
        return region.getId().hashCode();
    }

    @Override
    public String toString() {
        return region.getId();
    }
}
