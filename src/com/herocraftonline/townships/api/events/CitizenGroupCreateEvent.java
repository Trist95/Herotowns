package com.herocraftonline.townships.api.events;

import com.herocraftonline.townships.api.CitizenGroup;
import com.herocraftonline.townships.api.CitizenGroupManager;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Date;

/**
 * Author: gabizou
 */
public class CitizenGroupCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;

    private CitizenGroup group;

    public CitizenGroupCreateEvent(CitizenGroup group) {
        this.group = group;
    }

    public CitizenGroup getCitizenGroup() {
        return group;
    }

    public Date getCreationDate() {
        return new Date(group.getCreationDate());
    }

    public String getName() {
        return group.getName();
    }

    public CitizenGroupManager getManager() {
        return CitizenGroupManager.getInstance();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
