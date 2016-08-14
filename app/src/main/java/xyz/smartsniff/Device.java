package xyz.smartsniff;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Model class to represent devices.
 *
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 30/06/2016
 */
public class Device {
    private String ssid, bssid, characteristics, manufacturer;
    private DeviceType type;

    public Device(String ssid, String bssid, String characteristics, DeviceType type){
        this(ssid, bssid, characteristics, null, type);
    }

    public Device(String ssid, String bssid, String characteristics, String manufacturer, DeviceType type){
        this.ssid = ssid;
        this.bssid = bssid;
        this.characteristics = characteristics;

        if(manufacturer != null)
            this.manufacturer = manufacturer;

        this.type = type;
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

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }
}

