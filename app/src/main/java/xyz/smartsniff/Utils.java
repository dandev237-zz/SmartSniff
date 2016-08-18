package xyz.smartsniff;

import com.android.volley.RequestQueue;
import com.google.android.gms.location.LocationRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Class with methods used across the app.
 *
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 30/06/2016
 */
public class Utils {

    public static final String MANUFACTURER_REQUEST_URL = "http://api.macvendors.com/";
    public static final String MANUFACTURER_NOT_FOUND = "NotFound";
    public static final String PREFS_NAME = "SmartSniffPref";
    public static final String PREF_GPS_PRIORITY = "GPS Priority";
    public static final String PREF_SCAN_INTERVAL = "Scan Interval";
    public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final int SCAN_INTERVAL_DEFAULT = 3000;
    public static final int GPS_PRIORITY_DEFAULT = LocationRequest.PRIORITY_HIGH_ACCURACY;
    public static final int HEATMAP_RADIUS = 40;
    public static final int REQUEST_ENABLE_INTENT = 123;
    public static final float ZOOM_LEVEL = 17.0f;
    public static final double HEATMAP_OPACITY = 0.6;

    public static final Gson gson = new GsonBuilder().setDateFormat(Utils.DATE_FORMAT).disableHtmlEscaping().create();
    public static RequestQueue queue;

    /**
     * Given a date, this function returns the date with the format "dd/MM/yyyy HH:mm:ss"
     * @param date  The date to format.
     * @return      The date, formatted.
     */
    public static String formatDate(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        String formattedDate = dateFormat.format(date);

        return formattedDate;
    }

    /**
     * This method does the inverse operation of 'formatDate'.
     * Given a formatted date (string), returns the same date contained in a Date object.
     * @param stringDate    The string to parse.
     * @return              The date, in a Date object.
     */
    public static Date reverseFormatDate(String stringDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        Date date = null;
        try {
            date = dateFormat.parse(stringDate);
        } catch (ParseException | NullPointerException e) {
            //e.printStackTrace();
            date = new Date();
        }

        return date;
    }

    /**
     * This method returns the mac address of the Android device executing the application.
     * Since Android 6.0 (Marshmallow), it is no longer possible to use "getConnectionInfo().getMacAddress()"
     * to obtain the mac address. This workaround solves this problem.
     *
     * Reference: http://robinhenniges.com/en/android6-get-mac-address-programmatically
     * This piece of code is freely available to use for any user (see reference for more license details)
     * @return  The Mac Address of the Android device executing the application.
     */
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }
}
