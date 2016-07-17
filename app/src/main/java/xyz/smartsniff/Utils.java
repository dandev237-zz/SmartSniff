package xyz.smartsniff;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.LocationRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class with methods used across the app.
 *
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 30/06/2016
 */
public class Utils {

    public static final String PREFS_NAME = "SmartSniffPref";
    public static final String PREF_GPS_PRIORITY = "GPS Priority";
    public static final String PREF_SCAN_INTERVAL = "Scan Interval";
    public static final int SCAN_INTERVAL_DEFAULT = 3000;
    public static final int GPS_PRIORITY_DEFAULT = LocationRequest.PRIORITY_HIGH_ACCURACY;

    private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";


    /**
     * Given a date, this function returns the date with the format "dd/MM/yyyy HH:mm:ss"
     * @param date  The date to format.
     * @return      The date, formatted.
     */
    public static String formatDate(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String formattedDate = dateFormat.format(date);

        return formattedDate;
    }
}
