package com.herocraftonline.townships.storage.metas;

import com.herocraftonline.herostorage.api.BaseMeta;
import com.herocraftonline.townships.api.GroupType;

/**
 * Author: gabizou
 */
public class TownMeta extends BaseMeta {

    private String townName,townNick;
    private String mayor,successor;
    private int money,joinCost,taxCost,missedPayments;
    private long dateCreated;
    private GroupType size;

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public String getTownNick() {
        return townNick;
    }

    public void setTownNick(String townNick) {
        this.townNick = townNick;
    }

    public String getMayor() {
        return mayor;
    }

    public void setMayor(String mayor) {
        this.mayor = mayor;
    }

    public String getSuccessor() {
        return successor;
    }

    public void setSuccessor(String successor) {
        this.successor = successor;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getJoinCost() {
        return joinCost;
    }

    public void setJoinCost(int joinCost) {
        this.joinCost = joinCost;
    }

    public int getTaxCost() {
        return taxCost;
    }

    public void setTaxCost(int taxCost) {
        this.taxCost = taxCost;
    }

    public GroupType getSize() {
        return size;
    }

    public void setSize(GroupType size) {
        this.size = size;
    }

    public int getMissedPayments() {
        return missedPayments;
    }

    public void setMissedPayments(int missedPayments) {
        this.missedPayments = missedPayments;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

}
