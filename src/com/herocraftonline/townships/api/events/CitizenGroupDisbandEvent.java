package com.herocraftonline.townships.api.events;

import com.herocraftonline.townships.api.CitizenGroup;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Date;

/**
 * Author: gabizou
 */
public class CitizenGroupDisbandEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;

    private CitizenGroup group;

    public CitizenGroupDisbandEvent(CitizenGroup group) {
        this.group = group;
    }

    public CitizenGroup getCitizenGroup() {
        return group;
    }

    public Date getCreationDate() {
        return new Date(group.getCreationDate());
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

}
