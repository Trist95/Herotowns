package com.herocraftonline.townships.api.config;

import com.herocraftonline.townships.api.GroupType;
import com.herocraftonline.townships.util.BankItem;

import java.util.Set;

/**
 * Author: gabizou
 */
public interface UpgradableConfig {

    public String getGroupTypeName(GroupType type);

    public int getGroupTypeCost(GroupType type);

    public Set<BankItem> getGroupTypeResourceCost(GroupType type);

}
