package xyz.smartsniff;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for generating and assembling a JSON object containing all the stored data in the local device
 * database.
 *
 * Author: Daniel Castro García
 * Email: dandev237@gmail.com
 * Date: 14/08/2016
 */
public class JSONGenerator {

    private JSONObject jsonObject;
    private DatabaseHelper databaseHelper;

    public JSONGenerator(Context context){
        jsonObject = new JSONObject();
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    public JSONObject buildJsonObject(List<Association> associations) {
        /*
        JSON Structure:
        - 1 JSON Object (jsonObject), containing
            - 4 JSON Arrays, one for each model class (Session, Device, Location, Association)
         */
        try {
            buildJSON(associations);
        } catch (JSONException e) {
            Log.e("JSONGenerator", "ERROR: " + e.getCause());
            e.printStackTrace();
        }

        /*try {
            System.out.println(jsonObject.toString(3));
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        return jsonObject;
    }

    private void buildJSON(List<Association> associations) throws JSONException {
        Map<Integer, Session> consideredSessions = new HashMap<>();
        Map<Integer, Device> consideredDevices = new HashMap<>();
        Map<Integer, Location> consideredLocations = new HashMap<>();

        JSONArray associationsArray = new JSONArray();
        for(Association a: associations){
            Session s;
            if(consideredSessions.containsKey(a.sessionId))
                s = consideredSessions.get(a.sessionId);
            else {
                s = databaseHelper.getSession(a.sessionId);
                //Add the device Mac Address to the Session object
                s.setMacAddress(Utils.getMacAddr());
                consideredSessions.put(a.sessionId, s);
            }

            Device d;
            if(consideredDevices.containsKey(a.deviceId))
                d = consideredDevices.get(a.deviceId);
            else {
                d = databaseHelper.getDevice(a.deviceId);
                consideredDevices.put(a.deviceId, d);
            }

            Location l;
            if(consideredLocations.containsKey(a.locationId))
                l = consideredLocations.get(a.locationId);
            else {
                l = databaseHelper.getLocation(a.locationId);
                consideredLocations.put(a.locationId, l);
            }

            JSONObject associationObject = new JSONObject();

            JSONObject sessionJson = new JSONObject(Utils.gson.toJson(s));
            JSONObject deviceJson = new JSONObject(Utils.gson.toJson(d));

            associationObject.put("session", sessionJson);
            associationObject.put("device", deviceJson);

            JSONObject locationCoordinates = new JSONObject();
            locationCoordinates.put("latitude", l.getCoordinates().latitude);
            locationCoordinates.put("longitude", l.getCoordinates().longitude);
            associationObject.put("location", locationCoordinates);

            associationsArray.put(associationObject);
        }

        JSONArray sessionsArray = new JSONArray();
        for(Session s: consideredSessions.values()){
            JSONObject sessionObject = new JSONObject(Utils.gson.toJson(s));
            sessionsArray.put(sessionObject);
        }

        JSONArray devicesArray = new JSONArray();
        for(Device d: consideredDevices.values()){
            JSONObject deviceObject = new JSONObject(Utils.gson.toJson(d));
            devicesArray.put(deviceObject);
        }

        JSONArray locationsArray = new JSONArray();
        for(Location l: consideredLocations.values()){
            JSONObject locationObject = new JSONObject(Utils.gson.toJson(l));
            locationsArray.put(locationObject);
        }

        jsonObject.put("sessions", sessionsArray);
        jsonObject.put("devices", devicesArray);
        jsonObject.put("locations", locationsArray);
        jsonObject.put("asocsessiondevices", associationsArray);
    }
}
