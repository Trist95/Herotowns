package com.herocraftonline.townships.groups.kingdom;

import com.herocraftonline.townships.api.CitizenGroup;

/**
 * Author: gabizou
 */
public class Kingdom extends CitizenGroup {

    public Kingdom(String name, Integer bank) {
        super(name, bank);
    }

    public String getGroupType() {
        return "kingdom";
    }
}
