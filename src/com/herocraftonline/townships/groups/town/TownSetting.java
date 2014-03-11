package com.herocraftonline.townships.groups.town;

import com.herocraftonline.townships.util.BankItem;

import java.util.Collections;
import java.util.Set;

public final class TownSetting {

    private final String name;
    private final int minCitizens;
    private final int maxCitizens;
    private final int cost;
    private final int tax;
    private final int radius;
    private final int points;
    private final int maxChildRegions;
    private final boolean regions;
    private final boolean subregions;
    private final boolean channel;
    private final Set<BankItem> materialCost;

    public TownSetting(String name,int minCitizens, int maxCitizens, int cost, int tax, boolean regions, int radius,
                       int points, boolean subregions, int maxChildRegions, Set<BankItem> materials, boolean channel) {
        this.name = name;
        this.minCitizens = minCitizens;
        this.maxCitizens = maxCitizens;
        this.cost = cost;
        this.tax = tax;
        this.materialCost = materials;
        this.radius = radius;
        this.points = points;
        this.subregions = subregions;
        this.maxChildRegions = maxChildRegions;
        this.regions = regions;
        this.channel = channel;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the minimum required Citizens
     */
    public int getMinCitizens() {
        return minCitizens;
    }

    /**
     * @return the maxCitizens
     */
    public int getMaxCitizens() {
        return maxCitizens;
    }

    public int getMaxChildRegions() {
        return maxChildRegions;
    }

    /**
     * @return the cost for claiming this type of town
     */
    public int getCost() {
        return cost;
    }

    public int getRadius() {
        return radius;
    }

    /**
     * @return the tax
     */
    public int getTax() {
        return tax;
    }

    /**
     * @return the cost in items of creating or upgrading this town type.
     */
    public Set<BankItem> getMaterialCost() {
        return Collections.unmodifiableSet(materialCost);
    }

    public int getPoints() {
        return points;
    }

    public boolean getRegionEnabled() {
        return regions;
    }

    public boolean getSubRegionsEnabled() {
        return subregions;
    }

    public boolean getChannelEnabled() {
        return channel;
    }

}
