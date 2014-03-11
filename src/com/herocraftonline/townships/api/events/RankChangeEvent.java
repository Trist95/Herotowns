package com.herocraftonline.townships.api.events;

import com.herocraftonline.townships.api.CitizenGroup;
import com.herocraftonline.townships.api.Rank;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Author: gabizou
 */
public class RankChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final String player;
    private final Rank oldRank;
    private final Rank newRank;
    private final CitizenGroup town;
    private boolean isCancelled = false;

    public RankChangeEvent(String player, CitizenGroup town, Rank oldRank, Rank newRank) {
        this.player = player;
        this.town = town;
        this.oldRank = oldRank;
        this.newRank = newRank;
    }

    /**
     * @return the player
     */
    public String getName() {
        return player;
    }

    /**
     * @return the Player object
     */
    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(player);
    }

    /**
     * @return the town
     */
    public CitizenGroup getGroup() {
        return town;
    }

    /**
     * @return the oldRank
     */
    public Rank getOldRank() {
        return oldRank;
    }

    /**
     * @return the newRank
     */
    public Rank getNewRank() {
        return newRank;
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
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }
}
