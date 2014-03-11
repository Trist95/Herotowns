package com.herocraftonline.townships.api;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Set;

/**
 * Author: gabizou
 */
public class ChildRegion extends Region {

    private final CitizenGroup parent;

    public ChildRegion(CitizenGroup parent, Region existingRegion) {
        super(existingRegion.region, existingRegion.getOwners());
        this.parent = parent;
        addGuests(existingRegion.getGuests());
        addManagers(existingRegion.getManagers());
    }

    public ChildRegion(CitizenGroup parent, ProtectedRegion region) {
        super(region);
        this.parent = parent;
    }

    public ChildRegion(CitizenGroup parent, ProtectedRegion region, String owner) {
        super(region, owner);
        this.parent = parent;
    }

    public ChildRegion(CitizenGroup parent, ProtectedRegion region, Set<String> owners) {
        super(region, owners);
        this.parent = parent;
    }

    public CitizenGroup getParent() {
        return parent;
    }

}
