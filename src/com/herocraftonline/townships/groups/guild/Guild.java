package com.herocraftonline.townships.groups.guild;

import com.herocraftonline.townships.api.CitizenGroup;

/**
 * Author: gabizou
 */
public class Guild extends CitizenGroup {

    public Guild(String name, Integer bank) {
        super(name, bank);
    }

    public String getGroupType() {
        return "guild";
    }
}
