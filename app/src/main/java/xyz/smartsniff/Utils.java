package xyz.smartsniff;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import java.util.Date;

/**
 * Class with methods used across the app.
 *
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 30/06/2016
 */
public class Utils {

    private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private static final int PERMISSIONS_REQUEST = 1111;

    /**
     * Given a date, this function returns the date with the format "dd/MM/yyyy HH:mm:ss"
     * @param date  The date to format.
     * @return      The date, formatted.
     */
    public static String formatDate(Date date){
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String formattedDate = df.format(DATE_FORMAT, date).toString();

        return formattedDate;
    }

    public static void handlePermissions(Context context, Activity activity) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest
                .permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission
                    .ACCESS_FINE_LOCATION,
                    android.Manifest.permission
                            .ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST);
        }
    }
}
