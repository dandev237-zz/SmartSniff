package xyz.smartsniff;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.LinkedList;

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
    private LinkedList<Device> locatedDevices;

    public Location(Date date, LatLng coordinates){
        this.date = Utils.formatDate(date);
        this.coordinates = coordinates;
        locatedDevices = new LinkedList<>();
    }

    /**
     * Constructor used when reloading the heatmap after the user starts the app.
     * @param coordinates
     */
    public Location(LatLng coordinates){
        this.coordinates = coordinates;
    }

    public void addFoundDevice(Device device){
        locatedDevices.add(device);
    }

    public String getDate() {
        return date;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public String getCoordinatesString(){
        return coordinates.latitude + ", " + coordinates.longitude;
    }

    public LinkedList<Device> getLocatedDevices() {
        return locatedDevices;
    }

    public int getNumOfLocatedDevices() {
        return locatedDevices.size();
    }
}
