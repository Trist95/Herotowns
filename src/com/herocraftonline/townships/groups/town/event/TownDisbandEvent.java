package com.herocraftonline.townships.groups.town.event;

import com.herocraftonline.townships.api.events.CitizenGroupDisbandEvent;
import com.herocraftonline.townships.groups.town.Town;
import org.bukkit.event.HandlerList;

/**
 * Author: gabizou
 */
public final class TownDisbandEvent extends CitizenGroupDisbandEvent {

    private static final HandlerList handlers = new HandlerList();

    private Town town;

    public TownDisbandEvent(Town town) {
        super(town);
        this.town = town;
    }

    public Town getTown() {
        return town;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
