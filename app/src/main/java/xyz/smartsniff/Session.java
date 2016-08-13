package xyz.smartsniff;

import java.util.Date;

/**
 * Model class to represent sessions.
 *
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 30/06/2016
 */
public class Session {


    private String startDate, endDate;

    public Session(Date startDate){
        this(startDate, null);
    }

    public Session(Date startDate, Date endDate){
        this.startDate = Utils.formatDate(startDate);

        if(endDate != null)
            this.endDate = Utils.formatDate(endDate);
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate){
        this.endDate = Utils.formatDate(endDate);
    }

    @Override
    public String toString() {
        return "Start date: " + startDate + ". End date: " + endDate + ".";
    }

}
