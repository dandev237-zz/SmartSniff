package xyz.smartsniff;

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
}
