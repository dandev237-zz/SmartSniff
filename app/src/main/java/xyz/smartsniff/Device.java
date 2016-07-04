package xyz.smartsniff;

/**
 * Model class to represent devices.
 *
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 30/06/2016
 */
public class Device {

    private String ssid, bssid, characteristics;
    private DeviceType type;

    public Device(String ssid, String bssid, String characteristics, DeviceType type){
        this.ssid = ssid;
        this.bssid = bssid;
        this.characteristics = characteristics;
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

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }
}

