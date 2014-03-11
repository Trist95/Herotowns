package com.herocraftonline.townships.api;

import java.util.List;
import java.util.Set;

/**
 * Author: gabizou
 */
public interface Regionable extends Upgradable {

    public String getClaimedWorld();

    public void setClaimedWorld(String worldName);

    public Region getRegion();


    public void setRegion(Region region);

    public Set<ChildRegion> getChildRegions();

    public void addChildRegion(ChildRegion region);

    public void addChildRegions(List<ChildRegion> regions);

    public void removeChildRegion(ChildRegion region);

    public void clearChildRegions();

    public int amountOfChildRegions();

    public CitizenGroupCenter getCenter();

    public void setCenter(CitizenGroupCenter center);

    public boolean hasRegions();

    public void setHasRegion(boolean hasRegion);

}
