package com.herocraftonline.townships.groups.kingdom;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Region;
import com.herocraftonline.townships.groups.town.Town;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Author: gabizou
 */
public class KingdomRegion extends Region {

    private Map<Town,Region> towns = new HashMap<>();

    public KingdomRegion(ProtectedRegion region, Set<Town> towns, Set<String> owners) {
        super(region,owners);
        for(Town town : towns) {
            this.towns.put(town,town.getRegion());
        }
        this.owners = owners;
    }

    public void addTown(Town town) {
        if (town.getRegion() != null)
            towns.put(town, town.getRegion());
        Townships.log(Level.WARNING, "The Kingdom");
    }
}
