package com.herocraftonline.townships.groups.town.event;

import com.herocraftonline.townships.api.CitizenGroupCenter;
import com.herocraftonline.townships.api.events.CitizenGroupCreateEvent;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import org.bukkit.event.HandlerList;

/**
 * Author: gabizou
 */
public final class TownCreateEvent extends CitizenGroupCreateEvent {

    private static final HandlerList handlers = new HandlerList();

    private Town town;

    public TownCreateEvent(Town town) {
        super(town);
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
     * An easy check whether the Town has a region, if false, the Town does NOT have a Region nor a CitizenGroupCenter.
     * This should be checked before getCenter() or getRadius() is called.
     * @return whether this Town has a registered Region.
     */
    public boolean hasRegions() {
        return town.hasRegions();
    }

    /**
     * Returns the Town's configured Radius for their current GroupType. This does NOT check whether the Town
     * has an existing Region or CitizenGroupCenter.
     * @return the configured Radius for this Town's region if it has one.
     */
    public int getRadius() {
        return TownManager.getInstance().getConfig().getGroupTypeRadius(town.getType());
    }

    /**
     * A simple request to get the TownManager's instance for easy access.
     * @return the instance of the TownManager.
     */
    public TownManager getManager() {
        return TownManager.getInstance();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
