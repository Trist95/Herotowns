package com.herocraftonline.townships.groups.guild;

import com.herocraftonline.townships.api.PendingGroup;

import java.util.Collection;

/**
 * Author: gabizou
 */
public class PendingGuild extends PendingGroup {

    public PendingGuild(String name) {
        super(name);
    }

    public PendingGuild(String name, Collection<? extends String> c) {
        super(name,c);
    }

    public PendingGuild(String name, Collection<? extends String> c, long time) {
        super(name, c, time);
    }

    public PendingGuild(String name, long time) {
        super(name, time);
    }

    public String getGroupType() {
        return "guild";
    }
}
