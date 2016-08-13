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

    public int getSessionId() {
        return sessionId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public int getLocationId() {
        return locationId;
    }
}
