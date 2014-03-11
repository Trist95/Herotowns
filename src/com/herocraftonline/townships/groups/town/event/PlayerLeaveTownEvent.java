package com.herocraftonline.townships.groups.town.event;

import com.herocraftonline.townships.groups.town.Town;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Author: gabizou
 */
public final class PlayerLeaveTownEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private final String player;
    private final Town town;

    public PlayerLeaveTownEvent(String player, Town town) {
        this.player = player;
        this.town = town;
    }

    public Town getTown() {
        return town;
    }

    public String getPlayer() {
        return player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
