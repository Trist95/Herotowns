package com.herocraftonline.townships.groups.kingdom;

import com.herocraftonline.townships.api.PendingGroup;

import java.util.Collection;

/**
 * Author: gabizou
 */
public class PendingKingdom extends PendingGroup {

    public PendingKingdom(String name) {
        super(name);
    }

    public PendingKingdom(String name, Collection<? extends String> c) {
        super(name,c);
    }

    public PendingKingdom(String name, Collection<? extends String> c, long time) {
        super(name, c, time);
    }

    public PendingKingdom(String name, long time) {
        super(name, time);
    }

    public String getGroupType() {
        return "guild";
    }
}
