package com.herocraftonline.townships.api;

import java.util.EnumMap;
import java.util.Map;

/**
 * Author: gabizou
 */
public enum Rank {

    NONE,
    ENEMY,
    GUEST,
    CITIZEN,
    MANAGER,
    SUCCESSOR,
    OWNER;

    public static Map<Rank, String> getDefaultNames() {
        Map<Rank, String> ranks = new EnumMap<>(Rank.class);
        for (Rank rank : Rank.values()) {
            ranks.put(rank, rank.name().toLowerCase());
        }
        return ranks;
    }

    /**
     * Checks if the rank is a rank for non-members
     * @return
     */
    public boolean isOutsider() {
        switch (this) {
            case NONE :
            case ENEMY :
            case GUEST :
                return true;
            default :
                return false;
        }
    }

    public boolean isEnemy() {
        switch (this) {
            case ENEMY :
                return true;
            default :
                return false;
        }
    }

    /**
     * Returns if the rank is allowed to manage a town
     *
     * @return
     */
    public boolean canManage() {
        return this == MANAGER || this == OWNER || this == SUCCESSOR;
    }
}
