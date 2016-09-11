package xyz.smartsniff;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.LinkedList;

/**
 * Model class to represent locations.
 *
 * Author: Daniel Castro García
 * Email: dandev237@gmail.com
 * Date: 30/06/2016
 */
public class Location {

    private Date date;
    private LatLng coordinates;
    private transient LinkedList<Device> locatedDevices;

    public Location(Date date, LatLng coordinates){
        if(date != null)
            this.date = date;

        this.coordinates = coordinates;
        locatedDevices = new LinkedList<>();
    }

    /**
     * Overloaded constructor used when reloading the heatmap.
     * @param coordinates
     */
    public Location(LatLng coordinates){
        this(null, coordinates);
    }

    public void addFoundDevice(Device device){
        locatedDevices.add(device);
    }

    public String getDateString() {
        return Utils.formatDate(date);
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

    /**
     * A location is valid if its coordinates are not (0.0, 0.0)
     * This is a fail-safe function to ensure we are only registering locations received via
     * geolocation services.
     * @return
     */
    public boolean isValidLocation(){
        return coordinates.latitude != 0.0 && coordinates.longitude != 0.0;
    }

    //Getters and setters

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }
}
