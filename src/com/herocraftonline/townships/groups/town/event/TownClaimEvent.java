package com.herocraftonline.townships.groups.town.event;

import com.herocraftonline.townships.api.CitizenGroupCenter;
import com.herocraftonline.townships.api.Region;
import com.herocraftonline.townships.api.events.RegionedGroupClaimEvent;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import org.bukkit.event.HandlerList;

/**
 * Author: gabizou
 */
public final class TownClaimEvent extends RegionedGroupClaimEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Town town;

    public TownClaimEvent(Town town, Region region) {
        super(town, region);
        this.town = town;
    }

    /**
     * Returns a Town specific object for this town, use this instead of getGroup() for CitizenGroupCreateEvent
     *
     * @return the town
     */
    public Town getTown() {
        return town;
    }

    /**
     * Returns the Town's center. This may return null if the Town does NOT have a region claim or
     * whether the Town has yet to reclaim an old WorldGuard Region prior to importing said region to Townships.
     * @return the Town's current center if not null
     */
    public CitizenGroupCenter getCenter() {
        return town.getCenter();
    }

    /**
     * Returns the {@link com.herocraftonline.townships.groups.town.Town}'s configured Radius for their current
     * {@link com.herocraftonline.townships.api.GroupType}. This does NOT check whether the Town has an existing
     * {@link com.herocraftonline.townships.api.Region} or {@link com.herocraftonline.townships.api.CitizenGroupCenter}
     * @return the configured Radius for this Town's region if it has one.
     */
    public int getRadius() {
        return TownManager.getInstance().getConfig().getGroupTypeRadius(town.getType());
    }

    /**
     * Returns the newly created {@link com.herocraftonline.townships.api.Region} for this Town. With this, we can
     * perform various interactinos such as adding guests, managers, owners or retrieving the membership groups or
     * obtaining the WorldGuard ProtectedRegion.
     * @return the Region of this Town.
     */
    public Region getRegion() {
        return region;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
