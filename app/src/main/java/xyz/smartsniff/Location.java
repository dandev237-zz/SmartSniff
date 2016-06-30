package xyz.smartsniff;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Model class to represent locations.
 *
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 30/06/2016
 */
public class Location {

    private String date;
    private LatLng coordinates;

    public Location(Date date, LatLng coordinates){
        this.date = Utils.formatDate(date);
        this.coordinates = coordinates;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }
}
