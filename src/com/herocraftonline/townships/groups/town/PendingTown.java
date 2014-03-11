package com.herocraftonline.townships.groups.town;

import com.herocraftonline.townships.api.PendingGroup;

import java.util.Collection;

/**
 * Author: gabizou
 */
public final class PendingTown extends PendingGroup {

    public PendingTown(String name) {
        super(name);
    }

    public PendingTown(String name, Collection<? extends String> c) {
        super(name,c);
    }

    public PendingTown(String name, Collection<? extends String> c, long time) {
        super(name, c, time);
    }

    public PendingTown(String name, long time) {
        super(name, time);
    }

    public String getGroupType() {
        return "town";
    }
}
