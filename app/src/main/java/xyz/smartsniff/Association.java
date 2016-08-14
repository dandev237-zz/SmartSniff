package xyz.smartsniff;

/**
 * Model class to represent a stored association between a session, a location, and a device in the local database.
 *
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 14/08/2016
 */
public class Association {

    int sessionId, deviceId, locationId;

    public Association(int sessionId, int deviceId, int locationId){
        this.sessionId = sessionId;
        this.deviceId = deviceId;
        this.locationId = locationId;
    }

    //Getters and setters


    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
}
