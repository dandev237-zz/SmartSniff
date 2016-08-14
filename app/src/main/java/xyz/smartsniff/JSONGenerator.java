package xyz.smartsniff;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Class responsible of generating and assembling a JSON object containing all the stored data in the local device
 * database.
 *
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 14/08/2016
 */
public class JSONGenerator {

    private JSONObject jsonObject;
    private String deviceHardwareAddress;
    private Gson gson;

    public JSONGenerator(String macAddress){
        jsonObject = new JSONObject();
        deviceHardwareAddress = macAddress;
        gson = new GsonBuilder().setDateFormat(Utils.DATE_FORMAT).disableHtmlEscaping().create();
    }

    public JSONObject buildJsonObject(List<Session> sessions, List<Device> devices, List<Location> locations,
                                      List<Association> associations) {
        /*
        JSON Structure:
        - 1 JSON Object (jsonObject), containing
            - 4 JSON Arrays, one for each model class (Session, Device, Location, Association)
         */
        try {
            addSessionsToJSON(sessions);
            addDevicesToJSON(devices);
            addLocationsToJSON(locations);
            addAssociationsToJSON(associations);
        } catch (JSONException e) {
            Log.e("JSONGenerator", "ERROR: " + e.getCause());
            e.printStackTrace();
        }


        return jsonObject;
    }

    private void addSessionsToJSON(List<Session> sessions) throws JSONException {
        JSONArray sessionsArray = new JSONArray();
        for(Session s: sessions){
            s.setMacAddress(deviceHardwareAddress);

            String sessionJson = gson.toJson(s);
            JSONObject sessionObject = new JSONObject(sessionJson);

            sessionsArray.put(sessionObject);
        }

        jsonObject.put("sessions", sessionsArray);
    }

    private void addDevicesToJSON(List<Device> devices) throws JSONException {
        JSONArray devicesArray = new JSONArray();
        for(Device d: devices){
            String deviceJson = gson.toJson(d);
            JSONObject deviceObject = new JSONObject(deviceJson);

            devicesArray.put(deviceObject);
        }

        jsonObject.put("devices", devicesArray);
    }

    private void addLocationsToJSON(List<Location> locations) throws JSONException {
        JSONArray locationsArray = new JSONArray();
        for(Location l: locations){
            String locationJson = gson.toJson(l);
            JSONObject locationObject = new JSONObject(locationJson);

            locationsArray.put(locationObject);
        }

        jsonObject.put("locations", locationsArray);
    }

    private void addAssociationsToJSON(List<Association> associations) throws JSONException {
        JSONArray associationsArray = new JSONArray();
        for(Association a: associations){
            String associationJson = gson.toJson(a);
            JSONObject associationObject = new JSONObject(associationJson);

            associationsArray.put(associationObject);
        }

        jsonObject.put("asocsessiondevices", associationsArray);
    }
}
