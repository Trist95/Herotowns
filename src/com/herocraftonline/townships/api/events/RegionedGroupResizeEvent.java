package com.herocraftonline.townships.api.events;

import com.herocraftonline.townships.api.CitizenGroup;
import com.herocraftonline.townships.api.CitizenGroupCenter;
import com.herocraftonline.townships.api.Region;
import com.herocraftonline.townships.api.Regionable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Author: gabizou
 */
public class RegionedGroupResizeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    protected final CitizenGroup town;
    protected final Region region;

    public RegionedGroupResizeEvent(CitizenGroup town, Region region) {
        this.town = town;
        this.region = region;
    }

    public CitizenGroup getGroup() {
        return town;
    }

    public Region getRegion() {
        return region;
    }

    public CitizenGroupCenter getCenter() {
        return ((Regionable) town).getCenter();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
