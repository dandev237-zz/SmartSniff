package xyz.smartsniff.Model;

import java.util.Date;

import xyz.smartsniff.Utils.Utils;

/**
 * Model class to represent sessions.
 * <p>
 * Author: Daniel Castro García
 * Email: dandev237@gmail.com
 * Date: 30/06/2016
 */
public class Session {

    private Date startDate, endDate;
    private String macAddress;

    public Session(Date startDate) {
        this(startDate, null);
    }

    public Session(Date startDate, Date endDate) {
        this.startDate = startDate;

        if (endDate != null)
            this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public String toString() {
        return "Start date: " + startDate + ". End date: " + endDate + ".";
    }

    public String getStartDateString() {
        return Utils.formatDate(startDate);
    }

    public String getEndDateString() {
        return Utils.formatDate(endDate);
    }
}
