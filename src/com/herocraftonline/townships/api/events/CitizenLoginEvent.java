package com.herocraftonline.townships.api.events;

import com.herocraftonline.townships.api.Citizen;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Author: gabizou
 */
public class CitizenLoginEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Citizen citizen;
    private final String name;

    public CitizenLoginEvent(String name, Citizen citizen) {
        this.citizen = citizen;
        this.name = name;
    }

    public Citizen getCitizen() {
        return citizen;
    }

    public String getName() {
        return name;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
