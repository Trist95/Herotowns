package com.herocraftonline.townships.api;

/**
 * Author: gabizou
 */
public interface Taxable {

    /**
     * Attempts to collect taxes for this specific Town. Will return true if taxes have been collected
     * and the remaining amount is not negative, likewise, returns false if the bank has insufficient funds.
     * Does NOT update lastMissedTaxDate.
     * @return
     */
    public boolean collectTax();

    public int getTax();

    public void setTax(int tax);

    public long getLastTax();

    public void setLastTax(long lastTax);

    public long getLastTaxWarning();

    public void setLastTaxWarning(long time);

    public int getMissedPayments();

    public void setMissedPayments(int missedPayments);

}
