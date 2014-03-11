package com.herocraftonline.townships.groups.town.event;

import com.herocraftonline.townships.groups.town.Town;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Author: gabizou
 */
public final class PlayerJoinTownEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final String citizen;
    private final Town town;

    public PlayerJoinTownEvent(String citizen, Town town) {
        this.citizen = citizen;
        this.town = town;
    }

    public Town getTown() {
        return town;
    }

    public String getPlayer() {
        return citizen;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
