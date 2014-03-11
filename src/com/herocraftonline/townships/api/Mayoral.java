package com.herocraftonline.townships.api;

import java.util.Set;

/**
 * Author: gabizou
 */
public interface Mayoral {

    public long getLastCitizenWarning();

    public void setLastCitizenWarning(long time);

    public String getMayor();

    public void setMayor(String mayor);

    public String getSuccessor();

    public void setSuccessor(String successor);

    public Set<String> getManagers();

    public String promoteManager();

    public void promoteLastCitizen();
}
