package com.herocraftonline.townships.api.config;

import com.herocraftonline.townships.api.GroupType;

/**
 * Author: gabizou
 *
 * MayoralConfig is a generic config that should have a Minimum and Maximum Citizen requirement for a specified
 * CitizenGroup.
 */
public interface MayoralConfig {

    public int getGroupTypeMinResidents(GroupType type);

    public int getGroupTypeMaxResidents(GroupType tType);

}
