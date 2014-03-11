package com.herocraftonline.townships.api;

/**
 * Author: gabizou
 */
public interface Upgradable {

    /**
     * Returns the TownType of this town
     * @return TownType - Type of this Town
     */
    public GroupType getType();

    /**
     * Sets the town's type
     * @param type - Set's this Town's type
     */
    public void setType(GroupType type);

}
