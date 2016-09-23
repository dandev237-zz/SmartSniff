package xyz.smartsniff;

import android.app.Activity;
import android.net.wifi.ScanResult;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.Serializable;

/**
 * Model class to represent devices.
 *
 * Author: Daniel Castro García
 * Email: dandev237@gmail.com
 * Date: 30/06/2016
 */
public class Device implements Serializable {
    private String ssid, bssid, characteristics, manufacturer, channelWidth;
    private int frequency, signalIntensity;
    private DeviceType type;

    public Device(String ssid, String bssid, String characteristics, int channelWidthConstant, int frequency, int
            signalIntensity, DeviceType type){
        this(ssid, bssid, characteristics, null, channelWidthConstant, frequency, signalIntensity, type);
    }

    public Device(String ssid, String bssid, String characteristics, String manufacturer, Object channelWidth, int
            frequency, int signalIntensity, DeviceType type){
        //Null check to avoid violating the NOT-NULL restriction
        //Usually we enter this check because we have found a bluetooth device with no friendly name
        if(ssid == null)
            this.ssid = "nameNotDefined";
        else
            this.ssid = ssid;
        this.bssid = bssid;
        this.characteristics = characteristics;
        this.type = type;

        if(this.type.equals(DeviceType.WIFI)){
            if(channelWidth.getClass().equals(Integer.class)){
                int constant = (Integer) channelWidth;

                if(constant != 9999)
                    this.channelWidth = selectChannelWidthFromConstant(constant);
            }else if(channelWidth.getClass().equals(String.class)){
                this.channelWidth = (String) channelWidth;
            }


            if(frequency != 0)
                this.frequency = frequency;

            if(signalIntensity != 9999)
                this.signalIntensity = signalIntensity;
        }

        if(manufacturer != null)
            this.manufacturer = manufacturer;


    }

    /**
     * Obtains the manufacturer of the ethernet/bluetooth card based on the MAC address of the device.
     * This method is only called when the device must be associated with a particular location (i.e.
     * the device hasn't been discovered yet) in order to minimize the number of requests sent to the
     * API server.
     *
     * @see <a href="http://www.macvendors.com/api"> API Documentation</a>
     * @see <a href="https://developer.android.com/training/volley/simple.html">Volley Documentation</a>
     */
    public void getManufacturerFromBssid(Activity mainActivity) {
        //Fix to avoid creating a requestQueue for each request (OutOfMemory error)
        if(Utils.queue == null)
            Utils.queue = Volley.newRequestQueue(mainActivity);

        final DatabaseHelper dbHelper = DatabaseHelper.getInstance(mainActivity);

        //http://api.macvendors.com/00:11:22:33:44:55
        String url = Utils.MANUFACTURER_REQUEST_URL + getBssid();

        //Request a response from the provided URL
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Get the manufacturer from the response string
                //Log.d(TAG, "MANUFACTURER RECEIVED SUCCESSFULLY: " + manufacturer);
                setManufacturer(response, dbHelper);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Code 404 was received: no manufacturer found
                //Log.d(TAG, "MANUFACTURER NOT FOUND");
                setManufacturer(Utils.MANUFACTURER_NOT_FOUND, dbHelper);
            }
        });
        //Add the request to the queue
        Utils.queue.add(request);
    }

    private String selectChannelWidthFromConstant(int constant){
        String result = null;

        switch(constant){
            case ScanResult.CHANNEL_WIDTH_20MHZ:
                result = "20 MHz";
                break;

            case ScanResult.CHANNEL_WIDTH_40MHZ:
                result = "40 MHz";
                break;

            case ScanResult.CHANNEL_WIDTH_80MHZ:
                result = "80 MHz";
                break;

            case ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ:
                result = "160 MHz PLUS";
                break;

            default:
                break;
        }

        return result;
    }

    /**
     * Two devices are the same device if and only if their MAC addresses are equal
     * @param other the device to be compared to this device.
     * @return  whether or not both devices are equal.
     */
    @Override
    public boolean equals(Object other){
        boolean isEqual = false;
        if (other instanceof Device && this.getBssid().equals(((Device) other).getBssid()))
            isEqual = true;

        return isEqual;
    }

    @Override
    public int hashCode(){
        int hash = getBssid().hashCode();
        return hash;
    }

    //Getters and setters

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(String characteristics) {
        this.characteristics = characteristics;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    private void setManufacturer(String response, DatabaseHelper dbHelper) {
        this.manufacturer = response;
        dbHelper.updateDeviceWithManufacturer(this);
    }

    public void setManufacturer(String manufacturer){
        this.manufacturer = manufacturer;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public String getChannelWidth() {
        return channelWidth;
    }

    public void setChannelWidth(String channelWidth) {
        this.channelWidth = channelWidth;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public double getSignalIntensity() {
        return signalIntensity;
    }

    public void setSignalIntensity(int signalIntensity) {
        this.signalIntensity = signalIntensity;
    }
}

