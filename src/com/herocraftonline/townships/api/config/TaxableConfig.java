package com.herocraftonline.townships.api.config;

import com.herocraftonline.townships.api.GroupType;

/**
 * Author: gabizou
 */
public interface TaxableConfig {

    public int getGroupTypeTax(GroupType type);

    public int getGroupTypeCitizenTax(GroupType type);

    public int getGroupTypeCitizenJoinCost(GroupType type);

}
