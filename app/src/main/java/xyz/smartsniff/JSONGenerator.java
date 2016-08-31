package xyz.smartsniff;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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
    private Activity mainActivity;

    public JSONGenerator(Activity context){
        this.mainActivity = context;
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    public void sendJSONToServer(){
        jsonObject = new JSONObject();
        jsonObject = prepareJsonObject();

        //Send the JSON object to the server using the RESTful API
        String url = "http://192.168.1.199:5000/api/db/storedata";
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(mainActivity, "Datos enviados", Toast.LENGTH_SHORT)
                                .show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mainActivity, "ERROR: No se pudieron enviar los datos", Toast.LENGTH_SHORT)
                                .show();
                    }
                }) {

            //Workaround for dealing with empty response
            //Reference: http://stackoverflow.com/a/24566878
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response){
                try{
                    if(response.data.length == 0){
                        if (response.data.length == 0) {
                            byte[] responseData = "{}".getBytes("UTF8");
                            response = new NetworkResponse(response.statusCode, responseData, response.headers, response.notModified);
                        }
                    }
                }catch(UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                return super.parseNetworkResponse(response);
            }

        };
        if(Utils.queue == null)
            Utils.queue = Volley.newRequestQueue(mainActivity);

        Utils.queue.add(postRequest);
    }

    private JSONObject prepareJsonObject() {
        //Collect all the associations data
        List<Association> associations = databaseHelper.getAllAssociations();

        try {
            //Build a JSON object containing all the data
            buildJSON(associations);
        } catch (JSONException e) {
            Log.e("JSONGenerator", "ERROR: " + e.getCause());
            e.printStackTrace();
        }

        return jsonObject;
    }

    private void buildJSON(List<Association> associations) throws JSONException {
        /*
        JSON Structure:
        - 1 JSON Object (jsonObject), containing
            - 4 JSON Arrays, one for each model class (Session, Device, Location, Association)
         */

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
